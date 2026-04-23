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

package io.jrb.labs.eventbusprime.sample.quote.config

import io.jrb.labs.commons.eventbus.Event
import io.jrb.labs.commons.eventbus.EventBus
import io.jrb.labs.commons.workflow.api.WorkflowDefinition
import io.jrb.labs.eventbusprime.sample.quote.routing.InventoryCheckedOutcomeRouter
import io.jrb.labs.eventbusprime.sample.quote.routing.QuoteCompletedOutcomeRouter
import io.jrb.labs.eventbusprime.sample.quote.routing.QuoteValidatedOutcomeRouter
import io.jrb.labs.eventbusprime.sample.quote.routing.ShippingPricedOutcomeRouter
import io.jrb.labs.eventbusprime.sample.quote.steps.CheckInventoryStep
import io.jrb.labs.eventbusprime.sample.quote.steps.FinalizeQuoteStep
import io.jrb.labs.eventbusprime.sample.quote.steps.PriceShippingStep
import io.jrb.labs.eventbusprime.sample.quote.steps.ValidateQuoteStep
import io.jrb.labs.eventbusprime.sample.quote.sync.DefaultBlockingWorkflowRunner
import io.jrb.labs.eventbusprime.sample.quote.sync.QuoteWorkflowCompletionListener
import io.jrb.labs.eventbusprime.sample.quote.workflow.QuoteWorkflowDefinition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class QuoteWorkflowConfiguration {

    @Bean
    fun validateQuoteStep() = ValidateQuoteStep()

    @Bean
    fun checkInventoryStep() = CheckInventoryStep()

    @Bean
    fun priceShippingStep() = PriceShippingStep()

    @Bean
    fun finalizeQuoteStep() = FinalizeQuoteStep()

    @Bean
    fun quoteValidatedOutcomeRouter() = QuoteValidatedOutcomeRouter()

    @Bean
    fun inventoryCheckedOutcomeRouter() = InventoryCheckedOutcomeRouter()

    @Bean
    fun shippingPricedOutcomeRouter() = ShippingPricedOutcomeRouter()

    @Bean
    fun quoteCompletedOutcomeRouter() = QuoteCompletedOutcomeRouter()

    @Bean
    fun quoteWorkflowDefinition(
        validateQuoteStep: ValidateQuoteStep,
        quoteValidatedOutcomeRouter: QuoteValidatedOutcomeRouter,
        checkInventoryStep: CheckInventoryStep,
        inventoryCheckedOutcomeRouter: InventoryCheckedOutcomeRouter,
        priceShippingStep: PriceShippingStep,
        shippingPricedOutcomeRouter: ShippingPricedOutcomeRouter,
        finalizeQuoteStep: FinalizeQuoteStep,
        quoteCompletedOutcomeRouter: QuoteCompletedOutcomeRouter
    ): WorkflowDefinition =
        QuoteWorkflowDefinition(
            validateQuoteStep = validateQuoteStep,
            quoteValidatedOutcomeRouter = quoteValidatedOutcomeRouter,
            checkInventoryStep = checkInventoryStep,
            inventoryCheckedOutcomeRouter = inventoryCheckedOutcomeRouter,
            priceShippingStep = priceShippingStep,
            shippingPricedOutcomeRouter = shippingPricedOutcomeRouter,
            finalizeQuoteStep = finalizeQuoteStep,
            quoteCompletedOutcomeRouter = quoteCompletedOutcomeRouter
        )

    @Bean
    fun blockingWorkflowRunner(eventBus: EventBus<Event>): DefaultBlockingWorkflowRunner =
        DefaultBlockingWorkflowRunner(eventBus)

    @Bean
    fun quoteWorkflowCompletionListener(
        eventBus: EventBus<Event>,
        runner: DefaultBlockingWorkflowRunner
    ): QuoteWorkflowCompletionListener =
        QuoteWorkflowCompletionListener(eventBus, runner)

    @Bean
    fun quoteWorkflowCompletionSubscriptions(
        listener: QuoteWorkflowCompletionListener
    ): List<EventBus.Subscription> =
        listener.subscribe()

}