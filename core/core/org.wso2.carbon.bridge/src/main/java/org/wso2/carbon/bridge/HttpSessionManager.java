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
package org.wso2.carbon.bridge;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used for holding all the HttpSessions created in Carbon. It is used for
 * managing & invalidating all sessions created in the system
 * <p/>
 * It is also an HttpSessionListener registered in the web.xml file.
 */
public class HttpSessionManager implements HttpSessionListener {
    private static Map<String, CarbonHttpSession> sessions =
            new ConcurrentHashMap<String, CarbonHttpSession>();

    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        HttpSession session = httpSessionEvent.getSession();
        sessions.put(session.getId(), new CarbonHttpSession(session));
    }

    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        HttpSession session = httpSessionEvent.getSession();
        sessions.remove(session.getId());
    }

    public static CarbonHttpSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public static void invalidateSessions() {
        for (HttpSession session : sessions.values()) {
            try {
                session.invalidate();
            } catch (Throwable ignored) {
            }
        }
        sessions.clear();
    }
}
