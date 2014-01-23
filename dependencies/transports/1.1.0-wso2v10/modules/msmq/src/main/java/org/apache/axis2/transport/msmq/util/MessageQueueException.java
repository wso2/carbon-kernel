/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * 
 */
package org.apache.axis2.transport.msmq.util;

/**
 * An exception type to wrap any problems that occur during MSMQ operations.
 *
 **/
public class MessageQueueException extends java.lang.Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
	public int hresult;

    public MessageQueueException(int HRESULT) {
        super();
        hresult= HRESULT;
    }

    public MessageQueueException(String message, int HRESULT) {
        super(message);
        hresult= HRESULT;
    }

    /**
     * <p>Produce the string version for the given exception, including the
     * string mnemonic for the HR wrapped by the exception.</p>
     *
     * <p>Example:</p>
     *
     * <blockquote class='code'><pre>
     *   try {
     *       String label="testmessage";
     *       String body= "....";
     *       Message msg= new Message(body, label, null);
     *       queue.send(msg);
     *   }
     *   catch (MessageQueueException ex1) {
     *       System.out.println("Put failure: " + ex1.toString());
     *   }
     * </pre></blockquote>
     *
     */
    public String toString() {
        String msg = getLocalizedMessage();
        String hr= "hr=" + HrToString(hresult) ;
        return msg + " (" + hr + ")";
    }


    private static String HrToString(int hr) {
        if (hr== 0)
            return "SUCCESS";
        if (hr==0xC00E0002)
            return "MQ_ERROR_PROPERTY";
        if (hr== 0xC00E0003)
            return "MQ_ERROR_QUEUE_NOT_FOUND";
        if (hr==0xC00E0005)
            return "MQ_ERROR_QUEUE_EXISTS";
        if (hr==0xC00E0006)
            return "MQ_ERROR_INVALID_PARAMETER";
        if (hr==0xC00E0007)
            return "MQ_ERROR_INVALID_HANDLE";
        if (hr==0xC00E005A)
            return "MQ_ERROR_QUEUE_DELETED";
        if (hr==0xC00E000B)
            return "MQ_ERROR_SERVICE_NOT_AVAILABLE";
        if (hr==0xC00E001B)
            return "MQ_ERROR_IO_TIMEOUT";
        if (hr==0xC00E001E)
            return "MQ_ERROR_ILLEGAL_FORMATNAME";
        if (hr== 0xC00E0025)
            return "MQ_ERROR_ACCESS_DENIED";
        if (hr==0xC00E0013)
            return "MQ_ERROR_NO_DS";
        if (hr==0xC00E003F)
            return "MQ_ERROR_INSUFFICIENT_PROPERTIES";
        if (hr==0xC00E0014)
            return "MQ_ERROR_ILLEGAL_QUEUE_PATHNAME";
        if (hr==0xC00E0044)
            return "MQ_ERROR_INVALID_OWNER";
        if (hr==0xC00E0045)
            return "MQ_ERROR_UNSUPPORTED_ACCESS_MODE";
        if (hr== 0xC00E0069)
            return "MQ_ERROR_REMOTE_MACHINE_NOT_AVAILABLE";

        return "unknown hr (" + hr + ")";
    }
}
