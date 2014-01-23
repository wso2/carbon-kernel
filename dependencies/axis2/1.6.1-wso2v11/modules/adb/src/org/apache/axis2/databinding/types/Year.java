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
 * Implementation of the XML Schema type gYear
 *
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#gYear">XML Schema 3.2.11</a>
 */
public class Year implements Serializable {

    private static final long serialVersionUID = 7498876120334857019L;

    int year;
    String timezone = null;

    /** Constructs a Year with the given values No timezone is specified */
    public Year(int year) throws NumberFormatException {
        setValue(year);
    }

    /**
     * Constructs a Year with the given values, including a timezone string The timezone is
     * validated but not used.
     */
    public Year(int year, String timezone) throws NumberFormatException {
        setValue(year, timezone);
    }

    /** Construct a Year from a String in the format [-]CCYY[timezone] */
    public Year(String source) throws NumberFormatException {
        int negative = 0;

        if (source.charAt(0) == '-') {
            negative = 1;
        }
        if (source.length() < (4 + negative)) {
            throw new NumberFormatException();
            //Messages.getMessage("badYear00"));
        }

        // calculate how many more than 4 digits (if any) in the year
        int pos = 4 + negative;  // skip minus sign if present
        while (pos < source.length() && Character.isDigit(source.charAt(pos))) {
            ++pos;
        }

        setValue(Integer.parseInt(source.substring(0, pos)),
                 source.substring(pos));
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        // validate year, more than 4 digits are allowed!
        if (year == 0) {
            throw new NumberFormatException();
            //Messages.getMessage("badYear00"));
        }

        this.year = year;
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
                // Messages.getMessage("badTimezone00"));
            }
            // if we got this far, its good
            this.timezone = timezone;
        }
    }

    public void setValue(int year, String timezone)
            throws NumberFormatException {
        setYear(year);
        setTimezone(timezone);
    }

    public void setValue(int year) throws NumberFormatException {
        setYear(year);
    }

    public String toString() {
        // use NumberFormat to ensure leading zeros
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);

        // year
        nf.setMinimumIntegerDigits(4);
        String s = nf.format(year);

        // timezone
        if (timezone != null) {
            s = s + timezone;
        }
        return s;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Year)) return false;
        Year other = (Year)obj;
        if (this == obj) return true;

        boolean equals = (this.year == other.year);
        if (timezone != null) {
            equals = equals && timezone.equals(other.timezone);
        }
        return equals;
    }

    /**
     * Return the value of year XORed with the hashCode of timezone iff one is defined.
     *
     * @return an <code>int</code> value
     */
    public int hashCode() {
        return null == timezone ? year : year ^ timezone.hashCode();
    }
}
