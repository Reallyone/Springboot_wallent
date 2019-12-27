package com.really.spring_wallent.utils;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * 使用HttpURLConnection请求url的工具类
 */
@Slf4j
public class HttpURLConnUtils {


    public static String url;

    public static JSONObject doPost(List params, String Interface) {
        URL url = null;
        StringBuffer buffer = new StringBuffer();
        JSONObject jsonObject = null;
        try {
            url = new URL(HttpURLConnUtils.url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //设置参数
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            //写参数
            String jsonParms = JSON.toJSONString(params);
            String param = "{\"jsonrpc\":\"2.0\",\"method\":\"" + Interface + "\",\"params\":" + jsonParms + ",\"id\":0}";

            OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
            osw.write(param);
            osw.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line = null;

            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            jsonObject = JSONObject.parseObject(buffer.toString());
            closeIO(null, buffer, reader, osw, conn);
        } catch (ConnectException e) {
            log.error(url + "  --api server connection timed out.");
        } catch (IOException ie) {
            log.error(url + "  --https request error:{}", ie);
        }
        return jsonObject;
    }

    /**
     * 关闭IO流
     */
    private static void closeIO(DataOutputStream out, StringBuffer stringBuffer, BufferedReader reader, OutputStreamWriter osw, HttpURLConnection conn) {
        // 关闭DataOutputStream
        try {
            if (out != null) {
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //关闭stringbuffer
        if (stringBuffer != null) {
            stringBuffer.setLength(0);
        }
        try {
            if (osw != null) {
                osw.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != conn) {
            conn.disconnect();
        } else {
            log.error("  --api server connection timed out.");
        }
    }


}