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
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import global.Master;


public class RdfReplyPreprocessor
{
    private final static int DEBUG_LEVEL = 1;
    private final String LANG = "en";


    private String concept = "";
    private String prefLabel = "";
    private String definitionText = "";

    private String aboutAttribute = "";
    private String descriptionAboutAttribute = "";// alternative to first one
    private String baseAttribute = "";
    private String defLangAttr = "";
    private String prefLabelLangAttr = "";
    
    private boolean isConcept = false;
    private boolean isDescription = false;
    private boolean isDefinition = false;
    private boolean isPrefLabel = false;

    private AnnotationObject annotObject;

    private List<AnnotationObject> annotationsList = new ArrayList<AnnotationObject>();


    public void processRdfReply(String reply, boolean isNS_mode)
            throws ParserConfigurationException, SAXException, IOException
    {
        StringReader strRdr = new StringReader(reply);
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        SAXParser saxParser = spf.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setContentHandler(new SAXRdfReplyParser(isNS_mode));
        xmlReader.parse(new InputSource(strRdr));
    }

    private class SAXRdfReplyParser extends DefaultHandler
    {

        /**
         * NS Mode applies only to attributes ! For production use, this would
         * need to extended everywhere.
         */
        private boolean isNS_mode = true;

        SAXRdfReplyParser(boolean nsMode)
        {
            super();
            setNS_mode(nsMode);
        }

        @Override
        public void startDocument() throws SAXException
        {
            aboutAttribute = "";
            baseAttribute = "";
            isConcept = false;
            isDescription = false;
            isPrefLabel = false;
            concept = "";
            prefLabel = "";
        }

        @SuppressWarnings("unused")
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
        {
            if (Master.DEBUG_LEVEL > Master.LOW)
            {
                System.out.println("             - " + localName);
                if (attributes.getLength() > 0)
                    System.out.println("  -- Attributes[" + attributes.getLength() + "]: ");

                for (int i = 0; i < attributes.getLength(); i++)
                {
                    String attrName = attributes.getLocalName(i);
                    String attrValue = attributes.getValue(i);
                    if (Master.DEBUG_LEVEL > Master.LOW)
                        System.out.println("                  " + attrName + ": " + attrValue);
                }
            }


            if (localName.equals("Concept"))
            {
                isConcept = true;
                startConcept(attributes);
                annotObject = new AnnotationObject();
                annotObject.setAboutAttribute(aboutAttribute);
                annotObject.setBaseAttribute(baseAttribute);
            }
            if (localName.equals("prefLabel"))
            {
                // isPrefLabel = true;
                startPrefLabel(attributes);
                if (prefLabelLangAttr.equals(LANG))
                {
                    isPrefLabel = true;
                }
            }
            if (localName.equals("Description"))
            {
                isDescription = true;
                startDescription(attributes);
                if(null == annotObject)
                {
                    annotObject = new AnnotationObject();
                }
                annotObject.setDescriptionAboutAttribute(descriptionAboutAttribute);
            }
            if(localName.equals("broader"))
            {
               startBroader(attributes);
            }
            if(localName.equals("narrower"))
            {
                startNarrower(attributes);
            }
            if(localName.equals("definition"))
            {
               startDefinition(attributes);
               if(defLangAttr.equals(LANG))
               {
                  isDefinition =true;
               }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException
        {
            if (isPrefLabel)
            {
                prefLabel = new String(ch, start, length);
                annotObject.setKeyword(prefLabel);
            }
            if (isDefinition)
            {
                definitionText = new String(ch, start, length);
                annotObject.setDefinition(definitionText);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException
        {
            if (localName.equals("Concept"))
            {
                isConcept = false;
                annotationsList.add(annotObject);
            }
            if (localName.equals("prefLabel"))
            {
                isPrefLabel = false;
            }
            if (localName.equals("Description"))
            {
                isDescription = false;
                annotationsList.add(annotObject);
            }
            if (localName.equals("definition"))
            {
                isDefinition = false;
            }
        }


        @SuppressWarnings("unused")
        @Override
        public void endDocument() throws SAXException
        {
            if (Master.DEBUG_LEVEL > Master.LOW)
                System.out.println("Parsing rdf-reply - end");
        }

        @SuppressWarnings("unused")
        private void startConcept(Attributes attributes)
        {
            if (Master.DEBUG_LEVEL > Master.LOW)
                System.out.println("parsing...startConcept");
            baseAttribute = findAttribute(attributes, "xml", "base");
            if (Master.DEBUG_LEVEL > Master.LOW)
                System.out.println("Found baseAttribute: " + baseAttribute);
            aboutAttribute = findAttribute(attributes, "rdf", "about");
            if (Master.DEBUG_LEVEL > Master.LOW)
                System.out.println("Found aboutAttribute: " + aboutAttribute);
        }

        private void startDefinition(Attributes attributes)
        {
            defLangAttr = findAttribute(attributes, "xml", "lang");
        }

        private void startPrefLabel(Attributes attributes)
        {
            prefLabelLangAttr = findAttribute(attributes, "xml", "lang");
        }
        private void startDescription(Attributes attributes)
        {
            descriptionAboutAttribute = findAttribute(attributes,"rdf","about");
        }
        private void startBroader(Attributes attributes)
        {
           String broaderUrl = findAttribute(attributes,"rdf","resource");
           if(descriptionAboutAttribute.equals("") && !baseAttribute.equals(""))
           {
               broaderUrl = baseAttribute + broaderUrl;
           }
           annotObject.addBroaderConcept(new Concept("<broader concept>",broaderUrl)); 
        }
        private void startNarrower(Attributes attributes)
        {
           String narrowerUrl = findAttribute(attributes,"rdf","resource");
           if(descriptionAboutAttribute.equals("") && !baseAttribute.equals(""))
           {
             narrowerUrl = baseAttribute + narrowerUrl;
           }
           annotObject.addNarrowerConcept(new Concept("<narrower concept>",narrowerUrl));

        }
    
        private String findAttribute(Attributes attributes, String ns, String attributeName)
        {
            // if (isNS_mode)
            // {
            // return attributes.getValue(ns, attributeName);
            // }
            // else
            // {
            for (int i = 0; i < attributes.getLength(); i++)
            {
                if (attributes.getLocalName(i) == attributeName)
                {
                    return attributes.getValue(i);
                }
            }

            // }
            // is null if ns is invalid or attribute not found
            return attributes.getValue(ns, attributeName);
        }

        public void setNS_mode(boolean isNS_mode)
        {
            this.isNS_mode = isNS_mode;
        }

    }


    public String getConcept()
    {

        return concept;
    }


    public void setConcept(String concept)
    {

        this.concept = concept;
    }


    public List<AnnotationObject> getAnnotationsList()
    {

        return annotationsList;
    }


    public String getPrefLabel()
    {

        return prefLabel;
    }


    public void setPrefLabel(String prefLabel)
    {
        this.prefLabel = prefLabel;
    }


    public String getAboutAttribute()
    { 
        return aboutAttribute;
    }


    public void setAboutAttribute(String aboutAttribute)
    {

        this.aboutAttribute = aboutAttribute;
    }


    public String getBaseAttribute()
    {

        return baseAttribute;
    }


    public void setBaseAttribute(String baseAttribute)
    {

        this.baseAttribute = baseAttribute;
    }


    public boolean isConcept()
    {

        return isConcept;
    }


    public void setConcept(boolean isConcept)
    {

        this.isConcept = isConcept;
    }


    public boolean isPrefLabel()
    {

        return isPrefLabel;
    }


    public void setPrefLabel(boolean isPrefLabel)
    {

        this.isPrefLabel = isPrefLabel;
    }


    public String getDefLangAttr()
    {
        return defLangAttr;
    }


    public void setDefLangAttr(String defLangAttr)
    {
        this.defLangAttr = defLangAttr;
    }


    public String getPrefLabelLangAttr()
    {
        return prefLabelLangAttr;
    }


    public void setPrefLabelLangAttr(String prefLabelLangAttr)
    {
        this.prefLabelLangAttr = prefLabelLangAttr;
    }


    public String getDescriptionAboutAttribute()
    {
        return descriptionAboutAttribute;
    }


    public void setDescriptionAboutAttribute(String descriptionAboutAttribute)
    {
        this.descriptionAboutAttribute = descriptionAboutAttribute;
    }

}
