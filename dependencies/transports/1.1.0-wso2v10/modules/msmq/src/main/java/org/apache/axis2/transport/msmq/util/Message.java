package org.apache.axis2.transport.msmq.util;


/**
 * <p>The Message class models a message that is sent to or receive from an
 * MSMQ queue.  It exposes several properties that are known to MSMQ,
 * including the message label, the message correlationId, and the
 * message body.</p>
 *
 * <p>The maximum size for an MSMQ 4.0 message is slightly less than 4
 * MB. If you try to send a message that exceeds the maximum size, you
 * will receive a MessageQueueException, with hr =
 * MQ_ERROR_INSUFFICIENT_RESOURCES (0xC00E0027).</p>
 *
 */
public class Message {
    public static String _encoding = "UTF-16LE";
    public static String _utf8 = "UTF-8";
    byte[] _messageBody ;
    String _label ;
    byte[] _correlationId ; // up to PROPID_M_CORRELATIONID_SIZE bytes
    boolean _highPriority;


    /**
     * <p>Sets the message body, as a string.</p>
     *
     * <p>The string will be encoded as UTF-16LE, with no byte-order-mark.
     * This information may be useful if you use different libraries on the
     * the receiving and sending side. </p>
     *
     * @param  value    the string to use for the Message body
     * @see    #setBody(byte[])
     */
    public void setBodyAsString(String value)
        throws java.io.UnsupportedEncodingException
    { _messageBody= value.getBytes(_encoding); }


    /**
     * <p>Gets the message body, as a string.</p>
     *
     * <p>The string will be decoded as UTF-16LE, with no byte-order-mark.
     * This is mostly useful after receiving a message. </p>
     *
     * <p>If the message body is not a legal UTF-16LE bytestream, then this
     * method will return a rubbish string.</p>
     *
     * @return the message body, as a string.
     * @see    #getBody()
     */
    public String getBodyAsString()
        throws java.io.UnsupportedEncodingException
    { return new String(_messageBody, _encoding); }


    /**
     * <p>Sets the correlation Id on the message. </p>
     *
     * <p>MSMQ specifies that the ID should be a byte array, of 20 bytes
     * in length. But callers can use this convenience method to use a
     * string as a correlationId.  It will be encoded as UTF-8, and
     * limited to 20 bytes.</p>
     *
     * @param  value   the string to use as the correlation ID on the message.
     */

    public void setCorrelationIdAsString(String value)
        throws java.io.UnsupportedEncodingException
    { _correlationId= value.getBytes(_utf8); }


    /**
     * <p>Gets the correlation Id on the message, in the form of a string. </p>
     *
     * <p>The behavior is undefined if the correlation ID is not a
     * UTF-8 bytestream.</p>
     *
     * @return the correlation ID on the message, as a string.
     */
    public String getCorrelationIdAsString()
        throws java.io.UnsupportedEncodingException
    { return new String(_correlationId, _utf8); }


    /**
     * Sets the message body.
     *
     * @param  value    the byte array to use for the Message body
     * @see    #getBody()
     * @see    #setBodyAsString(String)
     */
    public void setBody(byte[] value)          { _messageBody= value; }

    /**
     * Gets the message body.
     *
     * @return the message body, as a byte array.
     */
    public byte[] getBody()                    { return _messageBody; }



    /**
     * Sets the message label.
     *
     * @param  value   the string to use as the label on the message.
     */
    public void setLabel(String value)         { _label= value; }

    /**
     * Gets the message body.
     *
     * @return the message label.
     */
    public String getLabel()                   { return _label; }



    /**
     * <p>Sets the correlation Id on the message. </p>
     *
     * <p> The ID should be a byte array, a maximum of 20 bytes.</p>
     *
     * @param  value  the byte array to use as the correlation ID on the
     *                message.
     */
    public void setCorrelationId(byte[] value) { _correlationId= value; }

    /**
     * <p>Gets the correlation Id on the message. </p>
     *
     * <p>The ID will be a byte array, of length 20.</p>
     *
     * @return the correlation ID on the message.
     */
    public byte[] getCorrelationId()           { return _correlationId; }


    /**
     * Sets whether the message should be trated as high priority or not.
     *
     * @param  value   true if the message should be delivered with high
     *                 priority.
     */
    public void setHighPriority(boolean value) { _highPriority= value; }


    /**
     * <p>Gets whether the message will be treated with high priority.</p>
     *
     * <p>This only makes sense for outgoing messages.</p>
     *
     * @return  true if the message will be trated with high priority.
     */
    public boolean getHighPriority()           { return _highPriority; }


    Message()    { }



    /**
     * <p>Creates a Message instance, using a string argument for the
     * contents of the body, and empty values for the label and
     * correlation ID.</p>
     *
     * @param  body    the string to use for the Message body
     */
    public Message(String body)
        throws java.io.UnsupportedEncodingException
    {
        this(body, "", "");
    }


    /**
     * <p>Creates a Message instance, using string arguments for the
     * contents.</p>
     *
     * <p>A Message contains byte array data in the body and correlation
     * Id. This constructor allows the specification of those items as
     * strings.  The actual values of the body and correlation Id are
     * set to the encoded form of the strings, using UTF-16LE for
     * encoding. </p>
     *
     * <p>If you use ASCII strings for the body and correlationId,
     * the encoding will be very inefficient. Each character in the input
     * will result in two bytes in output, one of which will be a zero.
     * Therefore, if you're concerned about efficiency, consider encoding
     * strings separately, before creating the Message instance. </p>
     *
     *
     * @param  body    the string to use for the Message body.
     * @param  label   the string to use for the Message label. The maximum
     *                 length of a message label is 250 bytes.
     * @param  correlationId    the string to use for the Message correlation Id
     */
    public Message(String body, String label, String correlationId)
        throws java.io.UnsupportedEncodingException
    {
        this(body.getBytes(Message._encoding),
             label,
             correlationId.getBytes(Message._encoding));
    }


    /**
     * <p>Creates a Message instance, using a string for the
     * body and label, and a byte array for the correlation ID.</p>
     *
     * <p>A Message contains byte array data in the body and correlation
     * Id. This constructor allows the specification of the body as a
     * string, and the correlation Id as a byte array.  The actual value
     * of the body will be set to the encoded form of the body string,
     * using UTF-16LE. </p>
     *
     * <p>Only the first 20 bytes of the correlationId will be used.</p>
     *
     * @param  body    the string to use for the Message body
     * @param  label   the string to use for the Message label
     * @param  correlationId    the byte array to use for the Message correlation Id
     *
     */
    public Message(String body, String label, byte[] correlationId)
        throws java.io.UnsupportedEncodingException
    {
        this(body.getBytes(_encoding),
             label,
             correlationId);
    }


    /**
     * <p>Creates a Message instance, using a byte array for the
     * message body, and empty values for the label and correlation ID.</p>
     *
     * <p>A Message contains byte array data in the body and
     * correlation Id. This constructor allows the specification of the
     * body as a byte array. The label and correlation ID of the
     * Message are set to empty values. </p>
     *
     * @param  body    the string to use for the Message body
     *
     */
    public Message(byte[] body)
        throws java.io.UnsupportedEncodingException
    {
        this(body, "", "");
    }



    /**
     * <p>Creates a Message instance, using a byte array for the
     * body, and a string for the label and correlation ID.</p>
     *
     * <p>A Message contains byte array data in the body and correlation
     * Id. This constructor allows the specification of the body as a
     * byte array, and the correlation Id as a string.  The actual value
     * of the correlation Id will be the encoded form of the body
     * string, using UTF-16LE. </p>
     *
     * <p>Only the first 20 bytes of the correlationId will be used.</p>
     *
     * @param  body    the string to use for the Message body
     * @param  label   the string to use for the Message label
     * @param  correlationId    the byte array to use for the Message correlation Id
     *
     */
    public Message(byte[] body, String label, String correlationId)
        throws java.io.UnsupportedEncodingException
    {
        this(body,
             label,
             correlationId.getBytes(_encoding));
    }



    /**
     * <p>Creates a Message instance, using a byte array for the
     * body the correlation ID, and a string for the label.</p>
     *
     * <p>A Message contains byte array data in the body and correlation
     * Id. This constructor allows the specification of the body and
     * correlation ID as byte arrays. Applications may wish to use this
     * constructor when close control of the contents of the message is
     * desired. </p>
     *
     * <p>Only the first 20 bytes of the correlationId will be used.</p>
     *
     * @param  body    the string to use for the Message body
     * @param  label   the string to use for the Message label
     * @param  correlationId    the byte array to use for the Message correlation Id
     *
     */
    public Message(byte[] body, String label, byte[] correlationId)
    {
        _messageBody = body;
        _label= label;
        _correlationId= correlationId;
    }

}
