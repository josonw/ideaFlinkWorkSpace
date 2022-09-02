package com.huawei.bigdata.flink.examples;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huawei.bigdata.flink.examples.bean.caseName;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.Properties;

public class RealDataStreamFromKafkaprocess {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        String topic = "demo";
        Properties prop = new Properties();
        prop.setProperty("bootstrap.servers", "192.168.10.110:9092");
        prop.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        prop.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        prop.setProperty("group.id", "consumer1");
        prop.setProperty("auto.offset.reset", "latest");
        DataStreamSource<String> kafkaSource = env.addSource(new FlinkKafkaConsumer<String>(
                topic,
                new SimpleStringSchema(),
                prop
        ));

        SingleOutputStreamOperator<caseName> caseValue = kafkaSource.map(new StringTojsonString());

        caseValue.print();

        caseValue.addSink(JdbcSink.sink(
                "INSERT INTO envent_table (name,entry_time,name_manual_type,remark,update_time) values (?, ?,?,?,?)",
                (statement, kafkacase) -> {
                    statement.setString(1, kafkacase.name);
                    statement.setString(2, kafkacase.entry_time);
                    statement.setString(3, kafkacase.name_manual_type);
                    statement.setString(4, kafkacase.remark);
                    statement.setString(5, kafkacase.update_time);
                },
                JdbcExecutionOptions.builder()
                        .withBatchSize(1000)
                        .withBatchIntervalMs(200)
                        .withMaxRetries(5)
                        .build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl("jdbc:postgresql://192.168.10.110:5432/test_pg")
//                        .withUrl("jdbc:postgresql://43.138.134.40:5432/postgres")
                        .withDriverName("org.postgresql.Driver")
                        .withUsername("postgres")
                        .withPassword("postgres")
                        .build()

        ));


//        kafkaSource.print();


        env.execute();
    }


    /**
     * {"value":[{"name":"井水", "entry_time":"1661756734987", "name_manual_type":"国考", "remark":"统考", "update_time":"1661756734987"}]}
     *
     * 解析（fastjson） kafka JsonString  每个字段，封装pojo类中
     */

    public static class StringTojsonString implements MapFunction<String, caseName> {

        @Override
        public caseName map(String data) throws Exception {

            JSONObject jsonObject = JSON.parseObject(data);
            JSONArray jsonArray = jsonObject.getJSONArray("value");
            JSONObject value = jsonArray.getJSONObject(0);

            String name = value.getString("name");
            String entry_time = value.getString("entry_time");
            String name_manual_type = value.getString("name_manual_type");
            String remark = value.getString("remark");
            String update_time = value.getString("update_time");

            return new caseName(name, entry_time, name_manual_type, remark, update_time);
        }
    }
}
