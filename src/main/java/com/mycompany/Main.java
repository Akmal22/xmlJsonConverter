package com.mycompany;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.mycompany.Converter.*;

public class Main {
    private static final String TEST_FILE_PATH = "/home/akmal/IdeaProjects/xmlJsonConverter/src/main/resources/test.txt";

    public static void main(String[] args) throws IOException {
        String input = new String(Files.readAllBytes(Paths.get(TEST_FILE_PATH))).replaceAll("\\n+", "");
        StringBuffer stringBuffer = new StringBuffer();
        if (input.trim().matches(XML_INPUT_REGEX)) {
            convertFromXMlToJson(input, stringBuffer);
        } else if (input.trim().matches(JSON_INPUT_REGEX)) {
            if (jsonContainsMoreThanOneParentElement(input.trim())) {
                stringBuffer.append("<root>");
                convertJsonToXML("", input, true, stringBuffer);
                stringBuffer.append("</root>");
            } else {
                convertJsonToXML("", input, true, stringBuffer);
            }
        } else {
            throw new IllegalArgumentException("");
        }
        System.out.println(stringBuffer.toString());
    }
}

