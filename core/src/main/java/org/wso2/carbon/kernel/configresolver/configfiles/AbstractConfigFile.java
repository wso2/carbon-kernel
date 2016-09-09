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

/**
 * This class is used to return the new config values to the component developer.
 *
 * @since 5.2.0
 */
public abstract class AbstractConfigFile {

    private String content;
    private String canonicalContent;
    private String filename;

    /**
     * This constructor sets the file name that is represented by ${@link AbstractConfigFile}. Once an instance of any
     * sub class is created it is expected to call ${@link AbstractConfigFile#setCanonicalContent(String)} in its
     * constructor to populate the field ${@code canonicalContent}. Canonical content format is XML, which is the
     * default format accepted by the ${@link org.wso2.carbon.kernel.configresolver.ConfigResolver}
     *
     * @param filename filename
     */
    public AbstractConfigFile(String filename) {
        this.filename = filename;
    }

    /**
     * This method returns the content represented by this ${@link AbstractConfigFile}.
     *
     * @return content
     */
    public final String getContent() {
        return content;
    }

    /**
     * An implementation of this method is expected to convert the provided ${@code canonicalContent} in XML in to File
     * specific format and assign it to filed ${@code content}.
     *
     * @param content content in XML format
     */
    protected final void setContent(String content) {
        this.content = content;
    }

    /**
     * An implementation of this method is expected to convert the provided ${@code canonicalContent} in XML in to File
     * specific format and call the ${@link AbstractConfigFile#setContent(String)} to update the file with the
     * new content.
     *
     * @param canonicalContent content in XML format
     */
    public abstract void updateContent(String canonicalContent);

    /**
     * This method returns the ${@code canonicalContent}, which was initialized in the object creation.
     *
     * @return canonicalContent
     */
    public final String getCanonicalContent() {
        return canonicalContent;
    }

    /**
     * This method is expected to be called in the sub class constructor, which set the XML representation of the
     * content into filed ${@code canonicalContent}.
     *
     * @param canonicalContent XML representation of the content
     */
    protected final void setCanonicalContent(String canonicalContent) {
        this.canonicalContent = canonicalContent;
    }

    /**
     * This method returns the filename of the content that is represented by this ${@link AbstractConfigFile}.
     *
     * @return filename
     */
    public String getFilename() {
        return filename;
    }
}
