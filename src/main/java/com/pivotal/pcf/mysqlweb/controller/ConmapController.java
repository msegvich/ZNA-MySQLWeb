/*
PivotalMySQLWeb

Copyright (c) 2017-Present Pivotal Software, Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.pivotal.pcf.mysqlweb.controller;

import com.pivotal.pcf.mysqlweb.utils.ConnectionManager;
import com.pivotal.pcf.mysqlweb.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
public class ConmapController
{
    protected static Logger logger = LoggerFactory.getLogger(ConmapController.class);

    @GetMapping(value = "/viewconmap")
    public String viewConnections
            (Model model, HttpServletResponse response, HttpServletRequest request, HttpSession session) throws Exception
    {
        if (Utils.verifyConnection(response, session))
        {
            logger.info("user_key is null OR Connection stale so new Login required");
            return null;
        }

        logger.info("Received request to show connection map");

        ConnectionManager cm = ConnectionManager.getInstance();

        String conMapAction = request.getParameter("conMapAction");
        String key = request.getParameter("key");

        if (conMapAction != null)
        {
            logger.info("conMapAction = " + conMapAction);
            logger.info("key = " + key);

            if (conMapAction.equalsIgnoreCase("DELETE"))
            {
                // remove this connection from Map and close it.
                cm.removeDataSource(key);
                logger.info("Connection closed for key " + key);
                model.addAttribute("saved", "Successfully closed connection with key " + key);
            }
        }

        model.addAttribute("conmap", cm.getConnectionMap());
        model.addAttribute("conmapsize", cm.getConnectionListSize());

        return "conmap";
    }
}