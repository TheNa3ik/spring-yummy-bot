package com.thena3ik.mealplanner.bot;

import com.thena3ik.mealplanner.bot.handler.core.GlobalExceptionHandler;
import com.thena3ik.mealplanner.bot.handler.core.UpdateDispatcher;
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

    private final UpdateDispatcher updateDispatcher;
    private final GlobalExceptionHandler globalExceptionHandler;
    private final String botToken;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    @Autowired
    public MealPlannerBot(UpdateDispatcher updateDispatcher,
                          GlobalExceptionHandler globalExceptionHandler,
                          @Value("${bot.token}") String botToken) {
        this.updateDispatcher = updateDispatcher;
        this.globalExceptionHandler = globalExceptionHandler;
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
                updateDispatcher.handle(update);
            } catch (Exception e) {
                globalExceptionHandler.handle(e, update);
            }
        });
    }
}
