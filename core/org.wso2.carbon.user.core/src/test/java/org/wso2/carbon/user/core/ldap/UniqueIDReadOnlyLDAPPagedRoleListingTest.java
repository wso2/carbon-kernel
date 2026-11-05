/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.user.core.ldap;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.NotImplementedException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsResponseControl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/**
 * Tests the paged role listing of the LDAP user stores behind UserStore/enablePaginatedGroupListing.
 */
public class UniqueIDReadOnlyLDAPPagedRoleListingTest {

    private static final String DOMAIN = "TEST";
    private static final String SEARCH_BASE = "ou=Groups,dc=example,dc=com";
    private static final byte[] MORE = new byte[]{1};
    private static final byte[] LAST = new byte[0];

    private MockedStatic<ServerConfiguration> serverConfiguration;
    private ServerConfiguration configuration;
    private RealmConfiguration realmConfig;
    private LDAPConnectionContext connectionSource;
    private LdapContext ldapContext;
    private PagedLdapUserStoreManager manager;
    private final Deque<DirectoryPage> directoryPages = new ArrayDeque<>();
    private Control[] responseControls;

    /**
     * Sets the protected state the listing reads without a directory connection.
     */
    private static final class PagedLdapUserStoreManager extends UniqueIDReadOnlyLDAPUserStoreManager {

        private PagedLdapUserStoreManager(RealmConfiguration realmConfig, LDAPConnectionContext connectionSource) {

            this.realmConfig = realmConfig;
            this.connectionSource = connectionSource;
            this.readGroupsEnabled = true;
        }
    }

    /**
     * One page the directory answers: its entries and the paged results cookie, null for no response control.
     */
    private static final class DirectoryPage {

        private final List<String> names;
        private final byte[] cookie;

        private DirectoryPage(byte[] cookie, String... names) {

            this.names = Arrays.asList(names);
            this.cookie = cookie;
        }
    }

    @BeforeMethod
    public void setUp() throws Exception {

        configuration = mock(ServerConfiguration.class);
        serverConfiguration = mockStatic(ServerConfiguration.class);
        serverConfiguration.when(ServerConfiguration::getInstance).thenReturn(configuration);
        realmConfig = mock(RealmConfiguration.class);
        when(realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME)).thenReturn(DOMAIN);
        when(realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_ROLE_LIST)).thenReturn("3");
        when(realmConfig.getUserStoreProperty(LDAPConstants.GROUP_NAME_ATTRIBUTE)).thenReturn("cn");
        when(realmConfig.getUserStoreProperty(LDAPConstants.GROUP_NAME_LIST_FILTER))
                .thenReturn("(objectClass=groupOfNames)");
        when(realmConfig.getUserStoreProperty(LDAPConstants.GROUP_SEARCH_BASE)).thenReturn(SEARCH_BASE);
        directoryPages.clear();
        responseControls = null;
        ldapContext = mock(LdapContext.class);
        when(ldapContext.search(any(Name.class), anyString(), any(SearchControls.class))).thenAnswer(invocation -> {
            DirectoryPage page = directoryPages.poll();
            if (page == null) {
                return results(Collections.emptyList());
            }
            responseControls = page.cookie == null ? null : new Control[]{pagedResultsResponse(page.cookie)};
            return results(page.names);
        });
        when(ldapContext.getResponseControls()).thenAnswer(invocation -> responseControls);
        connectionSource = mock(LDAPConnectionContext.class);
        when(connectionSource.getContext()).thenReturn(ldapContext);
        manager = new PagedLdapUserStoreManager(realmConfig, connectionSource);
    }

    @AfterMethod
    public void tearDown() {

        serverConfiguration.close();
    }

    @Test
    public void testListingIsOffByDefault() throws Exception {

        directoryPages.add(new DirectoryPage(LAST, "g1"));
        assertThrows(NotImplementedException.class, () -> manager.doGetRoleNames("*", 3, 1));
        verify(connectionSource, never()).getContext();
    }

    @Test
    public void testListingIsOffWhenSwitchedOff() throws Exception {

        when(configuration.getFirstProperty(UserCoreConstants.PROP_ENABLE_PAGINATED_GROUP_LISTING)).thenReturn("false");
        directoryPages.add(new DirectoryPage(LAST, "g1"));
        assertThrows(NotImplementedException.class, () -> manager.doGetRoleNames("*", 3, 1));
        verify(connectionSource, never()).getContext();
    }

    @Test
    public void testZeroLimitListsNothing() throws Exception {

        switchOn();
        directoryPages.add(new DirectoryPage(LAST, "g1"));
        assertEquals(manager.doGetRoleNames("*", 0, 1).length, 0);
        verify(connectionSource, never()).getContext();
    }

    @Test
    public void testFirstPage() throws Exception {

        switchOn();
        threeDirectoryPages();
        assertEquals(manager.doGetRoleNames("*", 3, 1), new String[]{"TEST/g1", "TEST/g2", "TEST/g3"});
        // The page filled on the first directory page: one search for it and one to release the session, not two.
        verify(ldapContext, times(2)).search(any(Name.class), anyString(), any(SearchControls.class));
    }

    @Test
    public void testPageAcrossDirectoryPages() throws Exception {

        switchOn();
        threeDirectoryPages();
        assertEquals(manager.doGetRoleNames("*", 3, 4), new String[]{"TEST/g4", "TEST/g5", "TEST/g6"});
    }

    @Test
    public void testShortLastPage() throws Exception {

        switchOn();
        threeDirectoryPages();
        assertEquals(manager.doGetRoleNames("*", 3, 7), new String[]{"TEST/g7"});
    }

    @Test
    public void testPagePastTheEnd() throws Exception {

        switchOn();
        threeDirectoryPages();
        assertEquals(manager.doGetRoleNames("*", 3, 8).length, 0);
    }

    @Test
    public void testLimitAboveMaxRoleNameListLengthIsReset() throws Exception {

        switchOn();
        threeDirectoryPages();
        // MaxRoleNameListLength is 3, so a page of 50 is cut to 3, and a blank filter lists every role.
        assertEquals(manager.doGetRoleNames("", 50, 1), new String[]{"TEST/g1", "TEST/g2", "TEST/g3"});
    }

    @Test
    public void testNamesAreListedOnceAcrossSearchBases() throws Exception {

        switchOn();
        when(realmConfig.getUserStoreProperty(LDAPConstants.GROUP_SEARCH_BASE))
                .thenReturn(SEARCH_BASE + "#ou=Groups2,dc=example,dc=com");
        directoryPages.add(new DirectoryPage(LAST, "g1", "g2"));
        directoryPages.add(new DirectoryPage(LAST, "g2", "g3"));
        assertEquals(manager.doGetRoleNames("*", 3, 1), new String[]{"TEST/g1", "TEST/g2", "TEST/g3"});
    }

    @Test
    public void testOutstandingSessionIsReleasedWhenTheListingFails() throws Exception {

        switchOn();
        directoryPages.add(new DirectoryPage(MORE, "g1", "g2", "g3"));
        directoryPages.add(new DirectoryPage(null, "g4"));
        expectThrows(UserStoreException.class, () -> manager.doGetRoleNames("*", 3, 4));
        // Two page searches, then the release of the first page's session before the context is closed.
        verify(ldapContext, times(3)).search(any(Name.class), anyString(), any(SearchControls.class));
        verify(ldapContext).close();
    }

    @Test
    public void testDirectoryWithoutPagedResultsControlIsAnError() {

        switchOn();
        directoryPages.add(new DirectoryPage(null, "g1", "g2"));
        UserStoreException e = expectThrows(UserStoreException.class, () -> manager.doGetRoleNames("*", 3, 1));
        assertTrue(e.getMessage().contains("did not honour the paged results control"), e.getMessage());
    }

    private void switchOn() {

        when(configuration.getFirstProperty(UserCoreConstants.PROP_ENABLE_PAGINATED_GROUP_LISTING)).thenReturn("true");
    }

    private void threeDirectoryPages() {

        directoryPages.add(new DirectoryPage(MORE, "g1", "g2", "g3"));
        directoryPages.add(new DirectoryPage(MORE, "g4", "g5", "g6"));
        directoryPages.add(new DirectoryPage(LAST, "g7"));
    }

    /**
     * A paged results response control carrying the given cookie (BER: SEQUENCE { INTEGER 0, OCTET STRING cookie }).
     */
    private static PagedResultsResponseControl pagedResultsResponse(byte[] cookie) throws IOException {

        byte[] value = new byte[7 + cookie.length];
        value[0] = 0x30;
        value[1] = (byte) (5 + cookie.length);
        value[2] = 0x02;
        value[3] = 0x01;
        value[4] = 0x00;
        value[5] = 0x04;
        value[6] = (byte) cookie.length;
        System.arraycopy(cookie, 0, value, 7, cookie.length);
        return new PagedResultsResponseControl(PagedResultsResponseControl.OID, false, value);
    }

    /**
     * Search results with the given cn values.
     */
    private static NamingEnumeration<SearchResult> results(List<String> names) {

        Iterator<String> iterator = names.iterator();
        return new NamingEnumeration<SearchResult>() {

            @Override
            public SearchResult next() {

                return nextElement();
            }

            @Override
            public boolean hasMore() {

                return iterator.hasNext();
            }

            @Override
            public void close() {

            }

            @Override
            public boolean hasMoreElements() {

                return iterator.hasNext();
            }

            @Override
            public SearchResult nextElement() {

                String name = iterator.next();
                return new SearchResult("cn=" + name, null, new BasicAttributes("cn", name));
            }
        };
    }
}
