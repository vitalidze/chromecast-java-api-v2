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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

final class Util {
    private Util() {
    }

    /**
     * Converts specified byte array in Big Endian to int
     */
    static int fromArray(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        buffer.order(ByteOrder.BIG_ENDIAN);
        return buffer.getInt();
    }

    /**
     * Converts specified int to byte array in Big Endian
     */
    static byte[] toArray(int value) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(value);
        buffer.flip();
        return buffer.array();
    }
}
