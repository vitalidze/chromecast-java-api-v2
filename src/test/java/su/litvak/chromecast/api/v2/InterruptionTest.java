/*
 * Copyright 2018 Vitaly Litvak (vitavaque@gmail.com)
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

import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class InterruptionTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    MockedChromeCast chromeCastStub;
    ChromeCast cast = new ChromeCast("localhost");
    CyclicBarrier barrier = new CyclicBarrier(2);

    static class Custom implements Request, Response {
        Long requestId;
        @Override
        public Long getRequestId() {
            return requestId;
        }

        @Override
        public void setRequestId(Long requestId) {
            this.requestId = requestId;
        }
    }

    @Before
    public void initMockedCast() throws Exception {
        chromeCastStub = new MockedChromeCast();
        cast.connect();
        cast.launchApp("abcd");
    }

    @Test
    public void testInterrupt() throws IOException, ExecutionException, InterruptedException, BrokenBarrierException {
        chromeCastStub.customHandler = new MockedChromeCast.CustomHandler() {
            @Override
            public Response handle(JsonNode json) {
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return new Custom();
            }
        };


        final AtomicReference<IOException> exception = new AtomicReference<IOException>();
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    barrier.await();
                    cast.send("", new Custom());
                } catch (IOException e) {
                    exception.set(e);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
        barrier.await();
        t.interrupt();
        barrier.await();
        assertNotNull(exception.get());
        assertTrue(exception.get() instanceof ChromeCastException);
        assertTrue(exception.get().getCause() instanceof InterruptedException);
        assertEquals("Interrupted while waiting for response", exception.get().getMessage());
    }

    @Test
    public void testTimeOut() throws IOException {
        chromeCastStub.customHandler = new MockedChromeCast.CustomHandler() {
            @Override
            public Response handle(JsonNode json) {
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return new Custom();
            }
        };
        cast.setRequestTimeout(100L);
        thrown.expect(ChromeCastException.class);
        thrown.expectCause(CoreMatchers.is(TimeoutException.class));
        thrown.expectMessage("Waiting for response timed out");
        cast.send("", new Custom(), Custom.class);
    }

    @After
    public void destroy() throws IOException {
        cast.disconnect();
        chromeCastStub.close();
    }
}
