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

package org.apache.axiom.attachments.lifecycle;

import java.io.IOException;

public interface DataHandlerExt {
	
	/**
	 * This method will give users an option to trigger a purge
	 * on temporary attachment files. Temp files are created for
	 * attachment data that is greater than a threshold limit. 
	 * On client side These temp attachment files are not deleted 
	 * untilthe virtual machine exits as user can choose to read 
	 * this dataHandler. So if user is not going to use the data 
	 * handlers provided on this temproray files they can choose 
	 * to purge the file. 
	 */
	public void purgeDataSource() throws IOException;
	
	/**
	 * This method will give users an option to trigger a delete on 
	 * temporary attachment file when DataHandler associated with the 
	 * attachment is read once. Temp files are created for
	 * attachment data that is greater than a threshold limit. 
	 * On client side These temp attachment files are not deleted untill
	 * the virtual machine exits. This method gives options to user to 
	 * trigger a delete on attachment files when they read the dataHandler
	 * once.
	 */
	
	public void deleteWhenReadOnce() throws IOException;
}
