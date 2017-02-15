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

package esa.obeos.metadataeditor.model.impl.jxb;

import java.beans.Introspector;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import esa.obeos.metadataeditor.model.api.Debug;
import esa.obeos.metadataeditor.model.api.IMdeElement;
import esa.obeos.metadataeditor.model.api.Model;
import esa.obeos.metadataeditor.model.impl.MdeElement;
import esa.obeos.metadataeditor.model.impl.util.AnnotationUtil;
import esa.obeos.metadataeditor.model.impl.util.ReflectionUtil;
import esa.obeos.metadataeditor.model.impl.util.SelectionUtil;

/**
 * This class allows to marshal/un-marshal classes w/o the XmlRootElement annotation
 * to/from a XML file 
 * 
 * @author obeos
 *
 */
public class JAXBEx 
{
    protected static String NAMESPACE_PREFIX_MAPPER_PROPERTY_NAME = "com.sun.xml.bind.namespacePrefixMapper";

    /** The maps of already used JAXBContexts in order to not re-create it for the next time */  
    protected static Map<String, JAXBContext> singletons = new HashMap<String, JAXBContext>();

    /** The name space uri to prefix mapper */  
    protected static MdeNamespacePrefixMapper mapper = new MdeNamespacePrefixMapper();

    /** The beta object factories */
    private static List<Object> betaObjectFactories = new ArrayList<Object>();

    /** The release 2015 object factories */
    private static List<Object> rel2015ObjectFactories = new ArrayList<Object>();

    /** The factory methods found by name annotation */
    private static Map<String, Entry<Object, Method>> factoryMethodsByXmlElementRefName = new LinkedHashMap<String, Entry<Object, Method>>();

    /** The factory methods found by substitutionHeadName annotation */
    private static Map<String, List<Entry<Object, Method>>> factoryMethodsByXmlElementRefSubstHeadName = new LinkedHashMap<String, List<Entry<Object, Method>>>();

    /** The factory methods found by method's first argument type */
    private static Map<Class<?>, Entry<Object, Method>> factoryMethodsByFirstArgumentType = new LinkedHashMap<Class<?>, Entry<Object, Method>>();


    /** Provides JAXBContext for the given class */  
    public static MdeNamespacePrefixMapper getPrefixMapper()
    {
    	return mapper;
    }
    
    /** Provides JAXBContext for the given class */  
    public static <T extends Object> JAXBContext getInstance(Class<T> clazz)throws JAXBException
    {
        String pckName = clazz.getPackage().getName();
        JAXBContext ret = singletons.get(pckName);

        if( null == ret )
        {
            ret = JAXBContext.newInstance(pckName);
            singletons.put(pckName, ret);
        }

        return ret;
    }	

    /** Decapitalizes a class name */  
    private static String inferName(Class<?> clazz) 
    {
        return Introspector.decapitalize(clazz.getSimpleName());
    }

    /** Marshals JAXBElement to the given file */  
    public static void marshal(Object obj, URL urlSchema, File file) throws JAXBException, SAXException
    {
        Marshaller marshaller = createMarshaller(obj, urlSchema, false);
        marshaller.marshal(obj, file);
    }

    /** Marshals JAXBElement to the given DOM document */  
    public static void marshal(Object obj, URL urlSchema, Document  doc) throws JAXBException, SAXException
    {
        Marshaller marshaller = createMarshaller(obj, urlSchema, false);
        marshaller.marshal(obj, doc);
    }

    /** Marshals JAXBElement to the given writer */  
    public static void marshal(Object obj, URL urlSchema, Writer writer) throws JAXBException, SAXException
    {
        Marshaller marshaller = createMarshaller(obj, urlSchema, true);
        marshaller.marshal(obj, writer);
    }

    /** Marshals JAXBElement to the given writer */  
    public static void marshal(Object obj, URL urlSchema, OutputStream outstream) throws JAXBException, SAXException
    {
        Marshaller marshaller = createMarshaller(obj, urlSchema, false);
        marshaller.marshal(obj, outstream);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static JAXBElement<?> convert(IMdeElement element) throws Exception 
    {
    	Class<?> clazz = element.getClass();
        String packageName = clazz.getPackage().getName();
        String prefix = packageName.substring(packageName.lastIndexOf('.') + 1);
        String namespaceURI = JAXBEx.getPrefixMapper().getNamespaceUri(prefix, Model.getModelVersion(clazz));

        QName qName = new QName(namespaceURI, element.getXmlElementName());
        return new JAXBElement(qName, clazz, element);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	private static Marshaller createMarshaller(Object obj, URL urlSchema, boolean fragment) throws JAXBException, SAXException
    {
        JAXBContext ctx = null;

        if( obj instanceof JAXBElement) 
        {
            ctx = getInstance(((JAXBElement<?>)obj).getDeclaredType());
        } 
        else 
        {
            Class<?> clazz = obj.getClass();
            XmlRootElement annotation = clazz.getAnnotation(XmlRootElement.class);
            ctx = getInstance(clazz);
            if( null == annotation ) 
            {
                // we need to infer the name
                obj = new JAXBElement(new QName(inferName(clazz)), clazz, obj);
            }
        }


        Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, fragment);
        marshaller.setProperty(NAMESPACE_PREFIX_MAPPER_PROPERTY_NAME, mapper);

        if (null != urlSchema)
        {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(urlSchema);
            marshaller.setSchema(schema);
        }
        
        return marshaller;
    }

    /** Validate JAXBElement against to the schema */  
    public static void validate(Object obj, URL urlSchema) throws JAXBException, SAXException
    {
        Marshaller marshaller = createMarshaller(obj, urlSchema, false);
        marshaller.marshal(obj, new DefaultHandler());
    }

    /** Un-marshals JAXBElement from the given file 
     * @throws ParserConfigurationException 
     * @throws FactoryConfigurationError 
     * @throws XMLStreamException 
     * @throws IOException */  
    public static <T extends Object> T unmarshal(File file, URL urlSchema, Class<? extends T> type) throws JAXBException, ClassCastException, SAXException, ParserConfigurationException, XMLStreamException, FactoryConfigurationError, IOException 
    {
        T ret = null;
        FileInputStream fis = null;
        
        try
        {
            fis = new FileInputStream(file);
            ret = unmarshal(fis, urlSchema, type);
        }
        finally
        {
            if (null != fis)
            {
                fis.close();
                fis = null;
            }
        }

        return ret;
    }

    /** Un-marshals JAXBElement from the given file 
     * @throws FileNotFoundException 
     * @throws ParserConfigurationException 
     * @throws FactoryConfigurationError 
     * @throws XMLStreamException */  
    @SuppressWarnings("unchecked")
    public static <T extends Object> T unmarshal(InputStream instream, URL urlSchema, Class<? extends T> type) throws JAXBException, ClassCastException, SAXException, FileNotFoundException, ParserConfigurationException, XMLStreamException, FactoryConfigurationError 
    {
        JAXBContext ctx = getInstance(type);

        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        unmarshaller.setEventHandler(new ValidationEventHandler() 
        {
            @Override
            public boolean handleEvent(ValidationEvent arg0) 
            {
                if (Debug.enabled) System.out.println(arg0.getSeverity() + ", " + arg0.getMessage());
                return false;
            }
        });

        JAXBElement<?> jaxbElement = null;
        
        if (null != urlSchema)
        {
         
            // unmarshal w/ XSD schema
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(urlSchema);
            unmarshaller.setSchema(schema);
            jaxbElement = unmarshaller.unmarshal(new StreamSource(instream), type);
        }
        else
        {
            // unmarshal w/o XSD schema and ignore NS prefixes
            XMLInputFactory xif = XMLInputFactory.newInstance();
            xif.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
            XMLStreamReader xsr = xif.createXMLStreamReader(instream);
            MdeXmlReader xsrins = new MdeXmlReader(xsr, Model.getModelVersion(type));
            jaxbElement = unmarshaller.unmarshal(xsrins, type);
        }

        return (T)jaxbElement.getValue();
    }
    

    /** Un-marshals JAXBElement from the given DOM document 
     * @throws Exception */  
    public static <T> JAXBElement<T> unmarshal(Document doc, URL urlSchema, Class<T> type) throws Exception 
    {
        JAXBContext ctx = getInstance(type);

        Unmarshaller unmarshaller = ctx.createUnmarshaller();

        unmarshaller.setEventHandler(new ValidationEventHandler() {

            public boolean handleEvent(ValidationEvent arg0) 
            {
                if (Debug.enabled) System.out.println(arg0.getSeverity() + ", " + arg0.getMessage());
                return false;
            }
        });

        if (null != urlSchema)
        {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(urlSchema);
            unmarshaller.setSchema(schema);
        }

        JAXBElement<T> ret = unmarshaller.unmarshal(doc, type);
        if( ret.getValue() instanceof MdeElement )
        {
            MdeElement mdeElement = (MdeElement)ret.getValue();
            mdeElement.setXmlElementName(ret.getName().getLocalPart());
            mdeElement.setJaxbElement(ret, "");
        }
        return ret;
    }

    /** Initializes ObjectFactoris */
    private static void initialiseFactoryMap()
    {
        betaObjectFactories.add(new esa.obeos.metadataeditor.model.xsd.beta.gco.ObjectFactory());
        betaObjectFactories.add(new esa.obeos.metadataeditor.model.xsd.beta.gfc.ObjectFactory());
        betaObjectFactories.add(new esa.obeos.metadataeditor.model.xsd.beta.gmd.ObjectFactory());
        betaObjectFactories.add(new esa.obeos.metadataeditor.model.xsd.beta.gmi.ObjectFactory());
        betaObjectFactories.add(new esa.obeos.metadataeditor.model.xsd.beta.gml.ObjectFactory());
        betaObjectFactories.add(new esa.obeos.metadataeditor.model.xsd.beta.gmx.ObjectFactory());
        betaObjectFactories.add(new esa.obeos.metadataeditor.model.xsd.beta.gsr.ObjectFactory());
        betaObjectFactories.add(new esa.obeos.metadataeditor.model.xsd.beta.gss.ObjectFactory());
        betaObjectFactories.add(new esa.obeos.metadataeditor.model.xsd.beta.gts.ObjectFactory());
        betaObjectFactories.add(new esa.obeos.metadataeditor.model.xsd.beta.srv.ObjectFactory());
        betaObjectFactories.add(new esa.obeos.metadataeditor.model.xsd.beta.smil20.ObjectFactory());
        betaObjectFactories.add(new esa.obeos.metadataeditor.model.xsd.beta.smil20.Language.ObjectFactory());

        rel2015ObjectFactories.add(new esa.obeos.metadataeditor.model.xsd.rel2015.gco.ObjectFactory());
        rel2015ObjectFactories.add(new esa.obeos.metadataeditor.model.xsd.rel2015.gmd.ObjectFactory());
        rel2015ObjectFactories.add(new esa.obeos.metadataeditor.model.xsd.rel2015.gmi.ObjectFactory());
        rel2015ObjectFactories.add(new esa.obeos.metadataeditor.model.xsd.rel2015.gml.ObjectFactory());
        rel2015ObjectFactories.add(new esa.obeos.metadataeditor.model.xsd.rel2015.gmx.ObjectFactory());
        rel2015ObjectFactories.add(new esa.obeos.metadataeditor.model.xsd.rel2015.gsr.ObjectFactory());
        rel2015ObjectFactories.add(new esa.obeos.metadataeditor.model.xsd.rel2015.gss.ObjectFactory());
        rel2015ObjectFactories.add(new esa.obeos.metadataeditor.model.xsd.rel2015.gts.ObjectFactory());
        rel2015ObjectFactories.add(new esa.obeos.metadataeditor.model.xsd.rel2015.xlink.ObjectFactory());

        if (Debug.enabled) System.out.println("initialised object factories map");
    }

    private static List<Object> getObjectFactories(Package pck)
    {
        List<Object> ret = new ArrayList<Object>();
        
        if( pck.getName().contains("esa.obeos.metadataeditor.model.xsd.rel2015") )
        {
            ret = rel2015ObjectFactories;
        }
        else if( pck.getName().contains("esa.obeos.metadataeditor.model.xsd.beta") )
        {
            ret = betaObjectFactories;
        }

        return ret;
    }
    
    private static Class<?> getMethodFirstArgumentType(Method method) throws Exception
    {
        Class<?> ret = null;

        Type[] argumentTypes =  method.getParameterTypes();
        if (null != argumentTypes && argumentTypes.length > 0 && null != argumentTypes[0])
        {
            if (argumentTypes[0] instanceof Class<?>)
            {
                Class<?> argClass = (Class<?>)argumentTypes[0];
                if (!argClass.isPrimitive())
                {
                    ret = Class.forName(ReflectionUtil.getTypeName(argumentTypes[0]));
                }
            }
        }
        return ret;
    }

    /** Returns all suitable factory method to create an object for XmlElementRef annotated java field
     * 
     * @param xmlElementRefName the name stored in XmlElementRef annotation
     * 
     * @return List<Entry<Object, Method>> the factories
     *
     * @throws Exception
     */
    public static List<Entry<Object, Method>> getAllSuitableFactoryMethods(final String xmlElementRefName, final Package pck) throws Exception
    {
        Entry<Object, Method> factoryMethod = getFactoryMethod(xmlElementRefName, pck);

        boolean abstractArgClass = false;

        if (null != factoryMethod && null != factoryMethod.getKey() && null != factoryMethod.getValue())
        {
            Class<?> argClass = getMethodFirstArgumentType(factoryMethod.getValue());
            if (null != argClass && Modifier.isAbstract(argClass.getModifiers()))
            {
                abstractArgClass = true;
            }
        }

        List<Entry<Object, Method>> ret = getFactoryMethods(xmlElementRefName, pck);

        if (null != ret)
        {
            if (ret.size() > 1)
            {
                ret = removeFactoryMethodOfAbstractClasses(ret);
            }
        }

        if (null != factoryMethod && !abstractArgClass )
        {
            if (null != ret)
            {
                ret.add(0, factoryMethod);
            }
            else
            {
                ret = new ArrayList<Entry<Object, Method>>();
                ret.add(factoryMethod);
            }
        }

        return ret;
    }

    /** Creates JAXBElement of the given xmlElementRefName
     * 
     * @param xmlElementRefName
     * @return JAXBElement<?> the created JAXBElement
     *
     * @throws Exception
     */
    public static JAXBElement<?> createJAXBElement(final String xmlElementRefName, final String choose, final Package pck) throws Exception
    {
        List<Entry<Object, Method>> factoryMethods = getAllSuitableFactoryMethods(xmlElementRefName, pck);

        Entry<Object, Method> factoryMethod = null;

        if (null != factoryMethods && !factoryMethods.isEmpty())
        {
            if (factoryMethods.size() > 1)
            {
                factoryMethod = SelectionUtil.select(factoryMethods, choose);
            }
            else
            {
                factoryMethod = factoryMethods.get(0);
            }
        }

        if (null == factoryMethod || null == factoryMethod.getValue() || null == factoryMethod.getKey())
        {
            throw new Exception("There is not available a factory method for JAXBElement<" + xmlElementRefName + ">.");
        }

        if (null == factoryMethod || null == factoryMethod.getValue() || null == factoryMethod.getKey())
        {
            throw new Exception("There is not available a factory method for JAXBElement<" + xmlElementRefName + ">.");
        }

        return invokeMethod(factoryMethod.getKey(), factoryMethod.getValue(), null);
    }

    /** Creates JAXBElement of the given class
     * 
     * @param object
     * @return JAXBElement<?>
     * @throws Exception
     */
    public static JAXBElement<?> createJAXBElement(Object object) throws Exception
    {
        Class<?> clazz = object.getClass();
        
        Entry<Object, Method> factoryMethod = getFactoryMethod(clazz);
        
        if (null == factoryMethod || null == factoryMethod.getValue() || null == factoryMethod.getKey())
        {
            throw new Exception("There is not available a factory method for JAXBElement<" + clazz.getSimpleName() + ">.");
        }

        return invokeMethod(factoryMethod.getKey(), factoryMethod.getValue(), object);
    }

    
    /** 
     * Removes factory methods creating JAXBelement<An abstract class>
     * 
     * @param List<Entry<Object, Method>> factoryMethods 
     * @return List<Entry<Object, Method>> 
     * @throws Exception
     */
    private static List<Entry<Object, Method>> removeFactoryMethodOfAbstractClasses(final List<Entry<Object, Method>> factoryMethods) throws Exception
    {
        List<Entry<Object, Method>> ret = new ArrayList<Map.Entry<Object,Method>>();
        
        for (Entry<Object, Method> factoryMethod : factoryMethods)
        {
            Class<?> argClass = getMethodFirstArgumentType(factoryMethod.getValue());
            if (null != argClass && !Modifier.isAbstract(argClass.getModifiers()))
            {
                ret.add(factoryMethod);
            }
        }
        
        return ret;
    }

    /** 
     * Returns factory method for the given class
     * 
     * @param String xmlElementRefName
     * @return Entry<Object, Method>
     * @throws Exception
     */
    public static Entry<Object, Method> getFactoryMethod(final Class<?> argClass) throws Exception
    {
        if (betaObjectFactories.isEmpty())
        {
            initialiseFactoryMap();
        }

        Entry<Object, Method> ret = factoryMethodsByFirstArgumentType.get(argClass);
        if (null == ret)
        {
            // the factory method for the given class has not been used yet
            // we got to find it in object factories 
            for (Object objectFactory : getObjectFactories(argClass.getPackage()))
            {
                Method method = findFactoryMethodByFirstArgumentType(objectFactory.getClass(), argClass);
                if (null != method)
                {
                    ret = new AbstractMap.SimpleEntry<Object, Method>(objectFactory, method);
                    factoryMethodsByFirstArgumentType.put(argClass, ret);
                    break;
                }
            }
        }

        return ret;
    }


    /** 
     * Returns factory methods for derived classes
     * 
     * @param String xmlElementRefName
     * @return List<Entry<Object, Method>>
     * @throws Exception
     */
    public static List<Entry<Object, Method>> getFactoryMethods(String xmlElementRefName, Package pck) throws Exception
    {
        if (betaObjectFactories.isEmpty())
        {
            initialiseFactoryMap();
        }

        List<Entry<Object, Method>> ret = null; //factoryMethodsByXmlElementRefSubstHeadName.get(xmlElementRefName);
        if (null == ret || ret.isEmpty())
        {
            // the factory method for the given class has not been used yet
            // we got to find it in object factories 
            for (Object objectFactory : getObjectFactories(pck))
            {
                List<Method> methods = findFactoryMethodBySubstHeadName(objectFactory.getClass(), xmlElementRefName);
                if (null != methods && !methods.isEmpty())
                {
                    if (null == ret)
                    {
                        ret = new ArrayList<Map.Entry<Object,Method>>();
                    }

                    for (Method method : methods)
                    {
                        Entry<Object, Method> entry = new AbstractMap.SimpleEntry<Object, Method>(objectFactory, method);
                        ret.add(entry);
                    }

                    factoryMethodsByXmlElementRefSubstHeadName.put(xmlElementRefName, ret);
                }
            }

            if (null != ret)
            {
                // find also methods creating derived class instances
                List<Entry<Object, Method>> derivedClassInstances = getFactoryMethodsOfDerivedClassInstances(ret, pck);
                if (null != derivedClassInstances && !derivedClassInstances.isEmpty())
                {
                    ret.addAll(derivedClassInstances);
                }
            }
        }

        return ret;
    }

    /** 
     * Returns factory methods for the derived classes
     * 
     * @param List<Entry<Object, Method>> baseClassInstances
     * @return List<Entry<Object, Method>> the derived class instances
     * @throws Exception
     */
    public static List<Entry<Object, Method>> getFactoryMethodsOfDerivedClassInstances(List<Entry<Object, Method>> baseClassInstances, Package pck) throws Exception
    {
        List<Entry<Object, Method>> ret = new ArrayList<Entry<Object, Method>>();
        for (Entry<Object, Method> method : baseClassInstances)
        {
            String methodTypeName = getFactoryMethodTypeName(method.getValue());
            List<Entry<Object, Method>> derivedClassInstances = getFactoryMethods(methodTypeName, pck);
            if (null != derivedClassInstances && !derivedClassInstances.isEmpty())
            {
                ret.addAll(derivedClassInstances);
            }
        }

        return ret;
    }
    
    /** 
     * Returns factory method for the given class
     * 
     * @param String xmlElementRefName
     * @return Method
     * @throws Exception
     */
    private static Entry<Object, Method> getFactoryMethod(String xmlElementRefName, Package pck) throws Exception
    {
        if (betaObjectFactories.isEmpty())
        {
            initialiseFactoryMap();
        }

        Entry<Object, Method> ret = factoryMethodsByXmlElementRefName.get(xmlElementRefName);
        if (null == ret)
        {
            // the factory method for the given class has not been used yet
            // we got to find it in object factories 
            for (Object objectFactory : getObjectFactories(pck))
            {
                Method method = findFactoryMethodByName(objectFactory.getClass(), xmlElementRefName);
                if (null != method)
                {
                    ret = new AbstractMap.SimpleEntry<Object, Method>(objectFactory, method);
                    factoryMethodsByXmlElementRefName.put(xmlElementRefName, ret);
                    break;
                }
            }
        }

        return ret;
    }

    /** Returns class type name creatable from the given factory method 
     * 
     * @param method
     * @return String
     * @throws Exception
     */
    private static String getFactoryMethodTypeName(Method method) throws Exception
    {
        String ret = null;

        XmlElementDecl xmlElementDecl = AnnotationUtil.getXmlElementDecl(method);
        if (null != xmlElementDecl)
        {
            ret = xmlElementDecl.name();
        }

        return ret;
    }

    /** Returns substitution head name of the given factory method 
     * 
     * @param method
     * @return String
     * @throws Exception
     */
    private static String getFactoryMethodSubstHeadName(Method method) throws Exception
    {
        String ret = null;

        XmlElementDecl xmlElementDecl = AnnotationUtil.getXmlElementDecl(method);
        if (null != xmlElementDecl)
        {
            ret = xmlElementDecl.substitutionHeadName();
        }

        return ret;
    }

    /** Looks for the factory method for the given class in object factories 
     *  
     * @param objectFactoryClass
     * @param xmlElementRefName
     * @return Method
     * @throws Exception
     */
    private static Method findFactoryMethodByName(Class<?> objectFactoryClass, String xmlElementRefName) throws Exception
    {
        Method ret = null;

        if (null == objectFactoryClass)
        {
            throw new Exception("objectFactoryClass is null");
        }

        if (null == xmlElementRefName)
        {
            throw new Exception("xmlElementRefName is null");
        }

        // iterate over all factory class' method and try to find
        // a method creating the instance of xmlElementRefName
        for (Method method : objectFactoryClass.getMethods())
        {
            if (xmlElementRefName.equals(getFactoryMethodTypeName(method)))
            {
                ret = method;
                if (Debug.enabled) System.out.println("found factory method: " + ret.getName());
                break;
            }
        }  

        return ret;
    }

    /** Looks for the factory method for the given class in object factories 
     *  
     * @param objectFactoryClass
     * @param xmlElementRefName
     * @return Method
     * @throws Exception
     */
    private static Method findFactoryMethodByFirstArgumentType(final Class<?> objectFactoryClass, final Class<?> argClass) throws Exception
    {
        Method ret = null;

        if (null == objectFactoryClass)
        {
            throw new Exception("objectFactoryClass is null");
        }

        if (null == argClass)
        {
            throw new Exception("argClass is null");
        }

        // iterate over all factory class' method and try to find
        // a method accepting the provided argument type
        for (Method method : objectFactoryClass.getMethods())
        {
            Class<?> clazz = getMethodFirstArgumentType(method);
            if (argClass == clazz)
            {
                ret = method;
                if (Debug.enabled) System.out.println("found factory method: " + ret.getName());
                break;
            }
        }  

        return ret;
    }

    /** Looks for the factory method for the given class in object factories 
     *  
     * @param objectFactoryClass
     * @param argumentClass
     * @return List<Method>
     * @throws Exception
     */
    private static List<Method> findFactoryMethodBySubstHeadName(Class<?> objectFactoryClass, String xmlElementRefName) throws Exception
    {
        List<Method> ret = new ArrayList<Method>();

        if (null == objectFactoryClass)
        {
            throw new Exception("objectFactoryClass is null");
        }

        if (null == xmlElementRefName)
        {
            throw new Exception("xmlElementRefSubstHeadName is null");
        }

        // iterate over all factory class' method and try to find
        // a method creating an instance from substitution name xmlElementRefName
        for (Method method : objectFactoryClass.getMethods())
        {
            String substHeadName = getFactoryMethodSubstHeadName(method);

            if (null != substHeadName && xmlElementRefName.equals(substHeadName))
            {
                ret.add(method);
                if (Debug.enabled) System.out.println("found factory method: " + method.getName());
            }
        }  

        return ret;
    }

    /**
     * Invokes a method on the given instance
     * 
     * @param instance
     * @param method
     * @return JAXBElement
     * @throws Exception
     */
    private static JAXBElement<?> invokeMethod(Object instance, Method method, Object objArg) throws Exception
    {
        JAXBElement<?> ret = null;

        Class<?> argClazz = getMethodFirstArgumentType(method);

        if( null == objArg )
        {
            objArg = argClazz.newInstance();
        }

        Object obj = method.invoke(instance, objArg);

        if (obj instanceof JAXBElement<?>)
        {
            ret = (JAXBElement<?>)obj;
        }

        return ret;
    }
    
    /**
     * Checks whether a child element can be inserted to a parent element
     * 
     * @param childXmlName child's element name 
     * @param parentXmlName parent's element name
     * @param f the target field
     * @throws Exception is thrown in case if the child cannot be inserted to the parent
     */
    public static void canBeInserted(String childXmlName, String parentXmlName, Field f, Package pck) throws Exception
    {
        XmlElements xmlElements = AnnotationUtil.getXmlElements(f);
        if( null == xmlElements || null == xmlElements.value() )
        {
            throw new Exception("The child '" + childXmlName 
                    + "' cannot be inserted to parent '" + parentXmlName + "' because of missing "
                    + "'XmlElements' field annotation.");
        }

        if( !AnnotationUtil.contains(xmlElements, childXmlName) )
        {
            throw new Exception("The child '" + childXmlName 
                    + "' cannot be inserted to parent '" + parentXmlName + "' because the target field "
                    + " does not contain XmlElement '" + childXmlName + "'.");
        }
        
    }

    /**
     * Checks whether parent's target field annotated by xmlElementRefName 
     * is compatible with child's XML element name
     * 
     * @param xmlElementRefName the reference name of parent's target field 
     * @param xmlElementName child's element name
     * @param pck the java package
     * @return boolean true if the child can be inserted to the parent's filed
     * @throws Exception
     */
    private static boolean canBeSubstituted(String xmlElementRefName, String xmlElementName, Package pck) throws Exception
    {
        boolean ret = false;
        
        if( null == xmlElementRefName )
        {
            throw new NullPointerException("xmlElementRefName is null");
        }

        if( null == xmlElementName )
        {
            throw new NullPointerException("xmlElementName is null");
        }
        
        List<Entry<Object, Method>> methodEntries = getFactoryMethods(xmlElementRefName, pck);
        if( null != methodEntries )
        {
            for( Entry<Object, Method> methodEntry : methodEntries )
            {
                Method method = methodEntry.getValue();
                if( null == method )
                {
                    throw new NullPointerException("method is null");
                }
                
                String name = getFactoryMethodTypeName(method);
                if( xmlElementName.equals(name) )
                {
                    ret = true;
                    break;
                }
            }
        }
        
        return ret;
    }

    /**
     * Checks whether a child element can be inserted to a parent element
     * 
     * @param childXmlName child's element name 
     * @param parentXmlName parent's element name
     * @param f the target field
     * @throws Exception is thrown in case if the child cannot be inserted to the parent
     */
    public static void canBeInsertedAsJaxbElement(String childXmlName, String parentXmlName, Field f, Package pck) throws Exception
    {
        XmlElementRef xmlElementRef = AnnotationUtil.getXmlElementRef(f);
        XmlElementRefs xmlElementRefs = AnnotationUtil.getXmlElementRefs(f);
        if( null == xmlElementRef && (null == xmlElementRefs || null == xmlElementRefs.value()) )
        {
            throw new Exception("The child '" + childXmlName 
                    + "' could not be inserted to parent '" + parentXmlName + "' because of missing "
                    + "field's annotations 'XmlElementRef' or 'XmlElementRefs'.");
        }

        if( null != xmlElementRef )
        {
            if( !JAXBEx.canBeSubstituted(xmlElementRef.name(), childXmlName, pck) )
            {
                throw new Exception("The child '" + childXmlName
                        + "' could not be inserted to parent '" + parentXmlName + "' because it cannot " 
                        + "substitute '" + xmlElementRef.name() + "'.");
            }
        }
        else
        {
            boolean canBeSubstituted = false;

            StringBuilder sb = new StringBuilder();
            
            for( XmlElementRef xmlElementRef2 : xmlElementRefs.value() )
            {
                if( JAXBEx.canBeSubstituted(xmlElementRef2.name(), childXmlName, pck) )
                {
                    canBeSubstituted = true;
                    break;
                }
                sb.append(" '" + xmlElementRef2.name() + "'");
            }
            
            if( !canBeSubstituted )
            {
                throw new Exception("The child '" + childXmlName
                        + "' could not be inserted to parent '" + parentXmlName + "' because it cannot " 
                        + "substitute" + sb.toString() + ".");
            }
        }
    }
    
    /** Gets the class hierarchy 
     * 
     * @return List<Class<?>>
     * 
     * */
    public static List<Class<?>> getSubstitutingClasses(Field f, Package pck) throws Exception
    {
        List<Class<?>> ret = new ArrayList<Class<?>>();

        if (null != f)
        {
            Class<?> clazz = f.getType();
            if( JAXBElement.class.equals(clazz) || List.class.equals(clazz) )
            {
                Type type = f.getGenericType();
                clazz = ReflectionUtil.getFirstParametrizedType(type);
            }

            XmlElementRef xmlElementRef = AnnotationUtil.getXmlElementRef(f);
            XmlElementRefs xmlElementRefs = AnnotationUtil.getXmlElementRefs(f);
            if( null != xmlElementRef || (null != xmlElementRefs && null != xmlElementRefs.value()) )
            {
                List<String> xmlElementRefNames = new ArrayList<String>();
                if( null != xmlElementRef )
                {
                    xmlElementRefNames.add(xmlElementRef.name());
                }
                else
                {
                    for( XmlElementRef xmlElementRef2 : xmlElementRefs.value())
                    {
                        xmlElementRefNames.add(xmlElementRef2.name());
                    }
                }
                
                for( String xmlElementRefName : xmlElementRefNames)
                {
                    List<Entry<Object, Method>> methodEntries = getFactoryMethods(xmlElementRefName, pck);
                    if( null != methodEntries )
                    {
                        for( Entry<Object, Method> methodEntry : methodEntries )
                        {
                            Method method = methodEntry.getValue();
                            if( null == method )
                            {
                                throw new NullPointerException("method is null");
                            }

                            Class<?> argumentClass = getMethodFirstArgumentType(method);
                            ret.add(argumentClass);
                        }
                    }
                }
            }
            else
            {
                ret.add(clazz);
            }

        }
        return ret;
    }
}
