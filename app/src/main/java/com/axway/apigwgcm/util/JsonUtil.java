package com.axway.apigwgcm.util;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import java.util.Locale;

/**
 * Created by su on 11/20/2014.
 */
public class JsonUtil {
    private static final String TAG = JsonUtil.class.getSimpleName();

    private static JsonUtil instance = null;
    private JsonParser parser;

    protected JsonUtil() {
        super();
        parser = null;
    }

    public static JsonUtil getInstance() {
        if (instance == null)
            instance = new JsonUtil();
        return instance;
    }

    public JsonParser getJsonParser() {
        if (parser == null)
            parser = new JsonParser();
        return parser;
    }

    public JsonElement parse(String json) {
        JsonElement rv = null;
        if (!TextUtils.isEmpty(json))
            rv = getJsonParser().parse(json);
        return rv;
    }


    public JsonObject parseAsJsonObject(String json) {
        JsonObject rv = null;
        JsonElement ele = parse(json);
        if (ele != null && ele.isJsonObject())
            rv = ele.getAsJsonObject();
        return rv;
    }

    public JsonArray parseAsJsonArray(String json) {
        JsonArray rv = null;
        JsonElement ele = parse(json);
        if (ele != null && ele.isJsonArray())
            rv = ele.getAsJsonArray();
        return rv;
    }

    public JsonObject eventFrom(Bundle extras) {
        JsonObject rv = new JsonObject();
        rv.addProperty("msg_id", extras.getString("msg_id"));
        rv.addProperty("sender", extras.getString("sender"));
        rv.addProperty("from", extras.getString("from"));
        rv.addProperty("event", extras.getString("event"));
        String s = extras.getString("http_request");
        JsonObject o = JsonUtil.getInstance().parseAsJsonObject(s);
        if (o != null)
            rv.add("http_request", o);
        s = extras.getString("message");
        o = null;
        try {
            o = JsonUtil.getInstance().parseAsJsonObject(s);
        }
        catch (JsonSyntaxException e) {
            o = null;
        }
        if (o == null)
            rv.addProperty("message", s);
        else
            rv.add("message", o);
//        o = null;
        s = extras.getString("trigger_names");
        if (!TextUtils.isEmpty(s)) {
            rv.addProperty("trigger_names", s);
/*
            try {
                o = JsonUtil.getInstance().parseAsJsonObject(s);
            }
            catch (JsonSyntaxException e) {
                o = null;
            }
*/
        }
/*
        if (o != null)
            rv.add("trigger", o);
*/
        return rv;
    }

    public boolean compareStr(final JsonObject o1, final JsonObject o2, final String nm) {
        if (o1 == null || o2 == null)
            return false;
        if (o1.has(nm) && o2.has(nm)) {
            String v1 = o1.get(nm).getAsString();
            String v2 = o2.get(nm).getAsString();
//            Log.d(TAG, StringUtil.format("compareStr: '%s', '%s'", v1, v2));
            return v1.equals(v2);
        }
        return false;
    }

    public boolean compareInt(final JsonObject o1, final JsonObject o2, final String nm) {
        if (o1 == null || o2 == null)
            return false;
        if (o1.has(nm) && o2.has(nm)) {
            int v1 = o1.get(nm).getAsInt();
            int v2 = o2.get(nm).getAsInt();
//            Log.d(TAG, StringUtil.format("compareInt: '%d', '%d'", v1, v2));
            return v1 == v2;
        }
        return false;
    }

    public int coerceInt(final JsonObject obj, final String nm) {
        return coerceInt(obj, nm, 0);
    }

    public int coerceInt(final JsonObject obj, final String nm, final int defVal) {
        if (obj == null || !obj.has(nm))
            return defVal;
        JsonElement e = obj.get(nm);
        if (e.isJsonNull() || !e.isJsonPrimitive())
            return defVal;
        JsonPrimitive p = e.getAsJsonPrimitive();
        return StringUtil.strToIntDef(p.getAsString(), defVal);
/*
        String s = p.getAsString();
        int rv = defVal;
        try {
            rv = Integer.parseInt(s);
        }
        catch (NumberFormatException nfe) {
            rv = defVal;
        }
//        Log.d(TAG, StringUtil.format("coerceInt: %s from %s to %d: ", nm, s, rv));
        return rv;
*/
    }


    public long coerceLong(final JsonObject obj, final String nm) {
        return coerceLong(obj, nm, 0L);
    }

    public long coerceLong(final JsonObject obj, final String nm, final long defVal) {
        if (obj == null || !obj.has(nm))
            return defVal;
        JsonElement e = obj.get(nm);
        if (e.isJsonNull() || !e.isJsonPrimitive())
            return defVal;
        JsonPrimitive p = e.getAsJsonPrimitive();
        return StringUtil.strToLongDef(p.getAsString(), defVal);
//        String s = p.getAsString();
//        long rv = defVal;
//        try {
//            rv = Long.parseLong(s);
//        }
//        catch (NumberFormatException nfe) {
//            rv = defVal;
//        }
////        Log.d(TAG, StringUtil.format("coerceLong: %s from %s to %d: ", nm, s, rv));
//        return rv;
    }

    public String coerceString(final JsonObject obj, final String nm) {
        return coerceString(obj, nm, null);
    }

    public String coerceString(final JsonObject obj, final String nm, final String defVal) {
        if (obj == null || !obj.has(nm))
            return defVal;
        JsonElement e = obj.get(nm);
        if (e.isJsonNull() || !e.isJsonPrimitive())
            return defVal;
        JsonPrimitive p = e.getAsJsonPrimitive();
        if (p.isString())
            return p.getAsString();
        return defVal;
    }
}
