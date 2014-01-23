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

package org.apache.axis2.scripting;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class ScriptInvoker {

	protected File scriptFile;

	protected long lastModified;

	protected long lastCheckTime;

	protected int hotUpdateInterval;

	protected BSFEngine engine;

	private String scriptName;

	public ScriptInvoker(File scriptFile, int hotUpdateInterval) {
		this.scriptFile = scriptFile;
		this.hotUpdateInterval = hotUpdateInterval;
		this.scriptName = scriptFile.getName();
		initEngine();
	}

	public Object invoke(String functionName, Object[] args) {

		if (hotUpdateInterval > 0) {
			checkUpdate();
		}

		try {
			engine.call(null, functionName, args);
		} catch (BSFException e) {
			throw new RuntimeException(e);
		}

		return null;
	}

	protected synchronized void checkUpdate() {
		long now = System.currentTimeMillis();
		if (now - lastCheckTime > hotUpdateInterval) {
			lastCheckTime = now;
			long lm = scriptFile.lastModified();
			if (lm != lastModified) {
				lastModified = lm;
				initEngine();
			}
		}
	}

	protected void initEngine() {
		try {

			String scriptLanguage = BSFManager.getLangFromFilename(scriptName);
			BSFManager bsfManager = new BSFManager();
			bsfManager.setClassLoader(BSFManager.class.getClassLoader());
			// bsfManager.declareBean("_AxisService", axisService,
			// AxisService.class);

			BSFEngine bsfEngine = bsfManager
					.loadScriptingEngine(scriptLanguage);
			Object scriptSrc = readScript();
			bsfEngine.exec(scriptName, 0, 0, scriptSrc);

		} catch (BSFException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Reads the complete script source code into a String
	 */
	protected String readScript() {
		Reader reader = null;
		try {

			reader = new FileReader(scriptFile);
			char[] buffer = new char[1024];
			StringBuffer source = new StringBuffer();
			int count;
			while ((count = reader.read(buffer)) > 0) {
				source.append(buffer, 0, count);
			}

			return source.toString();

		} catch (IOException e) {
			throw new RuntimeException("IOException reading script: "
					+ scriptName, e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				throw new RuntimeException("IOException closing script: "
						+ scriptName, e);
			}
		}
	}
}
