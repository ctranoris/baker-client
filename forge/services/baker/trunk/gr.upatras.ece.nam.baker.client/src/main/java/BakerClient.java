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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.log.JavaUtilLog;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.webapp.WebAppContext;


/**
 * Example of using JSP's with embedded jetty and not requiring
 * all of the overhead of a WebAppContext
 */
public class BakerClient
{
    // Resource path pointing to where the WEBROOT is
    //private static final String WEBROOT_INDEX = "/webapp/";

    public static void main(String[] args) throws Exception
    {
    	//mvn clean install, to produce a war with embedded jetty that runs with: 
    	//java -jar '-Dmarketplace_api_endpoint=http://localhost:13000/baker/services/api/repo' '-Dbaker_client_owner_id=1' '-Dbaker_client_owner_username=admin' '-Dbaker_client_host_fqdn=example.com' gr.upatras.ece.nam.baker.client-0.0.1-SNAPSHOT.war 


        //String marketplace_api_endpoint = args[0];
        //System.setProperty("marketplace_api_endpoint",marketplace_api_endpoint);

    	
        int port = 13001;
        Log.setLog(new JavaUtilLog());

        LOG.info("marketplace_api_endpoint= "+System.getProperty("marketplace_api_endpoint"));
        LOG.info("baker_client_owner_id= "+System.getProperty("baker_client_owner_id"));
        LOG.info("baker_client_owner_username= "+System.getProperty("baker_client_owner_username"));
        LOG.info("baker_client_host_fqdn= "+System.getProperty("baker_client_host_fqdn"));
    	
    	
        BakerClient main = new BakerClient(port);
        main.start();
        main.waitForInterrupt();
    }

    private static final Logger LOG = Logger.getLogger(BakerClient.class.getName());

    private int port;
    private Server server;
    private URI serverURI;

    public BakerClient(int port)
    {
        this.port = port;
    }

    public URI getServerURI()
    {
        return serverURI;
    }

    public void start() throws Exception
    {
        server = new Server();
        ServerConnector connector = connector();
        server.addConnector(connector);

       
        ProtectionDomain domain = BakerClient.class.getProtectionDomain();
        URL location = domain.getCodeSource().getLocation();
        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        
        webapp.setWar(location.toExternalForm());
        server.setHandler(webapp);

        // Start Server
        server.start();

        // Show server state
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine(server.dump());
        }
        this.serverURI = getServerUri(connector);
    }

    private ServerConnector connector()
    {
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        return connector;
    }

   

   

    /**
     * Establish the Server URI
     */
    private URI getServerUri(ServerConnector connector) throws URISyntaxException
    {
        String scheme = "http";
        for (ConnectionFactory connectFactory : connector.getConnectionFactories())
        {
            if (connectFactory.getProtocol().equals("SSL-http"))
            {
                scheme = "https";
            }
        }
        String host = connector.getHost();
        if (host == null)
        {
            host = "localhost";
        }
        int port = connector.getLocalPort();
        serverURI = new URI(String.format("%s://%s:%d/", scheme, host, port));
        LOG.info("Server URI: " + serverURI);
        return serverURI;
    }

    public void stop() throws Exception
    {
        server.stop();
    }

    /**
     * Cause server to keep running until it receives a Interrupt.
     * <p>
     * Interrupt Signal, or SIGINT (Unix Signal), is typically seen as a result of a kill -TERM {pid} or Ctrl+C
     */
    public void waitForInterrupt() throws InterruptedException
    {
        server.join();
    }
}
