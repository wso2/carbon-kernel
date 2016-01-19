/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.caching.internal.processor;

import org.wso2.carbon.caching.internal.CarbonCachedValue;

import javax.cache.processor.MutableEntry;

/**
 * The operation to perform on a {@link CarbonCachedValue} as a result of
 * actions performed on a {@link MutableEntry}.
 */
public enum MutableEntryOperation {
  /**
   * Don't perform any operations on the {@link CarbonCachedValue}.
   */
  NONE,

  /**
   * Access an existing {@link CarbonCachedValue}.
   */
  ACCESS,

  /**
   * Create a new {@link CarbonCachedValue}.
   */
  CREATE,

  /**
   * Loaded a new {@link CarbonCachedValue}.
   */
  LOAD,

  /**
   * Remove the {@link CarbonCachedValue} (and thus the Cache Entry).
   */
  REMOVE,

  /**
   * Update the {@link CarbonCachedValue}.
   */
  UPDATE;
}
