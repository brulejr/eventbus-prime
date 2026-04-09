/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2026 Jon Brule <brulejr@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.jrb.labs.commons.eventbus

/**
 * Represents a subscriber to events on the event bus. Implementations of this interface will be invoked when an event
 * is published to the bus that matches the subscriber's criteria (e.g., event type, feature ID, station ID, etc.).
 * The subscriber is responsible for handling the event appropriately, which may include processing the event data,
 * performing side effects, or triggering other actions in response to the event.
 */
fun interface EventSubscriber {

    /**
     * Handles an event published to the event bus. Implementations should handle the event as needed, such as
     * processing the event data, performing side effects, or triggering other actions. This method is a suspend
     * function, allowing for asynchronous processing of events if necessary.
     */
    suspend fun onEvent(event: EventEnvelope<*>)

}
