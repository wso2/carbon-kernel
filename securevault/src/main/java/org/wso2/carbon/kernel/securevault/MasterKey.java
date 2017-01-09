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

import java.util.Optional;

/**
 * This class represents a master key that is needed to initialize a secret repository. A secret repository might need
 *  a few master keys to get it initialized.
 *
 * @since 5.2.0
 */
public class MasterKey {
    private String masterKeyName;
    private Optional<char[]> optMasterKeyValue = Optional.empty();

    public MasterKey(String masterKeyName) {
        this.masterKeyName = masterKeyName;
    }

    public String getMasterKeyName() {
        return masterKeyName;
    }

    public Optional<char[]> getMasterKeyValue() {
        return optMasterKeyValue;
    }

    public void setMasterKeyValue(char[] masterKeyValue) {
        this.optMasterKeyValue = Optional.ofNullable(masterKeyValue);
    }
}
