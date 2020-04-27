/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.user.core.constants;

import org.wso2.carbon.user.api.Property;

/**
 * Meta constants which are required to populate the configuration UIs.
 */
public class UserStoreUIConstants {

    public static final String TYPE = "type";

    public static final String CATEGORY = "category";

    public static final String REQUIRED = "required";

    private UserStoreUIConstants() {

    }

    /**
     * Data categories of the user store configuration.
     */
    public enum DataCategory {

        BASIC("basic"),
        CONNECTION("connection"),
        USER("user"),
        GROUP("group");

        private Property property;

        DataCategory(String dataCategory) {

            property = new Property(CATEGORY, dataCategory, null, null);
        }

        public Property getProperty() {

            return property;
        }
    }

    /**
     * Data types for user store configuration.
     */
    public enum DataTypes {

        STRING("string"),
        NUMBER("number"),
        BOOLEAN("boolean"),
        PASSWORD("password"),
        SQL("sql");

        private Property property;

        DataTypes(String dataType) {

            property = new Property(TYPE, dataType, null, null);
        }

        public Property getProperty() {

            return property;
        }
    }

    /**
     * Importance of the user store configuration.
     */
    public enum DataImportance {

        TRUE("true"),
        FALSE("false");

        private Property property;

        DataImportance(String dataImportance) {

            property = new Property(REQUIRED, dataImportance, null, null);
        }

        public Property getProperty() {

            return property;
        }
    }
}
