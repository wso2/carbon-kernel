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

package org.apache.axis2.jaxws.providerapi;

import javax.activation.DataSource;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An impl class for javax.activation.DataSource interface.
 *
 */
public class DataSourceImpl implements DataSource {

    private final String fileName;

    private final String contentType;

    private byte[] byteArray;

    private ByteArrayOutputStream byteArrayOS;
    
	/**
	 * Constructor
	 * 
	 * @param _contentType
	 * @param _fileName
	 * @param image
	 * @throws Exception
	 */
	public DataSourceImpl(String _contentType, String _fileName, Image image) throws Exception{
		super();
        
        try {
        	if((_contentType == null) || (_contentType == "")){
        		throw new NullPointerException("Type is NULL: Must provide content type");
        	}
        	if((_fileName == null) || (_contentType == "")){
        		throw new NullPointerException("File name is NULL: Must provide content file name");
        	}
        	
        	this.fileName = _fileName;
    		this.contentType = _contentType;
        	
            if (image != null) {
            	byteArrayOS = new ByteArrayOutputStream();
                AttachmentUtil.storeImage(this.contentType, image, byteArrayOS);
            }
        } catch (Exception e) {
            throw e;
        }

	}

	/* (non-Javadoc)
	 * @see javax.activation.DataSource#getContentType()
	 */
	public String getContentType() {
		return this.contentType;
	}

	/* (non-Javadoc)
	 * @see javax.activation.DataSource#getInputStream()
	 */
	public InputStream getInputStream() throws IOException {
		if (this.byteArrayOS.size() != 0) {
			this.byteArray = this.byteArrayOS.toByteArray();
            this.byteArrayOS.reset();
        }
		
		if(this.byteArray == null){
			return new ByteArrayInputStream(new byte[0]);
			
		}
        return new ByteArrayInputStream(this.byteArray);
	}

	/* (non-Javadoc)
	 * @see javax.activation.DataSource#getName()
	 */
	public String getName() {
		return this.fileName;
	}

	/* (non-Javadoc)
	 * @see javax.activation.DataSource#getOutputStream()
	 */
	public OutputStream getOutputStream() throws IOException {
		if (this.byteArrayOS.size() != 0) {
			this.byteArray = this.byteArrayOS.toByteArray();
            this.byteArrayOS.reset();
        }
        return this.byteArrayOS;
	}
}
