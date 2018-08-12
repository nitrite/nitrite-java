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

import org.dizitart.no2.datagate.models.DataGateError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Anindya Chatterjee
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class DataGateErrorController implements ErrorController {

    @Autowired
    private ErrorAttributes errorAttributes;

    @RequestMapping
    @ResponseBody
    public ResponseEntity<DataGateError> error(HttpServletRequest request) {
        HttpStatus status = getStatus(request);
        RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        Map<String, Object> attributeMap = this.errorAttributes.getErrorAttributes(requestAttributes, false);
        DataGateError dataGateError = new DataGateError();
        if (attributeMap != null) {
            String message = (String) attributeMap.get("message");
            dataGateError.setMessage(message);
        } else {
            dataGateError.setMessage("Unknown error");
        }

        return new ResponseEntity<>(dataGateError, status);
    }

    @RequestMapping(produces = "text/html")
    public String errorPage(HttpServletRequest request, Model model) {
        String message, error;
        HttpStatus status = getStatus(request);
        RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        Map<String, Object> attributeMap = this.errorAttributes.getErrorAttributes(requestAttributes, false);

        if (attributeMap != null) {
            message = (String) attributeMap.get("message");
            error = (String) attributeMap.get("error");
            model.addAttribute("header", error);
            model.addAttribute("message", message);
        }

        if (status.is4xxClientError()){
            if(status.value() == 404) {
                model.addAttribute("header", "Sorry but we couldn't find this page");
                model.addAttribute("message", "This page you are looking for does not exist.");
            } else if (status.value() == 403) {
                model.addAttribute("header", "Access denied");
                model.addAttribute("message", "Full authentication is required to access this resource.");
            }
        } else if (status.value() == 500){
            model.addAttribute("header", "Internal Server Error");
            model.addAttribute("message",
                    "If the problem persists feel free to create an issue in Github.");
        }
        model.addAttribute("number", status.value());
        return "errorPage";
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }

    private HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request
                .getAttribute("javax.servlet.error.status_code");
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        try {
            return HttpStatus.valueOf(statusCode);
        }
        catch (Exception ex) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
