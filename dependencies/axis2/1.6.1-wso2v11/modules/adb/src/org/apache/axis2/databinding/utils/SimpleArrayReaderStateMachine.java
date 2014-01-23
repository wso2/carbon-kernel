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
import java.util.ArrayList;
import java.util.List;

/** A state machine that reads arrays with simple content. returns a string array */
public class SimpleArrayReaderStateMachine implements States, Constants {

    private QName elementNameToTest = null;
    private int currentState = INIT_STATE;
    private boolean nillable = false;

    private boolean canbeAbsent = false;

    private List list = new ArrayList();

    /** @return an array of strings */
    public String[] getTextArray() {
        return (String[])list.toArray(new String[list.size()]);
    }


    public void setNillable() {
        nillable = true;
    }

    public boolean isCanbeAbsent() {
        return canbeAbsent;
    }

    public void setCanbeAbsent(boolean canbeAbsent) {
        this.canbeAbsent = canbeAbsent;
    }

    /**
     * Resets the state machine. Once the reset is called the state machine is good enough for a
     * fresh run
     */
    public void reset() {
        elementNameToTest = null;
        currentState = INIT_STATE;
        nillable = false;
        list = new ArrayList();
    }

    public void setElementNameToTest(QName elementNameToTest) {
        this.elementNameToTest = elementNameToTest;
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
                    list.add(null);
                    //force the state to be null found
                    currentState = NULLED_STATE;
                }
            }

            if (currentState == TEXT_FOUND_STATE) {
                //read the text value and store it in the list
                list.add(reader.getText());
            }
            //increment the parser only if the  state is
            //not finished
            if (currentState != FINISHED_STATE
                    && currentState != ILLEGAL_STATE) {
                reader.next();
            }

        } while (currentState != FINISHED_STATE
                && currentState != ILLEGAL_STATE);

        if (currentState == ILLEGAL_STATE) {
            throw new RuntimeException("Illegal state!");
        }

    }


    private void updateState(XMLStreamReader reader) throws XMLStreamException {
        int event = reader.getEventType();

        switch (currentState) {

            case INIT_STATE:
                if (event == XMLStreamConstants.START_DOCUMENT) {
                    currentState = STARTED_STATE;
                } else if (event == XMLStreamConstants.START_ELEMENT) {
                    if (elementNameToTest.equals(reader.getName())) {
                        currentState = START_ELEMENT_FOUND_STATE;
                    } else {
                        //we found a start element that does not have
                        //the name of the element

                        currentState = canbeAbsent ?
                                FINISHED_STATE :
                                STARTED_STATE;
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    // an end element is found at the init state
                    // we should break the process here ?
                    if (!elementNameToTest.equals(reader.getName())) {
                        currentState = FINISHED_STATE;
                    }
                }
                break;

            case STARTED_STATE:
                if (event == XMLStreamConstants.END_ELEMENT) {
                    if (elementNameToTest.equals(reader.getName())) {
                        currentState = ILLEGAL_STATE;
                    } else {
                        currentState = FINISHED_STATE;
                    }
                } else if (event == XMLStreamConstants.START_ELEMENT) {
                    QName name = reader.getName();
                    if (elementNameToTest.equals(name)) {
                        currentState = START_ELEMENT_FOUND_STATE;
                    }
                }
                break;

            case START_ELEMENT_FOUND_STATE:
                if (event == XMLStreamConstants.CHARACTERS) {
                    currentState = TEXT_FOUND_STATE;
                }
                break;

            case TEXT_FOUND_STATE:
                if (event == XMLStreamConstants.END_ELEMENT) {
                    if (elementNameToTest.equals(reader.getName())) {
                        currentState = END_ELEMENT_FOUND_STATE;
                    } else {
                        currentState = ILLEGAL_STATE;
                    }
                } else if (event == XMLStreamConstants.CHARACTERS) {
                    //another char event -
                    //so append it to the current text
                }
                break;
            case NULLED_STATE:
                //read upto the end and set the state to END_ELEMENT_FOUND_STATE
                while (event != XMLStreamConstants.END_ELEMENT) {
                    event = reader.next();
                }
                currentState = END_ELEMENT_FOUND_STATE;
                break;
            case END_ELEMENT_FOUND_STATE:
                if (event == XMLStreamConstants.START_ELEMENT) {
                    //restart the parsing
                    if (elementNameToTest.equals(reader.getName())) {
                        currentState = START_ELEMENT_FOUND_STATE;
                    } else {
                        currentState = FINISHED_STATE;
                    }
                    //another end element found after end-element
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    currentState = FINISHED_STATE;
                    //end  document found
                }
                break;
            default:

                //characters found - if this is a characters event that was in the correct place then
                //it would have been handled already. we need to check whether this is a ignorable
                //whitespace and if not push the state machine to a illegal state.
                if (event == XMLStreamConstants.CHARACTERS) {
                    if (reader.getText().trim().length() == 0) {
                        //the text is empty - don't change the state
                    } else {
                        //we do NOT handle mixed content
                        currentState = ILLEGAL_STATE;
                    }

                } else {
                    currentState = ILLEGAL_STATE;
                }

        }

    }


}
