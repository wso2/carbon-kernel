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

package org.apache.axiom.attachments;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.net.URL;

/**
 * This Axiom DataHandler inplementation allows the user to set custom values for the following MIME
 * body part headers. <ul> <li>content-transfer-encoding</li> <li>content-type</li> </ul> <p>Data
 * written to the MIME part gets encoded by content-transfer-encoding specified as above</p>
 * <p/>
 * <p>Usage is Similar to the javax.activation.DataHandler except for the setting of the above
 * properties. </p> <p>eg: </p> <p>    dataHandler = new ConfigurableDataHandler(new
 * ByteArrayDataSource(byteArray));</p> <p>    dataHandler.setTransferEncoding("quoted-printable");</p>
 * <p>    dataHandler.setContentType("image/jpg");</p>
 *
 * @see javax.activation.DataHandler
 */
public class ConfigurableDataHandler extends DataHandler {

    private String transferEncoding;

    private String contentType;

    private String contentID;

    public ConfigurableDataHandler(DataSource arg0) {
        super(arg0);
    }

    public ConfigurableDataHandler(Object arg0, String arg1) {
        super(arg0, arg1);
    }

    public ConfigurableDataHandler(URL arg0) {
        super(arg0);
    }

//	public String getContentID() {
//		return contentID;
//	}
//
//	public void setContentID(String contentID) {
//		this.contentID = contentID;
//	}

    public String getContentType() {
        if (contentType != null) {
            return contentType;
        } else {
            return super.getContentType();
        }

    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getTransferEncoding() {
        return transferEncoding;
    }

    public void setTransferEncoding(String transferEncoding) {
        this.transferEncoding = transferEncoding;
    }

}
