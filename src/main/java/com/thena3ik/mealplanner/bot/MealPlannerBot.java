package com.thena3ik.mealplanner.bot;

import com.thena3ik.mealplanner.bot.handlers.ErrorHandler;
import com.thena3ik.mealplanner.bot.handlers.UpdateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class MealPlannerBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final UpdateHandler updateHandler;
    private final ErrorHandler errorHandler;
    private final String botToken;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    @Autowired
    public MealPlannerBot(UpdateHandler updateHandler,
                          ErrorHandler errorHandler,
                          @Value("${bot.token}") String botToken) {
        this.updateHandler = updateHandler;
        this.errorHandler = errorHandler;
        this.botToken = botToken;
    }

    @Override
    public String getBotToken() {
        return this.botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        executor.submit(() -> {
            try {
                updateHandler.handle(update);
            } catch (Exception e) {
                errorHandler.handle(e, update);
            }
        });
    }
}
