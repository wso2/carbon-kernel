/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.maven2.mar;

import java.io.File;


/**
 * A FileSet defines additional files, which are being added to the
 * Axis application archive. The objects structure follows the
 * FileSet object from the maven-assembly-plugin, see the
 * <a href="http://maven.apache.org/plugins/maven-assembly-plugin/assembly.html">plugin documentation</a>
 * for details.
 */
public class FileSet
{
    private File directory;
    private String outputDirectory;
    private String[] includes, excludes;
    private boolean skipDefaultExcludes;

    /**
     * Returns the file sets base directory. May be omitted, in which
     * case the projects base directory is assumed.
     */
    public File getDirectory()
    {
        return directory;
    }

    /**
     * Sets the file sets base directory. May be omitted, in which
     * case the projects base directory is assumed.
     */
    public void setDirectory( File directory )
    {
        this.directory = directory;
    }

    /**
     * Returns the file sets exclusion list.
     */
    public String[] getExcludes()
    {
        return excludes;
    }

    /**
     * Sets the file sets exclusion list.
     */
    public void setExcludes( String[] excludes )
    {
        this.excludes = excludes;
    }

    /**
     * Returns the file sets inclusion list.
     */
    public String[] getIncludes()
    {
        return includes;
    }

    /**
     * Sets the file sets inclusion list.
     */
    public void setIncludes( String[] includes )
    {
        this.includes = includes;
    }

    /**
     * Sets a prefix, which the included files should have
     * when being added to the Axis application archive.
     */
    public String getOutputDirectory()
    {
        return outputDirectory;
    }

    /**
     * Returns a prefix, which the included files should have
     * when being added to the Axis application archive.
     */
    public void setOutputDirectory( String outputDirectory )
    {
        this.outputDirectory = outputDirectory;
    }

    /**
     * Returns, whether the default excludes should be used. Defaults
     * to true.
     */
    public boolean isSkipDefaultExcludes()
    {
        return skipDefaultExcludes;
    }

    /**
     * Sets, whether the default excludes should be used. Defaults to
     * true.
     */
    public void setSkipDefaultExcludes( boolean skipDefaultExcludes )
    {
        this.skipDefaultExcludes = skipDefaultExcludes;
    }
}
