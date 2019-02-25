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
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.SocketUtils;
import java.net.InetSocketAddress;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.buffer.ByteBuf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class Client {

	static InetSocketAddress sequencer;

	public static void main(String[] args) {

		loadConfig("", "");

		try {
			int numberOfOps = (args.length > 2) ? Integer.parseInt(args[2]) : 1000;
			int dataSize = Integer.parseInt(args[1]);

			ByteBuf buf = Unpooled.buffer(dataSize);

			EventLoopGroup group = new NioEventLoopGroup();
			Bootstrap b = new Bootstrap();
			b.group(group)
			 .channel(NioDatagramChannel.class);

			Channel ch = b.bind(0).sync().channel();

			for (int i = 0; i < numberOfOps; i++) {
				ch.writeAndFlush(new DatagramPacket(buf, sequencer)).sync();
			}
		} catch(InterruptedException ex) {
			
		}
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
                    if (str.nextToken() == "sequenceraddr") {
                        sequencer = SocketUtils.socketAddress(str.nextToken(), Integer.valueOf(str.nextToken()));
                    }
                }
            }
            fr.close();
            rd.close();
        }catch(Exception e){
        }
    }

}