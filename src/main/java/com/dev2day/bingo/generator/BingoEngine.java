package com.dev2day.bingo.generator;

import com.dev2day.bingo.model.Bingo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BingoEngine {
    private final Logger logger = LoggerFactory.getLogger(BingoEngine.class);

    private Map<String, Integer> countMap = new HashMap<>();

    public byte[] generate(Bingo bingo) {

        bingo.getNormalWords().forEach((key) -> countMap.put(key, 0));
        bingo.getHardWords().forEach((key) -> countMap.put(key, 0));
        long start = System.currentTimeMillis();
        logger.info("Generiere SVGs");
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (int i = 1; i <= bingo.getParticipants(); i++) {
                String out = createSVG(bingo.getTemplate(), bingo.getNormalWords(), bingo.getHardWords());
                byte[] svg = out.getBytes();
                zos.putNextEntry(new ZipEntry("bingo_" + i + ".svg"));
                zos.write(svg);
                zos.closeEntry();
                zos.flush();
            }
            logger.info("Fertig ({} ms)", (System.currentTimeMillis() - start));
            logger.info(countMap.toString());
            zos.flush();
            zos.close();
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String createSVG(String template, List<String> normalWordsList, List<String> hardWordsList) {
        List<String> normalWords = new ArrayList<>(normalWordsList);
        List<String> hardWords = new ArrayList<>(hardWordsList);
        Random rnd = new Random();
        //fill hard words with normal ones in case there are not enough
        if (hardWords.size() < 5) {
            final int initialSize = hardWords.size();
            for (int i = 0; i < 5 - initialSize; i++) {
                int j = rnd.nextInt(normalWords.size());
                hardWords.add(normalWords.get(j));
                normalWords.remove(j);
            }
        }
        for (int i = 1; i <= 5; i++) {
            int j = rnd.nextInt(hardWords.size());
            String s = hardWords.get(j);
            countMap.put(s, countMap.get(s) + 1);
            template = template.replace("quak" + (i < 10 ? "0" : "") + i, s);
            hardWords.remove(j);
        }
        normalWords.addAll(hardWords);
        for (int i = 6; i <= 25; i++) {
            int j = rnd.nextInt(normalWords.size());
            String s = normalWords.get(j);
            countMap.put(s, countMap.get(s) + 1);
            template = template.replace("quak" + (i < 10 ? "0" : "") + i, s);
            normalWords.remove(j);
        }
        return template;
    }
}
