package ru.litvinov.getPostParser.notariatParser.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.litvinov.getPostParser.notariatParser.models.Client;
import ru.litvinov.getPostParser.notariatParser.models.result.GetResult;
import ru.litvinov.getPostParser.notariatParser.models.result.Record;
import ru.litvinov.getPostParser.utils.fileUtils.FileUtils;
import ru.litvinov.getPostParser.utils.jsonUtils.JsonUtils;
import ru.litvinov.getPostParser.utils.requestUtils.RequestUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.HttpCookie;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CoreImpl implements Core {

    @Value("https://notariat.ru/api/probate-cases/eis-proxy")
    private String url;
    @Autowired
    @Qualifier("headers")
    private Map<String, String> headers;
    @Value("${notariat.filePath}")
    private String inputFile;
    private String outputFile = "notariatOutput.txt";
    private List<Client> clients = new ArrayList<>();

    public CoreImpl() {
    }

    ;

    @PostConstruct
    public void init() {
        if (!Files.exists(Paths.get(inputFile))) {
            System.out.println("отсутствует файл завершаем работу программы");
            System.exit(0);
        } else {
            try {
                loadClients();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        getCookies();
    }

    public void processor() {
        System.out.println("Парсим сайт");
        int counter = 0;
        for (Client client : clients) {
            String resultString = sendPost(JsonUtils.objectToJson(client));
            GetResult result = (GetResult) JsonUtils.jsonToObject(resultString, GetResult.class);
            FileUtils.writeFile(resultProcessor(client, result), outputFile, true);
            System.out.println("Загружено " + ++counter + " из " + clients.size());
        }
        System.out.println("Загрузка завершена");
    }

    public void getCookies() {
        //Загружаем куки и дописываем хедеры при иницииализации
        System.out.print("грузим куки");
        List<HttpCookie> cookies = RequestUtils.getCookies(url);
        String token = "";
        String cookieString = "";
        StringBuilder builder = new StringBuilder();
        if (!cookies.isEmpty()) {
            for (HttpCookie cookie : cookies) {
                String tmp = cookie.toString();
                if (tmp.startsWith("fnc_csrftoken")) {
                    token = tmp.split("=")[1];
                }
                cookieString += tmp + "; ";
            }
            //Если в куках был токен
            if (token.length() > 0) {
                System.out.println("найдены новые куки перезаписываем");
                headers.put("X-CSRFToken", token);
                headers.put("Cookie", cookieString.substring(0, cookieString.length() - 2));
                //перезаписываем проперти
                String newProperties = "notariat.Cookie = " + cookieString + "\n" + "notariat.X-CSRFToken = " + token + "\n" + "notariat.filePath = " + inputFile;
                FileUtils.writeFile(newProperties, "src\\main\\resources\\notariat.properties", false);
            }
            System.out.println(" - ок");
            try {
                System.out.println("Пауза 3 секунды");
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadClients() throws IOException {
        System.out.print("Загружаем клиентов из файла");
        List<String> list = Files.readAllLines(Paths.get(inputFile));
        for (String s : list) {
            String[] tmp = s.split(";");
            Client client = new Client();
            client.setName(tmp[0]);
            client.setBirth_date(tmp[1].split("\\.")[2] + tmp[1].split("\\.")[1] + tmp[1].split("\\.")[0]);
            client.setDeath_date(tmp[2].split("\\.")[2] + tmp[2].split("\\.")[1] + tmp[2].split("\\.")[0]);
            clients.add(client);
        }
        System.out.println(" - ок");
        //System.out.println(clients);
    }

    @Override
    public String sendPost(String body) {
        String s = "";
        try {
            s = RequestUtils.postRequest(url, body, headers);
        } catch (Exception e) {
            if (e.getMessage().contains("Service Temporarily Unavailable")) {
                try {
                    Thread.sleep(2000);
                    s = sendPost(body);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            } else {
                e.printStackTrace();
            }
        }
        return s;
    }

    //Шапка для будущего файла
    //ФИО~ДР~Дата смерти~Количество записей~deathActDate~deathActNumber~address~caseIndex~caseNumber~caseDate~caseCloseDate~notaryID~notaryName~notaryStatus~districtName~contactName~contactAddress~contactPhone~chamberID~chamberName~caseID~caseIDDate
    public String resultProcessor(Client client, GetResult result) {
        StringBuilder sb = new StringBuilder();
        StringBuilder resultSb = new StringBuilder();
        sb.append(client.getName()).append("~")
                .append(client.getBirth_date().substring(0, 4) + "." + client.getBirth_date().substring(4, 6) + "." + client.getBirth_date().substring(6, 8)).append("~")
                .append(client.getDeath_date().substring(0, 4) + "." + client.getDeath_date().substring(4, 6) + "." + client.getDeath_date().substring(6, 8)).append("~")
                .append(result.getCount()).append("~");
        if (result.getCount() == 0L) {
            return sb.toString() + "информация не найдена \n";
        } else {
            Record[] records = result.getRecords();
            for (Record record : records) {
                StringBuilder secondSb = new StringBuilder();
                secondSb.append(record.getDeathActDate()).append("~")
                        .append(record.getDeathActNumber()).append("~")
                        .append(record.getAddress()).append("~")
                        .append(record.getCaseIndex()).append("~")
                        .append(record.getCaseNumber()).append("~")
                        .append(record.getCaseDate()).append("~")
                        .append(record.getCaseCloseDate()).append("~")
                        .append(record.getNotaryID()).append("~")
                        .append(record.getNotaryName()).append("~")
                        .append(record.getNotaryStatus()).append("~")
                        .append(record.getDistrictName()).append("~")
                        .append(record.getContactName()).append("~")
                        .append(record.getContactAddress()).append("~")
                        .append(record.getContactPhone()).append("~")
                        .append(record.getChamberID()).append("~")
                        .append(record.getChamberName()).append("~")
                        .append(record.getCaseID()).append("~")
                        .append(record.getCaseIDDate());
                resultSb.append(sb.toString() + secondSb + "\n");
            }
        }
        return resultSb.toString();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map getHeaders() {
        return headers;
    }

    public void setHeaders(Map headers) {
        this.headers = headers;
    }
}
