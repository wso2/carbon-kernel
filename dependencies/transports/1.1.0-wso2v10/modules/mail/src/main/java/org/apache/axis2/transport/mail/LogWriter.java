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

package org.apache.axis2.transport.mail;

import java.io.Writer;

import org.apache.commons.logging.Log;

/**
 * {@link Writer} implementation that redirects to a logger.
 */
public class LogWriter extends Writer {
	private final Log log;
	private final String endOfLine;
	private final StringBuffer lineBuffer = new StringBuffer();
	private int endOfLineMatch;
	
	public LogWriter(Log log, String endOfLine) {
		this.log = log;
		this.endOfLine = endOfLine;
	}

	public LogWriter(Log log) {
		this(log, System.getProperty("line.separator"));
	}
	
	@Override
	public void write(char[] cbuf, int off, int len) {
		int start = off;
		for (int i=off; i<off+len; i++) {
			if (cbuf[i] == endOfLine.charAt(endOfLineMatch)) {
				endOfLineMatch++;
				if (endOfLineMatch == endOfLine.length()) {
					lineBuffer.append(cbuf, start, i-start+1);
					lineBuffer.setLength(lineBuffer.length()-endOfLine.length());
					flushLineBuffer();
					start = i+1;
					endOfLineMatch = 0;
				}
			} else {
				endOfLineMatch = 0;
			}
		}
		lineBuffer.append(cbuf, start, off+len-start);
	}

	@Override
	public void close() {
		if (lineBuffer.length() > 0) {
			flushLineBuffer();
		}
	}
	
	@Override
	public void flush() {
		// Nothing to do
	}

	private void flushLineBuffer() {
		log.info(lineBuffer.toString());
		lineBuffer.setLength(0);
	}
}
