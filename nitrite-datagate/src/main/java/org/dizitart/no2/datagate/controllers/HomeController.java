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

import org.dizitart.no2.datagate.services.UserAccountService;
import org.dizitart.no2.sync.types.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import static org.dizitart.no2.datagate.Constants.AUTH_ADMIN;

/**
 * @author Anindya Chatterjee
 */
@Controller
public class HomeController {
    @Autowired
    private UserAccountService userAccountService;

    @GetMapping("/")
    public String home(Model model) {
        if (userAccountService.isNoUserFound()) {
            model.addAttribute("userAccount", new UserAccount());
            return "signUp";
        } else {
            return "redirect:/admin/";
        }
    }

    @PostMapping("/signUp")
    public String signUp(@ModelAttribute UserAccount userAccount) {
        if (userAccount != null) {
            userAccount.setAccountNonExpired(true);
            userAccount.setAccountNonLocked(true);
            userAccount.setAuthorities(new String[] { AUTH_ADMIN });
            userAccount.setEnabled(true);
            userAccountService.insert(userAccount);
            return "redirect:/admin/";
        } else {
            return "signUp";
        }
    }

    @GetMapping(value = "/loginError")
    public String loginError(Model model) {
        model.addAttribute("loginError", true);
        return "login";
    }
}
