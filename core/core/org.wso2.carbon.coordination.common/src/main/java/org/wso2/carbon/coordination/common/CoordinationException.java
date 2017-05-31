/**
 *  Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.coordination.common;

/**
 * Exception class to represent error conditions in coordination activities.
 */
public class CoordinationException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public String code;
	
	public CoordinationException(String code, Exception e) {
		this(null, code, e);
	}
	
	public CoordinationException(String msg, String code, Exception e) {
		super(msg, e);
		this.code = code;
	}
	
	public CoordinationException(String code) {
		this(null, code, null);
	}
	
	public CoordinationException(String msg, String code) {
		this(msg, code, null);
	}
	
	public static final class ExceptionCode {
		
		public static String COORDINATION_SERVICE_NOT_ENABLED = "COORDINATION_SERVICE_NOT_ENABLED";
		public static String GENERIC_ERROR = "GENERIC_ERROR";
		public static String IO_ERROR = "IO_ERROR";
		public static String CONFIGURATION_ERROR = "CONFIGURATION_ERROR";
		public static String WAIT_TIMEOUT = "WAIT_TIMEOUT";
		
	}
	
	public String getCode() {
		return code;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		String code = this.getCode();
		builder.append("\n");
		if (code != null) {
			builder.append("ExceptionCode: " + code + "\n");			
		}
		String msg = this.getMessage();
		if (msg != null) {
			builder.append("Message: " + msg + "\n");
		}
		Throwable ex = this.getCause();
		if (ex != null) {
			builder.append("Nested Exception: " + ex.getMessage() + "\n");
		}
		return builder.toString();
	}
	
}
