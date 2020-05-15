package ru.litvinov.getPostParser.FSSPparser.core;

import ru.litvinov.getPostParser.FSSPparser.models.getResponse.GetResponse;
import ru.litvinov.getPostParser.FSSPparser.models.postRequest.PostRequest;
import ru.litvinov.getPostParser.utils.serialize.SerializationImpl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class CacheWork implements Serializable {

    transient String casheFile;
    Map cacheMap = new HashMap();

    public CacheWork() {
    }

    public CacheWork(String casheFile) {
        this.casheFile = casheFile;
    }

    abstract public void writePostRequestResult(PostRequest postRequest, GetResponse getResponse);

    abstract public void init();

    abstract public void saveCasheToFile(String outputFile);

    public void saveCache() {
        SerializationImpl.saveObject(this, casheFile);
    }

    public void printCache() {
        Iterator iterator = cacheMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
    }


    public String getCasheFile() {
        return casheFile;
    }

    public void setCasheFile(String casheFile) {
        this.casheFile = casheFile;
    }

    public Map getCacheMap() {
        return cacheMap;
    }

    public void setCacheMap(Map<String, GetResponse> cacheMap) {
        this.cacheMap = cacheMap;
    }

}
