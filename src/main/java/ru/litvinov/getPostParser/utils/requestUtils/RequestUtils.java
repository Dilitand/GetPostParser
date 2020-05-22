package ru.litvinov.getPostParser.utils.requestUtils;

import com.sun.corba.se.impl.presentation.rmi.ExceptionHandler;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.Map;

public class RequestUtils {

    //get
    public static String getRequest(String query, Map headers) throws Exception {
        HttpURLConnection connection = null;
        StringBuilder builder = new StringBuilder();
        try {
            connection = (HttpURLConnection) new URL(query).openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("GET"); //Задаем тип запроса
            connection.setConnectTimeout(1500); //задаем таймауты
            connection.setReadTimeout(1500);

            //Пример хедеров
            //connection.setRequestProperty("Content-Language", "en-US"); //пример хеадеров
            //connection.setRequestProperty("Cookie","JSESSIONID=412364AA14H155125D125XSW1512");
            if (!headers.isEmpty()) {
                Iterator<Map.Entry<String, String>> iterator = headers.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String> entry = iterator.next();
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            if (HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
                //получаем ответ
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "Windows-1251"));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
                throw new Exception(connection.getResponseCode() + "\n" + connection.getResponseMessage() + "\n" + query);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    //post
    public static String postRequest(String myUrl, String body, Map<String, String> headers) throws Exception {
        try {
            URL url = new URL(myUrl); // here is your URL path
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(); //открываем коннект
            connection.setReadTimeout(15000 /* milliseconds */);
            connection.setConnectTimeout(15000 /* milliseconds */);
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //наполняем хэдеры
            //пример создания хэдеров:
            //conn.setRequestProperty("Content-Type", "application/json; utf-8");
            //conn.setRequestProperty("Accept", "application/json");
            if (!headers.isEmpty()) {
                Iterator<Map.Entry<String, String>> iterator = headers.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String> entry = iterator.next();
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            //грузим тело запроса
            try (OutputStream os = connection.getOutputStream();
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));) {
                //byte[] input = body.getBytes("utf-8");
                //os.write(input, 0, input.length);
                writer.write(body);
                writer.flush();
            }

            //получаем ответ
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));) {
                    StringBuffer sb = new StringBuffer("");
                    String line = "";
                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                    }
                    return sb.toString();
                }
            } else {
                throw new Exception(connection.getResponseCode() + "\n" + connection.getResponseMessage() + "\n" + body);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Преобразует строку в специальный формат для отправки url
     * @param s
     * @return
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    public static String encodeString(String s) throws MalformedURLException, URISyntaxException {
        URL url4 = new URL(s);
        URI uri = new URI(url4.getProtocol(), url4.getUserInfo(), IDN.toASCII(url4.getHost()), url4.getPort(), url4.getPath(), url4.getQuery(), url4.getRef());
        return uri.toASCIIString();
    }

}
