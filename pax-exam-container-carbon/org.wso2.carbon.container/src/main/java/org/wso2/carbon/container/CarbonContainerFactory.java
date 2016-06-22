/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.container;

import org.kohsuke.MetaInfServices;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestContainerFactory;
import org.wso2.carbon.container.options.CarbonHomeOption;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.ops4j.pax.exam.CoreOptions.maven;

/**
 * Factory class for the CarbonTestContainer.
 */
@MetaInfServices
public class CarbonContainerFactory implements TestContainerFactory {

    @Override
    public TestContainer[] create(ExamSystem system) {
        List<TestContainer> containers = Arrays.asList(system.getOptions(CarbonHomeOption.class)).stream()
                .map(option -> new CarbonTestContainer(system, option)).collect(Collectors.toList());

        if (containers.isEmpty()) {
            containers.add(new CarbonTestContainer(system, getDefaultConfiguration()));
        }

        return containers.toArray(new TestContainer[containers.size()]);
    }

    /**
     * Sets the default distribution.
     *
     * @return carbon home option
     */
    private CarbonHomeOption getDefaultConfiguration() {
        String defaultDistribution = System.getProperty("org.wso2.carbon.test.default.distribution");
        if (defaultDistribution == null) {
            throw new TestContainerException("Default distribution is not specified.");
        }
        String[] distribution = defaultDistribution.split(":");
        if (distribution.length < 3) {
            return new CarbonHomeOption().distributionMavenURL(
                    maven().groupId(distribution[0]).artifactId(distribution[1]).versionAsInProject().type("zip"));
        } else {
            return new CarbonHomeOption().distributionMavenURL(
                    maven().groupId(distribution[0]).artifactId(distribution[1]).version(distribution[2]).type("zip"));
        }
    }
}
