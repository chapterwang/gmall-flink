package com.bing.app.dim;

import com.alibaba.fastjson.JSONObject;
import com.bing.app.func.DimSinkFunction;
import com.bing.app.func.TableProcessFunction;
import com.bing.bean.TableProcess;
import com.bing.utils.MyKafkautils;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

public class DimApp {
    public static void main(String[] args) throws Exception {

        //TODO 1.获取执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);  //生成环境设置为Kafka主题的分区数

//        env.setStateBackend(new HashMapStateBackend());
//        env.enableCheckpointing(5000L);
//        env.getCheckpointConfig().setCheckpointTimeout(10000L);
//        env.getCheckpointConfig().setCheckpointStorage("hdfs:xxx:8020//xxx/xx");

        //TODO 2.读取 Kafka topic_db 主题数据创建流
        DataStreamSource<String> kafkaDS = env.addSource(MyKafkautils.getConsumer("topic_db", "dim_app_211027"));

        //TODO 3.过滤掉非JSON格式的数据,并将其写入侧输出流
        OutputTag<String> dirtyDataTag = new OutputTag<String>("Dirty") {
        };
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaDS.process(new ProcessFunction<String, JSONObject>() {
            @Override
            public void processElement(String s, Context context, Collector<JSONObject> collector) throws Exception {
                try {
                    JSONObject jsonObject = JSONObject.parseObject(s);
                    collector.collect(jsonObject);
                } catch (Exception e) {
                    context.output(dirtyDataTag, s);
                }
            }
        });

        //取出脏数据并打印
        DataStream<String> sideOutput = jsonObjDS.getSideOutput(dirtyDataTag);
        sideOutput.print("Dirty>>>>>>>>>>");

        //TODO 4.使用FlinkCDC读取MySQL中的配置信息
        MySqlSource<String> mySqlSource = MySqlSource.<String>builder()
                .hostname("hadoop102")
                .port(3306)
                .username("root")
                .password("000000")
                .databaseList("gmall-211027-config")
                .tableList("gmall-211027-config.table_process")
                .deserializer(new JsonDebeziumDeserializationSchema())
                .startupOptions(StartupOptions.initial())
                .build();
        DataStreamSource<String> mysqlSourceDS = env.fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "MysqlSource");

        //TODO 5.将配置信息流处理成广播流
        MapStateDescriptor<String, TableProcess> mapStateDescriptor = new MapStateDescriptor<>("map-state", String.class, TableProcess.class);
        BroadcastStream<String> broadcastStream = mysqlSourceDS.broadcast(mapStateDescriptor);

        //TODO 6.连接主流与广播流
        BroadcastConnectedStream<JSONObject, String> connectedStream = jsonObjDS.connect(broadcastStream);

        //TODO 7.根据广播流数据处理主流数据
        SingleOutputStreamOperator<JSONObject> hbaseDS = connectedStream.process(new TableProcessFunction(mapStateDescriptor));

        //TODO 8.将数据写出到Phoenix中
        hbaseDS.print(">>>>>>>>>>>>>");
        hbaseDS.addSink(new DimSinkFunction());

        //TODO 9.启动任务
        env.execute("DimApp");

    }
}
