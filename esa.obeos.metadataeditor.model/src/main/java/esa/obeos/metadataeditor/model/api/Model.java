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

package esa.obeos.metadataeditor.model.api;

import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Date;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import esa.obeos.metadataeditor.model.impl.MdeElement;
import esa.obeos.metadataeditor.model.impl.MdeSubstElement;
import esa.obeos.metadataeditor.model.impl.jxb.FromElement;
import esa.obeos.metadataeditor.model.impl.jxb.FromModel;
import esa.obeos.metadataeditor.model.impl.jxb.JAXBEx;
import esa.obeos.metadataeditor.model.impl.jxb.ToElement;
import esa.obeos.metadataeditor.model.impl.jxb.ToModel;

public class Model
{
    /**
     * C-tor
     */
    private Model()
    {
    }

    /**
     * Creates an empty MDE model.
     */
    public static IMdeElement create(final ModelType modelType, final ModelVersion modelVersion) throws Exception
    {
        ToModel toModel = new ToModel(modelType, modelVersion, false);
        Object objModel = toModel.getClazz().newInstance();

        if( !(objModel instanceof MdeElement) )
        {
            throw new Exception("The model is not derived from the 'MdeElement' class.");
        }

        MdeElement ret = (MdeElement)objModel;
        setFieldName(ret, modelType);
        return ret;
    }


    /**
     * Tries to load an MDE model from an input stream
     */
    public static IMdeElement load(final InputStream instream, final ModelType modelType, 
            final ModelVersion modelVersion, final boolean validate) throws Exception
    {
        ToModel toModel = new ToModel(modelType, modelVersion, validate);

        MdeElement ret = JAXBEx.unmarshal(instream, toModel.getSchema(), toModel.getClazz());

        setFieldName(ret, modelType);
        return ret;
    }

    /**
     * Tries to load an MDE model from an XML file.
     */
    public static IMdeElement load(final File file, final ModelType modelType, 
            final ModelVersion modelVersion, final boolean validate) throws Exception
    {
        ToModel toModel = new ToModel(modelType, modelVersion, validate);

        MdeElement ret = JAXBEx.unmarshal(file, toModel.getSchema(), toModel.getClazz());

        setFieldName(ret, modelType);
        return ret;
    }

    protected static void validateInspire(IMdeElement metaData) throws Exception
    {
        Date start = new Date();
        metaData.validateInspire();
        Date stop = new Date();
        if (Debug.enabled) System.out.println("validateInspire took: " + (stop.getTime()-start.getTime()) + "[ms]");
    }
    
    /**
     * Archives an MDE model to an XML file.
     * 
     * @param file the output file
     * @param metaData the GMI or GMD metadata model to be saved
     * @param validate the validation flag
     * 
     * @throws Exception
     */
    public static void save(File file, IMdeElement metaData, boolean validate) throws Exception
    {
        FromModel fromModel = new FromModel(metaData, validate);

        if (validate)
        {
            validateInspire(metaData);
        }

        JAXBEx.marshal(fromModel.getJaxbElement(), fromModel.getSchema(), file);
    }

    /**
     * Validates an MDE model.
     * 
     * @param metaData the GMI or GMD metadata model to be saved
     * 
     * @throws Exception
     */
    public static void validate(IMdeElement metaData) throws Exception
    {
    	validate(metaData,true);
    }
    public static void validate(IMdeElement metaData, boolean validateInspire) throws Exception
    {
        FromModel fromModel = new FromModel(metaData, true);

        if(validateInspire)
        {
          // validates against to INSPIRE
          validateInspire(metaData);
        }

        // validates against to XSD
        JAXBEx.validate(fromModel.getJaxbElement(), fromModel.getSchema());
    }

    /**
     * Converts an MDE element to a DOM document.
     */
    public static Document convert(final IMdeElement element,boolean validate) throws Exception
    {
        Document ret = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        FromElement fromElement = new FromElement(element, validate);

        JAXBEx.marshal(fromElement.getJaxbElement(), fromElement.getSchema(), ret);

        return ret;
    }
    
    /**
     * Converts an MDE element to an XML string.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static String toString(final IMdeElement element) throws Exception
    {
        StringWriter stringWriter = new StringWriter();

        String childXmlName = null;
        IMdeElement el = element;

        Exception prevExc = null;

        Class<?> clazz = element.getClass();

        for (;;)
        {
            try
            {
                if (MdeSubstElement.class == clazz || null != prevExc)
                {
                    // workaround to handle 
                    IMdeElement parent = element.getParent();
                    if (null != parent)
                    {
                        childXmlName = element.getXmlElementName();
                        clazz = parent.getClass();
                        el = parent;
                    }
                    else
                    {
                        throw new Exception("To string conversion is not supported for the element '" 
                                + element.getXmlElementName() + "'.");
                    }
                }

                JAXBElement<?> jaxbElement = ((MdeElement)el).getJaxbElement();

                if (null == jaxbElement)
                {
                    String packageName = clazz.getPackage().getName();
                    String prefix = packageName.substring(packageName.lastIndexOf('.') + 1);
                    String namespaceURI = JAXBEx.getPrefixMapper().getNamespaceUri(prefix, getModelVersion(clazz));

                    QName qName = new QName(namespaceURI, el.getXmlElementName());
                    jaxbElement = new JAXBElement(qName, clazz, el);
                }

                JAXBEx.marshal(jaxbElement, null, stringWriter);
                break;
            }
            catch (Exception exc)
            {
                if (null == prevExc)
                {
                    // retry but marshal parent
                    prevExc = exc;
                    continue;
                }
                else
                {
                    throw exc;
                }
            }
        }

        String xmlFragment =  stringWriter.toString();

        xmlFragment = removeURI(xmlFragment);

        if (null != childXmlName)
        {
            xmlFragment = extractChildFragment(xmlFragment, childXmlName);
            xmlFragment = correctIndentination(xmlFragment);
        }

        return xmlFragment;
    }

    protected static String removeURI(final String xmlFragment)
    {
        int pos1 = xmlFragment.indexOf(' ');
        int pos2 = xmlFragment.indexOf('>');
        
        StringBuilder sbNoXmlnsAttrs = new StringBuilder(xmlFragment.substring(pos1, pos2));

        // remove xmlns attributes
        for (;;)
        {
            int start = sbNoXmlnsAttrs.indexOf(" xmlns");
            int end = sbNoXmlnsAttrs.indexOf(" ", start+1);
            
            if (-1 == start)
            {
                break;
            }

            // remove xmlns attribute
            if (-1 != end)
            {
                sbNoXmlnsAttrs.replace(start, end, "");
            }
            else
            {
                sbNoXmlnsAttrs.replace(start, sbNoXmlnsAttrs.length(), "");
            }
        } 

        return new StringBuilder(xmlFragment).replace(pos1, pos2, sbNoXmlnsAttrs.toString()).toString();
    }
    
    protected static String extractChildFragment(final String xmlFragment, final String childXmlName)
    {
        String token = childXmlName + ">";
        int pos1 = xmlFragment.indexOf(token);
        pos1 = xmlFragment.lastIndexOf("<", pos1);
        int pos2 = xmlFragment.indexOf(token, pos1+token.length()+1);
        
        return xmlFragment.substring(pos1, pos2 + token.length());
    }

    protected static String correctIndentination(final String xmlFragment)
    {
        String ret = xmlFragment;

        final String newLine = System.getProperty("line.separator");

        String lines[] = xmlFragment.split(newLine);
        if (null != lines && lines.length > 1)
        {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < lines.length; i++)
            {
                StringBuilder sbLine = new StringBuilder(lines[i]);
                if (i > 0)
                {
                    sbLine.delete(0, 3);
                }
                if ( i < lines.length - 1)
                {
                    sbLine.append(newLine);
                }
                sb.append(sbLine);
            }

            ret = sb.toString();
        }

        return ret;
    }

    /**
     * Tries to convert a DOM document to an MDE element.
     */
    public static <T> JAXBElement<T> convert(final Document doc, final Class<T> clazz, boolean validate) throws Exception
    {
        ToElement toElement = new ToElement(clazz, validate);

        return JAXBEx.unmarshal(doc, toElement.getSchema(), clazz);
    }


    /**
     * Tries to convert a DOM document to an MDE element.
     */
    public static IMdeElement convert(final Document doc, final ModelType type, final ModelVersion version, boolean validate) throws Exception
    {
        IMdeElement ret = null;

        ToModel toModel = new ToModel(type, version, validate);

        JAXBElement<?> jaxbElement = JAXBEx.unmarshal(doc, toModel.getSchema(), toModel.getClazz());
        if( null != jaxbElement )
        {
            if( jaxbElement.getValue() instanceof MdeElement )
            {
                ret = (MdeElement)jaxbElement.getValue();
            }
            else
            {
                throw new Exception("The XML element '" + jaxbElement.getName().getLocalPart()
                        + " cannot be converted to 'IMdelement'.");
            }
        }
        else
        {
            throw new NullPointerException("The JAXBElement<?> is null.");
        }

        return ret;
    }

    /**
     * Sets XML name of the MDE model .
     */
    private static void setFieldName(final MdeElement element, final ModelType modelType) throws Exception
    {
        if( null == element )
        {
            throw new Exception("Cannot set the field name of a null element.");
        }

        switch (modelType)
        {
        case GMD:
            element.setFieldName("MD_Metadata");
            break;

        case GMI:
            element.setFieldName("MI_Metadata");
            break;

        default:
            throw new Exception("Unsupported metadata '" + element.getClass().getName() + "'");
        }
    }

    public static ModelVersion getModelVersion(final Class<?> clazz)
    {
        ModelVersion ret = ModelVersion.Invalid;

        Package pck = clazz.getPackage();
        if (pck.getName().contains("esa.obeos.metadataeditor.model.xsd.beta"))
        {
            ret = ModelVersion.Beta;
        }
        else if (pck.getName().contains("esa.obeos.metadataeditor.model.xsd.rel2015"))
        {
            ret = ModelVersion.Rel2015;
        }

        return ret;
    }

}
