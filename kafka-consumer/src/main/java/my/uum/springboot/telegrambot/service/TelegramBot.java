package my.uum.springboot.telegrambot.service;

import my.uum.springboot.telegrambot.config.BotConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    final BotConfig config;
    public TelegramBot(BotConfig config) {
        this.config = config;
    }

    @Value("${bot.token}")
    private String token;

    @Value("${bot.username}")
    private String username;


    @Override
    public void onUpdateReceived(Update update) {
        System.out.println(update.getMessage().getText());

    }

    @Override
    public String getBotUsername() {
        return config.getUserName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    private void sendMessage(long chatID, String message){
        SendMessage botMessage = new SendMessage();
        botMessage.setChatId(chatID);
        botMessage.setText(message);

        try {
            execute(botMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}

