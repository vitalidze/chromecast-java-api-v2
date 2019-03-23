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

import su.litvak.chromecast.mdns.api.MulticastDNSServiceInfo;

import javax.jmdns.ServiceInfo;

import java.net.Inet4Address;

/**
 * Multicast DNS service information implementation based on JmDNS.
 */
final class JmDNSServiceInfo implements MulticastDNSServiceInfo {
    final ServiceInfo serviceInfo;

    JmDNSServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    @Override
    public Inet4Address[] getInet4Addresses() {
        return serviceInfo.getInet4Addresses();
    }

    @Override
    public int getPort() {
        return serviceInfo.getPort();
    }

    @Override
    public String[] getURLs() {
        return serviceInfo.getURLs();
    }

    @Override
    public String getApplication() {
        return serviceInfo.getApplication();
    }

    @Override
    public String getPropertyString(String propertyName) {
        return serviceInfo.getPropertyString(propertyName);
    }
}
