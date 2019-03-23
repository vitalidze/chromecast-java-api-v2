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
import su.litvak.chromecast.mdns.api.MulticastDNSServiceListener;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

/**
 * Multicast DNS service discovery listener implementation based on JmDNS.
 */
final class JmDNSServiceListener implements ServiceListener {
    final MulticastDNS mDNS;
    final MulticastDNSServiceListener outerListener;

    JmDNSServiceListener(MulticastDNS mDNS, MulticastDNSServiceListener outerListener) {
        this.mDNS = mDNS;
        this.outerListener = outerListener;
    }

    @Override
    public void serviceAdded(ServiceEvent serviceEvent) {
        outerListener.serviceAdded(mDNS, serviceEvent.getType(),
                serviceEvent.getInfo() == null ? null : serviceEvent.getInfo().getName());
    }

    @Override
    public void serviceRemoved(ServiceEvent serviceEvent) {
        outerListener.serviceRemoved(mDNS, serviceEvent.getType(),
                serviceEvent.getInfo() == null ? null : serviceEvent.getInfo().getName());
    }

    @Override
    public void serviceResolved(ServiceEvent serviceEvent) {
        // intentionally blank
    }
}
