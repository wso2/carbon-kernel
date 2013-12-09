${product.name} v${product.version}


This file explains the usages of all the scipts contained within
this directory.

1. README.txt
    - This file

2. version.txt
    - A simple text file used for storing the version

3. wso2server.sh & wso2server.bat
    - The main script file used for running the server.

    Usage: wso2server.sh [commands] [system-properties]

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

		-Dcarbon.config.dir.path=[path]
				Overwrite the conf directory path where we keep all 
				configuration files like carbon.xml, etc.

		-Dcarbon.logs.path=[path]
				Define the path to keep Log files.

		-Dcomponents.repository=[path]
				Overwrite the default location we keep all the OSGi bundles.
