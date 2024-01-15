package my.uum.springboot;

import my.uum.springboot.entity.Issue;
import my.uum.springboot.githubAPI.GitHubAPIConnector;
import my.uum.springboot.kafkaProducer.service.KafkaMessagePublisher;
import org.springframework.boot.CommandLineRunner;

import java.util.List;

public class AppRunner implements CommandLineRunner {
    private final GitHubAPIConnector githubAPIConnector;
    private final KafkaMessagePublisher kafkaProducer;

    public AppRunner(GitHubAPIConnector githubAPIConnector, KafkaMessagePublisher kafkaProducer) {
        this.githubAPIConnector = githubAPIConnector;
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public void run(String... args) {
        // Fetch data from GitHub and filter the needed issue data
        List<Issue> githubData = githubAPIConnector.getIssues();

        // Process the data (you can add your logic here)
        String jsonIssues = githubAPIConnector.toJson(githubData);

        // Send data to Kafka
        kafkaProducer.sendMessageToTopic(jsonIssues);
    }
}
