package com.example.seckill.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class JsonUtil {
    private static ObjectMapper objectMapper=new ObjectMapper();

    public static String object2JsonStr(Object obj){
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e){
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T jsonStr2Object(String jsonStr,Class<T> clazz){
        try {
            return objectMapper.readValue(jsonStr.getBytes(StandardCharsets.UTF_8),clazz);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static  <T> List<T> jsonToList(String jsonStr,Class<T> clazz){
        JavaType javaType=objectMapper.getTypeFactory().constructParametricType(List.class,clazz);
        try {
            List<T> list=objectMapper.convertValue(jsonStr,javaType);
            return list;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
