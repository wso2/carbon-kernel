# Welcome to WSO2 Carbon Kernel
WSO2 Carbon Kernel 5.2.0 is the core of the next-generation WSO2 Carbon platform. 

It is completely rearchitected Carbon Kernel from the ground up with the latest technologies and patterns. Additionally, the Carbon Kernel is now a lightweight, general-purpose OSGi runtime specializing in hosting servers, providing key functionality for server developers. The result is a streamlined and even more powerful middleware platform than ever before.

# Architecture
Carbon Kernel is a modular, light-weight, OSGi-based server development framework, which provides the base for developing servers. Eclipse Equinox is used as the OSGi runtime from Kernel 5.0.0 onwards. However, you can plug in any OSGi implementation to your Carbon server. The diagram below depicts the architecture of WSO2 Carbon Kernel and its key components.
.......

# Key Features

Given below are details of the core capabilities of Carbon Kernel.

* Resolving the component startup order
* Adding new transports
* Plugging a new runtime
* Using the CarbonContext API
* Developing a Carbon tool
* Configuring Logging for a Carbon Server
* Monitoring Carbon Servers

* Setting up the Carbon Launcher
* Using in-container OSGi testing for development

Given below are details of tools and archetypes that can be used for developing Carbon products.

* Creating a Carbon component in one step using Maven archetypes
* Creating a generic OSGi bundle in one step using Maven archetypes
* Converting JARs to OSGi bundles
* Using the dropins support for OSGi bundles

# Getting Started
### Downloading the Kernel
You can download the product distribution from here.
Extract the archive file to a dedicated directory for the product, which will hereafter be referred to as <PRODUCT_HOME>.

### Installation prerequisites
Prior to installing any WSO2 Carbon-based product, it is necessary to have the appropriate prerequisite software installed on your system. Verify that the computer has the supported operating system and development platforms before starting the installation.

*System requirements*

|----------- |:--------------------------------------------------| 
| Memory     | ~ 256 MB minimum                                  | 
| Disk       | ~ 50 MB, excluding space allocated for log files. |
  
*Environment compatibility*
 Operating Systems / Databases 
 
WSO2 Carbon Kernel can be run on Windows / Linux and MacOS platforms that are Oracle/Open JDK 1.8.* compliant.      

*Supporting applications*

The following applications are required for running the product and its samples or for building from the source code. Mandatory installs are marked with an asterisk (*).
Application
Purpose
Version
Download Links
Oracle Java SE Development Kit (JDK)*
To launch the product, as each product is a Java application.
To build the product from the source distribution (both JDK and Apache Maven are required).
1.8.*
If you want to build the product from the source distribution, you must use JDK 1.8.*.
http://www.oracle.com/technetwork/java/javase/downloads/index.html
Apache Maven
To build the product from the source distribution (both JDK and Apache Maven are required). If you are installing the product by downloading and extracting the binary distribution instead of building from the source code, you do not need to install Maven.
To build samples.
3.3.x
http://maven.apache.org/

### Installing on Linux

#### Setting the JAVA_HOME

You must set your JAVA_HOME environment variable to point to the directory where the Java Development Kit (JDK) is installed on the computer. The way you set the JAVA_HOME depends on the operating system and the shell you are using. Given below is a sample configuration for bash shell.

>Environment variables are global system variables accessible to all the processes running under the operating system.

1. In your home directory, open the .bashrc file in your favorite Linux text editor.
2. Add the following two lines at the bottom of the file, replacing /opt/java/jdk1.8.* with the actual directory where the JDK is installed.

 ```export JAVA_HOME=<jdk-install-dir>
 export PATH=$JAVA_HOME/bin:$PATH```

3. To get the changes reflected, run the following command:

 ```source ~/.bashrc```

4. To verify that the JAVA_HOME variable is set correctly, execute the following command:

 ```echo $JAVA_HOME```

The above command should return the JDK installation path.

### Installing on Windows

#### Setting JAVA_HOME

You must set your JAVA_HOME environment variable to point to the directory where the Java Development Kit (JDK) is installed on the computer. Typically, the JDK is installed in a directory under C:\Program Files\Java, such as C:\Program Files\Java\jdk1.8.*.

>Environment variables are global system variables accessible to all the processes running under the operating system. You can define an environment variable as a system variable, which applies to all users, or as a user variable, which applies only to the user who is currently logged in.

You can set JAVA_HOME using the system properties, as described below. Alternatively, if you just want to set JAVA_HOME temporarily in the current command prompt window, set it at the command prompt. 

* Setting JAVA_HOME using the System Properties

 1. Right-click the My Computer icon on the desktop and click Properties.
 2. In the System Properties window, go to the Advanced tab, and then click Environment Variables.
 3. Click New under "System variables" (for all users) or under "User variables" (just for the user who is currently logged in).
 4. Enter the following information:
   * In the Variable name field, enter: JAVA_HOME
   * In the Variable value field, enter the installation path of the Java Development Kit, such as: c:\Program Files\Java jdk1.8.*

 5. Click OK.
 
 The JAVA_HOME variable is now set and will apply to any subsequent command prompt windows that you open. If you have any command prompt windows currently running, you must close and reopen them for the JAVA_HOME variable to take effect, or manually set the JAVA_HOME variable in those command prompt windows as described in the next section. To verify that the JAVA_HOME variable is set correctly, open a command window (from the Start menu, click Run, and then type CMD and click Enter) and execute the following command:

 ```set JAVA_HOME```

 The system returns the JDK installation path.
 
* Setting JAVA_HOME temporarily using the Windows command prompt (CMD)
 You can temporarily set the JAVA_HOME environment variable within a Windows command prompt window (CMD). This is useful when you have an existing command prompt window running and you do not want to restart it.
 
 1. In the command prompt window, enter the following command where <JDK_INSTALLATION_PATH> is the JDK installation directory and press  Enter: 
 set `JAVA_HOME=<JDK_INSTALLATION_PATH>`
 
 For example:
 set `JAVA_HOME=c:\Program Files\java\jdk1.8.*`
 
 The JAVA_HOME variable is now set only for the current CMD session.

 2. To verify that the JAVA_HOME variable is set correctly, execute the following command:
 set `JAVA_HOME`

 The system returns the JDK installation path.

### Launching the Kernel

#### Starting the server

To start the server, you need to run the carbon.bat (on Windows) script or the carbon.sh (on Linux) script from the <PRODUCT_HOME>/bin folder:

>To start and stop the server in the daemon mode in Linux, run carbon.sh start and carbon.sh stop commands.

1. Open a command prompt.

(On Windows, choose Start -> Run, type cmd at the prompt, and press Enter).
Execute one of the following commands, where <PRODUCT_HOME> is the directory where you installed the product distribution:
OS	Command
On Windows	
<PRODUCT_HOME>\bin\carbon.bat
On Linux/Solaris	
sh <PRODUCT_HOME>/bin/carbon.sh

Now, the server startup logs will get printed. When the server has completed the server startup, the log will display the message "WSO2 Carbon started in 'n' seconds."

#### Stopping the server
To stop the server, press Ctrl+C in the command window. If you have started the server in daemon mode in Linux, run carbon.sh stop command.

# How To Contribute
* Please report issues at [WSO2 JIRA](https://wso2.org/jira/browse/Carbon).
* Send your pull requests to [master branch](https://github.com/wso2/carbon-kernel/tree/master).
* You can find more instructions on how to contribute on community site (http://wso2.com/community).

# Contact Us
WSO2 developers can be contacted via the mailing lists:
* WSO2 Developers List : dev@wso2.org
* WSO2 Architecture List : architecture@wso2.org
