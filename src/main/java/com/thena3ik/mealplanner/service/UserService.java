package com.thena3ik.mealplanner.service;

import com.thena3ik.mealplanner.models.user.UserSession;
import com.thena3ik.mealplanner.repository.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserDao userDao;

    @Autowired
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public UserSession findOrCreateById(long chatId) {
        return userDao.findById(chatId).orElseGet(() -> {
            UserSession newSession = new UserSession(chatId);
            userDao.save(newSession);
            return newSession;
        });
    }

    public void save(UserSession session) {
        userDao.save(session);
    }
}