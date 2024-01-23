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

    private final IssueService issueService;
    private final RestTemplate restTemplate;

    /**
     * The constructor of Telegram Bot Service
     * @param config The configuration of telegram bot
     * @param issueService The list of issue object
     * @param restTemplate To perform HTTP method
     */
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
    @Value("${producer1.api.url}")
    private String fetchUrl1;
    @Value("${producer2.api.url}")
    private String fetchUrl2;

    /**
     * The method is to handle user interaction with telegrambot
     * @param update The updated input from user, either by text, click or other inputs
     */
    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            Message userMessage = update.getMessage();
            long chatID = userMessage.getChatId();


            switch (userMessage.getText()) {
                case "/start":
                    String welcomeMessage = "Hi there! \uD83D\uDC4B Welcome to the Real-Time Issues Tracking System Bot!\n" +
                            "\n" +
                            "This bot provides real-time insights into GitHub Issues. Here are the available commands:\n" +
                            "\n" +
                            "- /start: Get started with the bot.\n" +
                            "- /fetch: Fetch real-time GitHub Issues data.\n" +
                            "- /display: Display the latest GitHub Issues.\n" +
                            "- /process: Perform basic data processing.\n" +
                            "\n" +
                            "Happy exploring! \uD83D\uDE80";
                    sendMessage(chatID, welcomeMessage);
                    break;
                case "/fetch":
                    issueService.getIssues().clear();
                    // Send HTTP GET request to the first producer
                    String producer1FetchUrl = fetchUrl1 + "/fetch";
                    processFetchRequest(chatID, producer1FetchUrl);

                    // Send HTTP GET request to the second producer
                    String producer2FetchUrl = fetchUrl2 + "/fetch";
                    processFetchRequest(chatID, producer2FetchUrl);
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
                case "/clear":
                    issueService.getIssues().clear();
                    sendMessage(chatID, "Issues cleared");
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

    /**
     * To Request data from Producer Applications
     * @param chatID The ID of user who interacts with telegram bot
     * @param fetchUrl The url to trigger producer to fetch data from github and send to kafka topic
     */
    private void processFetchRequest(long chatID, String fetchUrl) {
        try {
            // Use exchange to capture the response
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    fetchUrl,
                    HttpMethod.GET,
                    null,
                    String.class);

            // Process the response
            HttpStatus statusCode = (HttpStatus) responseEntity.getStatusCode();
            if (statusCode == HttpStatus.OK) {
                String responseBody = responseEntity.getBody();
                sendMessage(chatID, "Fetch success from " + fetchUrl);
                // Process the response body as needed
                 sendMessage(chatID, "Received response from producer: " + responseBody);
            } else {
                // Handle other status codes if needed
                sendMessage(chatID, "Failed to fetch from " + fetchUrl);
            }
        } catch (Exception e) {
            sendMessage(chatID, "Error processing fetch request: " + e.getMessage());
        }
    }

    /**
     * The method to display top 10 active commenters
     * @param processIssues List of issues consumed
     * @return String of active commenters list
     */
    private String displayActiveCommenter(List<Issue> processIssues) {
        Map<String, Long> usernameOccurrences = countUsernames(processIssues);
        // Print the results
        List<Map.Entry<String, Long>> sortedEntries = usernameOccurrences.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
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

    /**
     * The method is to count the occurrence of every username
     * @param issues List of issues
     * @return Map of username and its count
     */
    private Map<String, Long> countUsernames(List<Issue> issues) {
        return issues.stream()
                .map(Issue::getUsername)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    /**
     * The method to display top 10 word count
     * @param processIssues List of issues
     * @return String of word count list
     */
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
                .limit(10)
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

    /**
     * The method is to count the occurrence of every word
     * @param input All text in comment body
     * @return Map of word and its count
     */
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

    /**
     * The method is to handle the message to be sent by telegram bot
     * @param chatID ID of user who interacts with telegram
     * @param message Message needs to be sent by telegram bot
     */
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

    /**
     * The method is to display all issues
     * @param chatID ID of user who interacts with telegram
     * @param issues List of issues
     */
    private void displayIssues(long chatID, List<Issue> issues) {
        StringBuilder issuesMessage = new StringBuilder("List of Issues:\n");
        for (int i = 0; i < issues.size(); i++) {
            Issue issue = issues.get(i);
            issuesMessage.append(String.format("%d. User: %s, Comment: %s\n", i, issue.getUsername(), issue.getComment()));
        }
        sendMessage(chatID, issuesMessage.toString());
    }
}

