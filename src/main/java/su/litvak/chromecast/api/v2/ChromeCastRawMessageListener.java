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

/**
 * The listener interface for receiving raw messages. The class that is interested in processing raw
 * messages implements this interface, and object create with that class is registered with <code>ChromeCast</code>
 * instance using the <code>registerRawMessageListener</code> method. When messages are received, that object's
 * <code>rawMessageReceived</code> is invoked.
 */
public interface ChromeCastRawMessageListener {

    void rawMessageReceived(ChromeCastRawMessage message, Long requestId);

}
