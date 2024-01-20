package my.uum.springboot.kafkaConsumer.service;

//import my.uum.springboot.kafkaConsumer.entity.GitHubIssueData;
//import my.uum.springboot.kafkaConsumer.repository.GitHubIssueRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import my.uum.springboot.kafkaConsumer.entity.Issue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KafkaMessageListener {
    private static final Logger log = LoggerFactory.getLogger(KafkaMessageListener.class);
    private final IssueService issueService;
    private final ObjectMapper objectMapper;  // Autowire an instance of ObjectMapper

    @Autowired
    public KafkaMessageListener(IssueService issueService, ObjectMapper objectMapper) {
        this.issueService = issueService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = {"${kafka.topic1}", "${kafka.topic2}"}, groupId = "${kafka.groupID}")
    public void consume(String message){
        try {
            // Deserialize JSON string to your object (Issue in this case)
            List<Issue> issues = objectMapper.readValue(message, new TypeReference<List<Issue>>() {});

            // Retrieve the existing list of issues
            List<Issue> existingIssues = issueService.getIssues();

            // Merge the new issues into the existing list
            existingIssues.addAll(issues);

            // Set the merged list back to the service
            issueService.setIssues(existingIssues);

            for (Issue issue : issues) {
                log.info("Deserialized Issue => {} @ {} @ {}", issue.getId(), issue.getUsername(), issue.getComment());
                // Code Save Data To Database...
            }

        } catch (Exception e) {
            log.error("Error deserializing JSON: {}", e.getMessage());
        }
    }
}
