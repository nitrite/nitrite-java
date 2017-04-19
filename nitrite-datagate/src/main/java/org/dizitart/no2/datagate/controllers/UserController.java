package org.dizitart.no2.datagate.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.sync.data.UserAccount;
import org.dizitart.no2.datagate.models.DataGateError;
import org.dizitart.no2.datagate.services.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for user management endpoints.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@Slf4j
@RestController
@RequestMapping(path = "api/v1/user")
public class UserController {

    @Autowired
    private UserAccountService userAccountService;

    @RequestMapping(path = "/{username}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "This operation gets an user details for sync client authentication.")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid request", response = DataGateError.class),
        @ApiResponse(code = 401, message = "Request is not authorized", response = DataGateError.class),
        @ApiResponse(code = 500, message = "Error processing request", response = DataGateError.class),
    })
    public UserAccount getUserDetails(
            @PathVariable
            @ApiParam("The username of the user to search")
            String username) {
        return userAccountService.findByUsername(username);
    }

    @RequestMapping(path = "/create",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "This operation creates an user for sync client authentication.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request", response = DataGateError.class),
            @ApiResponse(code = 401, message = "Request is not authorized", response = DataGateError.class),
            @ApiResponse(code = 500, message = "Error processing request", response = DataGateError.class),
    })
    public void createUser(
            @RequestBody
            @ApiParam("The user account details")
            UserAccount userAccount) {
        userAccountService.insert(userAccount);
    }

    @RequestMapping(path = "/update",
        method = RequestMethod.PUT,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "This operation creates an user for sync client authentication.")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid request", response = DataGateError.class),
        @ApiResponse(code = 401, message = "Request is not authorized", response = DataGateError.class),
        @ApiResponse(code = 500, message = "Error processing request", response = DataGateError.class),
    })
    public void updateUser(
        @RequestBody
        @ApiParam("The user account details")
            UserAccount userAccount) {
        userAccountService.update(userAccount);
    }

    @RequestMapping(path = "/delete/{username}",
            method = RequestMethod.DELETE)
    @ApiOperation(value = "This operation deletes an user for sync authentication.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request", response = DataGateError.class),
            @ApiResponse(code = 401, message = "Request is not authorized", response = DataGateError.class),
            @ApiResponse(code = 500, message = "Error processing request", response = DataGateError.class),
    })
    public void deleteUser(
            @PathVariable
            @ApiParam("The username of the user to delete")
            String username) {
        userAccountService.delete(username);
    }
}
