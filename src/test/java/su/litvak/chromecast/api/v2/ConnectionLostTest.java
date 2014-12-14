/*
 * Copyright 2014 Vitaly Litvak (vitavaque@gmail.com)
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
package su.litvak.chromecast.api.v2;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class ConnectionLostTest {
    MockedChromeCast chromeCastStub;

    @Before
    public void initMockedCast() throws Exception {
        chromeCastStub = new MockedChromeCast();
    }

    @Test
    public void test() throws Exception {
        ChromeCast cast = new ChromeCast("localhost");
        cast.connect();
        cast.disconnect();
    }

    @After
    public void shutdown() throws IOException {
        chromeCastStub.close();
    }
}
