# Welcome to WSO2 Carbon Kernel
WSO2 Carbon Kernel 5.2.0 is the core of the next-generation WSO2 Carbon platform. 

It is completely rearchitected Carbon Kernel from the ground up with the latest technologies and patterns. Additionally, the Carbon Kernel is now a lightweight, general-purpose OSGi runtime specializing in hosting servers, providing key functionality for server developers. The result is a streamlined and even more powerful middleware platform than ever before.

* **[Architecture](#architecture)**
* **[Key Features and Tools](#key-features-and-tools)**
* **[Getting Started](#getting-started)**
* **[How to Release a Git Repository](#how-to-release-a-git-repository)**
* **[How To Contribute](#how-to-contribute)**
* **[Contact Us](#contact-us)**

## Architecture
Carbon Kernel is a modular, light-weight, OSGi-based server development framework, which provides the base for developing servers. Eclipse Equinox is used as the OSGi runtime from Kernel 5.0.0 onwards. However, you can plug in any OSGi implementation to your Carbon server. The diagram below depicts the architecture of WSO2 Carbon Kernel and its key components.

![carbon-kernel-architecture-01](https://cloud.githubusercontent.com/assets/21237558/20616347/939893b6-b307-11e6-882f-4c3f302ada0c.png)

## Key Features and Tools
Follow the links given below for details of the core capabilities of Carbon Kernel.

* [Resolving the component startup order](https://github.com/nilminiwso2/carbon-kernel-1/tree/master/core#resolving-the-component-startup-order)
* [Adding new transports](https://github.com/nilminiwso2/carbon-kernel-1/tree/master/core#adding-new-transports)
* [Plugging a new runtime](https://github.com/nilminiwso2/carbon-kernel-1/tree/master/core#plugging-a-new-runtime)
* [Using the CarbonContext API](https://github.com/nilminiwso2/carbon-kernel-1/tree/master/core#using-the-carboncontext-api)
* [Developing a Carbon tool](https://github.com/nilminiwso2/carbon-kernel-1/tree/master/core#developing-a-carbon-tool)
* [Configuring Logging for a Carbon Server](https://github.com/nilminiwso2/carbon-kernel-1/tree/master/core#configuring-logging-for-a-carbon-server)
* [Monitoring Carbon Servers](https://github.com/nilminiwso2/carbon-kernel-1/tree/master/core#monitoring-carbon-servers)
* [Setting up the Carbon Launcher](https://github.com/nilminiwso2/carbon-kernel-1/blob/master/launcher/README.md)
* [Using in-container OSGi testing for development](https://github.com/nilminiwso2/carbon-kernel-1/blob/master/pax-exam-container-carbon/README.md)

Follow the links given below for details of tools and archetypes that can be used for developing Carbon products.

* [Creating a Carbon component in one step using Maven archetypes](https://github.com/nilminiwso2/carbon-kernel-1/blob/master/archetypes/README.md#creating-a-carbon-component-in-one-step)
* [Creating a generic OSGi bundle in one step using Maven archetypes](https://github.com/nilminiwso2/carbon-kernel-1/blob/master/archetypes/README.md#creating-a-generic-osgi-bundle-in-one-step)
* [Converting JARs to OSGi bundles](https://github.com/nilminiwso2/carbon-kernel-1/blob/master/tools/README.md#converting-jars-to-osgi-bundles)
* [Using the dropins support for OSGi bundles](https://github.com/nilminiwso2/carbon-kernel-1/blob/master/tools/README.md#dropins-support-for-osgi-bundles)

Follow the links given below for details of plugins:

* [Using the Carbon Feature Plugin](https://github.com/nilminiwso2/carbon-kernel-1/blob/master/tools/README.md#using-the-carbon-feature-plugin)
* [Using the Maven Bundle Plugin](https://github.com/nilminiwso2/carbon-kernel-1/blob/master/tools/README.md#using-the-maven-bundle-plugin)

Follow the links given below for reference information.

* [Using Annotations with OSGi Declarative Services](https://github.com/nilminiwso2/carbon-kernel-1/wiki/Using-Annotations-with-OSGi-Declarative-Services)

## Getting Started

### Downloading the Kernel
You can download the product distribution from here.
Extract the archive file to a dedicated directory for the product, which will hereafter be referred to as `<PRODUCT_HOME>`.

### Installation prerequisites
Prior to installing any WSO2 Carbon-based product, it is necessary to have the appropriate prerequisite software installed on your system. Verify that the computer has the supported operating system and development platforms before starting the installation.

 * #### System requirements
   * **Memory:** ~ 256 MB minimum   
   * **Disk:** ~ 50 MB, excluding space allocated for log files.
  
 * #### Environment compatibility

   **Operating Systems/Databases:** WSO2 Carbon Kernel can be run on Windows / Linux and MacOS platforms that are Oracle/Open JDK 1.8.* compliant.      

 * #### Supporting applications

   The following applications are required for running the product and its samples or for building from the source code. Mandatory installs are marked with an asterisk (*).

   * [Oracle Java SE Development Kit (JDK)](http://www.oracle.com/technetwork/java/javase/downloads/index.html) version 1.8 is required for the following: 
     * To launch the product, as each product is a Java application.
     * To build the product from the source distribution (both JDK and Apache Maven are required).
  
   * [Apache Maven](http://maven.apache.org/) version 3.3.x is required for the following:
     * To build the product from the source distribution (both JDK and Apache Maven are required). If you are installing the product by  downloading and extracting the binary distribution instead of building from the source code, you do not need to install Maven.
     * To build samples.

### Setting 'JAVA_HOME`

 * #### On Linux: Setting `JAVA_HOME`

  You must set your `JAVA_HOME` environment variable to point to the directory where the Java Development Kit (JDK) is installed on the computer. The way you set the `JAVA_HOME` depends on the operating system and the shell you are using.

  > Environment variables are global system variables accessible to all the processes running under the operating system.

    1. In your home directory, open the `.bashrc` file in your favorite Linux text editor.
    2. Add the following two lines at the bottom of the file, replacing `/opt/java/jdk1.8.*` with the actual directory where the JDK is installed.

           export JAVA_HOME=<jdk-install-dir>
           export PATH=$JAVA_HOME/bin:$PATH

    3. To get the changes reflected, run the following command:

           source ~/.bashrc

    4. To verify that the JAVA_HOME variable is set correctly, execute the following command:

           echo $JAVA_HOME

       The above command should return the JDK installation path.

 * #### On Windows: Setting `JAVA_HOME`

    You must set your `JAVA_HOME` environment variable to point to the directory where the Java Development Kit (JDK) is installed on the computer. Typically, the JDK is installed in a directory under `C:\Program Files\Java`, such as `C:\Program Files\Java\jdk1.8.*`.

    > Environment variables are global system variables accessible to all the processes running under the operating system. You can define an environment variable as a system variable, which applies to all users, or as a user variable, which applies only to the user who is currently logged in.

    You can set `JAVA_HOME` using the system properties, as described below. Alternatively, if you just want to set `JAVA_HOME` temporarily in the current command prompt window, set it at the command prompt. 

   * Setting `JAVA_HOME` using the System Properties

    1. Right-click the **My Computer** icon on the desktop and click **Properties**.
    2. In the **System Properties** window, go to the **Advanced** tab, and then click **Environment Variables**.
    3. Click **New** under "System variables" (for all users) or under "User variables" (just for the user who is currently logged in).
    4. Enter the following information:
      * In the **Variable name** field, enter: `JAVA_HOME`
      * In the **Variable value** field, enter the installation path of the Java Development Kit, such as: `c:\Program Files\Java jdk1.8.*`
     5. Click **OK**.
 
     The `JAVA_HOME` variable is now set and will apply to any subsequent command prompt windows that you open. If you have any command prompt windows currently running, you must close and reopen them for the `JAVA_HOME` variable to take effect, or manually set the `JAVA_HOME` variable in those command prompt windows as described in the next section. To verify that the `JAVA_HOME` variable is set correctly, open a command window (from the Start menu, click Run, and then type **CMD** and click **Enter**) and execute the following command:

          set JAVA_HOME

      The system returns the JDK installation path.
 
    * Setting `JAVA_HOME` temporarily using the Windows command prompt (CMD)

      You can temporarily set the `JAVA_HOME` environment variable within a Windows command prompt window (CMD). This is useful when you have an existing command prompt window running and you do not want to restart it.
 
      1. In the command prompt window, enter the following command where `<JDK_INSTALLATION_PATH>` is the JDK installation directory and press Enter: `set JAVA_HOME=<JDK_INSTALLATION_PATH>`
      For example: `set JAVA_HOME=c:\Program Files\java\jdk1.8.*`
     
      The `JAVA_HOME` variable is now set only for the current CMD session.

      2. To verify that the `JAVA_HOME` variable is set correctly, execute the following command:
 set `JAVA_HOME`.

      The system returns the JDK installation path.

 ### Launching the Kernel

 #### Starting the server

 To start the server, you need to run the `carbon.bat` (on Windows) script or the `carbon.sh` (on Linux) script from the `<PRODUCT_HOME>/bin` folder:

 > To start and stop the server in the daemon mode in Linux, run `carbon.sh start` and `carbon.sh stop` commands.

 1. Open a command prompt.
 (On **Windows**, choose `Start -> Run`, type `cmd` at the prompt, and press **Enter**).
 2. Execute one of the following commands, where `<PRODUCT_HOME>` is the directory where you installed the product distribution:
   * **On Windows:**	`<PRODUCT_HOME>\bin\carbon.bat`
   * **On Linux/Solaris:** `sh <PRODUCT_HOME>/bin/carbon.sh`

 Now, the server startup logs will get printed. When the server has completed the server startup, the log will display the message "WSO2 Carbon started in 'n' seconds."

 #### Stopping the server
 To stop the server, press **Ctrl+C** in the command window. If you have started the server in `daemon` mode in Linux, `run carbon.sh stop` command.

## How to Release a Git Repository

The following are the steps for creating and releasing a Git repository:

* **[Setting up a Git Repository](#setting-up-a-git-repository)**
* **[Releasing a Git Repository](#releasing-a-git-repository)**

### Setting up a Git Repository

With the git based model, releases can be made easy using the `maven-release-plugin` and nexus staging repository. The following are the common guidelines for releasing from any git repository under a WSO2 user:

> Note that WSO2 approval is required for setting up git repositories under a WSO2 user. Also, this is a one-time process, which does not have to be repeated.

#### Step 1: Creating the repository

The following guidelines refer to carbon4-kernel as the sample project being released from git:

1. Create a “Repository Target” in [http://maven.wso2.org/nexus/](https://maven.wso2.org/nexus/#welcome) that matches the `groupID` of the project and add a “Pattern Expression”. This pattern expression is used by nexus to automatically determine the staging profile. Shown below are the values used when creating a "Repository Target" for the `carbon-kernel` project in git.

 * **Name:** `org.wso2.carbon`

 * **Repository Type:** `Maven2`

 * **Pattern Expression:** `.*/org/wso2/carbon/.*`

2. Create a nexus “Staging Profile” for the project in [http://maven.wso2.org/nexus/](https://maven.wso2.org/nexus/#welcome), if it is not already created. The name of the profile should match the project's `groupID`. For example, the name of the profile for the `carbon4-kernel` project should be `org.wso2.carbon`.

   a. Select the "Repository Target" that was created in step 1 above as the "Repository Target" for this profile.

   b. Select the `Releases` repository in [http://maven.wso2.org/nexus/content/repositories/releases/](http://maven.wso2.org/nexus/content/repositories/releases/) as the “Release Repository” for all staging profiles.

   c. Add “WSO2 Staging” to Target Groups. Make sure that `org.wso2.carbon` staging profile is the last entry in that list. You will have to move up the newly created profiles.

   d. Finally, give the `wso2-nexus-deployer` user permissions to stage the repository as follows: 
     
     i. “Staging: Repositories (`<staging-profile-name>`)”  
  
     ii. “Staging: Deployer (`<staging-profile-name>`)”

 The main reason for creating a separate staging profile and repository target is for nexus to uniquely identify artifacts belonging to a staging profile. It uses the `groupID` of the artifacts. Nexus uses pattern matching for this purpose as explained above.

 > Setting up the "Staging Profile" as explained in the above steps will be handled by the WSO2 Infra team, for every project in the WSO2 git repository.

### Step 2: Restructuring the POM files

Follow the instructions given below to restructure the pom files. The following uses [`carbon-kernel`](https://github.com/wso2/carbon-kernel/) as a reference project. 

> The top level pom file is the parent pom for your project and there is no real requirement to have a separate Maven module to host the parent POM file.

1. Update the project parent pom with the [WSO2 Master parent pom](http://repo1.maven.org/maven2/org/wso2/wso2/2/wso2-2.pom) as shown below. The WSO2 Mater parent pom holds all the common things that are used in almost all the repositories, such as `distributionManagement`, `pluginManagement`, `repositories`, `pluginRepositories`, etc.

            <parent>
                <groupId>org.wso2</groupId>
                <artifactId>wso2</artifactId>
                <version>5</version>
            </parent> 
 
 > Make sure that your parent-child pom hierarchy is followed in all the sub-modules. That is, a child sub-module cannot have a parent pom reference to an external pom. The parent pom references should be self-contained except in the above instance, where the project root pom’s parent reference is set to the WSO2 Master parent pom (`org.wso2:wso2:2`).

 > Also WSO2 carbon parent now is pre-configured with all the `maven-bundle-plugin` configurations as properties. Please refer this documentation on how to configure these properties for bundles that are coming from sub modules.

2. The following is the composite maven multi-module pom of `carbon-kernel` project. It directly calls those maven modules. You can keep  a similar directory structure to enhance human readability.

            <modules>
                 <module>parent</module>
                 <module>archetypes</module>
                 <module>launcher</module>
                 <module>core</module>
                 <module>tests</module>
                 <module>features</module>
                 <module>tools</module>
                 <module>distribution</module>
            </modules>

3. Update the project parent pom with the correct SCM configuration as shown below.

            <scm>
               <url>https://github.com/wso2/carbon-kernel.git</url>
               <developerConnection>scm:git:https://github.com/wso2/carbon-kernel.git</developerConnection>
               <connection>scm:git:https://github.com/wso2/carbon-kernel.git</connection>
               <tag>HEAD</tag>
            </scm>

 Then, remove all the scm configurations from the child poms.

4. You must have a `<dependencyManagement>` section on the project parent pom file, which defines all your project dependencies along with versions.

 > Note that you cannot have `<dependencyManagement>` sections on any other pom file other than the parent pom. When you add dependencies in the pom files of sub-modules, ensure that you don't specify the version, because it is already specified in the parent pom file under the `<dependencyManagement>` section.

5. Add the plugins given below to the `<build>` section in the project parent pom. The versions of these will be inherited from the [WSO2 Master parent pom](http://repo1.maven.org/maven2/org/wso2/wso2/2/wso2-2.pom).

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-release-plugin</artifactId>
                <configuration>
                    <preparationGoals>clean install</preparationGoals>
                    <autoVersionSubmodules>true</autoVersionSubmodules>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-deploy-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-javadoc-plugin</artifactId>
                <executions>
                    <execution>
                    <id>docs</id>
                    <phase>compile</phase>
                    <goals>
                        <goal>javadoc</goal>
                    </goals>
                </execution>
            </executions>
           </plugin>

 > Note: You can add the `autoVersionSubmodules` configuration parameter to release the plugin configuration section, which will automatically version the sub modules. However, please note that this will cause issues with versioning if your project has an orbit sub-module. This is because, for orbit modules, we follow a different versioning convention.

6. Remove `<distributionManagement>` from the project parent pom, if there are any. This step is mandatory as the repositories for the `<distributionManagement>` section is inherited from the WSO2 Master parent pom.

7. Add a server config element in the maven configuration (`<MVN_HOME>/conf/settings.xml`) for the `nexus-releases` server configuration given above. The nexus user credentials that will be used for remote artifact deployment are as follows:

          <server>
            <id>nexus-releases</id>
            <username>username</username>
            <password>password</password>
          </server>

 > Note: For the above step, you can request WSO2 Infra to create a user for the project in nexus.

8. Add another server config element that stores the SCM related credentials. This is an optional step, but will be useful to hide your SCM credentials when using the `mvn-release-plugin`.

         <server>
            <id>scm-server</id>
            <username>username</username>
            <password>password</password>
         </server>

 After adding the above, you have to update the parent pom properties section of your project with the following property: `project.scm.id`:

         <properties>
             <project.scm.id>scm-server</project.scm.id>
         </properties>


9. Make sure that the project does not have any SNAPSHOT dependencies and update those with released versions. If there are unreleased SNAPSHOT dependencies, we will have to release them separately. This will be checked by the release plugin during the `release:prepare` stage.

10. Then, make sure that you have properly parameterized the versions of dependencies. That is,  the dependencies from the `carbon-identity` repository should have a version parameter called `carbon.identity.version`. It’s unacceptable to have a version such as `carbon.platform.version` or `wso2carbon.version`. You need version parameters according to the repo.

### Releasing a Git Repository

If a Git repository is already set up as explained in setting up a git repository, the following steps can be followed to release the repository.

1. Create a git release branch from the master. The branch name would be `release-<release-version>`.

         git checkout -b release-<release-version> master

2. Maven release plug-in does not update some properties that we use, such as the osgi import and export versions. These properties also have the `SNAPSHOT` part in it. This has to be manually updated before performing the release preparation command.  

 > Also make sure that the project does not have any SNAPSHOT dependencies and update those with released versions. If there are any unreleased SNAPSHOT dependencies, we will have to release them separately. This will anyway be checked by the release plugin during the `release:prepare` stage.
 
 To test the above, we can use the [“dryRun”](http://maven.apache.org/maven-release/maven-release-plugin/prepare-mojo.html#dryRun) option with the maven release plugin.

3. Issue the following release preparation command: `mvn release:clean release:prepare`. 

 You can use the dedicated builder machine is used for the release purpose. Contact WSO2 Infra for credentials.

 The build artifacts will have this username in its `MANIFEST` file. Give appropriate values for the release, development, and tag versions as shown below when prompted for the release preparation command. Use the git tag versioning strategy `v1.x` when the tag version prompted: [http://git-scm.com/book/en/v2/Git-Basics-Tagging](https://git-scm.com/book/en/v2/Git-Basics-Tagging).

      [INFO] Checking dependencies and plugins for snapshots …
      What is the release version for "WSO2 Carbon Kernel"? (org.wso2.carbon:carbon-kernel) 5.0.0: : 5.0.0
      What is SCM release tag or label for "WSO2 Carbon Kernel"? (org.wso2.carbon:carbon-kernel) carbon-5.0.0: : v5.0.0
      What is the new development version for "WSO2 Carbon Kernel"? (org.wso2.carbon:carbon-kernel) 5.0.1-SNAPSHOT: : 5.1.0-  SNAPSHOT

 > Note the SCM release tag label. The release tag name should only have the version prefixed by the letter 'v'.

4. Once you have completed the above steps, it is recommended to perform some checks as shown below.

    a. Copy all the generated jars and zips to one place and extract them all.

          mkdir /tmp/artifacts1/
          cp `find . -iname *jar` /tmp/artifacts1/
          cp `find . -iname *zip` /tmp/artifacts1/
          cd /tmp/artifacts1/; for x in `ls`; do echo $x; unzip $x -d $x.unzip; done

    b. Do a `grep`. Ideally, this should not return anything.

          grep -ri "\${" . --include=MANIFEST.MF --include=feature.xml
          grep -ri “version=\"0\.0\.0\.” . --include=feature.xml
          grep -ri "Build-Jdk" . --include=MANIFEST.MF --include=feature.xml ##Should be 8

    c. Open the root pom from the release tag and make sure there are no snapshot dependencies or properties.

    d. Sources should be built using JDK 8.

    e. Optional - Use the builder machine to perform the release. Please request for credentials.

    f. If the above looks fine, then you can proceed to release:perform as instructed in the next step. After doing `release:perform`, go to `target/checkout/` folder at the repo root folder.

    g. Repeat step 1. This time, copy it to a different folder.

    h. Do a `grep`: 
    
           grep -ri "SNAPSHOT" . --include=MANIFEST.MF --include=feature.xml

      This too shouldn't return anything. 

5. Issue the release perform command: `mvn release:perform` 

6. When the above process succeeds, the artifacts will be deployed to a staging repo. The newly created staging repo will not be closed automatically when the artifacts are uploaded. This can be done through the `release-manager`. That is, by logging into the nexus UI, the repo can be manually closed. When a staging repo is closed, it becomes available for public access.

7. If there is a failure, the prepared release process can be rolled back using the following command: `mvn release:rollback`. This will revert all the commits made during the preparation process. 

 > When we are starting over again, always use a clean maven repo.

8. With the staging repo in effect, a release candidate VOTE should be called on [dev@wso2.org](mailto:dev@wso2.org) using the template given below. This VOTE is essential for a product release. For other projects, this is optional.

          Subject : [VOTE] Release <Project Name> <Project Version> <RC #>
         <BEGIN>
         This is the <RC #> release candidate of <Project Name> <Project Version> 
         Eg : WSO2 Carbon Kernel 5.0.0 rc1
         This release fixes the following issues:
         <URL to the fixed jira list>
         Please download, test and vote. Please refer the release verification guide for detailed information on verifying this release.

         Source & binary distribution files:
         <URL to the source and binary files>

         Maven staging repo:
         <URL to the maven nexus staging repo>
         Eg: http://maven.wso2.org/nexus/content/repositories/orgwso2carbon-1000/

         The tag to be voted upon:
         <URL to the release tag location>
         Eg: https://github.com/wso2/carbon-kernel/tree/v5.0.0-RC2

         KEYS file containing PGP keys we use to sign the release:
         <URL to the Keys used with signing the artifacts>

         Release verification guide:
         <If any>

         [ ] Broken - do not release (explain why)
         [ ] Stable - go ahead and release
         </BEGIN>

9. A release VOTE should be kept open for 72 hours. During this period, the developers should test the artifacts and then vote. When there are at least 3 binding +1 votes and no -1 votes, the vote is considered as a pass. Once the release vote is completed (the artifacts are tested and verified), the staging repo can be released, which will make the artifacts available in the public maven repo. Note that this should be done by the release manager. The released artifacts will be available in the WSO2 Releases maven repository at : [http://maven.wso2.org/nexus/content/repositories/releases/](http://maven.wso2.org/nexus/content/repositories/releases/)

10. If the vote failed, then the staging repository should be dropped and the changes for the release branch should be reverted. This process should be started again from #1 onwards, after fixing the issues mentioned during the vote.

 > Always use clean maven repo when you start over.

11. Finally, when you are done with the release, merge the release branch with the master. You can create a pull request for this from release-<release-version> to master.

## How To Contribute
* Please report issues at [WSO2 JIRA](https://wso2.org/jira/browse/Carbon).
* Send your pull requests to [master branch](https://github.com/wso2/carbon-kernel/tree/master).
* You can find more instructions on how to contribute on community site (http://wso2.com/community).

## Contact Us
WSO2 developers can be contacted via the mailing lists:
* WSO2 Developers List : dev@wso2.org
* WSO2 Architecture List : architecture@wso2.org
