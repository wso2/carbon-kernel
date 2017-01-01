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
package org.wso2.carbon.extensions.configuration.maven.plugin;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;
import org.wso2.carbon.kernel.annotations.Ignore;
import org.wso2.carbon.kernel.annotations.processor.ConfigurationProcessor;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;


/**
 * This class will create configuration document from bean class annotated in the project.
 * Get all configuration bean classes in the project from the resource created from ConfigurationProcessor
 *
 * @since 5.2.0
 */
@Mojo(name = "create-doc")
public class ConfigDocumentMojo extends AbstractMojo {

    private static final Logger logger = LoggerFactory.getLogger(ConfigDocumentMojo.class.getName());
    private static final String YAML_FILE_EXTENTION = ".yaml";
    private static final String NEW_LINE_REGEX_PATTERN = "\\r?\\n";
    private static final String COMMENT_KEY_PREFIX = "comment-";
    private static final String COMMENT_KEY_REGEX_PATTERN = COMMENT_KEY_PREFIX + ".*";
    private static final String EMPTY_LINE_REGEX_PATTERN = "(?m)^[ \t]*\r?\n";
    private static final String MANDATORY_FIELD_COMMENT = "# THIS IS A MANDATORY FIELD";
    private static final String UTF_8_CHARSET = "UTF-8";
    private static final String PLUGIN_DESCRIPTOR_KEY = "pluginDescriptor";
    private static final String CONFIG_DIR = "config-docs";
    private static final String LICENSE_FILE = "LICENSE.txt";

    @Parameter(defaultValue = "${project}", required = true)
    private MavenProject project;

    @Parameter(property = "configclasses")
    protected String[] configclasses;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // get the qualified names of all configuration bean in the project, if array is empty, return without further
        // processing and we not create any configuration document file.
        String[] configurationClasses = getConfigurationClasses();
        if (configurationClasses == null || configurationClasses.length == 0) {
            logger.info("Configuration classes doesn't exist in the component, hence configuration file not create");
            return;
        }

        List runtimeClasspathElements;
        try {
            runtimeClasspathElements = project.getRuntimeClasspathElements();
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException("Error while getting project classpath elements", e);
        }

        PluginDescriptor descriptor = (PluginDescriptor) getPluginContext().get(PLUGIN_DESCRIPTOR_KEY);
        ClassRealm realm = descriptor.getClassRealm();

        for (Object element : runtimeClasspathElements) {
            File elementFile = new File((String) element);
            try {
                realm.addURL(elementFile.toURI().toURL());
            } catch (MalformedURLException e) {
                logger.error("Error while adding URI: " + elementFile.toURI().toString(), e);
            }
        }

        // process configuration bean to create configuration document
        for (String configClassName : configurationClasses) {
            Map<String, Object> finalMap = new LinkedHashMap<>();
            try {
                Class configClass = realm.loadClass(configClassName);
                if (configClass != null && configClass.isAnnotationPresent(Configuration.class)) {
                    // read configuration annotation
                    Configuration configuration = (Configuration) configClass.getAnnotation(Configuration.class);
                    Object configObject = configClass.newInstance();
                    // add description comment to the root node.
                    finalMap.put(COMMENT_KEY_PREFIX + configuration.namespace(), createDescriptionComment(configuration
                            .description()));
                    // add root node to the config Map
                    finalMap.put(configuration.namespace(), readConfigurationElements(configObject, Boolean.TRUE));
                    // write configuration map as a yaml file
                    writeConfigurationFile(finalMap, configuration.namespace());
                } else {
                    logger.error("Error while loading the configuration class : " + configClassName);
                }
            } catch (ClassNotFoundException e) {
                logger.error("Error while creating new instance of the class : " + configClassName, e);
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error("Error while initializing the configuration class : " + configClassName, e);
            }
        }
    }

    /**
     * read license header from the resource file(LICENSE.txt) and add copyright year.
     * @return license header
     */
    private String getLicenseHeader() {
        InputStream inputStream = ConfigDocumentMojo.class.getClassLoader().getResourceAsStream(LICENSE_FILE);
        StringBuilder sb = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, UTF_8_CHARSET))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            logger.error("Error while reading the license header file.", e);
        }
        Calendar now = Calendar.getInstance();   // Gets the current date and time
        int year = now.get(Calendar.YEAR);      // The current year as an int
        return String.format(sb.toString(), year);
    }

    /**
     * write configuration map to configuration file.
     * @param finalMap configuration map
     * @param filename filename with out extension.
     * @throws MojoExecutionException
     */
    private void writeConfigurationFile(Map<String, Object> finalMap, String filename) throws MojoExecutionException {
        // create the yaml string from the map
        Yaml yaml = new Yaml();
        String content = yaml.dumpAsMap(finalMap);
        // remove all comments key lines from the content. this was added as key of each field description in the map.
        content = content.replaceAll(COMMENT_KEY_REGEX_PATTERN, "");
        content = content.replaceAll(EMPTY_LINE_REGEX_PATTERN, "");
        File configDir = new File(project.getBuild().getOutputDirectory(), CONFIG_DIR);

        // create config directory inside project output directory to save config files
        if (!configDir.exists() && !configDir.mkdirs()) {
            throw new MojoExecutionException("Error while creating config directory in classpath");
        }

        // write the yaml string to the configuration file in config directory
        try (PrintWriter out = new PrintWriter(new File(configDir.getPath(), filename
                + YAML_FILE_EXTENTION), UTF_8_CHARSET)) {
            out.println(getLicenseHeader());
            out.println(content);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new MojoExecutionException("Error while creating new resource file from the classpath", e);
        }

        // add configguration document to the project resources under config-docs/ directory.
        Resource resource = new Resource();
        resource.setDirectory(configDir.getAbsolutePath());
        resource.setTargetPath(CONFIG_DIR);
        project.addResource(resource);
    }

    /**
     * Read the resource file created by ConfigurationProcessor and create array of qualified names of bean classes.
     * @return Array of qualified Name of configuration beans
     * @throws MojoExecutionException
     */
    private String[] getConfigurationClasses() throws MojoExecutionException {
        String[] classList = null;
        if (configclasses != null && configclasses.length != 0) {
            classList = configclasses;
        } else {
            File configFile = new File(project.getBuild().getOutputDirectory(),
                    ConfigurationProcessor.TEMP_CONFIG_FILE_NAME);
            if (configFile.exists()) {
                try {
                    String content = new Scanner(configFile, UTF_8_CHARSET).useDelimiter("\\Z").next();
                    classList = content.split(",");
                } catch (FileNotFoundException e) {
                    throw new MojoExecutionException("Error while reading the configuration classes file", e);
                }
            }
        }
        return classList;
    }

    /**
     * This method will recursively run through the configuration bean class and create a map with,
     * key : field name
     * value : field value
     * description : field descriptions added in Element annotation. Omitting the description of composite type of an
     * array and argument type of a collection.
     * @param configObject configuration bean object.
     * @param enableDescription flag to enable description of the field. if true, it reads the annotated description
     *                          and add description before each field. omit the description otherwise.
     *                          This is added to omit description of composite type of an array and argument type of
     *                          a collection
     * @return Map of field name and values
     * @throws MojoExecutionException
     */
    private Map<String, Object> readConfigurationElements(Object configObject, boolean enableDescription) throws
            MojoExecutionException {
        if (configObject == null) {
            throw new MojoExecutionException("Error while reading the configuration elements, config object is null");
        }

        Map<String, Object> elementMap = new LinkedHashMap<>();
        Field[] fields = configObject.getClass().getDeclaredFields();

        for (Field field : fields) {
            // if @Ignore, it omits field from the configuration.
            if (field.getAnnotation(Ignore.class) != null) {
                continue;
            }
            // if the field is not accessible, make it accessible to read the value of the field.
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            // read the field type to check whether it is a composite type
            Class fieldTypeClass = field.getType();

            // read the field value from the bean object. IllegalAccessException will not occur, since it made
            // accessible.
            Object fieldValue = null;
            try {
                fieldValue = field.get(configObject);
            } catch (IllegalAccessException e) {
                logger.error("Error while accessing the value of the field: " + field.getName(), e);
            }

            // read the description of the field. if the required flag is set, it appends additional mandatory field
            // comment to the description.
            String fieldDescription = null;
            if (enableDescription && field.isAnnotationPresent(Element.class)) {
                Element element = field.getAnnotation(Element.class);
                fieldDescription = createDescriptionComment(element.description());
                if (element.required()) {
                    fieldDescription = fieldDescription + MANDATORY_FIELD_COMMENT;
                }
            }

            // check whether the field value is null, to avoid further processing, which is not required.
            if (fieldValue == null) {
                elementMap.put(field.getName(), null);
                continue;
            }

            // check whether the field type is another configuration bean
            if (fieldTypeClass != null && fieldTypeClass.isAnnotationPresent(Configuration.class)) {
                Configuration configuration = (Configuration) fieldTypeClass.getAnnotation(Configuration.class);
                fieldDescription = createDescriptionComment(configuration.description());
                fieldValue = readConfigurationElements(fieldValue, Boolean.TRUE);
                // check whether the field type is an enum
            } else if (fieldTypeClass != null && fieldTypeClass.isEnum()) {
                fieldValue = fieldValue.toString();
                // check whether the field type is an array
            } else if (fieldTypeClass != null && fieldTypeClass.isArray()) {
                Class compositeType = fieldTypeClass.getComponentType();
                // check whether the composite type is another configuration bean
                if (compositeType != null && compositeType.isAnnotationPresent(Configuration.class)) {
                    int length = Array.getLength(fieldValue);
                    Object[] elementArray = new Object[length];
                    for (int i = 0; i < length; i++) {
                        Object arrayElement = Array.get(fieldValue, i);
                        elementArray[i] = readConfigurationElements(arrayElement, Boolean.FALSE);
                    }
                    fieldValue = elementArray;
                }
                // check whether the field type is an collection
            } else if (fieldTypeClass != null && Collection.class.isAssignableFrom(fieldTypeClass)) {
                ParameterizedType paramType = (ParameterizedType) field.getGenericType();
                Class<?> argumentType = (Class<?>) paramType.getActualTypeArguments()[0];
                // check whether the argument type is another configuration bean
                if (argumentType != null && argumentType.isAnnotationPresent(Configuration.class)) {
                    final Collection<?> c = (Collection<?>) fieldValue;
                    Object[] elementArray = new Object[c.size()];
                    int i = 0;
                    for (final Object obj : c) {
                        elementArray[i] = readConfigurationElements(obj, Boolean.FALSE);
                        i++;
                    }
                    fieldValue = elementArray;
                }
            } else if (fieldValue instanceof Optional) {
                if (((Optional) fieldValue).isPresent()) {
                    fieldValue = ((Optional) fieldValue).get();
                } else {
                    fieldValue = null;
                }
            }

            // add description of each field, if description not null
            if (fieldDescription != null) {
                elementMap.put(COMMENT_KEY_PREFIX + field.getName(), fieldDescription);
            }
            // add field to the element map
            elementMap.put(field.getName(), fieldValue);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("class name: " + configObject.getClass().getSimpleName() + " | default configurations :: "
                    + elementMap.toString());
        }
        return elementMap;
    }

    /**
     * convert the annotated field description to comment.
     * @param description field description
     * @return comment string
     */
    private String createDescriptionComment(String description) {
        StringBuilder builder = new StringBuilder();
        String lines[] = description.split(NEW_LINE_REGEX_PATTERN);
        for (String line : lines) {
            builder.append("# ").append(line).append("\n");
        }
        return builder.toString();
    }
}
