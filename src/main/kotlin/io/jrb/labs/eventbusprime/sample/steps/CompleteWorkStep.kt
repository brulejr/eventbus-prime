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

package io.jrb.labs.eventbusprime.sample.steps

import io.jrb.labs.commons.workflow.api.StepResult
import io.jrb.labs.commons.workflow.api.WorkflowContext
import io.jrb.labs.commons.workflow.api.WorkflowInstance
import io.jrb.labs.commons.workflow.api.WorkflowStep
import io.jrb.labs.eventbusprime.sample.events.WorkCompleted
import io.jrb.labs.eventbusprime.sample.events.WorkValidated
import org.springframework.stereotype.Component

@Component
class CompleteWorkStep : WorkflowStep<WorkValidated, WorkCompleted> {
    override val name: String = "complete-work"

    override suspend fun handle(
        instance: WorkflowInstance,
        event: WorkValidated,
        context: WorkflowContext
    ): StepResult<WorkCompleted> {
        return StepResult.Success(
            WorkCompleted(
                requestId = event.requestId,
                result = "Processed: ${event.normalizedDescription}",
                correlationId = event.correlationId,
                causationId = event.eventId,
                workflowInstanceId = instance.instanceId
            )
        )
    }

}
