package com.dev2day.bingo.controller;

import com.dev2day.bingo.generator.BingoService;
import com.dev2day.bingo.model.Bingo;
import com.dev2day.bingo.model.BingoForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class BingoController {

    @Autowired
    private BingoService bingoService;

    @Value("${maxsentencelength:30}")
    private int maxSentenceLength;

    @Value("${maxwordlength:10}")
    private int maxWordLength;

    @GetMapping("/bingo")
    public String getBingo(final Model model) {
        model.addAttribute("bingoForm", new BingoForm());
        return "bingo/index";
    }

    @PostMapping("/bingo")
    public Object getBingo(@ModelAttribute("bingoForm") @Valid BingoForm bingoForm,
                           BindingResult bindingResult) {
        String template = readSVG(bingoForm.getTemplateName());
        Bingo b = new Bingo(template, bingoForm.getParticipants());
        if (bingoForm.isUseFile()) {
            if (bingoForm.getFile() != null && !bingoForm.getFile().isEmpty()) {
                final Map<String, Integer> stringIntegerMap = bingoService.readWords(bingoForm.getFile().getResource(), bindingResult, "file");
                for (Map.Entry<String, Integer> e : stringIntegerMap.entrySet()) {
                    if (e.getValue() == 1) {
                        b.getHardWords().add(clean(e.getKey()));
                    } else {
                        b.getNormalWords().add(clean(e.getKey()));
                    }
                    bingoForm.setNormalQuestions(String.join("\n", b.getNormalWords()));
                    bingoForm.setHardQuestions(String.join("\n", b.getHardWords()));
                }
            } else {
                bindingResult.rejectValue("file", "missingfile");
            }
        } else {
            b.getNormalWords().addAll(parseWords(bingoForm.getNormalQuestions()));
            b.getHardWords().addAll(parseWords(bingoForm.getHardQuestions()));
        }
        validateInput(b, bindingResult, bingoForm.isUseFile());
        if (bindingResult.hasErrors()) {
            return "bingo/index";
        }
        final Resource resource = bingoService.generateAndDownload(b);
        if (resource != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/zip")
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.builder("inline").filename("bingo.zip").build().toString())
                    .body(resource);
        }
        return "bingo/index";
    }

    private void validateInput(Bingo b, Errors errors, boolean isUseFile) {
        if (b.getNormalWords().size() + b.getHardWords().size() < 25) {
            errors.reject("toofewwords");
        }
        checkSentences(errors, b.getNormalWords(), isUseFile ? "file" : "normalQuestions");
        checkSentences(errors, b.getHardWords(), isUseFile ? "file" : "hardQuestions");
    }

    private void checkSentences(Errors errors, List<String> words, String field) {
        for (String s : words) {
            if (s.length() > maxSentenceLength) {
                errors.rejectValue(field, "sentencetolong", new Object[]{s}, "Sentence too long");
            }
            Arrays.stream(
                    s.split(" "))
                    .filter(w -> w.length() > maxWordLength)
                    .forEach(w -> errors.rejectValue(field, "wordtoolong", new Object[]{w}, "Word too long"));
        }
    }

    private List<String> parseWords(String words) {
        final String[] split = words.split("\n");
        return new ArrayList<>(Arrays.asList(split)).stream().map(this::clean).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }

    private String clean(final String sentence) {
        String cleaned = "";
        if (sentence == null) {
            return "";
        }
        cleaned = sentence.trim();
        return cleaned;
    }

    public String readSVG(String name) {

        InputStream is = null;
        if ("wedding".equals(name)) {
            is = BingoController.class.getClassLoader().getResourceAsStream("static/wedding_template.svg");
        } else if ("normal".equals(name)) {
            is = BingoController.class.getClassLoader().getResourceAsStream("static/normal_template.svg");
        } else if ("wedding2".equals(name)) {
            is = BingoController.class.getClassLoader().getResourceAsStream("static/wedding_adina.svg");
        }
        if (is == null) {
            return "";
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {

        }
        return "";
    }
}
