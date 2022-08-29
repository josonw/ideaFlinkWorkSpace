package com.huawei.bigdata.flink.examples;

import bean.Kafkacase;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

import java.util.*;

public class RealDataStreamToKafka {

    public static void main(String[] args) throws Exception {


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        String topic = "demo";
        Properties prop = new Properties();
        prop.setProperty("bootstrap.servers", "192.168.10.110:9092");
        prop.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        prop.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        prop.setProperty("group.id", "consumer1");



        DataStreamSource dataSource = env.addSource(new JsonString());

        dataSource.print();


        /**
         * 构造选择
         *public FlinkKafkaProducer(String topicId, SerializationSchema<IN> serializationSchema, Properties producerConfig)
         *  SerializationSchema 选择简单的 SimpleStringSchema
         *
         * 如果需要对消息简单的处理可以选择： KafkaSerializationSchema
         */
        dataSource.addSink(new FlinkKafkaProducer<String>(
                topic,
                new SimpleStringSchema(),
                prop
        ));


        env.execute();
    }

    public static class JsonString implements SourceFunction<String> {

        private static Boolean running = true;

        @Override
        public void run(SourceContext<String> ct) throws Exception {
//            name,entry_time,name_manual_type,remark,update_time

            while (running) {
                Random rd = new Random();
                List<String> nameString = Arrays.asList("地下水", "自来水", "山泉水", "井水");
                List<String> name_manual_typeString = Arrays.asList("国考", "省考", "联考", "统考");
                List<String> remarktypeString = Arrays.asList("监测站手工", "观察手工", "检验手工", "抽查");
                ct.collect(
                        new Kafkacase(nameString.get(rd.nextInt(4)),
                                String.valueOf(System.currentTimeMillis()),
                                name_manual_typeString.get(rd.nextInt(4)),
                                name_manual_typeString.get(rd.nextInt(4)),
                                String.valueOf(Calendar.getInstance().getTimeInMillis())
                        ).toString()

                );
                Thread.sleep(1000);

            }

        }

        @Override
        public void cancel() {
            running = false;
        }
    }

}
