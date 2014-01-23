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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * A state machine to read elements with simple content. Returns the text of the element and the
 * stream reader will be one event beyond the end element at return
 */
public class SimpleElementReaderStateMachine implements States, Constants {


    private QName elementNameToTest = null;
    private int currentState = INIT_STATE;
    private boolean nillable = false;
    private String text = "";
    private String errorMessage = "";
    private boolean elementSkipped = false;

    public boolean isElementSkipped() {
        return elementSkipped;
    }

    /**
     *
     */
    public String getText() {
        return text;
    }

    /** sets the nillable flag */
    public void setNillable() {
        nillable = true;
    }

    /**
     * the Qname of the element to be tested
     *
     * @param elementNameToTest
     */
    public void setElementNameToTest(QName elementNameToTest) {
        this.elementNameToTest = elementNameToTest;

    }

    /**
     * Resets the state machine. Once the reset is called the state machine is good enough for a
     * fresh run
     */
    public void reset() {
        elementNameToTest = null;
        currentState = INIT_STATE;
        nillable = false;
        text = "";
        errorMessage = "";
    }

    /**
     * public read method - reads a given reader to extract the text value
     *
     * @param reader
     */
    public void read(XMLStreamReader reader) throws XMLStreamException {

        do {
            updateState(reader);

            //test for the nillable attribute
            if (currentState == START_ELEMENT_FOUND_STATE &&
                    nillable) {
                if (TRUE.equals(reader.getAttributeValue(XSI_NAMESPACE, NIL))) {
                    text = null;
                    //force the state to be null found
                    currentState = NULLED_STATE;
                }
            }

            if (currentState == TEXT_FOUND_STATE) {
                //read the text value and store it
                text = reader.getText();
            }
            if (currentState != FINISHED_STATE
                    && currentState != ILLEGAL_STATE) {
                reader.next();
            }

        } while (currentState != FINISHED_STATE
                && currentState != ILLEGAL_STATE);

        if (currentState == ILLEGAL_STATE) {
            throw new RuntimeException("Illegal state!" + errorMessage);
        }

    }


    /**
     * Updates the state depending on the parser
     *
     * @param reader
     */
    private void updateState(XMLStreamReader reader) throws XMLStreamException {
        int event = reader.getEventType();


        switch (currentState) {
            case INIT_STATE:
                if (event == XMLStreamConstants.START_DOCUMENT) {
                    currentState = STARTED_STATE;
                    //start element found at init
                } else if (event == XMLStreamConstants.START_ELEMENT) {
                    if (elementNameToTest.equals(reader.getName())) {
                        currentState = START_ELEMENT_FOUND_STATE;
                    } else {
                        currentState = STARTED_STATE;
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    // an end element is found at the init state
                    // we should break the process here ?
                    currentState = FINISHED_STATE;
                    elementSkipped = true;
                }
                break;

            case STARTED_STATE:
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (elementNameToTest.equals(reader.getName())) {
                        currentState = START_ELEMENT_FOUND_STATE;
                    }
                }
                break;

            case START_ELEMENT_FOUND_STATE:
                if (event == XMLStreamConstants.CHARACTERS) {
                    currentState = TEXT_FOUND_STATE;
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    //force the text to be empty!
                    text = "";
                    if (elementNameToTest.equals(reader.getName())) {
                        currentState = END_ELEMENT_FOUND_STATE;
                    } else {
                        currentState = ILLEGAL_STATE;
                        errorMessage = "Wrong element name " + reader.getName();  //todo I18n this
                    }
                }
                break;

            case TEXT_FOUND_STATE:
                if (event == XMLStreamConstants.END_ELEMENT) {
                    if (elementNameToTest.equals(reader.getName())) {
                        currentState = END_ELEMENT_FOUND_STATE;
                    } else {
                        currentState = ILLEGAL_STATE;
                        //set the error message
                        errorMessage = "Wrong element name " + reader.getName();  //todo I18n this
                    }
                } else if (event == XMLStreamConstants.CHARACTERS) {
                    text = text + reader.getText();  //append the text
                    //do not change the state
                }
                break;

            case END_ELEMENT_FOUND_STATE:
                currentState = FINISHED_STATE;
                break;

                //the element was found to be null and this state was forced.
                //we are sure here that the parser was at the
                //START_ELEMENT_FOUND_STATE before
                //being forced. Hence we need to advance the parser upto the
                // end element and set the state to be end element found
            case NULLED_STATE:
                while (event != XMLStreamConstants.END_ELEMENT) {
                    event = reader.next();
                }
                currentState = END_ELEMENT_FOUND_STATE;
                break;

            default:
                if (event == XMLStreamConstants.CHARACTERS) {
                    if (reader.getText().trim().length() == 0) {
                        //the text is empty - don't change the state
                    } else {
                        //we do NOT handle mixed content
                        currentState = ILLEGAL_STATE;
                        errorMessage = "Mixed Content " + reader.getText();
                    }
                } else {
                    currentState = ILLEGAL_STATE;
                    errorMessage = "Current state is " + currentState;
                }
                break;
        }

    }


}
