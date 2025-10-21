package com.example.finflow.events;

import com.example.finflow.model.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Component
public class TransactionEventPublisher {

    private final KafkaSender<String, String> sender;
   // private final ObjectMapper mapper = new ObjectMapper();
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Value("${kafka.topic.transactions}")
    private String topic;

    public TransactionEventPublisher(KafkaSender<String, String> sender) {
        this.sender = sender;
    }

    public Mono<Void> publish(Transaction t) {
        return Mono.fromCallable(() -> mapper.writeValueAsString(t))
                .flatMap(json -> sender.send(Mono.just(SenderRecord.create(topic, null, null, t.getAccountId().toString(), json, null)))
                        .then())
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.empty();
                });
    }
}
