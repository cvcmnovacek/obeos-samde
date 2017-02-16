//------------------------------------------------------------------------------
//
// Project: OBEOS METADATA EDITOR
// Authors: Natascha Neumaerker, Siemens Convergence Creators, Prague (CZ)
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

package esa.obeos.metadataeditor.jaxrs.service;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import esa.obeos.metadataeditor.common.data.XmlFragments;
import esa.obeos.metadataeditor.common.util.DomUtil;
import esa.obeos.metadataeditor.common.util.TimeUtil;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.AllowedChildrenType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.BuildingBlockArgumentType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.BuildingBlockInfoType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.BuildingBlockResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.DownloadMetadataResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.GetAllowedChildrenResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.GetBuildingBlockInfoListResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.MetadataCotainerType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.MetadataResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.MetadataType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.MetadataVersionType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.ModelElementType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.NodeArgumentType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.NodeAttributeArgumentType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.NodeType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.NodeValueArgumentType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.SeverityType;
import esa.obeos.metadataeditor.model.api.IMdeElement;
import esa.obeos.metadataeditor.model.api.MdePath;
import esa.obeos.metadataeditor.model.api.Model;
import esa.obeos.metadataeditor.model.api.ModelType;
import esa.obeos.metadataeditor.model.api.ModelVersion;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for  MetadataEditorServiceImpl.
 */
public class MetadataEditorServiceImplTest extends TestCase
{
    private MetadataEditorServiceImpl service;

    // private String localFile = "file:///home/obeos/FEED/2JCLX9A3";

    private File testInputFile1 = new File("src/test/resources/aRBPgrHe_extr1");

    private File testInputFile2 = new File("src/test/resources/aRBPgrHe_extr2");

    private File testInputFile3 = new File("src/test/resources/aRBPgrHe_extr3");

    private File testInputFile4 = new File("src/test/resources/aRBPgrHe_extr4");

    private IMdeElement testModel1 = null;

    private IMdeElement testModel2 = null;

    private IMdeElement testModel3 = null;

    private IMdeElement testModel4 = null;

    private Document testDoc1 = null;

    private Document testDoc2 = null;

    private Document testDoc3 = null;

    private Document testDoc4 = null;

    private ModelElementType me1 = null;
    
    private ModelElementType me2 = null;
    
    private ModelElementType me3 = null;
    
    private ModelElementType me4 = null;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public MetadataEditorServiceImplTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( MetadataEditorServiceImplTest.class );
    }

    /**
     * Sets up the test data.
     */
    protected void setUp() throws Exception 
    {
        this.service = new MetadataEditorServiceImpl();

        try
        {
            System.out.println("loading " + testInputFile1.toString());
            this.testModel1 = Model.load(testInputFile1, ModelType.GMI, ModelVersion.Beta, true);

            System.out.println("loading " + testInputFile2.toString());
            this.testModel2 = Model.load(testInputFile2, ModelType.GMI, ModelVersion.Beta, true);

            System.out.println("loading " + testInputFile3.toString());
            this.testModel3 = Model.load(testInputFile3, ModelType.GMI, ModelVersion.Beta, true);

            System.out.println("loading " + testInputFile4.toString());
            this.testModel4 = Model.load(testInputFile4, ModelType.GMI, ModelVersion.Beta, true);

            System.out.println("converting the model from '" + testInputFile1.toString() + "' to DOM document");
            this.testDoc1 = Model.convert(this.testModel1, true);

            System.out.println("converting the model from '" + testInputFile2.toString() + "' to DOM document");
            this.testDoc2 = Model.convert(this.testModel2, true);

            System.out.println("converting the model from '" + testInputFile3.toString() + "' to DOM document");
            this.testDoc3 = Model.convert(this.testModel3, true);

            System.out.println("converting the model from '" + testInputFile4.toString() + "' to DOM document");
            this.testDoc4 = Model.convert(this.testModel4, true);

            me1 = new ModelElementType();
            me1.setFileName("a.xml");
            me1.setAny(this.testDoc1.getDocumentElement());
            
            me2 = new ModelElementType();
            me2.setFileName("b.xml");
            me2.setAny(this.testDoc2.getDocumentElement());
            
            me3 = new ModelElementType();
            me3.setFileName("c.xml");
            me3.setAny(this.testDoc3.getDocumentElement());
            
            me4 = new ModelElementType();
            me4.setFileName("d.xml");
            me4.setAny(this.testDoc4.getDocumentElement());
}
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    /**
     * Tears down the test data.
     */
    protected void tearDown() throws Exception 
    {
    }

    /**
     * Tests createMD() and closeMD()
     */
    public void testCreateMDcloseMD()
    {
        MetadataResultType result01 = this.service.createMD(MetadataType.GMD, MetadataVersionType.BETA, "testFile1.xml");
        assertTrue( SeverityType.SUCCESS == result01.getResult());

        MetadataResultType result02 = this.service.createMD(MetadataType.GMI, MetadataVersionType.BETA,  "testFile2.xml");
        assertTrue( SeverityType.SUCCESS == result02.getResult());

        MetadataResultType result03 = this.service.createMD(MetadataType.GMD, MetadataVersionType.REL_2015,  "testFile3.xml");
        assertTrue( SeverityType.SUCCESS == result03.getResult());

        MetadataResultType result04 = this.service.createMD(MetadataType.GMI, MetadataVersionType.REL_2015,  "testFile4.xml");
        assertTrue( SeverityType.SUCCESS == result04.getResult());

        MetadataResultType result05 = this.service.closeMD(result01.getMetadataId());
        assertTrue( SeverityType.SUCCESS == result05.getResult());

        MetadataResultType result06 = this.service.closeMD(result02.getMetadataId());
        assertTrue( SeverityType.SUCCESS == result06.getResult());

        MetadataResultType result07 = this.service.closeMD(result03.getMetadataId());
        assertTrue( SeverityType.SUCCESS == result07.getResult());

        MetadataResultType result08 = this.service.closeMD(result04.getMetadataId());
        assertTrue( SeverityType.SUCCESS == result08.getResult());

        MetadataResultType result09 = this.service.closeMD(result01.getMetadataId());
        assertTrue( SeverityType.FAILURE == result09.getResult());

        MetadataResultType result10 = this.service.closeMD(result02.getMetadataId());
        assertTrue( SeverityType.FAILURE == result10.getResult());
    }

    /**
     * Tests uploadMD() and downloadMD()
     */
    public void testUploadMDdownloadMD()
    {
        MetadataCotainerType mdContainer01 = new MetadataCotainerType();

        mdContainer01.setType(MetadataType.GMI);
        mdContainer01.setVersion(MetadataVersionType.BETA);
        mdContainer01.getMetadata().add(this.me1);
        mdContainer01.getMetadata().add(this.me2);
        mdContainer01.getMetadata().add(this.me3);
        mdContainer01.getMetadata().add(this.me4);

        MetadataResultType result01 = this.service.uploadMD(mdContainer01);
        assertTrue( SeverityType.SUCCESS == result01.getResult());

        DownloadMetadataResultType result02 = this.service.downloadMD(result01.getMetadataId());
        assertTrue( SeverityType.SUCCESS == result02.getResult());

        MetadataCotainerType mdContainer = result02.getMetadataCotainerType();
        assertNotNull(mdContainer);
        assertTrue(MetadataType.GMI == mdContainer.getType());
        assertTrue(MetadataVersionType.BETA == mdContainer.getVersion());
        assertNotNull(mdContainer.getMetadata());
        assertTrue(4 == mdContainer.getMetadata().size());

        DownloadMetadataResultType result03 = this.service.downloadMD(result01.getMetadataId());
        assertTrue( SeverityType.SUCCESS == result03.getResult());

        MetadataResultType result04 = this.service.closeMD(result01.getMetadataId());
        assertTrue( SeverityType.SUCCESS == result04.getResult());

        DownloadMetadataResultType result05 = this.service.downloadMD(result01.getMetadataId());
        assertTrue( SeverityType.FAILURE == result05.getResult());        

        MetadataResultType result06 = this.service.closeMD(result01.getMetadataId());
        assertTrue( SeverityType.FAILURE == result06.getResult());
    }

    /**
     * Tests GetAllowedNodes()
     */
    public void testGetAllowedNodes()
    {
        MetadataCotainerType mdContainer = new MetadataCotainerType();

        mdContainer.setType(MetadataType.GMI);
        mdContainer.setVersion(MetadataVersionType.BETA);
        mdContainer.getMetadata().add(this.me1);

        MetadataResultType result01 = this.service.uploadMD(mdContainer);
        assertTrue( SeverityType.SUCCESS == result01.getResult());

        String path01 = "/MI_Metadata";
        
        GetAllowedChildrenResultType result02 = this.service.getAllowedNodes(result01.getMetadataId(), path01);
        assertTrue( SeverityType.SUCCESS == result02.getResult());

        List<AllowedChildrenType> allowedChildList01 = result02.getAllowedChildren();
        assertNotNull(allowedChildList01);

        for( AllowedChildrenType allowedChild : allowedChildList01 )
        {
            assertNotNull(allowedChild.getChildNames());
            assertNotNull(allowedChild.getChildNames().getChildName());
            for( String childName : allowedChild.getChildNames().getChildName() )
            {
                assertNotNull(childName);
                assertFalse(childName.isEmpty());
            }
        }

        String path02 = "/MI_Metadata/identificationInfo[0]/MD_DataIdentification";
        
        GetAllowedChildrenResultType result03 = this.service.getAllowedNodes(result01.getMetadataId(), path02);
        assertTrue( SeverityType.SUCCESS == result03.getResult());

        List<AllowedChildrenType> allowedChildList02 = result03.getAllowedChildren();
        assertNotNull(allowedChildList02);

        for( AllowedChildrenType allowedChild : allowedChildList02 )
        {
            assertNotNull(allowedChild.getChildNames());
            assertNotNull(allowedChild.getChildNames().getChildName());
            for( String childName : allowedChild.getChildNames().getChildName() )
            {
                assertNotNull(childName);
                assertFalse(childName.isEmpty());
            }
        }

        String path03 = "/MI_Metadata0";

        GetAllowedChildrenResultType result04 = this.service.getAllowedNodes(result01.getMetadataId(), path03);
        assertTrue( SeverityType.FAILURE == result04.getResult());        
        assertNotNull( result04.getDiagnostic() );        
        assertNotNull( result04.getDiagnostic().getMesage() );
        System.out.println("Diag msg: " + result04.getDiagnostic().getMesage());

        String path04 = "/1";

        GetAllowedChildrenResultType result05 = this.service.getAllowedNodes(result01.getMetadataId(), path04);
        assertTrue( SeverityType.FAILURE == result05.getResult());        
        assertNotNull( result05.getDiagnostic() );        
        assertNotNull( result05.getDiagnostic().getMesage() );
        System.out.println("Diag msg: " + result05.getDiagnostic().getMesage());

        String path05 = "/MI_Metadata[0]";

        GetAllowedChildrenResultType result06 = this.service.getAllowedNodes(result01.getMetadataId(), path05);
        assertTrue( SeverityType.FAILURE == result06.getResult());        
        assertNotNull( result06.getDiagnostic() );        
        assertNotNull( result06.getDiagnostic().getMesage() );
        System.out.println("Diag msg: " + result06.getDiagnostic().getMesage());

        MetadataResultType result07 = this.service.closeMD(result01.getMetadataId());
        assertTrue( SeverityType.SUCCESS == result07.getResult());
    }

    /**
     * Tests insertNode()
     */
    public void testInsertNode()
    {
        MetadataCotainerType mdContainer_01 = new MetadataCotainerType();

        mdContainer_01.setType(MetadataType.GMI);
        mdContainer_01.setVersion(MetadataVersionType.BETA);
        mdContainer_01.getMetadata().add(this.me1);
        mdContainer_01.getMetadata().add(this.me2);
        mdContainer_01.getMetadata().add(this.me3);
        mdContainer_01.getMetadata().add(this.me4);

        MetadataResultType result01 = this.service.uploadMD(mdContainer_01);
        assertTrue( SeverityType.SUCCESS == result01.getResult());

        NodeArgumentType argument01 = new NodeArgumentType();
        argument01.setMetadataId(result01.getMetadataId());
        argument01.setPath("/MI_Metadata");
        argument01.setNodeName("identificationInfo[1]");
        
        MetadataResultType result02 = this.service.insertNode(argument01);
        assertTrue( SeverityType.SUCCESS == result02.getResult());

        Element domElement_01 = null;;
        try
        {
            domElement_01 = DomUtil.createElementFromXmlFragment(XmlFragments.CONTACT_01, true);
        } 
        catch(Exception e)
        {
          
            fail(e.getMessage());
        }
        
        assertNotNull(domElement_01);
        
        NodeType node_01 = new NodeType();
        node_01.setAny(domElement_01);
        
        NodeArgumentType argument02 = new NodeArgumentType();
        argument02.setMetadataId(result01.getMetadataId());
        argument02.setPath("/MI_Metadata");
        argument02.setNodeType(node_01);
        argument02.setIndex(1);
        
        MetadataResultType result03 = this.service.insertNode(argument02);
        assertTrue( SeverityType.SUCCESS == result03.getResult());

        Element domElement_02 = null;;
        try
        {
            domElement_02 = DomUtil.createElementFromXmlFragment(XmlFragments.PHONE_PROPERTY_01, true);
        } 
        catch(Exception e)
        {
          
            fail(e.getMessage());
        }
        
        assertNotNull(domElement_02);
        
        NodeType node_02 = new NodeType();
        node_02.setAny(domElement_02);
        
        NodeArgumentType argument03 = new NodeArgumentType();
        argument03.setMetadataId(result01.getMetadataId());
        argument03.setPath("/MI_Metadata");
        argument03.setNodeType(node_02);
        argument03.setIndex(1);
        
        MetadataResultType result04 = this.service.insertNode(argument03);
        assertTrue( SeverityType.FAILURE == result04.getResult());

        assertNotNull( result04.getDiagnostic() );        
        assertNotNull( result04.getDiagnostic().getMesage() );
        System.out.println("Diag msg: " + result04.getDiagnostic().getMesage());

        Element domElement_03 = null;;
        try
        {
            domElement_03 = DomUtil.createElementFromXmlFragment(XmlFragments.MD_DATA_IDENTIFICATION_01, true);
        } 
        catch(Exception e)
        {
          
            fail(e.getMessage());
        }
        
        assertNotNull(domElement_03);

        NodeType node_03 = new NodeType();
        node_03.setAny(domElement_03);

        NodeArgumentType argument04 = new NodeArgumentType();
        argument04.setMetadataId(result01.getMetadataId());
        argument04.setPath("/MI_Metadata/identificationInfo[1]");
        argument04.setNodeType(node_03);

        MetadataResultType result05 = this.service.insertNode(argument04);
        assertTrue( SeverityType.SUCCESS == result05.getResult());

        NodeArgumentType argument05 = new NodeArgumentType();
        argument05.setMetadataId(result01.getMetadataId());
        argument05.setPath("/MI_Metadata/contact[0]/CI_ResponsibleParty/contactInfo/CI_Contact");
        argument05.setXmlFragment(XmlFragments.PHONE_PROPERTY_01);

        MetadataResultType result06 = this.service.insertNode(argument05);
        assertTrue( SeverityType.SUCCESS == result06.getResult());

        NodeArgumentType argument06 = new NodeArgumentType();
        argument06.setMetadataId(result01.getMetadataId());
        argument06.setPath("/MI_Metadata");
        argument06.setXmlFragment(XmlFragments.IDENTIFICATION_INFO_02);
        argument06.setIndex(2);

        MetadataResultType result07 = this.service.insertNode(argument06);
        assertTrue( SeverityType.SUCCESS == result07.getResult());

        NodeArgumentType argument07 = new NodeArgumentType();
        argument07.setMetadataId(result01.getMetadataId());
        argument07.setPath("/MI_Metadata");
        argument07.setXmlFragment(XmlFragments.DATE_STAMP_01);

        MetadataResultType result08 = this.service.insertNode(argument07);
        assertTrue( SeverityType.SUCCESS == result08.getResult());

        DownloadMetadataResultType result09 = this.service.downloadMD(result01.getMetadataId());
        assertTrue( SeverityType.SUCCESS == result09.getResult());

        MetadataCotainerType mdContainer02 = result09.getMetadataCotainerType();
        assertNotNull(mdContainer02);
        assertNotNull(mdContainer02.getMetadata());

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

        try
        {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            for( ModelElementType element : mdContainer02.getMetadata() )
            {
                Document doc = docBuilder.newDocument();
                Node domNode = doc.importNode(element.getAny(), true);
                doc.appendChild(domNode);

                IMdeElement model = null;
                model = Model.convert(doc, ModelType.GMI, ModelVersion.Beta, true);

                IMdeElement identificationInfo_01 = MdePath.findElement("/MI_Metadata/identificationInfo[1]", model, false);
                assertNotNull(identificationInfo_01);

                IMdeElement contact_01 = MdePath.findElement("/MI_Metadata/contact[1]", model, false);
                assertNotNull(contact_01);

                IMdeElement mdDataIdentification_01 = MdePath.findElement("/MI_Metadata/identificationInfo[1]/MD_DataIdentification", model, false);
                assertNotNull(mdDataIdentification_01);

                System.out.println(mdDataIdentification_01.getXmlFragment());

                IMdeElement phone_01 = MdePath.findElement("/MI_Metadata/contact[0]/CI_ResponsibleParty/contactInfo/CI_Contact/phone", model, false);
                assertNotNull(phone_01);

                System.out.println(phone_01.getXmlFragment());

                IMdeElement mdDataIdentification_02 = MdePath.findElement("/MI_Metadata/identificationInfo[2]/MD_DataIdentification", model, false);
                assertNotNull(mdDataIdentification_02);

                System.out.println(mdDataIdentification_02.getXmlFragment());

                IMdeElement mdDateStamp_01 = MdePath.findElement("/MI_Metadata/dateStamp", model, false);
                assertNotNull(mdDateStamp_01);

                System.out.println(mdDateStamp_01.getXmlFragment());
            }
        } 
        catch (Exception e)
        {
            fail(e.getMessage());
            e.printStackTrace();
        }

        MetadataResultType result10 = this.service.closeMD(result01.getMetadataId());
        assertTrue( SeverityType.SUCCESS == result10.getResult());
    }

    /**
     * Tests deleteNode()
     */
    public void testDeleteNode()
    {
        MetadataCotainerType mdContainer01 = new MetadataCotainerType();

        mdContainer01.setType(MetadataType.GMI);
        mdContainer01.setVersion(MetadataVersionType.BETA);
        mdContainer01.getMetadata().add(this.me1);
        mdContainer01.getMetadata().add(this.me2);
        mdContainer01.getMetadata().add(this.me3);
        mdContainer01.getMetadata().add(this.me4);

        MetadataResultType result01 = this.service.uploadMD(mdContainer01);
        assertTrue( SeverityType.SUCCESS == result01.getResult());

        String path01 = "/MI_Metadata/identificationInfo[0]/MD_DataIdentification/citation/CI_Citation/identifier[0]";
        
        MetadataResultType result02 = this.service.deleteNode(result01.getMetadataId(), path01);
        assertTrue( SeverityType.SUCCESS == result02.getResult());

        DownloadMetadataResultType result03 = this.service.downloadMD(result01.getMetadataId());
        assertTrue( SeverityType.SUCCESS == result03.getResult());

        MetadataCotainerType mdContainer02 = result03.getMetadataCotainerType();
        assertNotNull(mdContainer02);
        assertNotNull(mdContainer02.getMetadata());

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

        try
        {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            for( ModelElementType element : mdContainer02.getMetadata() )
            {
                Document doc = docBuilder.newDocument();
                Node node = doc.importNode(element.getAny(), true);
                doc.appendChild(node);

                IMdeElement model = null;
                model = Model.convert(doc, ModelType.GMI, ModelVersion.Beta, true);

                IMdeElement mdeElement = MdePath.findElement(MdePath.getParentPath(path01), model, true);     
                Map<String, List<?>> childrenMap = mdeElement.getChildListElements();

                assertTrue(childrenMap.containsKey("identifier"));
                List<?> identifidentifierList = childrenMap.get("identifier");
                assertNull(identifidentifierList);
            }
        } 
        catch (Exception e)
        {
            fail(e.getMessage());
            e.printStackTrace();
        }

        MetadataResultType result04 = this.service.deleteNode(result01.getMetadataId(), path01);
        assertTrue( SeverityType.SUCCESS == result04.getResult());

        MetadataResultType result05 = this.service.closeMD(result01.getMetadataId());
        assertTrue( SeverityType.SUCCESS == result05.getResult() );
    }

    /**
     * Tests setNodeValue()
     */
    public void testSetNodeValue()
    {
        MetadataCotainerType mdContainer01 = new MetadataCotainerType();

        mdContainer01.setType(MetadataType.GMI);
        mdContainer01.setVersion(MetadataVersionType.BETA);
        mdContainer01.getMetadata().add(this.me1);
        mdContainer01.getMetadata().add(this.me2);
        mdContainer01.getMetadata().add(this.me3);
        mdContainer01.getMetadata().add(this.me4);

        MetadataResultType result01 = this.service.uploadMD(mdContainer01);
        assertTrue( SeverityType.SUCCESS == result01.getResult());

        NodeValueArgumentType argument01 = new NodeValueArgumentType();
        argument01.setMetadataId(result01.getMetadataId());
        argument01.setPath("/MI_Metadata/contact[0]/CI_ResponsibleParty/contactInfo/CI_Contact/address/CI_Address/electronicMailAddress[0]/CharacterString");
        argument01.setNodeValue("value01");
        
        MetadataResultType result02 = this.service.setNodeValue(argument01);
        assertTrue( SeverityType.SUCCESS == result02.getResult());

        NodeValueArgumentType argument02 = new NodeValueArgumentType();
        argument02.setMetadataId(result01.getMetadataId());
        argument02.setPath("/MI_Metadata/dateStamp/DateTime");
        argument02.setNodeValue("2016-07-17T01:02:03.004Z");

        MetadataResultType result03 = this.service.setNodeValue(argument02);
        assertTrue( SeverityType.SUCCESS == result03.getResult());

        DownloadMetadataResultType result04 = this.service.downloadMD(result01.getMetadataId());
        assertTrue( SeverityType.SUCCESS == result04.getResult());

        MetadataCotainerType mdContainer02 = result04.getMetadataCotainerType();
        assertNotNull(mdContainer02);
        assertNotNull(mdContainer02.getMetadata());

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

        try
        {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            for( ModelElementType element : mdContainer02.getMetadata() )
            {
                Document doc = docBuilder.newDocument();
                Node node = doc.importNode(element.getAny(), true);
                doc.appendChild(node);

                IMdeElement model = null;
                model = Model.convert(doc, ModelType.GMI, ModelVersion.Beta, true);

                IMdeElement mdeElement01 = MdePath.findElement(argument01.getPath(), model, true);     
                Object objVal01 = mdeElement01.getElementValue();
                
                assertTrue(objVal01 instanceof String);
                String val01 = (String)objVal01;

                assertTrue(val01.equals(argument01.getNodeValue()));

                IMdeElement mdeElement02 = MdePath.findElement(argument02.getPath(), model, true);     
                Object objVal02 = mdeElement02.getElementValue();
                
                assertTrue(objVal02 instanceof XMLGregorianCalendar);
                XMLGregorianCalendar cal02 = (XMLGregorianCalendar)objVal02;
                String val02 = TimeUtil.toString(cal02);
                
                assertTrue(val02.equals(argument02.getNodeValue()));
            }
        } 
        catch (Exception e)
        {
            fail(e.getMessage());
            e.printStackTrace();
        }

        NodeValueArgumentType argument03 = new NodeValueArgumentType();
        argument03.setMetadataId(result01.getMetadataId());
        argument03.setPath("/MI_Metadata");
        argument03.setNodeValue("value03");

        MetadataResultType result05 = this.service.setNodeValue(argument03);
        assertTrue( SeverityType.FAILURE == result05.getResult());

        assertNotNull( result05.getDiagnostic() );        
        assertNotNull( result05.getDiagnostic().getMesage() );
        System.out.println("Diag msg: " + result05.getDiagnostic().getMesage());

        MetadataResultType result06 = this.service.closeMD(result01.getMetadataId());
        assertTrue( SeverityType.SUCCESS == result06.getResult() );
    }

    /**
     * Tests setNodeAttribute()
     */
    public void testSetNodeAttribute()
    {
        MetadataCotainerType mdContainer01 = new MetadataCotainerType();

        mdContainer01.setType(MetadataType.GMI);
        mdContainer01.setVersion(MetadataVersionType.BETA);
        mdContainer01.getMetadata().add(this.me1);
        mdContainer01.getMetadata().add(this.me2);
        mdContainer01.getMetadata().add(this.me3);
        mdContainer01.getMetadata().add(this.me4);

        MetadataResultType result01 = this.service.uploadMD(mdContainer01);
        assertTrue( SeverityType.SUCCESS == result01.getResult());

        NodeAttributeArgumentType argument01 = new NodeAttributeArgumentType();
        argument01.setMetadataId(result01.getMetadataId());
        argument01.setPath("/MI_Metadata/identificationInfo[0]/MD_DataIdentification/extent[0]/EX_Extent/temporalElement[0]/EX_TemporalExtent/extent/TimePeriod");
        argument01.setAttributeName("id");
        argument01.setAttributeValue("attribValue01");
        
        MetadataResultType result02 = this.service.setNodeAttribute(argument01);
        assertTrue( SeverityType.SUCCESS == result02.getResult());

        DownloadMetadataResultType result03 = this.service.downloadMD(result01.getMetadataId());
        assertTrue( SeverityType.SUCCESS == result03.getResult());

        MetadataCotainerType mdContainer02 = result03.getMetadataCotainerType();
        assertNotNull(mdContainer02);
        assertNotNull(mdContainer02.getMetadata());

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

        try
        {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            for( ModelElementType element : mdContainer02.getMetadata() )
            {
                Document doc = docBuilder.newDocument();
                Node node = doc.importNode(element.getAny(), true);
                doc.appendChild(node);

                IMdeElement model = null;
                model = Model.convert(doc, ModelType.GMI, ModelVersion.Beta, true);

                IMdeElement mdeElement01 = MdePath.findElement(argument01.getPath(), model, true);     
                Object objAttribVal01 = mdeElement01.getAttributeValue(argument01.getAttributeName());
                
                assertTrue(objAttribVal01 instanceof String);
                String attribVal01 = (String)objAttribVal01;

                assertTrue(attribVal01.equals(argument01.getAttributeValue()));
            }
        } 
        catch (Exception e)
        {
            fail(e.getMessage());
            e.printStackTrace();
        }

        NodeAttributeArgumentType argument02 = new NodeAttributeArgumentType();
        argument02.setMetadataId(result01.getMetadataId());
        argument02.setPath("/MI_Metadata/dateStamp/DateTime");
        argument02.setAttributeName("id");
        argument02.setAttributeValue("attribValue01");
        
        MetadataResultType result04 = this.service.setNodeAttribute(argument02);
        assertTrue( SeverityType.FAILURE == result04.getResult());

        assertNotNull( result04.getDiagnostic() );        
        assertNotNull( result04.getDiagnostic().getMesage() );
        System.out.println("Diag msg: " + result04.getDiagnostic().getMesage());

        MetadataResultType result05 = this.service.closeMD(result01.getMetadataId());
        assertTrue( SeverityType.SUCCESS == result05.getResult() );
    }

    /**
     * Tests addBuildingBlock() and getBuildingBlockInfoList
     */
    public void testAddBuildingBlockAndGetBuildingBlockInfoList()
    {
        BuildingBlockInfoType info01 = new BuildingBlockInfoType();
        info01.setName("Hello");
        info01.setType("greating");

        BuildingBlockArgumentType argument01 = new BuildingBlockArgumentType();
        argument01.setBuildingBlockInfoType(info01);
        argument01.setBody("<greating>Hello</greating>");

        BuildingBlockResultType result01 = this.service.addBuildingBlock(argument01);
        assertTrue(SeverityType.SUCCESS == result01.getResult());

        BuildingBlockInfoType info02 = new BuildingBlockInfoType();
        info02.setName("Hi");
        info02.setType("greating");

        BuildingBlockArgumentType argument02 = new BuildingBlockArgumentType();
        argument02.setBuildingBlockInfoType(info02);
        argument02.setBody("<greating>Hi</greating>");

        BuildingBlockResultType result02 = this.service.addBuildingBlock(argument02);
        assertTrue(SeverityType.SUCCESS == result02.getResult());

        GetBuildingBlockInfoListResultType result03 = this.service.getBuildingBlockInfoList();
        assertTrue( SeverityType.SUCCESS == result03.getResult());

        assertNotNull(result03.getBuildingBlockInfoType());
        assertTrue(2 == result03.getBuildingBlockInfoType().size());
    }
}
