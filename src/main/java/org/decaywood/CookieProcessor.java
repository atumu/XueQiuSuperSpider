package org.decaywood;

import org.decaywood.utils.FileLoader;
import org.decaywood.utils.MD5Utils;
import org.decaywood.utils.RequestParaBuilder;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * @author: decaywood
 * @date: 2016/04/10 20:01
 */
public interface CookieProcessor {


    default void updateCookie(String website) throws Exception {

        GlobalSystemConfigLoader.loadConfig();

        String areacode = System.getProperty("areaCode");
        String userID = System.getProperty("userID");
        String passwd = System.getProperty("password");
        boolean rememberMe = Boolean.parseBoolean(System.getProperty("rememberMe"));

        HttpURLConnection connection = null;
        if (userID != null && passwd != null) {
            connection = login(areacode, userID, passwd, rememberMe);
        }
        try {
            connection = connection == null ?
                    (HttpURLConnection) new URL(website).openConnection() : connection;
            connection.connect();

            List<String> cookies = connection.getHeaderFields().get("Set-Cookie");
            if (cookies != null) {
                String cookie = cookies
                        .stream()
                        .map(x -> x.split(";")[0].concat(";"))
                        .filter(x -> x.contains("token=") || x.contains("s="))
                        .reduce("", String::concat);
                FileLoader.updateCookie(cookie, website);
            }
        } finally {
            if (connection != null) connection.disconnect();
        }

    }

    default HttpURLConnection login(String areacode,
                                    String userID,
                                    String passwd,
                                    boolean rememberMe) throws Exception {

        areacode = areacode == null ? "86" : areacode;
        if (userID == null || passwd == null) {
            throw new IllegalArgumentException("null parameter: userID or password");
        }

        RequestParaBuilder builder = new RequestParaBuilder("http://xueqiu.com/user/login")
                .addParameter("areacode", areacode)
                .addParameter("username", userID)
                .addParameter("password", MD5Utils.parseStrToMd5U32(passwd))
                .addParameter("remember_me", rememberMe ? "on" : "off");

        URL url = new URL(builder.build());
        return (HttpURLConnection) url.openConnection();
    }


}
