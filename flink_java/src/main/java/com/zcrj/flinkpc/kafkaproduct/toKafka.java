package com.zcrj.flinkpc.kafkaproduct;


import com.zcrj.flinkpc.bean.StatusKeyValueMap;
import com.zcrj.flinkpc.bean.TestRecord;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import com.alibaba.fastjson.JSON;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;


/**
 *
 * flink to kafka
 * 数据格式如下：
 * {"num":400,"statusKeyValueMap":{"f1":"drivering","f2":30,"f3":333.333},"ts":1662083387259,"vin":"20200712082200003"}
 */
public class toKafka {
    public static void main(String[] args) throws Exception {
        String topic = "flink_test";
        Properties prop = new Properties();
        prop.setProperty("bootstrap.servers", "192.168.10.110:9092");
        prop.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        prop.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        prop.setProperty("group.id", "consumer1");

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.addSource(new customSounce()).addSink(
                new FlinkKafkaProducer<String>(
                        topic,
                        new SimpleStringSchema(),
                        prop
                )
        );

        env.execute();

    }

    public static class customSounce implements SourceFunction<String> {

        final List<Integer> numList = Arrays.asList(100, 200, 300, 400);
        final List<String> vinList = Arrays.asList("20200712082200001", "20200712082200002", "20200712082200003", "20200712082200004");
        final List<String> f1List = Arrays.asList("stop", "start", "drivering", "push");
        final List<Integer> f2List = Arrays.asList(10, 20, 30, 40);
        final List<Double> f3List = Arrays.asList(Double.parseDouble("111.111"), Double.parseDouble("222.222"), Double.parseDouble("333.333"), Double.parseDouble("444.444"));
        private static Boolean run = true;

        @Override
        public void run(SourceContext<String> context) throws Exception {
            while (run) {
                Random random = new Random();
                StatusKeyValueMap statusKeyValueMap = new StatusKeyValueMap(f1List.get(random.nextInt(4)), f2List.get(random.nextInt(4)), f3List.get(random.nextInt(4)));
                TestRecord testRecord = new TestRecord(numList.get(random.nextInt(4)), System.currentTimeMillis(), vinList.get(random.nextInt(4)), statusKeyValueMap);
                final String value = JSON.toJSONString(testRecord);
                context.collect(value);
                System.out.println(value);
                Thread.sleep(1000);
            }
        }

        @Override
        public void cancel() {
            run = false;
        }
    }
}
