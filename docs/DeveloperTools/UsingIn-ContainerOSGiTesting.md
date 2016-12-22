# Using In-Container OSGi Testing
> This section explains the Pax Exam container in Carbon, which is used for OSGi testing. For the full list of capabilities available in this kernel version, see the **features** section in the [root README.md file](../../README.md#key-features-and-tools). 

The new Pax Exam container for Carbon will allow users to start a Carbon Kernel or any other C5 distribution with any number of features (from a zip file or a maven reference) that have bundles with TestNG-based test code.

# About the Pax Exam Container for Carbon
In previous versions of Carbon 5, pax exam Native container mode was used to write osgi level test cases where it launches an embedded OSGi framework using all the Carbon bundles. These Carbon bundles are hard-code in the test configuration classes. So to write the osgi level tests for a component, all the Carbon level OSGi bundles and the component level OSGi bundles have to be put from the test class for testing. To overcome from this, a new Pax Exam container will be introduced for Carbon 5 where users can point to any C5 distribution and write their own osgi tests. So instead of injecting bundles to external osgi environment, it would be better to test in our own osgi environment.

# Writing a Test Module for C5-based Distribution
Following diagram shows the maven structure of the required modules and their components.

![Maven structure of required modules](https://cloud.githubusercontent.com/assets/21237558/21380177/3dae8146-c779-11e6-8817-75917e98592c.png)

## Creating the Test Distribution Module
To write osgi tests, first there should be a test distribution with the pax exam feature that is provided in the Kernel. Feature dependency is as shown below. Once this is added, you can include other product level features in the test distribution.

```
<dependency>
    <groupId>org.wso2.carbon</groupId>
    <artifactId>org.wso2.carbon.pax.exam.feature</artifactId>
    <version>${carbon.kernel.version}</version>
    <type>zip</type>
</dependency>
```

Then, a test module has to be created to include test cases. The following configurations are needed for that test module in order to use the container.

## Creating the OSGi Test module
The following updates are required for creating the OSGi test module:

### Updating the POM file
The following dependencies have to be included in the POM to get the pax exam features, unit test features and the container features.

```
<dependency>
    <groupId>javax.inject</groupId>
    <artifactId>javax.inject</artifactId>
</dependency>
<dependency>
    <groupId>org.testng</groupId>
    <artifactId>testng</artifactId>
</dependency>
<dependency>
    <groupId>org.ops4j.pax.exam</groupId>
    <artifactId>pax-exam-testng</artifactId>
</dependency>
<dependency>
    <groupId>org.ops4j.pax.exam</groupId>
    <artifactId>pax-exam-link-mvn</artifactId>
</dependency>
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
</dependency>
<dependency>
    <groupId>org.ow2.spec.ee</groupId>
    <artifactId>ow2-jta-1.1-spec</artifactId>
</dependency>
<dependency>
    <groupId>org.wso2.carbon</groupId>
    <artifactId>org.wso2.carbon.container</artifactId>
</dependency>
```

The following plugins have to be included in the POM in order to run the osgi tests:

* **jacoco-maven-plugin**

  Generates the coverage reports for the test cases. The plugin definition is as follows: “jcoverage.command” will be set as an environmental variable later in the maven-surefire-plugin. In the “includes” tag, indicate the packages that have to show in the coverage report.

  ```
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>prepare-agent</id>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
            <configuration>
                <propertyName>jcoverage.command</propertyName>
                <includes>
                    <include>org.wso2.carbon*</include>
                </includes>
                <append>true</append>
            </configuration>
        </execution>
        <execution>
            <id>default-check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

* **maven-surefire-plugin**

  Required to run the testng tests. Environmental variable has to be included in order to generate the jacoco test coverage file.
  
  ```
  <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>2.9</version>
    <configuration>
        <systemPropertyVariables>             <org.ops4j.pax.url.mvn.localRepository>${settings.localRepository}</org.ops4j.pax.url.mvn.localRepository>
        </systemPropertyVariables>
        <suiteXmlFiles>
          <suiteXmlFile>src/test/resources/testng.xml</suiteXmlFile>
        </suiteXmlFiles>
        <environmentVariables>
            <JAVA_OPTS>${jcoverage.command}</JAVA_OPTS>
        </environmentVariables>
    </configuration>
</plugin>
```

* **maven-antrun-plugin**

  Generates the coverage site using jacoco.exec.
  
  ```
  <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-antrun-plugin</artifactId>
    <executions>
        <execution>
            <phase>prepare-package</phase>
            <configuration>
                <target xmlns:jacoco="antlib:org.jacoco.ant">
                    <taskdef uri="antlib:org.jacoco.ant" resource="org/jacoco/ant/antlib.xml">
                        <classpath path="${project.build.directory}"/>
                    </taskdef>
                    <jacoco:report>
                        <executiondata>
                            <file file="${project.build.directory}/jacoco.exec"/>
                        </executiondata>
                        <structure name="Carbon Core OSGi Tests">
                            <classfiles>
                                <fileset dir="../../core/target/classes"/>
                            </classfiles>
                            <sourcefiles encoding="UTF-8">
                                <fileset dir="../../core/src"/>
                            </sourcefiles>
                        </structure>
                        <html destdir="${project.build.directory}/site/jacoco"/>
                    </jacoco:report>
                </target>
            </configuration>
            <goals>
                <goal>run</goal>
            </goals>
        </execution>
    </executions>
    <dependencies>
        <dependency>
            <groupId>org.jacoco</groupId>
            <artifactId>org.jacoco.ant</artifactId>
            <version>${org.jacoco.ant.version}</version>
        </dependency>
    </dependencies>
</plugin>
```

* **Maven-paxexam-plugin**

   Handles the “version as in project” methods in pax exam maven bundles. If the test classes need the “version as in project” method, this plugin has to be included.
   
   ```
   <plugin>
    <groupId>org.ops4j.pax.exam</groupId>
    <artifactId>maven-paxexam-plugin</artifactId>
    <version>${maven.paxexam.plugin.version}</version>
    <executions>
        <execution>
            <id>generate-config</id>
            <goals>
                <goal>generate-depends-file</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Creating the Test Class

Test class should be created according to the following structure:

* **Class Annotations**

  Following class annotations are mandatory for each and every test class in the test module.
  
  ```
  @Listeners(PaxExam.class)
@ExamFactory(CarbonContainerFactory.class)
```
  
  To specify the reactory strategy, the following annotations should be included.
  
  ```
  @ExamReactorStrategy(PerClass.class)
  ```
  
  There are three different reactory strategies:
   1. PerMethod: Distribution will be started for each test method in the test class.
   2. PerClass: Distribution will be started for each test class in the test suite.
   3. PerSuite: Distribution will be started once per a test suite.
  The default strategy is PerClass.
  
  > Note that these strategies cannot be mixed in the test module. For example, you cannot have two classes using persuite and one class using perclass etc. This possibility is still not available in pax exam.

* **Inject references**

   References of `bundlecontext` or any osgi services have to be included with the @inject annotation.
   
   ```
   @Inject
protected BundleContext bundleContext;

@Inject
private CarbonServerInfo carbonServerInfo;
```

* **Configuration Method**

   To specify the relevant test distribution and its options for the test class, the configuration method is needed as shown below.
   
   ```
   @Configuration
public Option[] config() {
}
```
   This is not a mandatory option. If multiple test classes should have the same distribution, it can be mentioned in the test module's POM. If the @Configuration method is not included in the test class, the default distribution will be taken from the module's POM file. The default distribution has to be included as a system property in the POM file.
   
   ```
   <systemPropertyVariables>
    <<org.wso2.carbon.test.default.distribution>
        org.wso2.carbon:wso2carbon-kernel-test
    </<org.wso2.carbon.test.default.distribution>
</systemPropertyVariables>
```

* **Distribution Configuration options**

   There is a set of options available in the container for the purpose of configuring the relevant test distribution. These options have to be included in the @Configuration method.
   
  * **CarbonDistributionOption**
   
    This option is used for creating the options related to the Carbon test container. This is simply a utility class providing static methods for each option. So you can configure the distribution by calling the static methods in this class. These methods are briefly explained below.

  * **carbonDistribution**
  
    This method is used to specify the relevant test distribution for running the OSGi tests. There are three ways of specifying the distribution.
 
    Maven reference
 
    ```
 Option option = CarbonDistributionOption.carbonDistribution(
 maven()
 .groupId(“org.wso2.carbon”)
 .artifactId(“wso2carbon-kernel-test”)
 .type(“zip”).
 versionAsInProject())
 ```
 
    Distribution zip path
 
    ```
 Option option = CarbonDistributionOption.carbonDistribution(
 Paths.get(basedir, “..”, “test-distribution”, “target”, “wso2carbon-kernel-test-5.1.0-SNAPSHOT.zip”))
 ```
 
    Distribution directory path

    ```
 Option option = CarbonDistributionOption.carbonDistribution(
     Paths.get("target","wso2carbon-kernel-test-5.1.0-SNAPSHOT"))
     ```

* **copyFile**

   This method is used if there is a need for changing the files in the distribution, e.g., configuration files. In order to change a file, a new file needs to be created and the source and destination paths have to be specified as the parameters of the method.
  
    ```
    Path carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources", "carbon-context", "carbon.yml");
    Option option = CarbonDistributionOption.copyFile(carbonYmlFilePath,
    Paths.get("conf", "carbon.yml"));
    ```
        
* **copyDropinsBundle**

  This method is used if there are external bundles or artifacts that need to be included to the distribution for the specific test class. The curresponding artifact or bundle should be specified as a maven reference.

  ```
  Option option = CarbonDistributionOption.copyDropinsBundle(
          maven()
           .artifactId("carbon-context-test-artifact")
           .groupId("org.wso2.carbon")
           .versionAsInProject())
    ```
           
* **keepDirectory**

    By default pax-exam deletes the test directories once the test is completed. To keep these directories (for later evaluation) this method has to be called.
   
   ```
  Option option = CarbonDistributionOption.keepDirectory()
  ```
  
* **debug**

 This is used for opening the Carbon distribution in debug mode. This is useful if you want to debug the test cases.
 
 ```
 Option option = CarbonDistributionOption.debug(8500)
 ```
 
 Also, you can call the debug method without any parameters to use default debug port, which is 5005.
 
 ```
 Option option = CarbonDistributionOption.debug()
 ```
 
 There will be more options in future releases. Apart from these methods, there are some useful options in the pax exam module as well.
 
* **systemProperty**

 If there are system properties to be set when starting the server, those have to be passed using this method. This method is in the `org.ops4j.pax.exam.CoreOptions` class.
 
  ```
 Option option = CoreOptions.systemProperty("propertyname").value("value")
 ```
 
  Note: All these methods return an option corresponding to the specific action you mentioned by each method call. After calling the methods, you should return all those options in the “Configuration” method in the test class as shown below.
 
 ```
 @Configuration
public Option[] config() {
  Option option1 = CarbonDistributionOption.carbonDistribution(
         maven()
            .groupId(“org.wso2.carbon”)
            .artifactId(“wso2carbon-kernel-test”)
            .type(“zip”).versionAsInProject());
  Option option2 = CarbonDistributionOption.keepDirectory();
  Option option3 = CarbonDistributionOption.debug();
        return new Option[] {option1, option2, option3};
}
```

### Adding Test Methods

Test methods should be indicated with the @Test annotation.

```
@Test
public void testMethod(){
}
```

* **Sample Test Class**

 In the following test class, we are specifying the distribution as a maven reference and in the testSample method I am checking whether or not the Carbon core bundle is activated.

 ```
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class Sample {

    @Inject
    protected BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Configuration
    public Option[] config() {
        return new Option[] { 
                carbonDistribution(
                         maven()
                            .groupId("org.wso2.carbon")
                            .artifactId("wso2carbon-kernel-test")
                            .type("zip").versionAsInProject())
        };
    }

    @Test
    public void testSample() {
        Bundle coreBundle = null;
        for (Bundle bundle : bundleContext.getBundles()) {
            if (bundle.getSymbolicName().equals("org.wso2.carbon.core")) {
                coreBundle = bundle;
                break;
            }
        }
        Assert.assertNotNull(coreBundle, "Carbon Core bundle not found");
        Assert.assertEquals(coreBundle.getState(), Bundle.ACTIVE, "Carbon Core Bundle is not activated");
    }

}
```

* **Updating the testng.xml file**

 This file should be included in the resources in order to automate the OSGi tests.

 ```
<suite name="Carbon-Kernel-Core_OSGI-Test-Suite">
    <test name="carbon-core-osgi-tests" preserve-order="true" parallel="false">
        <classes>
            <class name="samples.Sample"/>
        </classes>
    </test>
</suite>
```

