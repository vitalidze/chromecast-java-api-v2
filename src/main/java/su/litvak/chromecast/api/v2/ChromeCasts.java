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
import java.util.List;

/**
 * Utility class that discovers ChromeCast devices and holds references to all of them.
 */
public class ChromeCasts extends ArrayList<ChromeCast> implements ServiceListener {
    private final static ChromeCasts INSTANCE = new ChromeCasts();

    private JmDNS mDNS;

    private List<ChromeCastsListener> listeners = new ArrayList<ChromeCastsListener>();

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
            mDNS = null;
        }
    }

    @Override
    public void serviceAdded(ServiceEvent event) {
        if (event.getInfo() != null) {
            ChromeCast device = new ChromeCast(mDNS, event.getInfo().getName());
            add(device);
            for (ChromeCastsListener listener : listeners) {
                listener.newChromeCastDiscovered(device);
            }
        }
    }

    @Override
    public void serviceRemoved(ServiceEvent event) {
        if (ChromeCast.SERVICE_TYPE.equals(event.getType())) {
            // We have a ChromeCast device unregistering
            List<ChromeCast> copy = new ArrayList<ChromeCast>(this);
            ChromeCast deviceRemoved = null;
            // Probably better keep a map to better lookup devices
            for (ChromeCast device : copy) {
                if (device.getName().equals(event.getInfo().getName())) {
                    deviceRemoved = device;
                    this.remove(device);
                    break;
                }
            }
            if (deviceRemoved != null) {
                for (ChromeCastsListener listener : listeners) {
                    listener.chromeCastRemoved(deviceRemoved);
                }
            }
        }
    }

    @Override
    public void serviceResolved(ServiceEvent event) {
    }

    /**
     * Starts ChromeCast device discovery
     */
    public static void startDiscovery() throws IOException {
        INSTANCE._startDiscovery();
    }

    /**
     * Stops ChromeCast device discovery
     */
    public static void stopDiscovery() throws IOException {
        INSTANCE._stopDiscovery();
    }

    /**
     * Restarts discovery by sequentially calling 'stop' and 'start' methods
     */
    public static void restartDiscovery() throws IOException {
        stopDiscovery();
        startDiscovery();
    }

    /**
     * @return singleton container holding all discovered devices
     */
    public static ChromeCasts get() {
        return INSTANCE;
    }

    public static void registerListener(ChromeCastsListener listener) {
        if (listener != null) {
            INSTANCE.listeners.add(listener);
        }
    }

    public static void unregisterListener(ChromeCastsListener listener) {
        if (listener != null) {
            INSTANCE.listeners.remove(listener);
        }
    }
}
