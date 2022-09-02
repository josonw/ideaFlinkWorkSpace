package com.zcrj.flinkpc.flinksql.kafkaconn;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.*;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class sqlKakfaDimMysqlToMysql {

    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);


        String createKafkaSourceTable = "CREATE TABLE KafkaTable (\n" +
                "  `num` BIGINT,\n" +
                "  `ts` BIGINT,\n" +
                " `vin` STRING,\n" +
                "  `statusKeyValueMap` ROW(f1 STRING, f2 INT,f3 DOUBLE),\n" +
//              "  `rowtime` TIMESTAMP(3) METADATA FROM 'timestamp' \n" +  //timestamp 是数据今日kafka的时间
                "  `rowtime` as  to_timestamp(FROM_UNIXTIME(`ts`/1000,'yyyy-MM-dd HH:mm:ss')), \n" +
                " `pts` AS PROCTIME()   \n" +
                ") WITH (\n" +
                "  'connector' = 'kafka',\n" +
                "  'topic' = 'flink_test',\n" +
                "  'properties.bootstrap.servers' = '192.168.10.110:9092',\n" +
                "  'properties.group.id' = 'testGroup',\n" +
                "  'scan.startup.mode' = 'earliest-offset',\n" +
                "  'format' = 'json'\n" +
                ")";

        String createMyslDimTable = "CREATE TABLE mysqlDimTable (\n" +
                "  id BIGINT,\n" +
                "  vin STRING,\n" +
                "  brand_name STRING,\n" +
                "  PRIMARY KEY (id) NOT ENFORCED \n" +
                ") WITH ( \n" +
                "   'connector' = 'jdbc',\n" +
                "   'url' = 'jdbc:mysql://localhost:3306/test?serverTimezone=UTC&useSSL=false',\n" +
                "   'table-name' = 'vin_brand' ,\n" +
                "   'username' = 'root' ,\n" +
                "   'password' = '123123' \n" +
                ")";

        String createResultMysqlTable = "CREATE TABLE mysqlResult (\n" +
                "  vin STRING,\n" +
                "  brand_name STRING,\n" +
                "  f1 STRING,\n" +
                "  f2 INT,\n" +
                "  PRIMARY KEY (vin) NOT ENFORCED \n" +
                ") WITH (\n" +
                "   'connector' = 'jdbc', \n" +
                "   'url' = 'jdbc:mysql://localhost:3306/test?serverTimezone=UTC&useSSL=false',\n" +
                "   'username' = 'root', \n" +
                "   'password' = '123123', \n" +
                "   'lookup.cache.max-rows' = '5000', \n" +
                "   'lookup.cache.ttl' = '1min', \n" +
                "   'table-name' = 'mysql_result' \n" +
                ")";
        String insertMysql = "INSERT INTO mysqlResult select k.vin,m.brand_name,k.statusKeyValueMap.f1 AS f1,k.statusKeyValueMap.f2 AS f2 " +
                " from KafkaTable k left JOIN mysqlDimTable  FOR SYSTEM_TIME AS OF k.pts AS m " +
                "  on k.vin = m.vin ";

        tableEnv.executeSql(createKafkaSourceTable);
        tableEnv.executeSql(createMyslDimTable);
        tableEnv.executeSql(createResultMysqlTable);
        tableEnv.executeSql(insertMysql);

        TableResult execute = tableEnv.sqlQuery("select  * from KafkaTable").execute();
        execute.print();


    }
}
