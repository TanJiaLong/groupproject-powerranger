package my.uum.springboot.githubAPI;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import my.uum.springboot.entity.Issue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class GitHubIssueProcessor {
    private ObjectMapper objectMapper;

    @Autowired
    public GitHubIssueProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * This method extracts json data came from GitHub API
     * @param jsonData JsonData from GitHub
     * @return List of Issue instance
     */
    public List<Issue> extractIssueDataList(String jsonData) {
        try {
            // Convert JSON data to a list of Issue objects
            return objectMapper.readValue(jsonData, new TypeReference<List<Issue>>() {});
        } catch (IOException e) {
            // Handle exception (e.g., log it)
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method converts a list of Issue instances to Json data
     * @param issueList List of issue instances
     * @return JsonData for issue instances
     */
    public String serializeIssueList(List<Issue> issueList) {
        // Serialize the List<Issue> to JSON
        try {
            return objectMapper.writeValueAsString(issueList);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
