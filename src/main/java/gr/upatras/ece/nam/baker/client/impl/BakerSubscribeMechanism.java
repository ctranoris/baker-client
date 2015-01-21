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

import gr.upatras.ece.nam.baker.client.model.IRepositoryWebClient;
import gr.upatras.ece.nam.baker.client.model.SubscribedResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BakerSubscribeMechanism {

	private static final transient Log logger = LogFactory.getLog(BakerSubscribeMechanism.class.getName());
	private IRepositoryWebClient repoWebClient;
	private BakerJpaController bakerJpaController;
	private volatile Thread registerClientThread;
	private volatile Thread pollBrokerThread;

	public BakerSubscribeMechanism() {
		logger.info("======> Creating BakerSubscribeMechanism");
		
		(new Thread(new RegisterClientRunnable(registerClientThread))).start();
		(new Thread(new PollBrokerRunnable(pollBrokerThread))).start();

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
			
			
			while ( !threadSuspended ) {
				
				
				try {

					//REGISTERclient
					logger.info("REGISTERclient thread");
					String uuid = "";
					SubscribedResource sr=null;
					
					if ( (bakerJpaController!=null) && (bakerJpaController.readPropertyByName("UUID")!=null) ){
						uuid = bakerJpaController.readPropertyByName("UUID").getValue();
						sr = repoWebClient.registerClientToRepo(uuid);
						logger.info("REGISTERclient thread succesfully registered sr="+sr.toString());
						
					}
					
					if (sr!=null){
						logger.info("REGISTERclient thread succesfully registered");
						threadSuspended = true;
						
					}
					else {
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
			while ( true ) {
				try {
					Thread.sleep(60 * 1000); // wait a minute

					synchronized (this) {
						while (threadSuspended)
							wait();
					}

				} catch (InterruptedException e) {
				}

				//REGISTERclient
				logger.info("PollBroker thread");
				
			}

		}
		

	}

}
