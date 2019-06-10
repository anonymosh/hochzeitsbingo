package de.dev2day.bingo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class Bingo {

    private static final int COUNT_PARTICIPANTS = 40;
    private static Map<String, Integer> countMap = new HashMap<>();

    public static void main(String[] args) throws IOException {
        String template = readSVG();
        Map<String, Integer> words = readWords();

        Path outDir = Paths.get("out");
        if (Files.exists(outDir)) {
            System.out.println("Verzeichnis existiert...lösche alte Daten");
            deleteFolder(outDir);
        }
        Files.createDirectory(outDir );
        if (template != null && words != null) {
            words.forEach((key, value) -> countMap.put(key, 0));
            System.out.println("Generiere SVGs");
            for (int i = 1; i <= COUNT_PARTICIPANTS; i++) {
                String out = createSVG(template, words);
                try (FileWriter writer = new FileWriter("out\\Hochzeitsbingo_" + i + ".svg")) {
                    writer.write(out);
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Fertig");
        System.out.println(countMap.toString());
    }

    private static String readSVG() {
        try {
            return new String(Files.readAllBytes(Paths.get("src\\main\\resources\\Hochzeitsbingo_template.svg")));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    private static Map<String, Integer> readWords() {
        try (BufferedReader br = new BufferedReader(new FileReader("src\\main\\resources\\Bingoliste.csv"))) {
            String line = null;
            Map<String, Integer> map = new HashMap<String, Integer>();
            int i = 0;
            while ((line = br.readLine()) != null) {
                if (i > 0) {
                    String[] str = line.split(";");
                    map.put(str[0], Integer.parseInt(str[1]));
                }
                i++;
            }
            return map;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    private static String createSVG(String template, Map<String, Integer> words) {
        Random rnd = new Random();
        List<String> hardWords = words.entrySet().stream().filter(e -> e.getValue() == 1).map(Map.Entry::getKey).collect(Collectors.toList());
        List<String> otherWords = words.entrySet().stream().filter(e -> e.getValue() == 0).map(Map.Entry::getKey).collect(Collectors.toList());
        for (int i = 1; i <= 5; i++) {
            int j = rnd.nextInt(hardWords.size());
            String s = hardWords.get(j);
            countMap.put(s, countMap.get(s) + 1);
            template = template.replace("quak" + (i < 10 ? "0" : "") + i, s);
            hardWords.remove(j);
        }
        otherWords.addAll(hardWords);
        for (int i = 6; i <= 25; i++) {
            int j = rnd.nextInt(otherWords.size());
            String s = otherWords.get(j);
            countMap.put(s, countMap.get(s) + 1);
            template = template.replace("quak" + (i < 10 ? "0" : "") + i, s);
            otherWords.remove(j);
        }
        return template;
    }

    private static void deleteFolder(Path outDir) throws IOException {
        Files.walkFileTree(outDir, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                Files.delete(path);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path path, IOException e) throws IOException {
                Files.delete(path);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
