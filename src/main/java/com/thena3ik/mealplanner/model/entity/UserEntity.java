package com.thena3ik.mealplanner.model.entity;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @Column(name = "chat_id")
    private long chatId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "language_code")
    private String languageCode;

    @Column(name = "is_ai_translation_enabled")
    private boolean aiTranslationEnabled;

    @Column(name = "diet")
    private String diet;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private SearchStateEntity searchState;

    public void setSearchState(SearchStateEntity searchState) {
        this.searchState = searchState;
        if (searchState != null) {
            searchState.setUser(this);
        }
    }
}