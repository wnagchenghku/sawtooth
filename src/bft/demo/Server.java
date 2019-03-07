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
package bft.demo;

import bft.tom.ServiceReplica;
import java.io.IOException;

public final class Server {

    public Server(int id) {
        new ServiceReplica(id);
    }

    public static void main(String[] args){
        if(args.length < 1) {
            System.out.println("Use: java Server <processId>");
            System.exit(-1);
        }
        new Server(Integer.parseInt(args[0]));
    }
}
