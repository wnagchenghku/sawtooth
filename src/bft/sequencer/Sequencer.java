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

import java.io.IOException;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.SocketUtils;
import java.net.InetSocketAddress;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.buffer.CompositeByteBuf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public final class Sequencer extends SimpleChannelInboundHandler<DatagramPacket> {

	static InetSocketAddress multicast;
    protected static int port;
    protected Map<String, String> configs;
    static CompositeByteBuf sequenceBuf;
    static int sequnceNum;

    public static void main(String[] args){
        sequenceBuf = Unpooled.compositeBuffer();
        sequnceNum = 0;

    	loadConfig("", "");

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioDatagramChannel.class);

            ChannelFuture f = b.bind(port).sync();

        } catch (InterruptedException ex) {
        } finally {
            group.shutdownGracefully();
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        sequenceBuf.setInt(0, sequnceNum++);
        ctx.writeAndFlush(new DatagramPacket(Unpooled.compositeBuffer().addComponents(sequenceBuf, packet.content()), multicast));
    }

    private static void loadConfig(String configHome, String fileName){
        try{
            String path =  "";
            String sep = System.getProperty("file.separator");
            if(configHome.equals("")){
                   if (fileName.equals(""))
                        path = "config"+sep+"hosts.config";
                   else
                        path = "config"+sep+fileName;
            }else{
                   if (fileName.equals(""))
                        path = configHome+sep+"hosts.config";
                   else
                       path = configHome+sep+fileName;
            }
            FileReader fr = new FileReader(path);
            BufferedReader rd = new BufferedReader(fr);
            String line = null;
            while((line = rd.readLine()) != null){
                if(!line.startsWith("#")){
                    StringTokenizer str = new StringTokenizer(line," ");
                    if (str.nextToken() == "groupaddr") {
                        multicast = SocketUtils.socketAddress(str.nextToken(), Integer.valueOf(str.nextToken()));
                    } else if(str.nextToken() == "listenport"){
                        port = Integer.valueOf(str.nextToken());
                    }
                }
            }
            fr.close();
            rd.close();
        }catch(Exception e){
        }
    }

}
