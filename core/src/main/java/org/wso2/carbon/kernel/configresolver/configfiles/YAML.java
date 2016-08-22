/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.kernel.configresolver.configfiles;

import org.wso2.carbon.kernel.configresolver.ConfigResolverUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * This class represents YAML formatted config data and contains YAML in String format.
 *
 * @since 5.2.0
 */
public final class YAML extends AbstractConfigFile {
    private static final String ROOT_ELEMENT = "configurations";

    public YAML(File file) throws IOException {
        this(new FileInputStream(file), file.getName());
    }

    public YAML(FileInputStream fileInputStream, String filename) throws IOException {
        super(filename);
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream,
                StandardCharsets.UTF_8))) {
            String input = bufferedReader.lines().collect(Collectors.joining("\n"));
            setCanonicalContent(ConfigResolverUtils.convertYAMLToXML(input, ROOT_ELEMENT));
        }
    }

    public void updateContent(String canonicalContent) {
        setContent(ConfigResolverUtils.convertXMLToYAML(canonicalContent, ROOT_ELEMENT));
    }
}
