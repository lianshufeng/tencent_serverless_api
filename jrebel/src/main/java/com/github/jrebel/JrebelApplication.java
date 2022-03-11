package com.github.jrebel;


import com.github.jrebel.core.util.JsonUtil;
import com.github.jrebel.core.util.StreamUtils;
import com.github.jrebel.core.util.jrebel.util.JrebelSign;
import com.github.jrebel.model.ResponseModel;
import lombok.Cleanup;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JrebelApplication {

    @SneakyThrows
    public static void main(String[] args) {
        @Cleanup FileInputStream fileInputStream = new FileInputStream(new File("c:/test.json"));
        String eventText = StreamUtils.copyToString(fileInputStream, Charset.forName("UTF-8"));
        String ret = new JrebelApplication().mainHandler(eventText);
        System.out.println(ret);
    }

    @SneakyThrows
    public String mainHandler(String eventText) {
        final Map<String, Object> event = JsonUtil.toObject(eventText, Map.class);
        final Map<String, Object> requestContext = (Map<String, Object>) event.get("requestContext");
        final String contextPath = String.valueOf(requestContext.get("path"));
        final String path = String.valueOf(event.get("path")).substring(contextPath.length());

        if (path == null || "".equals(path)) {
            return ResponseModel.builder().statusCode(500).body("error").build().toJson();
        }

        ResponseModel responseModel = null;
        switch (path) {
            case "/jrebel/leases":
                responseModel = jrebelLeasesHandler(event);
                break;
            case "/jrebel/leases/1":
                responseModel = jrebelLeases1Handler(event);
                break;
            case "/agent/leases":
                responseModel = jrebelLeasesHandler(event);
                break;
            case "/agent/leases/1":
                responseModel = jrebelLeases1Handler(event);
                break;
            case "/jrebel/validate-connection":
                responseModel = jrebelValidateHandler(event);
                break;
            default:
                responseModel = new ResponseModel().setBody("{}").setStatusCode(200);
        }

        return responseModel.setStatusCode(200).toJson();
    }


    @SneakyThrows
    public ResponseModel jrebelLeasesHandler(Map<String, Object> event) {
        final Map<String, String> body = form(String.valueOf(event.get("body")));
        String clientRandomness = body.get("randomness");
        String username = body.get("username");
        String guid = body.get("guid");
        boolean offline = Boolean.parseBoolean(body.get("offline"));
        String validFrom = "null";
        String validUntil = "null";
        if (offline) {
            String clientTime = body.get("clientTime");
            String offlineDays = body.get("offlineDays");
            long clinetTimeUntil = Long.parseLong(clientTime) + 180L * 24 * 60 * 60 * 1000;
            validFrom = clientTime;
            validUntil = String.valueOf(clinetTimeUntil);
        }
        String jsonStr = "{\n" + "    \"serverVersion\": \"3.2.4\",\n" + "    \"serverProtocolVersion\": \"1.1\",\n" + "    \"serverGuid\": \"a1b4aea8-b031-4302-b602-670a990272cb\",\n" + "    \"groupType\": \"managed\",\n" + "    \"id\": 1,\n" + "    \"licenseType\": 1,\n" + "    \"evaluationLicense\": false,\n" + "    \"signature\": \"OJE9wGg2xncSb+VgnYT+9HGCFaLOk28tneMFhCbpVMKoC/Iq4LuaDKPirBjG4o394/UjCDGgTBpIrzcXNPdVxVr8PnQzpy7ZSToGO8wv/KIWZT9/ba7bDbA8/RZ4B37YkCeXhjaixpmoyz/CIZMnei4q7oWR7DYUOlOcEWDQhiY=\",\n" + "    \"serverRandomness\": \"H2ulzLlh7E0=\",\n" + "    \"seatPoolType\": \"standalone\",\n" + "    \"statusCode\": \"SUCCESS\",\n" + "    \"offline\": " + String.valueOf(offline) + ",\n" + "    \"validFrom\": " + validFrom + ",\n" + "    \"validUntil\": " + validUntil + ",\n" + "    \"company\": \"Administrator\",\n" + "    \"orderId\": \"\",\n" + "    \"zeroIds\": [\n" + "        \n" + "    ],\n" + "    \"licenseValidFrom\": 1490544001000,\n" + "    \"licenseValidUntil\": 1691839999000\n" + "}";

        if (clientRandomness == null || username == null || guid == null) {
            return ResponseModel.builder().statusCode(302).build();
        } else {
            Map map = JsonUtil.toObject(jsonStr, Map.class);
            JrebelSign jrebelSign = new JrebelSign();
            jrebelSign.toLeaseCreateJson(clientRandomness, guid, offline, validFrom, validUntil);
            String signature = jrebelSign.getSignature();
            map.put("signature", signature);
            map.put("company", username);
            return new ResponseModel().setBody(JsonUtil.toJson(map));
        }

    }


    @SneakyThrows
    public ResponseModel jrebelLeases1Handler(Map<String, Object> event) {
        final Map<String, String> body = form(String.valueOf(event.get("body")));
        String username = body.get("username");
        String jsonStr = "{\n" + "    \"serverVersion\": \"3.2.4\",\n" + "    \"serverProtocolVersion\": \"1.1\",\n" + "    \"serverGuid\": \"a1b4aea8-b031-4302-b602-670a990272cb\",\n" + "    \"groupType\": \"managed\",\n" + "    \"statusCode\": \"SUCCESS\",\n" + "    \"msg\": null,\n" + "    \"statusMessage\": null\n" + "}\n";
        Map map = JsonUtil.toObject(jsonStr, Map.class);
        if (username != null) {
            map.put("company", username);
        }
        return new ResponseModel().setBody(JsonUtil.toJson(map));
    }

    public ResponseModel jrebelValidateHandler(Map<String, Object> event) {
        String jsonStr = "{\n" + "    \"serverVersion\": \"3.2.4\",\n" + "    \"serverProtocolVersion\": \"1.1\",\n" + "    \"serverGuid\": \"a1b4aea8-b031-4302-b602-670a990272cb\",\n" + "    \"groupType\": \"managed\",\n" + "    \"statusCode\": \"SUCCESS\",\n" + "    \"company\": \"Administrator\",\n" + "    \"canGetLease\": true,\n" + "    \"licenseType\": 1,\n" + "    \"evaluationLicense\": false,\n" + "    \"seatPoolType\": \"standalone\"\n" + "}\n";

        return new ResponseModel().setBody(jsonStr);
    }
//


    @SneakyThrows
    private static Map<String, String> form(String body) {
        final Map<String, String> ret = new HashMap<>();
        List.of(body.split("&")).forEach((it) -> {
            String[] kv = it.split("=");
            if (kv.length > 1) {
                ret.put(URLDecoder.decode(kv[0], Charset.forName("UTF-8")), URLDecoder.decode(kv[1], Charset.forName("UTF-8")));
            }
        });
        return ret;
    }
}
