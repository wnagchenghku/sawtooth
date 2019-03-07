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
package bft.communication.client;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.crypto.Mac;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import bftsmart.communication.client.CommunicationSystemServerSide;
import bftsmart.communication.client.RequestReceiver;
import bftsmart.reconfiguration.ServerViewController;
import bftsmart.tom.core.messages.TOMMessage;
import bftsmart.tom.util.TOMUtil;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;

/**
 *
 * @author Paulo
 */
@Sharable
public class ClientServerCommunicationSystemServerSide extends SimpleChannelInboundHandler<TOMMessage> implements CommunicationSystemServerSide {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private RequestReceiver requestReceiver;
    private HashMap sessionTable;
    private ReentrantReadWriteLock rl;
    private ServerViewController controller;
    private boolean closed = false;
    private Channel mainChannel;

    // This locked seems to introduce a bottleneck and seems useless, but I cannot recall why I added it
    //private ReentrantLock sendLock = new ReentrantLock();
    private NettyServerPipelineFactory serverPipelineFactory;
        
	public ClientServerCommunicationSystemServerSide(ServerViewController controller) {
		try {

			this.controller = controller;
			sessionTable = new HashMap();
			rl = new ReentrantReadWriteLock();

			//Configure the server.
			Mac macDummy = TOMUtil.getMacFactory();

			serverPipelineFactory = new NettyServerPipelineFactory(this, sessionTable, controller, rl);

			EventLoopGroup bossGroup = new NioEventLoopGroup();
                        
                        //If the numbers of workers are not specified by the configuration file,
                        //the event group is created with the default number of threads, which
                        //should be twice the number of cores available.
                        int nWorkers = this.controller.getStaticConf().getNumNettyWorkers();
			EventLoopGroup workerGroup = (nWorkers > 0 ? new NioEventLoopGroup(nWorkers) : new NioEventLoopGroup());

			ServerBootstrap b = new ServerBootstrap(); 
			b.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class) 
			.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(serverPipelineFactory.getDecoder());
					ch.pipeline().addLast(serverPipelineFactory.getEncoder());
					ch.pipeline().addLast(serverPipelineFactory.getHandler());
				}
			})	.childOption(ChannelOption.SO_KEEPALIVE, true).childOption(ChannelOption.TCP_NODELAY, true);

                        String myAddress;
                        String confAddress =
                                    controller.getStaticConf().getRemoteAddress(controller.getStaticConf().getProcessId()).getAddress().getHostAddress();
                        
                        if (InetAddress.getLoopbackAddress().getHostAddress().equals(confAddress)) {
                            
                            myAddress = InetAddress.getLoopbackAddress().getHostAddress();
                            
                        }
                        
                        else if (controller.getStaticConf().getBindAddress().equals("")) {
                            
                            myAddress = InetAddress.getLocalHost().getHostAddress();
                              
                            //If Netty binds to the loopback address, clients will not be able to connect to replicas.
                            //To solve that issue, we bind to the address supplied in config/hosts.config instead.
                            if (InetAddress.getLoopbackAddress().getHostAddress().equals(myAddress) && !myAddress.equals(confAddress)) {
                                
                                myAddress = confAddress;
                            }
                            
                            
                        } else {
                            
                            myAddress = controller.getStaticConf().getBindAddress();
                        }
                        
                        int myPort = controller.getStaticConf().getPort(controller.getStaticConf().getProcessId());

			ChannelFuture f = b.bind(new InetSocketAddress(myAddress, myPort)).sync(); 

			logger.info("ID = " + controller.getStaticConf().getProcessId());
			logger.info("N = " + controller.getCurrentViewN());
			logger.info("F = " + controller.getCurrentViewF());
        		logger.info("Port = " + controller.getStaticConf().getPort(controller.getStaticConf().getProcessId()));
			logger.info("requestTimeout = " + controller.getStaticConf().getRequestTimeout());
			logger.info("maxBatch = " + controller.getStaticConf().getMaxBatchSize());
			if (controller.getStaticConf().getUseMACs() == 1) logger.info("Using MACs");
			if(controller.getStaticConf().getUseSignatures() == 1) logger.info("Using Signatures");
                        logger.info("Binded replica to IP address " + myAddress);
                        //******* EDUARDO END **************//
                        
                        mainChannel = f.channel();

		} catch (NoSuchAlgorithmException | InterruptedException | UnknownHostException ex) {
			logger.error("Failed to create Netty communication system",ex);
		}
	}
}
