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

package io.jrb.labs.eventbusprime.sample

import io.jrb.labs.eventbusprime.bus.EventBus
import io.jrb.labs.eventbusprime.bus.EventEnvelope
import io.jrb.labs.eventbusprime.engine.WorkflowInstanceStore
import io.jrb.labs.eventbusprime.sample.events.WorkRequested
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/work")
class SampleWorkflowController(
    private val eventBus: EventBus,
    private val workflowInstanceStore: WorkflowInstanceStore
) {

    @PostMapping
    fun submit(@RequestBody request: WorkRequested): Map<String, String> {
        runBlocking {
            eventBus.publish(
                EventEnvelope(
                    eventType = "WorkRequested",
                    correlationId = request.requestId,
                    payload = request
                )
            )
        }
        return mapOf(
            "status" to "accepted",
            "requestId" to request.requestId
        )
    }

    @GetMapping("/instances")
    fun findAll(): List<Map<String, Any>> = runBlocking {
        workflowInstanceStore.findAll().map { instance ->
            mapOf(
                "instanceId" to instance.instanceId,
                "workflowName" to instance.workflowName,
                "correlationId" to instance.correlationId,
                "state" to instance.state,
                "status" to instance.status.name,
                "historySize" to instance.history.size
            )
        }
    }

    @GetMapping("/instances/{instanceId}")
    fun findByInstanceId(@PathVariable instanceId: String): Map<String, Any?> = runBlocking {
        val instance = workflowInstanceStore.findByInstanceId(instanceId)
        mapOf("instance" to instance)
    }

    @GetMapping("/instances/by-correlation/{correlationId}")
    fun findByCorrelationId(@PathVariable correlationId: String): Map<String, Any> = runBlocking {
        mapOf("instances" to workflowInstanceStore.findByCorrelationId(correlationId))
    }
}
