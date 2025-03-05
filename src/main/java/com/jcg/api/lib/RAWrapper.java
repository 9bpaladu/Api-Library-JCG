package com.jcg.api.lib;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * This is Rest assured Wrapper class which provides capabilities for webservice execution
 */
@SuppressWarnings("unchecked")
public class RAWrapper {

    private static RequestSpecification request;
    private static String jsonPayload;
    private static String dataFilePath = "/data/" + System.getProperty("env").toLowerCase() + "/";

    /**
     * This method returns json payload.
     *
     * @return
     */
    protected String getUpdatedPayloadFile() {
        return jsonPayload;
    }

    /**
     * This method sets json pay load.
     *
     * @param jsonPayLoad
     */
    protected void setUpdatedPayloadFile(String jsonPayLoad) {
        RAWrapper.jsonPayload = jsonPayLoad;
    }

    /**
     * Constructor for wrapper class
     */
    public RAWrapper() {
        RequestSpecBuilder builder = new RequestSpecBuilder();
        builder.setContentType(ContentType.JSON);
        RequestSpecification requestSpec = builder.build();
        request = RestAssured.given().spec(requestSpec);
    }

    /**
     * This method takes the name of data file and reads corresponding file and returns an object.
     *
     * @param arg1
     * @return
     */
    public static Object getDataFile(String arg1) {
        URL url = RAWrapper.class.getResource(dataFilePath);
        String extn = "";
        List<File> filesLst = null;
        Object obj = null;
        File file = null;
        if (url != null) {
            filesLst = getFilesFromDirectory(url, arg1);
        }

        if (CollectionUtils.isEmpty(filesLst)) {
            url = RAWrapper.class.getResource("/data/");
            filesLst = getFilesFromDirectory(url, arg1);
        }
        if (!CollectionUtils.isEmpty(filesLst)) {
            file = (File) filesLst.get(0);
            extn = FilenameUtils.getExtension(file.getName());
        }
        if (file != null) {
            String fileName = file.getName();
            if (fileName.toLowerCase().endsWith(".json")) {
                JSONParser jsonParser = new JSONParser();
                try (FileReader reader = new FileReader(file)) {
                    obj = jsonParser.parse(reader);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            if (fileName.toLowerCase().endsWith(".xml")) {
                try {
                    obj = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return obj;
    }
    


    private static List getFilesFromDirectory(URL url, String filename) {
        File[] matches;
        List<File> matchesReturnLst = new ArrayList<>();
        File dir;
        String dirExt = "";
        if (filename.contains("/")) {
            int index = filename.lastIndexOf("/");
            dirExt = filename.substring(0, index + 1);
            filename = filename.substring(index + 1);
        }

        File myDir = new File(url.getPath() );
        Collection<File> filesCollection = FileUtils.listFiles(myDir, new String[] {"json"}, true);
        matches = filesCollection.toArray(new File[0]);
        if(matches!=null) {
        for (File f : matches) {
            if (f.isFile()) {
                int index = f.getName().lastIndexOf(".");
                String tempFileName = f.getName().substring(0, index);
                if (tempFileName.equalsIgnoreCase(filename)) {
                    matchesReturnLst.add(f);
                }
            }
        }
    }
        return matchesReturnLst;
    }


    /**
     * This method takes the name of data file and reads corresponding file and returns an object.
     *
     * @param arg1
     * @return
     */
    public static JSONArray getDataFilearray(String arg1) throws Throwable {
        URL url = null;
        url = RAWrapper.class.getResource(dataFilePath + arg1 + ".json");
        if (url == null) {
            url = url = RAWrapper.class.getResource("/data/" + arg1 + ".json");
        }
        if (url == null) {
            System.out.println("File Not Found !! " + arg1);
            return null;
        }
        JSONParser jsonParser = new JSONParser();
        Object obj = null;
        try (FileReader reader = new FileReader(url.getPath())) {
            obj = jsonParser.parse(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (JSONArray) obj;
    }

    /**
     * This method accepts test data object and returns headers after replacing name with value if any.
     *
     * @param testdata
     * @param name
     * @param value
     * @return
     */
    public static Headers getHeaders(Object testdata, String name, String value) {
        JSONObject jstr = parseJson(testdata, "Headers");
        Set<String> hname = jstr.keySet();
        Iterator<String> it = hname.iterator();
        List<Header> list = new ArrayList<>();
        String val = null;
        while (it.hasNext()) {
            String str = it.next();
            val = jstr.get(str).toString();
            if (name != null && value != null) {
                val = val.replaceAll(name, value);
            }
            list.add(new Header(str, val));

        }
        Headers header = new Headers(list);
        return header;
    }



    /**
     * This method parse json object at the given string.
     *
     * @param jsondata
     * @param str
     * @return
     */
    public static JSONObject parseJson(Object jsondata, String str) {

        JSONArray ja = (JSONArray) jsondata;
        JSONObject jobj = (JSONObject) ja.get(0);
        JSONObject jdata = ((JSONObject) jobj.get("Data"));
        JSONObject jstr = (JSONObject) jdata.get(str);
        return jstr;

    }

    /**
     * This method parse json array object at the given string.
     *
     * @param jsondata
     * @param str
     * @return
     */
    public static JSONArray parseJsonarray(Object jsondata, String str) {

        JSONArray ja = (JSONArray) jsondata;
        JSONObject jobj = (JSONObject) ja.get(0);
        JSONObject jdata = ((JSONObject) jobj.get("Data"));
        JSONArray jstr = (JSONArray) jdata.get(str);
        return jstr;

    }
    
    /**
     * This method parse json array object at the given string.
     *
     * @param jsondata
     * @param str
     * @return
     */
    public static JSONArray parseJsonArrayForKey(Object jsondata, String str) {

    	JSONArray ja = (JSONArray) jsondata;
    	JSONObject jobj = null;
    	JSONArray jstr = null;
    	for( int i=0;i< ja.size(); i++) {
    		jobj = (JSONObject) ja.get(i);
    		if(jobj.containsKey(str)) {
    			jstr = (JSONArray) jobj.get(str);
    			break;
    		}
    	}
        return jstr;

    }


    /**
     * This method parse json array object at the given string.
     *
     * @param jsondata
     * @param str
     * @return
     */
    public static Object parseJsonNew(Object jsondata, String str) {
        JSONArray ja = (JSONArray) jsondata;
        JSONObject jobj = (JSONObject) ja.get(0);
        JSONObject jdata = ((JSONObject) jobj.get("Data"));
        if (jdata.get(str) instanceof JSONArray) {
            return (JSONArray) jdata.get(str);
        } else if (jdata.get(str) instanceof JSONObject) {
            return (JSONObject) jdata.get(str);
        } else if (jdata.get(str) instanceof java.lang.String) {
            return  jdata.get(str);
        }else {
            return null;
        }
    }


    public static Object getJsonData(String resourceFile) {

        URL url = null;
        url = RAWrapper.class.getResource(dataFilePath + resourceFile + ".json");
        if (url == null) {
            url = url = RAWrapper.class.getResource("/data/" + resourceFile + ".json");
        }
        if (url == null) {
            System.out.println("getJsonData File Not Found !! " + resourceFile);
            return null;
        }
        JSONParser jsonParser = new JSONParser();
        Object obj = null;
        try (FileReader reader = new FileReader(url.getPath())) {
            obj = jsonParser.parse(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static JSONObject parseWebJsonForWebData(Object jsondata, String testCaseName) {
        JSONObject jo = (JSONObject) jsondata;
        JSONArray ja = (JSONArray) jo.get(testCaseName);
        if (ja == null) {
            System.out.println("Testcase Id not found in TestData Json file!! " + testCaseName);
        }
        JSONObject jobj = (JSONObject) ja.get(0);
        return ((JSONObject) jobj.get("InputData"));
    }

    public static String readFileAsString(String fileName) throws Exception {
        String data = "";
        data = new String(Files.readAllBytes(Paths.get(fileName)));
        return data;
    }


}