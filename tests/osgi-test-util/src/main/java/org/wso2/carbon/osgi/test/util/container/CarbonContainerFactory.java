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
package org.wso2.carbon.osgi.test.util.container;

import org.kohsuke.MetaInfServices;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerFactory;
import org.wso2.carbon.osgi.test.util.container.options.CarbonDistributionConfigurationOption;
import org.wso2.carbon.osgi.test.util.container.runner.CarbonRunner;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@MetaInfServices
public class CarbonContainerFactory implements TestContainerFactory {

    @Override
    public TestContainer[] create(ExamSystem system) {
        List<TestContainer> containers = Arrays.asList(system.getOptions(CarbonDistributionConfigurationOption.class))
                .stream().map(option -> new CarbonTestContainer(system, option, new CarbonRunner()))
                .collect(Collectors.toList());

        return containers.toArray(new TestContainer[containers.size()]);
    }

}
