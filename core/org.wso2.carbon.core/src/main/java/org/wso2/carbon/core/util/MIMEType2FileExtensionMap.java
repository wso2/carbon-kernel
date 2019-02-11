/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.core.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @scr.component name="MIMEType2FileExtensionMap.component"
 * @scr.service interface="org.wso2.carbon.core.util.MIMEType2FileExtensionMap"
 */
public class MIMEType2FileExtensionMap {

    public static final String mappingFileName = "org/wso2/carbon/core/util/mime-mappings.xml";

    private Map<String, String> extensionToMimeMap = new HashMap<String, String>();

    private static Log log = LogFactory.getLog(MIMEType2FileExtensionMap.class);

    public MIMEType2FileExtensionMap() {
        try {
            URL resource = this.getClass().getClassLoader().getResource(mappingFileName);
            if (resource == null) {
                String msg = "Unable to load all MIME Mappings";
                log.error(msg);
                throw new RuntimeException(msg);
            }
            InputStream in = resource.openStream();
            OMElement doc = new StAXOMBuilder(in).getDocumentElement();
            for (Iterator childIter = doc.getChildElements(); childIter.hasNext();) {
                OMElement mappingEle = (OMElement) childIter.next();
                String ext = mappingEle.getFirstChildWithName(new QName("Extension")).getText();
                String mimeType = mappingEle.getFirstChildWithName(new QName("MimeType")).getText();
                extensionToMimeMap.put(ext, mimeType);
            }
        } catch (Exception e) {
            String msg = "Unable to load all MIME Mappings";
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    public String getMIMEType(File file) {
        String filename = file.getName();
        int index = filename.lastIndexOf('.');
        if (index == -1 || index == filename.length() - 1) {
            return "application/octet-stream";
        } else {
            String extension = filename.substring(index + 1);
            String contentType = extensionToMimeMap.get(extension);
            return contentType != null ? contentType : "application/octet-stream";
        }
    }

    /**
     * @deprecated No need to call init at all
     * @param context
     */
    public void init(BundleContext context) {

    }
}
