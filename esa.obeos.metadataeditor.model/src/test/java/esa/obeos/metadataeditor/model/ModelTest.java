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

package esa.obeos.metadataeditor.model;


import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.MarshalException;
import javax.xml.namespace.QName;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;

import esa.obeos.metadataeditor.common.data.XmlFragments;
import esa.obeos.metadataeditor.common.util.DomUtil;
import esa.obeos.metadataeditor.common.util.TimeUtil;
import esa.obeos.metadataeditor.model.api.IMdeElement;
import esa.obeos.metadataeditor.model.api.MdePath;
import esa.obeos.metadataeditor.model.api.Model;
import esa.obeos.metadataeditor.model.api.ModelType;
import esa.obeos.metadataeditor.model.api.ModelVersion;
import esa.obeos.metadataeditor.model.xsd.beta.gco.CharacterStringPropertyType;
import esa.obeos.metadataeditor.model.xsd.beta.gco.DatePropertyType;
import esa.obeos.metadataeditor.model.xsd.beta.gco.ObjectFactory;
import esa.obeos.metadataeditor.model.xsd.beta.gmd.CIAddressType;
import esa.obeos.metadataeditor.model.xsd.beta.gmd.CIContactPropertyType;
import esa.obeos.metadataeditor.model.xsd.beta.gmd.CIContactType;
import esa.obeos.metadataeditor.model.xsd.beta.gmd.CIResponsiblePartyPropertyType;
import esa.obeos.metadataeditor.model.xsd.beta.gmd.MDIdentificationPropertyType;
import esa.obeos.metadataeditor.model.xsd.beta.gmi.MIMetadataType;

public class ModelTest 
{
    @BeforeClass 
    public static void setupClass()
    {
    }

    @Test
    public void test_editFile_2JCLX9A3_extr1_betaModel()
    {
        String inputFileName = "2JCLX9A3_extr1";
        String outputFileName = "2JCLX9A3_extr1_edited.xml";

        IMdeElement metaData = null;

        try 
        {
            File inputFile = new File("src/test/resources/" + inputFileName);
            File outputFile = new File("/tmp/" + outputFileName);

            // load GMI file
            metaData = Model.load(inputFile, ModelType.GMI, ModelVersion.Beta, true);

            Assert.assertNotNull(metaData);

            // get GCO object factory
            ObjectFactory gcoObjectFactory = new ObjectFactory();

            Assert.assertTrue(metaData instanceof MIMetadataType);
            
            MIMetadataType miMetaData = (MIMetadataType)metaData; 

            // change something in the meta data
            List<CIResponsiblePartyPropertyType> contacts = miMetaData.getContact();
            for (CIResponsiblePartyPropertyType contact : contacts)
            {
                CIContactPropertyType contactProp = contact.getCIResponsibleParty().getContactInfo();
                CIContactType ci = contactProp.getCIContact();
                CIAddressType address = ci.getAddress().getCIAddress();

                System.out.println(address.getCity().getCharacterString().getValue());
                System.out.println(address.getCountry().getCharacterString().getValue());

                // create an element
                CharacterStringPropertyType area = gcoObjectFactory.createCharacterStringPropertyType();

                area.setCharacterString(gcoObjectFactory.createCharacterString("A001"));

                // add the new element to the meta data
                address.setAdministrativeArea(area);
            }

            // save the GMI model to a file
            Model.save(outputFile, metaData, true);

            // load the GMI model from the file
            IMdeElement metaData2 = Model.load(outputFile, ModelType.GMI, ModelVersion.Beta, true);

            Assert.assertNotNull(metaData2);

            Document doc = Model.convert(metaData2, true);

            DomUtil.printDocument(doc, System.out);

            IMdeElement metaData3 = Model.convert(doc, ModelType.GMI, ModelVersion.Beta, true);

            // save GMI file
            Model.save(new File("/tmp/domed_beta.xml"), metaData3, true);

        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } 
    }

    @Ignore
    public void test_createFile_rel2015Model()
    {
        String outputFileName = "rel2015Model_created.xml";

        IMdeElement metaData = null; 

        try 
        {
            File outputFile = new File("/tmp/" + outputFileName);

            // create GMI file
            metaData = Model.create(ModelType.GMI, ModelVersion.Rel2015);

            Assert.assertNotNull(metaData);

            Assert.assertTrue(metaData instanceof esa.obeos.metadataeditor.model.xsd.rel2015.gmi.MIMetadataType);
            
            esa.obeos.metadataeditor.model.xsd.rel2015.gmi.MIMetadataType miMetaData = 
                    (esa.obeos.metadataeditor.model.xsd.rel2015.gmi.MIMetadataType)metaData; 

            // get GCO object factory
            esa.obeos.metadataeditor.model.xsd.rel2015.gco.ObjectFactory gcoObjectFactory = 
                    new esa.obeos.metadataeditor.model.xsd.rel2015.gco.ObjectFactory();

            // get GMD object factory
            esa.obeos.metadataeditor.model.xsd.rel2015.gmd.ObjectFactory gmdObjectFactory = 
                    new esa.obeos.metadataeditor.model.xsd.rel2015.gmd.ObjectFactory();

            miMetaData.setId("TestDocument");
            
            // fileIdentifier
            esa.obeos.metadataeditor.model.xsd.rel2015.gco.CharacterStringPropertyType fileIdentifier = gcoObjectFactory.createCharacterStringPropertyType();
            
            JAXBElement<String> fileIdentifierValue = new JAXBElement<String>(new QName("http://www.isotc211.org/2005/gco",
                    "CharacterString"), String.class, "rel2015Model_created");
            fileIdentifier.setCharacterString(fileIdentifierValue);
            miMetaData.setFileIdentifier(fileIdentifier);

            // language
            esa.obeos.metadataeditor.model.xsd.rel2015.gco.CharacterStringPropertyType language = gcoObjectFactory.createCharacterStringPropertyType();
            
            JAXBElement<String> languageValue = new JAXBElement<String>(new QName("http://www.isotc211.org/2005/gco",
                    "CharacterString"), String.class, "en");
            fileIdentifier.setCharacterString(languageValue);
            miMetaData.setLanguage(language);
            
            // characterSet
            esa.obeos.metadataeditor.model.xsd.rel2015.gmd.MDCharacterSetCodePropertyType characterSet = gmdObjectFactory.createMDCharacterSetCodePropertyType();
            
            esa.obeos.metadataeditor.model.xsd.rel2015.gco.CodeListValueType characterSetValue = 
                    new esa.obeos.metadataeditor.model.xsd.rel2015.gco.CodeListValueType();
            characterSetValue.setValue("en");
            characterSetValue.setCodeList("de,en,fr");
            miMetaData.setCharacterSet(characterSet);
            
            
            // contact          
            esa.obeos.metadataeditor.model.xsd.rel2015.gmd.CIResponsiblePartyType ciResponsible = gmdObjectFactory.createCIResponsiblePartyType();

            JAXBElement<String> individualNameValue = new JAXBElement<String>(new QName("http://www.isotc211.org/2005/gco",
                    "CharacterString"), String.class, "Rudolf Von Zeppelin");

            esa.obeos.metadataeditor.model.xsd.rel2015.gco.CharacterStringPropertyType individualName = gcoObjectFactory.createCharacterStringPropertyType();
            individualName.setCharacterString(individualNameValue);
            ciResponsible.setIndividualName(individualName);

            esa.obeos.metadataeditor.model.xsd.rel2015.gmd.CIRoleCodePropertyType role = 
                    new esa.obeos.metadataeditor.model.xsd.rel2015.gmd.CIRoleCodePropertyType();
            esa.obeos.metadataeditor.model.xsd.rel2015.gco.CodeListValueType roleValue = 
                    new esa.obeos.metadataeditor.model.xsd.rel2015.gco.CodeListValueType();
            roleValue.setValue("pilot");
            roleValue.setCodeList("de,en,fr");
            role.setCIRoleCode(roleValue);
            ciResponsible.setRole(role);
            
            esa.obeos.metadataeditor.model.xsd.rel2015.gmd.CIResponsiblePartyPropertyType ciResponsibleProperty = gmdObjectFactory.createCIResponsiblePartyPropertyType();

            ciResponsibleProperty.setTitle("From the New World");
            
            miMetaData.getContact().add(ciResponsibleProperty);
            
            esa.obeos.metadataeditor.model.xsd.rel2015.gco.DatePropertyType datePropertyType = gcoObjectFactory.createDatePropertyType();
            datePropertyType.setDateOrDateTime("2009-09-10");
            
            miMetaData.setDateStamp(datePropertyType);
            
            // save the GMI model to a file
            Model.save(outputFile, metaData, true);

            Document doc = Model.convert(metaData, true);

            DomUtil.printDocument(doc, System.out);
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } 
    }

    @Test
    public void test_editFile_2JCLX9A3_extr1_corrupted_betaModel()
    {
        String inputFileName = "2JCLX9A3_extr1_corrupted";

        IMdeElement metaData = null; 

        Exception exc = null;

        try 
        {
            File inputFile = new File("src/test/resources/" + inputFileName);

            // try to load corrupted GMI file
            metaData = Model.load(inputFile, ModelType.GMI, ModelVersion.Beta, true);
        } 
        catch (Exception e) 
        {
            // it is OK - corrupted cannot be validated
            exc = e;
        } 

        Assert.assertNotNull(exc);
        Assert.assertNull(metaData);

        try 
        {
            File inputFile = new File("src/test/resources/" + inputFileName);

            // try to load corrupted GMI file w/o validation
            metaData = Model.load(inputFile, ModelType.GMI, ModelVersion.Beta, false);

            Assert.assertNotNull(metaData);
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        } 

        Assert.assertNotNull(metaData);
    }

    @Test
    public void test_editFile_2JCLX9A3_extr1_validate_betaModel()
    {
        String inputFileName = "2JCLX9A3_extr1";

        IMdeElement metaData = null; 

        try 
        {
            File inputFile = new File("src/test/resources/" + inputFileName);

            // try to load the GMI file w/ validation
            metaData = Model.load(inputFile, ModelType.GMI, ModelVersion.Beta, true);

            Assert.assertNotNull(metaData);

            // validate the correct model
            Model.validate(metaData);

            IMdeElement dateStamp = null;

            dateStamp = metaData.getChildByXmlName("dateStamp");

            // remove a required element (invalidate the model)
            metaData.deleteChild(dateStamp);

            Exception exc = null;
            try 
            {
                // validate the incorrect model
                Model.validate(metaData);
            }
            catch (MarshalException e) 
            {
                // OK
                exc = e;
            }
            
            assertNotNull(exc);
            
        } 
        catch (Throwable t) 
        {
            fail(t.getMessage());
        }
    }

    @Test
    public void test_childInsertion_01()
    {
        String inputFileName_01 = "2JCLX9A3_extr1";
        String outputFileName_01 = "insertedContact_01.xml";

        IMdeElement metaData = null;

        try 
        {
            File inputFile_01 = new File("src/test/resources/" + inputFileName_01);
            File outputFile_01 = new File("/tmp/" + outputFileName_01);

            // load GMI file
            metaData = Model.load(inputFile_01, ModelType.GMI, ModelVersion.Beta, true);

            Assert.assertNotNull(metaData);

            IMdeElement originalContact_01 = MdePath.findElement("/MI_Metadata/contact[0]", metaData, false);
            if( null != originalContact_01 )
            {
                metaData.deleteChild(originalContact_01);
            }
            
            Document docNewContact_01 = DomUtil.createDocumentFromXmlFragment(XmlFragments.CONTACT_01);
            assertNotNull(docNewContact_01);

            DomUtil.printDocument(docNewContact_01, System.out);
            
            JAXBElement<CIResponsiblePartyPropertyType> jaxbElement_01 = Model.convert(docNewContact_01, CIResponsiblePartyPropertyType.class, false);
            assertNotNull(jaxbElement_01);
            assertTrue(jaxbElement_01.getValue() instanceof IMdeElement);
            
            IMdeElement newContact_01 = (IMdeElement)jaxbElement_01.getValue();
            
            IMdeElement originalContact_02 = metaData.insertChild(newContact_01, "contact", 0, null);
            assertNull(originalContact_02);
            
            // save the GMI model to a file
            Model.save(outputFile_01, metaData, true);
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } 
    }

    @Test
    public void test_childInsertion_02()
    {
        String inputFileName_01 = "2JCLX9A3_extr1";
        String outputFileName_01 = "insertedIdentificationInfo_01.xml";

        IMdeElement metaData = null;

        try 
        {
            File inputFile_01 = new File("src/test/resources/" + inputFileName_01);
            File outputFile_01 = new File("/tmp/" + outputFileName_01);

            // load GMI file
            metaData = Model.load(inputFile_01, ModelType.GMI, ModelVersion.Beta, true);

            Assert.assertNotNull(metaData);

            Document docNewIdentificationInfo_01 = DomUtil.createDocumentFromXmlFragment(XmlFragments.IDENTIFICATION_INFO_01);
            assertNotNull(docNewIdentificationInfo_01);

            DomUtil.printDocument(docNewIdentificationInfo_01, System.out);
            
            JAXBElement<MDIdentificationPropertyType> jaxbElement_01 = Model.convert(docNewIdentificationInfo_01, MDIdentificationPropertyType.class, false);
            assertNotNull(jaxbElement_01);
            assertTrue(jaxbElement_01.getValue() instanceof IMdeElement);
            
            IMdeElement newIdentificationInfo_01 = (IMdeElement)jaxbElement_01.getValue();

            
            IMdeElement originalIdentificationInf_01 = metaData.insertChild(newIdentificationInfo_01, "identificationInfo", 1, null);
            assertNull(originalIdentificationInf_01);
            
            // save the GMI model to a file
            Model.save(outputFile_01, metaData, true);
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } 
    }

    @Test
    public void test_childInsertion_03()
    {
        String inputFileName_01 = "2JCLX9A3_extr1";
        String outputFileName_01 = "insertedChildren_01.xml";

        IMdeElement metaData = null;

        try 
        {
            File inputFile_01 = new File("src/test/resources/" + inputFileName_01);
            File outputFile_01 = new File("/tmp/" + outputFileName_01);

            // load GMI file
            metaData = Model.load(inputFile_01, ModelType.GMI, ModelVersion.Beta, true);
            Assert.assertNotNull(metaData);

            Object originalContact_01 = metaData.insertChild(XmlFragments.CONTACT_01, "contact", 1, null);
            assertNull(originalContact_01);

            IMdeElement parent_01 = MdePath.findElement("/MI_Metadata/identificationInfo[0]", metaData, false);
            
            Object originalMdDataIdentification_01 = parent_01.insertChild(XmlFragments.MD_DATA_IDENTIFICATION_01, "abstractMDIdentification", null, null);
            assertNotNull(originalMdDataIdentification_01);
            
            // save the GMI model to a file
            Model.save(outputFile_01, metaData, true);
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } 
    }

    @Test
    public void test_dateStampInsertion_01()
    {
        String inputFileName_01 = "2JCLX9A3_extr1";
        String outputFileName_01 = "inserteddataStamp_01.xml";

        IMdeElement metaData = null;

        try 
        {
            File inputFile_01 = new File("src/test/resources/" + inputFileName_01);
            File outputFile_01 = new File("/tmp/" + outputFileName_01);

            // load GMI file
            metaData = Model.load(inputFile_01, ModelType.GMI, ModelVersion.Beta, true);
            Assert.assertNotNull(metaData);

            DatePropertyType datePropertyType = new DatePropertyType();
            datePropertyType.setFieldName("dateStamp");
            datePropertyType.setDateOrDateTime(TimeUtil.convert(new Date()));
            
            Object originalContact_01 = metaData.insertChild(datePropertyType, datePropertyType.getFieldName(), null, null);
            assertNotEquals(originalContact_01, datePropertyType);

            IMdeElement dateStamp_01 = MdePath.findElement("/MI_Metadata/dateStamp", metaData, false);
            assertNotNull(dateStamp_01);
            assertEquals(dateStamp_01, datePropertyType);
            
            System.out.println(dateStamp_01.getXmlFragment());
            
            // save the GMI model to a file
            Model.save(outputFile_01, metaData, true);
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } 
    }

    @Test
    public void test_getXmlFragment_01()
    {
        String inputFileName_01 = "2JCLX9A3_extr1";

        try 
        {
            File inputFile_01 = new File("src/test/resources/" + inputFileName_01);

            // load GMI file
            IMdeElement element_01 = Model.load(inputFile_01, ModelType.GMI, ModelVersion.Beta, true);
            Assert.assertNotNull(element_01);

            String xmlFragment_01 = element_01.getXmlFragment();
            assertNotNull(xmlFragment_01);
            
            System.out.println(xmlFragment_01);
            
            IMdeElement element_02 = MdePath.findElement("/MI_Metadata/identificationInfo[0]", element_01, false);
            
            String xmlFragment_02 = element_02.getXmlFragment();
            assertNotNull(xmlFragment_02);

            System.out.println(xmlFragment_02);


            IMdeElement element_03 = MdePath.findElement("/MI_Metadata/dateStamp", element_01, false);
            
            String xmlFragment_03 = element_03.getXmlFragment();
            assertNotNull(xmlFragment_03);

            System.out.println(xmlFragment_03);

            Map<String, IMdeElement> map = element_03.getChildElements();
            
            for (IMdeElement e : map.values())
            {
                e.getXmlElementName();
            }
            
            IMdeElement element_04 = MdePath.findElement("/MI_Metadata/dateStamp/DateTime", element_01, false);
            assertNotNull(element_04);
            
            String xmlFragment_04 = element_04.getXmlFragment();
            assertNotNull(xmlFragment_04);

            System.out.println(xmlFragment_04);
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } 
    }
   

    @Test
    public void test_getPath_01()
    {
        String inputFileName_01 = "2JCLX9A3_extr1";

        IMdeElement metaData = null;

        try 
        {
            File inputFile_01 = new File("src/test/resources/" + inputFileName_01);

            // load GMI file
            metaData = Model.load(inputFile_01, ModelType.GMI, ModelVersion.Beta, true);

            Assert.assertNotNull(metaData);

            String path_01 = "/MI_Metadata/contact[0]";
            
            IMdeElement child_01 = MdePath.findElement(path_01, metaData, false);
            Assert.assertNotNull(child_01);

            String path_02 = child_01.getPath();
            Assert.assertEquals(path_01, path_02);

            String path_03 = "/MI_Metadata/identificationInfo[0]/MD_DataIdentification/citation/CI_Citation/identifier[0]";
            
            IMdeElement child_02 = MdePath.findElement(path_03, metaData, false);
            Assert.assertNotNull(child_02);

            String path_04 = child_02.getPath();
            Assert.assertEquals(path_03, path_04);

            String path_05 = "/MI_Metadata/contact[0]/CI_ResponsibleParty/contactInfo/CI_Contact/address/CI_Address/electronicMailAddress[0]/CharacterString";
            
            IMdeElement child_03 = MdePath.findElement(path_05, metaData, false);
            Assert.assertNotNull(child_03);

            String path_06 = child_03.getPath();
            Assert.assertEquals(path_05, path_06);

        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } 
    }

    @Test
    public void test_getCreatableChildNames_01()
    {
        String inputFileName_01 = "2JCLX9A3_extr1";

        IMdeElement metaData = null;

        try 
        {
            File inputFile_01 = new File("src/test/resources/" + inputFileName_01);

            // load GMI file
            metaData = Model.load(inputFile_01, ModelType.GMI, ModelVersion.Beta, true);

            Assert.assertNotNull(metaData);

            String path_01 = "/MI_Metadata/dateStamp";

            IMdeElement child_01 = MdePath.findElement(path_01, metaData, false);
            Assert.assertNotNull(child_01);

            String path_02 = child_01.getPath();
            Assert.assertEquals(path_01, path_02);

            String path_03 = "/MI_Metadata/dateStamp/DateTime";

            IMdeElement child_02 = MdePath.findElement(path_03, metaData, false);
            Assert.assertNotNull(child_02);

            child_01.deleteChild(child_02);

            List<String> createbleChildrenList_01 = child_01.getCreatableChildNames(false);
            Assert.assertNotNull(createbleChildrenList_01);
            Assert.assertFalse(createbleChildrenList_01.isEmpty());
            
            System.out.println(createbleChildrenList_01.toString());
            
            List<String> createbleChildrenList_02 = child_01.getCreatableChildNames(true);
            Assert.assertNotNull(createbleChildrenList_02);
            Assert.assertTrue(createbleChildrenList_01.size() < createbleChildrenList_02.size());
            
            System.out.println(createbleChildrenList_02.toString());

            String path_04 = "/MI_Metadata/identificationInfo[0]";
            
            IMdeElement child_03 = MdePath.findElement(path_04, metaData, false);
            Assert.assertNotNull(child_03);

            String path_05 = "/MI_Metadata/identificationInfo[0]/MD_DataIdentification";

            IMdeElement child_04 = MdePath.findElement(path_05, metaData, false);
            Assert.assertNotNull(child_04);

            child_03.deleteChild(child_04);

            List<String> createbleChildrenList_03 = child_03.getCreatableChildNames(true);
            Assert.assertNotNull(createbleChildrenList_03);
            Assert.assertFalse(createbleChildrenList_03.isEmpty());

            System.out.println(createbleChildrenList_03.toString());
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } 
    }

    @Test
    public void test_loadXmlFragment_NoNS_01()
    {
        String inputFileName_01 = "model_fragment_01_NoNS.xml";

        FileInputStream fis = null;
 
        try 
        {
            File inputFile_01 = new File("src/test/resources/" + inputFileName_01);

            fis = new FileInputStream(inputFile_01);

            // load GMI file
            IMdeElement element_01 = Model.load(fis, ModelType.GMI, ModelVersion.Beta, false);
            Assert.assertNotNull(element_01);

        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        finally 
        {
            if (null != fis)
            {
                try
                {
                    fis.close();
                }
                catch (IOException e)
                {
                    // do nothing
                }
                fis = null;
            }
        }
    }

    @Test
    public void test_loadXmlFragment_WithNS_01()
    {
        String inputFileName_01 = "2JCLX9A3_extr1";

        FileInputStream fis = null;
 
        try 
        {
            File inputFile_01 = new File("src/test/resources/" + inputFileName_01);

            fis = new FileInputStream(inputFile_01);

            // load GMI file
            IMdeElement element_01 = Model.load(fis, ModelType.GMI, ModelVersion.Beta, false);
            Assert.assertNotNull(element_01);

        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        finally 
        {
            if (null != fis)
            {
                try
                {
                    fis.close();
                }
                catch (IOException e)
                {
                    // do nothing
                }
                fis = null;
            }
        }
    }

    @Test
    public void test_visitor_capability()
    {
        String inputFileName = "2JCLX9A3_extr1";

        IMdeElement metaData = null;

        try 
        {
            File inputFile = new File("src/test/resources/" + inputFileName);

            // load GMI file
            metaData = Model.load(inputFile, ModelType.GMI, ModelVersion.Beta, true);

            Assert.assertNotNull(metaData);

            CollectElementsNamesVisitor collectElementsNamesVisitor = new CollectElementsNamesVisitor();
            metaData.accept(collectElementsNamesVisitor);

            assertFalse(collectElementsNamesVisitor.getElementsNames().isEmpty());

            collectElementsNamesVisitor.dump(System.out);

            CountElementsVisitor countElementsVisitor = new CountElementsVisitor();
            metaData.accept(countElementsVisitor);

            assertTrue(countElementsVisitor.getElementCount() > 0);

            countElementsVisitor.dump(System.out);
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } 
    }
}
