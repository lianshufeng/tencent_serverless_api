package com.github.jrebel.model;

import com.github.jrebel.core.util.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ResponseModel {

    private boolean isBase64Encoded = false;

    private int statusCode = 200;

    private Map<String, Object> headers = new HashMap<>() {{
        put("Content-Type", "application/json; charset=utf-8");
    }};

    private String body;


    public String toJson() {
        return JsonUtil.toJson(this);
    }


}
