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

package org.apache.axis2.description;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Arrays;

/**
 * Class representing a version number and implementing a comparison algorithm compatible
 * with Maven. This class is primarily used for module versions.
 */
public class Version implements Serializable, Comparable<Version> {
    private static final String S_SNAPSHOT = "SNAPSHOT";
    
    public static final Version SNAPSHOT = new Version(null, S_SNAPSHOT);
    
    private final int[] components;
    private final String qualifier;
    
    /**
     * Constructor.
     * 
     * @param components the numeric components of the version; may be null for SNAPSHOT version
     * @param qualifier the qualifier
     */
    public Version(int[] components, String qualifier) {
        this.components = components == null ? null : components.clone();
        this.qualifier = qualifier;
    }
    
    /**
     * Constructor that parses the version from a string. The version must have the
     * following format:
     * <pre>
     * integer ( "." integer )* ( "-" qualifier )?
     * </pre>
     * 
     * @param versionString the string representation of the version
     * @throws ParseException if the version is not in the correct format
     */
    public Version(String versionString) throws ParseException {
        if (versionString.equals(S_SNAPSHOT)) {
            components = null;
            qualifier = S_SNAPSHOT;
        } else {
            int dashIndex = versionString.indexOf('-');
            if (dashIndex != -1) {
                qualifier = versionString.substring(dashIndex + 1);
                versionString = versionString.substring(0, dashIndex);
            } else if (versionString.endsWith(".SNAPSHOT")){
                qualifier = "SNAPSHOT";
                versionString = versionString.substring(0,versionString.indexOf(".SNAPSHOT"));
            } else {
                qualifier = null;
            }
            String[] componentStrings = versionString.split("\\.");
            int l = componentStrings.length;
            components = new int[l];
            for (int i=0; i<l; i++) {
                components[i] = Integer.parseInt(componentStrings[i]);
            }
        }
    }
    
    @Override
    public int hashCode() {
        return 31*Arrays.hashCode(components) + ((qualifier == null) ? 0 : qualifier.hashCode());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Version) {
            Version other = (Version)obj;
            return Arrays.equals(components, other.components)
                && (qualifier == other.qualifier ||
                        qualifier != null && qualifier.equals(other.qualifier));
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        if (components != null) {
            for (int i=0; i<components.length; i++) {
                if (i>0) {
                    buffer.append('.');
                }
                buffer.append(components[i]);
            }
        }
        if (qualifier != null) {
            if (components != null) {
                buffer.append('-');
            }
            buffer.append(qualifier);
        }
        return buffer.toString();
    }

    public int compareTo(Version o) {
        // components == null means SNAPSHOT and SNAPSHOT is always
        // assumed to be the most recent version
        if (components == null && o.components != null) {
            return 1;
        } else if (components != null && o.components == null) {
            return -1;
        } else if (components == null && o.components == null) {
            return 0;
        }
        // Now compare version components
        int l = Math.min(components.length, o.components.length);
        for (int i=0; i<l; i++) {
            int c = components[i] - o.components[i];
            if (c != 0) {
                return c;
            }
        }
        // x.y always comes before x.y.z
        int c = components.length - o.components.length;
        if (c != 0) {
            return c;
        }
        // x.y always comes after x.y-qqqqqq
        if (qualifier == null && o.qualifier != null) {
            return 1;
        } else if (qualifier != null && o.qualifier == null) {
            return -1;
        } else if (qualifier == null && o.qualifier == null) {
            return 0;
        }
        // x.y-SNAPSHOT comes after any x.y-qqqqqq
        boolean thisIsSnapshot = qualifier.equals(S_SNAPSHOT);
        boolean otherIsSnapshot = o.qualifier.equals(S_SNAPSHOT);
        if (thisIsSnapshot && !otherIsSnapshot) {
            return 1;
        } else if (!thisIsSnapshot && thisIsSnapshot) {
            return -1;
        } else if (thisIsSnapshot && otherIsSnapshot) {
            return 0;
        }
        // Finally compare the qualifiers using case-insensitive string comparison.
        return qualifier.compareToIgnoreCase(o.qualifier);
    }
}
