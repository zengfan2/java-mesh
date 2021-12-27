/*
 * Copyright (C) Ltd. 2021-2021. Huawei Technologies Co., All rights reserved
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


import HTTPClient.Cookie
import net.grinder.scriptengine.groovy.junit.annotation.AfterProcess;
import net.grinder.scriptengine.groovy.junit.annotation.AfterThread;
import org.hamcrest.Matchers;
import org.junit.After;

import static net.grinder.script.Grinder.grinder;
import static org.junit.Assert.*;
import net.grinder.plugin.http.HTTPPluginControl;
import net.grinder.plugin.http.HTTPRequest;
import net.grinder.script.GTest;
import net.grinder.scriptengine.groovy.junit.GrinderRunner;
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess;
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread;
import net.grinder.scriptengine.groovy.junit.GrinderRunner

import org.junit.runner.RunWith
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import HTTPClient.CookieModule;
import HTTPClient.HTTPResponse;
import HTTPClient.NVPair;

@RunWith(GrinderRunner)
class GroovyTemplate {

    public static HTTPRequest request;
    public Map variables;
    public HTTPResponse httpResult;

    @BeforeProcess
    public static void beforeProcess() {
        HTTPPluginControl.getConnectionDefaults().timeout = 6000;
        request = new HTTPRequest();
    }

    @BeforeThread
    public void beforeThread() {
        grinder.statistics.delayReports = true;
        // reset to the all cookies
        def threadContext = HTTPPluginControl.getThreadHTTPClientContext();
        CookieModule.listAllCookies(threadContext).each {
            CookieModule.removeCookie(it, threadContext);
        }
    }

    @Before
    public void before() {
        def threadContext = HTTPPluginControl.getThreadHTTPClientContext();
        CookieModule.addCookie(new Cookie(""),threadContext);
    }

    @Test
    public void test() {
    }

    @After
    public void after() {
    }

    @AfterThread
    public void afterThread() {
    }

    @AfterProcess
    public static void afterProcess() {
    }
}
