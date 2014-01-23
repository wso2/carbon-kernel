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


import java.text.NumberFormat;

/**
 * Implementation of the XML Schema type gMonth
 *
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#gMonth">XML Schema 3.2.14</a>
 */
public class Month implements java.io.Serializable {

    private static final long serialVersionUID = -7469265802807262347L;

    int month;
    String timezone;

    /** Constructs a Month with the given values No timezone is specified */
    public Month(int month) throws NumberFormatException {
        setValue(month);
    }

    /**
     * Constructs a Month with the given values, including a timezone string The timezone is
     * validated but not used.
     */
    public Month(int month, String timezone)
            throws NumberFormatException {
        setValue(month, timezone);
    }

    /** Construct a Month from a String in the format --MM--[timezone] */
    public Month(String source) throws NumberFormatException {
        if (source.length() < (6)) {
            throw new NumberFormatException();
            // Messages.getMessage("badMonth00"));
        }

        if (source.charAt(0) != '-' ||
                source.charAt(1) != '-' ||
                source.charAt(4) != '-' ||
                source.charAt(5) != '-') {
            throw new NumberFormatException();
            //Messages.getMessage("badMonth00"));
        }

        setValue(Integer.parseInt(source.substring(2, 4)),
                 source.substring(6));
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        // validate month
        if (month < 1 || month > 12) {
            throw new NumberFormatException();
            // Messages.getMessage("badMonth00"));
        }
        this.month = month;
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
                //Messages.getMessage("badTimezone00"));

            } else if (!timezone.equals("Z")) {
                throw new NumberFormatException();
                //Messages.getMessage("badTimezone00"));
            }
            // if we got this far, its good
            this.timezone = timezone;
        }
    }

    public void setValue(int month, String timezone) throws NumberFormatException {
        setMonth(month);
        setTimezone(timezone);
    }

    public void setValue(int month) throws NumberFormatException {
        setMonth(month);
    }

    public String toString() {
        // use NumberFormat to ensure leading zeros
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);

        // month
        nf.setMinimumIntegerDigits(2);
        String s = "--" + nf.format(month) + "--";

        // timezone
        if (timezone != null) {
            s = s + timezone;
        }
        return s;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Month)) return false;
        Month other = (Month)obj;
        if (this == obj) return true;

        boolean equals = (this.month == other.month);
        if (timezone != null) {
            equals = equals && timezone.equals(other.timezone);
        }
        return equals;
    }

    /**
     * Return the value of month XORed with the hashCode of timezone iff one is defined.
     *
     * @return an <code>int</code> value
     */
    public int hashCode() {
        return null == timezone ? month : month ^ timezone.hashCode();
    }
}
