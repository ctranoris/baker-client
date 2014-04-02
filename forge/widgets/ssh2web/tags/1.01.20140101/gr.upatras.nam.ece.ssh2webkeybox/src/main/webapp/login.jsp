<%
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
%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
<head>

<!-- 
	 <script type="text/javascript" src="_res/js/meemoo-min.js"></script>
 -->
	
    <jsp:include page="_res/inc/header.jsp"/>

   	<script src="http://meemoo.org/meemoo/v1/meemoo-min.js"></script>
	<script type="text/javascript">
			
		Meemoo.setInfo({
		    title: "ssh2web",
		    author: "ctranoris",
		    description: "logviewer to log files"
		  }).addInputs({
			  username: {
			        type: "string"
			      },

			  password: {
			        type: "string"
			      }  
		
		  }).addOutputs({
			  info: {
			        type: "string"
			      }
		  });
  
  
  
        //break if loaded in frame
        //if(top != self) top.location.replace(location);

        $(document).ready(function() {


            $("#login_btn").button().click(function() {
                $('#loginSubmit').submit();
            });
        });

    </script>
    <title>KeyBox - Login </title>
</head>
<body>

<div class="page">
    <div style="float: left;margin-top: 5px;margin-left: -10px"><img
            src="<%= request.getContextPath() %>/img/keybox_50x38.png"/></div>

    <h3>

        ssh2web - forgebox - Login
    </h3>

    <div class="content" style="border-left:none;">

        <s:actionerror/>
        <s:form action="loginSubmit"  autocomplete="off">
            <s:textfield name="auth.username" label="Username"/>
            <s:password name="auth.password" label="Password"/>
            <tr> <td>&nbsp;</td>
                <td align="right">  <div id="login_btn" class="login" >Login</div></td>
            </tr>
        </s:form>
    </div>
    
    <div class="footer">
    ForgeBox - ssh2web 1.02 - http://www.ict-forge.eu/ - 2014
    </div>
    
</div>

</body>
</html>
