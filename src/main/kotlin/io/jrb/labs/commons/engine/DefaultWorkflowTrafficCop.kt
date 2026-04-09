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

package io.jrb.labs.commons.engine

import io.jrb.labs.commons.eventbus.EventBus
import io.jrb.labs.commons.eventbus.EventEnvelope
import io.jrb.labs.commons.workflow.RecordedStepResult
import io.jrb.labs.commons.workflow.StepResult
import io.jrb.labs.commons.workflow.WorkflowDefinition
import io.jrb.labs.commons.workflow.WorkflowHistoryEntry
import io.jrb.labs.commons.workflow.WorkflowInstance
import io.jrb.labs.commons.workflow.WorkflowRegistry
import io.jrb.labs.commons.workflow.WorkflowStatus
import io.jrb.labs.commons.workflow.WorkflowTransition
import java.time.Instant

class DefaultWorkflowTrafficCop(
    private val workflowRegistry: WorkflowRegistry,
    private val instanceStore: WorkflowInstanceStore,
    private val transitionMatcher: TransitionMatcher,
    private val stepExecutor: StepExecutor,
    private val eventBus: EventBus
) : WorkflowTrafficCop {

    init {
        eventBus.subscribe(this)
    }

    override suspend fun onEvent(event: EventEnvelope<*>) {
        val byInstance = event.workflowInstanceId?.let { instanceStore.findByInstanceId(it) }
        if (byInstance != null) {
            advanceExistingWorkflow(byInstance, event)
            return
        }

        val byCorrelation = instanceStore.findByCorrelationId(event.correlationId)
        if (byCorrelation.isNotEmpty()) {
            byCorrelation.forEach { advanceExistingWorkflow(it, event) }
            return
        }

        val primedDefinitions = workflowRegistry.findByPrimingEventType(event.eventType)
        primedDefinitions.forEach { definition ->
            startWorkflow(definition, event)
        }
    }

    private suspend fun startWorkflow(
        definition: WorkflowDefinition,
        event: EventEnvelope<*>
    ) {
        val instance = WorkflowInstance(
            workflowName = definition.name,
            correlationId = event.correlationId,
            state = definition.initialState,
            status = WorkflowStatus.RUNNING
        )

        instanceStore.save(instance)
        advanceExistingWorkflow(instance, event)
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun advanceExistingWorkflow(
        instance: WorkflowInstance,
        event: EventEnvelope<*>
    ) {
        val definition = workflowRegistry.definitions().firstOrNull { it.name == instance.workflowName } ?: return
        val transition = transitionMatcher.findMatchingTransition(definition, instance.state, event.eventType) ?: return

        val typedTransition = transition as WorkflowTransition<Any, Any>
        val typedEvent = event as EventEnvelope<Any>
        val invocation = StepInvocation(
            instance = instance,
            event = typedEvent,
            step = typedTransition.step
        )

        val result = stepExecutor.execute(invocation)
        val resolution = typedTransition.outcomeRouter.route(result, instance)

        val recordedStepResult = RecordedStepResult(
            stepName = typedTransition.step.name,
            outcomeType = result::class.simpleName ?: "Unknown",
            summary = summarize(result)
        )

        val updatedContext = resolution.contextMutator(instance.context)
            .withRecordedStepResult(typedTransition.step.name, recordedStepResult)

        val updatedInstance = instance.copy(
            state = resolution.nextState,
            status = resolution.nextStatus,
            updatedAt = Instant.now(),
            context = updatedContext,
            history = instance.history + WorkflowHistoryEntry(
                stateBefore = instance.state,
                stateAfter = resolution.nextState,
                inboundEventType = event.eventType,
                stepName = typedTransition.step.name,
                outcomeType = result::class.simpleName ?: "Unknown",
                summary = summarize(result),
                outboundEventTypes = resolution.outboundEvents.map { it.eventType }
            )
        )

        instanceStore.save(updatedInstance)

        resolution.outboundEvents.forEach { routed ->
            eventBus.publish(
                EventEnvelope(
                    eventType = routed.eventType,
                    correlationId = updatedInstance.correlationId,
                    causationId = event.eventId,
                    workflowInstanceId = updatedInstance.instanceId,
                    payload = routed.payload,
                    metadata = routed.metadata
                )
            )
        }
    }

    private fun summarize(result: StepResult<*>): String =
        when (result) {
            is StepResult.Success<*> -> "Success"
            is StepResult.Failed -> result.reason
            is StepResult.Errored -> result.errorMessage
            is StepResult.Ignored -> result.reason
            is StepResult.Waiting -> result.reason
        }
}
