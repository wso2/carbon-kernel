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
package org.apache.axiom.om.util;

import javax.xml.stream.XMLStreamWriter;

/**
 * An interface used to identify a filter class for an XMLStreamWriter
 * The filter receives XMLStreamWriter events (and can change or log them).
 * The filter then sends the events to the delegate XMLStreamWriter
 * @see XMLStreamWriterFilterBase
 */
public interface XMLStreamWriterFilter extends XMLStreamWriter {
	
	/**
	 * Set a new delegate writer
	 * @param writer
	 */
	public void setDelegate(XMLStreamWriter writer);
	
	/**
	 * @return XMLStreamWriter delegate
	 */
	public XMLStreamWriter getDelegate();
}
