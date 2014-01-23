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

package org.apache.axis2.jaxws.catalog;

import org.apache.xml.resolver.Catalog;

import java.util.Vector;

public abstract interface JAXWSCatalogManager {

  /**
   * What is the current verbosity?
   */
  abstract public int getVerbosity();

  /**
   * Set the current verbosity.
   */
  abstract public void setVerbosity (int verbosity);

  /**
   * Return the current list of catalog files.
   *
   * @return A vector of the catalog file names or null if no catalogs
   * are available in the properties.
   */
  abstract public Vector getCatalogFiles();
  
  /**
   * Set the list of catalog files.
   */
  abstract public void setCatalogFiles(String fileList);

  /**
   * Return the current prefer public setting.
   *
   * @return True if public identifiers are preferred.
   */
  abstract public boolean getPreferPublic ();
  
  /**
   * Set the prefer public setting.
   */
  abstract public void setPreferPublic (boolean preferPublic);

  /**
   * Get the current use static catalog setting.
   */
  abstract public boolean getUseStaticCatalog();

  /**
   * Set the use static catalog setting.
   */
  abstract public void setUseStaticCatalog(boolean useStatic);

  /**
   * Get a new catalog instance.
   *
   * This method always returns a new instance of the underlying catalog class.
   */
  abstract public Catalog getPrivateCatalog(); 
  
  /**
   * Get a catalog instance.
   *
   * If this manager uses static catalogs, the same static catalog will
   * always be returned. Otherwise a new catalog will be returned.
   */
  abstract public Catalog getCatalog();

  /**
   * Get the current Catalog class name.
   */
  abstract public String getCatalogClassName();

  /**
   * Set the Catalog class name.
   */
  abstract public void setCatalogClassName(String className); 
}
