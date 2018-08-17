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

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.datagate.models.Device;
import org.dizitart.no2.datagate.models.Statistics;
import org.dizitart.no2.datagate.services.AnalyticsService;
import org.dizitart.no2.datagate.services.UserAccountService;
import org.dizitart.no2.sync.types.UserAccount;
import org.dizitart.no2.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.dizitart.no2.datagate.Constants.AUTH_CLIENT;
import static org.dizitart.no2.datagate.Constants.AUTH_USER;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
@Controller
@RequestMapping("admin")
public class AdminController {
    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/")
    public String adminHome(Model model) {
        Statistics statistics = analyticsService.getStatistics();
        model.addAttribute("userCount", statistics.getUserCount());
        model.addAttribute("clientCount", statistics.getClientCount());
        model.addAttribute("collCount", statistics.getCollectionCount());
        model.addAttribute("docCount", statistics.getDocumentCount());

        model.addAttribute("chart_plot_data", statistics.getSyncGraphData());
        model.addAttribute("devices", statistics.getDeviceList());

        model.addAttribute("doughnut_labels",
            statistics.getDeviceList().stream().map(Device::getName).toArray());
        model.addAttribute("doughnut_data",
            statistics.getDeviceList().stream().map(Device::getUsage).toArray());
        model.addAttribute("doughnut_backgroundColor",
            statistics.getDeviceList().stream().map(Device::getColor).toArray());
        model.addAttribute("doughnut_hoverBackgroundColor",
            statistics.getDeviceList().stream().map(Device::getHoverColor).toArray());

        model.addAttribute("appList", statistics.getAppList());
        return "adminHome";
    }

    @GetMapping("/clients")
    public String clients(Model model) {
        model.addAttribute("clientList", userAccountService
            .findUsersByAuthorities(AUTH_CLIENT));
        return "clients";
    }

    @GetMapping("/clientUpdate")
    public String clientUpdate(@RequestParam(value = "id", required = false) String clientId, Model model) {
        if (!StringUtils.isNullOrEmpty(clientId)) {
            UserAccount client = userAccountService.findByUsername(clientId);
            if (client != null) {
                model.addAttribute("client", client);
            }
        } else {
            model.addAttribute("client", new UserAccount());
        }
        return "clientUpdate";
    }

    @PostMapping("/clientUpdate")
    public String clientUpdate(@ModelAttribute UserAccount client,
                               @RequestParam(value = "action") String action,
                               Model model) {
        if (client != null) {
            switch (action) {
                case "cancel":
                    break;
                case "submit":
                    if (userAccountService.findByUsername(client.getUserName()) != null) {
                        userAccountService.update(client);
                    } else {
                        userAccountService.insert(client);
                    }
                    break;
                case "delete":
                    userAccountService.delete(client.getUserName());
                    break;
                default:
                    break;
            }
        }
        return clients(model);
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("userList", userAccountService
            .findUsersByAuthorities(AUTH_USER));
        return "users";
    }

    @GetMapping("/userUpdate")
    public String userUpdate(@RequestParam("id") String userId,
                             HttpServletResponse response,
                             Model model) throws IOException {
        if (!StringUtils.isNullOrEmpty(userId)) {
            UserAccount client = userAccountService.findByUsername(userId);
            if (client != null) {
                model.addAttribute("user", client);
            }
        } else {
            response.sendError(400, "A user id must be specified");
        }
        return "userUpdate";
    }

    @PostMapping("/userUpdate")
    public String userUpdate(@ModelAttribute UserAccount user,
                             HttpServletResponse response,
                             @RequestParam(value = "action") String action,
                             Model model) throws IOException {
        if (user != null) {
            switch (action) {
                case "cancel":
                    break;
                case "submit":
                    if (userAccountService.findByUsername(user.getUserName()) != null) {
                        userAccountService.update(user);
                    } else {
                        response.sendError(404, user.getUserName()
                            + " does not exists in the system");
                    }
                    break;
                case "delete":
                    userAccountService.delete(user.getUserName());
                    break;
                default:
                    break;
            }
        }
        return users(model);
    }
}
