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
    public static final String JSON_ELEMENT_REGEX = "\"(?<elementName>[^\"]*)\"\\s*:\\s*(?<elementValue>(\\{)|(\"[^\"]*\")|(null)|(\\d*\\.*\\d*))";

    public static final String XML_INPUT_REGEX = "^<.*>$";
    public static final String JSON_INPUT_REGEX = "^\\{.*\\}$";

    public static final Pattern xmlElementPattern = Pattern.compile(XML_ELEMENT_REGEX);
    public static final Pattern xmlAttributePattern = Pattern.compile(XML_ATTRIBUTE_REGEX);
    public static final Pattern jsonElementPattern = Pattern.compile(JSON_ELEMENT_REGEX);

    public static void convertFromXMLToJson(String input, StringBuffer stringBuffer) {
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
                    if (xmlAttributesMatcher.find()) {
                        stringBuffer.append("{");
                        do {
                            stringBuffer.append(String.format("\"@%s\":\"%s\",",
                                    xmlAttributesMatcher.group("attributeName"), xmlAttributesMatcher.group("attributeValue")));
                        } while (xmlAttributesMatcher.find());
                        stringBuffer.append(String.format("\"#%s\":", elementName));
                        convertFromXMLToJson(elementValue, stringBuffer);
                        stringBuffer.append("}");
                    } else {
                        convertFromXMLToJson(elementValue, stringBuffer);
                    }
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

    public static void convertFromJsonToXML(String parentElement, String input, boolean isValidSingleXML, StringBuffer stringBuffer) {
        Map<String, String> attributes = new LinkedHashMap<>();
        Map<String, String> elements = new LinkedHashMap<>();
        String validSingleXmlElementValue = null;
        Matcher matcher = jsonElementPattern.matcher(input);
        for (int i = 0; i < matcher.groupCount() && matcher.find(); i++) {
            String elementName = matcher.group("elementName");
            String elementValue = matcher.group("elementValue");

            if (!isNotBlank(elementName) || elementName.equals("#") || elementName.equals("@")) {
                isValidSingleXML = false;
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
                        convertFromJsonToXML(parentElement, input.substring(end), false, stringBuffer);
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

                stringBuffer.append(isNotBlank(parentElement) ? String.format("<%s", parentElement) : "");

                if (!elements.isEmpty() && !isValidSingleXML) {
                    stringBuffer.append(isNotBlank(parentElement) ? ">" : "");
                    printElements(elements, stringBuffer);
                    elements.clear();
                } else if (!attributes.isEmpty() && elementName.equals(parentElement) && isValidSingleXML) {
                    printAttributes(attributes, stringBuffer);
                    stringBuffer.append(isNotBlank(parentElement) ? ">" : "");
                    attributes.clear();
                } else {
                    stringBuffer.append(isNotBlank(parentElement) ? ">" : "");
                }

                int start = matcher.end();
                int end = start;
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

                if (!isNotBlank(input.substring(start, end - 1).replaceAll("\\s+", ""))) {
                    stringBuffer.append(String.format("<%s></%s>", elementName, elementName));
                } else {
                    if (parentElement.equals(elementName) && isValidSingleXML) {
                        convertFromJsonToXML("", input.substring(start, end - 1), true, stringBuffer);
                    } else {
                        convertFromJsonToXML(elementName, input.substring(start, end - 1), true, stringBuffer);
                    }
                }
                if (input.length() > end) {
                    convertFromJsonToXML("", input.substring(end), isValidSingleXML, stringBuffer);
                }

                stringBuffer.append(isNotBlank(parentElement) ? String.format("</%s>", parentElement) : "");
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
            if (!elements.isEmpty()) {
                stringBuffer.append(isNotBlank(parentElement) ? String.format("<%s>", parentElement) : "");
                printElements(elements, stringBuffer);
                elements.clear();
                stringBuffer.append(isNotBlank(parentElement) ? String.format("</%s>", parentElement) : "");
            } else {
                stringBuffer.append(isNotBlank(parentElement) && !input.contains("{") ? String.format("<%s></%s>", parentElement, parentElement) : "");
            }
        } else {
            if (isNotBlank(validSingleXmlElementValue)) {
                stringBuffer.append(isNotBlank(parentElement) ? String.format("<%s", parentElement) : "");
                if (!attributes.isEmpty()) {
                    printAttributes(attributes, stringBuffer);
                    attributes.clear();
                }
                stringBuffer.append(validSingleXmlElementValue.equals("null")
                        ? "/>"
                        : String.format(">%s</%s>", validSingleXmlElementValue.replaceAll("\"", ""), parentElement));
            } else {
                if (!attributes.isEmpty()) {
                    stringBuffer.append(isNotBlank(parentElement) ? String.format("<%s>", parentElement) : "");
                    printElements(attributes, stringBuffer);
                    attributes.clear();
                    stringBuffer.append(String.format("</%s>", parentElement));
                }
            }
        }
    }

    public static boolean jsonContainsMoreThanOneParentElement(String jsonInput) {
        Matcher matcher = jsonElementPattern.matcher(jsonInput);

        if (matcher.find()) {
            String elementValue = matcher.group("elementValue");

            if (!elementValue.trim().equals("{")) {
                return false;
            }

            int start = matcher.end();
            int end = start + 1;
            Deque<Character> brackets = new ArrayDeque<>();
            brackets.offer('{');

            while (!brackets.isEmpty()) {
                if (jsonInput.charAt(end) == '}') {
                    brackets.poll();
                } else if (jsonInput.charAt(end) == '{') {
                    brackets.push('{');
                }

                end++;
            }

            if (jsonInput.substring(end).contains("{")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean isNotBlank(String input) {
        return (input != null && input.length() > 0);
    }

    private static void printElements(Map<String, String> attributes, StringBuffer stringBuffer) {
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            if (entry.getValue().equals("null")) {
                stringBuffer.append(String.format("<%s/>", entry.getKey()));
            } else {
                stringBuffer.append(String.format("<%s>%s</%s>", entry.getKey(), entry.getValue().replaceAll("\"", ""), entry.getKey()));
            }
        }
    }

    private static void printAttributes(Map<String, String> attributes, StringBuffer stringBuffer) {
        stringBuffer.append(" ");
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            stringBuffer.append(String.format("%s=%s ", entry.getKey(), entry.getValue().equals("null") ? "\"\"" : entry.getValue().contains("\"") ? entry.getValue() : String.format("\"%s\"", entry.getValue())));
        }
    }
}
