/*
 * Copyright 2017 Vitaly Litvak (vitavaque@gmail.com)
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

/**
 * The listener interface for receiving connection open/close events. The class that is interested in processing
 * connection events implements this interface, and object create with that class is registered
 * with <code>ChromeCast</code> instance using the <code>registerConnectionListener</code> method.
 * When connection event occurs, that object's <code>connectionEventReceived</code> is invoked.
 *
 * @see ChromeCastConnectionEvent
 */
public interface ChromeCastConnectionEventListener {

    void connectionEventReceived(ChromeCastConnectionEvent event);

}
