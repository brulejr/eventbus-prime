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

package io.jrb.labs.eventbusprime.sample.quote.steps

import io.jrb.labs.commons.workflow.api.StepResult
import io.jrb.labs.commons.workflow.api.WorkflowContext
import io.jrb.labs.commons.workflow.api.WorkflowInstance
import io.jrb.labs.commons.workflow.api.WorkflowStep
import io.jrb.labs.eventbusprime.sample.quote.events.QuoteCompleted
import io.jrb.labs.eventbusprime.sample.quote.events.ShippingPriced
import java.math.BigDecimal

class FinalizeQuoteStep : WorkflowStep<ShippingPriced, QuoteCompleted> {

    override val name: String = "finalize-quote"

    override suspend fun handle(
        instance: WorkflowInstance,
        event: ShippingPriced,
        context: WorkflowContext
    ): StepResult<QuoteCompleted> {
        val itemUnitPrice = BigDecimal("19.99")
        val merchandiseTotal = itemUnitPrice.multiply(BigDecimal(event.quantity))
        val totalCost = merchandiseTotal + event.shippingCost

        return StepResult.Success(
            QuoteCompleted(
                requestId = event.requestId,
                sku = event.sku,
                quantity = event.quantity,
                destinationZip = event.destinationZip,
                shippingCost = event.shippingCost,
                totalCost = totalCost,
                correlationId = event.correlationId,
                causationId = event.eventId,
                workflowInstanceId = instance.instanceId
            )
        )
    }
}