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


import org.apache.axis2.databinding.utils.ConverterUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/** Class that represents the xsd:time XML Schema type */
public class Time implements java.io.Serializable {

    private static final long serialVersionUID = -9022201555535589908L;

    private Calendar _value;
    private boolean isFromString;
    private String originalString;

    /**
     * a shared java.text.SimpleDateFormat instance used for parsing the basic component of the
     * timestamp
     */

    /** Initializes with a Calender. Year, month and date are ignored. */
    public Time(Calendar value) {
        this._value = value;
        this._value.clear(Calendar.YEAR);
        this._value.clear(Calendar.MONTH);
        this._value.clear(Calendar.DATE);
    }

    /** Converts a string formatted as HH:mm:ss[.SSS][+/-offset] */
    public Time(String value) throws NumberFormatException {
        _value = makeValue(value);
        this.isFromString = true;
        this.originalString = value;
    }

    /**
     * Returns the time as a calendar. Ignores the year, month and date fields.
     *
     * @return Returns calendar value; may be null.
     */
    public Calendar getAsCalendar() {
        return _value;
    }

    /**
     * Sets the time; ignores year, month, date
     *
     * @param date
     */
    public void setTime(Calendar date) {
        this._value = date;
        this._value.clear(Calendar.YEAR);
        this._value.clear(Calendar.MONTH);
        this._value.clear(Calendar.DATE);
    }

    /**
     * Sets the time from a date instance.
     *
     * @param date
     */
    public void setTime(Date date) {
        _value.setTime(date);
        this._value.clear(Calendar.YEAR);
        this._value.clear(Calendar.MONTH);
        this._value.clear(Calendar.DATE);
    }

    /** Utility function that parses xsd:time strings and returns a Date object */
    private Calendar makeValue(String source) throws NumberFormatException {

        // cannonical form of the times is  hh ':' mm ':' ss ('.' s+)? (zzzzzz)?
        if ((source == null) || (source.trim().length() == 0)){
            return null;
        }

        source = source.trim();

        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setLenient(false);
        int hour = 0;
        int minite = 0;
        int second = 0;
        long miliSecond = 0;

        int timeZoneOffSet = TimeZone.getDefault().getRawOffset();
         int milliSecondPartLength = 0;
        if (source.length() >= 8) {
            if ((source.charAt(2) != ':' )|| (source.charAt(5) != ':')){
                throw new RuntimeException("Invalid time format (" + source + ") having : s in wrong places");
            }
            hour = Integer.parseInt(source.substring(0, 2));
            minite = Integer.parseInt(source.substring(3, 5));
            second = Integer.parseInt(source.substring(6, 8));



            if (source.length() > 8) {
                String rest = source.substring(8);
                if (rest.startsWith(".")) {
                    // i.e this have the ('.'s+) part
                    if (rest.endsWith("Z")) {
                        // this is in gmt time zone
                        timeZoneOffSet = 0;
                        miliSecond = Integer.parseInt(rest.substring(1, rest.lastIndexOf("Z")));
                        milliSecondPartLength = rest.substring(1,rest.lastIndexOf("Z")).trim().length();
                    } else if ((rest.lastIndexOf("+") > 0) || (rest.lastIndexOf("-") > 0)) {
                        // this is given in a general time zione
                        String timeOffSet = null;
                        if (rest.lastIndexOf("+") > 0) {
                            timeOffSet = rest.substring(rest.lastIndexOf("+") + 1);
                            miliSecond = Integer.parseInt(rest.substring(1, rest.lastIndexOf("+")));
                            milliSecondPartLength = rest.substring(1, rest.lastIndexOf("+")).trim().length();
                            // we keep +1 or -1 to finally calculate the value
                            timeZoneOffSet = 1;

                        } else if (rest.lastIndexOf("-") > 0) {
                            timeOffSet = rest.substring(rest.lastIndexOf("-") + 1);
                            miliSecond = Integer.parseInt(rest.substring(1, rest.lastIndexOf("-")));
                            milliSecondPartLength = rest.substring(1, rest.lastIndexOf("-")).trim().length();
                            // we keep +1 or -1 to finally calculate the value
                            timeZoneOffSet = -1;
                        }
                        if (timeOffSet.charAt(2) != ':') {
                            throw new RuntimeException("invalid time zone format (" + source
                                    + ") without : at correct place");
                        }
                        int hours = Integer.parseInt(timeOffSet.substring(0, 2));
                        int minits = Integer.parseInt(timeOffSet.substring(3, 5));
                        timeZoneOffSet = ((hours * 60) + minits) * 60000 * timeZoneOffSet;

                    } else {
                        // i.e it does not have time zone
                        miliSecond = Integer.parseInt(rest.substring(1));
                        milliSecondPartLength = rest.substring(1).trim().length();
                    }

                } else {
                    if (rest.startsWith("Z")) {
                        // this is in gmt time zone
                        timeZoneOffSet = 0;
                    } else if (rest.startsWith("+") || rest.startsWith("-")) {
                        // this is given in a general time zione
                        if (rest.charAt(3) != ':') {
                            throw new RuntimeException("invalid time zone format (" + source
                                    + ") without : at correct place");
                        }
                        int hours = Integer.parseInt(rest.substring(1, 3));
                        int minits = Integer.parseInt(rest.substring(4, 6));
                        timeZoneOffSet = ((hours * 60) + minits) * 60000;
                        if (rest.startsWith("-")) {
                            timeZoneOffSet = timeZoneOffSet * -1;
                        }
                    } else {
                        throw new NumberFormatException("in valid time zone attribute");
                    }
                }
            }
        } else {
            throw new RuntimeException("invalid message string");
        }

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minite);
        calendar.set(Calendar.SECOND, second);
        if (milliSecondPartLength != 3) {
            // milisecond part represenst the fraction of the second so we have to
            // find the fraction and multiply it by 1000. So if milisecond part
            // has three digits nothing required
            miliSecond = miliSecond * 1000;
            for (int i = 0; i < milliSecondPartLength; i++) {
                miliSecond = miliSecond / 10;
            }
        }
        calendar.set(Calendar.MILLISECOND, (int)miliSecond);
        calendar.set(Calendar.ZONE_OFFSET, timeZoneOffSet);

        // set the day light off set only if time zone
        if (source.length() > 8) {
            calendar.set(Calendar.DST_OFFSET, 0);
        }

        return calendar;
    }


    /**
     * Returns the time as it would be in GMT. This is accurate to the seconds. Milliseconds
     * probably gets lost.
     *
     * @return Returns String.
     */
    public String toString() {
        if (_value == null) {
            return "unassigned Time";
        }

        if (isFromString) {
            return originalString;
        } else {
            StringBuffer timeString = new StringBuffer();
            ConverterUtil.appendTime(_value,timeString);
            ConverterUtil.appendTimeZone(_value,timeString);
            return timeString.toString();
        }

    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Time)) return false;
        Time other = (Time)obj;
        if (this == obj) return true;

        boolean _equals;
        _equals = ((_value == null && other._value == null) ||
                (_value != null &&
                        _value.getTime().equals(other._value.getTime())));

        return _equals;

    }

    /**
     * Returns the hashcode of the underlying calendar.
     *
     * @return Returns an <code>int</code> value.
     */
    public int hashCode() {
        return _value == null ? 0 : _value.hashCode();
    }
}
