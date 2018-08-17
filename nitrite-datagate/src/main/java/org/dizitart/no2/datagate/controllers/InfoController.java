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
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.dizitart.no2.datagate.models.DataGateError;
import org.dizitart.no2.datagate.services.DataGateService;
import org.dizitart.no2.sync.types.InfoResponse;
import org.dizitart.no2.sync.types.OnlineResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for info endpoints.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@RestController
@RequestMapping(path = "api/v1")
public class InfoController {

    @Autowired
    private DataGateService dataGateService;

    @RequestMapping(path = "/",
        method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Returns meta-information about the datagate server.")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid request", response = DataGateError.class),
        @ApiResponse(code = 401, message = "Request is not authorized", response = DataGateError.class),
        @ApiResponse(code = 500, message = "Error processing request", response = DataGateError.class),
    })
    public InfoResponse info() {
        return dataGateService.getServerInfo();
    }

    @RequestMapping(path = "/ping",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Pings the datagate server.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request", response = DataGateError.class),
            @ApiResponse(code = 401, message = "Request is not authorized", response = DataGateError.class),
            @ApiResponse(code = 500, message = "Error processing request", response = DataGateError.class),
    })
    public OnlineResponse ping() {
        return new OnlineResponse(true);
    }
}
