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

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;
import java.io.InterruptedIOException;

public class Client {

    private static final int PORT = 7686;

	public static void main(String[] args) throws IOException {

        if ((args.length < 2) || (args.length > 3)) { // Test for correct # of args
            throw new IllegalArgumentException("Parameter(s): Parameter(s): <Server> <Data Size> [<Port>] ");
        }
        InetAddress serverAddress = InetAddress.getByName(args[0]);  // Server address

        int servPort = (args.length == 3) ? Integer.parseInt(args[2]) : PORT;

        byte[] bytesToSend = new byte[Integer.parseInt(args[1])];

        DatagramSocket socket = new DatagramSocket();

        DatagramPacket sendPacket = new DatagramPacket(bytesToSend, bytesToSend.length, serverAddress, servPort);

        for (; ; ) {
            socket.send(sendPacket);
        }
    }

}