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

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 *
 */
public class MulticastReceiver {

    public static final String ip = "228.1.2.3";
    public static final int port = 45678;

    public static void main(String[] args) throws Exception {

        MulticastSocket msocket = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName(ip);
        msocket.joinGroup(group);

        byte[] inbuf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(inbuf, inbuf.length);

        // Wait for packet
        msocket.receive(packet);

        // Data is now in inbuf
        int numBytesReceived = packet.getLength();
        System.out.println("Recd: " + new String(inbuf, 0, numBytesReceived));
    }
}
