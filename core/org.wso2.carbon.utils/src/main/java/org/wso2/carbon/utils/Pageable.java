/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.utils;

import java.util.List;

/**
 * Any data set which can be paged (as page 1, page 2, page3 ...) should implement this interface.
 * This interface is used in conjunction with
 * {@link org.wso2.carbon.utils.DataPaginator#doPaging(int, List, Pageable)}
 *
 * @see DataPaginator#doPaging(int, List, Pageable)
 */
public interface Pageable {

    /**
     * Get the total number of pages
     *
     * @return  the total number of pages
     */
    int getNumberOfPages();

    /**
     * Set the total number of pages
     *
     * @param numberOfPages The total number of pages
     */
    void setNumberOfPages(int numberOfPages);

    /**
     * Set the paged items
     *
     * @param t The collection
     * @param <T> The type of objects in the collection
     */
    <T extends Object> void set(List<T> t);
}
