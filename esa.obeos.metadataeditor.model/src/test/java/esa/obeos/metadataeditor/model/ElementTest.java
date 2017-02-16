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

package esa.obeos.metadataeditor.model;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;

import esa.obeos.metadataeditor.model.api.IMdeElement;
import esa.obeos.metadataeditor.model.api.INotify;
import esa.obeos.metadataeditor.model.api.Model;
import esa.obeos.metadataeditor.model.api.ModelType;
import esa.obeos.metadataeditor.model.api.ModelVersion;

public class ElementTest
{
	private static IMdeElement metaData = null;
	
	@BeforeClass 
	public static void setupClass() throws Exception
	{
		String inputFileName = "2JCLX9A3_extr1";
		File inputFile = new File("src/test/resources/" + inputFileName);

		// load GMI file
		metaData = Model.load(inputFile, ModelType.GMI, ModelVersion.Beta, true);
		
		Assert.assertNotNull(metaData);
	}

	@Test
	public void test_getElementName()
	{		
		try 
		{
			String elementName = metaData.getXmlElementType();
			
			Assert.assertNotNull(elementName);
			Assert.assertFalse(elementName.isEmpty());
			
			System.out.println("metaData.getElementName(): " + elementName);		
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			Assert.fail(e.getMessage());
		} 
	}

    @Test
    public void test_getChildElements()
    {       
        try 
        {
            Map<String, IMdeElement> childElements = metaData.getChildElements();
            for (Entry<String, IMdeElement> e : childElements.entrySet())
            {
                System.out.println(e.getKey() + ", " + e.getValue());
            }
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } 
    }

    @Test
    public void test_getChildListElements()
    {       
        try 
        {
            Map<String, List<?>> childListElements = metaData.getChildListElements();
            for (Entry<String, List<?>> e : childListElements.entrySet())
            {
                System.out.println(e.getKey() + ", " + e.getValue());
            }
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } 
    }

    @Test
    public void test_elementTraverse()        
    {
        INotify notify = new TestNotifyImpl(true); 
                
        String inputFileName = "2JCLX9A3_extr1";

        IMdeElement metaData = null; 

        try 
        {
            File inputFile = new File("src/test/resources/" + inputFileName);

            // load GMI file
            metaData = Model.load(inputFile, ModelType.GMI, ModelVersion.Beta, true);

            Assert.assertNotNull(metaData);

            // traverse GMI
            metaData.traverse("metaData", notify);
            
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } 
    }

    
	@Ignore
    public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(new DOMSource(doc), 
             new StreamResult(new OutputStreamWriter(out, "UTF-8")));
    }
}
