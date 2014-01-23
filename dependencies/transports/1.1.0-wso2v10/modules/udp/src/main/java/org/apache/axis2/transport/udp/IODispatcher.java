/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.axis2.transport.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import org.apache.axis2.transport.base.datagram.DatagramDispatcher;
import org.apache.axis2.transport.base.datagram.DatagramDispatcherCallback;
import org.apache.axis2.transport.base.datagram.ProcessPacketTask;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * I/O dispatcher for incoming UDP packets.
 * This class is responsible for receiving UDP packets and dispatch
 * the processing of these packets to worker threads.
 * It uses a {@link Selector} to receive packets from multiple endpoints
 * and a {@link WorkerPool} to dispatch the processing tasks.
 * <p>
 * The dispatcher uses the following thread model:
 * Incoming packets for all the registered endpoints are received
 * in the thread that executes the {@link #run()} method. For every
 * packet received, a {@link ProcessPacketTask} instance is created
 * and dispatched to a worker thread from the configured pool.
 * <p>
 * The methods {@link #addEndpoint(Endpoint)}, {@link #removeEndpoint(Endpoint)}
 * and {@link #stop()} are thread safe and may be called from any thread.
 * However, to avoid concurrency issues, the operation on the underlying
 * {@link Selector} will always be executed by the thread executing the
 * {@link #run()} method. The three methods mentioned above will block until
 * the operation has completed.
 */
public class IODispatcher implements DatagramDispatcher<Endpoint>, Runnable {
    private static abstract class SelectorOperation {
        private final CountDownLatch done = new CountDownLatch(1);
        private IOException exception;
        
        public void waitForCompletion() throws IOException, InterruptedException {
            done.await();
            if (exception != null) {
                throw exception;
            }
        }
        
        public void execute(Selector selector) {
            try {
                doExecute(selector);
            } catch (IOException ex) {
                exception = ex;
            } catch (Throwable ex) {
                exception = new IOException("Unexpected exception");
                exception.initCause(ex);
            }
            done.countDown();
        }
        
        public abstract void doExecute(Selector selector) throws IOException;
    }
    
    private static final Log log = LogFactory.getLog(IODispatcher.class);
    
    private final DatagramDispatcherCallback callback;
    private final Selector selector;
    private final Queue<SelectorOperation> selectorOperationQueue =
            new ConcurrentLinkedQueue<SelectorOperation>();
    
    /**
     * Constructor.
     * 
     * @param callback
     * @throws IOException if the {@link Selector} instance could not be created
     */
    public IODispatcher(DatagramDispatcherCallback callback) throws IOException {
        this.callback = callback;
        selector = Selector.open();
    }
    
    /**
     * Add a new endpoint. This method creates a new socket listening on
     * the UDP port specified in the endpoint description and makes sure
     * that incoming packets are routed to the specified service.
     * 
     * @param endpoint the endpoint description
     * @throws IOException if the socket could not be created or
     *         registered with the selector
     */
    public void addEndpoint(final Endpoint endpoint) throws IOException {
        final DatagramChannel channel = DatagramChannel.open();
        channel.socket().bind(new InetSocketAddress(endpoint.getPort()));
        channel.configureBlocking(false);
        execute(new SelectorOperation() {
            @Override
            public void doExecute(Selector selector) throws IOException {
                channel.register(selector, SelectionKey.OP_READ, endpoint);
            }
        });
        log.info("UDP endpoint started on port : " + endpoint.getPort());
    }
    
    /**
     * Remove an endpoint. This causes the corresponding UDP socket to be
     * closed.
     * 
     * @param endpoint the endpoint description
     * @throws IOException if an error occurred when closing the socket
     */
    public void removeEndpoint(final Endpoint endpoint) throws IOException {
        execute(new SelectorOperation() {
            @Override
            public void doExecute(Selector selector) throws IOException {
                Iterator<SelectionKey> it = selector.keys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    Endpoint endpointForKey = (Endpoint)key.attachment();
                    if (endpoint == endpointForKey) {
                        key.cancel();
                        key.channel().close();
                        break;
                    }
                }
            }
        });
    }
    
    /**
     * Stop the dispatcher.
     * This method closes all sockets and causes the execution of the
     * {@link #run()} method to stop.
     * 
     * @throws IOException
     */
    public void stop() throws IOException {
        execute(new SelectorOperation() {
            @Override
            public void doExecute(Selector selector) throws IOException {
                IOException exception = null;
                for (SelectionKey key : selector.keys()) {
                    try {
                        key.channel().close();
                    } catch (IOException ex) {
                        if (exception == null) {
                            exception = ex;
                        }
                    }
                }
                try {
                    selector.close();
                } catch (IOException ex) {
                    if (exception == null) {
                        exception = ex;
                    }
                }
                if (exception != null) {
                    throw exception;
                }
            }
        });
    }
    
    /**
     * Run the I/O dispatcher.
     * This method contains the event loop that polls the selector, reads the incoming
     * packets and dispatches the work.
     * It only returns when {@link #stop()} is called.
     */
    public void run() {
        while (true) {
            try {
                selector.select();
            } catch (IOException ex) {
                log.error("Exception in select; I/O dispatcher will be shut down", ex);
                return;
            }
            // Execute pending selector operations
            while (true) {
                SelectorOperation request = selectorOperationQueue.poll();
                if (request == null) {
                    break;
                }
                request.execute(selector);
                if (!selector.isOpen()) {
                    return;
                }
            }
            for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext(); ) {
                SelectionKey key = it.next();
                it.remove();
                if (key.isValid() && key.isReadable()) {
                    receive((Endpoint)key.attachment(), (DatagramChannel)key.channel());
                }
            }
        }
    }
    
    private void execute(SelectorOperation operation) throws IOException {
        selectorOperationQueue.add(operation);
        selector.wakeup();
        // Waiting for the execution of the selector operation will
        // never take a long time. It therefore makes no sense to
        // propagate InterruptedExceptions. If one is thrown, we
        // remember that and set the interruption status accordingly
        // afterwards.
        // See http://www.ibm.com/developerworks/java/library/j-jtp05236.html
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    operation.waitForCompletion();
                    return;
                } catch (InterruptedException ex) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    private void receive(Endpoint endpoint, DatagramChannel channel) {
        try {
            byte[] data = new byte[endpoint.getMaxPacketSize()];
            ByteBuffer buffer = ByteBuffer.wrap(data);
            InetSocketAddress address = (InetSocketAddress)channel.receive(buffer);
            int length = buffer.position();
            if (log.isDebugEnabled()) {
                log.debug("Received packet from " + address + " with length " + length);
            }
            callback.receive(endpoint, data, length, new UDPOutTransportInfo(address));
        } catch (IOException ex) {
            endpoint.getMetrics().incrementFaultsReceiving();
            log.error("Error receiving UDP packet", ex);
        }
    }
}
