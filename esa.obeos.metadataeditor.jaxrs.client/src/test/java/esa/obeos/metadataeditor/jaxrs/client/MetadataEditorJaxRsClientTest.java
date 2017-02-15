//------------------------------------------------------------------------------
//
// Project: OBEOS METADATA EDITOR
// Authors: Natasha Neumaerker, Siemens Convergence Creators, Prague (CZ)
//          Milan Novacek, Siemens Convergence Creators, Prague (CZ)
//          Radim Zajonc, Siemens Convergence Creators, Prague (CZ)
//          
//------------------------------------------------------------------------------
// Copyright (C) 2017 Siemens Convergence Creators, Prague (CZ)
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies of this Software or works derived from this Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
//------------------------------------------------------------------------------

package esa.obeos.metadataeditor.jaxrs.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.junit.BeforeClass;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import esa.obeos.metadataeditor.common.data.XmlFragments;
import esa.obeos.metadataeditor.common.util.DomUtil;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.AllowedChildrenType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.BuildingBlockResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.DoubleResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.DownloadMetadataResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.GetAllowedChildrenResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.GetBuildingBlockInfoListResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.GetMetadataListResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.MetadataCotainerType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.MetadataResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.MetadataType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.MetadataVersionType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.ModelElementType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.SeverityType;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for Meta data Editor JAX-RS.
 */
public class MetadataEditorJaxRsClientTest extends TestCase
{
    private MetadataEditorJaxRsClient client;

    @SuppressWarnings("unused")
    private String remoteFile1 = "http://geo.spacebel.be/opensearch/request/?httpAccept=application/atom2B%xml&recordSchema=iso&type=collection&parentIdentifier=EOP:ESA:FEDEO:COLLECTIONS&maximumRecords=5&startRecord=1";

    private String remoteFile2 = "http://obeos.spacebel.be/eo-catalogue/series?httpAccept=application/atom%2Bxml&type=collection&recordSchema=iso&maximumRecords=3";

    private File testInputFile1 = new File("src/test/resources/aRBPgrHe_extr1");

    private File testInputFile2 = new File("src/test/resources/aRBPgrHe_extr2");

    private File testInputFile3 = new File("src/test/resources/aRBPgrHe_extr3");

    private File testInputFile4 = new File("src/test/resources/aRBPgrHe_extr4");

    private DocumentBuilder docBuilder;
    
    private Document testDoc1 = null;

    private Document testDoc2 = null;

    private Document testDoc3 = null;

    private Document testDoc4 = null;

    private ModelElementType me1 = null;
    
    private ModelElementType me2 = null;
    
    private ModelElementType me3 = null;
    
    private ModelElementType me4 = null;

    private XPath xPath = null;
    
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public MetadataEditorJaxRsClientTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( MetadataEditorJaxRsClientTest.class );
    }

    /**
     * Sets up the test data.
     */
    @BeforeClass
    protected void setUp() 
    {
        try
        {
            System.out.println("***********************************************");
            System.out.println(" setUp");
            System.out.println("***********************************************");

            this.client = new MetadataEditorJaxRsClient("localhost", 8080, "/SAMDE-0.1.0/rest/mde");
          
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            
            dbFactory.setNamespaceAware(true);
            
            this.docBuilder = dbFactory.newDocumentBuilder();

            System.out.println("loading " + this.testInputFile1.toString());
            this.testDoc1 = this.docBuilder.parse(new FileInputStream(this.testInputFile1));

            System.out.println("loading " + this.testInputFile2.toString());
            this.testDoc2 = this.docBuilder.parse(new FileInputStream(this.testInputFile2));

            System.out.println("loading " + this.testInputFile3.toString());
            this.testDoc3 = this.docBuilder.parse(new FileInputStream(this.testInputFile3));

            System.out.println("loading " + this.testInputFile4.toString());
            this.testDoc4 = this.docBuilder.parse(new FileInputStream(this.testInputFile4));

            this.me1 = new ModelElementType();
            this.me1.setFileName("aRBPgrHe_extr1.xml");
            this.me1.setAny(this.testDoc1.getDocumentElement());

            this.me2 = new ModelElementType();
            this.me2.setFileName("aRBPgrHe_extr2.xml");
            this.me2.setAny(this.testDoc2.getDocumentElement());

            this.me3 = new ModelElementType();
            this.me3.setFileName("aRBPgrHe_extr3.xml");
            this.me3.setAny(this.testDoc3.getDocumentElement());

            this.me4 = new ModelElementType();
            this.me4.setFileName("aRBPgrHe_extr4.xml");
            this.me4.setAny(this.testDoc4.getDocumentElement());

            this.xPath = XPathFactory.newInstance().newXPath();
            this.xPath.setNamespaceContext(new NamespaceContextImpl());

        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Tears down the test data.
     */
    protected void tearDown() throws Exception 
    {
    }

    /**
     * Tests testGetMDList()
     */
    public void testGetMDList()
    {
        try
        {
            System.out.println("***********************************************");
            System.out.println("* testGetMDList");
            System.out.println("***********************************************");
            
            GetMetadataListResultType result01 = this.client.getMDList();
            assertTrue( SeverityType.SUCCESS == result01.getResult());
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Tests createMD() and closeMD()
     */
    public void testCreateMDcloseMD()
    {
        try
        {
            System.out.println("***********************************************");
            System.out.println("* testCreateMDcloseMD");
            System.out.println("***********************************************");

            System.out.println("Creating a new empty metadata '" + MetadataType.GMD.name() + "' version '" + MetadataVersionType.BETA.name() + "'");
            MetadataResultType result01 = this.client.createMD(MetadataType.GMD, MetadataVersionType.BETA, "test1.xml");
            System.out.println("Result '" + result01.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result01.getResult());
            System.out.println("Created metadata '" + result01.getMetadataId() + "'");

            System.out.println("Creating a new empty metadata '" + MetadataType.GMI.name() + "' version '" + MetadataVersionType.BETA.name() + "'");
            MetadataResultType result02 = this.client.createMD(MetadataType.GMI, MetadataVersionType.BETA, "test2.xml");
            System.out.println("Result '" + result02.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result02.getResult());
            System.out.println("Created metadata '" + result02.getMetadataId() + "'");

            System.out.println("Creating a new empty metadata '" + MetadataType.GMD.name() + "' version '" + MetadataVersionType.REL_2015.name() + "'");
            MetadataResultType result03 = this.client.createMD(MetadataType.GMD, MetadataVersionType.REL_2015, "test3.xml");
            System.out.println("Result '" + result03.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result03.getResult());
            System.out.println("Created metadata '" + result03.getMetadataId() + "'");

            System.out.println("Creating a new empty metadata '" + MetadataType.GMI.name() + "' version '" + MetadataVersionType.REL_2015.name() + "'");
            MetadataResultType result04 = this.client.createMD(MetadataType.GMI, MetadataVersionType.REL_2015, "test4.xml");
            System.out.println("Result '" + result04.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result04.getResult());
            System.out.println("Created metadata '" + result04.getMetadataId() + "'");

            System.out.println("Closing the metadata '" + result01.getMetadataId() + "'");
            MetadataResultType result05 = this.client.closeMD(result01.getMetadataId());
            System.out.println("Result '" + result05.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result05.getResult());
            System.out.println("Closed metadata '" + result05.getMetadataId() + "'");


            System.out.println("Closing the metadata '" + result02.getMetadataId() + "'");
            MetadataResultType result06 = this.client.closeMD(result02.getMetadataId());
            System.out.println("Result '" + result06.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result06.getResult());
            System.out.println("Closed metadata '" + result02.getMetadataId() + "'");

            System.out.println("Closing the metadata '" + result03.getMetadataId() + "'");
            MetadataResultType result07 = this.client.closeMD(result03.getMetadataId());
            System.out.println("Result '" + result07.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result07.getResult());
            System.out.println("Closed metadata '" + result03.getMetadataId() + "'");

            System.out.println("Closing the metadata '" + result04.getMetadataId() + "'");
            MetadataResultType result08 = this.client.closeMD(result04.getMetadataId());
            System.out.println("Result '" + result08.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result08.getResult());
            System.out.println("Closed metadata '" + result04.getMetadataId() + "'");

            System.out.println("Closing the non existing metadata '" + result01.getMetadataId() + "'");
            MetadataResultType result09 = this.client.closeMD(result01.getMetadataId());
            System.out.println("Result '" + result02.getResult().name() + "'");
            assertTrue( SeverityType.FAILURE == result09.getResult());
            System.out.println("Cannot close non existing metadata '" + result01.getMetadataId() + "'");
            System.out.println("Diag msg: " + result09.getDiagnostic().getMesage());

            System.out.println("Closing the non existing metadata '" + result02.getMetadataId() + "'");
            MetadataResultType result10 = this.client.closeMD(result02.getMetadataId());
            System.out.println("Result '" + result10.getResult().name() + "'");
            assertTrue( SeverityType.FAILURE == result10.getResult());
            System.out.println("Cannot close non existing metadata '" + result02.getMetadataId() + "'");
            System.out.println("Diag msg: " + result10.getDiagnostic().getMesage());
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Tests uploadMD() and downloadMD()
     */
    public void testUploadMDdownloadMD()
    {
        try
        {
            System.out.println("***********************************************");
            System.out.println("* testUploadMDdownloadMD");
            System.out.println("***********************************************");

            System.out.println("Creating an empty local container");
            MetadataCotainerType mdContainer01 = new MetadataCotainerType();

            mdContainer01.setType(MetadataType.GMI);
            mdContainer01.setVersion(MetadataVersionType.BETA);

            System.out.println("Adding the 1st metadata to the container:");
            DomUtil.printDocument(this.testDoc1, System.out);
            mdContainer01.getMetadata().add(this.me1);
            System.out.println("__________________________________________");

            System.out.println("Adding the 2nd metadata to the container:");
            DomUtil.printDocument(this.testDoc2, System.out);
            mdContainer01.getMetadata().add(this.me2);
            System.out.println("__________________________________________");

            System.out.println("Adding the 3rd metadata to the container:");
            DomUtil.printDocument(this.testDoc3, System.out);
            mdContainer01.getMetadata().add(this.me3);
            System.out.println("__________________________________________");

            System.out.println("Adding the 4th metadata to the container:");
            DomUtil.printDocument(this.testDoc3, System.out);
            mdContainer01.getMetadata().add(this.me4);
            System.out.println("__________________________________________");

            System.out.println("Uploading the container");
            MetadataResultType result01 = this.client.uploadMD(mdContainer01);
            System.out.println("Result '" + result01.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result01.getResult());
            System.out.println("The container uploaded and available under metadataId '" + result01.getMetadataId() + "'");

            System.out.println("Downloading the container '" + result01.getMetadataId() + "'");
            DownloadMetadataResultType result02 = this.client.downloadMD(result01.getMetadataId());
            System.out.println("Result '" + result02.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result02.getResult());

            System.out.println("Downloaded container '" + result01.getMetadataId() + "'");
            MetadataCotainerType mdContainer = result02.getMetadataCotainerType();
            assertNotNull(mdContainer);

            System.out.println("Container type '" + mdContainer.getType() + "'");
            assertTrue(MetadataType.GMI == mdContainer.getType());
            System.out.println("Container version '" + mdContainer.getVersion() + "'");
            assertTrue(MetadataVersionType.BETA == mdContainer.getVersion());
            System.out.println("Container contains '" + mdContainer.getMetadata().size() + "' metadata");
            assertNotNull(mdContainer.getMetadata());
            assertTrue(4 == mdContainer.getMetadata().size());

            for (ModelElementType elm : mdContainer.getMetadata())
            {
                DomUtil.printElement(elm.getAny(), System.out);
            }

            System.out.println("Downloading the container '" + result01.getMetadataId() + "' once more");
            DownloadMetadataResultType result03 = this.client.downloadMD(result01.getMetadataId());
            System.out.println("Result '" + result03.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result03.getResult());

            System.out.println("Closing the container '" + result01.getMetadataId() + "' once more");
            MetadataResultType result04 = this.client.closeMD(result01.getMetadataId());
            System.out.println("Result '" + result04.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result04.getResult());

            System.out.println("Downloading the closed container '" + result01.getMetadataId() + "' again");
            DownloadMetadataResultType result05 = this.client.downloadMD(result01.getMetadataId());
            System.out.println("Result '" + result05.getResult().name() + "'");
            assertTrue( SeverityType.FAILURE == result05.getResult());        

            System.out.println("Closing the non existing metadata '" + result01.getMetadataId() + "'");
            MetadataResultType result06 = this.client.closeMD(result01.getMetadataId());
            System.out.println("Result '" + result06.getResult().name() + "'");
            assertTrue( SeverityType.FAILURE == result06.getResult());
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }


    /**
     * Tests testEditMD()
     */
    public void testEditMD()
    {
        try
        {
            System.out.println("***********************************************");
            System.out.println("* testEditMD");
            System.out.println("***********************************************");

            System.out.println("Adding metadata from remote file: '" + remoteFile2 + "'");
            DoubleResultType result01 = this.client.editMD(this.remoteFile2);
            assertTrue( SeverityType.SUCCESS == result01.getResult());

            String metadataId = result01.getMdMetadataId();
            if (null == metadataId)
            {
                metadataId = result01.getMiMetadataId();
            }

            System.out.println("Download added metadata");
            DownloadMetadataResultType result02 = this.client.downloadMD(metadataId);
            assertTrue( SeverityType.SUCCESS == result02.getResult());

            if (null != result02.getDiagnostic() && null != result02.getDiagnostic().getMesage())
            {
                System.out.println(result02.getDiagnostic().getMesage());
            }

            assertNotNull( result02.getMetadataCotainerType() );
            for (ModelElementType modelElement : result02.getMetadataCotainerType().getMetadata())
            {
                try
                {
                    System.out.println("Metadata:");
                    DomUtil.printElement(modelElement.getAny(), System.out);
                }
                catch (IOException | TransformerException e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Tests GetAllowedNodes()
     */
    public void testGetAllowedNodes()
    {
        try
        {
            System.out.println("***********************************************");
            System.out.println("* testGetAllowedNodes");
            System.out.println("***********************************************");

            System.out.println("Creating an empty local container");
            MetadataCotainerType mdContainer = new MetadataCotainerType();

            mdContainer.setType(MetadataType.GMI);
            mdContainer.setVersion(MetadataVersionType.BETA);
            System.out.println("Adding the 1st metadata to the container:");
            DomUtil.printDocument(this.testDoc1, System.out);
            mdContainer.getMetadata().add(this.me1);
            System.out.println("__________________________________________");

            System.out.println("Uploading the container");
            MetadataResultType result01 = this.client.uploadMD(mdContainer);
            System.out.println("Result '" + result01.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result01.getResult());

            System.out.println("Getting the allowed nodes for element '/MI_Metadata'");
            GetAllowedChildrenResultType result02 = this.client.getAllowedNodes(result01.getMetadataId(), "/MI_Metadata");
            System.out.println("Result '" + result02.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result02.getResult());

            List<AllowedChildrenType> allowedChildList01 = result02.getAllowedChildren();
            assertNotNull(allowedChildList01);

            System.out.println("__________________________________________");
            for( AllowedChildrenType allowedChild : allowedChildList01 )
            {
                assertNotNull(allowedChild.getChildNames());
                assertNotNull(allowedChild.getChildNames().getChildName());
                for( String childName : allowedChild.getChildNames().getChildName() )
                {
                    assertNotNull(childName);
                    assertFalse(childName.isEmpty());
                    System.out.println("'/MI_Metadata' allowed child '" + childName + "'");
                }
            }
            System.out.println("__________________________________________");

            System.out.println("Getting the allowed nodes for element '/MI_Metadata/identificationInfo[0]/MD_DataIdentification'");
            GetAllowedChildrenResultType result03 = this.client.getAllowedNodes(result01.getMetadataId(), 
                    "/MI_Metadata/identificationInfo[0]/MD_DataIdentification");
            System.out.println("Result '" + result03.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result03.getResult());

            List<AllowedChildrenType> allowedChildList02 = result03.getAllowedChildren();
            assertNotNull(allowedChildList02);

            System.out.println("__________________________________________");
            for( AllowedChildrenType allowedChild : allowedChildList02 )
            {
                assertNotNull(allowedChild.getChildNames());
                assertNotNull(allowedChild.getChildNames().getChildName());
                for( String childName : allowedChild.getChildNames().getChildName() )
                {
                    assertNotNull(childName);
                    assertFalse(childName.isEmpty());
                    System.out.println("'/MI_Metadata/identificationInfo[0]/MD_DataIdentification' allowed child '" + childName + "'");
                }
            }
            System.out.println("__________________________________________");

            System.out.println("Getting the allowed nodes for non existing element '/MI_Metadata0'");
            GetAllowedChildrenResultType result04 = this.client.getAllowedNodes(result01.getMetadataId(), "/MI_Metadata0");
            System.out.println("Result '" + result04.getResult().name() + "'");
            assertTrue( SeverityType.FAILURE == result04.getResult());        

            assertNotNull( result04.getDiagnostic() );        
            assertNotNull( result04.getDiagnostic().getMesage() );
            System.out.println("Diag msg: " + result04.getDiagnostic().getMesage());

            System.out.println("Getting the allowed nodes for non existing element '/1'");
            GetAllowedChildrenResultType result05 = this.client.getAllowedNodes(result01.getMetadataId(), "/1");
            System.out.println("Result '" + result05.getResult().name() + "'");
            assertTrue( SeverityType.FAILURE == result05.getResult());        
            assertNotNull( result05.getDiagnostic() );        
            assertNotNull( result05.getDiagnostic().getMesage() );
            System.out.println("Diag msg: " + result05.getDiagnostic().getMesage());

            System.out.println("Getting the allowed nodes for non existing element '/MI_Metadata[0]'");
            GetAllowedChildrenResultType result06 = this.client.getAllowedNodes(result01.getMetadataId(), "/MI_Metadata[0]");
            System.out.println("Result '" + result06.getResult().name() + "'");
            assertTrue( SeverityType.FAILURE == result06.getResult());        
            assertNotNull( result06.getDiagnostic() );        
            assertNotNull( result06.getDiagnostic().getMesage() );
            System.out.println("Diag msg: " + result06.getDiagnostic().getMesage());

            System.out.println("Closing the metadata '" + result01.getMetadataId() + "'");
            MetadataResultType result07 = this.client.closeMD(result01.getMetadataId());
            System.out.println("Result '" + result07.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result07.getResult());
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Tests insertNode()
     */
    public void testInsertNode()
    {
        try
        {
            System.out.println("***********************************************");
            System.out.println("* testInsertNode");
            System.out.println("***********************************************");

            System.out.println("Creating an empty local container");
            MetadataCotainerType mdContainer01 = new MetadataCotainerType();

            mdContainer01.setType(MetadataType.GMI);
            mdContainer01.setVersion(MetadataVersionType.BETA);

            System.out.println("Adding the 1st metadata to the container:");
            DomUtil.printDocument(this.testDoc1, System.out);
            mdContainer01.getMetadata().add(this.me1);
            System.out.println("__________________________________________");

            System.out.println("Adding the 2nd metadata to the container:");
            DomUtil.printDocument(this.testDoc2, System.out);
            mdContainer01.getMetadata().add(this.me2);
            System.out.println("__________________________________________");

            System.out.println("Adding the 3rd metadata to the container:");
            DomUtil.printDocument(this.testDoc3, System.out);
            mdContainer01.getMetadata().add(this.me3);
            System.out.println("__________________________________________");

            System.out.println("Adding the 4th metadata to the container:");
            DomUtil.printDocument(this.testDoc3, System.out);
            mdContainer01.getMetadata().add(this.me4);
            System.out.println("__________________________________________");


            System.out.println("Uploading the container");
            MetadataResultType result01 = this.client.uploadMD(mdContainer01);
            System.out.println("Result '" + result01.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result01.getResult());
            System.out.println("Uploaded the container '" + result01.getMetadataId() + "'");

            System.out.println("Inserting the empty element 'identificationInfo[1]'");
            MetadataResultType result02 = this.client.insertNode(result01.getMetadataId(),
                    "/MI_Metadata", "identificationInfo[1]");
            System.out.println("Result '" + result02.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result02.getResult());

            Element domElement_01 = null;
            try
            {
                System.out.println("Creating the element 'contact' from an XML contact fragment");
                System.out.println(XmlFragments.CONTACT_01);
                domElement_01 = DomUtil.createElementFromXmlFragment(XmlFragments.CONTACT_01, true);
            }
            catch(Exception e)
            {
                fail(e.getMessage());
            }

            assertNotNull(domElement_01);

            System.out.println("Inserting the element 'contact' to the element 'MI_Metadata'");
            MetadataResultType result03 = this.client.insertNode(result01.getMetadataId(),
                    "/MI_Metadata", domElement_01, 1);
            System.out.println("Result '" + result03.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result03.getResult());

            Element domElement_02 = null;
            try
            {
                System.out.println("Creating the element 'phone' from an XML phone fragment");
                System.out.println(XmlFragments.PHONE_PROPERTY_01);
                domElement_02 = DomUtil.createElementFromXmlFragment(XmlFragments.PHONE_PROPERTY_01, true);
            } 
            catch(Exception e)
            {
                fail(e.getMessage());
            }

            assertNotNull(domElement_02);

            System.out.println("Inserting the element 'phone' to the element 'MI_Metadata'");
            MetadataResultType result04 = this.client.insertNode(result01.getMetadataId(),
                    "/MI_Metadata", domElement_02, null);
            System.out.println("Result '" + result04.getResult().name() + "'");
            assertTrue( SeverityType.FAILURE == result04.getResult());

            assertNotNull( result04.getDiagnostic() );        
            assertNotNull( result04.getDiagnostic().getMesage() );
            System.out.println("Diag msg: " + result04.getDiagnostic().getMesage());

            System.out.println("Downloading the container '" + result01.getMetadataId() + "' once more");
            DownloadMetadataResultType result05 = this.client.downloadMD(result01.getMetadataId());
            assertTrue( SeverityType.SUCCESS == result05.getResult());

            MetadataCotainerType mdContainer02 = result05.getMetadataCotainerType();
            assertNotNull(mdContainer02);
            assertNotNull(mdContainer02.getMetadata());

            String nsPath01 = "/gmi:MI_Metadata/gmd:identificationInfo[2]";
            String nsPath02 = "/gmi:MI_Metadata/gmd:contact[2]";
            String nsPath03 = "/gmi:MI_Metadata/gmd:contact[2]/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone";

            try
            {
                for( ModelElementType element : mdContainer02.getMetadata() )
                {
                    System.out.println("check that element at xpath '" + nsPath01 + "' is present");
                    Node node01 = (Node)this.xPath.evaluate(nsPath01, element.getAny(), XPathConstants.NODE);
                    assertNotNull(node01);

                    System.out.println("check that element at xpath '" + nsPath02 + "' is present");
                    Node node02 = (Node)this.xPath.evaluate(nsPath02, element.getAny(), XPathConstants.NODE);
                    assertNotNull(node02);

                    System.out.println("check that element at xpath '" + nsPath03 + "' is present");
                    Node node03 = (Node)this.xPath.evaluate(nsPath03, element.getAny(), XPathConstants.NODE);
                    assertNotNull(node03);
                }
            } 
            catch (Exception e)
            {
                fail(e.getMessage());
                e.printStackTrace();
            }

            System.out.println("Closing the metadata '" + result01.getMetadataId() + "'");
            MetadataResultType result07 = this.client.closeMD(result01.getMetadataId());
            System.out.println("Result '" + result07.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result07.getResult() );
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Tests deleteNode()
     */
    public void testDeleteNode()
    {
        try
        {
            System.out.println("***********************************************");
            System.out.println("testDeleteNode");
            System.out.println("***********************************************");

            System.out.println("Creating an empty local container");
            MetadataCotainerType mdContainer01 = new MetadataCotainerType();

            mdContainer01.setType(MetadataType.GMI);
            mdContainer01.setVersion(MetadataVersionType.BETA);

            System.out.println("Adding the 1st metadata to the container:");
            DomUtil.printDocument(this.testDoc1, System.out);
            mdContainer01.getMetadata().add(this.me1);
            System.out.println("__________________________________________");

            System.out.println("Adding the 2nd metadata to the container:");
            DomUtil.printDocument(this.testDoc2, System.out);
            mdContainer01.getMetadata().add(this.me2);
            System.out.println("__________________________________________");

            System.out.println("Adding the 3rd metadata to the container:");
            DomUtil.printDocument(this.testDoc3, System.out);
            mdContainer01.getMetadata().add(this.me3);
            System.out.println("__________________________________________");

            System.out.println("Adding the 4th metadata to the container:");
            DomUtil.printDocument(this.testDoc3, System.out);
            mdContainer01.getMetadata().add(this.me4);
            System.out.println("__________________________________________");

            System.out.println("Uploading the container");
            MetadataResultType result01 = this.client.uploadMD(mdContainer01);
            System.out.println("Result '" + result01.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result01.getResult());
            System.out.println("The container uploaded and available under metadataId '" + result01.getMetadataId() + "'");

            String nsPath01 = "/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier";

            try
            {
                for( ModelElementType element : mdContainer01.getMetadata() )
                {
                    System.out.println("check that element at xpath '" + nsPath01 + "' is present");
                    Node node01 = (Node)this.xPath.evaluate(nsPath01, element.getAny(), XPathConstants.NODE);
                    assertNotNull(node01);
                }
            }
            catch (Exception e)
            {
                fail(e.getMessage());
                e.printStackTrace();
            }

            String path01 = "/MI_Metadata/identificationInfo[0]/MD_DataIdentification/citation/CI_Citation/identifier[0]";

            System.out.println("Deleting the element at path '" + path01 + "'");
            MetadataResultType result02 = this.client.deleteNode(result01.getMetadataId(), path01);
            assertTrue( SeverityType.SUCCESS == result02.getResult());

            System.out.println("Downloading the container '" + result01.getMetadataId() + "'");
            DownloadMetadataResultType result03 = this.client.downloadMD(result01.getMetadataId());
            assertTrue( SeverityType.SUCCESS == result03.getResult());

            MetadataCotainerType mdContainer02 = result03.getMetadataCotainerType();
            assertNotNull(mdContainer02);
            assertNotNull(mdContainer02.getMetadata());

            try
            {
                for( ModelElementType element : mdContainer02.getMetadata() )
                {
                    System.out.println("check that element at xpath '" + nsPath01 + "' is no longer present");
                    Node node01 = (Node)this.xPath.evaluate(nsPath01, element.getAny(), XPathConstants.NODE);
                    assertNull(node01);
                }
            } 
            catch (Exception e)
            {
                fail(e.getMessage());
                e.printStackTrace();
            }

            System.out.println("Deleting the element at path '" + path01 + "' again");
            MetadataResultType result04 = this.client.deleteNode(result01.getMetadataId(), path01);
            System.out.println("Result '" + result04.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result04.getResult());

            System.out.println("Closing the metadata '" + result01.getMetadataId() + "'");
            MetadataResultType result05 = this.client.closeMD(result01.getMetadataId());
            System.out.println("Result '" + result05.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result05.getResult() );
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Tests setNodeValue()
     */
    public void testSetNodeValue()
    {
        try
        {
            System.out.println("***********************************************");
            System.out.println("testSetNodeValue");
            System.out.println("***********************************************");

            System.out.println("Creating an empty local container");
            MetadataCotainerType mdContainer01 = new MetadataCotainerType();

            mdContainer01.setType(MetadataType.GMI);
            mdContainer01.setVersion(MetadataVersionType.BETA);

            System.out.println("Adding the 1st metadata to the container:");
            DomUtil.printDocument(this.testDoc1, System.out);
            mdContainer01.getMetadata().add(this.me1);
            System.out.println("__________________________________________");

            System.out.println("Adding the 2nd metadata to the container:");
            DomUtil.printDocument(this.testDoc2, System.out);
            mdContainer01.getMetadata().add(this.me2);
            System.out.println("__________________________________________");

            System.out.println("Adding the 3rd metadata to the container:");
            DomUtil.printDocument(this.testDoc3, System.out);
            mdContainer01.getMetadata().add(this.me3);
            System.out.println("__________________________________________");

            System.out.println("Adding the 4th metadata to the container:");
            DomUtil.printDocument(this.testDoc3, System.out);
            mdContainer01.getMetadata().add(this.me4);
            System.out.println("__________________________________________");

            System.out.println("Uploading the container");
            MetadataResultType result01 = this.client.uploadMD(mdContainer01);
            System.out.println("Result '" + result01.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result01.getResult());
            System.out.println("The container uploaded and available under metadataId '" + result01.getMetadataId() + "'");

            String path01 = "/MI_Metadata/contact[0]/CI_ResponsibleParty/contactInfo/CI_Contact/address/CI_Address/electronicMailAddress[0]/CharacterString";
            String path02 = "/MI_Metadata/dateStamp/DateTime";

            String value01 = "value01";
            String value02 = "2016-07-17T01:02:03.004Z";

            System.out.println("Setting element value '" + value01 + "' at path '" + path01 + "'");
            MetadataResultType result02 = this.client.setNodeValue(result01.getMetadataId(), path01, value01);
            System.out.println("Result '" + result02.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result02.getResult());

            System.out.println("Setting element value '" + value02 + "' at path '" + path02 + "'");
            MetadataResultType result03 = this.client.setNodeValue(result01.getMetadataId(), path02, value02);
            System.out.println("Result '" + result02.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result03.getResult());

            System.out.println("Downloading the container '" + result01.getMetadataId() + "'");
            DownloadMetadataResultType result04 = this.client.downloadMD(result01.getMetadataId());
            System.out.println("Result '" + result04.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result04.getResult());

            MetadataCotainerType mdContainer02 = result04.getMetadataCotainerType();
            assertNotNull(mdContainer02);
            assertNotNull(mdContainer02.getMetadata());

            String nsPath01 = "/gmi:MI_Metadata/gmd:contact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString";
            String nsPath02 = "/gmi:MI_Metadata/gmd:dateStamp/gco:DateTime";

            try
            {
                for (ModelElementType element : mdContainer02.getMetadata())
                {
                    System.out.println("Getting the element at the path '" + nsPath01 + "'");
                    Node node01 = (Node)this.xPath.evaluate(nsPath01, element.getAny(), XPathConstants.NODE);
                    assertNotNull(node01);

                    String val01 = node01.getTextContent();
                    System.out.println("The element value is '" + val01 + "'");
                    assertTrue(val01.equals(value01));

                    System.out.println("Getting the element at the path '" + nsPath02 + "'");
                    Node node02 = (Node)this.xPath.evaluate(nsPath02, element.getAny(), XPathConstants.NODE);
                    assertNotNull(node02);

                    String val02 = node02.getTextContent();
                    System.out.println("The element value is '" + val02 + "'");
                    assertTrue(val02.equals(value02));
                }
            } 
            catch (Exception e)
            {
                fail(e.getMessage());
                e.printStackTrace();
            }

            String path03 = "/MI_Metadata";
            System.out.println("Setting element value '" + value01 + "' at path '" + path03 + "'");
            MetadataResultType result05 = this.client.setNodeValue(result01.getMetadataId(), path03, value01);
            System.out.println("Result '" + result05.getResult().name() + "'");
            assertTrue( SeverityType.FAILURE == result05.getResult());

            assertNotNull( result05.getDiagnostic() );        
            assertNotNull( result05.getDiagnostic().getMesage() );
            System.out.println("Diag msg: " + result05.getDiagnostic().getMesage());

            System.out.println("Closing the metadata '" + result01.getMetadataId() + "'");
            MetadataResultType result06 = this.client.closeMD(result01.getMetadataId());
            System.out.println("Result '" + result06.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result06.getResult() );
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Tests setNodeAttribute()
     */
    public void testSetNodeAttribute()
    {
        try
        {
            System.out.println("***********************************************");
            System.out.println("testSetNodeAttribute");
            System.out.println("***********************************************");

            System.out.println("Creating an empty local container");
            MetadataCotainerType mdContainer01 = new MetadataCotainerType();

            mdContainer01.setType(MetadataType.GMI);
            mdContainer01.setVersion(MetadataVersionType.BETA);

            System.out.println("Adding the 1st metadata to the container:");
            DomUtil.printDocument(this.testDoc1, System.out);
            mdContainer01.getMetadata().add(this.me1);
            System.out.println("__________________________________________");

            System.out.println("Adding the 2nd metadata to the container:");
            DomUtil.printDocument(this.testDoc2, System.out);
            mdContainer01.getMetadata().add(this.me2);
            System.out.println("__________________________________________");

            System.out.println("Adding the 3rd metadata to the container:");
            DomUtil.printDocument(this.testDoc3, System.out);
            mdContainer01.getMetadata().add(this.me3);
            System.out.println("__________________________________________");

            System.out.println("Adding the 4th metadata to the container:");
            DomUtil.printDocument(this.testDoc3, System.out);
            mdContainer01.getMetadata().add(this.me4);
            System.out.println("__________________________________________");

            System.out.println("Uploading the container");
            MetadataResultType result01 = this.client.uploadMD(mdContainer01);
            System.out.println("Result '" + result01.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result01.getResult());
            System.out.println("The container uploaded and available under metadataId '" + result01.getMetadataId() + "'");

            String path01 = "/MI_Metadata/identificationInfo[0]/MD_DataIdentification/extent[0]/EX_Extent/temporalElement[0]/EX_TemporalExtent/extent/TimePeriod";
            String path02 = "/MI_Metadata/dateStamp/DateTime";

            String attribName01 = "id";
            String attribValue01 = "attribValue01";

            System.out.println("Setting attribute value '" + attribName01 + "' at path '" + path02 + "'");
            MetadataResultType result02 = this.client.setNodeAttribute(result01.getMetadataId(), path01, attribName01, attribValue01);
            System.out.println("Result '" + result01.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result02.getResult());

            System.out.println("Downloading the container '" + result01.getMetadataId() + "'");
            DownloadMetadataResultType result03 = this.client.downloadMD(result01.getMetadataId());
            System.out.println("Result '" + result03.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result03.getResult());

            MetadataCotainerType mdContainer02 = result03.getMetadataCotainerType();
            assertNotNull(mdContainer02);
            assertNotNull(mdContainer02.getMetadata());

            String nsPath01 = "/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod";

            try
            {
                for( ModelElementType element : mdContainer02.getMetadata() )
                {
                    Node node01 = (Node)this.xPath.evaluate(nsPath01, element.getAny(), XPathConstants.NODE);
                    assertNotNull(node01);
                    
                    Element element01 = (Element)node01;
                    String attribVal01 = element01.getAttribute("gml:" + attribName01);

                    assertNotNull(attribVal01);
                    assertTrue(attribVal01.equals(attribValue01));
                }
            }
            catch (Exception e)
            {
                fail(e.getMessage());
                e.printStackTrace();
            }

            System.out.println("Setting attribute value '" + attribName01 + "' at path '" + path02 + "'. The element does not support the attribute.");
            MetadataResultType result04 = this.client.setNodeAttribute(result01.getMetadataId(), path02, attribName01, attribValue01);
            System.out.println("Result '" + result01.getResult().name() + "'");
            assertTrue( SeverityType.FAILURE == result04.getResult());

            assertNotNull( result04.getDiagnostic() );        
            assertNotNull( result04.getDiagnostic().getMesage() );
            System.out.println("Diag msg: " + result04.getDiagnostic().getMesage());

            System.out.println("Closing the metadata '" + result01.getMetadataId() + "'");
            MetadataResultType result05 = this.client.closeMD(result01.getMetadataId());
            System.out.println("Result '" + result05.getResult().name() + "'");
            assertTrue( SeverityType.SUCCESS == result05.getResult() );
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Tests addBuildingBlock() and getBuildingBlockInfoList
     */
    public void testAddBuildingBlockAndGetBuildingBlockInfoList()
    {
        try
        {
            BuildingBlockResultType result01 = this.client.addBuildingBlock("Hello", "greating", "<greating>Hello</greating>");
            assertTrue(SeverityType.SUCCESS == result01.getResult());

            BuildingBlockResultType result02 = this.client.addBuildingBlock("Hi", "greating", "<greating>Hi</greating>");
            assertTrue(SeverityType.SUCCESS == result02.getResult());

            GetBuildingBlockInfoListResultType result03 = this.client.getBuildingBlockInfoList();
            assertTrue( SeverityType.SUCCESS == result03.getResult());

            assertNotNull(result03.getBuildingBlockInfoType());
            assertTrue(2 == result03.getBuildingBlockInfoType().size());

            BuildingBlockResultType result04 = this.client.deleteBuildingBlock("Hi");
            assertTrue(SeverityType.SUCCESS == result04.getResult());

            GetBuildingBlockInfoListResultType result05 = this.client.getBuildingBlockInfoList();
            assertTrue( SeverityType.SUCCESS == result05.getResult());

            assertNotNull(result05.getBuildingBlockInfoType());
            assertTrue(1 == result05.getBuildingBlockInfoType().size());

            BuildingBlockResultType result06 = this.client.deleteBuildingBlock("Salut");
            assertTrue(SeverityType.FAILURE == result06.getResult());

            BuildingBlockResultType result07 = this.client.deleteBuildingBlock("Hello");
            assertTrue(SeverityType.SUCCESS == result07.getResult());

            GetBuildingBlockInfoListResultType result08 = this.client.getBuildingBlockInfoList();
            assertTrue( SeverityType.SUCCESS == result08.getResult());

            assertNotNull(result08.getBuildingBlockInfoType());
            assertTrue(0 == result08.getBuildingBlockInfoType().size());
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
