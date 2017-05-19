${product.name} v${product.version}


This file explains the usages of all the scripts contained within
this directory.

1. README.txt
    - This file

2. kernel-version.txt
    - A simple text file used for storing the version

3. jartobundle.sh & jartobundle.bat
    - The script file which runs the org.wso2.carbon.jartobundle-tool.jar tool.

    - This tool is capable of converting specified jar files to their corresponding OSGi bundles.

    Usage: jartobundle.sh [source] [destination]

        -- source       source jar file/directory path containing jar file(s) to be converted to
                           OSGi bundle(s)

        -- destination  destination directory path in which the OSGi bundles are to be created

4. osgi-lib.sh & osgi-lib.bat
    - The script files which run the OSGi-Lib deployer tool.

    - This tool updates the bundles.info file of the specified Carbon Runtime.

    Usage : osgi-lib.sh [runtime]

        -- runtime      name of the Carbon Runtime to be updated or keyword 'ALL' to update all the runtimes.
