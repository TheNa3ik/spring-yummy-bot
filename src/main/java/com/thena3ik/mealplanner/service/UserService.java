package com.thena3ik.mealplanner.service;

import com.thena3ik.mealplanner.model.entity.SearchStateEntity;
import com.thena3ik.mealplanner.model.entity.UserEntity;
import com.thena3ik.mealplanner.model.enums.UserState;
import com.thena3ik.mealplanner.repository.UserRepository;
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

            SearchStateEntity initialState = new SearchStateEntity();
            initialState.setUserState(UserState.MAIN_MENU);

            newUser.setSearchState(initialState);

            return userRepository.save(newUser);
        });
    }

    public void save(UserEntity user) {
        userRepository.save(user);
    }
}