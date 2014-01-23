package org.apache.axis2.transport.msmq.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.msmq.navtive_support.CtypeMapClazz;
import org.apache.camel.component.msmq.native_support.ByteArray;
import org.apache.camel.component.msmq.native_support.MsmqMessage;
import org.apache.camel.component.msmq.native_support.MsmqQueue;
import org.apache.camel.component.msmq.native_support.msmq_native_supportConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MSMQCamelClient implements IMSMQClient{
	private Log log = LogFactory.getLog(MSMQCamelClient.class);
	
	private MsmqQueue msmqQueue = null;
	
	public void create(String queuName, String queueLabel, boolean transactional) throws MessageQueueException {
		if (log.isDebugEnabled()) {  
			log.info("TODO Finalizing the message queue creatiion logic");
		}
    }

	public Message receive(int timeOut) throws AxisFault {
		try {
			Message message = new Message("");
			MsmqMessage msmqMessage = new MsmqMessage();
			int initsize = 1;
			boolean cont = true;
			ByteArray recvbuffer = new ByteArray(initsize);
			msmqMessage.setMsgBody(recvbuffer.cast());
			msmqMessage.setBodySize(initsize);

			while (cont) {
				try {
					msmqQueue.receiveMessage(msmqMessage, -1);
					cont = false;
				} catch (RuntimeException ex) {
					if (ex.getMessage().equals("Message body too big")) {
						initsize += 5;
						recvbuffer = new ByteArray(initsize);
						msmqMessage.setMsgBody(recvbuffer.cast());
						msmqMessage.setBodySize(initsize);
					} else {
						throw ex;
					}
				}
			}
			Long messageSize = Long.valueOf(msmqMessage.getBodySize());
			byte[] buffer = new byte[messageSize.intValue()];
			for (int i = 0; i < messageSize; ++i) {
				buffer[i] = recvbuffer.getitem(i);
			}
			message.setBody(new String(buffer).getBytes(Message._encoding));
			byte[] cid = new byte[msmq_native_supportConstants.PROPID_M_CORRELATIONID_SIZE];
		    message.setCorrelationId(cid);
		    message.setLabel(CtypeMapClazz.getCtypeNameById(msmqMessage.getAppSpecific()));
			if (log.isDebugEnabled()) {
				log.info("Message Body size" + msmqMessage.getBodySize());
			}
			return (msmqMessage.getBodySize() == 0 || msmqMessage.getBodySize() == 1) ? null : message;
		} catch (UnsupportedEncodingException e) {
			log.error("Error while Reading the message received via destination", e);
			throw new AxisFault("Error while Reading the message received via destination", e);
		}
	}

	public void close() throws AxisFault {
	    msmqQueue.close();
    }

	public void send(Message message) throws AxisFault {
		try{
		MsmqMessage msg = new MsmqMessage();
		String payload = message.getBodyAsString();
		ByteArray sendbuffer = new ByteArray(payload.length());
		msg.setMsgBody(sendbuffer.cast());
		msg.setBodySize(payload.length());
		for (int i = 0; i < payload.length(); ++i) {
			sendbuffer.setitem(i, payload.getBytes()[i]);
		}
		msg.setCorrelationId(message.getCorrelationId());
		msg.setAppSpecifc(CtypeMapClazz.getIdByName(message.getLabel()));
	    msmqQueue.sendMessage(msg);
		}catch (Exception e) {
			log.error("Error while sending message to destination", e);
	        throw new AxisFault("Error while sending message to destination", e);
		}
    }

	public void open(String queueName, IMSMQClient.Access access) throws AxisFault {
	    // TODO Auto-generated method stub
	    try{ 
		msmqQueue = new  MsmqQueue();
	     if(access.equals(IMSMQClient.Access.SEND)){
	        msmqQueue.open(queueName, msmq_native_supportConstants.MQ_SEND_ACCESS);
	     }else if(access.equals(IMSMQClient.Access.RECEIVE)){
	        msmqQueue.open(queueName, msmq_native_supportConstants.MQ_RECEIVE_ACCESS); 
	     }
	    }catch (Exception e) {
	    	log.error("Error while Opening the queue", e);
	        throw new AxisFault("Error while Opening the queue", e);
		}
    }

}
