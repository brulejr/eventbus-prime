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

package io.jrb.labs.eventbusprime.sample.quote.workflow

import io.jrb.labs.commons.eventbus.Event
import io.jrb.labs.commons.workflow.api.WorkflowDefinition
import io.jrb.labs.commons.workflow.api.WorkflowTransition
import io.jrb.labs.eventbusprime.sample.quote.events.InventoryChecked
import io.jrb.labs.eventbusprime.sample.quote.events.QuoteRequested
import io.jrb.labs.eventbusprime.sample.quote.events.QuoteValidated
import io.jrb.labs.eventbusprime.sample.quote.events.ShippingPriced
import io.jrb.labs.eventbusprime.sample.quote.routing.InventoryCheckedOutcomeRouter
import io.jrb.labs.eventbusprime.sample.quote.routing.QuoteCompletedOutcomeRouter
import io.jrb.labs.eventbusprime.sample.quote.routing.QuoteValidatedOutcomeRouter
import io.jrb.labs.eventbusprime.sample.quote.routing.ShippingPricedOutcomeRouter
import io.jrb.labs.eventbusprime.sample.quote.steps.CheckInventoryStep
import io.jrb.labs.eventbusprime.sample.quote.steps.FinalizeQuoteStep
import io.jrb.labs.eventbusprime.sample.quote.steps.PriceShippingStep
import io.jrb.labs.eventbusprime.sample.quote.steps.ValidateQuoteStep

class QuoteWorkflowDefinition(
    validateQuoteStep: ValidateQuoteStep,
    quoteValidatedOutcomeRouter: QuoteValidatedOutcomeRouter,
    checkInventoryStep: CheckInventoryStep,
    inventoryCheckedOutcomeRouter: InventoryCheckedOutcomeRouter,
    priceShippingStep: PriceShippingStep,
    shippingPricedOutcomeRouter: ShippingPricedOutcomeRouter,
    finalizeQuoteStep: FinalizeQuoteStep,
    quoteCompletedOutcomeRouter: QuoteCompletedOutcomeRouter
) : WorkflowDefinition {

    override val name: String = "quote-workflow"
    override val primingEventClass = QuoteRequested::class
    override val initialState: String = "RECEIVED"

    override val transitions = listOf(
        WorkflowTransition(
            fromState = "RECEIVED",
            inboundEventClass = QuoteRequested::class,
            step = validateQuoteStep,
            outcomeRouter = quoteValidatedOutcomeRouter
        ),
        WorkflowTransition(
            fromState = "VALIDATED",
            inboundEventClass = QuoteValidated::class,
            step = checkInventoryStep,
            outcomeRouter = inventoryCheckedOutcomeRouter
        ),
        WorkflowTransition(
            fromState = "INVENTORY_CONFIRMED",
            inboundEventClass = InventoryChecked::class,
            step = priceShippingStep,
            outcomeRouter = shippingPricedOutcomeRouter
        ),
        WorkflowTransition(
            fromState = "PRICED",
            inboundEventClass = ShippingPriced::class,
            step = finalizeQuoteStep,
            outcomeRouter = quoteCompletedOutcomeRouter
        )
    )

    override fun correlationIdOf(event: Event): String =
        when (event) {
            is QuoteRequested -> event.requestId
            else -> error("Unsupported priming event type: ${event::class.qualifiedName}")
        }

}