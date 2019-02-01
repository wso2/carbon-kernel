package org.wso2.ei.config;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    private static final String CONF_PLACEHOLDER_PREFIX = "${";
    private static final String SYS_PROPERTY_PLACEHOLDER_PREFIX = "$sys{";
    private static final String ENV_VAR_PLACEHOLDER_PREFIX = "$env{";
    private static final String PLACEHOLDER_SUFFIX = "}";

    public static void resolve(Map<String, Object> context) throws ValidationException {

        resolveSystemProperties(context);
        resolveEnvVariables(context);
        resolveConfigReferences(context);

    }

    private static void resolveConfigReferences(Map<String, Object> context) throws ValidationException {

        Map<String, Set<String>> unresolvedKeys = new HashMap<>();
        Map<String, Set<String>> valuesToResolve = new HashMap<>();

        context.forEach((k, v) -> {
            if (v instanceof String) {
                String[] fileRefs = StringUtils.substringsBetween((String) v, CONF_PLACEHOLDER_PREFIX,
                        PLACEHOLDER_SUFFIX);
                if (fileRefs != null && fileRefs.length > 0) {
                    for (String ref : fileRefs) {
                        Set<String> dependentKeys = valuesToResolve.getOrDefault(ref, new HashSet<>());
                        Set<String> keysUsed = unresolvedKeys.getOrDefault(k, new HashSet<>());
                        dependentKeys.add(k);
                        keysUsed.add(ref);
                        valuesToResolve.put(ref, dependentKeys);
                        unresolvedKeys.put(k, keysUsed);
                    }

                }
            }
            //todo handle list types
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
            throw new ValidationException("References can't be resolved for " +
                    StringUtils.join(unresolvedKeys.keySet(), ","));
        }
    }

    private static void resolveSystemProperties(Map<String, Object> context) {

        context.replaceAll((s, o) -> {
            if (o instanceof String) {
                return resolveStringWithSysVarPlaceholders((String) o);
            } else if (o instanceof List) {
                ((List<Object>) o).replaceAll(listItem -> {
                    if (listItem instanceof String) {
                        return resolveStringWithSysVarPlaceholders((String) listItem);
                    } else {
                        return listItem;
                    }
                });
                return o;
            }
            return o;
        });
    }

    private static void resolveEnvVariables(Map<String, Object> context) {

        context.replaceAll((s, o) -> {
            if (o instanceof String) {
                return resolveStringWithEnvVarPlaceholders((String) o);
            } else if (o instanceof List) {
                ((List<Object>) o).replaceAll(listItem -> {
                    if (listItem instanceof String) {
                        return resolveStringWithEnvVarPlaceholders((String) listItem);
                    } else {
                        return listItem;
                    }
                });
                return o;
            }
            return o;
        });
    }

    private static void resolvePropertyPlaceholders(Map<String, Object> context, String key,
                                                    Set<String> dependentKeys) throws ValidationException {

        Object value = context.get(key);
        if (Objects.isNull(value)) {
            throw new ValidationException("Configuration with key " + key + " doesn't exist");
        }
        for (String k : dependentKeys) {
            Object existingValue = context.get(k);
            if (value instanceof List) {
                //todo $ref-someOtherString when $ref is a list
                context.put(k, value);
            } else if (existingValue instanceof String) {
                existingValue = ((String) existingValue).replaceAll(Pattern.quote(
                        CONF_PLACEHOLDER_PREFIX + key + PLACEHOLDER_SUFFIX), value.toString());
                context.put(k, existingValue);
            }
        }
    }

    private static String resolveStringWithSysVarPlaceholders(String value) {

        String[] sysRefs = StringUtils.substringsBetween(value, SYS_PROPERTY_PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX);
        if (sysRefs != null) {
            for (String ref : sysRefs) {
                value = value.replaceAll(Pattern.quote(SYS_PROPERTY_PLACEHOLDER_PREFIX + ref + PLACEHOLDER_SUFFIX),
                        System.getProperty(ref));
            }
        }
        return value;
    }

    private static String resolveStringWithEnvVarPlaceholders(String value) {

        String[] envRefs = StringUtils.substringsBetween(value, ENV_VAR_PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX);
        if (envRefs != null) {
            for (String ref : envRefs) {
                value = value.replaceAll(Pattern.quote(ENV_VAR_PLACEHOLDER_PREFIX + ref + PLACEHOLDER_SUFFIX),
                        System.getenv(ref));
            }
        }
        return value;
    }
}
