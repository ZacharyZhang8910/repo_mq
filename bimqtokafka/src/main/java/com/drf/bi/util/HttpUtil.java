package com.drf.bi.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.drf.bi.config.Constant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * HTTP工具类
 *
 * @author jian.zhang
 * <p>
 * DateTime 2019/5/13 18:45
 */
@Slf4j
public class HttpUtil {

    /**
     * POST请求
     *
     * @param path  请求地址
     * @param param 请求参数
     * @return 返回结果字符串
     * @throws IOException 异常
     */
    private static String post(String path, String param) throws Exception {
        HttpURLConnection connection = null;
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            URL url = new URL(path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            out = new PrintWriter(connection.getOutputStream());
            out.println(param);
            out.flush();

            StringBuilder sb = new StringBuilder();
            String tmp;
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((tmp = in.readLine()) != null) {
                sb.append(tmp);
            }
            return sb.toString();
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 请求对应接口，获取JSON信息
     *
     * @param apiMaps      接口参数
     * @param param        请求参数
     * @param subDataField 二级key,对于{a:123, data:{b1:'abc'}}这种，获取data数据后，还需要根据二级key取得b1中的对象
     * @return 返回JSONArray对象，对于无数据返回一个空的JSONArray对象
     */
    public static JSONArray getJsonArray(Map<String, String> apiMaps, String param, String... subDataField) {
        String url = apiMaps.get(Constant.API_URL);
        String statusField = apiMaps.get(Constant.API_STATUS_FIELD);
        String statusCode = apiMaps.get(Constant.API_STATUS_CODE);
        String dataField = apiMaps.get(Constant.API_DATA_FIELD);
        String dataType = apiMaps.get(Constant.API_DATA_TYPE);
        log.debug(String.format("%s, param:%s", url, param));
        try {
            String info = post(url, param);
            if (StringUtils.isBlank(info)) {
                throw new RuntimeException("接口结果为空");
            }
            JSONObject json = JSON.parseObject(info);
            Object status = json.get(statusField);

            if (status != null && status.toString().equals(statusCode)) {
                JSONArray result = new JSONArray();
                if (dataType.equals("JSONArray")) {
                    result = json.getJSONArray(dataField);
                } else if (dataType.equals("JSONObject")) {
                    JSONObject jsonObject = json.get(dataField) == null ? null : json.getJSONObject(dataField);
                    if (jsonObject != null && subDataField != null && subDataField.length > 0) {
                        int count = 0;
                        do {
                            jsonObject = jsonObject.getJSONObject(subDataField[count]);
                            if (jsonObject == null) {
                                break;
                            }
                        } while (++count < subDataField.length);
                    }
                    result.add(jsonObject);
                }
                return result;
            } else {
                String errorCode = json.getString("code");
                String errorMsg = json.getString("msg");
                throw new RuntimeException(String.format("查询接口失败,url:%s, param:%s, errorCode:%s, errorMsg:%s", url, param, errorCode, errorMsg));
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("查询接口失败, url:%s, param:%s", url, param), e);
        }
    }
}