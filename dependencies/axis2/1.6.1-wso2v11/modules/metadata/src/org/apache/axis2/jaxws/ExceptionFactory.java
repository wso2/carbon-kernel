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

package org.apache.axis2.jaxws;

import org.apache.axiom.om.OMException;
import org.apache.axis2.AxisFault;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.ws.ProtocolException;
import javax.xml.ws.WebServiceException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

/**
 * ExceptionFactory is used to create exceptions within the JAX-WS implementation. There are several
 * reasons for using a factory to create exceptions. 1. We can intercept all exception creation and
 * add the appropriate logging/serviceability. 2. Exceptions are chained.  ExceptionFactory can
 * lengthen or reduce the cause chains as necessary to support the JAX-WS programming model. 3.
 * Prevents construction of the same exception.  Uses similar principles as
 * AxisFault.makeException.
 * <p/>
 * Example Usage: // Example usage
 * <p/>
 * public fooMethod() throws WebServiceException { try{ ... } catch(Exception e){ throw
 * ExceptionFactory.makeWebServiceException(e); } }
 */
public class ExceptionFactory {

    protected static Log log =
            LogFactory.getLog(ExceptionFactory.class.getName());

    /** Private Constructor All methods are static.  The private constructor prevents instantiation. */
    private ExceptionFactory() {
    }

    /**
     * Create a WebServiceException using the information from a given Throwable instance and message
     *
     * @param message
     * @param throwable
     * @return WebServiceException
     */
    public static WebServiceException makeWebServiceException(String message, Throwable throwable) {
        try {
            // See if there is already a WebServiceException (Note that the returned exception could be a ProtocolException or
            // other kind of exception)
            WebServiceException e =
                    (WebServiceException)findException(throwable, WebServiceException.class);
            if (e == null) {
                e = createWebServiceException(message, throwable);
            }
            return e;
        } catch (RuntimeException re) {
            // TODO
            // This is not a good situation, an exception occurred while building the exception.
            // This should never occur!  For now log the problem and rethrow...we may revisit this later
            if (log.isDebugEnabled()) {
                log.debug(Messages.getMessage("exceptionDuringExceptionFlow"), re);
            }
            throw re;
        }
    }

    /**
     * Create a ProtocolException using the information from a Throwable and message
     *
     * @param message
     * @param throwable
     * @return ProtocolException
     */
    public static ProtocolException makeProtocolException(String message, Throwable throwable) {
        try {
            // See if there is already a ProtocolException
            ProtocolException e =
                    (ProtocolException)findException(throwable, ProtocolException.class);
            if (e == null) {
                e = createProtocolException(message, throwable);
            }
            return e;
        } catch (RuntimeException re) {
            // TODO
            // This is not a good situation, an exception occurred while building the exception.
            // This should never occur!  For now log the problem and rethrow...we may revisit this later
            if (log.isDebugEnabled()) {
                log.debug(Messages.getMessage("exceptionDuringExceptionFlow"), re);
            }
            throw re;
        }
    }

    /**
     * Make a WebServiceException with a given message
     *
     * @param message
     * @return WebServiceException
     */
    public static WebServiceException makeWebServiceException(String message) {
        return makeWebServiceException(message, null);
    }

    /**
     * Create a WebServiceException using the information from a given Throwable instance
     *
     * @param throwable
     * @return WebServiceException
     */
    public static WebServiceException makeWebServiceException(Throwable throwable) {
        return makeWebServiceException(null, throwable);
    }


    /**
     * Create a WebServiceException
     *
     * @param message
     * @param t       Throwable
     * @return WebServiceException
     */
    private static WebServiceException createWebServiceException(String message, Throwable t) {

        // We might have an embedded WebServiceException that has a good message on it
        WebServiceException me = (WebServiceException)findException(t, WebServiceException.class);
        if (me != null) {
            String meMessage = me.getMessage();
            if (meMessage != null) {
                if (message == null) {
                    message = meMessage;
                } else {
                    message = message + ": " + meMessage;
                }
            }
        }

        // Get the root cause.  We want to strip out the intermediate exceptions (like AxisFault) because
        // these won't make sense to the user.
        Throwable rootCause = null;
        if (t != null) {
            rootCause = getRootCause(t);
        }
        rootCause = rootCause == null ? t : rootCause;
        WebServiceException e = null;

        // The root cause may not have a good message.  We might want to enhance it
        String enhancedMessage = enhanceMessage(rootCause);
        if (enhancedMessage != null) {
            if (message != null)
                message = message + ": " + enhancedMessage;
            else
                message = enhancedMessage;
        }

        if (message != null) {
            e = new WebServiceException(message, rootCause);
        } else {
            e = new WebServiceException(rootCause);
        }

        if (log.isDebugEnabled()) {
            log.debug("Create Exception:", e);
        }
        return e;
    }

    /**
     * Create a ProtocolException
     *
     * @param message
     * @param t       Throwable
     * @return ProtocolException
     */
    private static ProtocolException createProtocolException(String message, Throwable t) {
        Throwable rootCause = null;
        if (t != null) {
            rootCause = getRootCause(t);
        }
        rootCause = rootCause == null ? t : rootCause;
        ProtocolException e = null;
        if (message != null) {
            e = new ProtocolException(message, rootCause);
        } else {
            e = new ProtocolException(rootCause);
        }
        if (log.isDebugEnabled()) {
            log.debug("create Exception:", e);
        }
        return e;
    }

    /**
     * Return the exception or nested cause that is assignable from the specified class
     *
     * @param t   Throwable
     * @param cls
     * @return Exception or null
     */
    private static Exception findException(Throwable t, Class cls) {
        while (t != null) {
            if (cls.isAssignableFrom(t.getClass())) {
                return (Exception)t;
            }
            t = getCause(t);
        }
        return null;
    }

    /**
     * Gets the Throwable cause of the Exception.  Some exceptions store the cause in a different
     * field, which is why this method should be used when walking the causes.
     *
     * @param t Throwable
     * @return Throwable or null
     */
    private static Throwable getCause(Throwable t) {
        Throwable cause = null;

        // Look for a specific cause for this kind of exception
        if (t instanceof InvocationTargetException) {
            cause = ((InvocationTargetException)t).getTargetException();
        }

        // If no specific cause, fall back to the general cause.
        if (cause == null) {
            cause = t.getCause();
        }
        return cause;
    }

    /**
     * This method searches the causes of the specified throwable until it finds one that is
     * acceptable as a "root" cause.
     * <p/>
     * Example: If t is an AxisFault, the code traverses to the next cause.
     *
     * @param t Throwable
     * @return Throwable root cause
     */
    private static Throwable getRootCause(Throwable t) {
        while (t != null) {
            Throwable nextCause = null;
            if (t instanceof InvocationTargetException ||
                    t instanceof OMException ||
                    t instanceof AxisFault) {
                // Skip over this cause
                nextCause = getCause(t);
                if (nextCause == null) {
                    logRootCause(t);
                    return t;
                }
                t = nextCause;
            } else {
                // This is the root cause
                logRootCause(t);
                return t;
            }
        }
        logRootCause(t);
        return t;
    }

    /**
     * Other developers may add additional criteria to give better error messages back to the user.
     *
     * @param t Throwable
     * @return String a message that helps the user understand what caused the exception
     */
    private static String enhanceMessage(Throwable t) {
        if (t == null)
            return null;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true);
        t.printStackTrace(ps);
        String stackTrace = baos.toString();

        // TODO better criteria
        if ((t instanceof StackOverflowError) && (stackTrace.contains("JAXB")))
            return Messages.getMessage("JABGraphProblem");

        return null;
    }
    /**
     * Log the root cause
     * @param t
     */
    private static void logRootCause(Throwable t) {
        // TODO Should certain throwables be logged elsewhere
        if (log.isDebugEnabled()) {
            log.debug("Root Cause:" +  t.toString());
            log.debug("stack:" + stackToString(t));
        }
    }
    
    /**
     * Get a string containing the stack of the specified exception
     * @param e
     * @return
     */
    public static String stackToString(Throwable e) {
        java.io.StringWriter sw= new java.io.StringWriter(); 
        java.io.BufferedWriter bw = new java.io.BufferedWriter(sw);
        java.io.PrintWriter pw= new java.io.PrintWriter(bw); 
        e.printStackTrace(pw);
        pw.close();
        return sw.getBuffer().toString();
    }

}

