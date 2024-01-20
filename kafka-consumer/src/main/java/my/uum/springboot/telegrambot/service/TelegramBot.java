package my.uum.springboot.telegrambot.service;

import my.uum.springboot.kafkaConsumer.entity.Issue;
import my.uum.springboot.kafkaConsumer.service.IssueService;
import my.uum.springboot.telegrambot.config.BotConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    final BotConfig config;

    //No connect to database yet
    private final IssueService issueService;
    private final RestTemplate restTemplate;

    @Autowired
    public TelegramBot(BotConfig config, IssueService issueService, RestTemplate restTemplate) {
        this.config = config;
        this.issueService = issueService;

        this.restTemplate = restTemplate;
    }

    @Value("${bot.token}")
    private String token;
    @Value("${bot.username}")
    private String username;
    @Value("${producer.api.url}")
    private String fetchUrl;



    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            Message userMessage = update.getMessage();
            long chatID = userMessage.getChatId();


            switch (userMessage.getText()) {
                case "/start":
                    String welcomeMessage = "Hi ...";
                    sendMessage(chatID, welcomeMessage);
                    break;
                case "/fetch":
                    // Send an HTTP GET request to the producer
                    String producerFetchUrl = fetchUrl + "/fetch";

                    // Use exchange to capture the response
                    ResponseEntity<String> responseEntity = restTemplate.exchange(
                            producerFetchUrl,
                            HttpMethod.GET,
                            null,
                            String.class);

                    // Process the response
                    HttpStatus statusCode = (HttpStatus) responseEntity.getStatusCode();
                    if (statusCode == HttpStatus.OK) {
                        String responseBody = responseEntity.getBody();
                        // Process the response body as needed
                        sendMessage(chatID,"Fetch success");
//                        sendMessage(chatID, "Received response from producer: " + responseBody);
                    } else {
                        // Handle other status codes if needed
                        sendMessage(chatID, "Failed to fetch");
                    }
                    break;
                case "/display":
                    List<Issue> issues = issueService.getIssues();
                    displayIssues(chatID, issues);
                    //display all issue instance data from consumer by telegrambot
                    break;
                case "/process":
                    List<Issue> processIssues = issueService.getIssues();

                    //Process Message
                    StringBuilder processMessage = new StringBuilder();
                    String commenterCountMessage = displayActiveCommenter(processIssues);
                    String wordCountMessage = displayWordCount(processIssues);
                    processMessage.append(commenterCountMessage).append(wordCountMessage);

                    sendMessage(chatID, processMessage.toString());
                    break;
                default:
                    String errMessage = "Please enter within the following command:\n" +
                            "/start\n" +
                            "/fetch\n" +
                            "/display\n" +
                            "/process";
                    sendMessage(chatID, errMessage);
                    break;
            }
        }
    }

    private String displayActiveCommenter(List<Issue> processIssues) {
        Map<String, Long> usernameOccurrences = countUsernames(processIssues);
        // Print the results
        List<Map.Entry<String, Long>> sortedEntries = usernameOccurrences.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toList());

        StringBuilder activeCommenterMsg = new StringBuilder("List of active commenters\n");
        for (int i = 0; i < sortedEntries.size(); i++) {
            Map.Entry<String, Long> entry = sortedEntries.get(i);
            activeCommenterMsg.append(i + 1).append(". ")
                    .append(entry.getKey()).append(" [").append(entry.getValue()).append(" comments]\n");
        }
        activeCommenterMsg.append("\n");
        return activeCommenterMsg.toString();
    }

    private Map<String, Long> countUsernames(List<Issue> issues) {
        return issues.stream()
                .map(Issue::getUsername)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    private String displayWordCount(List<Issue> processIssues) {
        StringBuilder issuesString = new StringBuilder();

        //Add all comment in 1 var for counting word
        for (Issue issue : processIssues) {
            issuesString.append(issue.getComment()).append(" ");
        }

        //List of Word Count
        Map<String, Integer> wordOccurrence = countWord(issuesString.toString());
        List<Map.Entry<String, Integer>> sortedEntries = wordOccurrence.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        //Word Count Message
        StringBuilder wordCount = new StringBuilder("List of word count\n");
        for (int i = 0; i < sortedEntries.size(); i++) {
            Map.Entry<String, Integer> entry = sortedEntries.get(i);
            wordCount.append(i + 1).append(". ")
                    .append(entry.getKey()).append(" [").append(entry.getValue()).append(" times]\n");
        }
        return wordCount.toString();
    }

    private static Map<String, Integer> countWord(String input) {
        Map<String, Integer> occurrences = new HashMap<>();

        // Remove special characters and split the input into words
        String[] words = input.replaceAll("[^a-zA-Z\\s]", "").split("\\s+");

        for (String word : words) {
            occurrences.put(word, occurrences.getOrDefault(word, 0) + 1);
        }
        return occurrences;
    }
    @Override
    public String getBotUsername() {
        return config.getUserName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    private void sendMessage(long chatID, String message) {
        SendMessage botMessage = new SendMessage();
        botMessage.setChatId(chatID);
        botMessage.setText(message);

        try {
            execute(botMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void displayIssues(long chatID, List<Issue> issues) {
        StringBuilder issuesMessage = new StringBuilder("List of Issues:\n");
        for (int i = 0; i < issues.size(); i++) {
            Issue issue = issues.get(i);
            issuesMessage.append(String.format("%d. User: %s, Comment: %s\n", i, issue.getUsername(), issue.getComment()));
        }
        sendMessage(chatID, issuesMessage.toString());
    }
}

