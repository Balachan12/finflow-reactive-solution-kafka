package com.example.finflow.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topic.transactions}")
    private String transactionTopic;

    @Value("${kafka.producer.client-id}")
    private String clientId;

    @Value("${kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public NewTopic transactionTopic() {
        return new NewTopic(transactionTopic, 1, (short) 1);
    }

    @Bean
    public KafkaSender<String, String> kafkaSender() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return KafkaSender.create(SenderOptions.create(props));
    }

    @Bean
    public KafkaReceiver<String, String> kafkaReceiver() {
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", groupId);
        props.put("key.deserializer", org.apache.kafka.common.serialization.StringDeserializer.class);
        props.put("value.deserializer", org.apache.kafka.common.serialization.StringDeserializer.class);
        props.put("auto.offset.reset", "earliest");

        ReceiverOptions<String, String> receiverOptions =
                ReceiverOptions.<String, String>create(props)
                        .subscription(java.util.List.of(transactionTopic));

        return KafkaReceiver.create(receiverOptions);
    }
}
