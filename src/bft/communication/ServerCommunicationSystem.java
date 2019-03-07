/**
Copyright (c) 2007-2013 Alysson Bessani, Eduardo Alchieri, Paulo Sousa, and the authors indicated in the @author tags

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

/**
 *
 * @author alysson
 */
package bft.communication;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import bft.communication.client.CommunicationSystemServerSide;
import bft.communication.client.CommunicationSystemServerSideFactory;
import bft.communication.server.ServersCommunicationLayer;
import bftsmart.reconfiguration.ServerViewController;
import bftsmart.tom.ServiceReplica;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerCommunicationSystem extends Thread {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private boolean doWork = true;
    public final long MESSAGE_WAIT_TIME = 100;
    private LinkedBlockingQueue<SystemMessage> inQueue = null;//new LinkedBlockingQueue<SystemMessage>(IN_QUEUE_SIZE);
    protected MessageHandler messageHandler;
    private ServersCommunicationLayer serversConn;
    private CommunicationSystemServerSide clientsConn;
    private ServerViewController controller;

    /**
     * Creates a new instance of ServerCommunicationSystem
     */
    public ServerCommunicationSystem(ServerViewController controller, ServiceReplica replica) throws Exception {
        super("Server CS");

        this.controller = controller;
        
        messageHandler = new MessageHandler();

        inQueue = new LinkedBlockingQueue<SystemMessage>(controller.getStaticConf().getInQueueSize());

        //create a new conf, with updated port number for servers
        //TOMConfiguration serversConf = new TOMConfiguration(conf.getProcessId(),
        //      Configuration.getHomeDir(), "hosts.config");

        //serversConf.increasePortNumber();

        serversConn = new ServersCommunicationLayer(controller, inQueue, replica);

        //******* EDUARDO BEGIN **************//
       // if (manager.isInCurrentView() || manager.isInInitView()) {
            clientsConn = CommunicationSystemServerSideFactory.getCommunicationSystemServerSide(controller);
       // }
        //******* EDUARDO END **************//
        //start();
    }
}