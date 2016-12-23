# Designing the Product Directory Structure

> The process of designing the product directory structure for a Carbon product is explained below. For the full list of capabilities available in this kernel version, see the **features** section in the [root README.md file](../../README.md#key-features-and-tools). 

This version of Carbon 5, introduces a new directory structure for organizing the server files in a product. According to the new structure, a Carbon server will have the directories listed below as root-level directories.

![screen shot 2016-12-22 at 4 10 51 pm](https://cloud.githubusercontent.com/assets/21237558/21423157/77f594fa-c861-11e6-8d56-133699cead2d.png)

The root-level directories are explained below.

* **bin** - Contains the script files that are common to all the Carbon runtimes and the kernel-version.txt file,which indicates the carbon kernel version.
* **conf** - Contains all the Carbon configuration files.
* **lib** -  Contains the OSGI bundles that are required in addition to the default set of WSO2 OSGi bundles. Note that this directory is similar to the CARBON-HOME/repository/components/dropins directory in C4.
* **logs** - Contains all the log files that are created while the Carbon server is running.
* **resources** - Contains all the resources such as database scripts, keystore file etc. that are needed when the Carbon server runs.
* **tmp** - Stores the temporary files that are created while the Carbon server is running. The `java.io.tmpdir` system property in the product startup script points to these temporary files.
* **wso2** - Contains the Carbon runtimes and its OSGi artifacts.

The next section explains the usage of the WSO2 sub directory in detail.

## The wso2 directory

![screen shot 2016-12-22 at 4 10 59 pm](https://cloud.githubusercontent.com/assets/21237558/21423153/72e163e0-c861-11e6-8cbb-e8c9185e49bb.png)
    
This directory contains the WSO2-specific OSGi artifacts (lib directory) and the Carbon runtimes. There is a separate sub directory for each Carbon runtime in addition to lib subdirectory.

### The `<runtime>` directory

A Carbon server can contain multiple runtimes and each runtime will have a separate directory under in the `CARBON-HOME/wso2` folder. This runtime directory will contain all the configurations that are specific to the runtime. The following is a sample runtime directory, which contains the runtime’s configurations:

![screen shot 2016-12-22 at 4 11 05 pm](https://cloud.githubusercontent.com/assets/21237558/21423145/6d68848e-c861-11e6-90d8-d9b0b04b398d.png)

### The lib directory
This directory is referred to as the OSGi repository, which contains all the artifacts related to the installed features. It has three sub directories: 

* features
* p2
* plugins

![screen shot 2016-12-22 at 4 11 14 pm](https://cloud.githubusercontent.com/assets/21237558/21423137/68e6cdee-c861-11e6-859f-12bb9093ee9d.png)

The sub directories under the lib directory is explained below.

#### The features directory

This directory contains the installed features and each feature is added as a sub directory (with the feature name). Each feature directory contains the feature's meta-data files and other resources that are added while creating the feature. The following is a sample feature directory, which only contains the feature's metadata files:

![screen shot 2016-12-22 at 4 11 22 pm](https://cloud.githubusercontent.com/assets/21237558/21423136/67058358-c861-11e6-8344-c40ff4004143.png)

#### The p2 directory
This directory contains the configuration files related to Carbon provisioning (p2).

#### The plugins directory
This directory contains a set of OSGi bundles that are used by all Carbon server runtimes. Thereby, this directory is a common place to share the OSGi bundles of all the Carbon runtimes.
