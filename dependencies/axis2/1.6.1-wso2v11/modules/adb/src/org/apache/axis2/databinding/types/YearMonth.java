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
 * Implementation of the XML Schema type gYearMonth
 *
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#gYearMonth">XML Schema 3.2.10</a>
 */
public class YearMonth implements Serializable {

    private static final long serialVersionUID = -5510739842661690551L;

    int year;
    int month;
    String timezone = null;

    /** Constructs a YearMonth with the given values No timezone is specified */
    public YearMonth(int year, int month) throws NumberFormatException {
        setValue(year, month);
    }

    /**
     * Constructs a YearMonth with the given values, including a timezone string The timezone is
     * validated but not used.
     */
    public YearMonth(int year, int month, String timezone) throws NumberFormatException {
        setValue(year, month, timezone);
    }

    /** Construct a YearMonth from a String in the format [-]CCYY-MM */
    public YearMonth(String source) throws NumberFormatException {
        int negative = 0;

        if (source.charAt(0) == '-') {
            negative = 1;
        }
        if (source.length() < (7 + negative)) {
            throw new NumberFormatException();
            // Messages.getMessage("badYearMonth00"));
        }

        // look for first '-'
        int pos = source.substring(negative).indexOf('-');
        if (pos < 0) {
            throw new NumberFormatException();
            //Messages.getMessage("badYearMonth00"));
        }
        if (negative > 0) pos++;    //adjust index for orginal string

        setValue(Integer.parseInt(source.substring(0, pos)),
                 Integer.parseInt(source.substring(pos + 1, pos + 3)),
                 source.substring(pos + 3));
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        // validate year, more than 4 digits are allowed!
        if (year == 0) {
            throw new NumberFormatException();
            // Messages.getMessage("badYearMonth00"));
        }

        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        // validate month
        if (month < 1 || month > 12) {
            throw new NumberFormatException();
            //Messages.getMessage("badYearMonth00"));
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

    public void setValue(int year, int month, String timezone) throws NumberFormatException {
        setYear(year);
        setMonth(month);
        setTimezone(timezone);
    }

    public void setValue(int year, int month) throws NumberFormatException {
        setYear(year);
        setMonth(month);
    }

    public String toString() {
        // use NumberFormat to ensure leading zeros
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);

        // year
        nf.setMinimumIntegerDigits(4);
        String s = nf.format(year) + "-";

        // month
        nf.setMinimumIntegerDigits(2);
        s += nf.format(month);

        // timezone
        if (timezone != null) {
            s = s + timezone;
        }
        return s;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof YearMonth)) return false;
        YearMonth other = (YearMonth)obj;
        if (this == obj) return true;

        boolean equals = (this.year == other.year && this.month == other.month);
        if (timezone != null) {
            equals = equals && timezone.equals(other.timezone);
        }
        return equals;
    }

    /**
     * Return the value of (month + year) XORed with the hashCode of timezone iff one is defined.
     *
     * @return an <code>int</code> value
     */
    public int hashCode() {
        return null == timezone
                ? (month + year)
                : (month + year) ^ timezone.hashCode();
    }
}
