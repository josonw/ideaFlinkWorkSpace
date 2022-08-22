package com.huawei.bigdata.flink.examples;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class testJson {

    public static void main(String[] args) {
        String s = "{\"result\": {\"data\": [{\"code\": \"CF327\",\"value\": \"0.0016592020664306757\"},{\"code\": \"CF327\",\"value\": \"0.0016592020664306757\"}],\"message\": \"计算成功！\",\"status\": 0}}";
        String content = "\"value\": \"[{\"name_wave\":\"地下水\",\"entry_time\":\"2020-09-25 18:28:19\",\"name_monitor_type\":\"地下水\",\"name_manual_type\":\"国考\",\"latitude\":22.575785,\n" + "\"remark\":\"监测站手工\",\"code_manual_type\":1,\"point_name\":\"恩上村\",\"point_code\":\"GD-14-014\",\"assessment_level\":0,\"is_black\":0,\"update_time\":\"2021-07-16 14:17:07\",\n" + "\"monitor_mode\":\"0 \",\"code_monitor_type\":5,\"system_source\":\"思路数据库\",\"longitude\":114.246642,\"region_code\":\"440308000000\",\"code_wave\":2,\n" + "\"rksj\":\"2022-08-19 16:31:31\",\"ywsj\":null,\"ywdwtyshxydm\":\"11440300MB2C93125R\",\"sbdwtyshxydm\":\"11440300MB2C93166R\"}]";

        JSONObject jsonObject = JSON.parseObject(content);
        Object result = jsonObject.get("value");

        System.out.println(result);
    }
}
