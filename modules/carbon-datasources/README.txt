# Carbon data sources

### Introduction

This is a POC conducted to read datasource configuration files and bind these data sources through jndi so these data sources can be used by other OSGi bundles. For the moment this support only file based JNDI service.


### Usage

- Build the project and copy the org.wso2.carbon.datasource.core-1.0-SNAPSHOT.jar into the osgi folder located in CARBON_HOME directory of the C5 distribution. 

- This bundle search for data source configuration files in 'datasources' directory of the carbon configuration folder. Thus in your C5 distribution, create a folder named 'datasources' in CARBON_HOME/conf directory and copy the master-datasources.xml file found in the resources folder of the maven project into CARBON_HOME/conf/datasources directory. This master-datasources.xml file is a dummy configuration file. Thus you should update before use this. In addition you can place any data source configuration file have it's file name ends with '-datasources.xml'. This is the convention used in previous carbon versions.

- org.wso2.carbon.datasource.core bundle has a dependency with following jar files;
-- commons-io
-- fscontext
-- tomcat-jdbc
-- sun_jndi_providerutil

These jar files can be found in 'osgi' folder of the maven project. Copy these jars into CARBON-HOME/osgi folder. 

After completing aforementioned activities, start C5 kernel by running carbon.sh or carbon.bat. Then org.wso2.carbon.datasource.core-1.0-SNAPSHOT.jar will read the configuration files from the configuration directory and bind data sources.

