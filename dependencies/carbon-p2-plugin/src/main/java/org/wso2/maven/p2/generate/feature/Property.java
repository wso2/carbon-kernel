package org.wso2.maven.p2.generate.feature;

import org.apache.maven.plugin.MojoExecutionException;

public class Property {
	/**
     * property key
     *
     * @parameter
     * @required
     */
	private String key;
	
	/**
     * property value
     *
     * @parameter 
     * @required
     */
	private String value;
	
	
	public void setValue(String value) {
		this.value = value;
	}
	public String getValue() {
		return value;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getKey() {
		return key;
	}
	
	public static Property getProperty(String advicePropertyDefinition) throws MojoExecutionException{
		if (advicePropertyDefinition.trim().equalsIgnoreCase("")) throw new MojoExecutionException("Invalid advice property definition.");
		String[] propertyDefs = advicePropertyDefinition.split(":");
		Property property = new Property();
		if (propertyDefs.length>1){
			property.setKey(propertyDefs[0]);
			property.setValue(propertyDefs[1]);
		}else
			throw new MojoExecutionException("Invalid advice property definition: "+advicePropertyDefinition);
		return property;
	}
}
