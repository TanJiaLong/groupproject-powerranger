package my.uum.springboot.githubAPI;

import my.uum.springboot.entity.Issue;
import my.uum.springboot.kafkaProducer.service.KafkaMessagePublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FetchDataController {
    private final GitHubAPIConnector gitHubAPIConnector;
    private final KafkaMessagePublisher kafkaProducer;

    public FetchDataController(GitHubAPIConnector gitHubAPIConnector, KafkaMessagePublisher kafkaProducer) {
        this.gitHubAPIConnector = gitHubAPIConnector;
        this.kafkaProducer = kafkaProducer;
    }

    @GetMapping("/fetch")
    public ResponseEntity<String> handleFetchRequest() {
        // Handle the fetch request and fetch data from GitHub API
        List<Issue> fetchedData = gitHubAPIConnector.getIssues();
        // Process the data (you can add your logic here)
        String jsonIssues = gitHubAPIConnector.toJson(fetchedData);
        // Send data to Kafka
        kafkaProducer.sendMessageToTopic(jsonIssues);

        // Return a response to the consumer or the client
        return ResponseEntity.ok("Data fetched successfully");
    }
}
