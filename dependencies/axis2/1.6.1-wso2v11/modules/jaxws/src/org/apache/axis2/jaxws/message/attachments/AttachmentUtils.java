/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.jaxws.message.attachments;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.attachments.CachedFileDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMText;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.namespace.QName;
import java.io.File;

/** A suite of utilities used for handling MTOM attachment data. */
public class AttachmentUtils {

    private static final Log log = LogFactory.getLog(AttachmentUtils.class);
    private static final QName XOP_INCLUDE =
            new QName("http://www.w3.org/2004/08/xop/include", "Include");

    /**
     * Given an <pre><xop:include></pre> element, create an OMText element
     * with the appropriate attachment data.
     *
     * @param xop
     * @param data
     * @return
     */
    public static OMText makeBinaryOMNode(OMElement xop, DataHandler dh) {
        OMFactory factory = xop.getOMFactory();
        OMText binaryNode = factory.createOMText(dh, true);
        return binaryNode;
    }

    /**
     * Given an OMText node, create it's corresponding <pre><xop:include></pre>
     * element.
     */
    public static OMElement makeXopElement(OMText data) {
        OMFactory factory = data.getOMFactory();
        OMElement xop = factory.createOMElement(XOP_INCLUDE, null);
        xop.addAttribute("href", data.getContentID(), null);
        return xop;
    }
    
    /**
     * Process attachment's dataHandlers for CachedFileDataSource.
     * If exist, execute file.deleteOnExit() request on the cached
     * attachment file referenced by each CachedFileDataSource.
     * This will delete the cached attachment file on JVM exit.
     * 
     * @param attachments
     */
    public static void findCachedAttachment(Attachments attachments){
    	if(attachments == null){
    		return;
    	}
    	
    	String[] contentIds = attachments.getAllContentIDs();
    	if(contentIds.length > 0){
            if (log.isDebugEnabled()) {
            	log.debug("Attachments exist....");
            }
        	for(int i=0; i < contentIds.length; i++){
        		DataHandler dh = attachments.getDataHandler(contentIds[i]);
        		if(dh != null){
        			DataSource dataSource = dh.getDataSource();
        			if(dh != null && dataSource instanceof CachedFileDataSource){
        				if (log.isDebugEnabled()) {
                        	log.debug("Attachment's DataHandler uses CachedFileDataSource...");
                        }
                    	File file = ((CachedFileDataSource)dataSource).getFile();
                    	if (log.isDebugEnabled()) {
                        	log.debug(" Making file.deleteOnExit() request on "+file.getAbsolutePath());
                        }
                    	file.deleteOnExit();
                    }
        		}
        	}
        }
    }

}
