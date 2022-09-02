package com.zcrj.flinkpc.flinksql.jdbccdc;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.*;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class flinkMySqlcdcToMysql {


    /**
     * @tag:
     * 需要了解需要哪些的依赖，官网
     * 建表的字段顺序需要和mysql表一致
     * 读取的并行度要设置成 1
     */
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

//        EnvironmentSettings settings = EnvironmentSettings
//                .newInstance()
//                .inStreamingMode()
//                .useBlinkPlanner()
//                .build();
//        TableEnvironment tableEnv = TableEnvironment.create(settings);
        env.setParallelism(1);

        /**
         * 使用flink mysql-cdc  方式实时解析mysql binglo,实现实时同步
         */
        String mysqlBinTble = "CREATE TABLE mysql_binlog (\n" +
                " id INT NOT NULL,\n" +
                " text STRING,\n" +
                " title STRING,\n" +
                " type STRING,\n" +
                " PRIMARY KEY (id) NOT ENFORCED\n" +
                ") WITH (\n" +
                " 'connector' = 'mysql-cdc',\n" +
                " 'hostname' = 'localhost',\n" +
                " 'port' = '3306',\n" +
                " 'username' = 'root',\n" +
                " 'password' = '123123',\n" +
                " 'database-name' = 'test',\n" +
                " 'table-name' = 'person'\n" +
//                " 'debezium.snapshot.mode' = 'initial'\n" +
                ")";

        String mysqlCdcSinkTable = "CREATE TABLE mysqlSinktable (\n" +
                " id INT NOT NULL,\n" +
                " text STRING,\n" +
                " title STRING,\n" +
                " type STRING,\n" +
                " PRIMARY KEY (id) NOT ENFORCED\n" +
                ") WITH (\n" +
                "   'connector' = 'jdbc',\n" +
                "   'url' = 'jdbc:mysql://localhost:3306/test?serverTimezone=UTC&useSSL=false',\n" +
                "   'username' = 'root',\n" +
                "   'password' = '123123',\n" +
                "   'table-name' = 'person2',\n" +
                "   'driver' = 'com.mysql.cj.jdbc.Driver'\n" +
                ")";


        String insert = "INSERT INTO mysqlSinktable SELECT *  FROM mysql_binlog";
        //创建 mysqlcdc执行表
        tableEnv.executeSql(mysqlBinTble);
        //创建mysqlSink 表
        tableEnv.executeSql(mysqlCdcSinkTable);
        //同步数据
        tableEnv.executeSql(insert);

//测试打印
        TableResult execute = tableEnv.sqlQuery("select  *  from mysql_binlog").execute();
        execute.print();

    }
}
