package com.mycompany;

import java.io.IOException;
import java.io.InputStream;

import static com.mycompany.Converter.*;

public class Main {
    private static final String FILE_NAME = "test2.txt";

    public static void main(String[] args) throws IOException {
        InputStream is = Main.class.getClassLoader().getResourceAsStream(FILE_NAME);
        if (is == null) {
            throw new RuntimeException(String.format("File %s does not exist", FILE_NAME));
        }
        byte[] isAsBytes = is.readAllBytes();
        String input = new String(isAsBytes).replaceAll("\\n+", "").replaceAll("\\r+", "");
        StringBuffer stringBuffer = new StringBuffer();
        if (input.trim().matches(XML_INPUT_REGEX)) {
            convertFromXMLToJson(input, stringBuffer);
        } else if (input.trim().matches(JSON_INPUT_REGEX)) {
            if (jsonContainsMoreThanOneParentElement(input.trim())) {
                stringBuffer.append("<root>");
                convertFromJsonToXML("", input, true, stringBuffer);
                stringBuffer.append("</root>");
            } else {
                convertFromJsonToXML("", input, true, stringBuffer);
            }
        } else {
            throw new IllegalArgumentException("Unknown format");
        }
        System.out.println(stringBuffer.toString());
    }
}

