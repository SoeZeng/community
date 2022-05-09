package com.nowcoder.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommunityUtil {

    //生成随机字符串
    public static String generateUUID() {
        //将所有横线替换为空字符串
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    //MD5加密，只能加密，不能解密，每次加密都是同一个值
    //解决方式：增加随机字符串后再进行加密
    public static String MD5(String key) {
        //当key为null、空串、空格都为判定为空
        if(StringUtils.isBlank(key)) {
            return null;
        }
        //将传入的值加密成一个十六进制的结果返回
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        json.put("code",code);
        json.put("msg",msg);
        if(map != null) {
            for(String key : map.keySet()) {
                json.put(key,map.get(key));

            }
        }
        return json.toJSONString();
    }

    public static String getJSONString(int code, String msg) {

        return getJSONString(code,msg,null);
    }

//    public static String getJSONString(int code, Map<String, Object> map) {
//
//        return getJSONString(code,null,map);
//    }

    public static String getJSONString(int code) {
        return getJSONString(code,null,null);
    }

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("name","zhangsan");
        map.put("age",25);
        System.out.println(getJSONString(0,"ok",map));
    }



}
