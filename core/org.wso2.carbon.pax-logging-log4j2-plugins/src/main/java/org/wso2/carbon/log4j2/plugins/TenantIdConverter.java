/*
 * Copyright 2017 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.log4j2.plugins;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.StringBuilders;
import org.wso2.carbon.base.MultitenantConstants;

/**
 * This Converter is used to register the tenantId as a parameter in the log message.
 * The value specified for @ConverterKeys is used as the syntax to refer
 * the tenantId in the configuration file.
 * Refer documentation
 * {@linktourl https://logging.apache.org/log4j/2.0/manual/extending.html#PatternConverters}
 *
 * Converters are required to provide a static newInstance method that accepts
 * an array of Strings as the only parameter.
 *
 * Similar implementation in log4J package
 * {@link org.apache.logging.log4j.core.pattern.ThreadNamePatternConverter}
 */
@Plugin(name = "TenantIdConverter", category = "Converter")
@ConverterKeys({ "tenantId" })
public class TenantIdConverter extends LogEventPatternConverter {
    private static final TenantIdConverter INSTANCE = new TenantIdConverter();

    /**
     * Constructs an instance of TenantIdConverter.
     */
    private TenantIdConverter() {
        super("tenantId", "tenantId");
    }

    /**
     * Obtains an instance of TenantIdConverter.
     *
     * @param options ignored since it cannot be configured in log4j2.properties
     * @return instance of TenantIdConverter
     */
    public static TenantIdConverter newInstance(String[] options) {
        return INSTANCE;
    }

    @Override
    public void format(LogEvent event, StringBuilder toAppendTo) {

        ReadOnlyStringMap contextData = event.getContextData();
        if (contextData != null && contextData.size() != 0) {
            Object value = contextData.getValue(MultitenantConstants.TENANT_ID);
            if (value != null) {
                StringBuilders.appendValue(toAppendTo, value);
            }
        }
    }
}
