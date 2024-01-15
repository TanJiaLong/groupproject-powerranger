package my.uum.springboot;

import my.uum.springboot.githubAPI.GitHubAPIConnector;
import my.uum.springboot.kafkaProducer.service.KafkaMessagePublisher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringBootProducerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootProducerApplication.class);
    }
    @Bean
    public CommandLineRunner run(GitHubAPIConnector gitHubClient, KafkaMessagePublisher kafkaProducer) {
        return new AppRunner(gitHubClient, kafkaProducer);
    }
}
