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

/**
 * Base classes for datagram transports.
 * <p>
 * A datagram type transport is a transport that entirely reads a message
 * into memory before starting to process it: in contrast to transports like HTTP,
 * it doesn't support streaming. This approach can be chosen either because
 * of the characteristics of the underlying protocol (such as in the case of UDP)
 * or because streaming a message would unnecessarily delay the processing of the
 * next available message (as in the case of a UNIX pipe).
 */
package org.apache.axis2.transport.base.datagram;
