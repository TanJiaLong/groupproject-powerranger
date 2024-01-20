package my.uum.springbootp2;

import my.uum.springbootp2.githubAPI.GitHubAPIConnector;
import my.uum.springbootp2.kafkaProducer.service.KafkaMessagePublisher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringBootProducer2Application {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootProducer2Application.class);
    }

    @Bean
    public CommandLineRunner run(GitHubAPIConnector gitHubClient, KafkaMessagePublisher kafkaProducer) {
        return new AppRunner(gitHubClient, kafkaProducer);
    }
}
