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
package bft.reconfiguration.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class Configuration {
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());

	protected int processId;
	protected Map<String, String> configs;
	protected HostsConfig hosts;
    protected int multicastPort;

    protected String configHome = "";

   
    protected static String hostsFileName = "";

    public Configuration(int procId){
        processId = procId;
        init();
    }
    
    public Configuration(int procId, String configHomeParam){
        processId = procId;
        configHome = configHomeParam;
        init();
    }

    protected void init(){
        try{
            hosts = new HostsConfig(configHome, hostsFileName);
            
            loadConfig();

            String s = (String) configs.remove("system.multicast.port");
            if(s == null){
                multicastPort = 5000;
            }else{
                multicastPort = Integer.parseInt(s);
            }

        }catch(Exception e){
            LoggerFactory.getLogger(this.getClass()).error("Wrong system.config file format.");
        }
    }

    public final int getMulticastPort() {
        return multicastPort;
    }
    

    public final InetSocketAddress getRemoteAddress(int id){
        return hosts.getRemoteAddress(id);
    }

    public final String getHost(int id){
        return hosts.getHost(id);
    }

    public final int getPort(int id){
        return hosts.getPort(id);
    }
    
     public final int getServerToServerPort(int id){
        return hosts.getServerToServerPort(id);
    }

    public final int getProcessId(){
        return processId;
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

}