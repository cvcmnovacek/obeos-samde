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

package esa.obeos.metadataeditor.model.impl;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import esa.obeos.metadataeditor.model.api.INotify;
import esa.obeos.metadataeditor.model.api.exc.AmbiguityException;
import esa.obeos.metadataeditor.model.impl.jxb.JAXBEx;
import esa.obeos.metadataeditor.model.impl.util.AnnotationUtil;
import esa.obeos.metadataeditor.model.impl.util.ReflectionUtil;
import esa.obeos.metadataeditor.model.impl.util.SelectionUtil;

public class ElementFactory
{
    public static MdeElement createChildElement(MdeElement parent, String fieldName, String choose, Field f, INotify notify) throws AmbiguityException, Exception
    {
        MdeElement ret = null;

        if (null == parent)
        {
            throw new Exception("parameter 'parent' is null");
        }

        if (null == fieldName)
        {
            throw new Exception("parameter 'fieldName' is null");
        }

        if (null == f)
        {
            throw new Exception("parameter 'f' is null");
        }

        Class<?> childClass = f.getType();
        if (MdeElement.class.isAssignableFrom(childClass))
        {
            ret = createMdeChild(parent, fieldName, choose, f, notify);

            parent.updateSimpleChild(f, ret);
        }
        else if (JAXBElement.class.isAssignableFrom(childClass))
        {
            ret = createJaxbChild(parent, fieldName, f, notify, choose);
            
            f.setAccessible(true);
            f.set(parent, ret.getJaxbElement());
            f.setAccessible(false);

            parent.updateSimpleChild(f, ret);
        }
        else if (List.class.isAssignableFrom(childClass))
        {
            f.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>)f.get(parent);
            f.setAccessible(false);

            if (null == list)
            {
                list = new ArrayList<Object>();

                f.setAccessible(true);
                f.set(parent, list);
                f.setAccessible(false);
            }

            if (list.isEmpty())
            {
                parent.updateListChild(f, list);
            }

            ret = createListChild(parent, fieldName, choose, list, f, notify);
        }
        else
        {
            if (Object.class == childClass && null != choose)
            {
                Class<?> choosedClazz = AnnotationUtil.getType(f, choose);
                if( null != choosedClazz && MdeElement.class.isAssignableFrom(choosedClazz))
                {
                    ret = createMdeChild(parent, fieldName, choose, f, notify);
                }
                else
                {
                    ret = createNonMdeChild(parent, fieldName, choose, f, notify);
                }
            }
            else
            {
                ret = createNonMdeChild(parent, fieldName, choose, f, notify);
            }

            parent.updateSimpleChild(f, ret);
        }

        return ret;
    }

    public static Object createAttribute(MdeElement parent, String name, Field f) throws Exception
    {
        Object ret = null;

        if (null == parent)
        {
            throw new Exception("parameter 'parent' is null");
        }

        if (null == name)
        {
            throw new Exception("parameter 'name' is null");
        }

        if (null == f)
        {
            throw new Exception("parameter 'f' is null");
        }

        Class<?> attribClass = f.getType();
        if (String.class.isAssignableFrom(attribClass))
        {
            ret = attribClass.getConstructor(String.class).newInstance("");
        }
        else
        {
            ret = attribClass.newInstance();
        }

        f.setAccessible(true);
        f.set(parent, ret);
        f.setAccessible(false);

        parent.updateAttribute(name, f, ret);

        return ret;
    }

    public static void createValue(final MdeElement element, final Field f) throws Exception
    {
        if (null == element)
        {
            throw new Exception("parameter 'parent' is null");
        }

        if (null == f)
        {
            throw new Exception("parameter 'f' is null");
        }

        if (f.getType().isPrimitive())
        {
            ReflectionUtil.initPrimitive(element, f);
        }
        else
        {
            element.setElementValue(null);
        }
    }


    private static void notifyAndCreateChildElements(final MdeElement parent, final MdeElement element, final String fieldName, 
            final String choose, final INotify notify) throws Exception
    {
        if (null != notify)
        {
            notify.startElement(element, parent.getNewChildIndex(fieldName));
        }

        element.createChildren(choose, notify);

        if (null != notify)
        {
            notify.endElement(element);
        }
    }

    private static Class<?> supportChildCreation(final MdeElement parent, final String fieldName, final String choose, final Field f) throws Exception
    {
        if (null == f)
        {
            throw new IllegalArgumentException("field is null");
        }

        Class<?> ret = f.getType();
        
        XmlElements xmlElements = AnnotationUtil.getXmlElements(f);
        if (null != xmlElements)
        {
            ret = SelectionUtil.select(xmlElements, choose);
        }
        
        return ret;
    }

    private static MdeElement createMdeChild(final MdeElement parent, final String fieldName, final String choose, 
            final Field f, final INotify notify) throws Exception
    {
        MdeElement ret = null;

        Class<?> clazz = supportChildCreation(parent, fieldName, choose, f);

        ret = (MdeElement)clazz.newInstance();
        ret.setAnnotationName(f, ret);
        ret.setFieldName(fieldName);
        ret.setParent(parent);

        f.setAccessible(true);
        f.set(parent, ret);
        f.setAccessible(false);

        notifyAndCreateChildElements(parent, ret, fieldName, choose, notify);

        return ret;
    }

    private static MdeElement createNonMdeChild(final MdeElement parent, final String fieldName, final String choose, 
            final Field f, final INotify notify) throws Exception
    {
        MdeElement ret = null;

        Class<?> clazz = supportChildCreation(parent, fieldName, choose, f);

        Object obj = createObject(clazz);
        ret = new MdeSubstElement(fieldName, parent, f, obj); 

        f.setAccessible(true);
        f.set(parent, obj);
        f.setAccessible(false);

        if (null != notify)
        {
            notify.startElement(ret, parent.getNewChildIndex(fieldName));
            notify.endElement(ret);
        }

        return ret;
    }

    private static MdeElement createListChild(final MdeElement parent, final String fieldName, final String choose, 
            final List<Object> list, final Field f, final INotify notify) throws Exception
    {
        MdeElement ret = null;

        if (null == list)
        {
            throw new Exception("list is null");
        }

        if (null == f)
        {
            throw new Exception("field is null");
        }

        XmlElementRefs xmlElementRefs = AnnotationUtil.getXmlElementRefs(f);
        XmlElements xmlElements = AnnotationUtil.getXmlElements(f);

        if (null != xmlElementRefs)
        {
            // It handles list defined like e.g.:
            // @XmlElementRefs({
            //    @XmlElementRef(name = "attributes", namespace = "http://www.isotc211.org/2005/gmd", type = JAXBElement.class),
            //    @XmlElementRef(name = "features", namespace = "http://www.isotc211.org/2005/gmd", type = JAXBElement.class),
            //    @XmlElementRef(name = "dataset", namespace = "http://www.isotc211.org/2005/gmd", type = JAXBElement.class),
            //    @XmlElementRef(name = "featureInstances", namespace = "http://www.isotc211.org/2005/gmd", type = JAXBElement.class),
            //    @XmlElementRef(name = "other", namespace = "http://www.isotc211.org/2005/gmd", type = JAXBElement.class),
            //    @XmlElementRef(name = "attributeInstances", namespace = "http://www.isotc211.org/2005/gmd", type = JAXBElement.class)
            // })
            // protected List<JAXBElement<? extends MdeElement>> attributesOrFeaturesOrFeatureInstances;           

            String prevChoose = choose;
            if (!list.isEmpty())
            {
                // always use the same child type -> the previously selected
                prevChoose = ((JAXBElement<?>)list.get(0)).getName().getLocalPart();
            }

            ret = createJaxbChild(parent, fieldName, f, notify, prevChoose);
            JAXBElement<?> jaxbElement = ret.getJaxbElement();
            list.add(jaxbElement);
        }
        else if (null != xmlElements)
        {
            // It handles list defined like e.g.:
            // @XmlElements({
            //    @XmlElement(name = "pointProperty", type = PointPropertyType.class),
            //    @XmlElement(name = "pos", type = DirectPositionType.class),
            //    @XmlElement(name = "posList", type = DirectPositionListType.class)
            // })
            // protected List<MdeElement> geometricPositionListGroup;


            String prevChoose = choose;
            if (!list.isEmpty())
            {
                // always use the same child type -> the previously selected
                prevChoose = ((MdeElement)list.get(0)).getXmlElementName();
            }

            Class<?> clazz = SelectionUtil.select(xmlElements, prevChoose);

            ret = (MdeElement)clazz.newInstance();
            ret.setAnnotationName(choose);
            ret.setFieldName(fieldName);
            ret.setParent(parent);

            notifyAndCreateChildElements(parent, ret, fieldName, choose, notify);
            list.add(ret);
        }
        else
        {
            // It handles list defined like e.g.:
            // protected List<GMPointPropertyType> cornerPoints

            Class<?> parameterizedClass = ReflectionUtil.getFirstParametrizedType(f.getGenericType());
            Object obj = parameterizedClass.newInstance();
            if (obj instanceof MdeElement)
            {
                ret = (MdeElement)obj;
                ret.setAnnotationName(f);
                ret.setFieldName(fieldName);
                ret.setParent(parent);

                notifyAndCreateChildElements(parent, ret, fieldName, choose, notify);
                list.add(ret);
            }
            else
            {
                MdeSubstElement subst = new MdeSubstElement(fieldName, parent, f, obj, list.size());
                ret = subst;

                if (null != notify)
                {
                    notify.startElement(ret, parent.getNewChildIndex(fieldName));
                    notify.endElement(ret);
                }
                list.add(obj);
                parent.addSubstitute(subst);
            }
        }

        return ret;
    }

    /**
     * Creates JAXBElement child and provides substituted element name if it is available
     * 
     * @param xmlElementRef
     * @param parameterizedclass
     * @param choice
     * @return Entry<JAXBElement<?>, String>
     * @throws Exception
     */
    private static MdeElement createJaxbChild(MdeElement parent, String fieldName, final Field f, final INotify notify, final String choose) throws Exception
    {
        MdeElement ret = null;

        if (null == parent)
        {
            throw new NullPointerException("parent is null");
        }

        XmlElementRef xmlElementRef = AnnotationUtil.getXmlElementRef(f);
        if (null == xmlElementRef)
        {
            XmlElementRefs xmlElementRefs = AnnotationUtil.getXmlElementRefs(f);
            if (null != xmlElementRefs)
            {
                xmlElementRef = SelectionUtil.select(xmlElementRefs, choose);
                if (null == xmlElementRef)
                {
                    throw new NullPointerException("Selected 'XmlElementRef' on JAXBElement creation is null");
                }
            }
            else
            {
                throw new Exception("JAXBElement field '" + fieldName + "' w/o 'XmlElementRef' or 'XmlElementRefs' annotation");
            }
        }

        JAXBElement<?> jaxbElement = JAXBEx.createJAXBElement(xmlElementRef.name(), choose, parent.getClass().getPackage());

        if (null == jaxbElement)
        {
            throw new NullPointerException("JAXBEx.createJAXBElement() returned null");
        }

        if (jaxbElement.getValue() instanceof MdeElement)
        {
            ret = (MdeElement)jaxbElement.getValue();
            ret.setFieldName(fieldName);
            ret.setParent(parent);
        }
        else
        {
            ret = new MdeSubstElement(fieldName, parent, f, jaxbElement.getValue());
        }

        ret.setJaxbElement(jaxbElement, "");

        notifyAndCreateChildElements(parent, ret, fieldName, choose, notify);

        return ret;
    }

    private static Object createObject(final Class<?> clazz) throws Exception
    {
        Object ret = null;

        if (clazz == String.class)
        {
            ret = clazz.newInstance();
        }
        else if (clazz == BigInteger.class)
        {
            ret = BigInteger.valueOf(0);
        }
        else if (clazz == Integer.class)
        {
            ret = Integer.valueOf(0);
        }
        else if (clazz == Long.class)
        {
            ret = Long.valueOf(0);
        }
        else if (clazz == Short.class)
        {
            ret = Short.valueOf((short)0);
        }
        else if(clazz == BigDecimal.class)
        {
            ret = BigDecimal.valueOf(0.0);
        }
        else if(clazz == Double.class)
        {
            ret = Double.valueOf(0.0);
        }
        else if(clazz == Boolean.class)
        {
            ret = Boolean.FALSE;
        }
        else if(clazz == Byte.class)
        {
            ret = Byte.valueOf((byte)0);
        }
        else if(clazz == XMLGregorianCalendar.class)
        {
            GregorianCalendar gregorianCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            gregorianCalendar.setTime(new Date());
            ret = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
        }
        else if(clazz == Byte[].class)
        {
            ret = new Byte[0];
        }
        else if (clazz == Duration.class)
        {
            ret = DatatypeFactory.newInstance().newDuration(0);
        }
        else if(clazz.isEnum())
        {
            Object enumConstans[] = clazz.getEnumConstants();
            if (null != enumConstans && enumConstans.length > 0)
            {
                ret = enumConstans[0];
            }
        }
        else
        {
            throw new Exception("unsupported value type '" + clazz.getName() + "'");
        }

        return ret;
    }
}
