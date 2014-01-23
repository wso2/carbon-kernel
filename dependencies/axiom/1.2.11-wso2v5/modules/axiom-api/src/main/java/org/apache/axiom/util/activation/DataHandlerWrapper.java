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
package org.apache.axiom.util.activation;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.CommandInfo;
import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;

/**
 * Base class for {@link DataHandler} wrappers.
 */
public class DataHandlerWrapper extends DataHandler {
    private final DataHandler parent;

    public DataHandlerWrapper(DataHandler parent) {
        // Some JavaMail implementations allow passing null to the constructor,
        // but this is not the case for all implementations. We use an empty data
        // source to avoid this issue. This approach is known to work with Sun's
        // and Geronimo's JavaMail implementations.
        super(EmptyDataSource.INSTANCE);
        this.parent = parent;
    }
    
    public CommandInfo[] getAllCommands() {
        return parent.getAllCommands();
    }

    public Object getBean(CommandInfo cmdinfo) {
        return parent.getBean(cmdinfo);
    }

    public CommandInfo getCommand(String cmdName) {
        return parent.getCommand(cmdName);
    }

    public Object getContent() throws IOException {
        return parent.getContent();
    }

    public String getContentType() {
        return parent.getContentType();
    }

    public DataSource getDataSource() {
        return parent.getDataSource();
    }

    public InputStream getInputStream() throws IOException {
        return parent.getInputStream();
    }

    public String getName() {
        return parent.getName();
    }

    public OutputStream getOutputStream() throws IOException {
        return parent.getOutputStream();
    }

    public CommandInfo[] getPreferredCommands() {
        return parent.getPreferredCommands();
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return parent.getTransferData(flavor);
    }

    public DataFlavor[] getTransferDataFlavors() {
        return parent.getTransferDataFlavors();
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return parent.isDataFlavorSupported(flavor);
    }

    public void setCommandMap(CommandMap commandMap) {
        parent.setCommandMap(commandMap);
    }

    public void writeTo(OutputStream os) throws IOException {
        parent.writeTo(os);
    }
}
