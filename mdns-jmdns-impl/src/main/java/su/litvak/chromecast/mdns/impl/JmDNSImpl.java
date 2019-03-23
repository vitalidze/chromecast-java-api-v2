/*
 * Copyright 2019 Vitaly Litvak (vitavaque@gmail.com)
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
package su.litvak.chromecast.mdns.impl;

import su.litvak.chromecast.mdns.api.MulticastDNS;
import su.litvak.chromecast.mdns.api.MulticastDNSServiceInfo;
import su.litvak.chromecast.mdns.api.MulticastDNSServiceListener;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import java.io.IOException;

/**
 * Multicast DNS implementation based on JmDNS.
 */
final class JmDNSImpl implements MulticastDNS {
    private final JmDNS jmDNS;

    JmDNSImpl(JmDNS jmDNS) {
        this.jmDNS = jmDNS;
    }

    @Override
    public void addServiceListener(String serviceType, MulticastDNSServiceListener serviceListener) {
        jmDNS.addServiceListener(serviceType, new JmDNSServiceListener(this, serviceListener));
    }

    @Override
    public MulticastDNSServiceInfo getServiceInfo(String serviceType, String serviceName) {
        ServiceInfo serviceInfo = jmDNS.getServiceInfo(serviceType, serviceName);
        return serviceInfo == null ? null : new JmDNSServiceInfo(serviceInfo);
    }

    @Override
    public void close() throws IOException {
        jmDNS.close();
    }
}
