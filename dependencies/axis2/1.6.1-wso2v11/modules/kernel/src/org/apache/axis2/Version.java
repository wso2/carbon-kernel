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

package org.apache.axis2;

import org.apache.axis2.i18n.Messages;

/**
 * Little utility to get the version and build date of the axis2.jar.
 * <p/>
 * The messages referenced here are automatically kept up-to-date by the
 * build.xml.
 */
public class Version {
    /**
     * Get the version of this AXIS.
     *
     * @return the version of this axis2
     */
    public static String getVersion() {
        return Messages.getMessage("axisVersion") + "\n" +
                Messages.getMessage("builtOn");
    }

    /**
     * Returns the Axis Version number and build date.
     * <p/>
     * Example output: 1.1 Jul 08, 2003 (09:00:12 EDT)
     *
     * @return the full version of this axis2
     */
    public static String getVersionText() {
        return Messages.getMessage("axisVersionRaw");
    }

    /**
     * Entry point.
     * <p/>
     * Calling this with no arguments returns the version of the client-side
     * axis2.jar.
     */
    public static void main(String[] args) {
        System.out.println(getVersion());
    }
}
