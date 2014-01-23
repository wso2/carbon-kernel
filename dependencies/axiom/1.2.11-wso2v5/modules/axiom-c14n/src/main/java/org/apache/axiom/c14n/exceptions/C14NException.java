
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

package org.apache.axiom.c14n.exceptions;

import org.apache.axiom.c14n.utils.PropLoader;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.MessageFormat;

/**
 * class C14NException is the base class of all the exception classes
 * used in org.apache.axiom.c14n
 *
 * @author Saliya Ekanayake (esaliya@gmail.com)
 */
public class C14NException extends Exception {
    public static final String baseName = "exceptions";
    protected Exception e = null;

    protected String msgID;

    public C14NException() {
        super("Missing message ID");
        this.e = null;
        this.msgID = null;
    }

    public C14NException(String msgID) {
        super(PropLoader.getExceptionProperty(msgID, baseName));
        this.e = null;
        this.msgID = msgID;
    }

    public C14NException(String msgID, Object[] exArgs) {
        super(MessageFormat.format(
                PropLoader.getExceptionProperty(msgID, baseName), exArgs));
        this.e = null;
        this.msgID = msgID;
    }

    public C14NException(Exception e) {
        super(e.getMessage());
        this.e = e;
        this.msgID = null;
    }

    public C14NException(String msgID, Exception e) {
        super(MessageFormat.format(
                PropLoader.getExceptionProperty(msgID, baseName), new Object[]{e.getMessage()}));
        this.e = e;
        this.msgID = msgID;
    }

    public C14NException(String msgID, Object [] exArgs, Exception e) {
        super(MessageFormat.format(
                PropLoader.getExceptionProperty(msgID, baseName), exArgs));
        this.e = e;
        this.msgID = msgID;
    }

    public String getMsgID() {
        if (msgID == null) {
            return "Missing message ID";
        }
        return msgID;
    }

    /**
     * @inheritDoc
     */
    public String toString() {

        String s = this.getClass().getName();
        String message = super.getLocalizedMessage();

        if (message != null) {
            message = s + ": " + message;
        } else {
            message = s;
        }

        if (e != null) {
            message = message + "\nOriginal Exception was "
                    + e.toString();
        }
        return message;
    }

    public void printStackTrace() {
        synchronized (System.err) {
            super.printStackTrace(System.err);
            if (this.e != null) {
                this.e.printStackTrace(System.err);
            }
        }
    }

    public void printStackTrace(PrintWriter printwriter) {
        super.printStackTrace(printwriter);
        if (this.e != null) {
            this.e.printStackTrace(printwriter);
        }
    }

    public void printStackTrace(PrintStream printstream) {
        super.printStackTrace(printstream);
        if (this.e != null) {
            this.e.printStackTrace(printstream);
        }
    }

    public Exception getOriginalException() {
        return e;
    }
}


