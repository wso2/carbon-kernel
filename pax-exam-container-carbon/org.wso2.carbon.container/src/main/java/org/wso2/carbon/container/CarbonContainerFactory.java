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
import org.ops4j.pax.exam.TestContainerFactory;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.wso2.carbon.container.options.CarbonDistributionConfigurationOption;
import org.wso2.carbon.container.options.CarbonDistributionOption;
import org.wso2.carbon.container.runner.CarbonRunner;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.ops4j.pax.exam.CoreOptions.maven;

@MetaInfServices
public class CarbonContainerFactory implements TestContainerFactory {

    @Override
    public TestContainer[] create(ExamSystem system) {
        List<TestContainer> containers = Arrays.asList(system.getOptions(CarbonDistributionConfigurationOption.class))
                .stream().map(option -> new CarbonTestContainer(system, option))
                .collect(Collectors.toList());

        if(containers.isEmpty()){
            containers.add(new CarbonTestContainer(system, getDefaultConfiguration()));
        }

        return containers.toArray(new TestContainer[containers.size()]);
    }

    public CarbonDistributionConfigurationOption getDefaultConfiguration(){
        String defaultDistribution = System.getProperty("pax.default.distribution");
        String[] distribution = defaultDistribution.split(":");
        return CarbonDistributionOption.CarbonDistributionConfiguration().distributionMavenURL(maven().groupId
                (distribution[0]).artifactId(distribution[1]).versionAsInProject().type("zip"));
    }
}
