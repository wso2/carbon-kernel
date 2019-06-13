/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.nextgen.config;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.nextgen.config.model.Context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves placeholder references in the configuration.
 * Placeholders can be added to any of the following categories, and their combinations.
 * <p>
 * ${ref} - Resolves to the 'ref' configuration's value
 * $sys{ref} - Resolves to the 'ref' system property
 * $env{ref} - Resolves to the 'ref' environment variable
 * <p>
 * There can also be combinations of the above.
 * eg.
 * <p>
 * http://$env{hostname}:${port}/somecontext/
 */
public class ReferenceResolver {

    private static final String CONF_PLACEHOLDER_PREFIX = "$ref{";
    private static final String SYS_PROPERTY_PLACEHOLDER_PREFIX = "$sys{";
    private static final String SECRET_PROPERTY_PLACEHOLDER_PREFIX = "$secret{";
    private static final String ENV_VAR_PLACEHOLDER_PREFIX = "$env{";
    private static final String PLACEHOLDER_SUFFIX = "}";
    private static final String PLAIN_TEXT_VALUE_PLACE_HOLDER_PREFIX = "[";
    private static final String PLAIN_TEXT_VALUE_PLACE_HOLDER_SUFFIX = "]";

    private ReferenceResolver() {

    }

    /**
     * Resolves the placeholder strings.
     *
     * @param context Context object
     * @throws ConfigParserException
     */
    public static void resolve(Context context) throws ConfigParserException {

        resolve(context.getTemplateData(), context.getSecrets(), context.getResolvedSystemProperties(),
                context.getResolvedEnvironmentVariables());
    }

    /**
     * Resolves the placeholder strings.
     *
     * @param templateData      template data
     * @param secrets           map of secrets
     * @param resolvedSystemProperties          map of resolved system properties
     * @param resolvedEnvironmentVariables      map of resolved environment varialbles
     * @throws ConfigParserException            Config parser exception
     */
    static void resolve(Map<String, Object> templateData, Map<String, String> secrets, Map<String,
            String> resolvedSystemProperties, Map<String, String> resolvedEnvironmentVariables)
            throws ConfigParserException {

        resolveSecrets(templateData, secrets);
        resolveSystemProperties(templateData, resolvedSystemProperties);
        resolveEnvVariables(templateData, resolvedEnvironmentVariables);
        resolveConfigReferences(templateData);
    }

    /**
     * Resolves the reference with ${ref} which refers to an existing configuration in the context.
     *
     * @param context Configuration context
     * @throws ConfigParserException
     */
    private static void resolveConfigReferences(Map<String, Object> context) throws ConfigParserException {

        Map<String, Set<String>> unresolvedKeys = new LinkedHashMap<>();
        Map<String, Set<String>> valuesToResolve = new LinkedHashMap<>();

        context.forEach((key, value) -> {
            if (value instanceof String) {
                extractPlaceholdersFromString(key, (String) value, unresolvedKeys, valuesToResolve);
            } else if (value instanceof ArrayList) {
                for (Object elementItem : (ArrayList) value) {
                    if (elementItem instanceof String) {
                        extractPlaceholdersFromString(key, (String) elementItem, unresolvedKeys, valuesToResolve);
                    }
                }
            }
        });

        boolean atLeastOneResolved = true;

        while (!valuesToResolve.isEmpty() && atLeastOneResolved) {
            atLeastOneResolved = false;
            Set<String> resolvedInIteration = new HashSet<>();
            for (Map.Entry<String, Set<String>> entry : valuesToResolve.entrySet()) {
                if (!unresolvedKeys.containsKey(entry.getKey())) {
                    resolvePropertyPlaceholders(context, entry.getKey(), entry.getValue());
                    resolvedInIteration.add(entry.getKey());
                    atLeastOneResolved = true;
                }
            }
            unresolvedKeys.forEach((key, value) -> value.removeIf(key1 -> !unresolvedKeys.containsKey(key1)));
            unresolvedKeys.keySet().removeIf(entry -> unresolvedKeys.get(entry).isEmpty());
            valuesToResolve.keySet().removeAll(resolvedInIteration);
        }

        if (!valuesToResolve.isEmpty()) {
            throw new ConfigParserException("References can't be resolved for " +
                    StringUtils.join(unresolvedKeys.keySet(), ","));
        }
    }

    /**
     * Extract the placeholder references in the context.
     *
     * @param key             The key of the configuration
     * @param value           The value of the configuration
     * @param unresolvedKeys  The map which maintains keys with placeholders and on which config they depend on
     * @param valuesToResolve The map which maintains the placeholder references and the keys that depend on those
     */
    private static void extractPlaceholdersFromString(String key, String value, Map<String, Set<String>> unresolvedKeys,
                                                      Map<String, Set<String>> valuesToResolve) {

        String[] fileRefs = StringUtils.substringsBetween(value, CONF_PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX);
        if (fileRefs != null && fileRefs.length > 0) {
            for (String ref : fileRefs) {
                Set<String> dependentKeys = valuesToResolve.getOrDefault(ref, new HashSet<>());
                Set<String> keysUsed = unresolvedKeys.getOrDefault(key, new HashSet<>());
                dependentKeys.add(key);
                keysUsed.add(ref);
                valuesToResolve.put(ref, dependentKeys);
                unresolvedKeys.put(key, keysUsed);
            }
        }
    }

    /**
     * Resolves system property references ($sys{ref}).
     *
     * @param context The configuration context
     */
    private static void resolveSystemProperties(Map<String, Object> context, Map references)
            throws ConfigParserException {

        for (Map.Entry<String, Object> entry : context.entrySet()) {
            if (entry.getValue() instanceof String) {
                context.replace(entry.getKey(), resolveStringWithSysVarPlaceholders((String) entry.getValue(),
                        references));
            } else if (entry.getValue() instanceof List) {
                ListIterator listIterator = ((List) entry.getValue()).listIterator();
                while (listIterator.hasNext()) {
                    Object value = listIterator.next();
                    if (value instanceof String) {
                        listIterator.remove();
                        listIterator.add(resolveStringWithSysVarPlaceholders((String) value, references));
                    }
                }
            }

        }
    }

    /**
     * Resolves system property references ($sys{ref}).
     *
     * @param context The configuration context
     * @param secrets
     */
    private static void resolveSecrets(Map<String, Object> context, Map secrets) throws ConfigParserException {

        boolean enabledSecret = false;
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                enabledSecret = !(!enabledSecret && !resolveStringWithSecretPlaceHolders(value, secrets));
            } else if (value instanceof List) {
                for (Object v : (List) value) {
                    if (v instanceof String) {
                        enabledSecret = !(!enabledSecret && !resolveStringWithSecretPlaceHolders(v, secrets));
                    } else if (v instanceof Map) {
                        Set<Map.Entry> entries = ((Map) v).entrySet();
                        for (Map.Entry entrySet : entries) {
                            enabledSecret = !(!enabledSecret &&
                                    !resolveStringWithSecretPlaceHolders(entrySet.getValue(), secrets));
                        }
                    }
                }
            }
        }
        System.setProperty(ConfigConstants.ENABLE_SEC_VAULT, String.valueOf(enabledSecret));
    }

    private static boolean resolveStringWithSecretPlaceHolders(Object value, Map secrets)
            throws ConfigParserException {

        boolean exists = false;

        if (value instanceof String) {
            String[] secretRefs = StringUtils.substringsBetween((String) value, SECRET_PROPERTY_PLACEHOLDER_PREFIX,
                    PLACEHOLDER_SUFFIX);
            if (secretRefs != null) {
                for (String secretRef : secretRefs) {
                    Object secretValue = secrets.get(secretRef);
                    if (secretValue == null) {
                        throw new ConfigParserException("Secret references can't be resolved for " + secretRef);
                    } else if (!(!(secretValue instanceof String)
                            || Boolean.getBoolean(ConfigConstants.ENCRYPT_SECRETS))) {
                        String[] secretArray = StringUtils.substringsBetween((String) secretValue,
                                PLAIN_TEXT_VALUE_PLACE_HOLDER_PREFIX, PLAIN_TEXT_VALUE_PLACE_HOLDER_SUFFIX);
                        if (secretArray != null && secretArray.length > 0) {
                            throw new ConfigParserException("Secret references can't be plain text for " + secretRef);
                        }
                    }
                    exists = true;
                }
            }
        }
        return exists;
    }

    /**
     * Resolves environment variable references ($env{ref}).
     *
     * @param context    The configuration context
     * @param references Resolved Environment Variables;
     * @throws ConfigParserException if Environment Variable not defined
     */
    private static void resolveEnvVariables(Map<String, Object> context, Map<String, String> references)
            throws ConfigParserException {

        for (Map.Entry<String, Object> entry : context.entrySet()) {
            if (entry.getValue() instanceof String) {
                context.replace(entry.getKey(), resolveStringWithEnvVarPlaceholders((String) entry.getValue(),
                        references));
            } else if (entry.getValue() instanceof List) {
                ListIterator values = ((List) entry.getValue()).listIterator();
                while (values.hasNext()) {
                    Object value = values.next();
                    if (value instanceof String) {
                        values.remove();
                        values.add(resolveStringWithEnvVarPlaceholders((String) value, references));
                    }
                }
            }
        }
    }

    /**
     * Resolves the configuration values that depends on already resolved placeholders.
     *
     * @param context       The configuration context
     * @param key           The resolved key on which other keys depend on
     * @param dependentKeys The keys that depends on <code>key</code>
     * @throws ConfigParserException
     */
    private static void resolvePropertyPlaceholders(Map<String, Object> context, String key,
                                                    Set<String> dependentKeys) throws ConfigParserException {

        Object value = context.get(key);
        if (Objects.isNull(value)) {
            throw new ConfigParserException("Configuration with key " + key + " doesn't exist");
        }
        for (String k : dependentKeys) {
            Object existingValue = context.get(k);
            if (value instanceof List) {
                context.put(k, value);
            } else if (value instanceof Boolean) {
                context.put(k, value);
            } else if (existingValue instanceof String) {
                existingValue = ((String) existingValue).replaceAll(Pattern.quote(
                        CONF_PLACEHOLDER_PREFIX + key + PLACEHOLDER_SUFFIX),
                        Matcher.quoteReplacement(value.toString()));
                context.put(k, existingValue);
            } else if (existingValue instanceof  ArrayList) {
                ArrayList<String> modifiedValueList = new ArrayList<>();
                for (String item : (ArrayList<String>) existingValue) {
                    item = item.replaceAll(Pattern.quote(
                            CONF_PLACEHOLDER_PREFIX + key + PLACEHOLDER_SUFFIX),
                                           Matcher.quoteReplacement(value.toString()));
                    modifiedValueList.add(item);
                }
                context.put(k, modifiedValueList);
            }
        }
    }

    /**
     * Resolves system property references ($sys{ref}) in an individual string.
     *
     * @param value                    string with system property reference
     * @param resolvedSystemProperties resolved System Properties
     * @return the value with system properties references resolved
     */
    private static String resolveStringWithSysVarPlaceholders(String value, Map resolvedSystemProperties)
            throws ConfigParserException {

        String[] sysRefs = StringUtils.substringsBetween(value, SYS_PROPERTY_PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX);
        if (sysRefs != null) {
            for (String ref : sysRefs) {
                String property = System.getProperty(ref);
                if (StringUtils.isNotEmpty(property)) {
                    resolvedSystemProperties.put(ref, property);
                    value = value.replaceAll(Pattern.quote(SYS_PROPERTY_PLACEHOLDER_PREFIX + ref + PLACEHOLDER_SUFFIX),
                            property);
                } else {
                    throw new ConfigParserException("Error while retrieving " + ref + " system property");
                }
            }
        }
        return value;
    }

    /**
     * Resolves environment variable references ($env{ref}) in an individual string.
     *
     * @param value string with environment variable reference
     * @return the value with environment variables references resolved
     */
    private static String resolveStringWithEnvVarPlaceholders(String value, Map resolvedEnvironmentVariables)
            throws ConfigParserException {

        String[] envRefs = StringUtils.substringsBetween(value, ENV_VAR_PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX);
        if (envRefs != null) {
            for (String ref : envRefs) {
                String resolvedValue = System.getenv(ref);
                if (resolvedValue != null) {
                    resolvedEnvironmentVariables.put(ref, resolvedValue);
                    value = value.replaceAll(Pattern.quote(ENV_VAR_PLACEHOLDER_PREFIX + ref + PLACEHOLDER_SUFFIX),
                            resolvedValue);
                } else {
                    throw new ConfigParserException("Environment variable " + ref + " not defined in system");
                }
            }
        }
        return value;
    }
}
