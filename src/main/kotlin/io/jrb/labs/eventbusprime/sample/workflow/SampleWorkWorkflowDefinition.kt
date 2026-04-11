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

package io.jrb.labs.eventbusprime.sample.workflow

import io.jrb.labs.commons.eventbus.Event
import io.jrb.labs.commons.workflow.api.WorkflowDefinition
import io.jrb.labs.commons.workflow.api.WorkflowTransition
import io.jrb.labs.eventbusprime.sample.events.WorkRequested
import io.jrb.labs.eventbusprime.sample.events.WorkValidated
import io.jrb.labs.eventbusprime.sample.routing.CompleteWorkOutcomeRouter
import io.jrb.labs.eventbusprime.sample.routing.ValidateWorkOutcomeRouter
import io.jrb.labs.eventbusprime.sample.steps.CompleteWorkStep
import io.jrb.labs.eventbusprime.sample.steps.ValidateWorkStep
import org.springframework.stereotype.Component

@Component
class SampleWorkWorkflowDefinition(
    private val validateWorkStep: ValidateWorkStep,
    private val validateWorkOutcomeRouter: ValidateWorkOutcomeRouter,
    private val completeWorkStep: CompleteWorkStep,
    private val completeWorkOutcomeRouter: CompleteWorkOutcomeRouter
) : WorkflowDefinition {

    override val name: String = "sample-workflow"

    override val primingEventClass = WorkRequested::class

    override val initialState: String = "RECEIVED"

    override val transitions = listOf(
        WorkflowTransition(
            fromState = "RECEIVED",
            inboundEventClass = WorkRequested::class,
            step = validateWorkStep,
            outcomeRouter = validateWorkOutcomeRouter
        ),
        WorkflowTransition(
            fromState = "VALIDATED",
            inboundEventClass = WorkValidated::class,
            step = completeWorkStep,
            outcomeRouter = completeWorkOutcomeRouter
        )
    )

    override fun correlationIdOf(event: Event): String =
        when (event) {
            is WorkRequested -> event.requestId
            else -> error("Unsupported priming event type: ${event::class.qualifiedName}")
        }

}