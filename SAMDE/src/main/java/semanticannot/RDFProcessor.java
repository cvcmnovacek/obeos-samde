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
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import global.Master;


public class RDFProcessor 
{
	/*
	 * Namespaces:
	 * xmlns:foaf="http://xmlns.com/foaf/0.1/" 
	 * xmlns:void="http://rdfs.org/ns/void#" 
	 * xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
	 * xmlns:dcterms="http://purl.org/dc/terms/" 
	 */

	/* Sample file input GCMD
	 * ----------------------
	 * 
	  <void:Dataset rdf:about="http://gcmdservices.gsfc.nasa.gov/kms/concept/">
	    <dcterms:description>GCMD KMS</dcterms:description>
	    <void:uriLookupEndpoint rdf:resource=""/>
	    <void:openSearchDescription rdf:resource="http://localhost/ontology/gcmd-osdd.xml"/>
	    <void:feature rdf:resource="http://www.w3.org/ns/formats/RDF_XML" />
	    <void:rootResource 
	         rdf:resource="http://gcmdservices.gsfc.nasa.gov/kms/concept/1eb0ea0a-312c-4d74-8d42-6f1ad7581999" />
	    <void:uriSpace>http://gcmdservices.gsfc.nasa.gov/kms/concept/</void:uriSpace>
	        <foaf:account>
	                <foaf:OnlineAccount>
	                        <foaf:accountName>nqminh</foaf:accountName>
	                </foaf:OnlineAccount>
	        </foaf:account>
	  </void:Dataset>
	 * ----------------------
	 *
	 * Sample file input GMET
	 * ----------------------
	 *  
	 *  <void:Dataset rdf:about="http://www.eionet.europa.eu/gemet">
          <dcterms:description>GEMET</dcterms:description>
          <void:uriLookupEndpoint rdf:resource="http://www.eionet.europa.eu/gemet/rdf/getConcept?concept_uri="/>
          <void:feature rdf:resource="http://www.w3.org/ns/formats/RDF_XML" />
          <void:rootResource 
              rdf:resource="http://www.eionet.europa.eu/gemet/getTopmostConcepts?thesaurus_uri=http://www.eionet.europa.eu/gemet/group/"/>
          <void:uriSpace>http://www.eionet.europa.eu/gemet</void:uriSpace>
        </void:Dataset>
	 * ----------------------
	 *  
	 */
	
	//private final static int DEBUG_LEVEL = 2;
	
	/** The map of datasets found in the input file 
	 *  key = uriSpace
	 *  This means the file should not contain two entries 
	 *  with the same uriSpace.
	 */
	public Map<String,RDFDataset> dataSets = new HashMap<String,RDFDataset>();
	
	private class SAXRDFParser extends DefaultHandler
	{
		/** Namespaces */
		public final String RDF_NAMESPACE            = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
		@SuppressWarnings("unused")
		public final String VOID_NAMESPACE           = "http://rdfs.org/ns/void#";
		
		/** element and attribute names */
		private final String RDF_RDF_STR              = "RDF"; 
		private final String RDF_ABOUT_STR            = "about";
		private final String RDF_RESOURCE_STR         = "resource";
		private final String VOID_DATASET_STR         = "Dataset";
		private final String VOID_OSD_STR             = "openSearchDescription";
		private final String VOID_URISPACE_STR        = "uriSpace";
		private final String VOID_URILOOKUPENDPT_STR  = "uriLookupEndpoint";
		private final String DCTERMS_DESCRIPTION_STR  = "description";
		
		/** Characters in  "uriSpace". */ 
		public String uriSpaceChars;
		
		/** Value of rdf:resource in tag uriLookupEndpoint */
		private String uriLookupEndpointResource;
		
		/** Characters in  "Description". */ 
		public String descriptionChars;
		
		/** void:openSearchDescription resource attribute */ 
		public String osdResource;
		
		/** void:Dataset about attribute */ 
		public String aboutAttr;
		
		private boolean foundRDF = false;
		
		/** Is the current tag being parsed a "uriSpace". */ 
		private boolean isUriSpace;
		
		/** Is the current tag being parsed a "Description". */ 
		private boolean isDescription;
		
		private RDFDataset currentDataset = null;
		
		@SuppressWarnings("unused")
        @Override
		public void startDocument() throws SAXException
		{
			if (Master.DEBUG_LEVEL > Master.LOW) System.out.println("Parsing VOID/RDF file - start");
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
				System.out.println("    - "+localName);
				if (attributes.getLength() > 0)
					System.out.println("      -- Attributes["+attributes.getLength()+"]: ");
				
				for(int i = 0; i < attributes.getLength(); i++)
				{
					String attrName  = attributes.getLocalName(i);
					String attrValue = attributes.getValue(i);
					if (Master.DEBUG_LEVEL > Master.LOW) System.out.println("          " + attrName + ": " + attrValue);
				}
			}
			
			if      (localName.equals(RDF_RDF_STR            ))  this.foundRDF = true;
			else if (localName.equals(VOID_DATASET_STR       ))  startDataSet(attributes);
			else if (localName.equals(DCTERMS_DESCRIPTION_STR))  this.isDescription = true;
			else if (localName.equals(VOID_URISPACE_STR      ))  this.isUriSpace = true;
			else if (localName.equals(VOID_URILOOKUPENDPT_STR))  startUriLookup(attributes);
			else if (localName.equals(VOID_OSD_STR           ))  startOSD(attributes);
			
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException
		{		
			if (this.isUriSpace   ) this.uriSpaceChars    = new String(ch, start, length);
			if (this.isDescription) this.descriptionChars = new String(ch, start, length);
		}

		@Override
		public void endElement(
				String uri, 
				String localName, 
				String qName) throws SAXException 
		{
			if (localName.equals(VOID_DATASET_STR       ))  endDataSet();
			if (localName.equals(DCTERMS_DESCRIPTION_STR))  this.isDescription = false;
			if (localName.equals(VOID_URISPACE_STR      ))  this.isUriSpace    = false;
		}

		@SuppressWarnings("unused")
        @Override
		public void endDocument() throws SAXException
		{
			if (!this.foundRDF) throw new SAXException("Not an RDF document");
			if (Master.DEBUG_LEVEL > Master.LOW) System.out.println("Parsing - end");
		}

		private void startDataSet(Attributes attributes)
		{
			this.currentDataset = new RDFDataset();
			this.aboutAttr = findAttribute(attributes, RDF_NAMESPACE, RDF_ABOUT_STR);
		}
		
		private void startUriLookup(Attributes attributes)
		{
			this.uriLookupEndpointResource = findAttribute(attributes, RDF_NAMESPACE, RDF_RESOURCE_STR);
		}

		private void startOSD(Attributes attributes)
		{
			this.osdResource = findAttribute(attributes, RDF_NAMESPACE, RDF_RESOURCE_STR);
		}

		private void endDataSet()throws SAXException
		{
			if (aboutAttr.isEmpty()) throw new SAXException ("Dataset about attribute not found");
			this.currentDataset.setAboutAttribute(this.aboutAttr);
			this.currentDataset.setDescriptionChars(this.descriptionChars);
			this.currentDataset.setOpenSearchDescriptionAttribute(this.osdResource);
			this.currentDataset.setUriSpaceChars(this.uriSpaceChars);
			this.currentDataset.setUriLookupEndpointResource(this.uriLookupEndpointResource);
			dataSets.put(this.uriSpaceChars, this.currentDataset);
			this.aboutAttr = "";
			this.descriptionChars = "";
			this.osdResource = "";
			this.uriSpaceChars = "";
			this.uriLookupEndpointResource = "";
		}
		
		private String findAttribute(
				Attributes attributes, 
				String ns, 
				String attributeName)
		{
			return attributes.getValue(ns, attributeName);
		}

	}  // end class SAXRDFParser
	
	public void processRDF(String rdfXml) throws ParserConfigurationException, SAXException, IOException 
	{
		
		StringReader strRdr = new StringReader(rdfXml);
		SAXParserFactory spf = SAXParserFactory.newInstance();
	    spf.setNamespaceAware(true);
	    SAXParser saxParser = spf.newSAXParser();
	    XMLReader xmlReader = saxParser.getXMLReader();
	    xmlReader.setContentHandler(new SAXRDFParser());
	    xmlReader.parse(new InputSource(strRdr));
	    
	}

	public Map<String, RDFDataset> getDataSets() {
		return dataSets;
	}
}
