/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.osgi;

import org.ops4j.pax.exam.ConfigurationFactory;
import org.ops4j.pax.exam.Option;
import org.wso2.carbon.osgi.test.util.OSGiTestConfigurationUtils;

import java.util.List;

/**
 * This class will provide the shared configurations for the OSGi tests.
 * <p>
 * In order to this file be affected, full qualified name of this class needs to be put into the
 * META-INF/services/org.ops4j.pax.exam.ConfigurationFactory resource file.
 *
 * @since 5.0.0
 */
public class CarbonOSGiConfiguration implements ConfigurationFactory {

    @Override
    /**
     * Populates the default configuration required to run PAX-EXAM.
     */
    public Option[] createConfiguration() {
        //setting up the environment
        List<Option> optionList = OSGiTestConfigurationUtils.getConfiguration();
        return optionList.toArray(new Option[optionList.size()]);
    }
}
