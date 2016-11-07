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
package org.wso2.carbon.configuration.annotations.processors;

import org.wso2.carbon.configuration.annotations.Configuration;
import org.wso2.carbon.configuration.annotations.Reference;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Configuration annotation processor extending AbstractProcessor
 *
 * @since 5.0.0
 */
@SupportedAnnotationTypes("org.wso2.carbon.configuration.annotations.Configuration")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ConfigurationProcessor extends AbstractProcessor {

    public ConfigurationProcessor() {
        super();
    }

    /**
     * This method processes all Configuration annotation in the project and create config file for the documentation.
     * It reads all annotation values and set as the default values.
     *
     * @param annotations set of annotations
     * @param roundEnv annotation object to process
     * @return return true if processing is completed, false otherwise
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<Element> configSet = (Set<Element>) roundEnv.getElementsAnnotatedWith(Configuration.class);
        for (Element element : configSet) {
            Configuration configuration = element.getAnnotation(Configuration.class);
            if (configuration.level() == 0) {
                Map<String, Object> finalMap = new HashMap<>();
                finalMap.put(configuration.key(), readElementAnnotation(element, configSet));
                Yaml yaml = new Yaml();
                String content = yaml.dumpAsMap(finalMap);
                Writer writer = null;
                try {
                    FileObject file = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "",
                            configuration.key() + ".yaml");
                    content = content.replace("'", "");
                    writer = file.openWriter();
                    writer.write(content);
                } catch (IOException e) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException igorned) {
                        }
                    }
                }
            }
        }
        return true;
    }

    private Map<String, Object> readElementAnnotation(Element element, Set<Element> configSet) {
        Map<String, Object> elementMap = new HashMap<>();
        for (Element child : element.getEnclosedElements()) {
            org.wso2.carbon.configuration.annotations.Element childElem = child
                    .getAnnotation(org.wso2.carbon.configuration.annotations.Element.class);
            if (childElem != null) {
                elementMap.put(childElem.name(), childElem.value());
            } else {
                if (child.getAnnotation(Reference.class) != null) {
                    readReferenceAnnotation(configSet, elementMap, child);
                }
            }

        }
        return elementMap;
    }

    private void readReferenceAnnotation(Set<Element> configSet, Map<String, Object> elementMap, Element child) {
        List<? extends AnnotationMirror> annotationMirrors = child.getAnnotationMirrors();
        for (AnnotationMirror mirror : annotationMirrors) {
            Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues
                    = mirror.getElementValues();
            String value = null;
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                    elementValues.entrySet()) {
                if ("value".equals(entry.getKey().getSimpleName().toString())) {
                    value = String.valueOf(entry.getValue());
                }
            }
            if (value == null) {
                continue;
            }
            for (Element elem : configSet) {
                if (value.contains(elem.toString())) {
                    Configuration configuration = elem.getAnnotation(Configuration.class);
                    elementMap.put(configuration.key(), readElementAnnotation(elem, configSet));
                }
            }
        }
    }
}
