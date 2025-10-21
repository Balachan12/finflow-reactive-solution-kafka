package com.example.finflow.events;

import com.example.finflow.model.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

@Component
public class TransactionEventConsumer {

    private final KafkaReceiver<String, String> receiver;
    //private final ObjectMapper mapper = new ObjectMapper();
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public TransactionEventConsumer(KafkaReceiver<String, String> receiver) {
        this.receiver = receiver;
    }

    @PostConstruct
    public void subscribe() {
        receiver.receive()
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(record -> {
                    try {
                        Transaction t = mapper.readValue(record.value(), Transaction.class);
                        System.out.println("✅ Received Kafka Transaction Event: " + t.getTransactionId());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .subscribe();
    }
}
