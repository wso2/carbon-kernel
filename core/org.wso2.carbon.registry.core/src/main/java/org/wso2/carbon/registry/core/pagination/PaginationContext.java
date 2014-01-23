/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.core.pagination;


/**
 * This contains the details that is required for resource pagination.
 */
public class PaginationContext {

    //Don't allow to init by outside
    private PaginationContext() {

    }

    private int start;
    private int count;
    private String sortOrder;
    private String sortBy;
    private int limit;
    private int length;

    private static final ThreadLocal<PaginationContext> PAGINATION_CONTEXT_THREAD_LOCAL
            = new ThreadLocal<PaginationContext>();

    public static PaginationContext init(int start, int count,
                                         String sortOrder, String sortBy, int limit) {
        PaginationContext paginationContext = new PaginationContext();
        paginationContext.setStart(start);
        paginationContext.setCount(count);
        paginationContext.setSortOrder(sortOrder);
        paginationContext.setSortBy(sortBy);
        paginationContext.setLimit(limit);
        setPaginationContextThreadLocal(paginationContext);

        return paginationContext;
    }

    /**
     * Set PaginationContext to threadLocal
     *
     * @param paginationContext PaginationContext
     */
    private static void setPaginationContextThreadLocal(PaginationContext paginationContext) {
        PAGINATION_CONTEXT_THREAD_LOCAL.set(paginationContext);

    }

    /**
     * Destroy the already initialize PaginationContext
     */
    public static void destroy() {
        PAGINATION_CONTEXT_THREAD_LOCAL.remove();
    }

    /**
     * Get PaginationContext
     *
     * @return PaginationContext
     */
    public static PaginationContext getInstance() {
        return PAGINATION_CONTEXT_THREAD_LOCAL.get();
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }


}
