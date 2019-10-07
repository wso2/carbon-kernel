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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests TenantIdConverter class.
 */
public class TenantIdConverterTest {
    private TenantIdConverter tenantIdConverter;
    private LogEvent logEvent;

    /**
     * Creates a log event to test appending the tenantId.
     */
    @BeforeMethod
    public void setUp() throws Exception {
        tenantIdConverter = TenantIdConverter.newInstance(null);
        Message msg = new SimpleMessage("Test logging");
        logEvent = Log4jLogEvent.newBuilder()
                .setLoggerName("TestLogger")
                .setLevel(Level.INFO)
                .setMessage(msg).build();
    }

    /**
     * Tests appending the tenantId into the log message.
     *
     * @throws IOException for failed or interrupted file creation
     */
    @Test
    public void testFormat() throws IOException {
        Path carbonHome = Files.createTempDirectory("carbonHome");
        File repository = new File(carbonHome.toFile(), "repository");
        repository.mkdir();
        File conf = new File(repository, "conf");
        conf.mkdir();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("carbon.xml").getFile());

        Path src = file.toPath();
        Path dest = Paths.get(conf.getAbsolutePath() + File.separator + file.getName());
        Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
        assertTrue(dest.toFile().exists());
        System.setProperty(ServerConstants.CARBON_HOME, carbonHome.toString());

        final StringBuilder sb = new StringBuilder();
        tenantIdConverter.format(logEvent, sb);
        assertEquals("-1", sb.toString());
    }
}
