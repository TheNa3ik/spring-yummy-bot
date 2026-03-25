package com.thena3ik.yummybot.model.entity;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "search_states")
public class SearchStateEntity {

    @Id
    @Column(name = "chat_id")
    private long chatId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "chat_id")
    private UserEntity user;

    @Column(name = "search_name")
    private String searchName;

    @Column(name = "search_ingredients")
    private String ingredients;

    @Column(name = "search_offset")
    private int offset;

    @Column(name = "viewed_recipe_id")
    private int recipeId;

    @Column(name = "cuisine")
    private String cuisine;

    public SearchStateEntity() {
        this.offset = 0;
    }

    public void incrementOffset() {
        this.offset++;
    }

    public void decrementOffset() {
        if (this.offset > 0) this.offset--;
    }

    public void resetOffset() {
        this.offset = 0;
    }
}