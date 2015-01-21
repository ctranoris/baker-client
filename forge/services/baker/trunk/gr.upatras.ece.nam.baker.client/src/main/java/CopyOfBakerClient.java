/**
 * Copyright 2014 University of Patras 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and limitations under the License.
 */

import java.net.URL;
import java.security.ProtectionDomain;

import org.apache.cxf.transport.servlet.CXFServlet;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

public class CopyOfBakerClient {


	public static void main(String[] args) throws Exception {

        String marketplace_api_endpoint = args[0];
        System.setProperty("marketplace_api_endpoint",marketplace_api_endpoint);
        
        // Set JSP to use Standard JavaC always
        System.setProperty("org.apache.jasper.compiler.disablejsr199", "false");
        
		Server server = new Server(13001);

//		WebAppContext context = new WebAppContext();
//		context.setDescriptor("src/main/webapp/WEB-INF/web.xml");
//		context.setResourceBase("/src/main/webapp");
//		context.setContextPath("/");
//		context.setParentLoaderPriority(true);
//		server.setHandler(context);
		
		ProtectionDomain domain = CopyOfBakerClient.class.getProtectionDomain();
        URL location = domain.getCodeSource().getLocation();
        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setWar(location.toExternalForm());
        server.setHandler(webapp);
        
        
		server.start();
		server.join();

	}

	// public static void main(String[] args) throws Exception {
	// // Create a basic Jetty server object that will listen on port 8080. Note that if you set this to port 0
	// // then a randomly available port will be assigned that you can either look in the logs for the port,
	// // or programmatically obtain it for use in test cases.
	// Server server = new Server(13001);
	//
	// // Create the ResourceHandler. It is the object that will actually handle the request for a given file. It is
	// // a Jetty Handler object so it is suitable for chaining with other handlers as you will see in other examples.
	// ResourceHandler resource_handler = new ResourceHandler();
	// // Configure the ResourceHandler. Setting the resource base indicates where the files should be served out of.
	// // In this example it is the current directory but it can be configured to anything that the jvm has access to.
	// resource_handler.setDirectoriesListed(true);
	// resource_handler.setWelcomeFiles(new String[]{ "index.html" });
	// resource_handler.setResourceBase(".");
	//
	// // Add the ResourceHandler to the server.
	// HandlerList handlers = new HandlerList();
	// handlers.setHandlers(new Handler[] { resource_handler, new DefaultHandler() });
	// server.setHandler(handlers);
	//
	// // Start things up! By using the server.join() the server thread will join with the current thread.
	// // See "http://docs.oracle.com/javase/1.5.0/docs/api/java/lang/Thread.html#join()" for more details.
	// server.start();
	// server.join();
	//
	// }

}
