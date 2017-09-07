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
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.logging.handler.TenantDomainSetter;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * This Converter is used to append the appName as a parameter in the log message.
 * The value specified for @ConverterKeys is used as the syntax to refer
 * the appName in the configuration file.
 * Refer documentation
 * {@linktourl https://logging.apache.org/log4j/2.0/manual/extending.html#PatternConverters}
 *
 * Converters are required to provide a static newInstance method that accepts
 * an array of Strings as the only parameter.
 *
 * Similar implementation in log4J package
 * {@link org.apache.logging.log4j.core.pattern.ThreadNamePatternConverter}
 */
@Plugin(name = "AppNameConverter", category = "Converter")
@ConverterKeys({ "appName" })
public class AppNameConverter extends LogEventPatternConverter {
    private static final AppNameConverter INSTANCE = new AppNameConverter();

    /**
     * Constructs an instance of AppNameConverter.
     */
    private AppNameConverter() {
        super("appName", "appName");
    }

    /**
     * Obtains an instance of AppNameConverter.
     *
     * @param options ignored since it cannot be configured in log4j2.properties
     * @return instance of AppNameConverter
     */
    public static AppNameConverter newInstance(String[] options) {
        return INSTANCE;
    }

    @Override
    public void format(LogEvent event, StringBuilder toAppendTo) {
        if (getAppName() != null) {
            toAppendTo.append(getAppName());
        }
    }

    /**
     * Obtains the application name.
     *
     * @return application name in type String
     */
    private String getAppName() {
        String appName = CarbonContext.getThreadLocalCarbonContext().getApplicationName();
        if (appName == null) {
            appName = TenantDomainSetter.getServiceName();
        }
        return appName;
    }
}
