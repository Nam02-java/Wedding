package com.example.Selenium.Package03;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;
@RestController

public class CaptchaSolove_bot extends TelegramLongPollingBot {
    @Override
    public void onUpdateReceived(Update update) {
        String command = update.getMessage().getText();
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        if (command.equals("/myname")) {
            if (update.getMessage().getFrom() != null) {
                message.setText("dit me cong san viet nam");
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }

    @Override
    public String getBotUsername() {
        return "CaptchaSlove_bot";
    }

    @Override
    public void onRegister() {
        super.onRegister();
    }

    @Override
    public String getBotToken() {
        return "6928830332:AAGmv3fN_k8YdITzJeOyjqtsDQfWuviF308";
    }

    @RequestMapping("/test")
    public String hello() throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId("1159534870"); // Thay thế YourChatId bằng chat ID của bạn
        message.setText("test 123 good good");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        return "world";
    }
}
