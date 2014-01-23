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

package org.apache.axis2.dataretrieval;

import java.util.HashMap;

/**
 * Allow to specify options/parameters for getData request. The list is extensible
 * based on the information needed for the Data Locator to process the request.
 */

public class DataRetrievalRequest extends HashMap {

    private static final long serialVersionUID = -374933082062124908L;

    /**
     * Key used to define Dialect of data to be retrieved.
     */
    public final static String DIALET = "Dialect";
    /**
     * Key used to defined Identify of data to be retrieved.
     */
    public final static String IDENTIFIER = "Identifier";
    /**
     * Key used to define the output format of retrieved data to be returned.
     */
    public final static String OUTPUT_FORM = "OutputForm";


    /**
     * Returns the Dialect value specified in the request.
     *
     * @return a String that has dialect info.
     */

    public String getDialect() throws DataRetrievalException {
        String dialect = (String) (get(DIALET));
        if (dialect == null || dialect.length() == 0) {
            throw new DataRetrievalException(
                    "Empty dialect was detected. Dialect must have value.");
        }

        return (dialect);
    }

    /**
     * Returns the Identifier value specified in the request.
     *
     * @return a String that has Identifier info.
     */

    public String getIdentifier() {
        return (String) (get(IDENTIFIER));
    }

    /**
     * Returns the output format specified in the request.
     *
     * @return output format of data retrieved.
     */

    public OutputForm getOutputForm() {
        return (OutputForm) (get(OUTPUT_FORM));
    }

    /**
     * Allow to set the dialect of data to retrieve
     *
     * @param dialect - Valid dialect value supported by the Data Locator.
     * @throws DataRetrievalException
     */

    public void putDialect(String dialect) throws DataRetrievalException {
        if (dialect == null || dialect.length() == 0) {
            throw new DataRetrievalException(
                    "Empty dialect was detected. Dialect must have value.");
        }
        put(DIALET, dialect);
    }

    /**
     * Allow to set the identifier of data to retrieve
     *
     * @param identifier - identifier value
     * @throws DataRetrievalException
     */

    public void putIdentifier(String identifier) {
        put(IDENTIFIER, identifier);
    }

    /**
     * Allow to set the output format of the data retrieved.
     *
     * @param form - Valid output format types supported by the Data Locator.
     * @throws DataRetrievalException
     */

    public void putOutputForm(OutputForm form) {
        put(OUTPUT_FORM, form);
    }


}
