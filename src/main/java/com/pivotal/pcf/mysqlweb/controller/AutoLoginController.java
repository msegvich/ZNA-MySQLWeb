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

import com.pivotal.pcf.mysqlweb.beans.UserPref;
import com.pivotal.pcf.mysqlweb.beans.WebResult;
import com.pivotal.pcf.mysqlweb.dao.PivotalMySQLWebDAOFactory;
import com.pivotal.pcf.mysqlweb.dao.generic.Constants;
import com.pivotal.pcf.mysqlweb.dao.generic.GenericDAO;
import com.pivotal.pcf.mysqlweb.utils.AdminUtil;
import com.pivotal.pcf.mysqlweb.utils.ConnectionManager;
import com.pivotal.pcf.mysqlweb.utils.MysqlConnection;
import com.pivotal.pcf.mysqlweb.utils.Themes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Connection;
import java.util.LinkedList;
import java.util.Map;

@Controller
public class AutoLoginController
{
    protected static Logger logger = LoggerFactory.getLogger(AutoLoginController.class);

    @Autowired
    UserPref userPref;

    @GetMapping(value = "/autologin")
    public String autoLogin
            (Model model,
             HttpSession session,
             HttpServletRequest request) throws Exception
    {
        logger.info("Received request to auto login");

        ConnectionManager cm = ConnectionManager.getInstance();
        String username = null;
        String passwd = null;
        String url = null;
        WebResult databaseList;
        try
        {
            username = fixRequestParam(request.getParameter("username"));
            passwd = fixRequestParam(request.getParameter("passwd"));
            url = fixRequestParam(request.getParameter("url"));

            logger.info("username = " + username);
            logger.info("url = " + url);

            if (username.trim().equals(""))
            {
                cm.addDataSourceConnection(AdminUtil.newSingleConnectionDataSource(url, null, null), session.getId());
            }
            else
            {
                cm.addDataSourceConnection(AdminUtil.newSingleConnectionDataSource(url, username, passwd), session.getId());
            }

            MysqlConnection newConn =
                    new MysqlConnection
                            (url,
                             new java.util.Date().toString(),
                             username.toUpperCase());

            cm.addConnection(newConn, session.getId());
            cm.addDataSourceConnection(AdminUtil.newSingleConnectionDataSource(url, username, passwd), session.getId());

            String schema = url.substring(url.lastIndexOf("/") + 1);

            session.setAttribute("user_key", session.getId());
            session.setAttribute("user", username.toUpperCase());
            session.setAttribute("schema", schema);
            session.setAttribute("url", url);
            session.setAttribute("prefs", userPref);
            session.setAttribute("history", new LinkedList());
            session.setAttribute("connectedAt", new java.util.Date().toString());
            session.setAttribute("themeMain", Themes.defaultTheme);
            session.setAttribute("themeMin", Themes.defaultThemeMin);

            GenericDAO genericDAO = PivotalMySQLWebDAOFactory.getGenericDAO();

            databaseList = genericDAO.runGenericQuery
                    (Constants.DATABASE_LIST, null, session.getId(), -1);

            Map<String, Long> schemaMap;
            schemaMap = genericDAO.populateSchemaMap(schema, session.getId());

            logger.info("schemaMap=" + schemaMap);
            session.setAttribute("schemaMap", schemaMap);

            model.addAttribute("databaseList", databaseList);

            logger.info(userPref.toString());

        }
        catch (Exception ex)
        {
            model.addAttribute("loginerror", ex.getMessage());
            model.addAttribute("loginObj");
            return "login";
        }

        return "main";
    }

    private String fixRequestParam (String s)
    {
        if (s == null)
        {
            return "";
        }
        else
        {
            return s;
        }
    }
}
