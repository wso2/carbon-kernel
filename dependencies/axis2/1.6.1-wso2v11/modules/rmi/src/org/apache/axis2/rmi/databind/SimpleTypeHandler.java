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

package org.apache.axis2.rmi.databind;

import org.apache.axis2.databinding.utils.ConverterUtil;

import java.util.Calendar;
import java.util.Date;

/**
 * this class is used to handle simple types serializing and deserializing
 */
public class SimpleTypeHandler {

    /////////////////////////////////////////////////////////////
    //
    //  convertToString mehtods
    //
    /////////////////////////////////////////////////////////////

    public String convertToString(int i) {
        return ConverterUtil.convertToString(i);
    }

    public String convertToString(float i) {
        return ConverterUtil.convertToString(i);
    }

    public String convertToString(long i) {
        return ConverterUtil.convertToString(i);
    }

    public String convertToString(double i) {
        return ConverterUtil.convertToString(i);
    }

    public String convertToString(byte i) {
        return ConverterUtil.convertToString(i);
    }

    public String convertToString(char i) {
        return ConverterUtil.convertToString(i);
    }

    public String convertToString(short i) {
        return ConverterUtil.convertToString(i);
    }

    public String convertToString(boolean i) {
        return ConverterUtil.convertToString(i);
    }

    public String convertToString(Date value) {
        return ConverterUtil.convertToString(value);
    }

    public String convertToString(Calendar value) {
        return ConverterUtil.convertToString(value);
    }

    public String convertToString(Byte o) {
        return ConverterUtil.convertToString(o);
    }

    public String convertToString(Integer o) {
        return ConverterUtil.convertToString(o);
    }

    public String convertToString(Long o) {
        return ConverterUtil.convertToString(o);
    }

    public String convertToString(Short o) {
        return ConverterUtil.convertToString(o);
    }

    public String convertToString(Double o) {
        return ConverterUtil.convertToString(o);
    }

    public String convertToString(Float o) {
        return ConverterUtil.convertToString(o);
    }

    /////////////////////////////////////////////////////////////
    //
    //  convertFromString mehtods
    //
    /////////////////////////////////////////////////////////////

    public int convertToInt(String s) {
        return ConverterUtil.convertToInt(s);
    }

    public double convertToDouble(String s) {
        return ConverterUtil.convertToDouble(s);
    }

    public float convertToFloat(String s) {
        return ConverterUtil.convertToFloat(s);
    }

    public String convertToString(String s) {
        return ConverterUtil.convertToString(s);
    }

    public long convertToLong(String s) {
        return ConverterUtil.convertToLong(s);
    }

    public short convertToShort(String s) {
        return ConverterUtil.convertToShort(s);
    }

    public boolean convertToBoolean(String s) {
        return ConverterUtil.convertToBoolean(s);
    }

    public byte convertToByte(String s) {
        return ConverterUtil.convertToByte(s);
    }

    public Date convertToDate(String s) {
        return ConverterUtil.convertToDate(s);
    }

    public Calendar convertToDateTime(String s) {
        return ConverterUtil.convertToDateTime(s);
    }

}
