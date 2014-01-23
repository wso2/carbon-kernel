/**
 *  Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.coordination.core;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.coordination.common.CoordinationException;
import org.wso2.carbon.coordination.common.CoordinationConstants.ConfigurationNames;
import org.wso2.carbon.coordination.common.CoordinationException.ExceptionCode;

/**
 * This class represents the global Coordination service configuration.
 */
public class CoordinationConfiguration {

	private List<Server> servers;
	
	private int sessionTimeout;
	
	private boolean enabled;
	
	public CoordinationConfiguration(String filePath) throws CoordinationException {
		this.initConfig(filePath);
	}
	
	private void initConfig(String filePath) throws CoordinationException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
		    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Element docEl = dBuilder.parse(filePath).getDocumentElement();
			if (!docEl.getNodeName().equals(ConfigurationNames.CLIENT_CONFIG_ELEMENT)) {
				throw new CoordinationException("The root element of the coordination client configuration must be '" + 
			            ConfigurationNames.CLIENT_CONFIG_ELEMENT + "'", 
						ExceptionCode.CONFIGURATION_ERROR);
			}
			String enabledAttr = docEl.getAttribute(ConfigurationNames.ENABLED);
			this.enabled = true; 
			if (enabledAttr != null && enabledAttr.length() > 0) {
				this.enabled = Boolean.parseBoolean(enabledAttr);
			}
			NodeList tmpList = docEl.getElementsByTagName(ConfigurationNames.SESSION_TIMEOUT);
			if (tmpList.getLength() > 0) {
				try {
				    this.sessionTimeout = Integer.parseInt(((Element) tmpList.item(0)).getTextContent());
				} catch (NumberFormatException e) {
					throw new CoordinationException("Invalid session timeout value", 
							ExceptionCode.CONFIGURATION_ERROR, e);
				}
			}
			tmpList = docEl.getElementsByTagName(ConfigurationNames.SERVERS);
			if (tmpList.getLength() == 0) {
				throw new CoordinationException("Servers section is missing in the configuration", 
						ExceptionCode.CONFIGURATION_ERROR);
			}
			Element serversEl = (Element) tmpList.item(0);
			tmpList = serversEl.getElementsByTagName(ConfigurationNames.SERVER);
			if (tmpList.getLength() == 0) {
				throw new CoordinationException("There must be atleast one server entry in the configuration", 
						ExceptionCode.CONFIGURATION_ERROR);
			}
			this.servers = new ArrayList<CoordinationConfiguration.Server>();
			int nservers = tmpList.getLength();
			for (int i = 0; i < nservers; i++) {
				this.servers.add(this.parseServerConfig((Element) tmpList.item(i)));
			}
		} catch (Exception e) {
			if (e instanceof CoordinationException) {
				throw (CoordinationException) e;
			}
			throw new CoordinationException(ExceptionCode.CONFIGURATION_ERROR, e);
		}
	}
	
	private Server parseServerConfig(Element serverEl) throws CoordinationException {
		String host = serverEl.getAttribute(ConfigurationNames.HOST);
		if (host == null || host.length() == 0) {
			throw new CoordinationException("Host attribute must be there in a server element", 
					ExceptionCode.CONFIGURATION_ERROR);
		}
		int port = -1;
		String portStr = serverEl.getAttribute(ConfigurationNames.PORT);
		if (portStr != null && portStr.length() > 0) {
		    try {
			    port = Integer.parseInt(portStr);
		    } catch (NumberFormatException e) {
			    throw new CoordinationException("Invalid server port value", 
				    	ExceptionCode.CONFIGURATION_ERROR, e);
		    }
		    if (port <= 0) {
				throw new CoordinationException("The server port must be a positive integer", 
						ExceptionCode.CONFIGURATION_ERROR);
			}
		}
		return new Server(host, port);
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public List<Server> listServers() {
		return servers;
	}
	
	/**
	 * Creates a single connection string from the available servers,
	 * the format is "host1:port1,host2:port2,...".
	 * @return The connection string
	 */
	public String getConnectionString() {
		StringBuilder builder = new StringBuilder();
		List<Server> servers = this.listServers();
		Server tmpServer;
		int tmpPort;
		for (int i = 0; i < servers.size(); i++) {
			if (i > 0) {
				builder.append(",");
			}
			tmpServer = servers.get(i);
			tmpPort = tmpServer.getPort();
			builder.append(tmpServer.getHost());
			/* put as default port or override it */
			if (tmpPort > 0) {
				builder.append(":" + tmpPort);
			}
		}
		return builder.toString();
	}
	
	public int getSessionTimeout() {
		return sessionTimeout;
	}
	
	/**
	 * Represents a single coordination server.
	 */
	public class Server {
		
		private String host;
		
		private int port;
		
		public Server(String host, int port) {
			this.host = host;
			this.port = port;
		}
		
		public String getHost() {
			return host;
		}
		
		public int getPort() {
			return port;
		}
		
	}
	
}
