/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.kernel.util;

import org.wso2.carbon.kernel.CarbonConstants;

import java.io.File;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final String VAR_REGEXP = "\\$\\{[^}]*}";
    private static final Pattern varPattern = Pattern.compile(VAR_REGEXP);

    public static String getCarbonXMLLocation() {
          return getCarbonHome() + File.separator + CarbonConstants.CONF_REPO_DIR +
                  File.separator + CarbonConstants.CARBON_CONFIG_XML;
    }

    public static String getCarbonHome(){
        // Assumption. This property is always get validated in the Carbon launcher module.
        return System.getProperty(CarbonConstants.CARBON_HOME);
    }

    /**
     * Replace system property holders in the property values.
     * e.g. Replace ${carbon.home} with value of the carbon.home system property.
     */
    public static String substituteVars(String value) {
        //TODO this method is duplicated in org.wso2.carbon.launcher.utils package. FIX IT.

        String newValue = value;

        Matcher matcher = varPattern.matcher(value);
        while (matcher.find()) {
            String sysPropKey = value.substring(matcher.start() + 2, matcher.end() - 1);
            String sysPropValue = System.getProperty(sysPropKey);
            if (sysPropValue == null || sysPropValue.length() == 0) {
                throw new RuntimeException("System property " + sysPropKey + " cannot be null");
            }
            sysPropValue = sysPropValue.replace("\\", "\\\\");   // Due to reported bug under CARBON-14746
            newValue = newValue.replaceFirst(VAR_REGEXP, sysPropValue);
        }

//        if(logger.isLoggable(Level.FINE)){
//            logger.log(Level.FINE, "Substitute Variables before: " + value + ", after: " + newValue);
//        }

        return newValue;
    }

}
