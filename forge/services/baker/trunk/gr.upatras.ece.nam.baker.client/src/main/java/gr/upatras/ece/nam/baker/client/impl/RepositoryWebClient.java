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

package gr.upatras.ece.nam.baker.client.impl;

import gr.upatras.ece.nam.baker.client.model.BakerUser;
import gr.upatras.ece.nam.baker.client.model.BunMetadata;
import gr.upatras.ece.nam.baker.client.model.IRepositoryWebClient;
import gr.upatras.ece.nam.baker.client.model.SubscribedResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class RepositoryWebClient implements IRepositoryWebClient {

	private static final transient Log logger = LogFactory.getLog(RepositoryWebClient.class.getName());

	@Override
	public BunMetadata fetchMetadata(String uuid, String url) {
		logger.info("fetchMetadata from: " + url + " , for uuid=" + uuid);

		try {
			List<Object> providers = new ArrayList<Object>();
			providers.add(new org.codehaus.jackson.jaxrs.JacksonJsonProvider());
			WebClient client = WebClient.create(url, providers);
			Response r = client.accept("application/json").type("application/json").get();
			if (r.getStatus() == Response.Status.OK.getStatusCode()) {
				MappingJsonFactory factory = new MappingJsonFactory();
				JsonParser parser = factory.createJsonParser((InputStream) r.getEntity());
				BunMetadata output = parser.readValueAs(BunMetadata.class);
				return output;
			}

		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public Path fetchPackageFromLocation(String uuid, String packageLocation) {

		logger.info("fetchPackageFromLocation: " + packageLocation);

		try {
			WebClient client = WebClient.create(packageLocation);
			Response r = client.get();

			InputStream inputStream = (InputStream) r.getEntity();

			// Path tempDir = Files.createTempDirectory("baker");
			String tempDir = System.getProperty("user.home") + File.separator + ".baker" + File.separator + "extractedbuns";
			File destFile = new File(tempDir + File.separator + uuid + File.separator + "bun.tar.gz");
			Files.createDirectories(Paths.get(tempDir + File.separator + uuid));
			Path targetPath = destFile.toPath();
			OutputStream output = new FileOutputStream(targetPath.toFile());
			IOUtils.copy(inputStream, output);

			return targetPath;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public SubscribedResource registerClientToRepo(String uuid) {

		SubscribedResource sr = new SubscribedResource();
		
		try {
			String url = System.getProperty("marketplace_api_endpoint") + "/registerresource";
			BakerUser owner = new BakerUser();

			sr.setOwner(owner);
			sr.setUuid(uuid);

			String IPs = "";

			Enumeration nics;

			nics = NetworkInterface.getNetworkInterfaces();

			while (nics.hasMoreElements()) {
				NetworkInterface n = (NetworkInterface) nics.nextElement();
				Enumeration ee = n.getInetAddresses();
				while (ee.hasMoreElements()) {
					InetAddress i = (InetAddress) ee.nextElement();
					IPs = IPs + "HostAddress=" + i.getHostAddress() + ",";
				}
			}

			IPs = IPs + "FQDN =" + System.getProperty("baker_client_host_fqdn");
			sr.setURL(IPs);

			owner.setId(Integer.parseInt(System.getProperty("baker_client_owner_id")));
			owner.setUsername(System.getProperty("baker_client_owner_username"));

			logger.info("registerClientToRepo from: " + url + " , for uuid=" + uuid);

			List<Object> providers = new ArrayList<Object>();
			providers.add(new org.codehaus.jackson.jaxrs.JacksonJsonProvider());
			WebClient client = WebClient.create(url, providers);
			Response r = client.accept("application/json").type("application/json").post(sr);

			if (r.getStatus() == Response.Status.OK.getStatusCode()) {
				MappingJsonFactory factory = new MappingJsonFactory();
				JsonParser parser = factory.createJsonParser((InputStream) r.getEntity());
				sr = parser.readValueAs(SubscribedResource.class);

				logger.info("registerClientToRepo OK SubscribedResource: " + sr.getId() + " , for uuid=" + sr.getUuid());
					
				return sr;
			}

		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;

	}

	@Override
	public List<BunMetadata> getRepoBuns(String url) {
		logger.info("fetchMetadata from: " + url  );

		try {
			List<Object> providers = new ArrayList<Object>();
			providers.add(new org.codehaus.jackson.jaxrs.JacksonJsonProvider());
			WebClient client = WebClient.create(url, providers);
			Response r = client.accept("application/json").type("application/json").get();
			if (r.getStatus() == Response.Status.OK.getStatusCode()) {
				MappingJsonFactory factory = new MappingJsonFactory();
				JsonParser parser = factory.createJsonParser((InputStream) r.getEntity());
				
				JsonNode node = parser.readValueAsTree();
				ObjectMapper mapper = new ObjectMapper();
				TypeReference<List<BunMetadata>> typeRef = new TypeReference<List<BunMetadata>>() {
				};
				List<BunMetadata> output = mapper.readValue(node.traverse(), typeRef);				
				
				return output;
			}

		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public BunMetadata getRepoBun(String url) {
		logger.info("fetchMetadata from: " + url  );

		try {
			List<Object> providers = new ArrayList<Object>();
			providers.add(new org.codehaus.jackson.jaxrs.JacksonJsonProvider());
			WebClient client = WebClient.create(url, providers);
			Response r = client.accept("application/json").type("application/json").get();
			if (r.getStatus() == Response.Status.OK.getStatusCode()) {
				MappingJsonFactory factory = new MappingJsonFactory();
				JsonParser parser = factory.createJsonParser((InputStream) r.getEntity());
				BunMetadata output = parser.readValueAs(BunMetadata.class);
				return output;
			}

		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

}
