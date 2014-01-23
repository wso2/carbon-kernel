package org.apache.axis2.jaxws.utility;

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

import java.awt.Image;
import java.awt.image.BufferedImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AttachmentUtils {
private static final Log log = LogFactory.getLog(AttachmentUtils.class);
	
	public static boolean compareImages(Image img1, Image img2){
		if(img1 == null && img2==null){
			if(log.isDebugEnabled()){
				log.debug("Both Image Objects are null, cannot compare Images returning false");
			}
			return false;
		}
		int h1 = img1.getHeight(null);
		int h2 = img2.getHeight(null);
		int w1 = img1.getWidth(null);
		int w2 = img1.getWidth(null);
		if(h1 != h2 || w1 != w2){
			return false;
		}
		if(img1 instanceof BufferedImage && img2 instanceof BufferedImage){
			BufferedImage bi1 = (BufferedImage)img1;
			BufferedImage bi2 = (BufferedImage)img2;
			for(int h=0;h<h1;++h){
				for(int w=0; w<w1; ++w){
					int Img1Pixel =bi1.getRGB(w, h);
					int Img2Pixel =bi2.getRGB(w, h);
					if(Img1Pixel!=Img2Pixel){
						return false;
					}
				}
			}
		}
		return true;		
	}
}
