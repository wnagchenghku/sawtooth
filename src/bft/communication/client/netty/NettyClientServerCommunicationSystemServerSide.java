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
package bft.communication.client.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.DatagramPacket;

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

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import bft.communication.client.CommunicationSystemServerSide;
import bft.reconfiguration.ServerViewController;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;

/**
 *
 * @author Paulo
 */
@Sharable
public class NettyClientServerCommunicationSystemServerSide extends SimpleChannelInboundHandler<DatagramPacket> implements CommunicationSystemServerSide {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private HashMap sessionTable;
    private ReentrantReadWriteLock rl;
    private ServerViewController controller;
    private boolean closed = false;
    private Channel mainChannel;

    // This locked seems to introduce a bottleneck and seems useless, but I cannot recall why I added it
    //private ReentrantLock sendLock = new ReentrantLock();
    private NettyServerPipelineFactory serverPipelineFactory;
        
	public NettyClientServerCommunicationSystemServerSide(ServerViewController controller) {
		try {

			this.controller = controller;
			sessionTable = new HashMap();
            rl = new ReentrantReadWriteLock();

            serverPipelineFactory = new NettyServerPipelineFactory(this, sessionTable, controller, rl);

            EventLoopGroup group = new NioEventLoopGroup();
            
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioDatagramChannel.class)
             .option(ChannelOption.SO_BROADCAST, true)
             .handler(serverPipelineFactory.getDecoder());

			ChannelFuture f = b.bind(controller.getStaticConf().getMulticastPort()).sync();

            logger.info("ID = " + controller.getStaticConf().getProcessId());
                        
                        mainChannel = f.channel();

		} catch (InterruptedException ex) {
			logger.error("Failed to create Netty communication system",ex);
		}
	}


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
    }
}
