package com.jcg.api.lib;

import com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.config.RedirectConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.config.SSLConfig;
import io.restassured.http.Cookies;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.Assert;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import groovy.util.Eval;

import static com.jcg.api.lib.RAWrapper.parseJson;


public class APIlib {

    private static Response response;
    private static String env;
    private static String response_body;
    private static String request_body;
    private static String response_header;
    private static RequestSpecification request = RestAssured.given().config(RestAssured.config().sslConfig( new SSLConfig().relaxedHTTPSValidation()));
    private static String url;
    private static Object testdata;
    private static Hashtable table = new Hashtable();
    private static final Logger logger = LogManager.getLogger(APIlib.class);
    public static HashMap<String, Object> storeData = new HashMap<String, Object>();
    public static HashMap<String, Object> storeDataRequest = new HashMap<String, Object>();
    private static List<Preprocessor> preprocessors = new ArrayList<Preprocessor>();
    public static List<Integer> errorCodes=Arrays.asList(409,500,429,404,422,504);
    private static boolean secretFlag;
    private static Cookies cookies;


    public void SendGetRequest() throws Throwable {
        if(!secretFlag){
            ExtentCucumberAdapter.addTestStepLog(request.log().all().toString());
        }
        RestAssured.config = RestAssured.config().redirect(RedirectConfig.redirectConfig().followRedirects(false));
        request = RestAssured.given().config(RestAssured.config().sslConfig( new SSLConfig().relaxedHTTPSValidation()));
        response = request.relaxedHTTPSValidation().urlEncodingEnabled(false).get(url);
        retryFailedAPI("get");
        RestAssured.config = RestAssured.config().redirect(RedirectConfig.redirectConfig().followRedirects(true));
    }

    /**
     * This method sends a Put Request to specified end point.
     *
     * @throws Throwable
     */
    public void SendPutRequest() throws Throwable {
        if(!secretFlag){
            ExtentCucumberAdapter.addTestStepLog(request.log().all().toString());
        }
        response = request.relaxedHTTPSValidation().put(url);
        retryFailedAPI("put");
    }

    /**
     * This method sends post request to specified end point.
     *
     * @throws Throwable
     */

    public void SendPostRequest() throws Throwable {
        if(!secretFlag){
            ExtentCucumberAdapter.addTestStepLog(request.log().all().toString());
        }
        response = request.relaxedHTTPSValidation().urlEncodingEnabled(false).post(url);
        cookies = response.getDetailedCookies();
        System.out.println("cookies ::" + cookies);
        retryFailedAPI("post");
    }
    /**
     * This method sends post request to specified end point.
     *
     * @throws Throwable
     */

    public void SendPostRequest(String url) throws Throwable {
        if(!secretFlag){
            ExtentCucumberAdapter.addTestStepLog(request.log().all().toString());
        }
        response = request.relaxedHTTPSValidation().urlEncodingEnabled(false).post(url);
        cookies = response.getDetailedCookies();
        System.out.println(response.getBody().asString());
        response_body = response.body().prettyPrint();
        if(response.getStatusCode()==200){
            System.out.println("Price API -Sucessful");
        }else{
            System.out.println("Price API - Not Sucessful");
        }

    }
    private static Properties properties;
    public String getApiProperty(String propKey){
        InputStream iStream = null;
        String propertyValue="";
        properties = new Properties();
        try {
            iStream = new FileInputStream("src/main/resources/api/"+env+"/api.properties");
            properties.load(iStream);
            propertyValue=properties.getProperty(propKey);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally {
            try {
                if(iStream != null){
                    iStream.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return propertyValue;
    }



    public void retryFailedAPI(String method) throws Throwable {
        if(!(getApiProperty("apiRetryCount")==null)) {
            int statusCode = response.getStatusCode();
            int apiRetryCount = Integer.parseInt(getApiProperty("apiRetryCount"));
            if (errorCodes.contains(response.getStatusCode())) {
                logReport("API failed with errorCode : " + response.getStatusCode());
                System.out.println("API failed with errorCode : " + response.getStatusCode());
                for (int i = 0; i < apiRetryCount; i++) {
                    logReport("API failed retry attempt : " + (i + 1) + "");
                    Wait(5);
                    System.out.println("Retring API call for " + (i + 1) + " time");
                    if (method.contains("post")) {
                        response = request.relaxedHTTPSValidation().post(url);
                    } else if (method.contains("get")) {
                        response = request.relaxedHTTPSValidation().get(url);
                    } else if (method.contains("put")) {
                        response = request.relaxedHTTPSValidation().put(url);
                    } else if (method.contains("delete")) {
                        response = request.relaxedHTTPSValidation().delete(url);
                    } else if (method.contains("patch")) {
                        response = request.relaxedHTTPSValidation().patch(url);
                    }
                    if (!errorCodes.contains(response.getStatusCode())) {
                        break;
                    } else {
                        logReport("API failed with errorCode : " + response.getStatusCode());
                        System.out.println("API failed with errorCode : " + response.getStatusCode());
                    }
                }
            }
        }

    }
    /**
     * This method sends a Delete Request
     *
     * @throws Throwable
     */

    public void SendDeleteRequest() throws Throwable {
        if(!secretFlag){
            ExtentCucumberAdapter.addTestStepLog(request.log().all().toString());
        }
        response = request.relaxedHTTPSValidation().delete(url);
        retryFailedAPI("delete");
    }

    /**
     * This method sends a Patch Request
     *
     * @throws Throwable
     */

    public void SendPatchRequest() throws Throwable {
        if(!secretFlag){
            ExtentCucumberAdapter.addTestStepLog(request.log().all().toString());
        }
        response = request.relaxedHTTPSValidation().patch(url);
        retryFailedAPI("patch");
    }



    /**
     * This method validates status code as specified in test data
     *
     * @throws Throwable
     */

    public void CheckStatusCode() throws Throwable {
        System.out.println("All Response Headers" + response.getHeaders().toString());
        if (testdata instanceof java.lang.String) {
            String responseCode = ((String) testdata).substring(((String) testdata).indexOf("<ResponseCode>"), ((String) testdata).indexOf("</ResponseCode>") + 15);
            if (!StringUtils.isBlank(responseCode)) {
                String statusInTestData = parseData(new XmlPath(responseCode).getString("ResponseCode.status"));
                assertResult("Equal", response.getStatusCode(), statusInTestData);
            }
        } else {
            JSONObject jresponsecd = parseJson(testdata, "ResponseCode");
            if (jresponsecd.get("status") != null) {
                String statusInTestData = jresponsecd.get("status").toString();
                statusInTestData = parseData(statusInTestData);
                assertResult("Equal", response.getStatusCode(), statusInTestData);
            }

        }
        request = RestAssured.given().relaxedHTTPSValidation();
    }

    public void CheckStatusCode(String dataFileName, String testCaseName, String payloadFile, String payloadJsonKey) throws Throwable {
        System.out.println(response.getHeaders().toString());
        int respStatusCode = response.getStatusCode();
        if (testdata instanceof java.lang.String) {
            String responseCode = ((String) testdata).substring(((String) testdata).indexOf("<ResponseCode>"), ((String) testdata).indexOf("</ResponseCode>") + 15);
            if (!StringUtils.isBlank(responseCode)) {
                String statusInTestData = parseData(new XmlPath(responseCode).getString("ResponseCode.status"), dataFileName, testCaseName);
                assertResult("Equal", respStatusCode, statusInTestData);
            }
        } else {
            testdata = RAWrapper.getDataFile(payloadFile);
            if(payloadJsonKey!=null) {
                testdata = RAWrapper.parseJsonArrayForKey(testdata, payloadJsonKey);
            }
            JSONObject jresponsecd = parseJson(testdata, "ResponseCode");
            if (jresponsecd.get("status") != null) {
                String statusInTestData = jresponsecd.get("status").toString();
                statusInTestData = parseData(statusInTestData, dataFileName, testCaseName);
                if(!StringUtils.isEmpty(statusInTestData.trim()) && statusInTestData.contains(",")){
                    String[] split = statusInTestData.split(",");
                    boolean statCodeMatchFound = false;
                    for(String statCodeOne : split){
                        int intStatCodeOne = Integer.valueOf(statCodeOne);
                        if(respStatusCode == intStatCodeOne){
                            statCodeMatchFound = true;
                            break;
                        }
                    }
                    assertResult("True",statCodeMatchFound,"True");
                }else {
                    assertResult("Equal", respStatusCode, statusInTestData);
                }
                request = RestAssured.given().relaxedHTTPSValidation();
            }
        }
    }



    /**
     * This method validates and prints status code line
     *
     * @throws Throwable
     */
    public void CheckStatusCodeLine(String dataFileName, String testCaseName, String payloadFile, String payloadJsonKey) throws Throwable {
        if (testdata instanceof java.lang.String) {
            String responseCode = ((String) testdata).substring(((String) testdata).indexOf("<ResponseCode>"), ((String) testdata).indexOf("</ResponseCode>") + 15);
            if (!StringUtils.isBlank(responseCode)) {
                System.out.println("responseCode ::" + responseCode);
                String statusLineInTestData = parseData(new XmlPath(responseCode).getString("ResponseCode.statusLine"), dataFileName, testCaseName);
                assertResult("Equal", response.getStatusLine(), statusLineInTestData);
            }
        } else {
            testdata = RAWrapper.getDataFile(payloadFile);
            if(payloadJsonKey!=null) {
                testdata = RAWrapper.parseJsonArrayForKey(testdata, payloadJsonKey);
            }
            JSONObject jresponsecd = parseJson(testdata, "ResponseCode");

            if (jresponsecd.get("statusLine") != null) {
                System.out.println( "here ");
                String statusLineInTestData = jresponsecd.get("statusLine").toString();
                System.out.println("statusLineInTestData " + statusLineInTestData);
                statusLineInTestData = parseData(statusLineInTestData, dataFileName, testCaseName);
                System.out.println("statusLineInTestData2 " + statusLineInTestData);

                assertResult("Equal", response.getStatusLine(), statusLineInTestData);

            } else {
                logger.info("status line not found in payload file");
            }
        }
        request = RestAssured.given().relaxedHTTPSValidation();
    }

    public void PrintResponseHeaders() {
        Map<String, Object> Headers = new HashMap<>();
        Headers allHeaders = response.headers();
        allHeaders.forEach(entry -> {
            logger.info("Key : " + entry.getName() + "  Value : " + entry.getValue());
        });
    }

    /**
     * This method validates and prints status code line
     *
     * @throws Throwable
     */
    public void CheckStatusCodeLine() throws Throwable {
        if (testdata instanceof java.lang.String) {
            String responseCode = ((String) testdata).substring(((String) testdata).indexOf("<ResponseCode>"), ((String) testdata).indexOf("</ResponseCode>") + 15);
            if (!StringUtils.isBlank(responseCode)) {
                System.out.println("responseCode ::" + responseCode);

                XmlPath xpath = new XmlPath(responseCode);
                String statusLineInTestData = parseData(new XmlPath(responseCode).getString("ResponseCode.statusLine"));

//                String statusLineInTestData = parseData(xpath.get("ResponseCode.statusLine").toString());
                assertResult("Equal", response.getStatusLine(), statusLineInTestData);
            }
        } else {
            logger.info("Response Status Line is = " + response.getStatusLine());
            JSONObject jresponsecd = parseJson(testdata, "ResponseCode");
            if (jresponsecd.get("statusLine") != null) {
                assertResult("Equal", response.getStatusLine(), jresponsecd.get("statusLine").toString());

            } else {
                logger.info("status line not found in payload file");
            }
            logger.info("Response body=" + response.getBody().asString());
        }
        // Re initilizing the request
        request = RestAssured.given().relaxedHTTPSValidation();
    }


    /**
     * This method prints response body
     *
     * @throws Throwable
     */
    public void Printresponsebody() throws Throwable {
        response_body = response.body().prettyPrint();
        ExtentCucumberAdapter.addTestStepLog(response_body);
    }





    private boolean  isAllPropertiesMatched(String arg1, String arg2, String compareIndex) throws Exception {

        List<String> propertiesNameList = Arrays.asList(arg1.split("&"));
        List<String> expectedValuesList = Arrays.asList(arg2.split("&"));

        int noOfPropsToCompare = propertiesNameList.size();
        System.out.println("propertiesNameList::"+propertiesNameList);
        System.out.println("expectedValuesList::"+expectedValuesList);
        if(propertiesNameList.isEmpty() || expectedValuesList.isEmpty() || noOfPropsToCompare != expectedValuesList.size())
            Assert.fail("propertiesNameList or expectedValuesList is empty or both lists sizes are not matching");

        int propListCur =0;
        boolean prevAllMatches = false;
        while(noOfPropsToCompare > 0) {
            String propertyObj = propertiesNameList.get(propListCur);

            JsonPath jsonPathResp = response.jsonPath();
            String actualValue = jsonPathResp.getString(propertyObj);

            String expectedValue = expectedValuesList.get(propListCur);

            if(findTheMatch(actualValue, expectedValue, propertyObj, compareIndex)){
                prevAllMatches = true;
            }else{
                prevAllMatches = false;
            }
            System.out.println("prevAllMatches : " + prevAllMatches);
            if(!prevAllMatches || noOfPropsToCompare - 1 == propListCur){
                break ;
            }
            propListCur++;
        }
        return prevAllMatches;
    }

    private boolean findTheMatch(String actualValues, String expectedValues, String propertyKey, String compareIndex) throws Exception {
        System.out.println("expectedValues = " + expectedValues + " actualValues = " + actualValues + " propertyKey = " + propertyKey);
        if(actualValues == null || expectedValues == null)
            Assert.fail("actualValues or expected values are null");
        if (actualValues.contains("%")) {
            actualValues = parseData(actualValues);
        }
        if (expectedValues.contains("%")) {
            expectedValues = parseData(expectedValues);
        }
        System.out.println("Before comparision expectedValues = " + expectedValues + " actualValues = " + actualValues + " propertyKey = " + propertyKey);

        if(actualValues.startsWith("[") && actualValues.endsWith("]")){
            actualValues = actualValues.replaceAll("\\[", "");
            actualValues = actualValues.replaceAll("\\]", "");
            if(expectedValues.startsWith("[") && expectedValues.endsWith("]")) {
                expectedValues = expectedValues.replaceAll("\\[", "");
                expectedValues = expectedValues.replaceAll("\\]", "");
            }

            List<String> actualPropList = Arrays.asList(actualValues.split(","));
            List<String> expectedList = Arrays.asList(expectedValues.split(","));

            System.out.println("actualPropList = " + actualPropList + " expectedList = " + expectedList);
            System.out.println("compareIndex::"+compareIndex);
            if(compareIndex != null){
                Integer intExpectedCur = Integer.valueOf(compareIndex);
                intExpectedCur--;
                String actualValueStr = (actualPropList !=null && actualPropList.size() > 1) ? actualPropList.get(intExpectedCur) : actualPropList.get(0);

                System.out.println("Inside new if block expectedValues  "+expectedValues);
                System.out.println("Inside new if block actualValueStr   "+actualValueStr);

                if(expectedValues.replaceAll(" ","").contains(actualValueStr.replaceAll(" ","")) ){
                    return true;
                }
            }else {
                Set<String> matchedSet = actualPropList.stream()
                        .map(str -> str.replaceAll(" ", ""))
                        .filter(expectedList::contains)
                        .collect(Collectors.toSet());
                if (matchedSet.size() > 0) {
                    System.out.println("Assertion is true without any retry from List lengthExp....");
                    return true;
                }
            }
        }else {
            if (expectedValues.toLowerCase().contains(actualValues.toLowerCase())) {
                System.out.println("Assertion is true expectedValues Contains actualValues....");
                return true;
            }
        }
        return false;
    }

    /**
     * This method sends the current request until status meets arg2.
     *
     * @param arg1
     * @param arg2
     * @throws Throwable
     */
    public void PollUntilStatus(String arg1, String arg2) throws Throwable {
        System.out.println("in poll until status");
        JSONObject jresponsecd = parseJson(testdata, "ResponseCode");
        while (response.getStatusCode() == Integer.parseInt(jresponsecd.get("status").toString())) {
            System.out.println("inside first while loop");
            String sts = response.jsonPath().getString(arg1);
            System.out.println("sts=" + sts);
            Thread.sleep(10000);
            response = request.get(url);
            if (sts.equals(arg2))
                break;
            request = RestAssured.given().relaxedHTTPSValidation();
            System.out.println("sts=" + sts);
        }
    }



    /**
     * This method is used to wait for some tasks to complete.
     */

    public void Wait(int timer) {
        try {
            Long l = (long) (timer * 1000);
            Thread.sleep(l);
        } catch (Exception e) {
            logger.error("Wait Exception" + e.getStackTrace());
        }
    }


    /**
     * This method validates response body against testdata
     *
     * @throws Throwable
     */
    public void ValidateResponse(String propname, String emsg, String condition, String testDataFile, String testCaseName) throws Throwable {
        if (emsg.contains("%")) {
            emsg = parseData(emsg, testDataFile, testCaseName);
        }
        if (condition.equalsIgnoreCase("True")) {
            assertResult("Equal", response.jsonPath().getString(propname), emsg);
        } else if (condition.equalsIgnoreCase("false")) {
            assertResult("NotEqual", response.jsonPath().getString(propname), emsg);
        }
    }

    /**
     * This method validates response body against testdata
     *
     * @throws Throwable
     */
    public void ValidateResponse(String propname, String emsg, String testDataFile, String testCaseName) throws Throwable {
        if (emsg.contains("%")) {
            emsg = parseData(emsg, testDataFile, testCaseName);
        }
        if (propname.contains("%")) {
            propname = parseData(propname, testDataFile, testCaseName);
        }

        if (response.getBody().asString().startsWith("<")) {
            assertResult("Equal",  response.xmlPath().getString(propname),emsg);

        }
        else {
            assertResult("Equal", response.jsonPath().getString(propname), emsg);
        }
    }

    /**
     * This method validates response body
     *
     * @throws Throwable
     */
    public void ValidateResponse(String propname, String emsg) throws Throwable {
        if (emsg.contains("%")) {
            emsg = parseData(emsg);
        }
        if (propname.contains("%")) {
            propname = parseData(propname);
        }

        if (response.getBody().asString().startsWith("<")) {
            assertResult("Equal",  response.xmlPath().getString(propname),emsg);

        }
        else {
            assertResult("Equal", response.jsonPath().getString(propname), emsg);
        }

    }


    /**
     * This method validates response body
     *
     * @throws Throwable
     */
    public void ValidateResponse(String propname, String emsg, String condition) throws Throwable {
        if (emsg.contains("%")) {
            emsg = parseData(emsg);
        }
        if (condition.equalsIgnoreCase("True")) {
            Assert.assertEquals(response.jsonPath().getString(propname), emsg);
            assertResult("Equal", response.jsonPath().getString(propname), emsg);
        } else if (condition.equalsIgnoreCase("false")) {
            assertResult("NotEqual", response.jsonPath().getString(propname), emsg);
        }
    }


    /**
     * This method validates response body with error message and property name, it performs
     * only values comparison based on key name.
     *
     * @throws Throwable
     */
    public void ValidateResponseBodyForErrorMessage(String propname, String emsg) throws Throwable {
        if (propname.equals("message")) {
            assertResult("Equal", response.jsonPath().getString(propname), emsg);
        } else {
            assertResult("Equal", response.jsonPath().getString("detail.failedPolicyRequirements[0].property"), propname);
            assertResult("Equal", response.jsonPath().getString("detail.failedPolicyRequirements[0].policyRequirements[0].policyRequirement"), emsg);
        }
    }

    public static Response getResponse() throws Throwable {
        return response;
    }

    public void ExtractAndStore(String arg1, String arg2) throws Throwable {
        if (arg1.contains("%")){
            arg1 = parseData(arg1);
        }
        if (response.getBody().asString().startsWith("<")) {
            XmlPath xmlpath = new XmlPath(response.getBody().asString());
            String result = xmlpath.get(arg1).toString();
            storeData.put(arg2, result);
            logger.info("Storing var = " + arg2 + " with Data = " + result);

        } else {
            storeData.put(arg2, response.jsonPath().getString(arg1));
            logger.info("Storing var = " + arg2 + " with Data = " + response.jsonPath().getString(arg1));
        }
    }

    public void ExtractAndStore(String arg1, String arg2, String testDataFile, String testcaseName) throws Throwable {
        if (arg1.contains("%")){
            arg1 = parseData(arg1,testDataFile,testcaseName);
        }
        if (response.getBody().asString().startsWith("<")) {
            XmlPath xmlpath = new XmlPath(response.getBody().asString());
            String result = xmlpath.get(arg1).toString();
            storeData.put(arg2, result);
            logger.info("Storing var = " + arg2 + " with Data = " + result);

        } else {
            storeData.put(arg2, response.jsonPath().getString(arg1));
            logger.info("Storing var = " + arg2 + " with Data = " + response.jsonPath().getString(arg1));
        }
    }


    public void ExtractAndStoreToFile(String arg1, String arg2, String outputFileName, boolean appendFlag) throws Throwable {
        if (arg1.contains("%")){
            arg1 = parseData(arg1);
        }
        if (response.getBody().asString().startsWith("<")) {
            XmlPath xmlpath = new XmlPath(response.getBody().asString());
            String result = xmlpath.get(arg1).toString();
            String data = arg2+ "=" + result;
            logger.info("Writing to a File "+ outputFileName + " with the content  = " + data);
            FileUtils.write(new File(outputFileName), data, "UTF-8", appendFlag);
        } else {
            String data = arg2+ "=" + response.jsonPath().getString(arg1);
            logger.info("Writing to a File "+ outputFileName + " with the content  = " + data);
            FileUtils.write(new File(outputFileName), data, "UTF-8", appendFlag);
        }
    }

    public void ExtractAndStoreToFile(String arg1, String arg2, String testDataFile, String testcaseName, String outputFileName, boolean appendFlag) throws Throwable {
        if (arg1.contains("%")){
            arg1 = parseData(arg1,testDataFile,testcaseName);
        }
        if (response.getBody().asString().startsWith("<")) {
            XmlPath xmlpath = new XmlPath(response.getBody().asString());
            String result = xmlpath.get(arg1).toString();
            String data = arg2+ "=" + result;
            logger.info("Writing to a File "+ outputFileName + " with the content  = " + data);
            FileUtils.write(new File(outputFileName), data, "UTF-8", appendFlag);
        } else {
            String value = response.jsonPath().getString(arg1);
            String data = arg2+ "=" + value;
            logger.info("Writing to a File "+ outputFileName + " with the content  = " + data);
            FileUtils.write(new File(outputFileName), data, "UTF-8", appendFlag);
        }
    }

    public void ExtractAndStoreHeader(String arg1, String arg2) throws Throwable {
        if (response.getHeader(arg1) != null) {
            storeData.put(arg2, response.getHeader(arg1));
            logger.info("Storing var = " + arg2 + " with Data = " + response.getHeader(arg1));
        }
    }


    public void endPoint(String arg1) throws Throwable {
        env = System.getProperty("env");
        url = getApiProperty(arg1);
        System.out.println("url:" + url);

        String updatedUrl = getUrlFromJson();
        System.out.println("updatedUrl:" + updatedUrl);
        if (updatedUrl != null) {
            url = url + updatedUrl;
        }
        url = parseData(url);
        logReport("Invoking API : " + url + " ");
    }

    public String getUrlFromJson() throws Exception {
        String result = "";
        if (testdata instanceof java.lang.String) {

            XmlPath xpath = new XmlPath((String) testdata);
            result = xpath.get("Data.Endpoint.url").toString();
        } else {
            if (parseJson(testdata, "Endpoint") != null) {
                result = parseJson(testdata, "Endpoint").get("url").toString();
            }
        }
        return result;
    }

    public void endPoint(String dataFileName, String testCaseName, String endpoint) throws Throwable {
        env = System.getProperty("env");
        url = getApiProperty(endpoint);
        if (getUrlFromJson() != null) {
            url = url + getUrlFromJson();
        }
        url = parseData(url, dataFileName, testCaseName);
    }


    public void DataFile(String dataFileName, String testCaseName, String payloadFile, String payloadJsonKey) throws Throwable {

        secretFlag = false;
        System.out.println("Getting file .." + payloadFile);
        testdata = RAWrapper.getDataFile(payloadFile);
        String body = "";

        request = null;
        request = RestAssured.given().relaxedHTTPSValidation();

        if (testdata instanceof java.lang.String) {
            body = ((String) testdata).substring(((String) testdata).indexOf("<Body>") + 6, ((String) testdata).indexOf("</Body>") + 7);
            body = parseData(body,dataFileName,testCaseName);
            request.contentType("application/xml");
            request_body = body;
            request.relaxedHTTPSValidation().body(body);

        } else {
            if(payloadJsonKey!=null) {
                testdata = RAWrapper.parseJsonArrayForKey(testdata, payloadJsonKey);
            }
            Object jsonBody = RAWrapper.parseJsonNew(testdata, "Body");
            if (jsonBody instanceof JSONArray) {
                body = ((JSONArray) jsonBody).toString();
            } else if (jsonBody instanceof JSONObject) {
                body = ((JSONObject) jsonBody).toString();
            }else if (jsonBody instanceof java.lang.String) {
                body = jsonBody.toString();
            }

            request = null;
            request = RestAssured.given().relaxedHTTPSValidation();

            Object jstr1 = RAWrapper.parseJsonNew(testdata, "FormData");
            JSONObject jstr = null;

            if (jstr1 instanceof java.lang.String) {
                Object jstr2 = parseData(jstr1.toString(), dataFileName, testCaseName);
                JSONParser parser = new JSONParser();
                jstr = (JSONObject) parser.parse(jstr2.toString());
            }else{
                jstr = (JSONObject)  jstr1;
            }

            if (jstr != null && jstr.size() > 1) {
                Set<String> hname = jstr.keySet();
                Iterator<String> it = hname.iterator();
                String val = null;

                while (it.hasNext()) {
                    String str = it.next();
                    val = jstr.get(str).toString();
                    if (val.contains("%") && val.contains("%")) {
                        val = parseData(val, dataFileName, testCaseName);
                    }
                    request.formParam(str, val);
                }
            } else {
                String body1 = body;
                String parsedBody = "";
                body = body.substring(1, body.length() - 1);
                if (jsonBody instanceof JSONArray) {
                    parsedBody = parseData(body, dataFileName, testCaseName);
                    body = "[" + parsedBody + "]";
                } else if (jsonBody instanceof JSONObject) {
                    parsedBody = parseData(body, dataFileName, testCaseName);
                    body = "{" + parsedBody + "}";
                } else if (jsonBody instanceof java.lang.String) {
                    parsedBody = parseData(body1, dataFileName, testCaseName);
                    body = parsedBody;
                }
                request_body = body;
                if(parsedBody.length() > 0){
                    request.relaxedHTTPSValidation().body(body);
                }else{
                    request.relaxedHTTPSValidation();
                }
            }
        }
    }


    public  String parseData(String Data, String dataFileName, String testCaseName) throws Exception {
        System.out.println("parseData" + Data);
        env = System.getProperty("env");
        JSONObject webDataJSONObject = null;
        if (null != dataFileName && !dataFileName.equalsIgnoreCase("")) {
            webDataJSONObject = RAWrapper.parseWebJsonForWebData(RAWrapper.getJsonData(dataFileName), testCaseName);
        }
        Pattern pattern = Pattern.compile("\\%(.*?)\\%", Pattern.DOTALL);
        java.util.regex.Matcher matcher = pattern.matcher(Data);
        while (matcher.find()) {
            String match = matcher.group(1);
            String[] splits = match.split(",");
            String val = "";
            if (splits.length > 1) {
                String location = splits[0];
                String param = splits[1];
                System.out.println("location : " + location);
                System.out.println("param : " + param);

                String numField = "";
                if (splits.length == 3) {
                    numField = splits[2];
                }
                if (location.equalsIgnoreCase("Response")) {
                    if (storeData.containsKey(param)) {
                        System.out.println("param" + param);
                        val = String.valueOf(storeData.get(param));
                    }
                } else if (location.equalsIgnoreCase("Memory")) {
                    if (storeData.containsKey(param)) {
                        System.out.println("param" + param);
                        val = String.valueOf(storeData.get(param));
                    }
                } else if (location.equalsIgnoreCase("EnvVariable")) {
                    val = System.getenv(param);
                } else if (location.equalsIgnoreCase("Property")) {
                    val = getApiProperty(param);
                } else if (location.equalsIgnoreCase("RequestData")) {
                    val = parseJson(testdata, "Body").get(param).toString();
                } else if (location.equalsIgnoreCase("TestData")) {
                    if (webDataJSONObject != null) {
                        val = webDataJSONObject.get(param).toString();
                    }
                } else if (location.equalsIgnoreCase("Date")) {
                    val = getDate(Integer.parseInt(splits[2]), param);
                } else if (location.equalsIgnoreCase("DateFunction")) {
                    val = performDateFunction(match);
                } else if (location.equalsIgnoreCase("RandomHexNumber")) {
                    val = randomHexNumber(Integer.parseInt(param));
                    System.out.println("val=="+ val + " " + param);
                    storeDynamicGeneratedData(splits, val);
                } else if (location.equalsIgnoreCase("RandomNumber")) {
                    val = generateRandom(Integer.parseInt(param), false, true);
                    storeDynamicGeneratedData(splits, val);
                } else if (location.equalsIgnoreCase("RandomChar")) {
                    val = generateRandom(Integer.parseInt(param), true, false);
                    storeDynamicGeneratedData(splits, val);
                } else if (location.equalsIgnoreCase("RandomAlphaNumeric")) {
                    val = generateRandom(Integer.parseInt(param), true, true);
                    storeDynamicGeneratedData(splits, val);
                }
                java.util.regex.Matcher matcherInner = pattern.matcher(val);
                val = Matcher.quoteReplacement(val);
                if (matcherInner.find()) {
                    val = parseData(val, dataFileName, testCaseName);
                }

                if(! match.contains("SECRET") && ! match.contains("Secret") && ! match.contains("secret") && ! match.contains("ENCRYPTED") && ! match.contains("encrypted") && ! match.contains("Encrypted")){
                    System.out.println("Replace value  for " + "%" + match + "%" + " is " + val);
                }

                if (numField != "" && numField.equalsIgnoreCase("number")) {
                    Data = Data.replaceAll("\\\"\\%" + match + "\\%\\\"", val);
                } else {
                    Data = Data.replaceAll("\\%" + match + "\\%", val);
                }
            }
        }
        return Data;
    }

    private String getDecodeBas64Val(String encodedString) {
        return new String(Base64.getDecoder().decode(encodedString));
    }

    public void DataFile(String payloadFile) throws Throwable {
        secretFlag = false;
        System.out.println("Getting file ..");
        testdata = RAWrapper.getDataFile(payloadFile);
        String body = "";

        request = null;
        request = RestAssured.given().relaxedHTTPSValidation();

        if (testdata instanceof java.lang.String) {
            System.out.println("inside file type" + testdata);
            body = ((String) testdata).substring(((String) testdata).indexOf("<Body>") + 6, ((String) testdata).indexOf("</Body>") + 7);
            body = parseData(body);
            request.contentType("application/xml");
            request_body = body;
            request.relaxedHTTPSValidation().body(body);

        } else {
//            request.contentType("application/json");
            Object jsonBody = RAWrapper.parseJsonNew(testdata, "Body");
            if (jsonBody instanceof JSONArray) {
                body = ((JSONArray) jsonBody).toString();
            } else if (jsonBody instanceof JSONObject) {
                body = ((JSONObject) jsonBody).toString();
            }

            JSONObject jstr = parseJson(testdata, "FormData");
            if (jstr != null && jstr.size() > 1) {
                Set<String> hname = jstr.keySet();
                Iterator<String> it = hname.iterator();
                String val = null;
                while (it.hasNext()) {
                    String str = it.next();
                    val = jstr.get(str).toString();
                    if (val.contains("%") && val.contains("%")) {
                        val = parseData(val);
                    }
                    request.formParam(str, val);
                }
            } else {
                String parsedBody = null;
                body = body.substring(1, body.length() - 1);

                if (jsonBody instanceof JSONArray) {
                    parsedBody = parseData(body);
                    body = "[" + parsedBody + "]";
                } else if (jsonBody instanceof JSONObject) {
                    parsedBody = parseData(body);
                    body = "{" + parsedBody + "}";
                }
                request_body = body;
                if(parsedBody.length() > 0){
                    request.relaxedHTTPSValidation().body(body);
                }else{
                    request.relaxedHTTPSValidation();
                }
            }
        }

    }

    public  void AddHeaders() throws Throwable {
        Headers header = getHeaders_New(testdata, null, null);
        request.relaxedHTTPSValidation().headers(header).config(RestAssuredConfig.config().encoderConfig(EncoderConfig.encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false)));

        if(!secretFlag){
            logger.info(request.log().all());
        }
    }

    public  void AddHeaders(String dataFileName, String testCaseName, String payloadFile) throws Throwable {
        testdata = RAWrapper.getDataFile(payloadFile);
        Headers header = getHeaders_New(testdata, dataFileName, testCaseName);
        request.relaxedHTTPSValidation().headers(header).config(RestAssuredConfig.config().encoderConfig(EncoderConfig.encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false)));
        if(!secretFlag) {
            logger.info(request.log().all());
        }
    }

    public Headers getHeaders_New(Object testdata, String dataFileName, String testCaseName) throws Exception {
        List<Header> list = new ArrayList<>();

            JSONObject jstr = parseJson(testdata, "Headers");
            Set<String> hname = jstr.keySet();
            Iterator<String> it = hname.iterator();
            String val = null;
            while (it.hasNext()) {
                String str = it.next();
                val = jstr.get(str).toString();
                val = parseData(val, dataFileName, testCaseName);
                list.add(new Header(str, val));
            }
        Headers header = new Headers(list);
        return header;
    }
    public  void AddHeaders(String dataFileName, String testCaseName, String payloadFile, String payloadJsonKey) throws Throwable {
        testdata = RAWrapper.getDataFile(payloadFile);
        if(payloadJsonKey!=null) {
            testdata = RAWrapper.parseJsonArrayForKey(testdata, payloadJsonKey);
        }
        Headers header = getHeaders_New(testdata, dataFileName, testCaseName);
        request.relaxedHTTPSValidation().headers(header).config(RestAssuredConfig.config().encoderConfig(EncoderConfig.encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false)));
        if(!secretFlag) {
            logger.info(request.log().all());
        }
    }

    public void DataFileArray(String dataFileName) throws Throwable {
        secretFlag = false;
        testdata = RAWrapper.getDataFile(dataFileName);
        String body = "";
        String parsedBody = "";

        Object jsonBody = RAWrapper.parseJsonNew(testdata, "Body");
        if (jsonBody instanceof JSONArray) {
            System.out.println(" it is a jsonarray");
            body = ((JSONArray) jsonBody).toString();
        } else if (jsonBody instanceof JSONObject) {
            System.out.println(" it is a jsonObject");

            body = ((JSONObject) jsonBody).toString();
        }

        body = body.substring(1, body.length() - 1);
        System.out.println("Initial Body == " + body);
        if (jsonBody instanceof JSONArray) {
            parsedBody = parseData(body);
            body = "[" + parsedBody + "]";

        } else if (jsonBody instanceof JSONObject) {
            parsedBody = parseData(body);
            body = "{" + parsedBody + "}";
        }
        System.out.println("After parsing Body == " + body);

        if(parsedBody.length() > 0){
            request.relaxedHTTPSValidation().body(body);
        }else{
            request.relaxedHTTPSValidation();
        }


    }


    public String parseData(String Data) throws Exception {
        env = System.getProperty("env");
        Pattern pattern = Pattern.compile("\\%(.*?)\\%", Pattern.DOTALL);
        java.util.regex.Matcher matcher = pattern.matcher(Data);
        while (matcher.find()) {
            String match = matcher.group(1);
            String[] splits = match.split(",");

            String val = "";
            if (splits.length > 1) {
                String location = splits[0];
                String param = splits[1];
                System.out.println("location : " + location);
                System.out.println("param : " + param);

                String numField = "";
                if (splits.length == 3) {
                    numField = splits[2];
                }

                if (location.equalsIgnoreCase("Response")) {
                    if (storeData.containsKey(param)) {
                        System.out.println("param" + param);
                        val = String.valueOf(storeData.get(param));
                    }
                } else if (location.equalsIgnoreCase("Memory")) {
                    if (storeData.containsKey(param)) {
                        System.out.println("param" + param);
                        val = String.valueOf(storeData.get(param));
                    }
                } else if (location.equalsIgnoreCase("EnvVariable")) {
                    val = System.getenv(param);
                } else if (location.equalsIgnoreCase("Property")) {
                    val = getApiProperty(param);
                } else if (location.equalsIgnoreCase("RequestData")) {
                    val = parseJson(testdata, "Body").get(param).toString();
                } else if (location.equalsIgnoreCase("RequestTestData")) {
                    if (storeDataRequest.containsKey(param)) {
                        System.out.println("param" + param);
                        val = String.valueOf(storeDataRequest.get(param));
                    }
                } else if (location.equalsIgnoreCase("TestData")) {
                    JSONObject webDataJSONObject = RAWrapper.parseWebJsonForWebData(RAWrapper.getJsonData(param), splits[2]);
                    val = webDataJSONObject.get(splits[3]).toString();
                } else if (location.equalsIgnoreCase("Date")) {
                    val = getDate(Integer.parseInt(splits[2]), param);
                } else if (location.equalsIgnoreCase("DateFunction")) {
                    val = performDateFunction(match);
                } else if (location.equalsIgnoreCase("RandomNumber")) {
                    val = generateRandom(Integer.parseInt(param), false, true);
                    storeDynamicGeneratedData(splits, val);
                } else if (location.equalsIgnoreCase("RandomHexNumber")) {
                    val = randomHexNumber(Integer.parseInt(param));
                    System.out.println("val=="+ val + " " + param);
                    storeDynamicGeneratedData(splits, val);
                } else if (location.equalsIgnoreCase("RandomChar")) {
                    val = generateRandom(Integer.parseInt(param), true, false);
                    storeDynamicGeneratedData(splits, val);
                } else if (location.equalsIgnoreCase("RandomAlphaNumeric")) {
                    val = generateRandom(Integer.parseInt(param), true, true);
                    storeDynamicGeneratedData(splits, val);
                }
                else if (location.equalsIgnoreCase("GenerateCodeChallenge")) {
                    generateCodeChallengeAndverifier();
                }
                java.util.regex.Matcher matcherInner = pattern.matcher(val);
                val = Matcher.quoteReplacement(val);

                if(match.contains("+")){
                    match = match.replace("+", "\\+");
                }

                if (matcherInner.find()) {
                    val = parseData(val);
                }

                if (numField != "" && numField.equalsIgnoreCase("number")) {
                    Data = Data.replaceAll("\\\"\\%" + match + "\\%\\\"", val);
                } else {
                    Data = Data.replaceAll("\\%" + match + "\\%", val);
                }
            }
        }
        return Data;
    }

    public void storeDynamicGeneratedData(String[] splitData, String value) {
        if (splitData.length > 2) {
            storeData.put(splitData[2], value);
        }
    }

    public void generateCodeChallengeAndverifier(){
        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);
        System.out.println("Code Verifier: " + codeVerifier);
        System.out.println("Code Challenge: " + codeChallenge);
        APIlib.storeData.put("code_challenge",codeChallenge);
        APIlib.storeData.put("code_verifier",codeVerifier);
    }

    public static String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifierBytes = new byte[96];
        codeVerifierBytes.toString().replaceAll("-","a");
        secureRandom.nextBytes(codeVerifierBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifierBytes).replaceAll("-","a");
    }

    public static String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = messageDigest.digest(codeVerifier.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available.", e);
        }
    }


    public String getDate(int days, String dFormat) {
        Date currentDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        c.add(Calendar.DATE, days);
        Date newDate = c.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat(dFormat);
        dateFormat.setTimeZone(TimeZone.getTimeZone("US/Central"));
        return dateFormat.format(newDate);
    }

    public void getDateAndStore(String inputDate,String inputDateFormat,String MonthOrYear,int count,String format,String variableToStore)throws Exception {

        if(inputDate.contains("%")){
            inputDate=parseData(inputDate);
        }
        SimpleDateFormat sdfSource = new SimpleDateFormat(inputDateFormat);
        sdfSource.setTimeZone(TimeZone.getTimeZone("US/Central"));
        Date date = sdfSource.parse(inputDate);
        SimpleDateFormat sdfDestination = new SimpleDateFormat(format);
        sdfDestination.setTimeZone(TimeZone.getTimeZone("US/Central"));
        String strDate = sdfDestination.format(date);
        System.out.println("Converted date is : " + strDate);
        String data="DateFunction,"+MonthOrYear+","+count+","+strDate+","+format;
        String returnDate=performDateFunction(data);
        storeData.put(variableToStore,returnDate);
    }

    public String performDateFunction(String data) throws ParseException {
        String returnStr = "";
        String[] splits = data.split(",");
        Calendar cal = Calendar.getInstance();
        TimeZone tz = TimeZone.getTimeZone("CST");
        cal.setTimeZone(tz);
        int size = splits.length;
        if (size < 5) {
            return null;
        }
        String operation = splits[1].toUpperCase();
        int amount = 0;
        if (!splits[2].trim().isEmpty()) {
            amount = Integer.parseInt(splits[2]);
        }
        String dateInput = splits[3];
        String format = splits[4];
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setTimeZone(TimeZone.getTimeZone("US/Central"));
        Date date = null;
        if (dateInput.equalsIgnoreCase("NOW")) {
            date = new Date();
        } else {
            date = dateFormat.parse(dateInput);
        }
        cal.setTime(date);
        switch (operation) {
            case "HOUR":
                cal.add(Calendar.HOUR, amount);
                break;
            case "DAY":
                cal.add(Calendar.DATE, amount);
                break;
            case "WEEK":
                cal.add(Calendar.WEEK_OF_YEAR, amount);
                break;
            case "MONTH":
                cal.add(Calendar.MONTH, amount);
                break;
            case "YEAR":
                cal.add(Calendar.YEAR, amount);
                break;
            case "LASTDAYOFMONTH":
                int lastDate = cal.getActualMaximum(Calendar.DATE);
                cal.set(Calendar.DATE, lastDate);
                int lastDay = cal.get(Calendar.DAY_OF_WEEK);
                break;
            case "LASTWEEKDAYOFMONTH":
                cal.add(Calendar.MONTH, 1);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                do {
                    cal.add(Calendar.DATE, -1);
                } while (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                        || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
        }
        Date newDate = cal.getTime();
        SimpleDateFormat date_Format = new SimpleDateFormat(format);
        date_Format.setTimeZone(TimeZone.getTimeZone("US/Central"));
        returnStr = date_Format.format(newDate);
        return returnStr;
    }

    public String randomHexNumber(int length){
        Random randomService = new Random();
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length) {
            sb.append(Integer.toHexString(randomService.nextInt()));
        }
        sb.setLength(length);
        System.out.println("random hexnum " + sb.toString());
        return sb.toString();
    }

    public String generateRandom(int length, boolean letters, boolean numbers) {
        String output = RandomStringUtils.random(length, letters, numbers);
        while (output.startsWith("0")) {
            output = RandomStringUtils.random(length, letters, numbers);
        }
        return output;
    }



    /**
     * Get a list of objects with certain key without indices
     *
     * @param newKey
     * @return
     */
    public List<Object> getData(String newKey) {
        Enumeration enums = table.keys();

        List<Object> list = new ArrayList<Object>();
        while (enums.hasMoreElements()) {
            String key = (String) enums.nextElement();
            StringBuilder builder = new StringBuilder();
            int i = 0;
            while (i < key.length()) {
                while (i < key.length() && key.charAt(i) != '[' && key.charAt(i) != ']') {


                    builder.append(key.charAt(i));
                    i++;
                }
                if (i < key.length() && key.charAt(i) == '[') {
                    while (key.charAt(i) != ']') {
                        i++;
                    }

                }
                i++;
            }

            System.out.println("newKey " + newKey + " parsed key " + builder.toString());
            if (newKey.equals(builder.toString())) {
                list.add(table.get(key));
            }
        }
        return list;
    }

    public void API_Wait(int noOfSeconds) throws Exception {
        System.out.println("waiting for " + noOfSeconds + " seconds");
        Thread.sleep(noOfSeconds * 1000);
    }


    public void addDataToJson(String arg1, String arg2) {
        table.put(arg1, arg2);
    }

    /**
     * While traversing
     *
     * @param table2
     * @param newKey
     */
    public void addTemporaryHash(Hashtable table2, String newKey, Object obj) {
        if (table2.get(newKey) == null) {
            List list = new ArrayList();
            list.add(obj);
            table2.put(newKey, list);
        } else {
            List list = (ArrayList) table2.get(newKey);
            list.add(obj);
        }

    }



    public void invokeAPI(String method, String endPoint, String dataFileName, String testCaseName, String payloadFile, String payloadKeyName) throws Throwable {
        DataFile(dataFileName, testCaseName, payloadFile,payloadKeyName);
        AddHeaders(dataFileName, testCaseName, payloadFile, payloadKeyName);
        if (cookies !=null){
            request.relaxedHTTPSValidation().cookies(cookies);
        }
        endPoint(dataFileName, testCaseName, endPoint);
        if (method.equalsIgnoreCase("POST")) {
            SendPostRequest();
        } else if (method.equalsIgnoreCase("GET")) {
            SendGetRequest();
        } else if (method.equalsIgnoreCase("PATCH")) {
            SendPatchRequest();
        } else if (method.equalsIgnoreCase("PUT")) {
            SendPutRequest();
        } else if (method.equalsIgnoreCase("DELETE")) {
            SendDeleteRequest();
        }
        Printresponsebody();
        logReport("Validating Status code and Status Line");
        CheckStatusCodeLine(dataFileName, testCaseName, payloadFile, payloadKeyName);
        CheckStatusCode(dataFileName, testCaseName, payloadFile, payloadKeyName);

    }

    //added composite method
    public void invokeAPI(String method, String endPoint, String dataFileName, String testCaseName, String payloadFile) throws Throwable {
        invokeAPI(method,endPoint, dataFileName, testCaseName, payloadFile, null);
    }

    //added composite method to check the VIN status for Invoke API
    public void invokeAPI(String method, String endPoint, String dataFileName, String testCaseName, String payloadFile, String accountStatus, String vinStatus) throws Throwable {
        accountStatus = parseData(accountStatus,dataFileName,testCaseName);

        if(!accountStatus.equals(vinStatus)){
            System.out.println("Inside" + vinStatus);
            invokeAPI(method,endPoint, dataFileName, testCaseName, payloadFile, null);
        }
    }


    public void invokeAPI(String method, String url, String payloadFile) throws Throwable {

        DataFile(payloadFile);
        endPoint(url);
        AddHeaders();
        if (cookies !=null){
            request.relaxedHTTPSValidation().cookies(cookies);
        }
        logReport("Sending " + method + " request");
        if (method.equalsIgnoreCase("POST")) {
            SendPostRequest();
        } else if (method.equalsIgnoreCase("GET")) {
            SendGetRequest();
        } else if (method.equalsIgnoreCase("PATCH")) {
            SendPatchRequest();
        } else if (method.equalsIgnoreCase("PUT")) {
            SendPutRequest();
        } else if (method.equalsIgnoreCase("DELETE")) {
            SendDeleteRequest();
        }
        Printresponsebody();
        logReport("Validating Status code and Status Line");
        CheckStatusCode();
        CheckStatusCodeLine();
    }


    public void invokeAPIBasicAuth(String method, String url, String payloadFile, String username, String password) throws Throwable {

        DataFile(payloadFile);
        endPoint(url);
        AddHeaders();
        if (cookies != null) {
            request.relaxedHTTPSValidation().cookies(cookies);
        }
        if (method.equalsIgnoreCase("POST")) {
            SendPostRequestForBasicAuth(username, password);
        }
        CheckStatusCode();
        CheckStatusCodeLine();
    }

    public void SendPostRequestForBasicAuth(String username, String password) throws Throwable {
        if (getApiProperty(username) != null && getApiProperty(password) != null) {
            String userID = getApiProperty(username);
            String pwd = getApiProperty(password);
            response = request.auth().preemptive().basic(userID, pwd).relaxedHTTPSValidation().post(url);
        }
    }


    public void logReport(String message) {
        try {
            if(message.contains("%")){
                message = parseData(message);
            }
            ExtentCucumberAdapter.addTestStepLog("<b style=\"color:Orange;\"> INFO: </b> <b style=\"color:Black;\"> " + message + " </b>");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void validationMessage(String passFailStatus, String message) {
        try {
            if (passFailStatus.contains("Pass")) {
                ExtentCucumberAdapter.addTestStepLog("<b style=\"color:LimeGreen;\">Validation PASS: </b>  <b style=\"color:Black;\">" + message + " </b>");
            } else {
                ExtentCucumberAdapter.addTestStepLog("<b style=\"color:Red;\">Validation FAIL: </b> <b style=\"color:FireBrick;\"> " + message + " </b>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void assertResult(String assertType, Object actual, Object expected) {
        try {
            System.out.println("actual-->"+actual+"---expected-->"+expected);
            String expectedResult = String.valueOf(expected);
            String actualResult = String.valueOf(actual);
            if (actualResult.contains("%")) {
                actualResult = parseData(actualResult);
            }
            if (expectedResult.contains("%")) {
                expectedResult = parseData(expectedResult);
            }
            if (assertType.equalsIgnoreCase("Equal")) {
                try {
                    System.out.println("actualResult:"+actualResult+"--expectedResult:"+expectedResult);
                    Assert.assertEquals(actualResult, expectedResult);
                    validationMessage("Pass", "Expected value " + expectedResult + " is matched with Actual value " + actualResult + "");
                } catch (AssertionError e) {
                    validationMessage("Fail", "Expected value " + expectedResult + " is NOT matched with Actual value " + actualResult + "");
                    Assert.fail(e.getMessage());
                }
            } else if (assertType.equalsIgnoreCase("NotNull")) {
                try {
                    Assert.assertNotNull(actualResult);
                    validationMessage("Pass", "Expected value " + actualResult + " is Not NULL");
                } catch (AssertionError e) {
                    validationMessage("Fail", "Expected value " + actualResult + "  NULL");
                    Assert.fail(e.getMessage());
                }
            } else if (assertType.equalsIgnoreCase("NotEqual")) {
                try {
                    Assert.assertNotEquals(actualResult, expectedResult);
                    validationMessage("Pass", "Expected value " + expectedResult + " is NOT matched with Actual value " + actualResult + "");
                } catch (AssertionError e) {
                    validationMessage("Fail", "Expected value " + expectedResult + " is matched with Actual value " + actualResult + "");
                    Assert.fail(e.getMessage());
                }

            }else if (assertType.equalsIgnoreCase("True")) {
                try {
                    Assert.assertTrue(actualResult.contains(expectedResult));
                    validationMessage("Pass", "Expected value " + expectedResult + " is matched with Actual value " + actualResult + "");
                } catch (AssertionError var8) {
                    validationMessage("Fail", "Expected value " + expectedResult + " is NOT matched with Actual value " + actualResult + "");
                    Assert.fail(var8.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void assertResult(String assertType, Object actual, Object expected, String testDataFile, String testCaseName) {
        try {
            String expectedResult = String.valueOf(expected);
            String actualResult = String.valueOf(actual);
            if (actualResult.contains("%")) {
                actualResult = parseData(actualResult, testDataFile, testCaseName);
            }
            if (expectedResult.contains("%")) {
                expectedResult = parseData(expectedResult, testDataFile, testCaseName);
            }
            if (assertType.equalsIgnoreCase("Equal")) {
                try {
                    Assert.assertEquals(actualResult, expectedResult);
                    validationMessage("Pass", "Expected value " + expectedResult + " is matched with Actual value " + actualResult + "");
                } catch (AssertionError e) {
                    validationMessage("Fail", "Expected value " + expectedResult + " is NOT matched with Actual value " + actualResult + "");
                    Assert.fail(e.getMessage());
                }
            } else if (assertType.equalsIgnoreCase("NotNull")) {
                try {
                    Assert.assertNotNull(actualResult);
                    validationMessage("Pass", "Expected value " + actualResult + " is Not NULL");
                } catch (AssertionError e) {
                    validationMessage("Fail", "Expected value " + actualResult + "  NULL");
                    Assert.fail(e.getMessage());
                }
            } else if (assertType.equalsIgnoreCase("NotEqual")) {
                try {
                    Assert.assertNotEquals(actualResult, expectedResult);
                    validationMessage("Pass", "Expected value " + expectedResult + " is NOT matched with Actual value " + actualResult + "");
                } catch (AssertionError e) {
                    validationMessage("Fail", "Expected value " + expectedResult + " is matched with Actual value " + actualResult + "");
                    Assert.fail(e.getMessage());
                }

            }else if (assertType.equalsIgnoreCase("True")) {
                try {
                    Assert.assertTrue(actualResult.contains(expectedResult));
                    validationMessage("Pass", "Expected value " + expectedResult + " is matched with Actual value " + actualResult + "");
                } catch (AssertionError e) {
                    validationMessage("Fail", "Expected value " + expectedResult + " is NOT matched with Actual value " + actualResult + "");
                    Assert.fail(e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void retryAPI(String statusCode) throws Throwable {

        SendPostRequest();
        int code = response.getStatusCode();
        if (String.valueOf(code).equals(code)) {
            Thread.sleep(20000);
            SendPostRequest();
        }

    }

    private String readFile(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

            return stringBuilder.toString();
        } finally {
            reader.close();
        }
    }

    public void storeDateTime(String format,String variableToStore) throws Throwable  {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        df.setTimeZone(TimeZone.getTimeZone("US/Central"));
        String strDate = df.format(new Date());
        storeData.put(variableToStore,strDate);
        logger.info("Storing var = " + variableToStore + " with Data = " + strDate);
    }

    public void storeIntoStoreData(String value,String key) throws Throwable  {
        if (value.contains("%")) {
            value = parseData(value);
        }

        System.out.println("Expression to evaluate:" +value);
        value = Eval.me(value).toString();
        storeData.put(key,value);
        logger.info("Storing var = " + key + " with Data = " + value);
    }

    public void storeIntoStoreData(String value,String key, String testDataFile,String testCaseName) throws Throwable  {
        if (value.contains("%")) {
            value = parseData(value, testDataFile, testCaseName);
        }
        System.out.println("Expression to evaluate:" +value);
        value = Eval.me(value).toString();
        storeData.put(key,value);
        logger.info("Storing var = " + key + " with Data = " + value);
    }



    public void ExtractAndStoreAsJSON(String varName) throws Throwable {
        if (varName.contains("%")){
            varName = parseData(varName);
        }
        if (! response.getBody().asString().startsWith("<")) {
            org.json.JSONObject jsonObject = new org.json.JSONObject(response.asString());
            storeData.put(varName, jsonObject);
            logger.info("Storing var = " + varName + " with Data = " + jsonObject);
        }
    }

    public void assertJson(String expected, String actual) throws Exception {
        org.json.JSONObject expectedJSON = new org.json.JSONObject(parseData(expected));
        org.json.JSONObject actualJSON = new org.json.JSONObject(parseData(actual));
        JSONAssert.assertEquals(expectedJSON, actualJSON, false);
    }

    public void assertJson(String expected, String actual, String dataFileName, String testCaseName) throws Exception {
        JSONObject webDataJSONObject = null;

        if (expected.contains("%")) {
            expected = parseData(expected, dataFileName, testCaseName);
        }
        logger.info("expected =" + expected);

        org.json.JSONObject expectedJSON = new org.json.JSONObject(expected);
        org.json.JSONObject actualJSON   = new org.json.JSONObject(parseData(actual));
        JSONAssert.assertEquals(expectedJSON, actualJSON, false);
    }


    public Map<String, Object> jsonToMap(JSONObject json) {
        Map<String, Object> retMap = new HashMap<String, Object>();

        if(json != null) {
            retMap = toMap(json);
        }
        return retMap;
    }

    public Map<String, Object> toMap(JSONObject object) {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keySet().iterator();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public List<Object> toList(JSONArray array) {
        List<Object> list = new ArrayList<Object>();
        for(int i = 0; i < array.size(); i++) {
            Object value = array.get(i);
            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

    List<String> allProducts;
    private static Response response2;
    public void validatePriceNotZero(){
        JsonPath jsonPathEvaluator = response.jsonPath();
        allProducts = jsonPathEvaluator.getList("hits");
        for(int i=0;i<allProducts.size();i++) {
            Object productPrice = jsonPathEvaluator.get("hits["+i+"].c_customData.aggregatedDetails.priceRange.minPrice");
            Object productMaxPrice=jsonPathEvaluator.get("hits["+i+"].c_customData.aggregatedDetails.priceRange.maxPrice");
            if(productPrice.equals(String.valueOf(0)) || productMaxPrice.equals(String.valueOf(0))){
                Assert.fail("Price min value displayed as zero for product:"+jsonPathEvaluator.get("hits["+i+"].c_customData.hitProductProperties.productId"));
            }
            else{
                ExtentCucumberAdapter.addTestStepLog("Product "+i+" : "+jsonPathEvaluator.get("hits["+i+"].c_customData.hitProductProperties.productId")+" minPrice value is  :  "+String.valueOf(productPrice)  +" And "+"Product "+i+" : "+jsonPathEvaluator.get("hits["+i+"].c_customData.hitProductProperties.productId")+" maxPrice value is  :  "+String.valueOf(productPrice));
            }
        }
    }
    private static RequestSpecification request2 = RestAssured.given().relaxedHTTPSValidation();
    public void validatePriceNotZeroInPDPPage(){
        JsonPath jsonPathEvaluator = response.jsonPath();
        JsonPath jsonPathEvaluator2;
        String url=getApiProperty("productDetails");
        for(int i=0;i<allProducts.size();i++) {
            String sku=jsonPathEvaluator.get("hits["+i+"].c_customData.hitProductProperties.productId");
            url=getApiProperty("productDetails");
            url=url.replace("SKU",sku);
            response2 = request2.relaxedHTTPSValidation().get(url);
            cookies = response2.getDetailedCookies();
            if(response2.getStatusCode()==200){
                System.out.println("Price API -Sucessful");
            }else{
                System.out.println("Price API - Not Sucessful");
            }
            try {
                jsonPathEvaluator2 = response2.jsonPath();
                Object productPrice = jsonPathEvaluator2.get("price");
                if (productPrice.equals(String.valueOf(0))) {
                    ExtentCucumberAdapter.addTestStepLog("<b style=\"color:Red;\">Validation FAIL: </b> <b style=\"color:FireBrick;\"> " + "Price min value displayed as zero for product:" + jsonPathEvaluator.get("hits["+i+"].c_customData.hitProductProperties.productId" + " </b>"));
                } else {
                    ExtentCucumberAdapter.addTestStepLog("Product  " + sku + " PDP page price value is  :  " + String.valueOf(productPrice));
                }
            }catch (Exception e){
            }
        }
    }
}
