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

import java.util.List;

import gr.upatras.ece.nam.baker.client.model.BunMetadata;
import gr.upatras.ece.nam.baker.client.model.DeployArtifact;
import gr.upatras.ece.nam.baker.client.model.DeployContainer;
import gr.upatras.ece.nam.baker.client.model.IRepositoryWebClient;
import gr.upatras.ece.nam.baker.client.model.InstalledBun;
import gr.upatras.ece.nam.baker.client.model.InstalledBunStatus;
import gr.upatras.ece.nam.baker.client.model.SubscribedResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BakerSubscribeMechanism {

	private static final transient Log logger = LogFactory.getLog(BakerSubscribeMechanism.class.getName());
	private IRepositoryWebClient repoWebClient;
	private BakerJpaController bakerJpaController;
	private BakerInstallationMgmt bakerInstallationMgmtRef;
	
	

	private volatile Thread registerClientThread;
	private volatile Thread pollBrokerThread;

	public BakerSubscribeMechanism() {
		logger.info("======> Creating BakerSubscribeMechanism");

		(new Thread(new RegisterClientRunnable(registerClientThread))).start();
		(new Thread(new PollBrokerRunnable(pollBrokerThread))).start();

	}

	public BakerInstallationMgmt getBakerInstallationMgmtRef() {
		return bakerInstallationMgmtRef;
	}

	public void setBakerInstallationMgmtRef(BakerInstallationMgmt bakerInstallationMgmtRef) {
		this.bakerInstallationMgmtRef = bakerInstallationMgmtRef;
	}
	
	public IRepositoryWebClient getRepoWebClient() {
		return repoWebClient;
	}

	public void setRepoWebClient(IRepositoryWebClient repoWebClient) {
		this.repoWebClient = repoWebClient;
	}

	public BakerJpaController getBakerJpaController() {
		return bakerJpaController;
	}

	public void setBakerJpaController(BakerJpaController b) {
		this.bakerJpaController = b;

	}

	public class RegisterClientRunnable implements Runnable {

		private boolean threadSuspended = false;

		public RegisterClientRunnable(Thread registerClientThread) {

		}

		@Override
		public void run() {
			Thread thisThread = Thread.currentThread();

			while (!threadSuspended) {

				try {
					// REGISTERclient
					logger.info("REGISTERclient thread");
					String uuid = "";
					SubscribedResource sr = null;

					if ((bakerJpaController != null) && (bakerJpaController.readPropertyByName("UUID") != null)) {
						uuid = bakerJpaController.readPropertyByName("UUID").getValue();
						sr = repoWebClient.registerClientToRepo(uuid);
						if (sr!=null)
							logger.info("REGISTERclient thread succesfully registered SubscribedResource=" + sr.getUuid() );
					}

					if (sr != null) {
						logger.info("REGISTERclient thread succesfully registered");
						threadSuspended = true;
					} else {
						logger.info("REGISTERclient thread will retry in 1 min");
						Thread.sleep(60 * 1000); // wait a minute
					}
				} catch (InterruptedException e) {

				}
			}
		}
	}

	public class PollBrokerRunnable implements Runnable {


		private boolean threadSuspended = false;

		public PollBrokerRunnable(Thread pollBrokerThread) {

		}

		@Override
		public void run() {
			Thread thisThread = Thread.currentThread();
			while (!threadSuspended) {

				try {
					
					logger.info("PollBroker thread will try in 1 min");
					Thread.sleep(60 * 1000); // wait a minute
					String clientUUID = "";
					SubscribedResource sr = null;

					if ((bakerJpaController != null) && (bakerJpaController.readPropertyByName("UUID") != null)) {
						clientUUID = bakerJpaController.readPropertyByName("UUID").getValue();
						DeployContainer dc = repoWebClient.getDeployContainerInfo(clientUUID);
						if (dc!=null){
							logger.info("PollBroker got a DeployContainer=" + dc.getId());
							List<DeployArtifact> das = dc.getDeployArtifacts();
							for (DeployArtifact deployArtifact : das) {								
								logger.info("deployArtifact artifactPackageURL=" + deployArtifact.getArtifactPackageURL() );
								logger.info("deployArtifact status =" + deployArtifact.getStatus() );
								logger.info("deployArtifact uuid =" + deployArtifact.getUuid()  );
								InstalledBun ibtest = bakerInstallationMgmtRef.getBun( deployArtifact.getUuid() );
								
								
								if ((ibtest != null) && (ibtest.getStatus()!= InstalledBunStatus.FAILED )&& (ibtest.getStatus()!= InstalledBunStatus.UNINSTALLED ) ){
									logger.info("installedBun status =" + ibtest.getStatus()  ); 
									repoWebClient.reportToContainerBunStatus(clientUUID, ibtest.getUuid(), ibtest.getStatus() );
									
								}else if (deployArtifact.getStatus().equals( InstalledBunStatus.INIT ) ) {
									
									logger.info("installedBun is not installed. We must take action if deployArtifact.getStatus()==INIT"  );
									logger.info("Fetching info for BunMetadata with UUID=  " + deployArtifact.getUuid() );									
									String url = System.getProperty("marketplace_api_endpoint")+"/buns/uuid/"+deployArtifact.getUuid();								
									
									//logger.info(" BunMetadata for UUID=  " + deployArtifact.getUuid() + " downloaded. Bun.id = " + bun.getId()+". Starting installation.");			

									InstalledBun installedBun = bakerInstallationMgmtRef.installBunAndStart( deployArtifact.getUuid(),  url );
									Thread.sleep(5*60*1000); // wait 5 minutes
									
								}
								
							}
						}else{
							logger.info("PollBroker got none DeployContainer");
						}
					}

					
					
					
					
				} catch (InterruptedException e) {

				}
			}

		}

	}

}
