package com.dev2day.bingo.model;

import java.util.ArrayList;
import java.util.List;

public class Bingo {
    private final List<String> normalWords = new ArrayList<>();
    private final List<String> hardWords = new ArrayList<>();
    private String template;
    private final int participants;

    public Bingo(String template, int participants) {
        this.template = template;
        this.participants = participants;
    }

    public List<String> getNormalWords() {
        return normalWords;
    }

    public List<String> getHardWords() {
        return hardWords;
    }

    public String getTemplate() {
        return template;
    }

    public int getParticipants() {
        return participants;
    }
}
