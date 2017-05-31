/* 
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
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
package org.wso2.carbon.utils.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

public class MessagesConstants {
    public static final String PROJECT_NAME = "org.wso2.carbon.utils".intern();
    public static final String RESOURCE_NAME = "resource".intern();
    public static final Locale LOCALE = null;

    public static final String rootPackageName = "org.wso2.carbon.utils.i18n".intern();
    
    public static final ResourceBundle rootBundle =
            ProjectResourceBundle.getBundle(PROJECT_NAME,
                    rootPackageName,
                    RESOURCE_NAME,
                    LOCALE,
                    MessagesConstants.class.getClassLoader(),
                    null);
}
