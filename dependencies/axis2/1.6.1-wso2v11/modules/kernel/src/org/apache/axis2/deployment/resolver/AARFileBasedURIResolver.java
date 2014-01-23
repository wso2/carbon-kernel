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

package org.apache.axis2.deployment.resolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.resolver.DefaultURIResolver;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * A custom URI resolver that can
 */
public class AARFileBasedURIResolver extends DefaultURIResolver {

    protected static final Log log = LogFactory
            .getLog(AARFileBasedURIResolver.class);

    private File aarFile;
    private URI lastImportLocation;

    public AARFileBasedURIResolver(File aarFile) {
        this.aarFile = aarFile;
    }

    public InputSource resolveEntity(
            String targetNamespace,
            String schemaLocation,
            String baseUri) {
        //no issue with abloslute schemas 
        // this schema can be in a relative location for another base scheama. so first
        // try to see the proper location

         lastImportLocation = URI.create(baseUri).resolve(schemaLocation);
        if (isAbsolute(lastImportLocation.toString())) {
            return super.resolveEntity(
                    targetNamespace, schemaLocation, baseUri);
        } else {
            //validate
            if ((baseUri == null || "".equals(baseUri)) && schemaLocation.startsWith("..")) {
                throw new RuntimeException(
                        "Unsupported schema location " + schemaLocation);
            }

            ZipInputStream zin = null;
            try {
                zin = new ZipInputStream(new FileInputStream(aarFile));

                ZipEntry entry;
                byte[] buf = new byte[1024];
                int read;
                ByteArrayOutputStream out;
                String searchingStr = lastImportLocation.toString();
                while ((entry = zin.getNextEntry()) != null) {
                    String entryName = entry.getName().toLowerCase();
                    if (entryName.equalsIgnoreCase(searchingStr)) {
                        out = new ByteArrayOutputStream();
                        while ((read = zin.read(buf)) > 0) {
                            out.write(buf, 0, read);
                        }
                        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
                        InputSource inputSoruce = new InputSource(in);
                        inputSoruce.setSystemId(lastImportLocation.getPath());
                        inputSoruce.setPublicId(targetNamespace);
                        return inputSoruce;
                    }
                }


            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    if (zin != null) {
                        zin.close();
                    }
                } catch (IOException e) {
                    log.debug(e);
                }
            }

        }

        log.info("AARFileBasedURIResolver: Unable to resolve" + lastImportLocation);
        return null;
    }
    
}
