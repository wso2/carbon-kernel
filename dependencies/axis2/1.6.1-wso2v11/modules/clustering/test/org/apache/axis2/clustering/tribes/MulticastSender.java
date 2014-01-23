/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.apache.axis2.clustering.tribes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 *
 */
public class MulticastSender {
    public static final String ip = "228.1.2.3";
    public static final int port = 45678;

    public static void main(String[] args) throws Exception {

        // Multicast send
        byte[] outbuf = "Hello world".getBytes();


        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            InetAddress groupAddr = InetAddress.getByName(ip);
            DatagramPacket packet = new DatagramPacket(outbuf, outbuf.length, groupAddr, port);
            socket.send(packet);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
        System.out.println("Sent");
    }
}
