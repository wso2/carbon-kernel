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

package org.apache.axis2.jaxws.utility;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.imageio.ImageIO;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Provides utilities to convert an object into a different kind of Object. For example, convert a
 * String[] into a List<String>
 */
public class ConvertUtils {

    private static final Log log = LogFactory.getLog(ConvertUtils.class);

    /**
     * This method should return true if the convert method will succeed.
     * <p/>
     * Note that any changes to isConvertable() must also be accompanied by similar changes to
     * convert()
     *
     * @param obj  source object or class
     * @param dest destination class
     * @return boolean true if convert(..) can convert obj to the destination class
     */
    public static boolean isConvertable(Object obj, Class dest) {
        Class src = null;

        if (obj != null) {
            if (obj instanceof Class) {
                src = (Class)obj;
            } else {
                src = obj.getClass();
            }
        }

        if (dest == null) {
            return false;
        }

        if (src == null) {
            return true;
        }

        // If we're directly assignable, we're good.
        if (dest.isAssignableFrom(src)) {
            return true;
        }

        // If it's a wrapping conversion, we're good.
        if (JavaUtils.getWrapperClass(src) == dest) {
            return true;
        }
        if (JavaUtils.getWrapperClass(dest) == src) {
            return true;
        }

        // If it's List -> Array or vice versa, we're good.
        if ((Collection.class.isAssignableFrom(src) || src.isArray()) &&
                (Collection.class.isAssignableFrom(dest) || dest.isArray())) {
            
            // TODO this should consider the component types instead of returning true.
            return true;
        }

        // Allow mapping of HashMaps to Hashtables
        if (src == HashMap.class && dest == Hashtable.class)
            return true;

        // Allow mapping of Calendar to Date
        if (Calendar.class.isAssignableFrom(src) && dest == Date.class) {
            return true;
        }

        if (src.isPrimitive()) {
            return isConvertable(JavaUtils.getWrapperClass(src), dest);
        }

        if (InputStream.class.isAssignableFrom(src) && dest == byte[].class) {
            return true;
         }
         
         if (Source.class.isAssignableFrom(src) && dest == byte[].class) {
             return true;
         }
         
         if (DataHandler.class.isAssignableFrom(src) && isConvertable(byte[].class, dest)) {
            return true;
        }

        if (DataHandler.class.isAssignableFrom(src) && dest == Image.class) {
            return true;
        }

        if (DataHandler.class.isAssignableFrom(src) && dest == Source.class) {
            return true;
        }

        if (byte[].class.isAssignableFrom(src) && dest == String.class) {
            return true;
        }
         
        // If it's a MIME type mapping and we want a DataHandler,
        // then we're good.
        // REVIEW Do we want to support this
        /*
        if (dest.getName().equals("javax.activation.DataHandler")) {
            String name = src.getName();
            if (src == String.class
                    || src == java.awt.Image.class
                    || name.equals("javax.mail.internet.MimeMultipart")
                    || name.equals("javax.xml.transform.Source"))
                return true;
        }
        */


        return false;
    }

    /**
     * Utility function to convert an Object to some desired Class.
     * <p/>
     * Normally this is used for T[] to List<T> processing. Other conversions are also done (i.e.
     * HashMap <->Hashtable, etc.)
     * <p/>
     * Use the isConvertable() method to determine if conversion is possible. Note that any changes
     * to convert() must also be accompanied by similar changes to isConvertable()
     *
     * @param arg       the array to convert
     * @param destClass the actual class we want
     * @return object of destClass if conversion possible, otherwise returns arg
     */
    public static Object convert(Object arg, Class destClass) throws WebServiceException {
        if (destClass == null) {
            return arg;
        }

        if (arg != null && destClass.isAssignableFrom(arg.getClass())) {
            return arg;
        }

        if (log.isDebugEnabled()) {
            String clsName = "null";
            if (arg != null) clsName = arg.getClass().getName();
            log.debug("Converting an object of type " + clsName + " to an object of type " +
                    destClass.getName());
        }

        // Convert between Calendar and Date
        if (arg instanceof Calendar && destClass == Date.class) {
            return ((Calendar)arg).getTime();
        }

        // Convert between HashMap and Hashtable
        if (arg instanceof HashMap && destClass == Hashtable.class) {
            return new Hashtable((HashMap)arg);
        }
        
        if (arg instanceof InputStream && destClass == byte[].class) {

            try {
                InputStream is = (InputStream) arg;
                return getBytesFromStream(is);
            } catch (IOException e) {
                throw ExceptionFactory.makeWebServiceException(e);
            }
        }

        if (arg instanceof Source && destClass == byte[].class) {
            try {
                if (arg instanceof StreamSource) {
                    InputStream is = ((StreamSource) arg).getInputStream();
                    if (is != null) {
                        return getBytesFromStream(is);
                    }
                }
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Result result = new StreamResult(out);
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.transform((Source) arg, result);
                byte[] bytes = out.toByteArray();
                return bytes;

            } catch (Exception e) {
                throw ExceptionFactory.makeWebServiceException(e);
            }
        }


        if (arg instanceof DataHandler) {
            try {
                InputStream is = ((DataHandler) arg).getInputStream();
                if (destClass == Image.class) {
                    return ImageIO.read(is);
                } else if (destClass == Source.class) {
                    return new StreamSource(is);
                }
                byte[] bytes = getBytesFromStream(is);
                return convert(bytes, destClass);
            } catch (Exception e) {
                throw ExceptionFactory.makeWebServiceException(e);
            }
        }

        if (arg instanceof byte[] && destClass == String.class) {
            return new String((byte[]) arg);
        }

        // If the destination is an array and the source
        // is a suitable component, return an array with 
        // the single item.
        /* REVIEW do we need to support atomic to array conversion ?
        if (arg != null &&
            destClass.isArray() &&
            !destClass.getComponentType().equals(Object.class) &&
            destClass.getComponentType().isAssignableFrom(arg.getClass())) {
            Object array = 
                Array.newInstance(destClass.getComponentType(), 1);
            Array.set(array, 0, arg);
            return array;
        }
        */

        // Return if no conversion is available
        if (!(arg instanceof Collection ||
                (arg != null && arg.getClass().isArray()))) {
            return arg;
        }

        if (arg == null) {
            return null;
        }

        // The arg may be an array or List 
        Object destValue = null;
        int length = 0;
        if (arg.getClass().isArray()) {
            length = Array.getLength(arg);
        } else {
            length = ((Collection)arg).size();
        }
        
        try {
            if (destClass.isArray()) {
                if (destClass.getComponentType().isPrimitive()) {

                    Object array = Array.newInstance(destClass.getComponentType(),
                                                     length);
                    // Assign array elements
                    if (arg.getClass().isArray()) {
                        for (int i = 0; i < length; i++) {
                            Array.set(array, i, Array.get(arg, i));
                        }
                    } else {
                        int idx = 0;
                        for (Iterator i = ((Collection)arg).iterator();
                             i.hasNext();) {
                            Array.set(array, idx++, i.next());
                        }
                    }
                    destValue = array;

                } else {
                    Object [] array;
                    try {
                        array = (Object [])Array.newInstance(destClass.getComponentType(),
                                                             length);
                    } catch (Exception e) {
                        return arg;
                    }

                    // Use convert to assign array elements.
                    if (arg.getClass().isArray()) {
                        for (int i = 0; i < length; i++) {
                            array[i] = convert(Array.get(arg, i),
                                               destClass.getComponentType());
                        }
                    } else {
                        int idx = 0;
                        for (Iterator i = ((Collection)arg).iterator();
                             i.hasNext();) {
                            array[idx++] = convert(i.next(),
                                                   destClass.getComponentType());
                        }
                    }
                    destValue = array;
                }
            } else if (Collection.class.isAssignableFrom(destClass)) {
                Collection newList = null;
                try {
                    // if we are trying to create an interface, build something
                    // that implements the interface
                    if (destClass == Collection.class || destClass == List.class) {
                        newList = new ArrayList();
                    } else if (destClass == Set.class) {
                        newList = new HashSet();
                    } else {
                        newList = (Collection)destClass.newInstance();
                    }
                } catch (Exception e) {
                    // No FFDC code needed
                    // Couldn't build one for some reason... so forget it.
                    return arg;
                }

                if (arg.getClass().isArray()) {
                    for (int j = 0; j < length; j++) {
                        newList.add(Array.get(arg, j));
                    }
                } else {
                    for (Iterator j = ((Collection)arg).iterator();
                         j.hasNext();) {
                        newList.add(j.next());
                    }
                }
                destValue = newList;
            } else {
                destValue = arg;
            }
        } catch (Throwable t) {
            throw ExceptionFactory.
              makeWebServiceException( 
                                    Messages.getMessage("convertUtils", 
                                                        arg.getClass().toString(), 
                                                        destClass.toString()),
                                    t);                        
        }
        
        return destValue;
    }
    
    private static byte[] getBytesFromStream(InputStream is) throws IOException {
        // TODO This code assumes that available is the length of the stream.
        byte[] bytes = new byte[is.available()];
        is.read(bytes);
        return bytes;
    }
}
