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
import io.netty.channel.socket.nio.NioDatagramChannel;

public final class Sequencer extends SimpleChannelInboundHandler<DatagramPacket>  {

    String multicastAddress;
    int multicastPort;

    public static void main(String[] args){
        if(args.length < 1) {
            System.out.println("Use: java Sequencer");
            System.exit(-1);
        }
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioDatagramChannel.class);

            int myPort = 4096;

            ChannelFuture f = b.bind(myPort).sync();

        } catch (InterruptedException ex) {
        } finally {
            group.shutdownGracefully();
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(packet.content()), SocketUtils.socketAddress(multicastAddress, multicastPort)));
    }

}
