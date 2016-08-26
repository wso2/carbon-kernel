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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * This class represents XML formatted config data and contains XML in String format.
 *
 * @since 5.2.0
 */
public final class XML extends AbstractConfigFile {

    public XML(File file) throws IOException {
        this(new FileInputStream(file), file.getName());
    }

    public XML(FileInputStream fileInputStream, String filename) throws IOException {
        super(filename);
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream,
                StandardCharsets.UTF_8))) {
            setCanonicalContent(bufferedReader.lines().collect(Collectors.joining("\n")));
        }
    }

    @Override
    public void updateContent(String canonicalContent) {
        setContent(canonicalContent);
    }
}
