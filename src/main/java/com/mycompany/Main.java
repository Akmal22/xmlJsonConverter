package com.mycompany;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final String TEST_FILE_PATH = "/home/akmal/IdeaProjects/exploring/src/main/resources/test.txt";
    private static final String JSON_ELEMENT_REGEX = "\"(?<elementName>[#@]?\\w+)\"\\s*:\\s*(?<elementValue>(\\{)|(\"[^\"]+\")|(null))";
    private static final String JSON_ATTRIBUTE_REGEX = "\"(?<elementName>@\\w+)\"\\s*:\\s*\"(?<elementValue>([^\"]+)|(null))\"";
    private static final String JSON_ELEMENT_VALUE_REGEX = "\"(?<elementName>#\\w+)\"\\s*:\\s*(?<elementValue>(\"([^\"]+)\")|(null)|(\\{))";
    private static final String JSON_INVALID_ATTRIBUTE_REGEX = "\"(?<elementName>@?\\w+)\"\\s*:\\s*(?<elementValue>\\{)";
    private static final String JSON_INVALID_ATTRIBUTE_NAME_REGEX = "\"(?<elementName>@)\"\\s*:\\s*(?<elementValue>\\{)";
    private static final String JSON_ORDINAL_ELEMENT_REGEX = "\"(?<elementName>\\w+)\"\\s*:\\s*(?<elementValue>(\\{)|(\"[^\"]+\")|(null))";
    private static final String JSON_ORDINAL_ELEMENT_VALUE_REGEX = "\"(?<elementName>\\w+)\"\\s*:\\s*(?<elementValue>(\"([^\"]+)\")|(null))";
    private static final String JSON_ENTRY_REGEX = "^\\{.*}$";

    private static final Pattern jsonElementPattern = Pattern.compile(JSON_ELEMENT_REGEX);

    private static final Pattern jsonEntryPattern = Pattern.compile(JSON_ENTRY_REGEX);

    public static void main(String[] args) throws IOException {
        String input = new String(Files.readAllBytes(Paths.get(TEST_FILE_PATH))).replaceAll("\\n+", "");


    }

    private static String getValidJsonFromInput(String input) {
        return null;
    }


    private static void printJsonHierarchy(String parentElement, String input) {
        if (input.matches(JSON_ENTRY_REGEX)) {
            int i = 0, j = 0; //todo variables for boundary
            boolean isValidSingleXML = true;
            Map<String, String> attributes = new HashMap<>();
            String validSingleXmlElementValue = "";
            Matcher matcher = jsonElementPattern.matcher(input);
            if (isNotBlank(parentElement)) {
                System.out.println("Element:");
                System.out.println("path = " + parentElement);
            }
            while (matcher.find()) {
                String matchedElement = matcher.group();
                String elementName = matcher.group("elementName");
                String elementValue = matcher.group("elementValue");
                if (matchedElement.matches(JSON_INVALID_ATTRIBUTE_NAME_REGEX)
                        || matchedElement.matches(JSON_INVALID_ATTRIBUTE_REGEX)
                        || matchedElement.matches(JSON_ORDINAL_ELEMENT_REGEX)) {

                    isValidSingleXML = false;
                    //todo define boundaries;
                }

                if (matchedElement.matches(JSON_ELEMENT_VALUE_REGEX)) {
                    if (parentElement.equals(elementName)) {
                        if (!elementValue.matches("\\s*\\{\\s*")) {
                            validSingleXmlElementValue = elementValue;
                        } else {
                            if (attributes.size() > 0) {
                                System.out.println("attributes:");
                                for (Map.Entry<String, String> entry : attributes.entrySet()) {
                                    System.out.println(String.format("%s = \"%s\"", entry.getKey(), entry.getValue()));
                                }
                                attributes.clear();
                            }
                            //todo define boundaries
                        }
                    } else {
                        isValidSingleXML = false;
                        String path = isNotBlank(parentElement) ? parentElement + ", " + elementName : elementName;
                        System.out.println("Element:");
                        System.out.println("path = " + path);
                    }
                }

                if (matchedElement.matches(JSON_ATTRIBUTE_REGEX)) {
                    elementName = matcher.group("elementName").substring(1);
                    if (isValidSingleXML) {
                        attributes.put(matcher.group("elementName"), matcher.group("elementValue"));
                    } else {
                        String path = isNotBlank(parentElement) ? parentElement + ", " + elementName : elementName;
                        System.out.println("Element:");
                        System.out.println("path = " + path);
                        System.out.println("value = " + elementValue);
                    }
                }
                if (matchedElement.matches(JSON_ORDINAL_ELEMENT_VALUE_REGEX)) {
                    isValidSingleXML = false;
                    String path = isNotBlank(parentElement) ? parentElement + ", " + elementName : elementName;
                    System.out.println("Element:");
                    System.out.println("path = " + path);
                    System.out.println("value = " + elementValue);
                }
            }

            if (!isNotBlank(validSingleXmlElementValue)) {
                isValidSingleXML = false;
                System.out.println();
            }

            if (isValidSingleXML) {
                if (isNotBlank(validSingleXmlElementValue)) {
                    System.out.println(String.format("value = \"%s\"", validSingleXmlElementValue));
                }
                if (attributes.size() > 0) {
                    System.out.println("attributes:");
                    for (Map.Entry<String, String> entry : attributes.entrySet()) {
                        System.out.println(String.format("%s = \"%s\"", entry.getKey(), entry.getValue()));
                    }
                }
            }
        } else {
            System.out.println("Wrong data format");
        }
    }

    private static boolean isNotBlank(String input) {
        return (input != null && input.length() > 0);
    }
}

