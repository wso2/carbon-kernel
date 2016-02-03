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
package org.wso2.carbon.datasource.rdbms.utils;

import org.wso2.carbon.datasource.core.common.DataSourceException;
import org.wso2.carbon.datasource.rdbms.RDBMSConfiguration;
import org.wso2.carbon.datasource.rdbms.RDBMSConfiguration.DataSourceProperty;
import org.wso2.carbon.datasource.rdbms.RDBMSDataSourceConstants;
import org.wso2.carbon.datasource.utils.DataSourceUtils;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import javax.xml.bind.JAXBContext;

/**
 * Utility class for RDBMS data sources.
 */
public class RDBMSDataSourceUtils {

    public static RDBMSConfiguration loadConfig(String xmlConfiguration)
            throws DataSourceException {
        try {
            xmlConfiguration = DataSourceUtils.replaceSystemVariablesInXml(xmlConfiguration);
            JAXBContext ctx = JAXBContext.newInstance(RDBMSConfiguration.class);
            return (RDBMSConfiguration) ctx.createUnmarshaller().unmarshal(
                    new ByteArrayInputStream(xmlConfiguration.getBytes()));
        } catch (Exception e) {
            throw new DataSourceException("Error in loading RDBMS configuration: " +
                    e.getMessage(), e);
        }
    }

    public static void assignBeanProps(Object obj, Map<String, Object> props)
            throws DataSourceException {
        Method method;
        for (Entry<String, Object> prop : props.entrySet()) {
            method = getSetterMethod(obj, getSetterMethodNameFromPropName(prop.getKey()));
            if (method == null) {
                throw new DataSourceException("Setter method for property '" + prop.getKey()
                        + "' cannot be found");
            }
            try {
                method.invoke(obj, convertStringToGivenType(prop.getValue(),
                        method.getParameterTypes()[0]));
            } catch (Exception e) {
                throw new DataSourceException("Cannot invoke setter for property '" +
                        prop.getKey() + "'", e);
            }
        }
    }

    private static Object convertStringToGivenType(Object value, Class<?> type)
            throws DataSourceException {
        if (String.class.equals(type) || Properties.class.equals(type)) {
            return value;
        }
        if (boolean.class.equals(type) || Boolean.class.equals(type)) {
            return Boolean.parseBoolean(String.valueOf(value));
        }
        if (int.class.equals(type) || Integer.class.equals(type)) {
            return Integer.parseInt(String.valueOf(value));
        }
        if (short.class.equals(type) || Short.class.equals(type)) {
            return Short.parseShort(String.valueOf(value));
        }
        if (byte.class.equals(type) || Byte.class.equals(type)) {
            return Byte.parseByte(String.valueOf(value));
        }
        if (long.class.equals(type) || Long.class.equals(type)) {
            return Long.parseLong(String.valueOf(value));
        }
        if (float.class.equals(type) || Float.class.equals(type)) {
            return Float.parseFloat(String.valueOf(value));
        }
        if (double.class.equals(type) || Double.class.equals(type)) {
            return Double.parseDouble(String.valueOf(value));
        }
        throw new DataSourceException("Cannot convert value: '" +
                value + "' to type: '" + type.getName() + "'");
    }

    public static boolean isEmptyString(String text) {
        return !(text != null && text.trim().length() > 0);
//        if (text != null && text.trim().length() > 0) {
//            return false;
//        } else {
//            return true;
//        }
    }

    private static String getSetterMethodNameFromPropName(String propName) throws RuntimeException {
        if (isEmptyString(propName)) {
            throw new RuntimeException("Invalid property name");
        }
        return "set" + propName.substring(0, 1).toUpperCase() + propName.substring(1);
    }

    private static Method getSetterMethod(Object obj, String name) {
        Method[] methods = obj.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(name)
                    && method.getReturnType().equals(void.class)
                    && method.getParameterTypes().length == 1) {
                return method;
            }
        }
        return null;
    }

    public static Map<String, String> extractPrimitiveFieldNameValuePairs(Object object)
            throws DataSourceException {
        Map<String, String> nameValueMap = new HashMap<>();
        Method methods[] = object.getClass().getMethods();
        for (Method method : methods) {
            if (isMethodMatched(method)) {
                String FieldName = getFieldNameFromMethodName(method.getName());
                try {
                    if (method.invoke(object) != null) {
                        String result = method.invoke(object).toString();
                        nameValueMap.put(FieldName, result);
                    }
                } catch (Exception e) {
                    throw new DataSourceException(
                            "Error in retrieving " + FieldName + " value from the object :" + object.getClass() + e.getMessage(), e);
                }
            }
        }
        return nameValueMap;
    }

    private static String getFieldNameFromMethodName(String name) throws DataSourceException {
        String prefixGet = "get";
        String prefixIs = "is";
        String firstLetter = null;

        if (name.startsWith(prefixGet)) {
            firstLetter = name.substring(3, 4);
            name = name.substring(4);
        } else if (name.startsWith(prefixIs)) {
            firstLetter = name.substring(2, 3);
            name = name.substring(3);
        } else {
            throw new DataSourceException("Error in retrieving attribute name from method : "
                    + name);
        }
        firstLetter = firstLetter.toLowerCase();
        return firstLetter.concat(name);
    }

    private static boolean isMethodMatched(Method method) {
        String returnType = method.getReturnType().getSimpleName();
        String methodName = method.getName();

        if (!Modifier.isPublic(method.getModifiers())) {
            return false;
        }
        if (returnType.equals("void")) {
            return false;
        }
        if (!(methodName.startsWith("get") ||
                (methodName.startsWith("is") && (returnType.equals("Boolean") || returnType.equals("boolean"))))) {
            return false;
        }
        if (!(method.getReturnType().isPrimitive() ||
                Arrays.asList(RDBMSDataSourceConstants.CLASS_RETURN_TYPES).contains(returnType))) {
            return false;
        }
        return true;
    }

    public static Map<String, Object> dataSourcePropsToMap(List<DataSourceProperty> dsProps) {
        Map<String, Object> result = new HashMap<>();
        if (dsProps != null) {
            String[] prop;
            Map<String, Properties> tmpPropertiesObjects = new HashMap<>();
            Properties tmpProp;
            for (DataSourceProperty dsProp : dsProps) {
                prop = dsProp.getName().split("\\.");
                if (prop.length > 1) {
                    if (!tmpPropertiesObjects.containsKey(prop[0])) {
                        tmpProp = new Properties();
                        tmpPropertiesObjects.put(prop[0], tmpProp);
                    } else {
                        tmpProp = tmpPropertiesObjects.get(prop[0]);
                    }
                    tmpProp.setProperty(prop[1], dsProp.getValue());
                } else {
                    result.put(dsProp.getName(), dsProp.getValue());
                }
            }
            result.putAll(tmpPropertiesObjects);
        }
        return result;
    }


}
