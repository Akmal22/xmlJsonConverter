package com.mycompany;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.mycompany.Converter.*;

public class Main {
    public static void main(String[] args) {
        try {
            String input = new String(Files.readAllBytes(Paths.get("test.txt"))).replaceAll("\\n+", "");
            printJsonHierarchy(input, "");
        } catch (IOException exc) {
            System.out.println("File test.txt does not exist");
        }
    }

    private static String convert(String input) {
        if (input.matches(XML_DOCUMENT_REGEX)) {
            return convertFromXMLToJson(input);
        } else if (input.matches(JSON_REGEX)) {
            return convertFromJsonToXML(input);
        }

        return null;
    }
}
