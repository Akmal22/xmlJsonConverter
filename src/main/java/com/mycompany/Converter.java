package com.mycompany;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Converter {
    public static final String XML_ELEMENT_REGEX = "<(?<elementEntry>(?<elementName>\\w+)\\s*[^></]*\\s*)((>(?<elementValue>.*?)</(\\k<elementName>)>)|(/>))";
    public static final String XML_ATTRIBUTE_REGEX = "(?<attributeName>\\w+)\\s*=\\s*\"(?<attributeValue>[^\"]+)\"";

    public static final String JSON_REGEX = "^\\{\\s*(\"(?<paramName>\\w+)\")\\s*:\\s*\\{(?<paramValue>.+)\\s*}\\s*}\\s*$";
    private static final String JSON_ELEMENT_REGEX = "\"(?<elementName>[^\"]*)\"\\s*:\\s*(?<elementValue>(\\{)|(\"[^\"]*\")|(null)|(\\d*\\.*\\d*))";
    public static final String JSON_ELEMENT_ATTRIBUTE_REGEX = "^\\{\\s*\"(?<elementName>@\\w+)\"\\s*:\\s*(?<elementValue>(null)|(\"\\s*\")|(\\{.*}))}$";

    public static final Pattern jsonPattern = Pattern.compile(JSON_REGEX);
    public static final Pattern jsonAttributePattern = Pattern.compile(JSON_ELEMENT_ATTRIBUTE_REGEX);
    public static final Pattern jsonElementPattern = Pattern.compile(JSON_ELEMENT_REGEX);

    public static final Pattern xmlElementPattern = Pattern.compile(XML_ELEMENT_REGEX);
    public static final Pattern xmlAttributePattern = Pattern.compile(XML_ATTRIBUTE_REGEX);

    public static void convertFromXMlToJson(String input, StringBuffer stringBuffer) {
        Matcher xmlMatcher = xmlElementPattern.matcher(input);
        stringBuffer.append("{");
        while (xmlMatcher.find()) {
            String elementName = xmlMatcher.group("elementName");
            String elementValue = xmlMatcher.group("elementValue");
            String elementEntry = xmlMatcher.group("elementEntry");
            if (stringBuffer.lastIndexOf("{") == stringBuffer.length() - 1) {
                stringBuffer.append(String.format("\"%s\":", elementName));
            } else {
                stringBuffer.append(String.format(",\"%s\":", elementName));
            }
            Matcher xmlAttributesMatcher = xmlAttributePattern.matcher(elementEntry);

            if (elementValue != null) {
                Matcher xmlElementValueMatcher = xmlElementPattern.matcher(elementValue);
                if (xmlElementValueMatcher.find()) {
                    convertFromXMlToJson(elementValue, stringBuffer);
                } else {
                    if (xmlAttributesMatcher.find()) {
                        stringBuffer.append("{");
                        do {
                            stringBuffer.append(String.format("\"@%s\":\"%s\",",
                                    xmlAttributesMatcher.group("attributeName"), xmlAttributesMatcher.group("attributeValue")));
                        } while (xmlAttributesMatcher.find());
                        stringBuffer.append(String.format("\"#%s\":\"%s\"", elementName, elementValue));
                        stringBuffer.append("}");
                    } else {
                        stringBuffer.append(String.format("\"%s\"", elementValue));
                    }
                }
            } else {
                if (xmlAttributesMatcher.find()) {
                    stringBuffer.append("{");
                    do {
                        stringBuffer.append(String.format("\"@%s\":\"%s\",",
                                xmlAttributesMatcher.group("attributeName"), xmlAttributesMatcher.group("attributeValue")));
                    } while (xmlAttributesMatcher.find());
                    stringBuffer.append(String.format("\"#%s\":null", elementName));
                    stringBuffer.append("}");
                } else {
                    stringBuffer.append("null");
                }
            }
        }
        stringBuffer.append("}");
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
