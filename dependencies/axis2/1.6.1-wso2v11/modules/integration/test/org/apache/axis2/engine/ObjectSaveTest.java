/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.engine;

import junit.framework.TestCase;
import org.apache.axis2.util.MetaDataEntry;
import org.apache.axis2.util.ObjectStateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class ObjectSaveTest extends TestCase {
    protected static final Log log = LogFactory.getLog(ObjectSaveTest.class);
    
    private String testArg = null;

	// simple constructor needed for nested class Externalizable interface
	public ObjectSaveTest() {
	}

	public ObjectSaveTest(String arg0) {
		super(arg0);
		testArg = new String(arg0);
	}

	protected void setUp() throws Exception {
		// org.apache.log4j.BasicConfigurator.configure();
	}

	public void testObjectSerializable() throws Exception {
		File theFile = null;
		String theFilename = null;
		boolean saved = false;
		boolean restored = false;
		boolean done = false;

        log.debug("ObjectSaveTest:testObjectSerializable():  BEGIN ---------------");

		// ---------------------------------------------------------
		// setup an object to use
		// ---------------------------------------------------------
		MetaDataEntry obj = new MetaDataEntry("object_1", "object_1");

		// ---------------------------------------------------------
		// setup a temporary file to use
		// ---------------------------------------------------------
		try {
			theFile = File.createTempFile("objectTest", null);
			theFilename = theFile.getName();
            log.debug("ObjectSaveTest:testObjectSerializable(): temp file = ["
                    + theFilename + "]");
		} catch (Exception ex) {
            log.debug("ObjectSaveTest:testObjectSerializable(): error creating temp file = ["
                    + ex.getMessage() + "]");
			theFile = null;
		}

		if (theFile != null) {
			// ---------------------------------------------------------
			// save to the temporary file
			// ---------------------------------------------------------
			try {
				// setup an output stream to a physical file
				FileOutputStream outStream = new FileOutputStream(theFile);

				// attach a stream capable of writing objects to the
				// stream connected to the file
				ObjectOutputStream outObjStream = new ObjectOutputStream(
						outStream);

				// try to save
                log.debug("ObjectSaveTest:testObjectSerializable(): saving .....");
				saved = false;
				ObjectStateUtils.writeObject(outObjStream, obj,
						"testObject:Serializable");

				// close out the streams
				outObjStream.flush();
				outObjStream.close();
				outStream.flush();
				outStream.close();

				saved = true;
                log.debug(
                        "ObjectSaveTest:testObjectSerializable(): ....save operation completed.....");

				long filesize = theFile.length();
                log.debug("ObjectSaveTest:testObjectSerializable(): file size after save ["
                        + filesize
                        + "]   temp file = ["
                        + theFilename
                        + "]");
			} catch (Exception ex2) {
                log.debug("ObjectSaveTest:testObjectSerializable(): error during save ["
                        + ex2.getClass().getName()
                        + " : "
                        + ex2.getMessage() + "]");
				ex2.printStackTrace();
			}

			assertTrue(saved);

			// ---------------------------------------------------------
			// restore from the temporary file
			// ---------------------------------------------------------
			try {
				// setup an input stream to the file
				FileInputStream inStream = new FileInputStream(theFile);

				// attach a stream capable of reading objects from the
				// stream connected to the file
				ObjectInputStream inObjStream = new ObjectInputStream(inStream);

				// try to restore the options
                log.debug("ObjectSaveTest:testObjectSerializable(): restoring .....");
				restored = false;
				MetaDataEntry restored_obj = (MetaDataEntry) ObjectStateUtils
						.readObject(inObjStream, "testObject:serializable");
				inObjStream.close();
				inStream.close();

				restored = true;
                log.debug(
                        "ObjectSaveTest:testObjectSerializable(): ....restored operation completed.....");

			} catch (Exception ex2) {
                log.debug("ObjectSaveTest:testObjectSerializable(): error during restore ["
                        + ex2.getClass().getName()
                        + " : "
                        + ex2.getMessage() + "]");
				ex2.printStackTrace();
			}

			assertTrue(restored);

			// if the save/restore of the object succeeded,
			// then don't keep the temporary file around
			boolean removeTmpFile = saved && restored;
			if (removeTmpFile) {
				try {
					theFile.delete();
				} catch (Exception e) {
					// just absorb it
				}
			}

			// indicate that the temp file was created ok
			done = true;
		}

		// this is false when there are problems with the temporary file
		assertTrue(done);

        log.debug("ObjectSaveTest:testObjectSerializable():  END ---------------");
	}

	public void testObjectNotSerializable() throws Exception {
		File theFile = null;
		String theFilename = null;
		boolean saved = false;
		boolean restored = false;
		boolean expected_exception = false;
		boolean done = false;

        log.debug("ObjectSaveTest:testObjectNotSerializable():  BEGIN ---------------");

		// ---------------------------------------------------------
		// setup an object to use
		// ---------------------------------------------------------
		NotSerializableObject obj = new NotSerializableObject("nso_1");

		// ---------------------------------------------------------
		// setup a temporary file to use
		// ---------------------------------------------------------
		try {
			theFile = File.createTempFile("objectTest", null);
			theFilename = theFile.getName();
            log.debug("ObjectSaveTest:testObjectNotSerializable(): temp file = ["
                    + theFilename + "]");
		} catch (Exception ex) {
            log.debug("ObjectSaveTest:testObjectNotSerializable(): error creating temp file = ["
                    + ex.getMessage() + "]");
			theFile = null;
		}

		if (theFile != null) {
			// ---------------------------------------------------------
			// save to the temporary file
			// ---------------------------------------------------------
			FileOutputStream outStream = null;
			ObjectOutputStream outObjStream = null;
			try {
				// setup an output stream to a physical file
				outStream = new FileOutputStream(theFile);

				// attach a stream capable of writing objects to the
				// stream connected to the file
				outObjStream = new ObjectOutputStream(outStream);

				// try to save
                log.debug("ObjectSaveTest:testObjectNotSerializable(): saving .....");
				saved = false;
				ObjectStateUtils.writeObject(outObjStream, obj,
						"testObject:NotSerializable");

				saved = true;
                log.debug(
                        "ObjectSaveTest:testObjectNotSerializable(): ....save operation completed.....");

				long filesize = theFile.length();
                log.debug("ObjectSaveTest:testObjectNotSerializable(): file size after save ["
                        + filesize
                        + "]   temp file = ["
                        + theFilename
                        + "]");
			} catch (Exception ex2) {
				// expect an error here
				// ObjectStateUtils catches the NotSerializableException and
				// logs it
				if (ex2 instanceof NotSerializableException) {
					expected_exception = true;
				} else {
                    log.debug("ObjectSaveTest:testObjectNotSerializable():  save ["
                            + ex2.getClass().getName()
                            + " : "
                            + ex2.getMessage() + "]");
				}
			}
			// close out the streams
			if (outObjStream != null)
				outObjStream.close();
			if (outStream != null)
				outStream.close();

			// ---------------------------------------------------------
			// restore from the temporary file
			// ---------------------------------------------------------
			try {
				// setup an input stream to the file
				FileInputStream inStream = new FileInputStream(theFile);

				// attach a stream capable of reading objects from the
				// stream connected to the file
				ObjectInputStream inObjStream = new ObjectInputStream(inStream);

				// try to restore the options
                log.debug("ObjectSaveTest:testObjectSerializable(): restoring .....");
				restored = false;
				Object restored_obj = ObjectStateUtils.readObject(inObjStream,
						"testObject:NotSerializable");
				inObjStream.close();
				inStream.close();

				restored = true;
                log.debug(
                        "ObjectSaveTest:testObjectNotSerializable(): ....restored operation completed.....");

			} catch (Exception ex) {
                log.debug("ObjectSaveTest:testObjectNotSerializable(): error during restore ["
                        + ex.getClass().getName()
                        + " : "
                        + ex.getMessage() + "]");
				ex.printStackTrace();
			}

			assertTrue(restored);

			// if the save/restore of the object succeeded,
			// then don't keep the temporary file around
			boolean removeTmpFile = saved && restored;
			if (removeTmpFile) {
				try {
					theFile.delete();
				} catch (Exception e) {
					// just absorb it
				}
			}

			assertTrue(expected_exception);
		}

        log.debug("ObjectSaveTest:testObjectNotSerializable():  END ---------------");
	}

	public void testArrayList() throws Exception {
		File theFile = null;
		String theFilename = null;
		boolean saved = false;
		boolean restored = false;
		boolean done = false;
		boolean comparesOK = false;

        log.debug("ObjectSaveTest:testArrayList():  BEGIN ---------------");

		// ---------------------------------------------------------
		// setup the object to use
		// ---------------------------------------------------------
		ArrayList obj = new ArrayList();
		obj.add(new Integer(1));
		obj.add(new Integer(2));
		obj.add(new Integer(3));
		obj.add(new String("string1"));
		obj.add(new String("string2"));
		obj.add(System.out);
		obj.add(new Integer(4));
		obj.add(new Integer(5));
		obj.add(new Integer(6));

		int initial_size = obj.size();

		// ---------------------------------------------------------
		// setup a temporary file to use
		// ---------------------------------------------------------
		try {
			theFile = File.createTempFile("arraylistTest", null);
			theFilename = theFile.getName();
            log.debug("ObjectSaveTest:testArrayList(): temp file = ["
                    + theFilename + "]");
		} catch (Exception ex) {
            log.debug("ObjectSaveTest:testArrayList(): error creating temp file = ["
                    + ex.getMessage() + "]");
			theFile = null;
		}

		if (theFile != null) {
			// ---------------------------------------------------------
			// save to the temporary file
			// ---------------------------------------------------------
			try {
				// setup an output stream to a physical file
				FileOutputStream outStream = new FileOutputStream(theFile);

				// attach a stream capable of writing objects to the
				// stream connected to the file
				ObjectOutputStream outObjStream = new ObjectOutputStream(
						outStream);

				// try to save
                log.debug("ObjectSaveTest:testArrayList(): saving .....");
				saved = false;
				ObjectStateUtils.writeArrayList(outObjStream, obj,
						"testObject:ArrayList");

				// close out the streams
				outObjStream.flush();
				outObjStream.close();
				outStream.flush();
				outStream.close();

				saved = true;
                log.debug("ObjectSaveTest:testArrayList(): ....save operation completed.....");

				long filesize = theFile.length();
                log.debug("ObjectSaveTest:testArrayList(): file size after save ["
                        + filesize
                        + "]   temp file = ["
                        + theFilename
                        + "]");
			} catch (Exception ex2) {
                log.debug("ObjectSaveTest:testArrayList(): error during save ["
                        + ex2.getClass().getName()
                        + " : "
                        + ex2.getMessage() + "]");
				ex2.printStackTrace();
			}

			assertTrue(saved);

			// ---------------------------------------------------------
			// restore from the temporary file
			// ---------------------------------------------------------
			ArrayList restored_obj = null;

			try {
				// setup an input stream to the file
				FileInputStream inStream = new FileInputStream(theFile);

				// attach a stream capable of reading objects from the
				// stream connected to the file
				ObjectInputStream inObjStream = new ObjectInputStream(inStream);

				// try to restore the options
                log.debug("ObjectSaveTest:testArrayList(): restoring .....");
				restored = false;
				restored_obj = ObjectStateUtils.readArrayList(inObjStream,
						"testObject:ArrayList");
				inObjStream.close();
				inStream.close();

				restored = true;
                log.debug("ObjectSaveTest:testArrayList(): ....restored operation completed.....");

			} catch (Exception ex2) {
                log.debug("ObjectSaveTest:testArrayList(): error during restore ["
                        + ex2.getClass().getName()
                        + " : "
                        + ex2.getMessage() + "]");
				ex2.printStackTrace();
			}

			// if the save/restore of the object succeeded,
			// then don't keep the temporary file around
			boolean removeTmpFile = saved && restored;
			if (removeTmpFile) {
				try {
					theFile.delete();
				} catch (Exception e) {
					// just absorb it
				}
			}

			assertTrue(restored);

			if (restored_obj != null) {
				int restored_size = restored_obj.size();
				if (restored_size == (initial_size - 1)) {
					comparesOK = true;
				}
			}

			// TODO: check for exact entries

			assertTrue(comparesOK);

			// indicate that the temp file was created ok
			done = true;
		}

		// this is false when there are problems with the temporary file
		assertTrue(done);

        log.debug("ObjectSaveTest:testArrayList():  END ---------------");
	}

	public void testHashMap() throws Exception {
		File theFile = null;
		String theFilename = null;
		boolean saved = false;
		boolean restored = false;
		boolean done = false;
		boolean comparesOK = false;

        log.debug("ObjectSaveTest:testHashMap():  BEGIN ---------------");

		// ---------------------------------------------------------
		// setup the object to use
		// ---------------------------------------------------------
		HashMap obj = new HashMap();
		obj.put(new String("key1"), new Integer(1));
		obj.put(new String("key2"), new Integer(2));
		obj.put(new String("key3"), new String("value1"));
		obj.put(new String("key4"), System.out);
		obj.put(new String("key5"), new Integer(3));
		obj.put(new String("key6"), new Integer(4));
		obj.put(new String("key7"), System.err);
		obj.put(new String("key8"), new Integer(5));
		obj.put(new String("key9"), new Integer(6));
		obj.put(new NotSerializableObject("TestForHashMapKey"), new Integer(7));
		obj.put(new String("key10"), new Integer(8));

		int initial_size = obj.size();

		// ---------------------------------------------------------
		// setup a temporary file to use
		// ---------------------------------------------------------
		try {
			theFile = File.createTempFile("hashmapTest", null);
			theFilename = theFile.getName();
            log.debug("ObjectSaveTest:testHashMap(): temp file = ["
                    + theFilename + "]");
		} catch (Exception ex) {
            log.debug("ObjectSaveTest:testHashMap(): error creating temp file = ["
                    + ex.getMessage() + "]");
			theFile = null;
		}

		if (theFile != null) {
			// ---------------------------------------------------------
			// save to the temporary file
			// ---------------------------------------------------------
			try {
				// setup an output stream to a physical file
				FileOutputStream outStream = new FileOutputStream(theFile);

				// attach a stream capable of writing objects to the
				// stream connected to the file
				ObjectOutputStream outObjStream = new ObjectOutputStream(
						outStream);

				// try to save
                log.debug("ObjectSaveTest:testHashMap(): saving .....");
				saved = false;
				ObjectStateUtils.writeHashMap(outObjStream, obj,
						"testObject:HashMap");

				// close out the streams
				outObjStream.flush();
				outObjStream.close();
				outStream.flush();
				outStream.close();

				saved = true;
                log.debug("ObjectSaveTest:testHashMap(): ....save operation completed.....");

				long filesize = theFile.length();
                log.debug("ObjectSaveTest:testHashMap(): file size after save ["
                        + filesize
                        + "]   temp file = ["
                        + theFilename
                        + "]");
			} catch (Exception ex2) {
                log.debug("ObjectSaveTest:testHashMap(): error during save ["
                        + ex2.getClass().getName()
                        + " : "
                        + ex2.getMessage() + "]");
				ex2.printStackTrace();
			}

			assertTrue(saved);

			// ---------------------------------------------------------
			// restore from the temporary file
			// ---------------------------------------------------------
			HashMap restored_obj = null;

			try {
				// setup an input stream to the file
				FileInputStream inStream = new FileInputStream(theFile);

				// attach a stream capable of reading objects from the
				// stream connected to the file
				ObjectInputStream inObjStream = new ObjectInputStream(inStream);

				// try to restore the options
                log.debug("ObjectSaveTest:testHashMap(): restoring .....");
				restored = false;
				restored_obj = ObjectStateUtils.readHashMap(inObjStream,
						"testObject:HashMap");
				inObjStream.close();
				inStream.close();

				restored = true;
                log.debug("ObjectSaveTest:testHashMap(): ....restored operation completed.....");

			} catch (Exception ex2) {
                log.debug("ObjectSaveTest:testHashMap(): error during restore ["
                        + ex2.getClass().getName()
                        + " : "
                        + ex2.getMessage() + "]");
				ex2.printStackTrace();
			}

			// if the save/restore of the object succeeded,
			// then don't keep the temporary file around
			boolean removeTmpFile = saved && restored;
			if (removeTmpFile) {
				try {
					theFile.delete();
				} catch (Exception e) {
					// just absorb it
				}
			}

			assertTrue(restored);

			if (restored_obj != null) {
				int restored_size = restored_obj.size();
				if (restored_size == (initial_size - 3)) {
					// there are entries in the map that are not serializable
					comparesOK = true;
				}
			}

			// TODO: check for exact entries

			assertTrue(comparesOK);

			// indicate that the temp file was created ok
			done = true;
		}

		// this is false when there are problems with the temporary file
		assertTrue(done);

        log.debug("ObjectSaveTest:testHashMap():  END ---------------");
	}

	public void testLinkedList() throws Exception {
		File theFile = null;
		String theFilename = null;
		boolean saved = false;
		boolean restored = false;
		boolean done = false;
		boolean comparesOK = false;

        log.debug("ObjectSaveTest:testLinkedList():  BEGIN ---------------");

		// ---------------------------------------------------------
		// setup the object to use
		// ---------------------------------------------------------
		LinkedList obj = new LinkedList();
		obj.add(new Integer(1));
		obj.add(new Integer(2));
		obj.add(new Integer(3));
		obj.add(new String("string1"));
		obj.add(new String("string2"));
		obj.add(System.in);
		obj.add(new Integer(4));
		obj.add(new Integer(5));
		obj.add(new Integer(6));

		int initial_size = obj.size();

		// ---------------------------------------------------------
		// setup a temporary file to use
		// ---------------------------------------------------------
		try {
			theFile = File.createTempFile("linkedlistTest", null);
			theFilename = theFile.getName();
            log.debug("ObjectSaveTest:testLinkedList(): temp file = ["
                    + theFilename + "]");
		} catch (Exception ex) {
            log.debug("ObjectSaveTest:testLinkedList(): error creating temp file = ["
                    + ex.getMessage() + "]");
			theFile = null;
		}

		if (theFile != null) {
			// ---------------------------------------------------------
			// save to the temporary file
			// ---------------------------------------------------------
			try {
				// setup an output stream to a physical file
				FileOutputStream outStream = new FileOutputStream(theFile);

				// attach a stream capable of writing objects to the
				// stream connected to the file
				ObjectOutputStream outObjStream = new ObjectOutputStream(
						outStream);

				// try to save
                log.debug("ObjectSaveTest:testLinkedList(): saving .....");
				saved = false;
				ObjectStateUtils.writeLinkedList(outObjStream, obj,
						"testObject:LinkedList");

				// close out the streams
				outObjStream.flush();
				outObjStream.close();
				outStream.flush();
				outStream.close();

				saved = true;
                log.debug("ObjectSaveTest:testLinkedList(): ....save operation completed.....");

				long filesize = theFile.length();
                log.debug("ObjectSaveTest:testLinkedList(): file size after save ["
                        + filesize
                        + "]   temp file = ["
                        + theFilename
                        + "]");
			} catch (Exception ex2) {
                log.debug("ObjectSaveTest:testLinkedList(): error during save ["
                        + ex2.getClass().getName()
                        + " : "
                        + ex2.getMessage() + "]");
				ex2.printStackTrace();
			}

			assertTrue(saved);

			// ---------------------------------------------------------
			// restore from the temporary file
			// ---------------------------------------------------------
			LinkedList restored_obj = null;

			try {
				// setup an input stream to the file
				FileInputStream inStream = new FileInputStream(theFile);

				// attach a stream capable of reading objects from the
				// stream connected to the file
				ObjectInputStream inObjStream = new ObjectInputStream(inStream);

				// try to restore the options
                log.debug("ObjectSaveTest:testLinkedList(): restoring .....");
				restored = false;
				restored_obj = ObjectStateUtils.readLinkedList(inObjStream,
						"testObject:LinkedList");
				inObjStream.close();
				inStream.close();

				restored = true;
                log.debug("ObjectSaveTest:testLinkedList(): ....restored operation completed.....");

			} catch (Exception ex2) {
                log.debug("ObjectSaveTest:testLinkedList(): error during restore ["
                        + ex2.getClass().getName()
                        + " : "
                        + ex2.getMessage() + "]");
				ex2.printStackTrace();
			}

			// if the save/restore of the object succeeded,
			// then don't keep the temporary file around
			boolean removeTmpFile = saved && restored;
			if (removeTmpFile) {
				try {
					theFile.delete();
				} catch (Exception e) {
					// just absorb it
				}
			}

			assertTrue(restored);

			if (restored_obj != null) {
				int restored_size = restored_obj.size();
				if (restored_size == (initial_size - 1)) {
					comparesOK = true;
				}
			}

			// TODO: check for exact entries

			assertTrue(comparesOK);

			// indicate that the temp file was created ok
			done = true;
		}

		// this is false when there are problems with the temporary file
		assertTrue(done);

        log.debug("ObjectSaveTest:testLinkedList():  END ---------------");
	}

	public class NotSerializableObject implements Externalizable {
		private String label = "TestObject";

		private String ID = null;

		// make sure we have some objects that don't serialize
		private PrintStream ps = System.out;

		// default constructor needed for Externalizable interface
		public NotSerializableObject() {
		}

		public NotSerializableObject(String identifier) {
			ID = identifier;
			ps = System.out;
		}

		public void setID(String s) {
			ID = s;
		}

		public String getID() {
			return ID;
		}

		public void writeExternal(java.io.ObjectOutput out) throws IOException {
			throw new NotSerializableException(
					"Test Object is not serializable");
		}

		public void readExternal(java.io.ObjectInput in) throws IOException,
				ClassNotFoundException {
			throw new IOException("Test object is not serializable");
		}

	}

}
