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

package org.apache.axis2.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * Tidies up the java source code using Jalopy.
 * This is used by both ADB and Codegen hence needs to be in the
 * commons rather than a specific module
 */
public class PrettyPrinter {
    private static final Log log = LogFactory.getLog(PrettyPrinter.class);


    /**
     * Pretty prints contents of the java source file.
     *
     * @param file
     */
    public static void prettify(File file) {
        // If the user has set "axis2.jalopy=false" on the system property,
        // then just return back to caller
        String property = System.getProperty("axis2.jalopy");
        if((property == null) || !JavaUtils.isTrueExplicitly(property)){
            return;
        }
        PrintStream backupOutputStream = System.out;
        PrintStream backupErrorStream = System.err;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        System.setErr(new PrintStream(new ByteArrayOutputStream()));
        try {
            Class clazzConfigurator = Loader.loadClass("org.apache.log4j.PropertyConfigurator");
            Method configure = clazzConfigurator.getMethod("configure", new Class[]{Properties.class});
            Properties properties = new Properties();
            properties.setProperty("log4j.logger.de.hunsicker.jalopy.io",
                    System.getProperty("log4j.logger.de.hunsicker.jalopy.io", "FATAL"));
            configure.invoke(null, new Object[]{properties});

            // Create an instance of the Jalopy bean
            Class clazz = Loader.loadClass("de.hunsicker.jalopy.Jalopy");
            Object prettifier = clazz.newInstance();

            // Set the input file
            Method input = clazz.getMethod("setInput", new Class[]{File.class});
            input.invoke(prettifier, new Object[]{file});

            // Set the output file
            Method output = clazz.getMethod("setOutput", new Class[]{File.class});
            output.invoke(prettifier, new Object[]{file});

            Class clazz2 = Loader.loadClass("de.hunsicker.jalopy.storage.Convention");
            Method instance = clazz2.getMethod("getInstance", new Class[]{});
            Object settings = instance.invoke(null, new Object[]{});

            Class clazz3 = Loader.loadClass("de.hunsicker.jalopy.storage.ConventionKeys");
            Field field = clazz3.getField("COMMENT_FORMAT_MULTI_LINE");
            Object key = field.get(null);
            Method put = clazz2.getMethod("put", new Class[]{key.getClass(), String.class});
            put.invoke(settings, new Object[]{key, "true"});

            // format and overwrite the given input file
            Method format = clazz.getMethod("format", new Class[]{});
            format.invoke(prettifier, new Object[]{});
            log.debug("Pretty printed file : " + file);
        } catch (ClassNotFoundException e) {
            log.debug("Jalopy/Log4j not found - unable to pretty print " + file);
        } catch (Exception e) {
            log.warn("Exception occurred while trying to pretty print file " + file, e);
        } catch (Throwable t) {
            log.debug("Exception occurred while trying to pretty print file " + file, t);
        } finally {
            System.setOut(backupOutputStream);
            System.setErr(backupErrorStream);
        }
    }
}
