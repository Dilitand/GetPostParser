package ru.litvinov.getPostParser.FSSPparser;

import ru.litvinov.getPostParser.FSSPparser.core.cache.CacheWorkerIp;
import ru.litvinov.getPostParser.FSSPparser.core.cores.CoreFssp;
import ru.litvinov.getPostParser.FSSPparser.core.cores.CoreFsspIp;
import ru.litvinov.getPostParser.FSSPparser.core.logic.LogicIp;

import java.util.Scanner;

public class MainFssp {

    static String token = "ZYrCH4sHnb89";
    static String token2 = "2oFwTlKyEa92";

    public static void main(String[] args) throws Exception {
        proceesing();
    }

    public static void proceesing() throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите 1 для отправки запросов с ИП, введите 2 для получения ответов из ФССП по ранее отправленным ИП");
        if (!scanner.hasNextInt()){
            System.out.println("Введите число");
            proceesing();
        } else {
            Integer i = scanner.nextInt();
            if (i == 1) {
                runRequest();
            } else if (i == 2){
                runResult();
            } else {
                System.out.println("Введите 1 или 2");
                proceesing();
            }
        }
        System.out.println("Загрузка завершена");
    }

    public static void runRequest() throws Exception {
        CoreFssp core = new CoreFsspIp(token2, new LogicIp());
        core.setInputFile("inputfile.txt");
        core.setCacheWork(new CacheWorkerIp());
        core.getCacheWork().init();
        core.sendPosts();
    }

    public static void runResult() throws Exception {
        CoreFssp coreFssp = new CoreFsspIp(token2,new LogicIp());
        coreFssp.getResults();
    }
}
