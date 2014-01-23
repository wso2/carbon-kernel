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

package org.apache.axis2.databinding.types;


import java.io.Serializable;
import java.text.NumberFormat;

/**
 * Implementation of the XML Schema type gDay
 *
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#gDay">XML Schema 3.2.13</a>
 */
public class Day implements Serializable {

    private static final long serialVersionUID = -9024662553918598132L;

    int day;
    String timezone;

    /** Constructs a Day with the given values No timezone is specified */
    public Day(int day) throws NumberFormatException {
        setValue(day);
    }

    /**
     * Constructs a Day with the given values, including a timezone string The timezone is validated
     * but not used.
     */
    public Day(int day, String timezone)
            throws NumberFormatException {
        setValue(day, timezone);
    }

    /** Construct a Day from a String in the format ---DD[timezone] */
    public Day(String source) throws NumberFormatException {
        if (source.length() < 5) {
            throw new NumberFormatException();
            //Messages.getMessage("badDay00"));
        }

        if (source.charAt(0) != '-' ||
                source.charAt(1) != '-' ||
                source.charAt(2) != '-') {
            throw new NumberFormatException();
            //Messages.getMessage("badDay00"));
        }

        setValue(Integer.parseInt(source.substring(3, 5)),
                 source.substring(5));
    }

    public int getDay() {
        return day;
    }

    /** Set the day */
    public void setDay(int day) {
        // validate day
        if (day < 1 || day > 31) {
            throw new NumberFormatException();
            //Messages.getMessage("badDay00"));
        }
        this.day = day;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        // validate timezone
        if (timezone != null && timezone.length() > 0) {
            // Format [+/-]HH:MM
            if (timezone.charAt(0) == '+' || (timezone.charAt(0) == '-')) {
                if (timezone.length() != 6 ||
                        !Character.isDigit(timezone.charAt(1)) ||
                        !Character.isDigit(timezone.charAt(2)) ||
                        timezone.charAt(3) != ':' ||
                        !Character.isDigit(timezone.charAt(4)) ||
                        !Character.isDigit(timezone.charAt(5)))
                    throw new NumberFormatException();
                // Messages.getMessage("badTimezone00"));

            } else if (!timezone.equals("Z")) {
                throw new NumberFormatException();
                // Messages.getMessage("badTimezone00"));
            }
            // if we got this far, its good
            this.timezone = timezone;
        }
    }

    public void setValue(int day, String timezone)
            throws NumberFormatException {
        setDay(day);
        setTimezone(timezone);
    }

    public void setValue(int day) throws NumberFormatException {
        setDay(day);
    }

    public String toString() {
        // use NumberFormat to ensure leading zeros
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);

        // Day
        nf.setMinimumIntegerDigits(2);
        String s = "---" + nf.format(day);

        // timezone
        if (timezone != null) {
            s = s + timezone;
        }
        return s;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Day)) return false;
        Day other = (Day)obj;
        if (this == obj) return true;

        boolean equals = (this.day == other.day);
        if (timezone != null) {
            equals = equals && timezone.equals(other.timezone);
        }
        return equals;
    }

    /**
     * Return the value of day XORed with the hashCode of timezone iff one is defined.
     *
     * @return an <code>int</code> value
     */
    public int hashCode() {
        return null == timezone ? day : day ^ timezone.hashCode();
    }
}
