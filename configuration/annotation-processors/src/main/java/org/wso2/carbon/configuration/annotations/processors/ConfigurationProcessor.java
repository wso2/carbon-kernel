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
import java.util.HashMap;
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
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
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

    private static final String LICENSE_HEADER =
            "################################################################################\n" +
            "#   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.\n" +
            "#\n" +
            "#   Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
            "#   you may not use this file except in compliance with the License.\n" +
            "#   You may obtain a copy of the License at\n" +
            "#\n" +
            "#   http://www.apache.org/licenses/LICENSE-2.0\n" +
            "#\n" +
            "#   Unless required by applicable law or agreed to in writing, software\n" +
            "#   distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
            "#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
            "#   See the License for the specific language governing permissions and\n" +
            "#   limitations under the License.\n" +
            "################################################################################\n";
    private static final String COMMENT_KEY_PREFIX = "comment-";
    private static final String COMMENT_KEY_REGEX_PATTERN = COMMENT_KEY_PREFIX + ".*";
    private static final String EMPTY_LINE_REGEX_PATTERN = "(?m)^[ \t]*\r?\n";
    private static final String YAML_FILE_EXTENTION = ".yaml";
    private static final String MANDATORY_FIELD_COMMENT = " # THIS IS A MANDATORY FIELD";
    private static final String NEW_LINE_REGEX_PATTERN = "\\r?\\n";

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
                    finalMap.put(COMMENT_KEY_PREFIX + configuration.namespace(), createDescriptionComment(configuration
                            .description()));
                }
                finalMap.put(configuration.namespace(), readConfigurationElements(element, configSet));
                Yaml yaml = new Yaml();
                String content = yaml.dumpAsMap(finalMap);
                content = content.replaceAll(COMMENT_KEY_REGEX_PATTERN, "");
                content = content.replaceAll(EMPTY_LINE_REGEX_PATTERN, "");
                Writer writer = null;
                try {
                    FileObject file = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "",
                            configuration.namespace() + YAML_FILE_EXTENTION);
                    content = content.replace("'", "");
                    writer = file.openWriter();
                    writer.write(LICENSE_HEADER);
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
            List<TypeMirror> argumentTypes = null;
            TypeMirror fieldType = field.asType();

            if (fieldType.getKind() == TypeKind.DECLARED) {
                fieldElement = ((DeclaredType) fieldType).asElement();
                argumentTypes = (List<TypeMirror>) ((DeclaredType) fieldType).getTypeArguments();
            }

            if (fieldElement != null && configSet.contains(fieldElement)) {
                Configuration configuration = fieldElement.getAnnotation(Configuration.class);
                if (!configuration.description().equals(Configuration.NULL)) {
                    elementMap.put(COMMENT_KEY_PREFIX + configuration.namespace(), createDescriptionComment
                            (configuration.description()));
                }
                elementMap.put(configuration.namespace(), readConfigurationElements(fieldElement, configSet));
            } else if (fieldType.getKind() == TypeKind.ARRAY) {
                ArrayType asArrayType = (ArrayType) fieldType;
                TypeMirror arrayFieldType = asArrayType.getComponentType();
                addFieldDescription(elementMap, field);
                elementMap.put(field.getSimpleName().toString(), getElementArrayObject(configSet, arrayFieldType));
            } else if (fieldType.getKind() == TypeKind.DECLARED && argumentTypes != null && !argumentTypes.isEmpty()) {
                if (argumentTypes.size() == 1) {
                    //(such as {@code Outer<String>.Inner<Number>})
                    TypeMirror argumentType = argumentTypes.get(0);
                    addFieldDescription(elementMap, field);
                    elementMap.put(field.getSimpleName().toString(), getElementArrayObject(configSet, argumentType));
                }
            } else {
                String defaultValue = "";
                boolean required = false;
                org.wso2.carbon.configuration.annotations.Element fieldElem = field.getAnnotation(org.wso2.carbon
                        .configuration.annotations.Element.class);
                if (fieldElem != null) {
                    if (!fieldElem.defaultValue().equals(org.wso2.carbon.configuration.annotations.Element.NULL)) {
                        defaultValue = fieldElem.defaultValue();
                    }
                    required = fieldElem.required();
                }

                if (required) {
                    defaultValue = defaultValue + MANDATORY_FIELD_COMMENT;
                }
                addFieldDescription(elementMap, field);
                elementMap.put(field.getSimpleName().toString(), defaultValue);
            }
        }
        return elementMap;
    }

    private void addFieldDescription(Map<String, Object> elementMap, VariableElement field) {
        org.wso2.carbon.configuration.annotations.Element fieldElem = field.getAnnotation(org.wso2.carbon
                .configuration.annotations.Element.class);
        if (fieldElem != null) {
            String description = fieldElem.description();
            if (!description.equals(org.wso2.carbon.configuration.annotations.Element.NULL)) {
                elementMap.put(COMMENT_KEY_PREFIX + field.getSimpleName().toString(),
                        createDescriptionComment(description));
            }
        }
    }

    private Object[] getElementArrayObject(Set<Element> configSet, TypeMirror argumentType) {
        Element argElement = null;
        if (argumentType.getKind() == TypeKind.DECLARED) {
            argElement = ((DeclaredType) argumentType).asElement();
        }
        Object[] elementArray;
        if (configSet.contains(argElement)) {
            elementArray = new HashMap[1];
            elementArray[0] = readConfigurationElements(argElement, configSet);
        } else {
            elementArray = new Object[1];
            elementArray[0] = "# add possible values here";
        }
        return elementArray;
    }

    private String createDescriptionComment(String description) {
        StringBuilder builder = new StringBuilder();
        String lines[] = description.split(NEW_LINE_REGEX_PATTERN);
        for (String line : lines) {
            builder.append("# " + line + "\n");
        }
        return builder.toString();
    }
}
