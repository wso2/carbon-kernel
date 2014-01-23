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

package org.apache.axis2.schema.decimal;

import org.apache.axis2.databinding.types.Duration;
import org.apache.axis2.schema.AbstractTestCase;

import java.math.BigDecimal;
import java.util.Calendar;

public class DecimalTest extends AbstractTestCase {

    public void testDecimal() throws Exception{
        GetHistoricNavResponse getHistoricNavResponse = new GetHistoricNavResponse();
        ArrayOfDecimal arrayOfDecimal = new ArrayOfDecimal();
        getHistoricNavResponse.setOut(arrayOfDecimal);
        arrayOfDecimal.addDecimal(null);
        arrayOfDecimal.addDecimal(null);
        arrayOfDecimal.addDecimal(new BigDecimal("111.38"));
        arrayOfDecimal.addDecimal(new BigDecimal("111.38"));
        arrayOfDecimal.addDecimal(new BigDecimal("111.38"));
        arrayOfDecimal.addDecimal(new BigDecimal("111.54"));

        testSerializeDeserialize(getHistoricNavResponse, false);
    }

    // TODO: explain what this test has to do with "DecimalTest"???
    public void testDuration(){
        Calendar calendar = Calendar.getInstance();
        Duration duration = new Duration(false,0,0,23,12,24,23.45);
        System.out.println("Duration ==> " + duration.toString());
        // P2007Y5M30DT8H40M55.87S
        // "\\-?P(\\d*D)?(T(\\d*H)?(\\d*M)?(\\d*(\\.\\d*)?S)?)?"
        if (duration.toString().matches("\\-?P(\\d*D)?(T(\\d*H)?(\\d*M)?(\\d*(\\.\\d*)?S)?)?")){
            System.out.println("Matches");
        }
    }
}
