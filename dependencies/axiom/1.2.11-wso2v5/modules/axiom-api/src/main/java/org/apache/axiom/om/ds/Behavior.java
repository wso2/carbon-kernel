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
package org.apache.axiom.om.ds;

public class Behavior {
    // An OMDataSource must communicate whether the data source is
    // can be queried without destruction.  OM makes decisions about
    // caching (etc) based on whether the data source query is 
    // destructive or not destructive.
    // 
    // The Behavior flag indicates the Behavior of this OMDataSource
    // DESTRUCTIVE:
    //    Indicates that the backing data can only be read one time.
    //    AFFECT ON OM:
    //    The OM tree will automatically make a OM cache of the 
    //    the tree
    // 
    // NOT_DESTRUCTIVE
    //    Indicates that the data may be queried multiple times.
    //    The InputStream's data is either copied or marks are used to
    //    allow the data to be read again.
    //    AFFECT ON OM:
    //    The OM tree will not automatically make a OM cache of the tree.
    //
    // ONE_USE_UNSAFE:
    //    Indicates that the data may be queried only one time.  The
    //    second query will cause an immediate failure.  This is an unsafe
    //    mode because it violates the OM contract.  The implementation of 
    //    this mode is done by lying to the OM model.  We tell it that the 
    //    data is not destructive, and yet we don't make a copy.
    //
	
    public static final int DESTRUCTIVE = 0;
    public static final int NOT_DESTRUCTIVE = 1;
    public static final int ONE_USE_UNSAFE = 2;
}
