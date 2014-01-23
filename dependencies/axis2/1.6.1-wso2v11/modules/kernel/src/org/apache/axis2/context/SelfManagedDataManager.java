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

package org.apache.axis2.context;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * An interface for use by a message handler to allow
 * it to save and restore any message-specific data.
 * <p/>
 * A handler can have message-specific data that
 * needs to be associated the message being processed.
 * The handler can keep this message-specific data
 * in the Messagecontext object by adding the
 * data to the user data table via the
 * MessageContext method.  When the MessageContext
 * object is saved (for example, to persistent storage),
 * and restored, this interface <B>SelfManagedDataManager</B>
 * provides a way for the handler to save and restore
 * the handler's message-specific data.
 *
 * @see MessageContext
 */
public interface SelfManagedDataManager {

    /**
     * This method is invoked when the MessageContext object is being saved.
     * <p/>
     * Implementors are expected to iterate through the data objects they wish
     * to save and return it in a ByteArrayOutputStream.  This data will
     * later be passed to the implementor's deserializeSelfManagedData method when
     * the data object is to be restored.
     * <p/>
     * The data being saved may be the data stored by the implementor
     * in the MessageContext object's SelfManagedData list and may include
     * additional information such as the implementor's fields.
     * Note that data stored by the implementor in the MessageContext object's SelfManagedData
     * list is retrievable by calling the MessageContext.getSelfManagedData() method
     * for EACH key/value pair.
     *
     * @param mc the MessageContext that is being saved
     * @return The saved data in the output stream. Note that the
     *         format of the data in the output stream is defined
     *         by the implementor.
     * @throws IOException
     */
    public ByteArrayOutputStream serializeSelfManagedData(MessageContext mc) throws IOException;

    /**
     * This method is invoked when the MessageContext object is being restored.
     * <p/>
     * Implementors will receive the data they had previously saved in the
     * serializeSelfManagedData() method. Implementors are responsible for
     * reconstituting their message-specific data.
     *
     * @param data ByteArrayInputStream consisting of the data that this handler had previously saved
     * @param mc   the MessageContext object being restored
     * @throws IOException
     */
    public void deserializeSelfManagedData(ByteArrayInputStream data, MessageContext mc)
            throws IOException;


    /**
     * This method is invoked when the MessageContext object is being restored and
     * after the deserializeSelfManagedData() method.
     * <p/>
     * Implementors use this method to re-establish transient message-specific data,
     * particularly if the implementor did not save any user data in the
     * serializeSelfManagedData() method.
     *
     * @param mc the MessageContext object being restored
     */
    public void restoreTransientData(MessageContext mc);

}
