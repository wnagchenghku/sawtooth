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
package bft.sequencer;

import java.io.IOException;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.SocketUtils;
import java.net.InetSocketAddress;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public final class Sequencer {

    private Map<String, String> configs;
    private int sequnceNum;
    static final int PORT =7686;

    private String configHome = "";

    public static void main(String[] args){
        new Sequencer().go();
    }

    public void go() {
        sequnceNum = 0;

        loadConfig();

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioDatagramChannel.class)
             .option(ChannelOption.SO_BROADCAST, true)
             .handler(new SequencerHandler());

            ChannelFuture f = b.bind(PORT).sync();

            f.channel().closeFuture().await();
        } catch (InterruptedException ex) {
        } finally {
            group.shutdownGracefully();
        }
    }

    private void loadConfig(){
        configs = new HashMap<>();
        try{
            if(configHome == null || configHome.equals("")){
                configHome="config";
            } 
            String sep = System.getProperty("file.separator");
            String path =  configHome+sep+"system.config";;
            FileReader fr = new FileReader(path);
            BufferedReader rd = new BufferedReader(fr);
            String line = null;
            while((line = rd.readLine()) != null){
                if(!line.startsWith("#")){
                    StringTokenizer str = new StringTokenizer(line,"=");
                    if(str.countTokens() > 1){
                        configs.put(str.nextToken().trim(),str.nextToken().trim());
                    }
                }
            }
            fr.close();
            rd.close();
        }catch(Exception e){
        }
    }

    private class SequencerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
        @Override
        public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
            packet.retain();
            packet.content().setInt(0, sequnceNum++);
            ctx.writeAndFlush(new DatagramPacket(packet.content(), SocketUtils.socketAddress("255.255.255.255", Integer.valueOf(configs.remove("system.multicast.port"))))).sync();
        }
    }

}
