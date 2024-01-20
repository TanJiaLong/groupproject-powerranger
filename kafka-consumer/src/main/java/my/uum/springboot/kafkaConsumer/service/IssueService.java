package my.uum.springboot.kafkaConsumer.service;

import my.uum.springboot.kafkaConsumer.entity.Issue;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IssueService {
    private List<Issue> issues = new ArrayList<Issue>();

    public List<Issue> getIssues() {
        return issues;
    }

    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }
}
