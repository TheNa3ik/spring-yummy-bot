package com.thena3ik.yummybot.service;

import com.thena3ik.yummybot.model.entity.SearchStateEntity;
import com.thena3ik.yummybot.model.entity.UserEntity;
import com.thena3ik.yummybot.model.enums.UserState;
import com.thena3ik.yummybot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity findOrCreateById(long chatId) {
        return userRepository.findById(chatId).orElseGet(() -> {
            UserEntity newUser = new UserEntity();
            newUser.setChatId(chatId);
            newUser.setAiTranslationEnabled(true);

            newUser.setUserState(UserState.MAIN_MENU);
            SearchStateEntity initialSearchFilters = new SearchStateEntity();

            newUser.setSearchState(initialSearchFilters);

            return userRepository.save(newUser);
        });
    }

    public void save(UserEntity user) {
        userRepository.save(user);
    }
}