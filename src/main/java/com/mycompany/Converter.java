package com.mycompany;

import java.util.HashMap;
import java.util.Map;
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

    public static void printJsonHierarchy(String input, String parentElements) {

    }
}
