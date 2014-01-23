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

import org.wso2.carbon.base.ServerConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for handling data pagination
 */
public final class DataPaginator {

    private DataPaginator() {
    }

    private static final int DEFAULT_NO_OF_ITEMS_PER_PAGE = 10;


    /**
     * A reusable generic method for doing item paging
     *
     * @param pageNumber The page required. Page number starts with 0.
     * @param sourceList The original list of items
     * @param pageable   The type of Pageable item
     * @return Returned page
     */
    public static <C> List<C> doPaging(int pageNumber, List<C> sourceList, Pageable pageable) {
    	
    	if (sourceList.size() == 0) {
            return sourceList;
        }
    	
    	int pageToReturn;
    	if (pageNumber < 0 || pageNumber == Integer.MAX_VALUE) {
    		pageToReturn = 0;
        } else {
        	pageToReturn = pageNumber;
        }
        
        String itemsPerPage = ServerConfiguration.getInstance().getFirstProperty("ItemsPerPage");
        int itemsPerPageInt = DEFAULT_NO_OF_ITEMS_PER_PAGE; // the default number of item per page
        if (itemsPerPage != null) {
            itemsPerPageInt = Integer.parseInt(itemsPerPage);
        }
        int numberOfPages = (int) Math.ceil((double) sourceList.size() / itemsPerPageInt);
        if (pageToReturn > numberOfPages - 1) {
        	pageToReturn = numberOfPages - 1;
        }
        int startIndex = pageToReturn * itemsPerPageInt;
        int endIndex = (pageToReturn + 1) * itemsPerPageInt;
        List<C> returnList = new ArrayList<C>();
        for (int i = startIndex; i < endIndex && i < sourceList.size(); i++) {
            returnList.add(sourceList.get(i));
        }

        pageable.setNumberOfPages(numberOfPages);
        pageable.set(returnList);
        return returnList;
    }
}
