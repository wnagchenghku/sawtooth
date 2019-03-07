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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import bft.reconfiguration.ViewController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paulo Sousa
 */
public class NettyTOMMessageDecoder extends ByteToMessageDecoder {
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * number of measures used to calculate statistics
     */
    //private final int BENCHMARK_PERIOD = 10000;
    private boolean isClient;
    private Map sessionTable;
    //private Storage st;
    private ViewController controller;
    private boolean firstTime;
    private ReentrantReadWriteLock rl;
    //******* EDUARDO BEGIN: commented out some unused variables **************//
    //private long numReceivedMsgs = 0;
    //private long lastMeasurementStart = 0;
    //private long max=0;
    //private Storage st;
    //private int count = 0;
   
    //private Signature signatureEngine;
    
    
     //******* EDUARDO END **************//
    
    private boolean useMAC;
    
    public NettyTOMMessageDecoder(boolean isClient, Map sessionTable, ViewController controller, ReentrantReadWriteLock rl, boolean useMAC) {
        this.isClient = isClient;
        this.sessionTable = sessionTable;
        this.controller = controller;
        this.firstTime = true;
        this.rl = rl;
        this.useMAC = useMAC;
        logger.debug("new NettyTOMMessageDecoder!!, isClient=" + isClient);
    }

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf buffer, List<Object> list) throws Exception  {
        return;
    }
}
