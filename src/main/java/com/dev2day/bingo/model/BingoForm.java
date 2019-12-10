package com.dev2day.bingo.model;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class BingoForm {

    private String normalQuestions;
    private String hardQuestions;
    private String templateName;
    private MultipartFile file;
    private boolean useFile;

    @Min(1)
    @Max(150)
    private int participants = 30;

    public String getNormalQuestions() {
        return normalQuestions;
    }

    public void setNormalQuestions(String normalQuestions) {
        this.normalQuestions = normalQuestions;
    }

    public String getHardQuestions() {
        return hardQuestions;
    }

    public void setHardQuestions(String hardQuestions) {
        this.hardQuestions = hardQuestions;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public int getParticipants() {
        return participants;
    }

    public void setParticipants(int participants) {
        this.participants = participants;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public boolean isUseFile() {
        return useFile;
    }

    public void setUseFile(boolean useFile) {
        this.useFile = useFile;
    }
}
