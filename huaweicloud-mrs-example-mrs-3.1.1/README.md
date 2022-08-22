## 简介

FlinkSteamReadKafkaToPostgres

```java
FlinkSteamReadKafkaToPostgres 使用自定义数据源，生成String json字符串，
使用 fastjson进行Json解析Json串中的属性，并保存在java ben对象中，最后使用jdbc方式把结构写入到postgres数据库表中。
```

![image](https://raw.githubusercontent.com/josonw/ideaFlinkWorkSpace/master/huaweicloud-mrs-example-mrs-3.1.1/IMG/flinksmtopg.png)