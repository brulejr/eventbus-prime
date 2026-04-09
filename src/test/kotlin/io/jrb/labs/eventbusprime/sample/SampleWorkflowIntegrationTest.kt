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
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SampleWorkflowIntegrationTest {

    @Autowired
    lateinit var eventBus: EventBus

    @Autowired
    lateinit var workflowInstanceStore: WorkflowInstanceStore

    @Test
    fun `workflow progresses from request to completion`() = runBlocking {
        val requestId = "req-123"

        eventBus.publish(
            EventEnvelope(
                eventType = "WorkRequested",
                correlationId = requestId,
                payload = WorkRequested(
                    requestId = requestId,
                    description = "  process me  "
                )
            )
        )

        delay(300)

        val instances = workflowInstanceStore.findByCorrelationId(requestId)
        assertThat(instances).hasSize(1)

        val instance = instances.single()
        assertThat(instance.workflowName).isEqualTo("sample-workflow")
        assertThat(instance.state).isEqualTo("COMPLETED")
        assertThat(instance.status.name).isEqualTo("COMPLETED")
        assertThat(instance.history).hasSize(2)
        assertThat(instance.history.map { it.stepName })
            .containsExactly("validate-work", "complete-work")
    }
}
