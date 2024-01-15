package my.uum.springboot.kafkaProducer.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaMessagePublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaMessagePublisher.class);
    @Autowired
    private KafkaTemplate<String, Object> template;

    @Value("${kafka.topic}")
    private String topic;

    public KafkaMessagePublisher(KafkaTemplate<String, Object> template) {
        this.template = template;
    }

    public void sendMessageToTopic(String message) {
        CompletableFuture<SendResult<String, Object>> future = template.send(topic, message);
        future.whenComplete((result, exception) -> {
                    if (exception == null) {
                        LOGGER.info("Sent Message: [" + message + "] with offset: [" + result.getRecordMetadata().offset() + "]");
                    } else {
                        LOGGER.info("Unable to send message: [" + message + "] due to: " + exception.getMessage());
                    }
                }
        );
    }
}
