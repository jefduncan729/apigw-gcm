package com.axway.apigwgcm.util;

import java.util.ArrayList;

/**
 * Created by su on 12/18/2014.
 */
public class HttpUtil {

    private static final String TAG = HttpUtil.class.getSimpleName();

    public static final String CUSTOM_HEADER= "Custom Header";
    public static final String ACCEPT= "Accept";
    public static final String ACCEPT_CHARSET= "Accept-Charset";
    public static final String ACCEPT_ENCODING= "Accept-Encoding";
    public static final String ACCEPT_LANGUAGE= "Accept-Language";
    public static final String ACCEPT_RANGES= "Accept-Ranges";
    public static final String AGE= "Age";
    public static final String ALLOW= "Allow";
    public static final String AUTHORIZATION= "Authorization";
    public static final String CACHE_CONTROL= "Cache-Control";
    public static final String CONNECTION= "Connection";
    public static final String CONTENT_DISPOSITION= "Content-Disposition";
    public static final String CONTENT_ENCODING= "Content-Encoding";
    public static final String CONTENT_LANGUAGE= "Content-Language";
    public static final String CONTENT_LENGTH= "Content-Length";
    public static final String CONTENT_LOCATION= "Content-Location";
    public static final String CONTENT_RANGE= "Content-Range";
    public static final String CONTENT_TYPE= "Content-Type";
    public static final String COOKIE= "Cookie";
    public static final String DATE= "Date";
    public static final String ETAG= "ETag";
    public static final String EXPECT= "Expect";
    public static final String EXPIRES= "Expires";
    public static final String FROM= "From";
    public static final String HOST= "Host";
    public static final String IF_MATCH= "If-Match";
    public static final String IF_MODIFIED_SINCE= "If-Modified-Since";
    public static final String IF_NONE_MATCH= "If-None-Match";
    public static final String IF_RANGE= "If-Range";
    public static final String IF_UNMODIFIED_SINCE= "If-Unmodified-Since";
    public static final String LAST_MODIFIED= "Last-Modified";
    public static final String LINK= "Link";
    public static final String LOCATION= "Location";
    public static final String MAX_FORWARDS= "Max-Forwards";
    public static final String ORIGIN= "Origin";
    public static final String PRAGMA= "Pragma";
    public static final String PROXY_AUTHENTICATE= "Proxy-Authenticate";
    public static final String PROXY_AUTHORIZATION= "Proxy-Authorization";
    public static final String RANGE= "Range";
    public static final String REFERER= "Referer";
    public static final String RETRY_AFTER= "Retry-After";
    public static final String SERVER= "Server";
    public static final String SET_COOKIE= "Set-Cookie";
    public static final String SET_COOKIE2= "Set-Cookie2";
    public static final String TE= "TE";
    public static final String TRAILER= "Trailer";
    public static final String TRANSFER_ENCODING= "Transfer-Encoding";
    public static final String UPGRADE= "Upgrade";
    public static final String USER_AGENT= "User-Agent";
    public static final String VARY= "Vary";
    public static final String VIA= "Via";
    public static final String WARNING= "Warning";
    public static final String WWW_AUTHENTICATE= "WWW-Authenticate";

    private static ArrayList<String> headerNames;

    public static ArrayList<String> getHeaderNames() {
        if (headerNames == null) {
            headerNames = new ArrayList<String>();
            headerNames.add(CUSTOM_HEADER);
            headerNames.add(ACCEPT); //"Accept"
            headerNames.add(ACCEPT_CHARSET); //"Accept-Charset"
            headerNames.add(ACCEPT_ENCODING); //"Accept-Encoding"
            headerNames.add(ACCEPT_LANGUAGE); //"Accept-Language"
            headerNames.add(ACCEPT_RANGES); //"Accept-Ranges"
            headerNames.add(AGE); //"Age"
            headerNames.add(ALLOW); //"Allow"
            headerNames.add(AUTHORIZATION); //"Authorization"
            headerNames.add(CACHE_CONTROL); //"Cache-Control"
            headerNames.add(CONNECTION); //"Connection"
            headerNames.add(CONTENT_DISPOSITION); //"Content-Disposition"
            headerNames.add(CONTENT_ENCODING); //"Content-Encoding"
            headerNames.add(CONTENT_LANGUAGE); //"Content-Language"
            headerNames.add(CONTENT_LENGTH); //"Content-Length"
            headerNames.add(CONTENT_LOCATION); //"Content-Location"
            headerNames.add(CONTENT_RANGE); //"Content-Range"
            headerNames.add(CONTENT_TYPE); //"Content-Type"
            headerNames.add(COOKIE); //"Cookie"
            headerNames.add(DATE); //"Date"
            headerNames.add(ETAG); //"ETag"
            headerNames.add(EXPECT); //"Expect"
            headerNames.add(EXPIRES); //"Expires"
            headerNames.add(FROM); //"From"
            headerNames.add(HOST); //"Host"
            headerNames.add(IF_MATCH); //"If-Match"
            headerNames.add(IF_MODIFIED_SINCE); //"If-Modified-Since"
            headerNames.add(IF_NONE_MATCH); //"If-None-Match"
            headerNames.add(IF_RANGE); //"If-Range"
            headerNames.add(IF_UNMODIFIED_SINCE); //"If-Unmodified-Since"
            headerNames.add(LAST_MODIFIED); //"Last-Modified"
            headerNames.add(LINK); //"Link"
            headerNames.add(LOCATION); //"Location"
            headerNames.add(MAX_FORWARDS); //"Max-Forwards"
            headerNames.add(ORIGIN); //"Origin"
            headerNames.add(PRAGMA); //"Pragma"
            headerNames.add(PROXY_AUTHENTICATE); //"Proxy-Authenticate"
            headerNames.add(PROXY_AUTHORIZATION); //"Proxy-Authorization"
            headerNames.add(RANGE); //"Range"
            headerNames.add(REFERER); //"Referer"
            headerNames.add(RETRY_AFTER); //"Retry-After"
            headerNames.add(SERVER); //"Server"
            headerNames.add(SET_COOKIE); //"Set-Cookie"
            headerNames.add(SET_COOKIE2); //"Set-Cookie2"
            headerNames.add(TE); //"TE"
            headerNames.add(TRAILER); //"Trailer"
            headerNames.add(TRANSFER_ENCODING); //"Transfer-Encoding"
            headerNames.add(UPGRADE); //"Upgrade"
            headerNames.add(USER_AGENT); //"User-Agent"
            headerNames.add(VARY); //"Vary"
            headerNames.add(VIA); //"Via"
            headerNames.add(WARNING); //"Warning"
            headerNames.add(WWW_AUTHENTICATE); //"WWW-Authenticate"
        }
        return headerNames;
    }

    private HttpUtil() {
        super();
    }
}
