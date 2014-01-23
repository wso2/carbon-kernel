/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.axis2.clustering.tribes;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.catalina.tribes.ByteMessage;
import org.apache.catalina.tribes.ChannelListener;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.RemoteProcessException;
import org.apache.catalina.tribes.UniqueId;
import org.apache.catalina.tribes.group.GroupChannel;
import org.apache.catalina.tribes.group.RpcChannel;
import org.apache.catalina.tribes.group.RpcMessage;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.catalina.tribes.util.Logs;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;

/**
 * Represents a Tribes GroupChannel. The difference between
 * org.apache.catalina.tribes.group.GroupChannel & this class is that the proper classloaders
 * are set before message deserialization
 */
public class Axis2GroupChannel extends GroupChannel{

    private static final Log log = LogFactory.getLog(Axis2GroupChannel.class);
    @Override
    public void messageReceived(ChannelMessage msg) {
        if ( msg == null ) return;
        try {
            if ( Logs.MESSAGES.isTraceEnabled() ) {
                Logs.MESSAGES.trace("GroupChannel - Received msg:" + new UniqueId(msg.getUniqueId()) +
                                    " at " +new java.sql.Timestamp(System.currentTimeMillis())+
                                    " from "+msg.getAddress().getName());
            }

            Serializable fwd;
            if ( (msg.getOptions() & SEND_OPTIONS_BYTE_MESSAGE) == SEND_OPTIONS_BYTE_MESSAGE ) {
                fwd = new ByteMessage(msg.getMessage().getBytes());
            } else {
                try {
                    fwd = XByteBuffer.deserialize(msg.getMessage().getBytesDirect(), 0,
                                                  msg.getMessage().getLength(),
                                                  ClassLoaderUtil.getClassLoaders());
                }catch (Exception sx) {
                    log.error("Unable to deserialize message:"+msg,sx);
                    return;
                }
            }
            if ( Logs.MESSAGES.isTraceEnabled() ) {
                Logs.MESSAGES.trace("GroupChannel - Receive Message:" + new UniqueId(msg.getUniqueId()) + " is " +fwd);
            }

            //get the actual member with the correct alive time
            Member source = msg.getAddress();
            boolean rx = false;
            boolean delivered = false;
            for (Object channelListener1 : channelListeners) {
                ChannelListener channelListener = (ChannelListener) channelListener1;
                if (channelListener != null && channelListener.accept(fwd, source)) {
                    channelListener.messageReceived(fwd, source);
                    delivered = true;
                    //if the message was accepted by an RPC channel, that channel
                    //is responsible for returning the reply, otherwise we send an absence reply
                    if (channelListener instanceof RpcChannel) rx = true;
                }
            }//for
            if ((!rx) && (fwd instanceof RpcMessage)) {
                //if we have a message that requires a response,
                //but none was given, send back an immediate one
                sendNoRpcChannelReply((RpcMessage)fwd,source);
            }
            if ( Logs.MESSAGES.isTraceEnabled() ) {
                Logs.MESSAGES.trace("GroupChannel delivered["+delivered+"] id:"+new UniqueId(msg.getUniqueId()));
            }

        } catch ( Exception x ) {
            //this could be the channel listener throwing an exception, we should log it
            //as a warning.
            if ( log.isWarnEnabled() ) log.warn("Error receiving message:",x);
            throw new RemoteProcessException("Exception:"+x.getMessage(),x);
        }
    }
}
