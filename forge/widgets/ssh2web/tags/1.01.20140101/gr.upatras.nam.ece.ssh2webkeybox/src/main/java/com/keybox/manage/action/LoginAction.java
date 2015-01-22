/**
 * Copyright 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.keybox.manage.action;

import com.keybox.common.util.AuthUtil;
import com.keybox.manage.db.AuthDB;
import com.keybox.manage.model.Auth;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Action to auth to keybox
 */
public class LoginAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {

    HttpServletResponse servletResponse;
    HttpServletRequest servletRequest;
    Auth auth;

    @Action(value = "/login",
            results = {
                    @Result(name = "success", location = "/login.jsp")
            }
    )
    public String login() {

        return SUCCESS;
    }

    @Action(value = "/admin/menu",
            results = {
                    @Result(name = "success", location = "/admin/menu.jsp")
            }
    )
    public String menu() {

        return SUCCESS;
    }


    @Action(value = "/loginSubmit",
            results = {
                    @Result(name = "input", location = "/login.jsp"),
                    @Result(name = "change_password", location = "/admin/setPassword.action", type = "redirect"),
                    @Result(name = "createoneconsoleX", location = "/admin/createOneConsoleX.action", type = "redirect"),
                    @Result(name = "success", location = "/admin/menu.action", type = "redirect")
            }
    )
    public String loginSubmit() {
        String retVal = SUCCESS;
        
        //pl3gu3$t#
        System.out.println("In loginSubmit");
        String authToken = AuthDB.login(auth);
        System.out.println("after loginSubmit authToken="+authToken);

        
        if (authToken != null) {
            AuthUtil.setAuthToken(servletRequest.getSession(), authToken);
            AuthUtil.setUserId(servletRequest.getSession(), AuthDB.getUserIdByAuthToken(authToken));
            AuthUtil.setTimeout(servletRequest.getSession());

            if ("changeme".equals(auth.getPassword())) {
                retVal = "change_password";
            }
            
            if ( auth.getUsername().contains("guest")  ) {
                retVal = "createoneconsoleX";
            }

        } else {
            System.out.println("Invalid username and password combination");
            addActionError("Invalid username and password combination");
            retVal = INPUT;
        }



        System.out.println("after loginSubmit retVal="+retVal);
        return retVal;
    }

    @Action(value = "/logout",
            results = {
                    @Result(name = "success", location = "/login.action", type = "redirect")
            }
    )
    public String logout() {
        AuthUtil.deleteAllSession(servletRequest.getSession());
        return SUCCESS;
    }

    @Action(value = "/admin/setPassword",
            results = {
                    @Result(name = "success", location = "/admin/set_password.jsp")
            }
    )
    public String setPassword() {

        return SUCCESS;
    }

    
    
    

    
    
    
    @Action(value = "/admin/passwordSubmit",
            results = {
                    @Result(name = "input", location = "/admin/set_password.jsp"),
                    @Result(name = "success", location = "/admin/menu.action", type = "redirect")
            }
    )
    public String passwordSubmit() {
        String retVal = SUCCESS;
        
        if (auth.getPassword().equals(auth.getPasswordConfirm())) {
            auth.setAuthToken(AuthUtil.getAuthToken(servletRequest.getSession()));

            if (!AuthDB.updatePassword(auth)) {
                addActionError("Current password is invalid");
                retVal = INPUT;
            }

        } else {
            addActionError("Passwords do not match");
            retVal = INPUT;
        }


        return retVal;
    }


    /**
     * Validates fields for auth submit
     */
    public void validateLoginSubmit() {
        if (auth.getUsername() == null ||
                auth.getUsername().trim().equals("")) {
            addFieldError("auth.username", "Required");
        }
        if (auth.getPassword() == null ||
                auth.getPassword().trim().equals("")) {
            addFieldError("auth.password", "Required");
        }


    }


    /**
     * Validates fields for password submit
     */
    public void validatePasswordSubmit() {
        if (auth.getPassword() == null ||
                auth.getPassword().trim().equals("")) {
            addFieldError("auth.password", "Required");
        }
        if (auth.getPasswordConfirm() == null ||
                auth.getPasswordConfirm().trim().equals("")) {
            addFieldError("auth.passwordConfirm", "Required");
        }
        if (auth.getPrevPassword() == null ||
                auth.getPrevPassword().trim().equals("")) {
            addFieldError("auth.prevPassword", "Required");
        }


    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }


    public HttpServletResponse getServletResponse() {
        return servletResponse;
    }

    public void setServletResponse(HttpServletResponse servletResponse) {
        this.servletResponse = servletResponse;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }
}