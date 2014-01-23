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

package org.apache.axis2.databinding.utils.reader;

import org.apache.axis2.util.ArrayStack;

import javax.xml.namespace.NamespaceContext;
import java.util.ArrayList;
import java.util.Iterator;

public class ADBNamespaceContext implements NamespaceContext {

    private NamespaceContext parentNsContext;

    public NamespaceContext getParentNsContext() {
        return parentNsContext;
    }

    public void setParentNsContext(NamespaceContext parentNsContext) {
        this.parentNsContext = parentNsContext;
    }

    //Keep two arraylists for the prefixes and namespaces. They should be in sync
    //since the index of the entry will be used to relate them
    //use the minimum initial capacity to let things handle memory better

    private ArrayStack prefixStack = new ArrayStack();
    private ArrayStack uriStack = new ArrayStack();

    /**
     * Register a namespace in this context
     *
     * @param prefix
     * @param uri
     */
    public void pushNamespace(String prefix, String uri) {
        prefixStack.push(prefix);
        uriStack.push(uri);

    }

    /** Pop a namespace */
    public void popNamespace() {
        prefixStack.pop();
        uriStack.pop();
    }

    public String getNamespaceURI(String prefix) {
        //do the corrections as per the javadoc
        if (prefixStack.contains(prefix)) {
            int index = prefixStack.indexOf(prefix);
            return (String)uriStack.get(index);
        }
        if (parentNsContext != null) {
            return parentNsContext.getPrefix(prefix);
        }
        return null;
    }

    public String getPrefix(String uri) {
        //do the corrections as per the javadoc
        int index = uriStack.indexOf(uri);
        if (index != -1) {
            return (String)prefixStack.get(index);
        }

        if (parentNsContext != null) {
            return parentNsContext.getPrefix(uri);
        }
        return null;
    }

    public Iterator getPrefixes(String uri) {
        //create an arraylist that contains the relevant prefixes
        String[] uris = (String[])uriStack.toArray(new String[uriStack.size()]);
        ArrayList tempList = new ArrayList();
        for (int i = 0; i < uris.length; i++) {
            if (uris[i].equals(uri)) {
                tempList.add(prefixStack.get(i));
                //we assume that array conversion preserves the order
            }
        }
        //by now all the relevant prefixes are collected
        //make a new iterator and provide a wrapper iterator to
        //obey the contract on the API
        return new WrappingIterator(tempList.iterator());
    }


    private class WrappingIterator implements Iterator {

        private Iterator containedIterator = null;

        public WrappingIterator(Iterator containedIterator) {
            this.containedIterator = containedIterator;
        }

        public Iterator getContainedIterator() {
            return containedIterator;
        }

        public void setContainedIterator(Iterator containedIterator) {
            this.containedIterator = containedIterator;
        }

        /**
         * As per the contract on the API of Namespace context the returned iterator should be
         * immutable
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }

        public boolean hasNext() {
            return containedIterator.hasNext();
        }

        public Object next() {
            return containedIterator.next();
        }
    }
}
