package com.jcg.api.lib.stepdefinitions;

import com.jcg.api.lib.APIlib;
import io.cucumber.java.en.And;


public class ApiStepDef {
    APIlib apiLibrary;
    String womenSweatersCategory="https://qa.app.jcrew.com/browse/product_search?count=60&country-code=US&expand=variations,availability&refine=c_allowedCountries=ALL|US&refine_1=c_displayOn=standard_usd&refine_2=cgid=womens|features|new-arrivals&refine_3=c_categories=women-na-sweaters&start=0";
    public ApiStepDef() throws Exception {
        apiLibrary=new APIlib();
    }

    @And("I send Post request")
    public void sendPostRequest() throws Throwable {
        apiLibrary.SendPostRequest();
    }

    @And("I send Get request")
    public void sendGetRequest(String url) throws Throwable {
        apiLibrary.SendGetRequest();
    }

    @And("I send Put request")
    public void sendPutRequest(String url) throws Throwable {
        apiLibrary.SendPutRequest();
    }


    @And("I send Delete request")
    public void sendDeleteRequest(String url) throws Throwable {
        apiLibrary.SendDeleteRequest();
    }

    @And("I verify price is not zero for products")
    public void iVerifyPriceIsNotZeroForProducts() {
        apiLibrary.validatePriceNotZero();
    }
    @And("I verify price is not zero for products in PDP page")
    public void iVerifyPriceIsNotZeroForProductsInPDPPage() {
        apiLibrary.validatePriceNotZeroInPDPPage();
    }

    @And("I send Post request for category {string}")
    public void sendPostRequestForCategory(String url) throws Throwable {
        url=apiLibrary.getApiProperty(url);
        apiLibrary.SendPostRequest(url);
    }

    @And("I send Get request for category {string}")
    public void sendGetRequestForCategory(String url) throws Throwable {
        url=apiLibrary.getApiProperty(url);
        apiLibrary.SendGetRequest();
    }

    @And("I retry to {string} failed api")
    public void iRetryApi(String method) throws Throwable {
        apiLibrary.retryFailedAPI(method);
    }

    @And("I verify status code")
    public void verifyStatusCode() throws Throwable {
        apiLibrary.CheckStatusCode();
    }
    @And("I invoke api {string} {string} {string}")
    public void InvokeAPI(String method, String url, String payloadFile) throws Throwable {
        apiLibrary.invokeAPI(method, url, payloadFile);
    }

    @And("I invoke api for basic auth {string} {string} {string}")
    public void InvokeAPIForBasicAuth(String method, String url, String payloadFile,String username,String password) throws Throwable {
        apiLibrary.invokeAPIBasicAuth(method,url,payloadFile,username,password);
    }

    @And("I extract and store {string} from response")
    public void extractAndStore(String key) throws Throwable {
        apiLibrary.ExtractAndStore(key,key);
    }
    @And("I extract and store {string} from response header")
    public void extractAndStoreResponseHeader(String key) throws Throwable {
        apiLibrary.ExtractAndStoreHeader(key,key);
    }

    @And("I validate response {string} matches {string}")
    public void validateResponse(String expected,String actual) throws Throwable {
        apiLibrary.ValidateResponse(expected,actual);
    }
    @And("I validate response {string} error message {string}")
    public void validateResponseError(String propname,String error) throws Throwable {
        apiLibrary.ValidateResponseBodyForErrorMessage(propname,error);
    }

}
