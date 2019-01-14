/*
 * Copyright 2015 - 2016 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.api;

import org.eclipse.jetty.websocket.servlet.*;
import org.traccar.Context;
import org.traccar.api.resource.SessionResource;
import org.traccar.helper.LogAction;

import java.sql.SQLException;

public class AsyncSocketOpenServlet extends WebSocketServlet {

    private static final long ASYNC_TIMEOUT = 10 * 60 * 1000;

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.getPolicy().setIdleTimeout(Context.getConfig().getLong("web.timeout", ASYNC_TIMEOUT));
        factory.setCreator(new WebSocketCreator() {
            @Override
            public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
                if (req.getParameterMap() != null) {
                    try {
                        long trackerid = Long.valueOf(req.getParameterMap().get("trackerid").get(0));
                        return new AsyncOpenSocket(trackerid);
                    } catch (Exception e) {
                        //TODO Add loggers
                        return null;
                    }
                } else {
                    return null;
                }
            }
        });
    }

}
