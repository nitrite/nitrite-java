/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.datagate.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Document;
import org.dizitart.no2.datagate.models.DataGateError;
import org.dizitart.no2.datagate.services.DataGateService;
import org.dizitart.no2.sync.TimeSpan;
import org.dizitart.no2.sync.types.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Controller for sync endpoints.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@Slf4j
@RestController
@RequestMapping(path = "api/v1/collection/{collection}")
public class SyncController {

    @Autowired
    private DataGateService dataGateService;

    @RequestMapping(path = "/changedSince",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "This operation gets the list of changes from remote collection.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request", response = DataGateError.class),
            @ApiResponse(code = 401, message = "Request is not authorized", response = DataGateError.class),
            @ApiResponse(code = 500, message = "Error processing request", response = DataGateError.class),
    })
    public ChangeFeed changedSince(
            @PathVariable
            @ApiParam("Name of the remote collection.")
                    String collection,
            @RequestBody
            @ApiParam("Options for change feed")
                    FeedOptions feedOptions) {

        log.debug("Validating changedSince request for " + collection);
        dataGateService.validateRequest(collection);
        ChangeFeed feed = dataGateService.changedSince(collection, feedOptions);
        log.debug(collection + " changed since " + feedOptions.getFromSequence() + " : " + feed);
        return feed;
    }

    @RequestMapping(path = "/change",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "This operation merges the changes into a remote collection.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request", response = DataGateError.class),
            @ApiResponse(code = 401, message = "Request is not authorized", response = DataGateError.class),
            @ApiResponse(code = 500, message = "Error processing request", response = DataGateError.class),
    })
    public ChangeResponse change(
            @PathVariable
            @ApiParam("Name of the remote collection.")
                    String collection,
            @RequestBody
            @ApiParam("The list of changes to be merged.")
                    ChangeFeed changeFeed) {

        log.debug("Validating change request for " + collection);
        dataGateService.validateRequest(collection);
        boolean result = dataGateService.change(collection, changeFeed);
        log.debug(collection + " changed with " + changeFeed);
        return new ChangeResponse(result);
    }

    @RequestMapping(path = "/fetch/offset/{offset}/limit/{limit}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "This operation fetches all documents from remote replica server in pages.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request", response = DataGateError.class),
            @ApiResponse(code = 401, message = "Request is not authorized", response = DataGateError.class),
            @ApiResponse(code = 500, message = "Error processing request", response = DataGateError.class),
    })
    public FetchResponse fetch(
            @PathVariable
            @ApiParam("Name of the remote collection.")
                    String collection,
            @PathVariable(name = "offset")
                    int offset,
            @PathVariable(name = "limit")
                    int limit) {

        log.debug("Validating fetch request for " + collection);
        dataGateService.validateRequest(collection);
        List<Document> documents = dataGateService.fetch(collection, offset, limit);
        log.debug("Fetch request for " + collection + " returned "
                + documents.size() + " records.");
        return new FetchResponse(documents);
    }

    @RequestMapping(path = "/size",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "This operation gets the size of the remote collection.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request", response = DataGateError.class),
            @ApiResponse(code = 401, message = "Request is not authorized", response = DataGateError.class),
            @ApiResponse(code = 500, message = "Error processing request", response = DataGateError.class),
    })
    public SizeResponse size(
            @PathVariable
            @ApiParam("Name of the remote collection.")
                    String collection) {

        log.debug("Validating size request for " + collection);
        dataGateService.validateRequest(collection);
        long size = dataGateService.size(collection);
        log.debug("Size of " + collection + " is " + size);
        return new SizeResponse(size);
    }

    @RequestMapping(path = "/clear",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "This operation clears the remote collection.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request", response = DataGateError.class),
            @ApiResponse(code = 401, message = "Request is not authorized", response = DataGateError.class),
            @ApiResponse(code = 500, message = "Error processing request", response = DataGateError.class),
    })
    public void clear(
            @PathVariable
            @ApiParam("Name of the remote collection.")
                    String collection) {

        log.debug("Validating clear request for " + collection);
        dataGateService.validateRequest(collection);
        dataGateService.clear(collection);
        log.debug(collection + " is cleared.");
    }

    @RequestMapping(path = "/tryLock/issuer/{issuer}/delay/{delay}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "This operation tries to acquire a synchronization lock on the remote collection." +
            "Before start of replication, a sync lock must be acquired on remote collection. If the acquisition " +
            "is unsuccessful, replication will not occur and it will be retried in next iteration.",
            response = TryLockResponse.class)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request", response = DataGateError.class),
            @ApiResponse(code = 401, message = "Request is not authorized", response = DataGateError.class),
            @ApiResponse(code = 500, message = "Error processing request", response = DataGateError.class),
    })
    public TryLockResponse tryLock(
            @PathVariable
            @ApiParam("Name of the remote collection.")
                    String collection,
            @PathVariable
            @ApiParam("The originator of the request.")
                    String issuer,
            @RequestHeader(value = UserAgent.USER_AGENT, required = false)
            @ApiParam("The user agent of the request.")
                    String userAgent,
            @PathVariable
            @ApiParam("The expiry delay. If the expiryDelay is expired, then a new lock " +
                    "will be acquired overwriting previous lock information.")
                    long delay) {

        log.debug("Validating tryLock request for " + collection + " by " + issuer);
        dataGateService.validateRequest(collection);
        boolean result = dataGateService.tryLock(collection, issuer, userAgent, TimeSpan.timeSpan(delay, TimeUnit.MILLISECONDS));
        if (result) {
            log.debug("Lock acquired for " + collection + " by " + issuer);
        } else {
            log.debug(issuer + " failed to acquire lock for " + collection);
        }
        return new TryLockResponse(result);
    }

    @RequestMapping(path = "/releaseLock/issuer/{issuer}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "This operation checks if the remote collection is online and reachable.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request", response = DataGateError.class),
            @ApiResponse(code = 401, message = "Request is not authorized", response = DataGateError.class),
            @ApiResponse(code = 500, message = "Error processing request", response = DataGateError.class),
    })
    public void releaseLock(
            @PathVariable
            @ApiParam("Name of the remote collection.")
                    String collection,
            @PathVariable
            @ApiParam("The originator of the request.")
                    String issuer) {

        log.debug("Validating releaseLock request for " + collection + " by " + issuer);
        dataGateService.validateRequest(collection);
        dataGateService.releaseLock(collection, issuer);
        log.debug("Lock released for " + collection + " by " + issuer);
    }
}
