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
package bft.communication.server;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import bft.reconfiguration.ServerViewController;
import bft.tom.ServiceReplica;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a connection with other server.
 *
 * ServerConnections are created by ServerCommunicationLayer.
 *
 * @author alysson
 */
public class ServerConnection {
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    //public static final String MAC_ALGORITHM = "HmacMD5";
    private static final long POOL_TIME = 5000;
    //private static final int SEND_QUEUE_SIZE = 50;
    private ServerViewController controller;
    private Socket socket;
    private DataOutputStream socketOutStream = null;
    private DataInputStream socketInStream = null;
    private int remoteId;
    private Lock connectLock = new ReentrantLock();
    private boolean doWork = true;

    public ServerConnection(ServerViewController controller, Socket socket, int remoteId, ServiceReplica replica) {

        this.controller = controller;

        this.socket = socket;

        this.remoteId = remoteId;

        // Connect to the remote process or just wait for the connection?
        if (isToConnect()) {
            //I have to connect to the remote server
            try {
                this.socket = new Socket(this.controller.getStaticConf().getHost(remoteId),
                        this.controller.getStaticConf().getServerToServerPort(remoteId));
                ServersCommunicationLayer.setSocketOptions(this.socket);
                new DataOutputStream(this.socket.getOutputStream()).writeInt(this.controller.getStaticConf().getProcessId());

            } catch (UnknownHostException ex) {
                logger.error("Failed to connect to replica",ex);
            } catch (IOException ex) {
                logger.error("Failed to connect to replica",ex);
            }
        }
        //else I have to wait a connection from the remote server

        if (this.socket != null) {
            try {
                socketOutStream = new DataOutputStream(this.socket.getOutputStream());
                socketInStream = new DataInputStream(this.socket.getInputStream());
            } catch (IOException ex) {
                logger.error("Error creating connection to "+remoteId,ex);
            }
        }

               
       //******* EDUARDO BEGIN **************//

        new ReceiverThread().start();
        //******* EDUARDO END **************//
    }
    
    /**
     * Stop message sending and reception.
     */
    public void shutdown() {
        logger.debug("SHUTDOWN for "+remoteId);
        
        doWork = false;
        closeSocket();
    }

    //******* EDUARDO BEGIN **************//
    //return true of a process shall connect to the remote process, false otherwise
    private boolean isToConnect() {
        boolean ret = false;
        if (this.controller.isInCurrentView()) {
            
             //in this case, the node with higher ID starts the connection
             if (this.controller.getStaticConf().getProcessId() > remoteId) {
                 ret = true;
             }
                
            /** JCS: I commented the code below to fix a bug, but I am not sure
             whether its completely useless or not. The 'if' above was taken
             from that same code (its the only part I understand why is necessary)
             I keep the code commented just to be on the safe side*/
            
            /**
            
            boolean me = this.controller.isInLastJoinSet(this.controller.getStaticConf().getProcessId());
            boolean remote = this.controller.isInLastJoinSet(remoteId);

            //either both endpoints are old in the system (entered the system in a previous view),
            //or both entered during the last reconfiguration
            if ((me && remote) || (!me && !remote)) {
                //in this case, the node with higher ID starts the connection
                if (this.controller.getStaticConf().getProcessId() > remoteId) {
                    ret = true;
                }
            //this process is the older one, and the other one entered in the last reconfiguration
            } else if (!me && remote) {
                ret = true;

            } //else if (me && !remote) { //this process entered in the last reconfig and the other one is old
                //ret=false; //not necessary, as ret already is false
            //}
              
            */
        }
        return ret;
    }
    //******* EDUARDO END **************//


    /**
     * (Re-)establish connection between peers.
     *
     * @param newSocket socket created when this server accepted the connection
     * (only used if processId is less than remoteId)
     */
    protected void reconnect(Socket newSocket) {
        
        connectLock.lock();

        if (socket == null || !socket.isConnected()) {

            try {

                //******* EDUARDO BEGIN **************//
                if (isToConnect()) {

                    socket = new Socket(this.controller.getStaticConf().getHost(remoteId),
                            this.controller.getStaticConf().getServerToServerPort(remoteId));
                    ServersCommunicationLayer.setSocketOptions(socket);
                    new DataOutputStream(socket.getOutputStream()).writeInt(this.controller.getStaticConf().getProcessId());

                //******* EDUARDO END **************//
                } else {
                    socket = newSocket;
                }
            } catch (UnknownHostException ex) {
                logger.error("Failed to connect to replica",ex);
            } catch (IOException ex) {
                
                logger.error("Impossible to reconnect to replica " + remoteId + ": " + ex.getMessage());
                //ex.printStackTrace();
            }

            if (socket != null) {
                try {
                    socketOutStream = new DataOutputStream(socket.getOutputStream());
                    socketInStream = new DataInputStream(socket.getInputStream());

                } catch (IOException ex) {
                    logger.error("Failed to authenticate to replica",ex);
                }
            }
        }

        connectLock.unlock();
    }

    private void closeSocket() {
        
        connectLock.lock();
        
        if (socket != null) {
            try {
                socketOutStream.flush();
                socket.close();
            } catch (IOException ex) {
                logger.debug("Error closing socket to "+remoteId);
            } catch (NullPointerException npe) {
            	logger.debug("Socket already closed");
            }

            socket = null;
            socketOutStream = null;
            socketInStream = null;
        }
        
        connectLock.unlock();
    }

    private void waitAndConnect() {
        if (doWork) {
            try {
                Thread.sleep(POOL_TIME);
            } catch (InterruptedException ie) {
            }
            
            reconnect(null);
        }
    }

    /**
     * Thread used to receive packets from the remote server.
     */
    protected class ReceiverThread extends Thread {

        public ReceiverThread() {
            super("Receiver for " + remoteId);
        }

        @Override
        public void run() {

            while (doWork) {
                if (socket != null && socketInStream != null) {
                } else {
                    waitAndConnect();
                }
            }
        }
    }
}
