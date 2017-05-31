/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.utils.component.xml;

public interface ComponentConstants {

    //OMElement local names
    String ELE_DEPLOYERS = "deployers";
    String ELE_DEPLOYER = "deployer";
    String ELE_DIRECTORY = "directory";
    String ELE_EXTENSION = "extension";

    String ELE_HTTP_GET_REQUEST_PROCESSORS = "httpGetRequestProcessors";
    String ELE_PROCESSOR = "processor";
    String ELE_ITEM = "item";

    String ELE_COMPONENT_BUILDERS = "componentBuilders";
    String ELE_COMPONENT_BUILDER = "componentBuilder";
    String ELE_CLASS = "class";
    String ELE_ID = "id";

    //namespaces
    String NS_WSO2CARBON = "http://products.wso2.org/carbon";

    String DEPLOYER_CONFIG = ELE_DEPLOYERS;
    String HTTP_GET_REQUEST_PROCESSORS = ELE_HTTP_GET_REQUEST_PROCESSORS;

}
