/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.identity.claim;

import org.wso2.carbon.identity.authn.StoreIdentifier;
import org.wso2.carbon.identity.commons.AttributeIdentifier;

import java.util.List;
import java.util.Properties;

public interface ClaimManager {

    /**
     * @param properties
     */
    public void init(Properties properties);

    /**
     * @param dialectUri
     * @return
     */
    public Dialect getDialect(DialectIdentifier dialectUri);

    /**
     * @return
     */
    public List<DialectIdentifier> getAllDialectUris();

    /**
     * @return
     */
    public List<Dialect> getAllDialects();

    /**
     * @param dialectUri
     * @return
     */
    public List<ClaimIdentifier> getAllClaimUris(DialectIdentifier dialectUri);

    /**
     * @param dialectUri
     * @return
     */
    public List<MetaClaim> getAllClaims(DialectIdentifier dialectUri);

    /**
     * @param dialect
     */
    public void addDialect(Dialect dialect);

    /**
     * @param dialectUri
     */
    public void dropDialect(DialectIdentifier dialectUri);

    /**
     * @param dialect
     */
    public void updateDialectIdentifier(DialectIdentifier oldDialect, DialectIdentifier newDialect);

    /**
     * @param dialect
     * @param claims
     */
    public void addClaimsToDialect(DialectIdentifier dialectUri, List<MetaClaim> claims);

    /**
     * @param dialect
     * @param claims
     */
    public void removeClaimsFromDialect(DialectIdentifier dialectUri, ClaimIdentifier[] claimUris);

    /**
     * @param dialectUri
     * @param claimUri
     * @param storeIdentifier
     * @return
     */
    public AttributeIdentifier getAttributeIdentifier(DialectIdentifier dialectUri,
                                                      ClaimIdentifier claimUri, StoreIdentifier storeIdentifier);

    /**
     * @param dialectUri
     * @param storeIdentifier
     * @return
     */
    public List<AttributeIdentifier> getAllAttributeIdentifiers(DialectIdentifier dialectUri,
                                                                StoreIdentifier storeIdentifier);

}
