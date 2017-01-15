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
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that discovers ChromeCast devices and holds references to all of them.
 */
public class ChromeCasts {
    private final static ChromeCasts INSTANCE = new ChromeCasts();
    private final MyServiceListener listener = new MyServiceListener();

    private JmDNS mDNS;

    private final List<ChromeCastsListener> listeners = new ArrayList<ChromeCastsListener>();
    private final List<ChromeCast> chromeCasts = new ArrayList<ChromeCast>();
    
    private ChromeCasts() {
    }
    
    /** Returns a copy of the currently seen chrome casts.
     * @return a copy of the currently seen chromecast devices.
     */
    public static List<ChromeCast> get() {
        synchronized (INSTANCE.chromeCasts) {
            return new ArrayList<ChromeCast>(INSTANCE.chromeCasts);
        }
    }
    
    /** Hidden service listener to receive callbacks.
     * Is hidden to avoid messing with it.
     */
    private class MyServiceListener implements ServiceListener {
        @Override
        public void serviceAdded(ServiceEvent se) {
        if (se.getInfo() != null) {
            ChromeCast device = new ChromeCast(mDNS, se.getInfo().getName());
            synchronized(chromeCasts) {
                chromeCasts.add(device);
            }
            for (ChromeCastsListener listener : listeners) {
                listener.newChromeCastDiscovered(device);
            }
        }
        }

        @Override
        public void serviceRemoved(ServiceEvent se) {
        if (ChromeCast.SERVICE_TYPE.equals(se.getType())) {
            // We have a ChromeCast device unregistering
            List<ChromeCast> copy = list();
            ChromeCast deviceRemoved = null;
            // Probably better keep a map to better lookup devices
            for (ChromeCast device : copy) {
                if (device.getName().equals(se.getInfo().getName())) {
                    deviceRemoved = device;
                    synchronized(chromeCasts) {
                        chromeCasts.remove(device);
                    }
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
        public void serviceResolved(ServiceEvent se) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }

    private void _startDiscovery(InetAddress addr) throws IOException {
        if (mDNS == null) {
            if(addr != null) {
                mDNS = JmDNS.create(addr);
            } else {
                mDNS = JmDNS.create();
            }            
            mDNS.addServiceListener(ChromeCast.SERVICE_TYPE, listener);
        }
    }

    private void _stopDiscovery() throws IOException {
        if (mDNS != null) {
            mDNS.close();
            mDNS = null;
        }
    }

    /**
     * Starts ChromeCast device discovery
     */
    public static void startDiscovery() throws IOException {
        INSTANCE._startDiscovery(null);
    }
    
    /**
     * Starts ChromeCast device discovery
     *
     * @param addr the address of the interface that should be used for discovery
     */
    public static void startDiscovery(InetAddress addr) throws IOException {
        INSTANCE._startDiscovery(addr);
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
        startDiscovery(null);
    }
    
    /**
     * Restarts discovery by sequentially calling 'stop' and 'start' methods
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
