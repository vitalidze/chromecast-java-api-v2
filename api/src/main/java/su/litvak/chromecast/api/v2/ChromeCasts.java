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

import su.litvak.chromecast.mdns.api.MulticastDNS;
import su.litvak.chromecast.mdns.api.MulticastDNSFactory;
import su.litvak.chromecast.mdns.api.MulticastDNSServiceInfo;
import su.litvak.chromecast.mdns.api.MulticastDNSServiceListener;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class that discovers ChromeCast devices and holds references to all of them.
 */
public final class ChromeCasts {
    private static final ChromeCasts INSTANCE = new ChromeCasts();
    private final MyServiceListener listener = new MyServiceListener();

    private MulticastDNSFactory mDNSFactory;
    private MulticastDNS mDNS;

    private final List<ChromeCastsListener> listeners = new ArrayList<ChromeCastsListener>();
    private final List<ChromeCast> chromeCasts = Collections.synchronizedList(new ArrayList<ChromeCast>());

    private ChromeCasts() {
    }

    /** Returns a copy of the currently seen chrome casts.
     * @return a copy of the currently seen chromecast devices.
     */
    public static List<ChromeCast> get() {
        return new ArrayList<ChromeCast>(INSTANCE.chromeCasts);
    }

    /** Hidden service listener to receive callbacks.
     * Is hidden to avoid messing with it.
     */
    private class MyServiceListener implements MulticastDNSServiceListener {
        @Override
        public void serviceAdded(MulticastDNS multicastDNS, String serviceType, String serviceName) {
            if (serviceType != null && serviceName != null) {
                MulticastDNSServiceInfo serviceInfo =  multicastDNS.getServiceInfo(serviceType, serviceName);
                ChromeCast device = new ChromeCast(serviceInfo, serviceName);
                chromeCasts.add(device);
                for (ChromeCastsListener nextListener : listeners) {
                    nextListener.newChromeCastDiscovered(device);
                }
            }
        }

        @Override
        public void serviceRemoved(MulticastDNS multicastDNS, String serviceType, String serviceName) {
            if (ChromeCast.SERVICE_TYPE.equals(serviceType)) {
                // We have a ChromeCast device unregistering
                List<ChromeCast> copy = get();
                ChromeCast deviceRemoved = null;
                // Probably better keep a map to better lookup devices
                for (ChromeCast device : copy) {
                    if (device.getName().equals(serviceName)) {
                        deviceRemoved = device;
                        chromeCasts.remove(device);
                        break;
                    }
                }
                if (deviceRemoved != null) {
                    for (ChromeCastsListener nextListener : listeners) {
                        nextListener.chromeCastRemoved(deviceRemoved);
                    }
                }
            }
        }
    }

    private void doStartDiscovery(InetAddress addr) throws IOException {
        if (mDNS == null) {
            if (addr != null) {
                mDNS = mDNSFactory.create(addr);
            } else {
                mDNS = mDNSFactory.create();
            }
            mDNS.addServiceListener(ChromeCast.SERVICE_TYPE, listener);
        }
    }

    private void doStopDiscovery() throws IOException {
        if (mDNS != null) {
            mDNS.close();
            mDNS = null;
        }
    }

    /**
     * Starts ChromeCast device discovery.
     */
    public static void startDiscovery() throws IOException {
        INSTANCE.doStartDiscovery(null);
    }

    /**
     * Starts ChromeCast device discovery.
     *
     * @param addr the address of the interface that should be used for discovery
     */
    public static void startDiscovery(InetAddress addr) throws IOException {
        INSTANCE.doStartDiscovery(addr);
    }

    /**
     * Stops ChromeCast device discovery.
     */
    public static void stopDiscovery() throws IOException {
        INSTANCE.doStopDiscovery();
    }

    /**
     * Restarts discovery by sequentially calling 'stop' and 'start' methods.
     */
    public static void restartDiscovery() throws IOException {
        stopDiscovery();
        startDiscovery();
    }

    /**
     * Restarts discovery by sequentially calling 'stop' and 'start' methods.
     *
     * @param addr the address of the interface that should be used for discovery
     */
    public static void restartDiscovery(InetAddress addr) throws IOException {
        stopDiscovery();
        startDiscovery(addr);
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
