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

package semanticannot;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import global.Master;


public class OSDDProcessor 
{
	/*
	 * Example OSDD file for NASA GCMD Thesaurus open-search 
	 * -------------------------------------------------------
	   <?xml version="1.0" encoding="UTF-8"?>
       <OpenSearchDescription xmlns="http://a9.com/-/spec/opensearch/1.1/">
         <Url type="application/rdf+xml" template=" http://gcmdservices.gsfc.nasa.gov/kms/concepts/pattern/{searchTerms}?format=rdf" /> 
         <Language>en</Language>
       </OpenSearchDescription>
	 *-------------------------------------------------------
	 */
	
	private final static int DEBUG_LEVEL = 1;
	
	private final static String OS_NS = "http://a9.com/-/spec/opensearch/1.1/";
	private final static String OS_TEMPLATE_STR = "template";
	
	/** Characters in  "ShortName". */ 
	public String shortName;
	
	/** Characters in  "Description". */ 
	public String description;
	
	/** Is the current tag being parsed a "ShortName". */ 
	private boolean isShortName;
	
	/** Is the current tag being parsed a "Description". */ 
	private boolean isDescription;

	/** template attribute in the URL element */
	public String urlTemplateAttribute;
	
	private class SAXOSDDParser extends DefaultHandler
	{
		// CAUTION: this is NOT a production-grade parser
		
		/** NS Mode applies only to attributes ! 
		 *  For production use, this would need to extended everywhere.
		 */
		private boolean isNS_mode = true;
		
		SAXOSDDParser(boolean nsMode)
		{
			super();
			setNS_mode(nsMode);
		}
		
		@Override
		public void startDocument() throws SAXException
		{
			if (Master.DEBUG_LEVEL > Master.LOW) System.out.println("Parsing osdd - start");
			urlTemplateAttribute = "";
			isDescription = false;
			isShortName = false;
			description = "";
			shortName = "";
	    }
		
		@SuppressWarnings("unused")
		@Override
		public void startElement(
				String uri,
				String localName,
				String qName,
				Attributes attributes)
				throws SAXException 
		{
			if (Master.DEBUG_LEVEL > Master.LOW) 
			{
				System.out.println("             - "+localName);
				if (attributes.getLength() > 0)
					System.out.println("  -- Attributes["+attributes.getLength()+"]: ");
				
				for(int i = 0; i < attributes.getLength(); i++)
				{
					String attrName  = attributes.getLocalName(i);
					String attrValue = attributes.getValue(i);
					if (Master.DEBUG_LEVEL > Master.LOW) System.out.println("                  " + attrName + ": " + attrValue);
				}
			}
			
			if (localName.equals("ShortName"  ))  isShortName   = true;
			if (localName.equals("Description"))  isDescription = true;	
			if (localName.equals("Url"))          startURL(attributes);	
			
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException 
		{		
			if (isShortName  ) shortName   = new String(ch, start, length);
			if (isDescription) description = new String(ch, start, length);
		}

		@Override
		public void endElement(
				String uri, 
				String localName, 
				String qName) throws SAXException 
		{
			if (localName.equals("ShortName"  ))  isShortName   = false;
			if (localName.equals("Description"))  isDescription = false;			
		}

		
		@Override
		public void endDocument() throws SAXException
		{
			if (Master.DEBUG_LEVEL > Master.LOW) System.out.println("Parsing osdd - end");
	    }
		
		private void startURL(Attributes attributes)
		{
			if (Master.DEBUG_LEVEL > Master.LOW) System.out.println("parsing...startURL");
			urlTemplateAttribute = findAttribute(attributes, OS_NS, OS_TEMPLATE_STR);
			if (Master.DEBUG_LEVEL > Master.LOW) System.out.println("Found OS_TEMPLATE: "+urlTemplateAttribute);
		}

		private String findAttribute(
				Attributes attributes, 
				String ns, 
				String attributeName)
		{
			if (isNS_mode)
			{
				return attributes.getValue(ns, attributeName);
			}
			else
			{
				for(int i = 0; i < attributes.getLength(); i++)
				{
					if (attributes.getLocalName(i) == attributeName)
					{
						return attributes.getValue(i);
					}
				}
			}
			return null;
		}

		public void setNS_mode(boolean isNS_mode) {
			this.isNS_mode = isNS_mode;
		}

	}
	
	public void processOSDD(String osddXml) 
			throws ParserConfigurationException, SAXException, IOException 
	{
		processOSDD(osddXml, true);
	}
	
	public void processOSDD(String osddXml, boolean isNS_mode) 
			throws ParserConfigurationException, SAXException, IOException 
	{		
		StringReader strRdr = new StringReader(osddXml);
		SAXParserFactory spf = SAXParserFactory.newInstance();
	    spf.setNamespaceAware(true);
	    SAXParser saxParser = spf.newSAXParser();
	    XMLReader xmlReader = saxParser.getXMLReader();
	    xmlReader.setContentHandler(new SAXOSDDParser(isNS_mode));
	    xmlReader.parse(new InputSource(strRdr));
	    
	}

	public String getShortName() {
		return shortName;
	}

	public String getDescription() {
		return description;
	}

	public String getUrlTemplateAttribute() {
		return urlTemplateAttribute;
	}

}
