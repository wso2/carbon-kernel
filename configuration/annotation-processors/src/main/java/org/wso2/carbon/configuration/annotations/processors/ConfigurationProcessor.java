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
import org.wso2.carbon.configuration.annotations.Ignore;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Configuration annotation processor extending AbstractProcessor
 *
 * @since 5.2.0
 */
@SupportedAnnotationTypes("org.wso2.carbon.configuration.annotations.Configuration")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ConfigurationProcessor extends AbstractProcessor {

    Map<String, String> descriptionMap = new LinkedHashMap<>();

    public ConfigurationProcessor() {
        super();
    }

    /**
     * This method processes all Configuration annotation in the project and create config file for the documentation.
     * It reads all annotation values and set as the default values.
     *
     * @param annotations set of annotations
     * @param roundEnv    annotation object to process
     * @return return true if processing is completed, false otherwise
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<Element> configSet = (Set<Element>) roundEnv.getElementsAnnotatedWith(Configuration.class);
        for (Element element : configSet) {
            Configuration configuration = element.getAnnotation(Configuration.class);
            if (configuration.namespace().startsWith("wso2")) {
                Map<String, Object> finalMap = new LinkedHashMap<>();

                if (!configuration.description().equals(Configuration.NULL)) {
                    descriptionMap.put(configuration.namespace(), configuration.description());
                    finalMap.put("comment-" + configuration.namespace(), createDescriptionComment(configuration
                            .description()));
                }
                descriptionMap.put(configuration.namespace(), configuration.description());
                finalMap.put(configuration.namespace(), readConfigurationElements(element, configSet));
                Yaml yaml = new Yaml();
                String content = yaml.dumpAsMap(finalMap);
                content = content.replaceAll("comment.*", "");
                content = content.replaceAll("(?m)^[ \t]*\r?\n", "");
                Writer writer = null;
                try {
                    FileObject file = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "",
                            configuration.namespace() + ".yaml");
                    content = content.replace("'", "");
                    writer = file.openWriter();
                    writer.write(createConfigurationDescription());
                    writer.append(content);
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

    private Map<String, Object> readConfigurationElements(Element element, Set<Element> configSet) {
        Map<String, Object> elementMap = new LinkedHashMap<>();
        List<VariableElement> fields = ElementFilter.fieldsIn(element.getEnclosedElements());
        for (VariableElement field : fields) {
            if (field.getAnnotation(Ignore.class) != null) {
                continue;
            }

            Element fieldElement = null;
            TypeMirror fieldType = field.asType();

            if (!fieldType.getKind().isPrimitive()) {
                fieldElement = ((DeclaredType) fieldType).asElement();
            }

            if (fieldElement != null && configSet.contains(fieldElement)) {
                Configuration configuration = fieldElement.getAnnotation(Configuration.class);
                if (!configuration.description().equals(Configuration.NULL)) {
                    descriptionMap.put(configuration.namespace(), configuration.description());
                    elementMap.put("comment-" + configuration.namespace(), createDescriptionComment(configuration
                            .description()));
                }
                elementMap.put(configuration.namespace(), readConfigurationElements(fieldElement, configSet));
            } else {
                org.wso2.carbon.configuration.annotations.Element fieldElem = field.getAnnotation(org.wso2.carbon
                        .configuration.annotations.Element.class);
                String description = "";
                String defaultValue = "";
                boolean required = false;
                if (fieldElem != null) {
                    description = fieldElem.description();
                    defaultValue = fieldElem.defaultValue();
                    required = fieldElem.required();

                    if (!description.equals(org.wso2.carbon.configuration.annotations.Element.NULL)) {
                        descriptionMap.put(field.getSimpleName().toString(), description);
                        elementMap.put("comment-" + field.getSimpleName().toString(),
                                createDescriptionComment(description));
                    }
                }
                if (required) {
                    defaultValue = defaultValue + " # THIS IS A MANDATORY FIELD";
                }
                elementMap.put(field.getSimpleName().toString(), defaultValue);
            }
        }
        return elementMap;
    }

    private String createConfigurationDescription() {
        Yaml yaml = new Yaml();
        String content = yaml.dumpAsMap(descriptionMap);
        String lines[] = content.split("\\r?\\n");

        StringBuilder builder = new StringBuilder();
        builder.append("# Please read the comments related to below configuration before using them\n" +
                "#\n");
        for (String line : lines) {
            builder.append("# " + line + "\n");
        }
        builder.append("#\n")
                .append("################################################################################\n");

        return builder.toString();
    }

    private String createDescriptionComment(String description) {
        StringBuilder builder = new StringBuilder();
        String lines[] = description.split("\\r?\\n");
        for (String line : lines) {
            builder.append("# " + line + "\n");
        }
        return builder.toString();
    }
}
