/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.axis2.format;

import javax.activation.DataSource;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.MessageContext;

/**
 * Message builder able to build messages from {@link DataSource} objects.
 * This interface can be optionally implemented by {@link Builder}
 * implementations that support building messages from {@link DataSource} objects.
 * Since by definition the data from a {@link DataSource} can be read multiple
 * times, this interface can be used by message builders to avoid storing the
 * message content in memory.
 * <p>
 * If a message builder implements this interface and the transport is able to
 * provide the message payload as a data source, then the method defined by this
 * interface should be preferred over the method defined by {@link Builder}.
 * <p>
 * Implementing this interface helps optimizing message processing with transports
 * that use messaging providers that store messages in memory or on the file system.
 * Examples are JMS and VFS.
 * <p>
 * The builder will typically expose the data source directly or indirectly through
 * the returned {@link OMElement}, e.g. by adding to the tree an {@link org.apache.axiom.om.OMText}
 * or {@link org.apache.axiom.om.OMDataSource} node referencing the data source.
 * This means that the builder will not be able to guarantee that all streams requested
 * from the data source are properly closed. Note that code accessing the returned
 * {@link OMElement} can't be expected to take care of this since in many cases the fact
 * that a data source is being used is completely transparent to that code.
 * It is therefore the responsibility of the transport to make sure that all resources linked to
 * the data source itself as well as any open stream requested from that data source are properly
 * released after the message has been processed. Depending on the type of transport, there are
 * three possible cases:
 * <ol>
 *   <li>All resources allocated to the data source or streams requested from it are
 *       memory based. In that case the garbage collector will take care of freeing
 *       these resources and the transport should simply pass the data source object
 *       to the builder.</li>
 *   <li>There are operation system resources linked to the data source and open
 *       streams will become invalid when these resources are freed, i.e.
 *       it is not required that all streams be closed explicitly.
 *       In this case the transport only needs to take care to properly dispose of
 *       the data source after the message has been processed by the Axis2 engine.</li>
 *   <li>Requesting a stream from the data source allocates operation system resources
 *       (e.g. a network connection) that remain linked to the stream, i.e. all streams requested
 *       from the data source must be closed properly. In that case the transport should use
 *       {@link ManagedDataSourceFactory#create(DataSource)} to wrap the original data source
 *       before passing it to the builder. After the message has been processed it should
 *       then call {@link ManagedDataSource#destroy()} on the wrapper to close all remaining
 *       open streams.</li>
 * </ol>
 */
public interface DataSourceMessageBuilder extends Builder {
    public OMElement processDocument(DataSource dataSource, String contentType,
            MessageContext messageContext) throws AxisFault;
}
