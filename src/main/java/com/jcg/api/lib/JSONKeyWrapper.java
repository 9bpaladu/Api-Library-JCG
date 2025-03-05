package com.jcg.api.lib;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONKeyWrapper
{
    private JSONObject obj;
    private String key;

    public JSONKeyWrapper( JSONObject obj, String key)
    {
        this.obj = obj;
        this.key = key;
    }

    public JSONKeyWrapper()
    {
        this.obj = null;
        this.key = "";
    }

    public JSONObject getObj()
    {
        return obj;
    }

    public void setObj( JSONObject obj)
    {
        this.obj = obj;
    }

    public String getKey()
    {
        return key;
    }


    public void setKey( String key)
    {
        this.key = key;
    }
}