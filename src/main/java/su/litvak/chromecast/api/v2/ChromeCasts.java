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

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.util.ArrayList;

public class ChromeCasts extends ArrayList<ChromeCast> implements ServiceListener {
    private final static ChromeCasts INSTANCE = new ChromeCasts();

    private JmDNS mDNS;

    private ChromeCasts() {
    }

    private void _startDiscovery() throws IOException {
        if (mDNS == null) {
            mDNS = JmDNS.create();
            mDNS.addServiceListener(ChromeCast.SERVICE_TYPE, this);
        }
    }

    private void _stopDiscovery() throws IOException {
        if (mDNS != null) {
            mDNS.close();
        }
    }

    @Override
    public void serviceAdded(ServiceEvent event) {
        if (event.getInfo() != null) {
            add(new ChromeCast(mDNS, event.getInfo().getName()));
        }
    }

    @Override
    public void serviceRemoved(ServiceEvent event) {
    }

    @Override
    public void serviceResolved(ServiceEvent event) {
    }

    public static void startDiscovery() throws IOException {
        INSTANCE._startDiscovery();
    }

    public static void stopDiscovery() throws IOException {
        INSTANCE._stopDiscovery();
    }

    public static ChromeCasts get() {
        return INSTANCE;
    }
}
