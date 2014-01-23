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

package org.apache.axis2.util;

import org.apache.axiom.attachments.utils.IOUtils;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An XML pretty printer based on xsl stylesheets
 */
public class XMLPrettyPrinter {

    private static final Log log = LogFactory.getLog(XMLPrettyPrinter.class);

    /**
     * Pretty prints contents of the xml file.
     *
     * @param file
     */
    public static void prettify(final File file) {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        byte[] byteArray = null;
        try {
            FileInputStream fin = new FileInputStream(file);
            byteArray = IOUtils.getStreamAsByteArray(fin);
            fin.close();
            inputStream = new ByteArrayInputStream(byteArray);
            outputStream = new FileOutputStream(file);

            Source stylesheetSource = new StreamSource(new ByteArrayInputStream(prettyPrintStylesheet.getBytes()));
            Source xmlSource = new StreamSource(inputStream);

            TransformerFactory tf = TransformerFactory.newInstance();
            Templates templates = tf.newTemplates(stylesheetSource);
            Transformer transformer = templates.newTransformer();
            transformer.setErrorListener(new ErrorListener(){
                public void warning(TransformerException exception) throws TransformerException {
                    log.warn("Exception occurred while trying to pretty print file " + file, exception);
                }

                public void error(TransformerException exception) throws TransformerException {
                    log.error("Exception occurred while trying to pretty print file " + file, exception);
                }

                public void fatalError(TransformerException exception) throws TransformerException {
                    log.error("Exception occurred while trying to pretty print file " + file, exception);
                }
            });
            transformer.transform(xmlSource, new StreamResult(outputStream));

            inputStream.close();
            outputStream.close();
            log.debug("Pretty printed file : " + file);
        } catch (Throwable t) {
            log.debug("Exception occurred while trying to pretty print file " + file, t);
            
            /* if outputStream is already created, close them, because we are going reassign
             * different value to that. It will leak the file handle (specially in windows, since
             * deleting is going to be an issue)
             */
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.debug(e.getMessage(), e);
                }
            }
            try {
                if (byteArray != null) {
                    outputStream = new FileOutputStream(file);
                    outputStream.write(byteArray);
                }
            } catch (IOException e) {
                log.debug(e.getMessage(), e);
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.debug(e.getMessage(), e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.debug(e.getMessage(), e);
                }
            }
        }
    }


    private static final String prettyPrintStylesheet =
                     "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0' " +
                             " xmlns:xalan='http://xml.apache.org/xslt' " +
                             " exclude-result-prefixes='xalan'>" +
                     "  <xsl:output method='xml' indent='yes' xalan:indent-amount='4'/>" +
                     "  <xsl:strip-space elements='*'/>" +
                     "  <xsl:template match='/'>" +
                     "    <xsl:apply-templates/>" +
                     "  </xsl:template>" +
                     "  <xsl:template match='node() | @*'>" +
                     "        <xsl:copy>" +
                     "          <xsl:apply-templates select='node() | @*'/>" +
                     "        </xsl:copy>" +
                     "  </xsl:template>" +
                     "</xsl:stylesheet>";

    public static void prettify(OMElement wsdlElement, OutputStream out) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        wsdlElement.serialize(baos);

        Source stylesheetSource = new StreamSource(new ByteArrayInputStream(prettyPrintStylesheet.getBytes()));
        Source xmlSource = new StreamSource(new ByteArrayInputStream(baos.toByteArray()));

        TransformerFactory tf = TransformerFactory.newInstance();
        Templates templates = tf.newTemplates(stylesheetSource);
        Transformer transformer = templates.newTransformer();
        transformer.transform(xmlSource, new StreamResult(out));
    }
}
