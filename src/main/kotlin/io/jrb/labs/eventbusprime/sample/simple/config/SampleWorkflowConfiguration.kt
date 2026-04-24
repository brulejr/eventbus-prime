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

package io.jrb.labs.eventbusprime.sample.simple.config

import io.jrb.labs.commons.eventbus.Event
import io.jrb.labs.commons.eventbus.EventBus
import io.jrb.labs.commons.workflow.api.WorkflowDefinition
import io.jrb.labs.commons.workflow.api.WorkflowRegistry
import io.jrb.labs.commons.workflow.engine.DefaultWorkflowTrafficCop
import io.jrb.labs.commons.workflow.engine.StepExecutor
import io.jrb.labs.commons.workflow.engine.TransitionMatcher
import io.jrb.labs.commons.workflow.engine.WorkflowMiddleware
import io.jrb.labs.commons.workflow.engine.WorkflowTrafficCop
import io.jrb.labs.commons.workflow.spi.EventBusWorkflowAdapter
import io.jrb.labs.commons.workflow.spi.WorkflowInstanceStore
import io.jrb.labs.commons.workflow.support.DefaultWorkflowRegistry
import io.jrb.labs.commons.workflow.support.InMemoryWorkflowInstanceStore
import io.jrb.labs.eventbusprime.sample.simple.events.SampleEventBus
import io.jrb.labs.eventbusprime.sample.simple.routing.CompleteWorkOutcomeRouter
import io.jrb.labs.eventbusprime.sample.simple.routing.ValidateWorkOutcomeRouter
import io.jrb.labs.eventbusprime.sample.simple.steps.ApproveWorkStep
import io.jrb.labs.eventbusprime.sample.simple.steps.CompleteWorkStep
import io.jrb.labs.eventbusprime.sample.simple.steps.ValidateWorkStep
import io.jrb.labs.eventbusprime.sample.simple.workflow.SampleWorkWorkflowDefinition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SampleWorkflowConfiguration {

    @Bean
    fun sampleEventBus(): EventBus<Event> =
        SampleEventBus()

    @Bean
    fun validateWorkStep(): ValidateWorkStep =
        ValidateWorkStep()

    @Bean
    fun completeWorkStep(): CompleteWorkStep =
        CompleteWorkStep()

    @Bean
    fun approveWorkStep(): ApproveWorkStep = ApproveWorkStep()

    @Bean
    fun validateWorkOutcomeRouter(): ValidateWorkOutcomeRouter =
        ValidateWorkOutcomeRouter()

    @Bean
    fun completeWorkOutcomeRouter(): CompleteWorkOutcomeRouter =
        CompleteWorkOutcomeRouter()

    @Bean
    fun sampleWorkflowDefinition(
        validateWorkStep: ValidateWorkStep,
        validateWorkOutcomeRouter: ValidateWorkOutcomeRouter,
        approveWorkStep: ApproveWorkStep,
        completeWorkOutcomeRouter: CompleteWorkOutcomeRouter
    ): WorkflowDefinition =
        SampleWorkWorkflowDefinition(
            validateWorkStep = validateWorkStep,
            validateWorkOutcomeRouter = validateWorkOutcomeRouter,
            approveWorkStep = approveWorkStep,
            completeWorkOutcomeRouter = completeWorkOutcomeRouter
        )

    @Bean
    fun workflowRegistry(definitions: List<WorkflowDefinition>): WorkflowRegistry =
        DefaultWorkflowRegistry(definitions)

    @Bean
    fun workflowInstanceStore(): WorkflowInstanceStore = InMemoryWorkflowInstanceStore()

    @Bean
    fun transitionMatcher(): TransitionMatcher = TransitionMatcher()

    @Bean
    fun workflowMiddlewares(): List<WorkflowMiddleware> =
        listOf(LoggingWorkflowMiddleware())

    @Bean
    fun stepExecutor(middlewares: List<WorkflowMiddleware>): StepExecutor = StepExecutor(middlewares)

    @Bean
    fun workflowEventAdapter(eventBus: EventBus<Event>): EventBusWorkflowAdapter<Event> =
        EventBusWorkflowAdapter(eventBus)

    @Bean
    fun workflowTrafficCop(
        workflowRegistry: WorkflowRegistry,
        instanceStore: WorkflowInstanceStore,
        transitionMatcher: TransitionMatcher,
        stepExecutor: StepExecutor,
        workflowEventAdapter: EventBusWorkflowAdapter<Event>
    ): WorkflowTrafficCop =
        DefaultWorkflowTrafficCop(
            workflowRegistry = workflowRegistry,
            instanceStore = instanceStore,
            transitionMatcher = transitionMatcher,
            stepExecutor = stepExecutor,
            eventPublisher = workflowEventAdapter
        )

    @Bean
    fun workflowTrafficCopSubscription(
        eventBus: EventBus<Event>,
        workflowTrafficCop: WorkflowTrafficCop
    ): EventBus.Subscription =
        eventBus.subscribeAll { workflowTrafficCop.handleEvent(it) }


}