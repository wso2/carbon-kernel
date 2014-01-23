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

package org.apache.axis2.jaxws.description;

/**
 * A parameter or return value may be represented as an attachment.
 * In such cases, the ParameterDescription or OperationDescription has
 * an AttachmentDescription defining the attachment information.
 * 
 * Note that an Attachment may be one of 3 types: SWA, SWAREF and MTOM.
 *    SWAREF and MTOM attachments have an xml element (either in the body or header) 
 *    that references the attachment part.
 *    
 *    SWA attachments are represented as an attachment part.  There are is no
 *    xml element that references the attachment.
 *
 */
public interface AttachmentDescription {
    
    /**
     * @Return The attachment type 
     */
    AttachmentType getAttachmentType();
    
    /**
     * @return one or more mime types defined for this attachment
     */
    String[] getMimeTypes();
}
