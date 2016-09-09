/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.kernel.securevault;


import org.wso2.carbon.kernel.securevault.config.model.MasterKeyReaderConfiguration;
import org.wso2.carbon.kernel.securevault.exception.SecureVaultException;

import java.util.List;

/**
 * This interface is used to register MasterKeyReaders. An implementation of this interface should be registered
 * as an OSGi service using the MasterKeyReader interface.
 *
 * The implementation of this interface can be different from one MasterKeyReader to another depending on its
 * requirements and behaviour.
 *
 * @since 5.2.0
 */
public interface MasterKeyReader {

    /**
     * An implementation of this method should initialize the MasterKeyReader, so that it could perform the
     * {@code readMasterKeys}.
     *
     * @param masterKeyReaderConfiguration  {@link MasterKeyReaderConfiguration}
     * @throws SecureVaultException     on an error while trying to initialize the MasterKeyReader
     */
    void init(MasterKeyReaderConfiguration masterKeyReaderConfiguration) throws SecureVaultException;

    /**
     * An implementation of this method should populate the master key value of all the {@link MasterKey}s
     * provided in the {@code masterKeys} list.
     *
     * @param masterKeys a list of {@link MasterKey}s
     * @throws SecureVaultException on an error while trying to read master keys
     */
    void readMasterKeys(List<MasterKey> masterKeys) throws SecureVaultException;
}
