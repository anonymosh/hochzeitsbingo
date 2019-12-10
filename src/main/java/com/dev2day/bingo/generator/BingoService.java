package com.dev2day.bingo.generator;

import com.dev2day.bingo.model.Bingo;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
public class BingoService {

    public Resource generateAndDownload(Bingo bingo) {
        BingoEngine bingoEngine = new BingoEngine();
        final byte[] generated = bingoEngine.generate(bingo);
        if (generated == null) {
            return null;
        }
        return new ByteArrayResource(generated);
    }

    public Map<String, Integer> readWords(Resource res, BindingResult bindingResult, String field) {
        Map<String, Integer> map = new HashMap<String, Integer>();
        try {
            if (res.contentLength() > 1024 * 1024 * 24) {
                bindingResult.rejectValue(field, "filetoobig");
                return map;
            }
        } catch (IOException e) {
            bindingResult.rejectValue(field, "filenotreadable");
            return map;
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(res.getInputStream()))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] str = line.split(";");
                if (str.length != 2) {
                    bindingResult.rejectValue(field, "lineformat", new Object[]{line}, "line not well formatted");
                    continue;
                }
                try {
                    final int value = Integer.parseInt(str[1]);
                    if (value != 0 && value != 1) {
                        bindingResult.rejectValue(field, "linenumberformat", new Object[]{line}, "line not well formatted");
                    } else {
                        map.put(str[0], value);
                    }
                } catch (NumberFormatException nfe) {
                    bindingResult.rejectValue(field, "linenumberformat", new Object[]{line}, "line not will formatted");
                }
            }
        } catch (
                IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
        return map;
    }
}
