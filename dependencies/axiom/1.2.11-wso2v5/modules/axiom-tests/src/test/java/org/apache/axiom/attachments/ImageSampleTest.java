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

package org.apache.axiom.attachments;

import org.apache.axiom.om.AbstractTestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.OMNamespaceImpl;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.impl.builder.MTOMStAXSOAPModelBuilder;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.xml.stream.XMLStreamReader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Iterator;

public class ImageSampleTest extends AbstractTestCase {

    public ImageSampleTest(String testName) {
        super(testName);
    }

    /*
     * @see TestCase#setUp()
     */
    Image expectedImage;

    MTOMStAXSOAPModelBuilder builder;

    DataHandler expectedDH;

    File outMTOMFile;

    File outBase64File;

    String outFileName = "target/ActualImageMTOMOut.bin";

    String outBase64FileName = "target/OMSerializeBase64Out.xml";

    String imageInFileName = "mtom/img/test.jpg";

    String imageOutFileName = "target/testOut.jpg";

    String inMimeFileName = "mtom/ImageMTOMOut.bin";

    String contentTypeString =
            "multipart/Related; type=\"application/xop+xml\";start=\"<SOAPPart>\"; boundary=\"----=_AxIs2_Def_boundary_=42214532\"";


    public void testImageSampleSerialize() throws Exception {

        outMTOMFile = new File(outFileName);
        outBase64File = new File(outBase64FileName);
        OMOutputFormat mtomOutputFormat = new OMOutputFormat();
        mtomOutputFormat.setDoOptimize(true);
        OMOutputFormat baseOutputFormat = new OMOutputFormat();
        baseOutputFormat.setDoOptimize(false);

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespaceImpl soap = new OMNamespaceImpl(
                "http://schemas.xmlsoap.org/soap/envelope/", "soap");
        OMElement envelope = new OMElementImpl("Envelope", soap, fac);
        OMElement body = new OMElementImpl("Body", soap, fac);

        OMNamespaceImpl dataName = new OMNamespaceImpl(
                "http://www.example.org/stuff", "m");
        OMElement data = new OMElementImpl("data", dataName, fac);

        DataSource dataSource = getTestResourceDataSource(imageInFileName);
        expectedDH = new DataHandler(dataSource);
        OMText binaryNode = new OMTextImpl(expectedDH, true, fac);

        envelope.addChild(body);
        body.addChild(data);
        data.addChild(binaryNode);

        envelope.serializeAndConsume(new FileOutputStream(outBase64File), baseOutputFormat);
        envelope.serializeAndConsume(new FileOutputStream(outMTOMFile), mtomOutputFormat);
    }

    public void testImageSampleDeserialize() throws Exception {
        InputStream inStream = getTestResource(inMimeFileName);
        Attachments attachments = new Attachments(inStream, contentTypeString);
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(new BufferedReader(
                new InputStreamReader(attachments.getSOAPPartInputStream())));
        builder = new MTOMStAXSOAPModelBuilder(reader, attachments, null);
        OMElement root = builder.getDocumentElement();
        OMElement body = (OMElement) root.getFirstOMChild();
        OMElement data = (OMElement) body.getFirstOMChild();
        OMText blob = (OMText) data.getFirstOMChild();
        /*
         * Following is the procedure the user has to follow to read objects in
         * OBBlob User has to know the object type & whether it is serializable.
         * If it is not he has to use a Custom Defined DataSource to get the
         * Object.
         */

        DataHandler actualDH;
        actualDH = (DataHandler) blob.getDataHandler();
        BufferedImage bufferedImage = ImageIO.read(actualDH.getDataSource().getInputStream());
        this.saveImage("image/jpeg", bufferedImage, new FileOutputStream(imageOutFileName));
        
        root.close(false);
    }

    /**
     * Saves an image.
     *
     * @param mimeType the mime-type of the format to save the image
     * @param image    the image to save
     * @param os       the stream to write to
     * @throws Exception if an error prevents image encoding
     */
    public void saveImage(String mimeType, BufferedImage image, OutputStream os)
            throws Exception {

        ImageWriter writer = null;
        Iterator iter = javax.imageio.ImageIO.getImageWritersByMIMEType(mimeType);
        if (iter.hasNext()) {
            writer = (ImageWriter) iter.next();
        }
        ImageOutputStream ios = javax.imageio.ImageIO.createImageOutputStream(os);
        writer.setOutput(ios);

        writer.write(new IIOImage(image, null, null));
        ios.flush();
        writer.dispose();
    } // saveImage

}