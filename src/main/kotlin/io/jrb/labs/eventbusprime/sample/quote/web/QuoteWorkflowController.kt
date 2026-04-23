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

package io.jrb.labs.eventbusprime.sample.quote.web

import io.jrb.labs.commons.eventbus.Event
import io.jrb.labs.eventbusprime.sample.quote.events.QuoteCompleted
import io.jrb.labs.eventbusprime.sample.quote.events.QuoteFailed
import io.jrb.labs.eventbusprime.sample.quote.events.QuoteRequested
import io.jrb.labs.eventbusprime.sample.quote.sync.BlockingWorkflowRunner
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.Duration
import java.util.UUID

@RestController
@RequestMapping("/api/quotes")
class QuoteWorkflowController(
    private val blockingWorkflowRunner: BlockingWorkflowRunner
) {

    @PostMapping("/sync")
    suspend fun createQuote(@RequestBody request: CreateQuoteRequest): QuoteResponse {
        val requestId = UUID.randomUUID().toString()

        val startEvent = QuoteRequested(
            requestId = requestId,
            sku = request.sku,
            quantity = request.quantity,
            destinationZip = request.destinationZip
        )

        return when (
            val terminal = blockingWorkflowRunner.runUntilCompleted<Event>(
                correlationId = requestId,
                startEvent = startEvent,
                timeout = Duration.ofSeconds(5)
            )
        ) {
            is QuoteCompleted -> QuoteResponse(
                requestId = terminal.requestId,
                workflowInstanceId = terminal.workflowInstanceId,
                sku = terminal.sku,
                quantity = terminal.quantity,
                destinationZip = terminal.destinationZip,
                shippingCost = terminal.shippingCost,
                totalCost = terminal.totalCost,
                status = "COMPLETED"
            )

            is QuoteFailed -> throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                terminal.reason
            )

            else -> throw IllegalStateException(
                "Unexpected terminal event type: ${terminal::class.qualifiedName}"
            )
        }
    }
}

data class CreateQuoteRequest(
    val sku: String,
    val quantity: Int,
    val destinationZip: String
)

data class QuoteResponse(
    val requestId: String,
    val workflowInstanceId: String?,
    val sku: String,
    val quantity: Int,
    val destinationZip: String,
    val shippingCost: BigDecimal,
    val totalCost: BigDecimal,
    val status: String
)
