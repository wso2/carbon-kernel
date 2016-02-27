/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.wso2.carbon.jndi.internal.impl;

import java.util.Iterator;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * Naming enumeration implementation.
 **/
public class NamingContextBindingsEnumeration implements NamingEnumeration<Binding> {

    /**
     * Underlying enumeration.
     */
    protected final Iterator<NamingEntry> iterator;

    /**
     * The context for which this enumeration is being generated.
     */
    private final Context context;

    /**
     * @param entries set of bindings.
     * @param context Context on which bindings are enumerated.
     */
    public NamingContextBindingsEnumeration(Iterator<NamingEntry> entries, Context context) {
        iterator = entries;
        this.context = context;
    }

    @Override
    public Binding next() throws NamingException {
        return nextElementInternal();
    }

    @Override
    public boolean hasMore() throws NamingException {
        return iterator.hasNext();
    }

    @Override
    public void close() throws NamingException {
    }

    @Override
    public boolean hasMoreElements() {
        return iterator.hasNext();
    }

    @Override
    public Binding nextElement() {
        try {
            return nextElementInternal();
        } catch (NamingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Retrieves the next element in the enumeration.
     *
     * @return the next element in the enumeration.
     * @throws NamingException
     */
    private Binding nextElementInternal() throws NamingException {
        NamingEntry entry = iterator.next();
        Object value;

        // If the entry is a reference, resolve it
        if (entry.type == NamingEntry.REFERENCE || entry.type == NamingEntry.LINK_REF) {

            try {
                value = context.lookup(new CompositeName(entry.name));
            } catch (NamingException e) {
                throw e;
            } catch (Exception e) {
                NamingException namingException = new NamingException(e.getMessage());
                namingException.initCause(e);
                throw namingException;
            }
        } else {
            value = entry.value;
        }

        return new Binding(entry.name, value.getClass().getName(), value, true);
    }
}

