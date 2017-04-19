package org.dizitart.no2.datagate.controllers;

import org.dizitart.no2.sync.data.UserAccount;
import org.dizitart.no2.datagate.services.UserAccountService;
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
}
