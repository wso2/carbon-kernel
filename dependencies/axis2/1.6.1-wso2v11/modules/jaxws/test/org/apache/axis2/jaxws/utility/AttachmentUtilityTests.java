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

package org.apache.axis2.jaxws.utility;

import org.apache.axis2.jaxws.unitTest.TestLogger;

import java.awt.Image;
import java.io.File;

import javax.imageio.ImageIO;

import junit.framework.TestCase;

public class AttachmentUtilityTests extends TestCase {
	public void testCompareDifferentImages(){
		try{
			String img1Path = "/test-resources/image/image1.jpeg";
			String img2Path = "/test-resources/image/image2.jpeg";
			
			String img1FilePath = getAbsoluteLocation()+img1Path;
			String img2FilePath = getAbsoluteLocation()+img2Path;
			
			File img1File = new File(img1FilePath);
			File img2File = new File(img2FilePath);
			Image img1=ImageIO.read(img1File) ;
			Image img2= ImageIO.read(img2File);
			assertFalse(AttachmentUtils.compareImages(img1, img2));
			TestLogger.logger.debug("Image Compared successfully");
		}catch(Exception e){
			System.out.println("Image Compare Failed");
			e.printStackTrace();
			fail();
		}
	}
	public void testCompareSameImages(){
		try{
			String img1Path = "/test-resources/image/image1.jpeg";
			String img1FilePath = getAbsoluteLocation()+img1Path;
			
			File img1File = new File(img1FilePath);
			File img2File = new File(img1FilePath);
			
			Image img1=ImageIO.read(img1File) ;
			Image img2= ImageIO.read(img2File);
			
			assertTrue(AttachmentUtils.compareImages(img1, img1));
			TestLogger.logger.debug("Image Compared successfully");
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Image Compare Failed");
			fail();
		}
	}
	private String getAbsoluteLocation(){
		
		return new File("").getAbsolutePath();
	}
}
