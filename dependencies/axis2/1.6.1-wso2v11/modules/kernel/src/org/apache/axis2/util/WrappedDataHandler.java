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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.CommandInfo;
import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class acts as a wrapper for the javax.activation.DataHandler class.
 * It is used to store away a (potentially) user-defined content-type value along with
 * the DataHandler instance.   We'll delegate all method calls except for getContentType()
 * to the real DataHandler instance.   
 */
public class WrappedDataHandler extends DataHandler {
    
    private static final Log log = LogFactory.getLog(WrappedDataHandler.class);
    
    DataHandler delegate;
    String contentType;
    
    private static FakeDataSource FAKE_DS = new FakeDataSource();
    
    // This class is simply used as a fake DataSource implementation so that the
    // WrappedDataHandler class can call it's superclass's ctor with a non-null
    // value that implements DataSource.   The FakeDataSource instance will never
    // be used, however.  It's simply a placeholder.
    private static class FakeDataSource implements DataSource {

        
        public String getContentType() {
            return "application/octet-stream";
        }

        
        public InputStream getInputStream() throws IOException {
            throw new UnsupportedOperationException();
        }

        
        public String getName() {
            return "FakeDataSource";
        }

        
        public OutputStream getOutputStream() throws IOException {
            throw new UnsupportedOperationException();
        }
    }
    
    /**
     * Constructs a new instance of the WrappedDataHandler.
     * @param _delegate the real DataHandler instance being wrapped
     * @param _contentType the user-defined contentType associated with the DataHandler instance
     */
    public WrappedDataHandler(DataHandler _delegate, String _contentType) {
        super(FAKE_DS);
        
        delegate = _delegate;
        contentType = _contentType;
        
        if (log.isDebugEnabled()) {
            log.debug("Created instance of WrappedDatahandler: " + this.toString() + ", contentType=" + contentType
                + "\nDelegate DataHandler: " + delegate.toString());
        }
    }

    /* (non-Javadoc)
     * @see javax.activation.DataHandler#getAllCommands()
     */
    @Override
    public CommandInfo[] getAllCommands() {
        return delegate.getAllCommands();
    }

    /* (non-Javadoc)
     * @see javax.activation.DataHandler#getBean(javax.activation.CommandInfo)
     */
    @Override
    public Object getBean(CommandInfo paramCommandInfo) {
        return delegate.getBean(paramCommandInfo);
    }

    /* (non-Javadoc)
     * @see javax.activation.DataHandler#getCommand(java.lang.String)
     */
    @Override
    public CommandInfo getCommand(String paramString) {
        return delegate.getCommand(paramString);
    }

    /* (non-Javadoc)
     * @see javax.activation.DataHandler#getContent()
     */
    @Override
    public Object getContent() throws IOException {
        return delegate.getContent();
    }

    /* (non-Javadoc)
     * @see javax.activation.DataHandler#getContentType()
     */
    @Override
    public String getContentType() {
        return (contentType != null ? contentType : delegate.getContentType());
    }

    /* (non-Javadoc)
     * @see javax.activation.DataHandler#getDataSource()
     */
    @Override
    public DataSource getDataSource() {
        return delegate.getDataSource();
    }

    /* (non-Javadoc)
     * @see javax.activation.DataHandler#getInputStream()
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return delegate.getInputStream();
    }

    /* (non-Javadoc)
     * @see javax.activation.DataHandler#getName()
     */
    @Override
    public String getName() {
        return delegate.getName();
    }

    /* (non-Javadoc)
     * @see javax.activation.DataHandler#getOutputStream()
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        return delegate.getOutputStream();
    }

    /* (non-Javadoc)
     * @see javax.activation.DataHandler#getPreferredCommands()
     */
    @Override
    public CommandInfo[] getPreferredCommands() {
        return delegate.getPreferredCommands();
    }

    /* (non-Javadoc)
     * @see javax.activation.DataHandler#getTransferData(java.awt.datatransfer.DataFlavor)
     */
    @Override
    public Object getTransferData(DataFlavor paramDataFlavor) throws UnsupportedFlavorException, IOException {
        return delegate.getTransferData(paramDataFlavor);
    }

    /* (non-Javadoc)
     * @see javax.activation.DataHandler#getTransferDataFlavors()
     */
    @Override
    public synchronized DataFlavor[] getTransferDataFlavors() {
        return delegate.getTransferDataFlavors();
    }

    /* (non-Javadoc)
     * @see javax.activation.DataHandler#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
     */
    @Override
    public boolean isDataFlavorSupported(DataFlavor paramDataFlavor) {
        return delegate.isDataFlavorSupported(paramDataFlavor);
    }

    /* (non-Javadoc)
     * @see javax.activation.DataHandler#setCommandMap(javax.activation.CommandMap)
     */
    @Override
    public synchronized void setCommandMap(CommandMap paramCommandMap) {
        delegate.setCommandMap(paramCommandMap);
    }

    /* (non-Javadoc)
     * @see javax.activation.DataHandler#writeTo(java.io.OutputStream)
     */
    @Override
    public void writeTo(OutputStream paramOutputStream) throws IOException {
        delegate.writeTo(paramOutputStream);
    }
}
