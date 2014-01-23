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
package org.apache.axiom.testutils.stax;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamReader;

import junit.framework.Assert;

/**
 * Helper class that compares the events produced by two {@link XMLStreamReader} objects.
 * Note that this class is not meant to be used to compare two XML documents (the error
 * reporting would not be clear enough for that purpose), but to validate implementations
 * of the {@link XMLStreamReader} interface. It uses a brute force approach: for each event,
 * all methods (that don't modify the reader state) are called on both readers and the results
 * (return values or exceptions thrown) of these invocations are compared to each other.
 */
public class XMLStreamReaderComparator extends Assert {
    private static final Map noPrefixValueMap = Collections.singletonMap("", null);
    
    private final XMLStreamReader expected;
    private final XMLStreamReader actual;
    private final LinkedList path = new LinkedList();
    
    /**
     * Set collecting all prefixes seen in the document to be able to test
     * {@link XMLStreamReader#getNamespaceURI(String)}.
     */
    private final Set prefixes = new HashSet();
    
    /**
     * Set collecting all namespace URIs seen in the document to be able to
     * test {@link NamespaceContext#getPrefix(String)}.
     */
    private final Set namespaceURIs = new HashSet();
    
    public XMLStreamReaderComparator(XMLStreamReader expected, XMLStreamReader actual) {
        this.expected = expected;
        this.actual = actual;
    }

    private String getLocation() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("event type ");
        buffer.append(expected.getEventType());
        buffer.append("; location ");
        for (Iterator it = path.iterator(); it.hasNext(); ) {
            buffer.append('/');
            buffer.append(it.next());
        }
        return buffer.toString();
    }
    
    private Object[] invoke(String methodName, Class[] paramTypes, Object[] args) throws Exception {
        Method method = XMLStreamReader.class.getMethod(methodName, paramTypes);
        
        Object expectedResult;
        Throwable expectedException;
        try {
            expectedResult = method.invoke(expected, args);
            expectedException = null;
        } catch (InvocationTargetException ex) {
            expectedResult = null;
            expectedException = ex.getCause();
        }

        Object actualResult;
        Throwable actualException;
        try {
            actualResult = method.invoke(actual, args);
            actualException = null;
        } catch (InvocationTargetException ex) {
            actualResult = null;
            actualException = ex.getCause();
        }
        
        if (expectedException == null) {
            if (actualException != null) {
                actualException.printStackTrace(System.out);
                fail("Method " + methodName + " threw unexpected exception " +
                        actualException.getClass().getName() + " (" + getLocation() + ")");
            } else {
                return new Object[] { expectedResult, actualResult };
            }
        } else {
            if (actualException == null) {
                fail("Expected " + methodName + " to throw " +
                        expectedException.getClass().getName() +
                        ", but the method retuned normally (" + getLocation() + ")");
            } else {
                assertEquals(expectedException.getClass(), actualException.getClass());
            }
        }
        return null;
    }
    
    private Object[] invoke(String methodName) throws Exception {
        return invoke(methodName, new Class[0], new Object[0]);
    }

    private Object assertSameResult(String methodName, Class[] paramTypes, Object[] args,
            Map valueMap) throws Exception {
        
        Object[] results = invoke(methodName, paramTypes, args);
        if (results != null) {
            Object expected = results[0];
            Object actual = results[1];
            if (valueMap != null) {
                // Attention! The value in the map can be null
                if (valueMap.containsKey(expected)) {
                    expected = valueMap.get(expected);
                }
                if (valueMap.containsKey(actual)) {
                    actual = valueMap.get(actual);
                }
            }
            assertEquals("Return value of " + methodName + " for arguments " +
                        Arrays.asList(args) + " (" + getLocation() + ")",
                        expected, actual);
            return results[0];
        } else {
            return null;
        }
    }
    
    private Object assertSameResult(String methodName, Class[] paramTypes, Object[] args) throws Exception {
        return assertSameResult(methodName, paramTypes, args, null);
    }
    
    private Object assertSameResult(String methodName, Map valueMap) throws Exception {
        return assertSameResult(methodName, new Class[0], new Object[0], valueMap);
    }

    private Object assertSameResult(String methodName) throws Exception {
        return assertSameResult(methodName, null);
    }
    
    private Set toPrefixSet(Iterator it) {
        Set set = new HashSet();
        while (it.hasNext()) {
            String prefix = (String)it.next();
            // TODO: Woodstox returns null instead of "" for the default namespace.
            //       This seems incorrect, but the javax.namespace.NamespaceContext specs are
            //       not very clear.
            set.add(prefix == null ? "" : prefix);
        }
        return set;
    }
    
    private void compareNamespaceContexts(NamespaceContext expected, NamespaceContext actual) {
        for (Iterator it = prefixes.iterator(); it.hasNext(); ) {
            String prefix = (String)it.next();
            if (prefix != null) {
                assertEquals("Namespace URI for prefix '" + prefix + "' (" + getLocation() + ")", expected.getNamespaceURI(prefix), actual.getNamespaceURI(prefix));
            }
        }
        for (Iterator it = namespaceURIs.iterator(); it.hasNext(); ) {
            String namespaceURI = (String)it.next();
            if (namespaceURI != null && namespaceURI.length() > 0) {
                assertEquals(
                        "Prefix for namespace URI '" + namespaceURI + "' (" + getLocation() + ")",
                        expected.getPrefix(namespaceURI),
                        actual.getPrefix(namespaceURI));
                assertEquals(
                        "Prefixes for namespace URI '" + namespaceURI + "' (" + getLocation() + ")",
                        toPrefixSet(expected.getPrefixes(namespaceURI)),
                        toPrefixSet(actual.getPrefixes(namespaceURI)));
            }
        }
    }
    
    /**
     * Add a prefix that should be used in testing the
     * {@link XMLStreamReader#getNamespaceURI(String)} method.
     * 
     * @param prefix the prefix to add
     */
    public void addPrefix(String prefix) {
        prefixes.add(prefix);
    }
    
    public void compare() throws Exception {
        while (true) {
            int eventType = ((Integer)assertSameResult("getEventType")).intValue();
            if (eventType == XMLStreamReader.START_ELEMENT) {
                path.addLast(expected.getName());
            }
            Integer attributeCount = (Integer)assertSameResult("getAttributeCount");
            // Test the behavior of the getAttributeXxx methods for all types of events,
            // to check that an appropriate exception is thrown for events other than
            // START_ELEMENT
            for (int i=0; i < (attributeCount == null ? 1 : attributeCount.intValue()); i++) {
                Class[] paramTypes = { Integer.TYPE };
                Object[] args = {i};
                assertSameResult("getAttributeLocalName", paramTypes, args);
                assertSameResult("getAttributeName", paramTypes, args);
                namespaceURIs.add(assertSameResult("getAttributeNamespace", paramTypes, args));
                prefixes.add(assertSameResult("getAttributePrefix", paramTypes, args, noPrefixValueMap));
                assertSameResult("getAttributeType", paramTypes, args);
                assertSameResult("getAttributeValue", paramTypes, args);
                assertSameResult("isAttributeSpecified", paramTypes, args);
            }
            assertSameResult("getLocalName");
            assertSameResult("getName");
            Integer namespaceCount = (Integer)assertSameResult("getNamespaceCount");
            if (namespaceCount != null) {
                Map expectedNamespaces = new HashMap();
                Map actualNamespaces = new HashMap();
                for (int i=0; i<namespaceCount.intValue(); i++) {
                    String expectedPrefix = expected.getNamespacePrefix(i);
                    String expectedNamespaceURI = expected.getNamespaceURI(i);
                    if (expectedNamespaceURI != null && expectedNamespaceURI.isEmpty()) {
                        expectedNamespaceURI = null;
                    }
                    String actualPrefix = actual.getNamespacePrefix(i);
                    String actualNamespaceURI = actual.getNamespaceURI(i);
                    if (actualNamespaceURI != null && actualNamespaceURI.isEmpty()) {
                        actualNamespaceURI = null;
                    }
                    expectedNamespaces.put(expectedPrefix, expectedNamespaceURI);
                    actualNamespaces.put(actualPrefix, actualNamespaceURI);
                    prefixes.add(expectedPrefix);
                    namespaceURIs.add(expectedNamespaceURI);
                }
                assertEquals(expectedNamespaces, actualNamespaces);
            }
            namespaceURIs.add(assertSameResult("getNamespaceURI"));
            assertSameResult("getPIData");
            assertSameResult("getPITarget");
            prefixes.add(assertSameResult("getPrefix"));
            assertSameResult("getText");
            Integer textLength = (Integer)assertSameResult("getTextLength");
            Object[] textStart = invoke("getTextStart");
            Object[] textCharacters = invoke("getTextCharacters");
            if (textLength != null) {
                assertEquals(new String((char[])textCharacters[0],
                                        ((Integer)textStart[0]).intValue(),
                                        textLength.intValue()),
                             new String((char[])textCharacters[1],
                                        ((Integer)textStart[1]).intValue(),
                                        textLength.intValue()));
            }
            assertSameResult("hasName");
            assertSameResult("hasText");
            assertSameResult("isCharacters");
            assertSameResult("isEndElement");
            assertSameResult("isStartElement");
            assertSameResult("isWhiteSpace");
            
            // Only check getNamespaceURI(String) for START_ELEMENT and END_ELEMENT. The Javadoc
            // of XMLStreamReader suggests that this method is valid for all states, but Woodstox
            // only allows it for some states.
            if (eventType == XMLStreamReader.START_ELEMENT ||
                    eventType == XMLStreamReader.END_ELEMENT) {
                for (Iterator it = prefixes.iterator(); it.hasNext(); ) {
                    String prefix = (String)it.next();
                    // The StAX specs are not clear about the expected result of getNamespaceURI
                    // when called with prefix "xml" (which doesn't require an explicit declaration)
                    if (prefix != null && !prefix.equals("xml")) {
                        assertSameResult("getNamespaceURI",
                                new Class[] { String.class }, new Object[] { prefix });
                    }
                }
            }
            
            compareNamespaceContexts(expected.getNamespaceContext(), actual.getNamespaceContext());
            
            if (eventType == XMLStreamReader.END_ELEMENT) {
                path.removeLast();
            }
            
            int expectedNextEvent;
            try {
                expectedNextEvent = expected.next();
            } catch (IllegalStateException ex) {
                expectedNextEvent = -1;
            } catch (NoSuchElementException ex) {
                expectedNextEvent = -1;
            }
            if (expectedNextEvent == -1) {
                try {
                    actual.next();
                } catch (IllegalStateException ex) {
                    break;
                } catch (NoSuchElementException ex) {
                    break;
                }
                fail("Expected reader to throw IllegalStateException or NoSuchElementException");
            } else {
                assertEquals(expectedNextEvent, actual.next());
            }
        };
    }
}
