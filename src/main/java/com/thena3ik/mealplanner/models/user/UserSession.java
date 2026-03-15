package com.thena3ik.mealplanner.models.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(name = "user_sessions")
public class UserSession {

    @Id
    private Long chatId;

    @Setter
    private String firstName;

    @Setter
    @Column(name = "language_code")
    private String languageCode;

    @Setter
    @Enumerated(EnumType.STRING)
    private UserState userState;

    @Setter
    @Embedded
    private LastSearch lastSearch;

    public UserSession() {
        this.userState = UserState.IDLE;
    }

    public UserSession(long chatId) {
        this.chatId = chatId;
        this.userState = UserState.IDLE;
    }

    public boolean hasDiet() {
        return lastSearch != null &&
                lastSearch.getDiet() != null &&
                !lastSearch.getDiet().isBlank();
    }
}
