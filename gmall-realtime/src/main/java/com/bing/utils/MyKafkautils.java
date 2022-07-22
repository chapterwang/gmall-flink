package com.bing.utils;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Properties;

public class MyKafkautils {
    public static FlinkKafkaConsumer getConsumer(String topic,String groupid){
        Properties config = new Properties();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"172.16.20.130:9092,172.16.20.131:9092,172.16.20.132:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG,groupid);
        return new FlinkKafkaConsumer(topic, new KafkaDeserializationSchema() {
            @Override
            public boolean isEndOfStream(Object nextElement) {
                return false;
            }

            @Override
            public Object deserialize(ConsumerRecord record) throws Exception {
                if(record.value() == null || record == null){
                    return null;
                }
                else {
                    return record.value();
                }
            }

            @Override
            public TypeInformation<String> getProducedType() {
                return BasicTypeInfo.STRING_TYPE_INFO;
            }
        }, config);
    }
}
