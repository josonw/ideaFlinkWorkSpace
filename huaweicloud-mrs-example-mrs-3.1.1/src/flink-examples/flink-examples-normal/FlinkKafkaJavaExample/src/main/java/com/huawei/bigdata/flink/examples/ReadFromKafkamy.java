package com.huawei.bigdata.flink.examples;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.huawei.bigdata.flink.examples.bean.Kafkacase;
import org.apache.flink.api.common.functions.MapFunction;

import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;


import java.util.Date;
import java.util.Properties;

public class ReadFromKafkamy {

    public static void main(String[] args) throws Exception {


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

//        String topic = "test";
//        Properties prop = new Properties();
//        prop.setProperty("bootstrap.servers", "10.224.153.74:9097,10.224.153.74:9098,10.224.153.74:9095,10.224.153.74:9096,10.224.153.74:9094,10.224.153.74:9103,10.224.153.74:9101,10.224.153.74:9102,10.224.153.74:9099,10.224.153.74:9100");//多个的话可以指定
//        prop.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
//        prop.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
//        prop.setProperty("auto.offset.reset", "earliest");
//        prop.setProperty("group.id", "consumer1");
//        FlinkKafkaConsumer<String> SourceSafe = new FlinkKafkaConsumer<String>(topic, new SimpleStringSchema(), prop);
//        DataStreamSource<String> kafkaSource = env.addSource(SourceSafe);

        DataStreamSource dataSource = env.addSource(
                new SourceFunction<String>() {
                    @Override
                    public void run(SourceContext<String> context) throws Exception {
                     String content=  "{\n" +
                               "    \"value\":[\n" +
                               "        {\n" +
                               "            \"name_wave\":\"地下水\",\n" +
                               "            \"entry_time\":\"2020-09-25 18:28:19\",\n" +
                               "            \"name_monitor_type\":\"地下水\",\n" +
                               "            \"name_manual_type\":\"国考\",\n" +
                               "            \"latitude\":\"22.575785\",\n" +
                               "            \"remark\":\"监测站手工\",\n" +
                               "            \"code_manual_type\":1,\n" +
                               "            \"point_name\":\"恩上村\",\n" +
                               "            \"point_code\":\"GD-14-014\",\n" +
                               "            \"assessment_level\":\"0\",\n" +
                               "            \"is_black\":\"0\",\n" +
                               "            \"update_time\":\"2021-07-16 14:17:07\",\n" +
                               "            \"monitor_mode\":\"0\",\n" +
                               "            \"code_monitor_type\":\"5\",\n" +
                               "            \"system_source\":\"思路数据库\",\n" +
                               "            \"longitude\":114.246642,\n" +
                               "            \"region_code\":\"440308000000\",\n" +
                               "            \"code_wave\":\"2\",\n" +
                               "            \"rksj\":\"2022-08-19 16:31:31\",\n" +
                               "            \"ywsj\":null,\n" +
                               "            \"ywdwtyshxydm\":\"11440300MB2C93125R\",\n" +
                               "            \"sbdwtyshxydm\":\"11440300MB2C93166R\"\n" +
                               "        }\n" +
                               "    ]\n" +
                               "}";
                     context.collect(content);
                    }
                    @Override
                    public void cancel() {

                    }
                }
        );


//        SingleOutputStreamOperator mapSource = dataSource.map(data -> {
//            JSONObject jsonObt = JSON.parseObject((String) data);
//            Object value = jsonObt.get("value");
//            return value;
//        });


        //注意这边要写上返回值的类型<Kafkacase>，否则 底下jdbc不知道类的字段
        SingleOutputStreamOperator<Kafkacase> jsonmap = dataSource.map(new maptoJson());

        jsonmap.print();

        jsonmap.addSink(JdbcSink.sink(
                "INSERT INTO envent_table (name,entry_time,name_manual_type,remark,update_time) values (?, ?,?,?,?)",
                (statement, kafkacase) -> {
                    statement.setString(1, kafkacase.name);
                    statement.setString(2, kafkacase.entry_time);
                    statement.setString(3, kafkacase.name_manual_type);
                    statement.setString(4,kafkacase.remark);
                    statement.setString(5, kafkacase.update_time);
                },
                JdbcExecutionOptions.builder()
                        .withBatchSize(1000)
                        .withBatchIntervalMs(200)
                        .withMaxRetries(5)
                        .build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl("jdbc:postgresql://192.168.10.110:5432/test_pg")
                        .withDriverName("org.postgresql.Driver")
                        .withUsername("postgres")
                        .withPassword("postgres")
                        .build()


        ));




        env.execute();


    }

    public  static  class    maptoJson implements MapFunction<String,Kafkacase> {

        @Override
        public Kafkacase map(String data) throws Exception {
            //把Kafka 中的json字符串转换成Json 类型5，方便取出json中的key
            JSONObject jsonObject = JSON.parseObject(data);
//            解析 key:value,对应的是JsonArray,不能用这个方法
//            JSONObject value = jsonObject.getJSONObject("value");  //com.alibaba.fastjson.JSONArray cannot be cast to com.alibaba.fastjson.JSONObject
            // 取出对应key:value,对应的array
            JSONArray arrayvalue = jsonObject.getJSONArray("value");
            //获取array中的第几个Json对象，这里模拟只有一个，现实中可以用循环取出所以数据
            JSONObject value = arrayvalue.getJSONObject(0);
            //以下就是取Json中每个key 的内容，根据key 的类型进行获取
            String name = value.getString("name_wave");
            String entry_time = value.getString("entry_time");
            String name_manual_type = value.getString("name_manual_type");
            String remark = value.getString("remark");
            String update_time = value.getString("update_time");

            Integer code_manual_type = value.getInteger("code_manual_type");

         return    new Kafkacase(name,entry_time,name_manual_type,remark,update_time);
//            String res=name+" "+entry_time+" "+code_manual_type;
//            return res;

        }
    }
}

