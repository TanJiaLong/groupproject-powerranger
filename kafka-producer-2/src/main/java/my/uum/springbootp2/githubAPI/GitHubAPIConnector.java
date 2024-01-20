package my.uum.springbootp2.githubAPI;

import my.uum.springbootp2.entity.Issue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class GitHubAPIConnector {
    @Value("${github.user.owner}")
    private String owner;
    @Value("${github.user.repo}")
    private String repo;

    private final RestTemplate restTemplate;
    private final GitHubIssueProcessor gitHubIssueProcessor;

    @Autowired
    public GitHubAPIConnector(RestTemplate restTemplate, GitHubIssueProcessor gitHubIssueProcessor) {
        this.restTemplate = restTemplate;
        this.gitHubIssueProcessor = gitHubIssueProcessor;
    }

    public List<Issue> getIssues() {
        final String apiUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/issues/comments";
        String jsonData = restTemplate.getForObject(apiUrl, String.class);

        return gitHubIssueProcessor.extractIssueDataList(jsonData);
    }

    public String toJson(List<Issue> issues) {
        // Serialize each Issue object to JSON and print it (you can send it to Kafka here)
        String jsonIssue = gitHubIssueProcessor.serializeIssueList(issues);
        System.out.println(jsonIssue);
        return jsonIssue;
    }
}
