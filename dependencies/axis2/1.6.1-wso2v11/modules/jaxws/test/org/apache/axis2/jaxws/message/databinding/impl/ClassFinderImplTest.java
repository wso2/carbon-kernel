
package org.apache.axis2.jaxws.message.databinding.impl;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.TestCase;

public class ClassFinderImplTest extends TestCase {

    public void testUpdateClassPath() {
        ClassFinderImpl finder = new ClassFinderImpl();
        URL[] mockpaths = new URL[] {};
        File f = new File(".");
        String filePath = f.getAbsolutePath();
        try{
            ClassLoader cl = new MockUCL(mockpaths);
            //Add a new file path to classpath
            finder.updateClassPath(filePath, cl);
            URL[] classPath = ((URLClassLoader)cl).getURLs();
            //check if the classpath was updated with the path.
            assertNotNull("ClassPath Object cannot be null",classPath);
            assertEquals("expected 1 object in path but found "+classPath.length,classPath.length, 1);
        }catch(Exception e){
            fail(e.getMessage());
        }
    }
    class MockUCL extends URLClassLoader {
        public MockUCL(URL[] urls) {
            super(urls);

        }

    }
}

