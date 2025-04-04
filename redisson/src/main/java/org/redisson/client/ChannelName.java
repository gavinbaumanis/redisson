/**
 * Copyright (c) 2013-2024 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.netty.util.CharsetUtil;

/**
 *
 * @author Nikita Koksharov
 *
 */
public class ChannelName implements CharSequence {

    public static final ChannelName TRACKING = new ChannelName("__redis__:invalidate");

    public static List<ChannelName> newList(ChannelName name) {
        List<ChannelName> result = new ArrayList<>(1);
        result.add(name);
        return result;
    }

    public static List<ChannelName> newList(String name) {
        List<ChannelName> result = new ArrayList<>(1);
        result.add(new ChannelName(name));
        return result;
    }

    private final byte[] name;
    private final String str;

    public ChannelName(byte[] name) {
        super();
        this.name = name;
        this.str = new String(name, CharsetUtil.UTF_8);
    }

    public ChannelName(String name) {
        this(name.getBytes(CharsetUtil.UTF_8));
    }

    @Override
    public String toString() {
        return str;
    }

    public byte[] getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(name);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ChannelName) {
            return Arrays.equals(name, ((ChannelName) obj).name);
        }
        if (obj instanceof CharSequence) {
            return toString().equals(obj);
        }
        return false;
    }

    @Override
    public int length() {
        return toString().length();
    }

    @Override
    public char charAt(int index) {
        return toString().charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }

    public boolean isKeyspace() {
        return str.startsWith("__keyspace") || str.startsWith("__keyevent");
    }

    public boolean isTracking() {
        return str.equals(TRACKING.toString());
    }

}

