${product.name} v${product.version}


This file explains the usages of all the scipts contained within
this directory.

1. chpasswd.sh & chpasswd.bat
    - Utiliy for changing the passwords of users registered in the CARBON user
      database

	This script is designed to be used with any databases. Tested with H2,MSSQL and Derby. H2 database is embedded with CARBON. 
	Open a console/shell and run the script from "$CARBON_HOME/bin" directory.

	Usage:
	# chpasswd.bat/sh --db-url jdbc:h2:/$CARBON_HOME/repository/database/WSO2CARBON_DB

	If the administrator wants to use other databases, he should configure the datasource in the "master-datasources.xml", which is in 
	"$CARBON_HOME/repository/conf/datasources" directory. This datasource is lookedup in "registry.xml" and "user-mgt.xml" as a JNDI resource.
  	He also needs to keep the drivers of the database inside the "$CARBON_HOME/repository/components/lib" directory.

	For eg,
	If you need to use MSSQL as your DB,
	a. Put the MSSQL driver inside the "$CARBON_HOME/repository/components/lib" directory.
	b. Edit the datasource in master-datasources.xml file with your database's url, username, password and drivername details. 

	eg:
	<datasource>
            <name>WSO2_CARBON_DB</name>
            <description>The datasource used for registry and user manager</description>
            <jndiConfig>
                <name>jdbc/WSO2CarbonDB</name>
            </jndiConfig>
            <definition type="RDBMS">
                <configuration>
                    <url>jdbc:jtds:sqlserver://10.100.1.68:1433/USERDB</url>
                    <username>USER</username>
                    <password>USER</password>
                    <driverClassName>net.sourceforge.jtds.jdbc.Driver</driverClassName>
                    <maxActive>50</maxActive>
                    <maxWait>60000</maxWait>
                    <testOnBorrow>true</testOnBorrow>
                    <validationQuery>SELECT 1</validationQuery>
                    <validationInterval>30000</validationInterval>
                </configuration>
            </definition>
        </datasource>

	c. The above datasource is looked up using JNDI in "registry.xml" and "usr-mgt.xml" as below.
	
	In registry.xml;
	
	eg:
	<dbConfig name="wso2registry">
        	<dataSource>jdbc/WSO2CarbonDB</dataSource>
    	</dbConfig>

	In usr-mgt.xml;

	eg:
	<Configuration>
                <AdminRole>admin</AdminRole>
                <AdminUser>
                     <UserName>admin</UserName>
                     <Password>admin</Password>
                </AdminUser>
            <EveryOneRoleName>everyone</EveryOneRoleName>
            <Property name="dataSource">jdbc/WSO2CarbonDB</Property>
            <Property name="MultiTenantRealmConfigBuilder">org.wso2.carbon.user.core.config.multitenancy.SimpleRealmConfigBuilder</Property>
        </Configuration>

	d. Open a console/shell and run the script from "$CARBON_HOME/bin" directory having shutdown the server.
	
	Usage:
	# chpasswd.bat/sh --db-url jdbc:jtds:sqlserver://10.100.1.68:1433/USERDB --db-driver net.sourceforge.jtds.jdbc.Driver --db-username USER --db-password USER

	e. Now you can access the admin console with your new password.

	NOTE:- To create your own database, you need to put the driver into the "$CARBON_HOME/repository/components/extensions" directory.
        Then, start the server with "-Dsetup" option.

	Usage:
	# wso2server.bat/sh -Dsetup
	It will create the tables. Thereafter shutdown the server and open a console/shell and run the script as enumerated above.
	You may delete the driver, which is inside the "$CARBON_HOME/repository/components/extensions" directory, as it is no more required.

2. README.txt
    - This file

3. version.txt
    - A simple text file used for storing the version

4. wso2server.sh & wso2server.bat
    - The main script file used for running the server.

    Usage: wso2server.sh [commands] [system-properties]

            commands:
                --debug <port>  Start the server in remote debugging mode.
                                port: The remote debugging port.

                --startHttpTransports	Immediately starts all Tomcat HTTP connectors.
                --start		Start Carbon using nohup
                --stop		Stop the Carbon server process
                --restart	Restart the Carbon server process
                --cleanRegistry Clean registry space.
                                [CAUTION] All Registry data will be lost..
                --version       The version of the product you are running.

            system-properties:

                -DosgiConsole=[port]
                                Start Carbon with Equinox OSGi console.
                                If the optional 'port' parameter is provided, a
                                telnet port will be opened.

                -DosgiDebugOptions=[options-file]
                                Start Carbon with OSGi debugging enabled.
                                If the optional 'options-file' is provided, the OSGi
                                debug options will be loaded from it.

                -Dsetup         Clean the Registry and other configuration,
                                recreate DB, re-populate the configuration,
                                and start Carbon.

                -Dcarbon.registry.root
                                The root of the Registry used by
                                this Carbon instance.

     
                -Dweb.location  The directory into which the UI artifacts
                                included in an Axis2 AAR file are extracted to.

		-Dcarbon.config.dir.path=[path]
				Overwrite the conf directory path where we keep all 
				configuration files like carbon.xml, axis2.xml etc.

		-Dcarbon.logs.path=[path]
				Define the path to keep Log files.

		-Daxis2.repository=[path]
				Overwrite the default location we keep axis2 client/service
				artifacts.

		-Dcomponents.repository=[path]
				Overwrite the default location we keep all the OSGi bundles. 

5. wsdl2java.sh & wsdl2java.bat - Tool for generating Java code from WSDLs

6. java2wsdl.sh & java2wsdl.bat - Tool for generating WSDL from Java code

7. ciphertool.sh & ciphertool.bat - Tool for encrypting and decrypting simple texts such as passwords.
    The arguments that are inputs to this tool with their meanings are shown bellow.

	keystore        - If keys are in a store , it's location
	storepass       - Password for access keyStore
	keypass         - To get private key
	alias           - Alias to identify key owner
	storetype       - Type of keyStore , Default is JKS
	keyfile         - If key is in a file
	opmode          - encrypt or decrypt , Default is encrypt
	algorithm       - encrypt or decrypt algorithm , Default is RSA
	source          - Either cipher or plain text as an in-lined form
	outencode       - Currently base64 and use for encode result
	inencode        - Currently base64 and use to decode input
	trusted         - Is KeyStore a trusted store? If presents this, consider as a trusted store
	passphrase      - if a simple symmetric encryption using a pass phrase shall be used

8. build.xml - Build configuration for the ant command. 
      Default task - Running the ant command in this directory, will copy the libraries that are require to run remote registry clients in to the repo                     sitory/lib directory.
      createWorker task - removes the front end components from the server runtime.
      localize task - Generates language bundles in the $CARBON_HOME/repository/components/dropins to be picked at a locale change.


	RUNNING INSTRUCTIONS FOR LOCALIZE TASK
	--------------------------------------
	(i) Create a directory as resources in your $CARBON_HOME.
	(ii) Add the relevent resources files of your desired languages in to that directory  by following the proper naming conventions of the ui jars.

		For an example in your resources directory, you can add the resource files as follows.

		<resources directory/folder>
			|
			|-----org.wso2.carbon.identity.oauth.ui_4.0.7
			|            |---------resources_fr.properties
			|            |---------resources_fr_BE.properties
			|	     |---------what ever your required language files
 			|
			|------org.wso2.carbon.feature.mgt.ui_4.0.6
                        |            |-------resources_fr.properties
                        |            |-------resources_fr_BE.properties
			|            |-------what ever your required language files
			|
			|------create directories/folders for each and every ui bundle


	(iii) Navigate to the $CARBON_HOME/bin and run the following command in command prompt. 
		ant localize

      	This will create the language bundles in the $CARBON_HOME/repository/components/dropins directory.

      	If you want to change the default locations of the resources directory and dropins directory, run the following command.
		ant localize -Dresources.directory=<your path to the resource directory> -Ddropins.directory=<path to the directory where you want to store generated language bundles>
9. yajsw - contains the wrapper.conf file to run a Carbon server as a windows service using YAJSW (Yet Another Java Service Wrapper)
