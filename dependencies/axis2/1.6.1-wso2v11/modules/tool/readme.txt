================
Using the tools  
================

=========================
(1) Axis2 elcipse plugins
=========================

Note - The plugins are specificallyqp for Eclipse version 3.1 and up

Maven2 Build
------------
	* To build the eclipse plugin type "mvn clean install -Dmaven.test.skip=true" command in the
	  tools/axis2-eclipse-<name>-plugin directory
	* If you have already downloaded the maven artifacts you can invoke a local build by
          same directory"mvn clean install -Dmaven.test.skip=true -o"
        * After the successful build the zip version of the plugin will be available at
          tools/axis2-eclipse-<name>-plugin/target/dist directory
	* To run the plugin you need please refer to,
	   - Tools Page On Apache Axis 2 Documentation  http://ws.apache.org/axis2/tools/

Ant Build
---------
	Create Eclipse Plugin Projects
	------------------------------

	* Since the source for the tools has a dependency on the eclipse classes. one has to run the
	  ant build file (build.xml) to generate a relevant eclipse project from the source.
	  
	* In order to compile the plugin first you must do a maven create-lib on Axis2 Source and
	  set ECLIPSE_HOME environment variable to point to your eclipse home directory.  
	
	* use the ant generate-projects command to generate the plugin projects.
	
	* Once the projects are generated in each eclipse plugin directory under tools directory
          (axis2-eclipse-service-plugin and axis2-eclipse-codegen-plugin) they can be opened as a Eclipse PDE
	  for building and editing.
	  
	* This can be done by File -> Import -> Existing project into workspace on Elcipse menu and 
	  point that to the any of eclipse plugin directory under tools directory.

	Build Eclipse Plugin Projects
	------------------------------

	* Build and install the eclipse plugin to your local eclipse plugin directory.
                * In order to compile the plugin first you must do a maven create-lib on Axis2 Source and
                  set ECLIPSE_HOME environment variable to point to your eclipse home directory.
                * stop eclpse if still on operation.
                * use ant install-plugins 
                * start eclipse
                * plugins will be accessible through [File -> New -> Other] ctl+n under Axis2 Wizards.

	* Release the plugins in compressed format
                * In order to compile the plugin first you must do a maven create-lib on Axis2 Source and
                  set ECLIPSE_HOME environment variable to point to your eclipse home directory.
                * stop eclpse if still on operation.
                * ant release-plugins
                * plugins will be available at target/eclipse_projects/release
	
	* The tool sources are not included in the build
	
	* To run the plugin you need please refer to,
		- Tools Page On Apache Axis 2 Documentation  http://ws.apache.org/axis2/tools/



==============================
(2) Axis2 Intellij Idea plugin
==============================

Maven build
-----------

        * To build the plugin type maven command in the tools/axis2-idea-plugin directory
        * If you have already downloaded the maven artifacts you can invoke a local build by maven -o
          same directory
        * After the successful build the zip version of the plugin will be available at
          tools/axis2-idea-plugin/target directory
	    * To run the plugin you need please refer to,
		    - Tools Page On Apache Axis 2 Documentation  http://ws.apache.org/axis2/tools/
