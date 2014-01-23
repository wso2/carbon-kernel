Steps to build and run the Savan-Eventing sample
------------------------------------------------

To build
--------

1. Move to the sample folder.
2. Create a subfolders 'build/lib'.
3. Copy all the jars from a compatible Axis2 distribution to 'build/lib'.
4. Copy the savan jar file to 'build/lib'.
5. Run 'ant' to build the sample.


To run
------
1. Start a Axis2 server.
2. Deploy savan and addressing modules.
3. Deploy the three services that were created in the 'build' folder.
4. Run the 'samples.eventing.Client' class, you can pass the repository with a '-r' parameter and the server port with a '-p' parameter.
