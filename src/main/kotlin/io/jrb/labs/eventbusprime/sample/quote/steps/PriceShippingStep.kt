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
import io.jrb.labs.eventbusprime.sample.quote.events.InventoryChecked
import io.jrb.labs.eventbusprime.sample.quote.events.ShippingPriced
import java.math.BigDecimal

class PriceShippingStep : WorkflowStep<InventoryChecked, ShippingPriced> {

    override val name: String = "price-shipping"

    override suspend fun handle(
        instance: WorkflowInstance,
        event: InventoryChecked,
        context: WorkflowContext
    ): StepResult<ShippingPriced> {
        val base = BigDecimal("5.00")
        val perItem = BigDecimal("1.25").multiply(BigDecimal(event.quantity))
        val zoneAdjustment = when {
            event.destinationZip.startsWith("0") -> BigDecimal("3.00")
            event.destinationZip.startsWith("9") -> BigDecimal("4.50")
            else -> BigDecimal("2.00")
        }

        val shippingCost = base + perItem + zoneAdjustment

        return StepResult.Success(
            ShippingPriced(
                requestId = event.requestId,
                sku = event.sku,
                quantity = event.quantity,
                destinationZip = event.destinationZip,
                shippingCost = shippingCost,
                correlationId = event.correlationId,
                causationId = event.eventId,
                workflowInstanceId = instance.instanceId
            )
        )
    }
}