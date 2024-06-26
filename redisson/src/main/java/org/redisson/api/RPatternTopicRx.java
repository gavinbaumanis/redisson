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
package org.redisson.api;

import java.util.List;

import org.redisson.api.listener.PatternMessageListener;
import org.redisson.api.listener.PatternStatusListener;

import io.reactivex.rxjava3.core.Single;

/**
 * RxJava2 interface for Pattern based observer for Publish Subscribe object.
 *
 * @author Nikita Koksharov
 *
 */
public interface RPatternTopicRx {

    /**
     * Get topic channel patterns
     *
     * @return list of topic names
     */
    List<String> getPatternNames();

    /**
     * Subscribes to this topic.
     * <code>MessageListener.onMessage</code> is called when any message
     * is published on this topic.
     * 
     * @param <T> type of message
     * @param type - type of message
     * @param listener - message listener
     * @return local JVM unique listener id
     * @see org.redisson.api.listener.MessageListener
     */
    <T> Single<Integer> addListener(Class<T> type, PatternMessageListener<T> listener);

    /**
     * Subscribes to status changes of this topic
     *
     * @param listener - message listener
     * @return local JVM unique listener id
     * @see org.redisson.api.listener.StatusListener
     */
    Single<Integer> addListener(PatternStatusListener listener);

    /**
     * Removes the listener by <code>id</code> for listening this topic
     *
     * @param listenerId - message listener id
     */
    void removeListener(int listenerId);

    /**
     * Returns active topic list of this pattern
     * @return all actives topic of this pattern
     */
    Single<List<String>> getActiveTopics();

}
