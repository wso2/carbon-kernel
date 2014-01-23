/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.databinding.utils;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class PrintEvents {

    public static void print(XMLStreamReader xmlr) throws XMLStreamException {
        while (xmlr.hasNext()) {
            printEvent(xmlr);
            xmlr.next();
        }
    }

    private static void printEvent(XMLStreamReader xmlr) {

        System.out.print("EVENT:[" + getEventString(xmlr.getEventType()) + "] ");

        System.out.print(" [");

        switch (xmlr.getEventType()) {

            case XMLStreamConstants.START_ELEMENT:
                System.out.print("<");
                printName(xmlr);
                printNamespaces(xmlr);
                printAttributes(xmlr);
                System.out.print(">");
                break;

            case XMLStreamConstants.END_ELEMENT:
                System.out.print("</");
                printName(xmlr);
                System.out.print(">");
                break;

            case XMLStreamConstants.SPACE:

            case XMLStreamConstants.CHARACTERS:
                int start = xmlr.getTextStart();
                int length = xmlr.getTextLength();
                System.out.print(new String(xmlr.getTextCharacters(),
                                            start,
                                            length));
                break;

            case XMLStreamConstants.PROCESSING_INSTRUCTION:
                System.out.print("<?");
                if (xmlr.hasText())
                    System.out.print(xmlr.getText());
                System.out.print("?>");
                break;

            case XMLStreamConstants.CDATA:
                System.out.print("<![CDATA[");
                start = xmlr.getTextStart();
                length = xmlr.getTextLength();
                System.out.print(new String(xmlr.getTextCharacters(),
                                            start,
                                            length));
                System.out.print("]]>");
                break;

            case XMLStreamConstants.COMMENT:
                System.out.print("<!--");
                if (xmlr.hasText())
                    System.out.print(xmlr.getText());
                System.out.print("-->");
                break;

            case XMLStreamConstants.ENTITY_REFERENCE:
                System.out.print(xmlr.getLocalName() + "=");
                if (xmlr.hasText())
                    System.out.print("[" + xmlr.getText() + "]");
                break;

            case XMLStreamConstants.START_DOCUMENT:
                System.out.print("<?xml");
                System.out.print(" version='" + xmlr.getVersion() + "'");
                System.out.print(" encoding='" + xmlr.getCharacterEncodingScheme() + "'");
                if (xmlr.isStandalone())
                    System.out.print(" standalone='yes'");
                else
                    System.out.print(" standalone='no'");
                System.out.print("?>");
                break;

        }
        System.out.println("]");
    }

    private static void printName(XMLStreamReader xmlr) {
        if (xmlr.hasName()) {
            String prefix = xmlr.getPrefix();
            String uri = xmlr.getNamespaceURI();
            String localName = xmlr.getLocalName();
            printName(prefix, uri, localName);
        }
    }

    private static void printName(String prefix,
                                  String uri,
                                  String localName) {
        if (uri != null && !("".equals(uri))) System.out.print("['" + uri + "']:");
        if (prefix != null) System.out.print(prefix + ":");
        if (localName != null) System.out.print(localName);
    }

    private static void printAttributes(XMLStreamReader xmlr) {
        for (int i = 0; i < xmlr.getAttributeCount(); i++) {
            printAttribute(xmlr, i);
        }
    }

    private static void printAttribute(XMLStreamReader xmlr, int index) {
        String prefix = xmlr.getAttributePrefix(index);
        String namespace = xmlr.getAttributeNamespace(index);
        String localName = xmlr.getAttributeLocalName(index);
        String value = xmlr.getAttributeValue(index);
        System.out.print(" ");
        printName(prefix, namespace, localName);
        System.out.print("='" + value + "'");
    }

    private static void printNamespaces(XMLStreamReader xmlr) {
        for (int i = 0; i < xmlr.getNamespaceCount(); i++) {
            printNamespace(xmlr, i);
        }
    }

    private static void printNamespace(XMLStreamReader xmlr, int index) {
        String prefix = xmlr.getNamespacePrefix(index);
        String uri = xmlr.getNamespaceURI(index);
        System.out.print(" ");
        if (prefix == null)
            System.out.print("xmlns='" + uri + "'");
        else
            System.out.print("xmlns:" + prefix + "='" + uri + "'");
    }

    private static String getEventString(int eventCode) {
        String event = "";

        switch (eventCode) {
            case 1 :
                event = "START_ELEMENT";
                break;
            case 2 :
                event = "END_ELEMENT";
                break;
            case 3 :
                event = "PROCESSING_INSTRUCTION";
                break;
            case 4 :
                event = "CHARACTERS";
                break;
            case 5 :
                event = "COMMENT";
                break;
            case 6 :
                event = "SPACE";
                break;
            case 7 :
                event = "START_DOCUMENT";
                break;
            case 8 :
                event = "END_DOCUMENT";
                break;
            case 9 :
                event = "ENTITY_REFERENCE";
                break;
            case 10 :
                event = "ATTRIBUTE";
                break;
            case 11 :
                event = "DTD";
                break;
            case 12 :
                event = "CDATA";
                break;
            case 13 :
                event = "NAMESPACE";
                break;
            case 14 :
                event = "NOTATION_DECLARATION";
                break;
            case 15 :
                event = "ENTITY_DECLARATION";
                break;
        }
        return event;
    }

}
