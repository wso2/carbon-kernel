package org.wso2.carbon.coordination.core;

import java.util.UUID;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.wso2.carbon.coordination.core.services.CoordinationService;
import org.wso2.carbon.coordination.core.services.impl.ZKCoordinationService;
import org.wso2.carbon.coordination.core.sync.Barrier;
import org.wso2.carbon.coordination.core.sync.Group;
import org.wso2.carbon.coordination.core.sync.GroupEventListener;
import org.wso2.carbon.coordination.core.sync.Lock;
import org.wso2.carbon.coordination.core.sync.Queue;

public class Tester implements Watcher, GroupEventListener {

	public Tester() throws Exception {
		CoordinationService service = new ZKCoordinationService("/home/laf/Desktop/coordination-client-config.xml");
		testGroupsArrive(service);
		service.close();
	}
	
	public void testLock(CoordinationService service) throws Exception {
		Lock lock = service.createLock("l1", -1);
		System.out.println("WAITING FOR LOCK");
		lock.acquire();
		System.out.println("LOCK ACQUIRED");
		Thread.sleep(5000);
		lock.release();
		System.out.println("LOCK RELEASED");
	}
	
	public void testGroupsArrive(CoordinationService service) throws Exception {
		Group g1 = service.createGroup("g1");
		g1.waitForMemberCount(5);
		System.out.println("All members arrived");
	}
	
	public void testGroupsCommRecv(CoordinationService service) throws Exception {
		Group g1 = service.createGroup("g1");
		g1.setGroupEventListener(this);
		Thread.sleep(10000000);
	}
	
	public void testGroupsCommSend(CoordinationService service) throws Exception {
		Group g1 = service.createGroup("g1");
		g1.setGroupEventListener(this);
		String myId = g1.getMemberId();
		System.out.println("My ID:" + myId);
		for (String mid : g1.getMemberIds()) {
			if (!mid.equals(myId)) {
				byte[] data = g1.sendReceive(mid, "Hello".getBytes());
				int sum = 0;
				for (int i = 0; i < data.length; i++) {
					sum += data[i];
				}
				System.out.println("RESPONSE RECEIVED:" + sum);
			}
		}
		System.out.println("END X");
	}
	
	public void testGroups1(CoordinationService service) throws Exception {
		Group g1 = service.createGroup("g1");
		g1.setGroupEventListener(this);
		String mid = g1.getMemberId();
		String lid = g1.getLeaderId();
		System.out.println("MID: " + mid);
		if (lid.equals(mid)) {
			System.out.println("I'm Leader!");
		}
		g1.broadcast(new String(UUID.randomUUID() + "").getBytes());
	}
	
	public void testGroups2(CoordinationService service) throws Exception {
		Group g1 = service.createGroup("g1");
		for (int i = 0; i < 100; i++) {
		    g1.broadcast(new String(UUID.randomUUID() + "").getBytes());
		}
	}
	
	public void testEQ(CoordinationService service) throws Exception {
		Queue q1 = service.createQueue("q1", -1);
		String val = new String("" + Math.random());
		System.out.println("ENQUEUED:" + val);
		q1.enqueue(val.getBytes());
	}
	
	public void testPEQ(CoordinationService service, int priority) throws Exception {
		Queue q1 = service.createQueue("q1", -1);
		String val = new String("" + Math.random());
		System.out.println("P ENQUEUED:" + val);
		q1.enqueue(val.getBytes(), priority);
	}
	
	public void testDQ(CoordinationService service) throws Exception {
		Queue q1 = service.createQueue("q1", -1);
		byte[] value = q1.dequeue();
		if (value == null) {
			System.out.println("EMPTY");
		} else {
		    System.out.println("DEQUED:" + new String(value));
		}
	}
	
	public void testBDQ(CoordinationService service) throws Exception {
		Queue q1 = service.createQueue("q1", 5000);
		byte[] value = q1.blockingDequeue();
		if (value == null) {
			System.out.println("EMPTY");
		} else {
		    System.out.println("DEQUED:" + new String(value));
		}
	}
	
	public void testB(CoordinationService service) throws Exception {
		Barrier b1 = service.createBarrier("b3", 4, 50000);
		System.out.println("B:" + b1);
		b1.enter();
		System.out.println("ENTERED");
		b1.leave();
		System.out.println("DONE");
		service.close();
	}

	@Override
	public void process(WatchedEvent e) {
		System.out.println("Event:" + e);
	}
	
	public static void main(String[] args) throws Exception {
		new Tester();
		System.exit(0);
	}

	@Override
	public void onLeaderChange(String newLeaderId) {
		System.out.println("LEADER CHANGED: " + newLeaderId);
	}

	@Override
	public void onGroupMessage(byte[] data) {
		System.out.println("GROUP MESSAGE RECEIVED: " + new String(data));
	}

	@Override
	public byte[] onPeerMessage(byte[] data) {
		System.out.println("Peer Message Request: " + new String(data));
		byte[] req = new byte[1024 * 1024 * 7];
		int sum = 0;
		for (int i = 0; i < req.length; i++) {
			req[i] = (byte) (Math.random() * 50);
			sum += req[i];
		}
		System.out.println("RESPONSE SENT:" + sum);
		return req;
	}

	@Override
	public void onMemberArrival(String newMemberId) {
		System.out.println("Member Arrived: " + newMemberId);
	}

	@Override
	public void onMemberDeparture(String oldMemberId) {
		System.out.println("Member Departed: " + oldMemberId);
	}
	
}
