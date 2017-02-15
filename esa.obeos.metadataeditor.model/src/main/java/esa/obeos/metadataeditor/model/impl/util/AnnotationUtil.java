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

package esa.obeos.metadataeditor.model.impl.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import esa.obeos.metadataeditor.model.impl.MdeElement;

public class AnnotationUtil
{
    public static XmlElement getXmlElement(Field f)
    {
        XmlElement ret = null;

        if (null != f)
        {
            ret = (XmlElement)f.getAnnotation(XmlElement.class);
        }

        return ret;
    }

    public static XmlAttribute getXmlAttribute(Field f)
    {
        XmlAttribute ret = null;

        if (null != f)
        {
            ret = (XmlAttribute)f.getAnnotation(XmlAttribute.class);
        }

        return ret;
    }

    public static XmlValue getXmlValue(Field f)
    {
        XmlValue ret = null;

        if (null != f)
        {
            ret = (XmlValue)f.getAnnotation(XmlValue.class);
        }

        return ret;
    }

    public static XmlElements getXmlElements(Field f)
    {
        XmlElements ret = null;

        if (null != f)
        {
            ret = (XmlElements)f.getAnnotation(XmlElements.class);
        }

        return ret;
    }

    public static XmlElementRef getXmlElementRef(Field f)
    {
        XmlElementRef ret = null;

        if (null != f)
        {
            ret = (XmlElementRef)f.getAnnotation(XmlElementRef.class);
        }

        return ret;
    }

    public static XmlElementRefs getXmlElementRefs(Field f)
    {
        XmlElementRefs ret = null;

        if (null != f)
        {
            ret = (XmlElementRefs)f.getAnnotation(XmlElementRefs.class);
        }

        return ret;
    }

    public static List<XmlElement> getXmls(Field f)
    {
        List<XmlElement> ret = new ArrayList<XmlElement>();
        
        XmlElement xmlElement = getXmlElement(f);
        if (null != xmlElement)
        {
            ret.add(xmlElement);
        }
        else
        {
            XmlElements xmlElements = getXmlElements(f);
            if (null != xmlElements && null != xmlElements.value())
            {
                for ( XmlElement xmlElement2 : xmlElements.value())
                {
                    ret.add(xmlElement2);
                }
            }
            
        }

        return ret;
    }
    
    /** Returns annotation XmlElementDecl of the given factory method 
     * 
     * @param method
     * @return XmlElementDecl
     * @throws Exception
     */
    public static XmlElementDecl getXmlElementDecl(Method method) throws Exception
    {
        XmlElementDecl ret = null;

        if (null == method)
        {
            throw new Exception("method is null");
        }

        XmlElementDecl xmlElementDecl = (XmlElementDecl)method.getAnnotation(XmlElementDecl.class);
        if (null != xmlElementDecl)
        {
            ret = xmlElementDecl;
        }

        return ret;
    }

    public static boolean contains(XmlElements xmlElements, String name)
    {                               
        boolean ret = false;

        if( null != name )
        {
            for( XmlElement xmlElement : xmlElements.value() )
            {                 
                if( name.equals(xmlElement.name()) )
                {
                    ret = true;
                    break;
                }
            }
        }

        return ret;
    }

    public static boolean contains(XmlElementRefs xmlElementRefs, String name)
    {                               
        boolean ret = false;

        if( null != name )
        {
            for( XmlElementRef xmlElementRef : xmlElementRefs.value() )
            {                 
                if( name.equals(xmlElementRef.name()) )
                {
                    ret = true;
                    break;
                }
            }
        }

        return ret;
    }

    /** Provides XML type name of an element.
     * 
     * @param clazz
     * 
     * @return  String
     */
    public static String getXmlType(Class<?> clazz)
    {
        String ret = null;

        XmlType xmlType = clazz.getAnnotation(XmlType.class);
        if (null != xmlType)
        {
            ret = xmlType.name();
        }

        return ret;
    }

    public static String getXmlType(Object obj)
    {
        String ret = null;

        if (null != obj)
        {
            ret = getXmlType(obj.getClass());
        }

        return ret;
    }

    /** Provides java type of an element.
     * 
     * @param Field f
     * @param String name
     * 
     * @return  String
     */
    public static Class<?> getType(Field f, String name)
    {
        Class<?> ret = null;

        if (null != name)
        {
            XmlElements xmlElements = AnnotationUtil.getXmlElements(f);
            if (null != xmlElements && null != xmlElements.value())
            {
                for (XmlElement xmlElement : xmlElements.value())
                {
                    if (name.equals(xmlElement.name()))
                    {
                        ret = xmlElement.type();
                    }
                }
            }
        }

        return ret;
    }

    /** Checks if the class field is XML attribute. 
     * 
     * @param obj
     * 
     * @return boolean
     * 
     * */
    public static boolean isAttribute(Object obj)
    {
        boolean ret = false;

        if (null != obj)
        {
            Class<?> clazz = obj.getClass();

            Annotation annotation = clazz.getAnnotation(XmlAnyAttribute.class);
            if (null != annotation)
            {
                ret = true;
            }
        }

        return ret;
    }

    /** Checks if the class filed is XML attribute 
     * 
     * @param f
     * 
     * @return boolean
     * 
     * */
    public static boolean isAttribute(Field f)
    {
        boolean ret = false;

        if (null != f)
        {
            Annotation annotation = f.getAnnotation(XmlAttribute.class);
            if (null != annotation)
            {
                ret = true;
            }
        }

        return ret;
    }

    /** Checks if the class filed is XML value element. 
     * 
     * @param obj
     * 
     * @return boolean
     * 
     * */
    public static boolean isValue(Field f)
    {
        boolean ret = false;

        if (null != f)
        {
            Annotation annotation = f.getAnnotation(XmlValue.class);
            if (null != annotation)
            {
                ret = true;
            }
//            else
//            {
//                annotation = f.getAnnotation(XmlMixed.class);
//                if (null != annotation)
//                {
//                    ret = true;
//                }
//            }
//            else
//            {
//                // try XmlElement type if XmlValue is not available
//                annotation = f.getAnnotation(XmlElement.class);
//                if (null != annotation)
//                {
//                    XmlElement xmlElement = (XmlElement)annotation;
//                    ret = isValueType(xmlElement.type());
//                    if (!ret)
//                    {
//                        ret = isValueType(f.getType());
//                    }
//                }
//                else
//                {
//                    // try XmlElements if XmlElement is not available
//                    annotation = f.getAnnotation(XmlElements.class);
//                    if (null != annotation)
//                    {
//                        XmlElements xmlElements = (XmlElements)annotation;
//                        if (null != xmlElements.value())
//                        {
//                            for (XmlElement xmlElement : xmlElements.value())
//                            {
//                                if (isValueType(xmlElement.type()))
//                                {
//                                    ret = true;
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                }
//            }
        }

        return ret;
    }

    /** Checks if the XML element filed is required. 
     * 
     * @param Field f
     * 
     * @return boolean
     * 
     * */
    public static boolean isXmlElementRequired(Field f)
    {
        boolean ret = false;

        if (null != f)
        {
            XmlElement xmlElement = getXmlElement(f);
            if (null != xmlElement)
            {
                ret = xmlElement.required();
            }
            else
            {
                XmlElementRefs xmlElementRefs = getXmlElementRefs(f);
                if (null != xmlElementRefs)
                {
                    // For all "Order" and "Group" indicators (any, all, choice, sequence, group name, and group reference) 
                    // the default value for maxOccurs and minOccurs is 1.
                    ret = true;
                }
                else
                {
                    XmlElements xmlElements = getXmlElements(f);
                    if (null != xmlElements)
                    {
                        // For all "Order" and "Group" indicators (any, all, choice, sequence, group name, and group reference) 
                        // the default value for maxOccurs and minOccurs is 1.
                        ret = true;
                    }
                }
            }
        }

        return ret;
    }

    /** Checks if the XML attribute filed is required. 
     * 
     * @param Field f
     * 
     * @return boolean
     * 
     * */
    public static boolean isXmlAttributeRequired(Field f)
    {
        boolean ret = false;

        if (null != f)
        {
            XmlAttribute xmlAttribute = getXmlAttribute(f);
            if (null != xmlAttribute)
            {
                ret = xmlAttribute.required();
            }
        }

        return ret;
    }

    /** Checks if the children filed is required. 
     * 
     * @param obj
     * 
     * @return boolean
     * 
     * */
    public static boolean isTransient(Field f)
    {
        boolean ret = false;

        if (null != f)
        {
            Annotation annotation = f.getAnnotation(XmlTransient.class);
            if (null != annotation)
            {
                ret = true;
            }
        }

        return ret;
    }

    /** Checks if the class is "simple" Java type. 
     * 
     * @param clazz
     * 
     * @return boolean
     * 
     * */
    public static boolean isValueType(Class<?> clazz)
    {
        boolean ret = false;

        if (null != clazz)
        {
            if (!MdeElement.class.isAssignableFrom(clazz))
            {
                if (String.class == clazz
                        || BigInteger.class == clazz
                        || Integer.class == clazz
                        || Long.class == clazz
                        || Short.class == clazz
                        || BigDecimal.class == clazz
                        || Double.class == clazz
                        || Boolean.class == clazz
                        || Byte.class == clazz
                        || XMLGregorianCalendar.class == clazz
                        || Byte[].class == clazz
                        || Duration.class == clazz
                        || clazz.isEnum())
                {
                    ret = true;
                }
                else
                {
                    ret = hasValueField(clazz);
                }
            }
        }

        return ret;
    }

    /** Has value field. 
     * 
     * @param clazz
     * 
     * @return boolean
     * 
     * */
    public static boolean hasValueField(Class<?> clazz)
    {
        boolean ret = false;

        if (null != clazz)
        {
            Field[] fields = clazz.getDeclaredFields();
            if (null != fields)
            {
                for (Field f : fields)
                {
                    if (isValue(f))
                    {
                        ret = true;
                        break;
                    }
                }
            }

        }

        return ret;
    }

    /** Gets original XML element name or XML element reference name
     *  and all possible XML elements if available. 
     * 
     * @param Field f
     * @param Object obj
     * 
     * @return Entry<String,XmlElements> 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * 
     * */
    public static Entry<String,XmlElements> getAnnotationName(Field f, Object obj) throws IllegalArgumentException, IllegalAccessException
    {
        String annotationName = null;
        XmlElements xmlElements = null;

        if (null != f)
        {
            XmlElement xmlElement = AnnotationUtil.getXmlElement(f);
            if (null != xmlElement)
            {
                if (!("##default".equals(xmlElement.name())))
                {
                    annotationName = xmlElement.name();
                }
            }

            if (null == annotationName)
            {
                // try XmlElementRef if XmlElement is not available
                XmlElementRef xmlElementRef = AnnotationUtil.getXmlElementRef(f);
                if (null != xmlElementRef)
                {
                    if (!("##default".equals(xmlElementRef.name())))
                    {
                        annotationName = xmlElementRef.name();
                    }
                }
            }

            xmlElements = AnnotationUtil.getXmlElements(f);
            if (null == annotationName && null != obj && null != xmlElements)
            {
                // try XmlElements if XmlElement is not available
                if (null != xmlElements.value())
                {
                    for (XmlElement xmlElement2 : xmlElements.value())
                    {
                        Class<?> clazz = xmlElement2.type();
                        if (clazz.isAssignableFrom(obj.getClass()))
                        {
                            annotationName = xmlElement2.name();
                            break;
                        }
                    }
                }
            } 
        }

        return new AbstractMap.SimpleEntry<String,XmlElements>(annotationName, xmlElements);
    }

    /** Gets original XML attribute name. 
     * 
     * @param Field f
     * 
     * @return String
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * 
     * */
    public static String getXmlAttributeName(Field f) throws IllegalArgumentException, IllegalAccessException
    {
        String ret = null;

        if (null != f)
        {
            XmlAttribute xmlAttribute = AnnotationUtil.getXmlAttribute(f);
            if (null != xmlAttribute && !("##default".equals(xmlAttribute.name())))
            {
                ret = xmlAttribute.name();
            }
            else
            {
                ret = f.getName();
            }
        }

        return ret;
    }

    /** Provides list of selected class including base classes until the ignored base class
     *  annotations of the element class. 
     * 
     * @return Set<String>
     * 
     * */
    public static List<Annotation> collectClassesAnnotations(Object obj, Class<? extends Annotation> annotationClazz, Class<?> ignoreBaseClass)
    {
        List<Annotation> ret = new ArrayList<Annotation>();

        if (null != obj)
        {
            Class<?> clazz = obj.getClass();

            do
            {
                Annotation annotation = clazz.getAnnotation(annotationClazz);
                if (null != annotation)
                {
                    ret.add(0, annotation);
                }
                clazz = clazz.getSuperclass();

            } while (null != clazz && clazz != ignoreBaseClass);
        }

        return ret;
    }

    /** Provides field names of all (including inherited) child elements  of the element.
     * 
     * @param Object obj
     * @param Class<?> ignoreBaseClass
     * 
     * @return Set<String>
     * 
     * */
    public static Set<String> collectProps(Object obj, Class<?> ignoreBaseClass)
    {
        Set<String> ret = new LinkedHashSet<String>();

        List<Annotation> annotations = AnnotationUtil.collectClassesAnnotations(obj, 
                XmlType.class, ignoreBaseClass);
        for (Annotation a : annotations)
        {
            XmlType xmlType = (XmlType)a;
            if (xmlType.propOrder().length > 0)
            {
                for (String prop : xmlType.propOrder())
                {
                    if (null != prop && !prop.isEmpty())
                    {
                        ret.add(prop);
                    }
                }
            }
        }

        return ret;
    }

    /** Checks whether xmlElementName is applicable for the filed
     * 
     * @param Field f
     * @param String xmlElementName
     * 
     * @return boolean indicating 
     * 
     * */
    public static boolean isXmlElementChoice(Field f, String xmlElementName)
    {
        boolean ret = false;

        if (null != f && null != xmlElementName)
        {
            XmlElements xmlElements = getXmlElements(f);
            if (null != xmlElements && null != xmlElements.value())
            {
                for (XmlElement xmlElement : xmlElements.value())
                {
                    if (xmlElementName.equals(xmlElement.name()))
                    {
                        ret = true;
                        break;
                    }
                }
            }
        }

        return ret;
    }
}
