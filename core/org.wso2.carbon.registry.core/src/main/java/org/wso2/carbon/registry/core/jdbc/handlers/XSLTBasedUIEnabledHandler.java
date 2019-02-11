/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.core.jdbc.handlers;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public class XSLTBasedUIEnabledHandler extends UIEnabledHandler {

    private static Log log = LogFactory.getLog(XSLTBasedUIEnabledHandler.class);

    protected Map<String, String> browseXSLTs = new HashMap<String, String>();
    protected Map<String, String> editXSLTs = new HashMap<String, String>();
    protected Map<String, String> newHTMLs = new HashMap<String, String>();

    protected List<String> browseViews = new ArrayList<String>();
    protected List<String> editViews = new ArrayList<String>();
    protected List<String> newViews = new ArrayList<String>();

    private TransformerFactory transformerFactory;

    public XSLTBasedUIEnabledHandler() {
        transformerFactory = TransformerFactory.newInstance();
    }

    public void setBrowseXSLT(OMElement browseElement) throws RegistryException {

        OMElement viewKeyElement = browseElement.getFirstChildWithName(new QName("viewKey"));
        OMElement viewXSLTElement = browseElement.getFirstChildWithName(new QName("xsltPath"));
        OMElement viewNameElement = browseElement.getFirstChildWithName(new QName("viewName"));

        if ("true".equals(browseElement.getAttributeValue(new QName("default")))) {
            setDefaultBrowseView(viewKeyElement.getText().trim());
        }

        if (viewKeyElement == null || viewXSLTElement == null) {
            String msg = getConfigFormatErrorMessage();
            log.error(msg);
            throw new RegistryException(msg);
        }

        String viewKey = viewKeyElement.getText().trim();
        String viewXSLT = viewXSLTElement.getText().trim();
        browseXSLTs.put(viewKey, viewXSLT);

        String browseView;
        if (viewNameElement != null) {
            browseView = viewKey + ":" + viewNameElement.getText().trim();
        } else {
            browseView = viewKey;
        }
        browseViews.add(browseView);
    }

    public void setEditXSLT(OMElement editElement) throws RegistryException {

        OMElement viewKeyElement = editElement.getFirstChildWithName(new QName("viewKey"));
        OMElement viewXSLTElement = editElement.getFirstChildWithName(new QName("xsltPath"));
        OMElement viewNameElement = editElement.getFirstChildWithName(new QName("viewName"));

        if ("true".equals(editElement.getAttributeValue(new QName("default")))) {
            setDefaultEditView(viewKeyElement.getText().trim());
        }

        if (viewKeyElement == null || viewXSLTElement == null) {
            String msg = getConfigFormatErrorMessage();
            log.error(msg);
            throw new RegistryException(msg);
        }

        String viewKey = viewKeyElement.getText().trim();
        String viewXSLT = viewXSLTElement.getText().trim();
        editXSLTs.put(viewKey, viewXSLT);

        String editView;
        if (viewNameElement != null) {
            editView = viewKey + ":" + viewNameElement.getText().trim();
        } else {
            editView = viewKey;
        }
        editViews.add(editView);
    }

    public void setNewHTML(OMElement newElement) throws RegistryException {

        OMElement viewKeyElement = newElement.getFirstChildWithName(new QName("viewKey"));
        OMElement viewHTMLElement = newElement.getFirstChildWithName(new QName("htmlPath"));
        OMElement viewNameElement = newElement.getFirstChildWithName(new QName("viewName"));

        if ("true".equals(newElement.getAttributeValue(new QName("default")))) {
            setDefaultNewView(viewKeyElement.getText().trim());
        }

        if (viewKeyElement == null || viewHTMLElement == null) {
            String msg = getConfigFormatErrorMessage();
            log.error(msg);
            throw new RegistryException(msg);
        }

        String viewKey = viewKeyElement.getText().trim();
        String viewXSLT = viewHTMLElement.getText().trim();
        newHTMLs.put(viewKey, viewXSLT);

        String newView;
        if (viewNameElement != null) {
            newView = viewKey + ":" + viewNameElement.getText().trim();
        } else {
            newView = viewKey;
        }
        newViews.add(newView);
    }

    public String[] getBrowseViews() {
        return browseViews.toArray(new String[browseViews.size()]);
    }

    public String[] getEditViews() {
        return editViews.toArray(new String[editViews.size()]);
    }

    public String[] getNewViews() {
        return newViews.toArray(new String[newViews.size()]);
    }

    public Resource getBrowseView(String viewKey, RequestContext requestContext)
            throws RegistryException {

        String xslt = browseXSLTs.get(viewKey);
        if (xslt == null) {
            String msg = "Unsupported browse view: " + viewKey +
                    ". XSLT is not registered for this view.";
            log.error(msg);
            throw new RegistryException(msg);
        }
        Transformer viewTransformer = getTransformer(xslt);

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        Resource resource =
                requestContext.getRegistry().get(requestContext.getResourcePath().getPath());

        try {
            viewTransformer.setParameter("resourcePath",
                    requestContext.getResourcePath().toString());
            viewTransformer.transform(
                    new StreamSource(resource.getContentStream()), new StreamResult(byteOut));

        } catch (TransformerException e) {
            String msg = "Failed to generate the view UI for resource " +
                    requestContext.getResourcePath() + ". XSLT transformation failed for XSLT " +
                    xslt + ". " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }

        try {
            String content = byteOut.toString();
            resource.setContent(content);
        } finally {
                try {
                    byteOut.close();
                } catch (IOException e) {
                    log.error("Failed to close the stream", e);
                }

        }

        return resource;
    }

    public Resource getEditView(String editViewKey, RequestContext requestContext)
            throws RegistryException {

        String xslt = editXSLTs.get(editViewKey);
        if (xslt == null) {
            String msg = "Unsupported edit view: " + editViewKey +
                    ". XSLT is not registered for this view.";
            log.error(msg);
            throw new RegistryException(msg);
        }
        Transformer viewTransformer = getTransformer(xslt);

        if (viewTransformer == null) {
            return getRawResource(requestContext);
        }

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        Resource resource =
                requestContext.getRegistry().get(requestContext.getResourcePath().getPath());

        try {
            viewTransformer.setParameter(
                    "resourcePath", requestContext.getResourcePath().getPath());
            viewTransformer.transform(
                    new StreamSource(resource.getContentStream()), new StreamResult(byteOut));

        } catch (TransformerException e) {
            String msg = "Failed to generate the edit UI for resource " +
                    requestContext.getResourcePath() + ". XSLT transformation failed for XSLT " +
                    xslt + ". " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }

        try {
            String content = byteOut.toString();
            resource.setContent(content);
        } finally {
                try {
                    byteOut.close();
                } catch (IOException e) {
                    log.error("Failed to close the stream", e);
                }

        }

        return resource;
    }

    public Resource getNewView(String newViewKey, RequestContext requestContext)
            throws RegistryException {

        String htmlPath = newHTMLs.get(newViewKey);
        if (htmlPath == null) {
            String msg = "Unsupported resource creation view: " + newViewKey +
                    ". HTML is not registered for this view.";
            log.error(msg);
            throw new RegistryException(msg);
        }

        InputStream htmlStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream(htmlPath);
        if (htmlStream == null) {
            String msg = "Could not locate the HTML file for generating the custom UI. " +
                    "Make sure that the file " + htmlPath +
                    " is in the class path of the WSO2 Registry.";
            log.error(msg);
            throw new RegistryException(msg);
        }

        Resource resource;
        try {
            String content = streamToString(htmlStream);

            resource = requestContext.getRegistry().newResource();
            resource.setContent(content);
        } finally {
                try {
                    htmlStream.close();
                } catch (IOException e) {
                    log.error("Failed to close the stream", e);
                }

        }

        return resource;
    }

    private Transformer getTransformer(String xsltPath) throws RegistryException {

        InputStream xsltStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream(xsltPath);
        if (xsltStream == null) {
            String msg = "Could not locate the XSLT file for generating the custom UI. " +
                    "Make sure that the file " + xsltPath +
                    " is in the class path of the WSO2 Registry.";
            log.error(msg);
            throw new RegistryException(msg);
        }
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer(new StreamSource(xsltStream));


        } catch (TransformerConfigurationException e) {
            String msg = "Failed to create XSLT transformer for the XSLT file " + xsltPath +
                    " while generating custom UI. " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        } finally {
                try {
                    xsltStream.close();
                } catch (IOException e) {
                    log.error("Failed to close the stream", e);
                }
        }
        return transformer;
    }

    private String streamToString(InputStream inputStream) throws RegistryException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer content = new StringBuffer();

        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line);
            }

        } catch (IOException e) {
            String msg = "Failed to read input stream while converting to a string. " +
                    e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);

        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                String msg = "Failed to close the input stream. " + e.getMessage();
                log.error(msg, e);
            }
        }

        return content.toString();
    }

    private String getConfigFormatErrorMessage() {
        String configFormat = "Invalid configuration format for XSLT based UI handler.";
        return configFormat;
    }
}
