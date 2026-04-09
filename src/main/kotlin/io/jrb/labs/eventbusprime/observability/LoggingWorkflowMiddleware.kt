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

package io.jrb.labs.eventbusprime.observability

import io.jrb.labs.eventbusprime.engine.StepInvocation
import io.jrb.labs.eventbusprime.engine.WorkflowMiddleware
import io.jrb.labs.eventbusprime.workflow.StepResult
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import kotlin.system.measureTimeMillis

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class LoggingWorkflowMiddleware : WorkflowMiddleware {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Suppress("UNCHECKED_CAST")
    override suspend fun <I : Any, O : Any> invoke(
        invocation: StepInvocation<I, O>,
        next: suspend (StepInvocation<I, O>) -> StepResult<O>
    ): StepResult<O> {
        lateinit var result: StepResult<O>
        val durationMs = measureTimeMillis {
            result = try {
                next(invocation)
            } catch (ex: Exception) {
                StepResult.Errored(
                    errorMessage = ex.message ?: "Unhandled exception",
                    cause = ex
                ) as StepResult<O>
            }
        }

        logger.info(
            "workflow={} instanceId={} step={} state={} inboundEventType={} resultType={} durationMs={}",
            invocation.instance.workflowName,
            invocation.instance.instanceId,
            invocation.step.name,
            invocation.instance.state,
            invocation.event.eventType,
            result::class.simpleName,
            durationMs
        )

        return result
    }
}
