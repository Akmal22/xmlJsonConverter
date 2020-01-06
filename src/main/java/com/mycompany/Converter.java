package com.mycompany;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Converter {
    public static final String XML_DOCUMENT_REGEX = "^<(?<xmlParent>\\w+)((</(\\k<xmlParent>)>)|(/>))$";
    public static final String XML_ELEMENT_REGEX = "<(?<elementEntry>(?<elementName>[A-Za-z]+)\\s*[^></]*\\s*)((>(?<elementValue>.*?)</(\\k<elementName>)>)|(/>))";
    public static final String XML_ATTRIBUTE_REGEX = "(?<attributeName>\\w+)\\s*=\\s*\"(?<attributeValue>[^\"]+)\"";
    public static final String JSON_REGEX = "^\\{\\s*(\"(?<paramName>\\w+)\")\\s*:\\s*\\{(?<paramValue>.+)\\s*}\\s*}\\s*$";
    public static final String JSON_ELEMENT_VALUE_REGEX = "\"#[A-Za-z]+\"\\s*:\\s*(?<elementValue>(\"[^,\"]*\"|null|\\d*))";
    public static final String JSON_ATTRIBUTE_REGEX = "\"@(?<attributeName>\\w+)\"\\s*:\\s*(?<attributeValue>(\"[^,\"]*\"|null|\\d*))";

    public static final String JSON_ELEMENT_REGEX = "^\\{\\s*\"(?<elementName>[@#]?\\w+)\"\\s*:\\s*(?<elementValue>(null)|(\"\\s*\")|(\\{}))}$";
    public static final String JSON_ELEMENT_VALUE = "^\\{\\s*\"(?<elementName>#\\w+)\"\\s*:\\s*"; //todo rename constant name

    public static final Pattern jsonPattern = Pattern.compile(JSON_REGEX);
    public static final Pattern jsonAttributePattern = Pattern.compile(JSON_ATTRIBUTE_REGEX);
    public static final Pattern jsonElementPattern = Pattern.compile(JSON_ELEMENT_REGEX);
    public static final Pattern jsonElementValuePattern = Pattern.compile(JSON_ELEMENT_VALUE);
    public static final Pattern xmlDocumentPattern = Pattern.compile(XML_DOCUMENT_REGEX);
    public static final Pattern xmlElementPattern = Pattern.compile(XML_ELEMENT_REGEX);
    public static final Pattern xmlAttributePattern = Pattern.compile(XML_ATTRIBUTE_REGEX);

    public static String convertFromJsonToXML(String input) {
        StringBuilder output = new StringBuilder("<");
        Map<String, String> attributes = new HashMap<>();
        String elementName;
        String elementValue;

        Matcher jsonMatcher = jsonPattern.matcher(input);
        Matcher jsonAttributesMatcher = jsonAttributePattern.matcher(input);
        Matcher jsonElementMatcher = jsonElementPattern.matcher(input);

        if (jsonMatcher.matches()) {
            elementName = jsonMatcher.group("paramName");
        } else {
            throw new RuntimeException("Invalid input type");
        }

        if (jsonElementMatcher.find()) {
            elementValue = jsonElementMatcher.group("elementValue");
        } else {
            throw new RuntimeException("Invalid input type");
        }

        if (jsonAttributesMatcher.find()) {
            do {
                attributes.put(jsonAttributesMatcher.group("attributeName"), jsonAttributesMatcher.group("attributeValue"));
            }
            while (jsonAttributesMatcher.find());
        }

        output.append(elementName);

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            output
                    .append(" ")
                    .append(entry.getKey())
                    .append("=")
                    .append("\"")
                    .append(entry.getValue().replaceAll("\"", ""))
                    .append("\"");
        }

        if (elementValue == null || elementValue.equals("null")) {
            output.append("/>");
        } else {
            output
                    .append(">")
                    .append(elementValue.replaceAll("\"", ""))
                    .append("</")
                    .append(elementName)
                    .append(">");
        }

        return output.toString();
    }

    public static String convertFromXMLToJson(String input) {
        StringBuilder output = new StringBuilder();
        Map<String, String> attributes = new HashMap<>();
        String elementName;
        String elementValue;

        Matcher xmlMatcher = xmlElementPattern.matcher(input);
        Matcher xmlAttributeMatcher = xmlAttributePattern.matcher(input);

        if (xmlMatcher.matches()) {
            elementName = xmlMatcher.group("elementName");
            elementValue = xmlMatcher.group("elementValue");
        } else {
            throw new RuntimeException("Invalid XML format");
        }

        if (xmlAttributeMatcher.find()) {
            do {
                attributes.put(xmlAttributeMatcher.group("attributeName"), xmlAttributeMatcher.group("attributeValue"));
            } while (xmlAttributeMatcher.find());
        }

        output
                .append("{\"")
                .append(elementName)
                .append("\":{")
                .append("\"#")
                .append(elementName)
                .append("\":")
                .append(elementValue == null ? null : "\"" + elementValue + "\"");

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            output.append(",\"@")
                    .append(entry.getKey())
                    .append("\":\"")
                    .append(entry.getValue())
                    .append("\"");
        }

        output
                .append("}}");

        return output.toString();
    }

    public static void printXMLHierarchy(String input, String parentElements) {
        Matcher xmlMatcher = xmlElementPattern.matcher(input);

        while (xmlMatcher.find()) {
            String elementName = xmlMatcher.group("elementName");
            String elementValue = xmlMatcher.group("elementValue");
            String elementEntry = xmlMatcher.group("elementEntry");
            System.out.println("Element:");
            System.out.println("path = " + parentElements + elementName);
            Matcher xmlAttributesMatcher = xmlAttributePattern.matcher(elementEntry);

            if (elementValue != null) {
                Matcher xmlElementValueMatcher = xmlElementPattern.matcher(elementValue);
                if (xmlElementValueMatcher.find()) {
                    if (xmlAttributesMatcher.find()) {
                        System.out.println("attributes:");
                        do {
                            System.out.println(String.format("%s = \"%s\"", xmlAttributesMatcher.group("attributeName"), xmlAttributesMatcher.group("attributeValue")));
                        } while (xmlAttributesMatcher.find());
                    }
                    printXMLHierarchy(elementValue, parentElements + elementName + ", ");
                } else {
                    System.out.println(String.format("value = \"%s\"", elementValue));
                    if (xmlAttributesMatcher.find()) {
                        System.out.println("attributes:");
                        do {
                            System.out.println(String.format("%s = \"%s\"", xmlAttributesMatcher.group("attributeName"), xmlAttributesMatcher.group("attributeValue")));
                        } while (xmlAttributesMatcher.find());
                    }
                }
            } else {
                System.out.println("value = " + elementValue);
                if (xmlAttributesMatcher.find()) {
                    System.out.println("attributes:");
                    do {
                        System.out.println(String.format("%s = \"%s\"", xmlAttributesMatcher.group("attributeName"), xmlAttributesMatcher.group("attributeValue")));
                    } while (xmlAttributesMatcher.find());
                }
            }
        }
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
