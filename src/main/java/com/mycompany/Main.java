package com.mycompany;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final String TEST_FILE_PATH = "/home/akmal/IdeaProjects/xmlJsonConverter/src/main/resources/test2.txt";
    private static final String JSON_ELEMENT_REGEX = "\"(?<elementName>[^\"]*)\"\\s*:\\s*(?<elementValue>(\\{)|(\"[^\"]*\")|(null)|(\\d*\\.*\\d*))";

    private static final Pattern jsonElementPattern = Pattern.compile(JSON_ELEMENT_REGEX);

    public static void main(String[] args) throws IOException {
        String input = new String(Files.readAllBytes(Paths.get(TEST_FILE_PATH))).replaceAll("\\n+", "");
        printJsonHierarchy("", "", input, true);
    }

    public static void printJsonHierarchy(String parentPath, String parentElement, String input, boolean isValidSingleXML) {
        boolean hasInvalidElements = false;
        Map<String, String> attributes = new LinkedHashMap<>();
        Map<String, String> elements = new LinkedHashMap<>();
        String validSingleXmlElementValue = null;
        Matcher matcher = jsonElementPattern.matcher(input);
        for (int i = 0; i < matcher.groupCount() && matcher.find(); i++) {
            String elementName = matcher.group("elementName");
            String elementValue = matcher.group("elementValue");

            if (!isNotBlank(elementName) || elementName.equals("#") || elementName.equals("@")) {
                isValidSingleXML = false;
                hasInvalidElements = true;
                if (elementValue.equals("{")) {
                    int start = matcher.end();
                    int end = start + 1;
                    Deque<Character> brackets = new ArrayDeque<>();
                    brackets.offer('{');

                    while (!brackets.isEmpty()) {
                        if (input.charAt(end) == '}') {
                            brackets.poll();
                        } else if (input.charAt(end) == '{') {
                            brackets.push('{');
                        }

                        end++;
                    }
                    if (input.length() > end) {
                        printJsonHierarchy(parentPath, parentElement, input.substring(end), false);
                    }
                    return;
                } else {
                    continue;
                }
            }

            if (elementValue.trim().equals("{")) {
                if (!elementName.matches("^#\\w+")) {
                    isValidSingleXML = false;
                } else {
                    elementName = elementName.substring(1);
                }

                if (elementName.startsWith("@")) {
                    elementName = elementName.substring(1);
                }

                if (!elements.isEmpty() && !isValidSingleXML) {
                    printElements(elements, parentPath);
                    elements.clear();
                }

                String path = elementName.equals(parentElement) && isValidSingleXML ? parentPath : isNotBlank(parentPath) ? parentPath + ", " + elementName : elementName;
                if (!isValidSingleXML) {
                    System.out.println();
                    System.out.println("Element:");
                    System.out.println("path = " + path);
                }

                if (!attributes.isEmpty() && elementName.equals(parentElement) && isValidSingleXML) {
                    printAttributes(attributes);
                    attributes.clear();
                }

                int start = matcher.end();
                int end = start + 1;
                Deque<Character> brackets = new ArrayDeque<>();
                brackets.offer('{');

                while (!brackets.isEmpty()) {
                    if (input.charAt(end) == '}') {
                        brackets.poll();
                    } else if (input.charAt(end) == '{') {
                        brackets.push('{');
                    }

                    end++;
                }

                if (input.substring(start, end - 1).matches("\\s*")) {
                    System.out.println("value = \"\"");
                }
                printJsonHierarchy(path, elementName, input.substring(start, end - 1), true);
                if (input.length() > end) {
                    printJsonHierarchy(parentPath, parentElement, input.substring(end), isValidSingleXML);
                }
                break;
            }
            if (elementName.matches("@[^\"]+") && isValidSingleXML) {
                elements.putIfAbsent(elementName.substring(1), elementValue);
                attributes.putIfAbsent(elementName.substring(1), elementValue);
            } else if (elementName.matches("#[^\"]+")) {
                elementName = elementName.substring(1);
                if (elementName.equals(parentElement) && isValidSingleXML) {
                    elements.putIfAbsent(elementName, elementValue);
                    validSingleXmlElementValue = elementValue;
                } else {
                    elements.putIfAbsent(elementName, elementValue);
                    isValidSingleXML = false;
                }
            } else {
                elements.put(elementName, elementValue);
                isValidSingleXML = false;
            }
        }

        if (!isValidSingleXML) {
            String path = isNotBlank(parentPath)
                    ? parentPath
                    : parentElement;
            printElements(elements, path);
        } else {
            if (isNotBlank(validSingleXmlElementValue)) {
                System.out.println("value = " + validSingleXmlElementValue);
                if (!attributes.isEmpty()) {
                    printAttributes(attributes);
                    attributes.clear();
                }
            } else {
                String path = isNotBlank(parentPath)
                        ? parentPath
                        : parentElement;
                printElements(attributes, path);
                attributes.clear();
            }
        }

        if (hasInvalidElements && attributes.isEmpty() && elements.isEmpty() && isNotBlank(input)) {
            System.out.println("value = \"\"");
        }
    }

    private static boolean isNotBlank(String input) {
        return (input != null && input.length() > 0);
    }

    private static void printElements(Map<String, String> attributes, String parentPath) {
        if (isNotBlank(parentPath)) {
            for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                System.out.println();
                System.out.println("Element:");
                System.out.println("path = " + parentPath + ", " + attribute.getKey());
                String valueToPrint = attribute.getValue().matches("\\d+\\.?\\d*") ? String.format("\"%s\"", attribute.getValue()) : attribute.getValue();
                System.out.println(String.format("value = %s", valueToPrint));
            }
        } else {
            for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                System.out.println();
                System.out.println("Element:");
                System.out.println("path = " + attribute.getKey());
                String valueToPrint = attribute.getValue().matches("\\d+\\.?\\d*") ? String.format("\"%s\"", attribute.getValue()) : attribute.getValue();
                System.out.println(String.format("value = %s", valueToPrint));
            }
        }
    }

    private static void printAttributes(Map<String, String> attributes) {
        System.out.println("attributes:");
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String valueToPrint = entry.getValue().equals("null")
                    ? "\"\""
                    : entry.getValue().matches("\\d+\\.?\\d*") ? String.format("\"%s\"", entry.getValue()) : entry.getValue();
            System.out.println(String.format("%s = %s", entry.getKey(), valueToPrint));
        }
    }
}

