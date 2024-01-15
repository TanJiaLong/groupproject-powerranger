package my.uum.springboot.kafkaConsumer.service;

//import my.uum.springboot.kafkaConsumer.entity.GitHubIssueData;
//import my.uum.springboot.kafkaConsumer.repository.GitHubIssueRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import my.uum.springboot.kafkaConsumer.entity.Issue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KafkaMessageListener {
    private static final Logger log = LoggerFactory.getLogger(KafkaMessageListener.class);
    private final ObjectMapper objectMapper;  // Autowire an instance of ObjectMapper

    public KafkaMessageListener(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${kafka.topic}", groupId = "${kafka.groupID}")
    public void consume(String message){
        try {
//            // Remove the double quotes from the string message
//            message = message.substring(1, message.length() - 1);

            // Deserialize JSON string to your object (Issue in this case)
            List<Issue> issues = objectMapper.readValue(message, new TypeReference<List<Issue>>() {});

            for (Issue issue : issues) {
                log.info("Deserialized Issue => {}@{}@{}", issue.getId(), issue.getUsername(), issue.getComment());

                // Code Save Data To Database...
            }

        } catch (Exception e) {
            log.error("Error deserializing JSON: {}", e.getMessage());
        }
    }
}
