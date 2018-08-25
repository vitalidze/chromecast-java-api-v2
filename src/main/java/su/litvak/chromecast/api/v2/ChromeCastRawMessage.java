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
 * <p>Identifies that a raw message was received from a ChromeCast device.</p>
 */
public final class ChromeCastRawMessage {

    private final CastChannel.CastMessage message;

    public ChromeCastRawMessage(CastChannel.CastMessage message) {
        this.message = message;
    }

    public CastChannel.CastMessage.PayloadType getPayloadType() {
        return this.message.getPayloadType();
    }

    public String getPayloadUtf8() {
        return this.message.getPayloadUtf8();
    }

    public byte[] getPayloadBinary() {
        return this.message.getPayloadBinary().toByteArray();
    }

    public String getNamespace() {
        return this.message.getNamespace();
    }
}
