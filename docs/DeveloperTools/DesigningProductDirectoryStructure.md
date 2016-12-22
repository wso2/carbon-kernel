# Designing the Product Directory Structure

New directory structure has been introduced in Carbon 5 which organizes the carbon server files in more cleaner and much user-friendly manner than the directory structure used in Carbon 4. This document will explain the new directory structure used in carbon 5 in detail. Carbon server has following main directories at the root level and, following that the purpose of each directory is explained briefly.

![screen shot 2016-12-22 at 4 10 51 pm](https://cloud.githubusercontent.com/assets/21237558/21423157/77f594fa-c861-11e6-8d56-133699cead2d.png)

* bin - contains script files common to the all the carbon runtimes and kernel-version.txt file which indicates the carbon kernel version
* conf - contains all the carbon configuration files
* lib -  OSGI bundles needed other than default set of wso2 OSGi bundles can be placed here and this directory is similar to CARBON-HOME/repository/components/dropins in C4
* logs - contains all the log files created while the carbon server is running
* resources - contains all the resources such as database scripts, keystore file and etc needed in when the carbon server runs
* tmp - temporary files created while the carbon server is running are stored here and is pointed to by the java.io.tmpdir System property
* wso2 - contains carbon runtimes and its OSGi artifacts

The next section will explain the usage of wso2 subdirectory in detail.

## The wso2 directory

![screen shot 2016-12-22 at 4 10 59 pm](https://cloud.githubusercontent.com/assets/21237558/21423153/72e163e0-c861-11e6-8cbb-e8c9185e49bb.png)
    
This directory contains only the wso2 specific OSGi artifacts (lib directory) and carbon runtimes. There is a separate subdirectory named with runtime’s name  for each carbon runtime in addition to lib subdirectory.

### The <runtime> directory

The carbon server can have multiple runtimes and for each runtime there will be a separate directory under CARBON-HOME/wso2 folder which will have the runtime’s specific configurations. The following is a sample runtime directory which contains the runtime’s configurations:

CARBON-HOME/wso2/<runtime>/
├── configuration
│   ├── config.ini
│   ├── org.eclipse.equinox.simpleconfigurator
│   │   └── bundles.info
│   ├── org.eclipse.osgi/
│   └── org.eclipse.update
│       └── platform.xml
└── eclipse.ini

### The lib directory
This directory is referred as the OSGi repository which has artifacts related to installed features. It has features, p2 and plugins as its sub directories

CARBON-HOME/wso2/lib/
├── features
├── p2
└── plugins

#### The features directory

It contains the installed features and each feature is added as a sub-directory with the feature name. Each feature directory contains the feature meta-data files and other resources added while creating the feature. The following is a sample feature directory which only contains the feature metadata files:

CARBON-HOME/wso2/lib/features/<feature-name>/
├── feature.properties
├── feature.xml
├── META-INF
│   ├── DEPENDENCIES
│   ├── LICENSE
│   ├── MANIFEST.MF
│   └── NOTICE
└── p2.inf

#### The p2 directory
This directory contains Carbon provisioning (p2) related configuration files.

#### The plugins directory
This directory contains set of OSGi bundles used by all carbon server runtimes and acts as the common place to share the OSGi bundles of all the carbon runtimes.
