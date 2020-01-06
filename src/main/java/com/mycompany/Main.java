package com.mycompany;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import static com.mycompany.Converter.printJsonHierarchy;

public class Main {
    private static final String TEST_FILE_PATH = "/home/akmal/IdeaProjects/xmlJsonConverter/src/main/resources/test2.txt";
    private static final String JSON_ELEMENT_REGEX = "\"(?<elementName>[^\"]*)\"\\s*:\\s*(?<elementValue>(\\{)|(\"[^\"]*\")|(null)|(\\d*\\.*\\d*))";

    private static final Pattern jsonElementPattern = Pattern.compile(JSON_ELEMENT_REGEX);

    public static void main(String[] args) throws IOException {
        String input = new String(Files.readAllBytes(Paths.get(TEST_FILE_PATH))).replaceAll("\\n+", "");
        printJsonHierarchy("", "", input, true);
    }
}

