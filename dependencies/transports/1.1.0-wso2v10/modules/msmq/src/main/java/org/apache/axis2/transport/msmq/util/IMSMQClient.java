package org.apache.axis2.transport.msmq.util;

import org.apache.axis2.AxisFault;

public interface IMSMQClient {

	public abstract void create(String queuName, String queueLabel, boolean transactional) throws MessageQueueException;

	/**
	 * JNI invocation retrieves message defined as in periodic manner the listener
	 * will client object will
	 * invoke the message receiver invocation
	 * 
	 * @param timeOut
	 * @return
	 * @throws AxisFault
	 */
	public abstract Message receive(int timeOut) throws AxisFault;

	/**
	 * JNI invocation to close existing MSMQ queue gracefully
	 * 
	 * @throws AxisFault
	 */
	public abstract void close() throws AxisFault;

	//TODO: at the moment we only support text messages.This need to be extended such that it support binary messages
	/**
	 * Send message to the MSMQ the JNI invocation interface definition
	 * 
	 * @param queueLabel
	 * @param transactional
	 * @param correlationID
	 * @param message
	 * @throws AxisFault
	 */
	public abstract void send(Message message) throws AxisFault;

	/**
	 * Open MSMQ for performing message queuing operation 
	 * 
	 * @param queueName
	 * @throws AxisFault
	 */
	public abstract void open(String queueName, IMSMQClient.Access access) throws AxisFault;
	
	
	public enum Access
    {
        /**
         * The queue will be accessible for Receive or READ (or GET)
         * operations.
         *
         **/
        RECEIVE(1),

        /**
         * The queue will be accessible for Send or WRITE (or PUT)
         * operations.
         *
         **/
            SEND(2),

        /**
         * The queue will be accessible for both Send ad Receive
         * operations.
         *
         **/
            SEND_AND_RECEIVE(3);

        int _accessFlag;

        Access(int value)
        {
            _accessFlag = value;
        }

        int getValue()   { return _accessFlag; }
    }

}