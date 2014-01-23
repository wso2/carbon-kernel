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

package org.tempuri.complex.data;

import org.tempuri.complex.data.arrays.ArrayOfshort;

import java.util.Calendar;


public class Employee {

    protected Person baseDetails;
    protected Calendar hireDate;
    protected Long jobID;
    protected ArrayOfshort numbers;

    /**
     * Gets the value of the baseDetails property.
     */
    public Person getBaseDetails() {
        return baseDetails;
    }

    /**
     * Sets the value of the baseDetails property.
     */
    public void setBaseDetails(Person value) {
        this.baseDetails = ((Person) value);
    }

    /**
     * Gets the value of the hireDate property.
     *
     * @return possible object is
     *         {@link Calendar }
     */
    public Calendar getHireDate() {
        return hireDate;
    }

    /**
     * Sets the value of the hireDate property.
     *
     * @param value allowed object is
     *              {@link Calendar }
     */
    public void setHireDate(Calendar value) {
        this.hireDate = value;
    }

    /**
     * Gets the value of the jobID property.
     *
     * @return possible object is
     *         {@link Long }
     */
    public Long getJobID() {
        return jobID;
    }

    /**
     * Sets the value of the jobID property.
     *
     * @param value allowed object is
     *              {@link Long }
     */
    public void setJobID(Long value) {
        this.jobID = value;
    }

    /**
     * Gets the value of the numbers property.
     */
    public ArrayOfshort getNumbers() {
        return numbers;
    }

    /**
     * Sets the value of the numbers property.
     */
    public void setNumbers(ArrayOfshort value) {
        this.numbers = value;
    }

}
