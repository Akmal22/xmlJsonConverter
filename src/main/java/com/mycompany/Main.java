package com.mycompany;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.mycompany.Converter.convertFromXMlToJson;

public class Main {
    private static final String TEST_FILE_PATH = "/home/akmal/IdeaProjects/xmlJsonConverter/src/main/resources/test.txt";


    public static void main(String[] args) throws IOException {
        String input = new String(Files.readAllBytes(Paths.get(TEST_FILE_PATH))).replaceAll("\\n+", "");
        StringBuffer stringBuffer = new StringBuffer();
        convertFromXMlToJson(input, stringBuffer);
        System.out.println(stringBuffer.toString());
    }
}

