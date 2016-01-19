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

import javax.cache.CacheException;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;

/**
 * An implementation of the {@link EntryProcessorResult}.
 *
 * @param <T> the type of {@link javax.cache.processor.EntryProcessor} result
 */
public class CarbonEntryProcessorResult<T> implements EntryProcessorResult<T> {
  /**
   * The result of processing the entry.
   */
  private final T result;

  /**
   * The {@link CacheException} that may have occurred executing an {@link javax.cache.processor.EntryProcessor}.
   */
  private final CacheException exception;

  /**
   * Constructs an {@link CarbonEntryProcessorResult} for a resulting value
   *
   * @param result  the result
   */
  public CarbonEntryProcessorResult(T result) {
    this.result = result;
    this.exception = null;
  }

  /**
   * Constructs an {@link CarbonEntryProcessorResult} for an {@link Exception},
   * that of which will be returned wrapped as an {@link EntryProcessorException}.
   *
   * @param exception  the {@link Exception}
   */
  public CarbonEntryProcessorResult(Exception exception) {
    this.result = null;
    this.exception = new EntryProcessorException(exception);
  }

  @Override
  public T get() throws EntryProcessorException {
    if (exception == null) {
      return result;
    } else {
      throw exception;
    }
  }
}
