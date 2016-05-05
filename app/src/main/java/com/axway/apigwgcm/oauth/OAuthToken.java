package com.axway.apigwgcm.oauth;

import com.axway.apigwgcm.util.JsonUtil;
import com.google.gson.JsonObject;

/**
 * Created by su on 11/18/2014.
 */
public class OAuthToken {
    private static final String TAG = OAuthToken.class.getSimpleName();

    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_REFRESH_TOKEN = "refresh_token";
    public static final String KEY_TOKEN_TYPE = "token_type";
    public static final String KEY_SCOPE = "scope";
    public static final String KEY_EXPIRES_IN = "expires_in";
    public static final String KEY_EXPIRY = "expiry";

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String scope;
    private long expiresIn;
    private long expiry;

    public OAuthToken() {
        super();
        accessToken = null;
        refreshToken = null;
        tokenType = null;
        scope = null;
        expiresIn = 0;
        expiry = 0;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public long getExpiry() {
        return expiry;
    }

    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public boolean isExpired() {
/*
        long now = System.currentTimeMillis();
        long exp = getExpiry();
        boolean rv =  (now >= exp);
        StringBuilder sb = new StringBuilder();
        sb.append("now: ").append(now).append(", expiry: ").append(exp).append(", delta: ").append(exp-now).append(", isExpired: ").append(rv);
        Log.d(TAG, sb.toString());
*/
        return (System.currentTimeMillis() >= getExpiry());
    }

    public static OAuthToken from(JsonObject json) {
        OAuthToken rv = new OAuthToken();
        if (json.has(KEY_ACCESS_TOKEN))
            rv.setAccessToken(json.get(KEY_ACCESS_TOKEN).getAsString());
        if (json.has(KEY_REFRESH_TOKEN) && !json.get(KEY_REFRESH_TOKEN).isJsonNull())
            rv.setRefreshToken(json.get(KEY_REFRESH_TOKEN).getAsString());
        if (json.has(KEY_TOKEN_TYPE))
            rv.setTokenType(json.get(KEY_TOKEN_TYPE).getAsString());
        if (json.has(KEY_SCOPE))
            rv.setScope(json.get(KEY_SCOPE).getAsString());
        if (json.has(KEY_EXPIRES_IN))
            rv.setExpiresIn(json.get(KEY_EXPIRES_IN).getAsLong());
        if (json.has(KEY_EXPIRY))
            rv.expiry = json.get(KEY_EXPIRY).getAsLong();
        if (rv.expiry == 0)
            rv.expiry = System.currentTimeMillis() + (rv.getExpiresIn() * 1000);
        return rv;
    }

    public static OAuthToken from(String jsonStr) {
        JsonObject json = JsonUtil.getInstance().parseAsJsonObject(jsonStr);
        if (json == null)
            return null;
        return from(json);
    }

    public JsonObject toJson() {
        JsonObject rv = new JsonObject();
        rv.addProperty(KEY_ACCESS_TOKEN, getAccessToken());
        rv.addProperty(KEY_REFRESH_TOKEN, getRefreshToken());
        rv.addProperty(KEY_TOKEN_TYPE, getTokenType());
        rv.addProperty(KEY_SCOPE, getScope());
        rv.addProperty(KEY_EXPIRES_IN, getExpiresIn());
        rv.addProperty(KEY_EXPIRY, getExpiry());
        return rv;
    }
/*
    public static AccessToken from(Bundle b) {
        if (b == null)
            return null;
        AccessToken rv = new AccessToken();
        if (b.containsKey(KEY_ACCESS_TOKEN))
            rv.setAccessToken(b.getString(KEY_ACCESS_TOKEN));
        if (b.containsKey(KEY_REFRESH_TOKEN))
            rv.setRefreshToken(b.getString(KEY_REFRESH_TOKEN));
        if (b.containsKey(KEY_TOKEN_TYPE))
            rv.setTokenType(b.getString(KEY_TOKEN_TYPE));
        if (b.containsKey(KEY_SCOPE))
            rv.setScope(b.getString(KEY_SCOPE));
        if (b.containsKey(KEY_EXPIRES_IN))
            rv.setExpiresIn(b.getLong(KEY_EXPIRES_IN));
        if (b.containsKey(KEY_EXPIRY))
            rv.setExpiry(b.getLong(KEY_EXPIRY));
        return rv;
    }

    public Bundle toBundle() {
        Bundle rv = new Bundle();
        rv.putString(KEY_ACCESS_TOKEN, getAccessToken());
        rv.putString(KEY_REFRESH_TOKEN, getRefreshToken());
        rv.putString(KEY_TOKEN_TYPE, getTokenType());
        rv.putString(KEY_SCOPE, getScope());
        rv.putLong(KEY_EXPIRES_IN, getExpiresIn());
        rv.putLong(KEY_EXPIRY, getExpiry());
        return rv;
    }
*/
    @Override
    public String toString() {
        return toJson().toString();
    }
}
