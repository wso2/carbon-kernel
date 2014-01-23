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

package org.apache.axis2.jibx.beans;

public class Book {
    private String m_type;
    private String m_isbn;
    private String m_title;
    private String[] m_authors;

    public Book() {
    }

    public Book(String type, String isbn, String title, String[] authors) {
        m_isbn = isbn;
        m_title = title;
        m_type = type;
        m_authors = authors;
    }

    public String getType() {
        return m_type;
    }

    public String getIsbn() {
        return m_isbn;
    }

    public String getTitle() {
        return m_title;
    }

    public String[] getAuthors() {
        return m_authors;
    }
}