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
package bftsmart.reconfiguration.util;

import java.util.StringTokenizer;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TOMConfiguration extends Configuration {
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    protected int n;
    protected int f;
    private String bindAddress;
    
    /** Creates a new instance of TOMConfiguration */
    public TOMConfiguration(int processId) {
        super(processId);
    }

    /** Creates a new instance of TOMConfiguration */
    public TOMConfiguration(int processId, String configHome) {
        super(processId, configHome);
    }


    @Override
    protected void init() {
        super.init();
        try {
            n = Integer.parseInt(configs.remove("system.servers.num").toString());
            String s = (String) configs.remove("system.servers.f");
            if (s == null) {
                f = (int) Math.ceil((n - 1) / 3);
            } else {
                f = Integer.parseInt(s);
            }

            s = (String) configs.remove("system.shutdownhook");
            shutdownHookEnabled = (s != null) ? Boolean.parseBoolean(s) : false;

            s = (String) configs.remove("system.communication.useMACs");
            if (s == null) {
                useMACs = 0;
            } else {
                useMACs = Integer.parseInt(s);
            }

            s = (String) configs.remove("system.communication.useSignatures");
            if (s == null) {
                useSignatures = 0;
            } else {
                useSignatures = Integer.parseInt(s);
            }

            s = (String) configs.remove("system.communication.inQueueSize");
            if (s == null) {
                inQueueSize = 1000;
            } else {

                inQueueSize = Integer.parseInt(s);
                if (inQueueSize < 1) {
                    inQueueSize = 1000;
                }

            }

            s = (String) configs.remove("system.bft");
            isBFT = (s != null) ? Boolean.parseBoolean(s) : true;

            s = (String) configs.remove("system.numrepliers");
            if (s == null) {
                numRepliers = 0;
            } else {
                numRepliers = Integer.parseInt(s);
            }
            
            s = (String) configs.remove("system.communication.bindaddress");
            
            Pattern pattern = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

            if (s == null || !pattern.matcher(s).matches()) {
                bindAddress = "";
            } else {
                bindAddress = s;
            }
            
            s = (String) configs.remove("system.samebatchsize");
            if (s != null) {
                    sameBatchSize = Boolean.parseBoolean(s);
            } else {
                    sameBatchSize = false;
            }
            
        } catch (Exception e) {
            logger.error("Could not parse system configuration file",e);
        }

    }

    public int getN() {
        return n;
    }

    public int getF() {
        return f;
    }

    public boolean isBFT(){
    	
    	return this.isBFT;
    }
    
    public String getBindAddress() {
        return bindAddress;
    }
}
