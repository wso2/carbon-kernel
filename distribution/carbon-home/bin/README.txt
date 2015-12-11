${product.name} v${product.version}


This file explains the usages of all the scripts contained within
this directory.

1. README.txt
    - This file

2. kernel-version.txt
    - A simple text file used for storing the version

3. carbon.sh & carbon.bat
    - The main script file used for running the server.

    Usage: carbon.sh [commands] [system-properties]

            commands:
                --debug <port>  Start the server in remote debugging mode.
                                port: The remote debugging port.
                --start		    Start Carbon using nohup
                --stop		    Stop the Carbon server process
                --restart	    Restart the Carbon server process
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

4. jartobundle.sh script
    - The script file which runs the org.wso2.carbon.jartobundle-tool.jar tool.

    - This tool is capable of converting specified jar files to their corresponding OSGi bundles.

    Usage: jartobundle.sh [source] [destination]

        -- source       source jar file/directory path containing jar file(s) to be converted to
                           OSGi bundle(s)

        -- destination  destination directory path in which the OSGi bundles are to be created