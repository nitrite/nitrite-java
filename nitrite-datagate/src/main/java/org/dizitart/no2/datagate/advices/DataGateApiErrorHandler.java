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

package org.dizitart.no2.datagate.advices;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.datagate.UnAuthorizedAccessException;
import org.dizitart.no2.datagate.controllers.InfoController;
import org.dizitart.no2.datagate.controllers.SyncController;
import org.dizitart.no2.datagate.controllers.UserController;
import org.dizitart.no2.datagate.models.DataGateError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Common controller error handlers for Data Gate server.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@Slf4j
@RestControllerAdvice(assignableTypes = {
    InfoController.class,
    UserController.class,
    SyncController.class
})
public class DataGateApiErrorHandler {

    @ExceptionHandler({
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<DataGateError> handleHttpError(HttpMessageNotReadableException e) {
        log.error("DataGate HTTP Error", e);
        DataGateError error = new DataGateError();
        error.setMessage("Invalid request");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
            SecurityException.class,
            UnAuthorizedAccessException.class
    })
    public ResponseEntity<DataGateError> handleSecurityError(RuntimeException se) {
        log.error("DataGate Security Error", se);
        DataGateError error = new DataGateError();
        error.setMessage(se.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({
            Throwable.class
    })
    public ResponseEntity<DataGateError> handleUnhandledException(Throwable t) {
        log.error("DataGate Server Error", t);
        DataGateError error = new DataGateError();
        error.setMessage(t.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
