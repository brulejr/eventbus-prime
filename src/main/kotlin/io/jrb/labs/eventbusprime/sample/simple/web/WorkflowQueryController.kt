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

package io.jrb.labs.eventbusprime.sample.simple.web

import io.jrb.labs.commons.workflow.spi.WorkflowInstanceStore
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/work")
class WorkflowQueryController(
    private val instanceStore: WorkflowInstanceStore
) {

    @GetMapping("/instances")
    fun findAllInstances() = runBlocking {
        instanceStore.findAll()
    }

    @GetMapping("/{instanceId}")
    fun findByInstanceId(@PathVariable instanceId: String) =
        runBlocking {
            instanceStore.findByInstanceId(instanceId)
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Workflow instance not found: $instanceId"
                )
        }

    @GetMapping("/correlation/{correlationId}")
    fun findByCorrelationId(@PathVariable correlationId: String) =
        runBlocking {
            instanceStore.findByCorrelationId(correlationId)
        }

}