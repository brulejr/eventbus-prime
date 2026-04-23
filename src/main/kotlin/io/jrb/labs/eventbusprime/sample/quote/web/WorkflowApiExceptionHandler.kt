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

import io.jrb.labs.commons.workflow.api.WorkflowRequestFailedException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class WorkflowApiExceptionHandler {

    @ExceptionHandler(WorkflowRequestFailedException::class)
    fun handleWorkflowRequestFailed(
        ex: WorkflowRequestFailedException,
        request: HttpServletRequest
    ): ProblemDetail {
        val failure = ex.failure

        val problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            failure.message
        )
        problem.title = "Workflow request rejected"
        problem.setProperty("requestId", failure.requestId)
        problem.setProperty("correlationId", failure.correlationId)
        problem.setProperty("instanceId", failure.instanceId)
        problem.setProperty("workflowName", failure.workflowName)
        problem.setProperty("stepName", failure.stepName)
        problem.setProperty("errorCode", failure.errorCode)
        problem.setProperty("path", request.requestURI)
        return problem
    }
}