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

package esa.obeos.metadataeditor.common.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class DomUtil
{
    private static String defaultNameSpaces = 
            " xmlns:gmi=\"http://www.isotc211.org/2005/gmi\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gml=\"http://www.opengis.net/gml/3.2\" xmlns:gmx=\"http://www.isotc211.org/2005/gmx\" xmlns:ows=\"http://www.opengis.net/ows/2.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
    
    public static void printDocument(final Document doc, final OutputStream out) throws IOException, TransformerException 
    {
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

    public static void printElement(final Element elm, final OutputStream out) throws IOException, TransformerException 
    {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(new DOMSource(elm), 
                new StreamResult(new OutputStreamWriter(out, "UTF-8")));
    }

    public static String convert(final Element element) throws NullPointerException, TransformerException
    {
        if( null == element )
        {
            throw new NullPointerException("connot convert null DOM Element");
        }
        
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer = transFactory.newTransformer();
        
        StringWriter stringWriter = new StringWriter();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(element), new StreamResult(stringWriter));
        
        return stringWriter.toString();
    }

    public static Document createDocumentFromXmlFragment(final InputStream inputStreamXmlFragment, final URL urlSchema) throws ParserConfigurationException, SAXException, IOException 
    {
        Document ret = null;
        
        if( null == inputStreamXmlFragment )
        {
            throw new NullPointerException("input stream XML fragment is null");
        }
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        if( null != urlSchema )
        {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(urlSchema);
            factory.setSchema(schema);
        }
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        
        ret = docBuilder.parse(inputStreamXmlFragment);
        inputStreamXmlFragment.close();
        return ret;
    }

    public static Document createDocumentFromElement(final Element element) throws ParserConfigurationException, SAXException, IOException 
    {
        Document ret = null;
        
        if( null == element )
        {
            throw new NullPointerException("input DOM element is null");
        }
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        ret = factory.newDocumentBuilder().newDocument();
        Node domNode = ret.importNode(element, true);
        ret.appendChild(domNode);

        return ret;
    }

    public static Document createDocumentFromElement(final Node node) throws ParserConfigurationException, SAXException, IOException 
    {
        Document ret = null;
        
        if( null == node )
        {
            throw new NullPointerException("input DOM node is null");
        }
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        ret = factory.newDocumentBuilder().newDocument();
        Node domNode = ret.importNode(node, true);
        ret.appendChild(domNode);

        return ret;
    }

    public static Document createDocumentFromXmlFragment(final InputStream inputStreamXmlFragment) throws ParserConfigurationException, SAXException, IOException
    {
        return createDocumentFromXmlFragment(inputStreamXmlFragment, null);
    }

    public static Document createDocumentFromXmlFragment(final String xmlFragment, final URL urlSchema) throws ParserConfigurationException, SAXException, IOException 
    {
        if( null == xmlFragment )
        {
            throw new NullPointerException("XML fragment is null");
        }
        
        StringBuilder xmlFragmentNs = new StringBuilder(xmlFragment);
        if( !hasNS(xmlFragment) )
        {
            xmlFragmentNs.insert(xmlFragmentNs.indexOf(">"), defaultNameSpaces);
        }
        
        return createDocumentFromXmlFragment(new ByteArrayInputStream(xmlFragmentNs.toString().getBytes()), urlSchema);
    }

    public static Document createDocumentFromXmlFragment(final String xmlFragment) throws ParserConfigurationException, SAXException, IOException
    {
        return createDocumentFromXmlFragment(xmlFragment, null);
    }

    public static Element createElementFromXmlFragment(final String xmlFragment, final URL urlSchema) throws ParserConfigurationException, SAXException, IOException 
    {
        Document doc = createDocumentFromXmlFragment(xmlFragment, urlSchema);
        return doc.getDocumentElement();
    }

    public static Element createElementFromXmlFragment(final String xmlFragment, final boolean addNameSpaces) throws ParserConfigurationException, SAXException, IOException 
    {
        return createElementFromXmlFragment(xmlFragment, null);
    }
    
    public static Element removeDefaultNameSpaceAttributes(final Element element)
    {
        Element ret = element;
        
        ret.removeAttribute("xmlns:gmi");
        ret.removeAttribute("xmlns:gco");
        ret.removeAttribute("xmlns:gmd");
        ret.removeAttribute("xmlns:gml");
        ret.removeAttribute("xmlns:gmx");
        ret.removeAttribute("xmlns:ows");
        ret.removeAttribute("xmlns:xsi");
        ret.removeAttribute("xmlns:xlink");
        
        return ret;
    }
    
    public static boolean hasNS(final String xmlFragment)
    {
        boolean ret = false;
        
        if( null != xmlFragment )
        {
            ret = xmlFragment.contains("xmlns:");
        }
        
        return ret;
    }
    
    public static Document load(final String pathFileName) throws ParserConfigurationException, SAXException, IOException
    {
    	File f = new File(pathFileName);
    	
    	FileInputStream fis = new FileInputStream(f);
    	
    	Document ret = createDocumentFromXmlFragment(fis);
    	
    	fis.close();
    	
    	return ret;
    }
}
