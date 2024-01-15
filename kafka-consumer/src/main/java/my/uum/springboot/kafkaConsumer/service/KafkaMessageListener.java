package my.uum.springboot.kafkaConsumer.service;

//import my.uum.springboot.kafkaConsumer.entity.GitHubIssueData;
//import my.uum.springboot.kafkaConsumer.repository.GitHubIssueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaMessageListener {
    private static final Logger log = LoggerFactory.getLogger(KafkaMessageListener.class);
//    private GitHubIssueRepository dataRepository;
//
//    public KafkaMessageListener(GitHubIssueRepository dataRepository) {
//        this.dataRepository = dataRepository;
//    }


    @KafkaListener(topics = "${kafka.topic}", groupId = "${kafka.groupID}")
    public void consume(String message){
        log.info("Message Consumed => {}",message);

//        GitHubIssueData gitHubIssueData = new GitHubIssueData();
//        dataRepository.save(gitHubIssueData);
    }
}
