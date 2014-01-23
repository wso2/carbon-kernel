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
 * Implementation of the XML Schema type gMonthDay
 *
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#gMonthDay">XML Schema 3.2.12</a>
 */
public class MonthDay implements java.io.Serializable {

    private static final long serialVersionUID = -345189609825249318L;

    int month;
    int day;
    String timezone;

    /** Constructs a MonthDay with the given values No timezone is specified */
    public MonthDay(int month, int day)
            throws NumberFormatException {
        setValue(month, day);
    }

    /**
     * Constructs a MonthDay with the given values, including a timezone string The timezone is
     * validated but not used.
     */
    public MonthDay(int month, int day, String timezone)
            throws NumberFormatException {
        setValue(month, day, timezone);
    }

    /** Construct a MonthDay from a String in the format --MM-DD[timezone] */
    public MonthDay(String source) throws NumberFormatException {
        if (source.length() < 6) {
            throw new NumberFormatException();
            //Messages.getMessage("badMonthDay00"));
        }

        if (source.charAt(0) != '-' ||
                source.charAt(1) != '-' ||
                source.charAt(4) != '-') {
            throw new NumberFormatException();
            //Messages.getMessage("badMonthDay00"));
        }

        setValue(Integer.parseInt(source.substring(2, 4)),
                 Integer.parseInt(source.substring(5, 7)),
                 source.substring(7));
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        // validate month
        if (month < 1 || month > 12) {
            throw new NumberFormatException();
            //Messages.getMessage("badMonthDay00"));
        }
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    /** Set the day NOTE: if the month isn't set yet, the day isn't validated */
    public void setDay(int day) {
        // validate day
        if (day < 1 || day > 31) {
            throw new NumberFormatException();
            //Messages.getMessage("badMonthDay00"));
        }
        // 30 days has September... All the rest have 31 (except Feb!)
        // NOTE: if month isn't set, we don't validate day.
        if ((month == 2 && day > 29) ||
                ((month == 9 || month == 4 || month == 6 || month == 11) && day > 30)) {
            throw new NumberFormatException();
            // Messages.getMessage("badMonthDay00"));
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
                //Messages.getMessage("badTimezone00"));
            }
            // if we got this far, its good
            this.timezone = timezone;
        }
    }

    public void setValue(int month, int day, String timezone)
            throws NumberFormatException {
        setMonth(month);
        setDay(day);
        setTimezone(timezone);
    }

    public void setValue(int month, int day) throws NumberFormatException {
        setMonth(month);
        setDay(day);
    }

    public String toString() {
        // use NumberFormat to ensure leading zeros
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);

        // month & Day: --MM-DD
        nf.setMinimumIntegerDigits(2);
        String s = "--" + nf.format(month) + "-" + nf.format(day);

        // timezone
        if (timezone != null) {
            s = s + timezone;
        }
        return s;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof MonthDay)) return false;
        MonthDay other = (MonthDay)obj;
        if (this == obj) return true;

        boolean equals = (this.month == other.month && this.day == other.day);
        if (timezone != null) {
            equals = equals && timezone.equals(other.timezone);
        }
        return equals;
    }

    /**
     * Return the value of (month + day) XORed with the hashCode of timezone iff one is defined.
     *
     * @return an <code>int</code> value
     */
    public int hashCode() {
        return null == timezone
                ? (month + day)
                : (month + day) ^ timezone.hashCode();
    }
}
