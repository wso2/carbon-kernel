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
package org.wso2.carbon.kernel.annotations.processor;


import org.wso2.carbon.kernel.annotations.Configuration;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Configuration annotation processor extending AbstractProcessor.
 * Reads all classes annotated Configuration
 *
 * @since 5.2.0
 */
@SupportedAnnotationTypes("org.wso2.carbon.kernel.annotations.Configuration")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ConfigurationProcessor extends AbstractProcessor {

    public static final String TEMP_CONFIG_FILE_NAME = "temp_config_classnames.txt";

    public ConfigurationProcessor() {
        super();
    }

    /**
     * This method reads all Configuration classes in the project and create temp files with qualified names of classes.
     *
     * @param annotations set of annotations
     * @param roundEnv    annotation object to process
     * @return return true if processing is completed, false otherwise
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<Element> configSet = (Set<Element>) roundEnv.getElementsAnnotatedWith(Configuration.class);
        StringBuilder builder = new StringBuilder();
        for (Element element : configSet) {
            Configuration configuration = element.getAnnotation(Configuration.class);
            if (configuration != null && !Configuration.NULL.equals(configuration.namespace())) {
                builder.append(((TypeElement) element).getQualifiedName()).append(",");
            }
        }
        if (builder.length() > 0) {
            try {
                FileObject file = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "",
                        TEMP_CONFIG_FILE_NAME);
                try (Writer writer = file.openWriter()) {
                    writer.write(builder.toString());
                }
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            }
        } else {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Configuration classes doesn't exist in " +
                    "the project");
        }
        return true;
    }
}
