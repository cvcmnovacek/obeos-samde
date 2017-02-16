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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import esa.obeos.metadataeditor.common.util.DomUtil;
import esa.obeos.metadataeditor.common.util.TimeUtil;
import esa.obeos.metadataeditor.model.api.Debug;
import esa.obeos.metadataeditor.model.api.IMdeElement;
import esa.obeos.metadataeditor.model.api.IMdeVisitor;
import esa.obeos.metadataeditor.model.api.INotify;
import esa.obeos.metadataeditor.model.api.Model;
import esa.obeos.metadataeditor.model.api.ModelVersion;
import esa.obeos.metadataeditor.model.api.exc.AmbiguityException;
import esa.obeos.metadataeditor.model.impl.internal.api.IElementStrategy;
import esa.obeos.metadataeditor.model.impl.jxb.JAXBEx;
import esa.obeos.metadataeditor.model.impl.util.AnnotationUtil;
import esa.obeos.metadataeditor.model.impl.util.AttributeUtil;
import esa.obeos.metadataeditor.model.impl.util.ReflectionUtil;
import esa.obeos.metadataeditor.model.impl.util.SelectionUtil;

@XmlTransient
public class MdeElement implements IMdeElement
{
    /** The default string value */
    @XmlTransient
    protected static final String DEFAULT_STR_VALUE = "";

    /** The default numeric value */
    @XmlTransient
    protected static final String DEFAULT_NUM_VALUE = "0";

    /** element name */
    @XmlTransient
    protected String m_xmlElementName = null;

    /** element type name */
    @XmlTransient
    private String m_xmlTypeName = null;

    /** element class field name */
    @XmlTransient
    private String m_fieldName = null;

    /** element annotation name */
    @XmlTransient
    private String m_annotationName = null;

    /** MdeElement children */
    @XmlTransient
    private Map<String, IMdeElement> m_mapChildElements = null;

    /** MdeElement list children */
    @XmlTransient
    private Map<String, List<?>> m_mapChildListElements = null;

    /** MdeElement children fields */
    @XmlTransient
    private Map<String, Field> m_mapChildrenFields = null;

    /** map of attributes */
    @XmlTransient
    private Map<String, Object> m_mapAttributes = null;

    /** MdeElement attributes fields */
    @XmlTransient
    private Map<String, Field> m_mapAttributesFields = null;

    /** map of required children */
    @XmlTransient
    private Map<String, Boolean> m_mapRequiredChildren = null;

    /** map of required attributes */
    @XmlTransient
    private Map<String, Boolean> m_mapRequiredAttributes = null;

    /** list of the substituted children */
    @XmlTransient
    private List<MdeSubstElement> m_substitutedChildren = null;

    /** JAXBElement */
    @XmlTransient
    protected JAXBElement<?> m_jaxbElement = null;

    /** All children elements */
    @XmlTransient
    private Map<String, Object> m_mapAllChildElements = null;

    /** The parent element */
    @XmlTransient
    protected MdeElement m_parent = null;

    /**
     * Constructor
     */
    public MdeElement()
    {
    }

    /**
     * Constructor @
     */
    public MdeElement(String fieldName, MdeElement parent)
    {
        this.m_fieldName = fieldName;
        this.m_parent = parent;
    }

    /**
     * Conversion constructor
     */
    public MdeElement(JAXBElement<?> jaxbElement, String xmlTypeName)
    {
        this.m_jaxbElement = jaxbElement;
        this.m_xmlTypeName = xmlTypeName;
    }

    /**
     * Sets a wrapping JAXBElement<this>
     * 
     * @param JAXBElement<?>
     *            jaxbElement
     * @param String
     *            xmlTypeName
     * 
     */
    public void setJaxbElement(JAXBElement<?> jaxbElement, String xmlTypeName) throws Exception
    {
        if (null == jaxbElement)
        {
            throw new IllegalArgumentException("jaxbElement cannot be null");
        }

        this.m_jaxbElement = jaxbElement;
        this.m_xmlTypeName = xmlTypeName;

        QName qName = this.m_jaxbElement.getName();
        if (null != qName)
        {
            String xmlName = qName.getLocalPart();
            this.m_xmlElementName = xmlName;
        }
    }

    /**
     * Gets the wrapping JAXBElement<this>
     * 
     * @return JAXBElement<?> jaxbElement
     * 
     */
    public JAXBElement<?> getJaxbElement()
    {
        return this.m_jaxbElement;
    }

    /**
     * Provides field name of this XML element.
     * 
     * @return String
     */
    public String getFieldName() throws Exception
    {
        if (null == this.m_fieldName)
        {
            collect();
        }
        
        return this.m_fieldName;
    }

    /**
     * Sets name of this XML element.
     * 
     * @return String
     */
    public void setXmlElementName(String name)
    {
        this.m_xmlElementName = name;
    }

    /**
     * Provides annotation name of this XML element.
     * 
     * @return String
     */
    public String getAnnotationName() throws Exception
    {
        if (null == this.m_annotationName)
        {
            collect();
        }

        return this.m_annotationName;
    }

    /**
     * Sets annotation name of this XML element.
     * 
     * @parameter String annotationName
     */
    public void setAnnotationName(String annotationName)
    {
        this.m_annotationName = annotationName;
    }

    protected void setAnnotationName(XmlElement xmlElement)
    {
        if (!"##default".equals(xmlElement.name()))
        {
            this.m_annotationName = xmlElement.name();
        }
    }

    /**
     * Sets annotation name of this XML element.
     * 
     * @parameter Field f
     */
    protected void setAnnotationName(Field f)
    {
        setAnnotationName(f, this);
    }

    /**
     * Sets annotation name of this XML element.
     * 
     * @parameter Field f
     * @parameter Object object
     */
    protected void setAnnotationName(Field f, Object object)
    {
        XmlElements xmlElements = AnnotationUtil.getXmlElements(f);
        if (null != xmlElements && null != xmlElements.value())
        {
            if (null != object)
            {
                for (XmlElement xmlElement : xmlElements.value())
                {
                    Class<?> clazz = xmlElement.type();
                    if (clazz.isAssignableFrom(object.getClass()))
                    {
                        setAnnotationName(xmlElement);
                    }
                }
            }
            else
            {
                this.m_annotationName = null;
            }
        }
        else
        {
            XmlElement xmlElement2 = AnnotationUtil.getXmlElement(f);
            if (null != xmlElement2)
            {
                setAnnotationName(xmlElement2);
            }
        }
    }

    /**
     * Sets class field name of this XML element.
     * 
     * @param String
     *            name
     */
    public void setFieldName(String fieldName)
    {
        this.m_fieldName = fieldName;
    }

    /**
     * Sets the parent element.
     * 
     * @param MdeElement
     *            parent
     */
    public void setParent(MdeElement parent)
    {
        this.m_parent = parent;
    }

    /**
     * Gets the parent element.
     * 
     * @return IMdeElement
     */
    public IMdeElement getParent()
    {
        return this.m_parent;
    }

    /**
     * Is element required.
     * 
     * @return boolean
     */
    @Override
    public boolean isRequired() throws Exception
    {
        boolean ret = true;

        if (null != m_parent)
        {
            return m_parent.isChildRequired(m_fieldName);
        }

        return ret;
    }

    /** Collects information about children, attributes and values. */
    private void collect() throws Exception
    {
        process(new CollectElementStrategy());
    }

    /**
     * Creates children, attributes and values of the element.
     * 
     * @param IChoice
     *            choice
     * 
     */
    public void createChildren(final String choose, final INotify notify) throws Exception
    {
        process(new CreateElementStrategy());
    }

    /** Process element children, attributes and values. */
    private void process(IElementStrategy strategy) throws Exception
    {
        m_mapChildElements = new LinkedHashMap<String, IMdeElement>();
        m_mapChildListElements = new LinkedHashMap<String, List<?>>();
        m_mapChildrenFields = new LinkedHashMap<String, Field>();
        m_mapAttributes = new LinkedHashMap<String, Object>();
        m_mapAttributesFields = new LinkedHashMap<String, Field>();
        m_mapRequiredChildren = new LinkedHashMap<String, Boolean>();
        m_mapRequiredAttributes = new LinkedHashMap<String, Boolean>();
        m_mapAllChildElements = new LinkedHashMap<String, Object>();

        Set<String> props = AnnotationUtil.collectProps(this, MdeElement.class);

        List<Class<?>> classes = ReflectionUtil.getClassHierarchy(this, MdeElement.class);

        for (Class<?> clazz : classes)
        {
            Field[] fields = clazz.getDeclaredFields();

            for (Field f : fields)
            {
                if (AnnotationUtil.isTransient(f))
                {
                    // ignore transient fields
                    continue;
                }

                if (AnnotationUtil.isValue(f))
                {
                    strategy.processValue(f, this);
                }
                else if (AnnotationUtil.isAttribute(f))
                {
                    strategy.processAttribute(f, this);
                }
                else if (props.contains(f.getName()))
                {
                    strategy.processElement(f, this);
                }
            }
        }
    }

    /**
     * Traverse a List<?>
     */
    private void traverse(String fieldName, List<?> list, INotify notify) throws Exception
    {
        if (!list.isEmpty())
        {
            int i = 0;
            for (Object listElement : list)
            {
                if (listElement instanceof MdeElement)
                {
                    String fn = fieldName + "[" + i++ + "]";
                    ((MdeElement) listElement).setFieldName(fieldName);
                    ((MdeElement) listElement).traverse(fn, notify);
                }
                else if (listElement instanceof JAXBElement<?>)
                {
                    JAXBElement<?> jaxbElement = (JAXBElement<?>) listElement;
                    traverse(this, fieldName + "[" + i++ + "]", jaxbElement, notify);
                }
                else
                {
                    traverse(this, fieldName + "[" + i++ + "]", listElement, notify);
                }
            }
        }
    }

    /**
     * Traverse a JAXBElement<?>
     */
    private static void traverse(MdeElement parent, String fieldName, JAXBElement<?> jaxbElement, INotify notify)
            throws Exception
    {
        Class<?> clazz = jaxbElement.getDeclaredType();

        if (MdeElement.class.isAssignableFrom(clazz))
        {
            MdeElement element = (MdeElement) jaxbElement.getValue();
            element.setJaxbElement(jaxbElement, "");
            element.traverse(fieldName, notify);
        }
        else
        {
            MdeElement element = new MdeElement(jaxbElement, "");
            element.setParent(parent);
            element.setFieldName(fieldName);
            notify.startElement(element, null);
            notify.endElement(element);
        }
    }

    /**
     * Traverse an Object
     */
    private static void traverse(MdeElement parent, String fieldName, Object obj, INotify notify) throws Exception
    {
        MdeElement element = new MdeElement(fieldName, parent);

        notify.startElement(element, null);

        element.collect();
        element.traverse(fieldName, notify);

        notify.endElement(element);
    }

    private List<String> getPossibleNames(String fieldName, boolean includeChoices)
    {
        ArrayList<String> ret = new ArrayList<String>();
        Field f = this.m_mapChildrenFields.get(fieldName);
        XmlElement xmlElement = AnnotationUtil.getXmlElement(f);
        XmlElements xmlElements = null;

        if (includeChoices)
        {
            xmlElements = AnnotationUtil.getXmlElements(f);
        }

        if (null != xmlElement && !("##default".equals(xmlElement.name())))
        {
            ret.add(xmlElement.name());
        }
        else if (null != xmlElements && null != xmlElements.value())
        {
            for (XmlElement xmlElement2 : xmlElements.value())
            {
                if (null != xmlElement2 && !"##default".equals(xmlElement2.name()))
                {
                    ret.add(xmlElement2.name());
                }
            }
        }
        else
        {
            ret.add(fieldName);
        }

        return ret;
    }

    // interface IMdeElement impl.

    /**
     * Provides the list of creatable child names
     * 
     * @returns List<String>
     */
    @Override
    public List<String> getCreatableChildNames(boolean includeChoices) throws Exception
    {
        List<String> ret = new ArrayList<String>();

        if (null == this.m_mapAllChildElements)
        {
            collect();
        }

        for (Entry<String, Object> childEntry : this.m_mapAllChildElements.entrySet())
        {
            String fieldName = childEntry.getKey();
            if (null == childEntry.getValue())
            {
                ret.addAll(getPossibleNames(fieldName, includeChoices));
                continue;
            }

            if (this.m_mapChildListElements.containsKey(fieldName))
            {
                ret.addAll(getPossibleNames(fieldName, includeChoices));
            }
        }

        return ret;
    }

    /**
     * Provides the parent element
     * 
     * @returns IMdeElement
     */
    public IMdeElement getXmlParent()
    {
        return this.m_parent;
    }

    /**
     * Provides the occurences of the given child
     * 
     * @param String
     *            childName
     * 
     * @ return int
     */
    @Override
    public int getChildOcurrences(String childName) throws Exception
    {
        int ret = 0;

        if (null == this.m_mapAllChildElements)
        {
            collect();
        }

        Object child = this.m_mapAllChildElements.get(childName);
        if (null != child)
        {
            if (child instanceof List<?>)
            {
                ret = ((List<?>) child).size();
            }
            else
            {
                ret = 1;
            }
        }

        return ret;
    }

    /**
     * Checks whether there are multiple occurrences of the given element
     * 
     * @ return boolean
     */
    @Override
    public boolean hasMultipleOccurrences() throws Exception
    {
        boolean ret = false;

        if (null != this.m_parent && 1 < this.m_parent.getChildOcurrences(this.m_fieldName))
        {
            ret = true;
        }

        return ret;
    }

    /**
     * Provides name of this XML element.
     * 
     * @return String
     */
    @Override
    public String getXmlElementName() throws Exception
    {
        if (null == this.m_fieldName)
        {
            collect();
        }

        String ret = this.m_fieldName;

        if (null != this.m_xmlElementName)
        {
            ret = this.m_xmlElementName;
        }
        else if (null != this.m_annotationName)
        {
            ret = this.m_annotationName;
        }
        else if (null != this.m_jaxbElement)
        {
            QName qName = this.m_jaxbElement.getName();
            if (null != qName)
            {
                this.m_xmlElementName = qName.getLocalPart();
            }
        }
        return ret;
    }

    /**
     * Provides XML type name of the element.
     * 
     * @return String
     */
    @Override
    public String getXmlElementType()
    {
        if (null == this.m_xmlTypeName)
        {
            this.m_xmlTypeName = AnnotationUtil.getXmlType(getClass());
        }
        return this.m_xmlTypeName;
    }

    /**
     * Provides map<filedName,childElement> child elements of the element.
     * 
     * @return Map<String, IMdeElement>
     */
    @Override
    public Map<String, IMdeElement> getChildElements() throws Exception
    {
        if (null == this.m_mapChildElements)
        {
            collect();
        }

        return m_mapChildElements;
    }

    /**
     * Provides map<filedName,childListElement> child elements of the element.
     * 
     * @return Map<String, List<? extends MdeElement>>
     */
    @Override
    public Map<String, List<?>> getChildListElements() throws Exception
    {
        if (null == m_mapChildListElements)
        {
            collect();
        }

        return m_mapChildListElements;
    }

    /**
     * Provides map<filedName,childElement> child elements of the element.
     * 
     * @return Map<String, IMdeElement>
     */
    @Override
    public IMdeElement getChildByXmlName(String childXmlName) throws Exception
    {
        IMdeElement ret = null;

        if (null == childXmlName)
        {
            throw new NullPointerException("childXmlName is null");
        }

        if (null == this.m_mapChildElements)
        {
            collect();
        }

        for (IMdeElement child : m_mapChildElements.values())
        {
            if (null != child)
            {
                if (childXmlName.equals(child.getXmlElementName()))
                {
                    ret = child;
                    break;
                }
            }
        }

        return ret;
    }

    public IMdeElement getChildByXmlName(String childXmlName, Integer index) throws Exception
    {
        IMdeElement ret = null;

        if (null == childXmlName)
        {
            throw new NullPointerException("childXmlName is null");
        }

        if (null == index)
        {
            throw new NullPointerException("index is null");
        }

        if (0 > index)
        {
            throw new IllegalArgumentException("the negative index is not allowed");
        }

        if (null == this.m_mapChildListElements)
        {
            collect();
        }

        for (Entry<String, List<?>> childEntry : m_mapChildListElements.entrySet())
        {
            List<?> childList = childEntry.getValue();

            if (null == childList || childList.isEmpty() || childList.size() <= index)
            {
                continue;
            }

            IMdeElement mdeElement = null;
            Object child = childList.get(index);
            if (child instanceof IMdeElement)
            {
                mdeElement = (IMdeElement) child;
            }
            else if (child instanceof JAXBElement<?>)
            {
                JAXBElement<?> jaxbElement = ((JAXBElement<?>) child);
                if (jaxbElement.getValue() instanceof IMdeElement)
                {
                    mdeElement = (IMdeElement) (jaxbElement.getValue());
                }
                else
                {
                    if (Debug.enabled) System.out.println("a child is not the 'JAXBElement<? extends IMdeElement>' type "
                            + "but it has type '" + jaxbElement.getValue().getClass().getName() + "'");
                }
            }
            else if(null==child)
            {
            }
            else
            {
                if (Debug.enabled) System.out.println("a child type is '" + child.getClass().getName() + "'");
            }

            if (null != mdeElement)
            {
                if (childXmlName.equals(mdeElement.getXmlElementName()))
                {
                    ret = mdeElement;
                    break;
                }
            }
        }

        return ret;
    }

    public List<?> getChildrenByXmlName(String childXmlName) throws Exception
    {
        List<?> ret = null;

        if (null == childXmlName)
        {
            throw new NullPointerException("childXmlName is null");
        }

        if (null == this.m_mapChildListElements)
        {
            collect();
        }

        ret = m_mapChildListElements.get(childXmlName);
        if (null == ret)
        {

            for (Entry<String, List<?>> childEntry : m_mapChildListElements.entrySet())
            {
                List<?> childList = childEntry.getValue();
                if (null == childList || childList.isEmpty())
                {
                    continue;
                }

                IMdeElement mdeElement = null;
                Object child = childList.get(0);
                if (child instanceof IMdeElement)
                {
                    mdeElement = (IMdeElement) child;
                }
                else if (child instanceof JAXBElement<?>)
                {
                    JAXBElement<?> jaxbElement = ((JAXBElement<?>) child);
                    if (jaxbElement.getValue() instanceof IMdeElement)
                    {
                        mdeElement = (IMdeElement) (jaxbElement.getValue());
                    }
                    else
                    {
                        if (Debug.enabled) System.out.println("a child is not the 'JAXBElement<? extends IMdeElement>' type "
                                + "but it has type '" + jaxbElement.getValue().getClass().getName() + "'");
                    }
                }
                else if(null==child)
                {
                }
                else
                {
                    if (Debug.enabled) System.out.println("a child type is '" + child.getClass().getName() + "'");
                }

                if (null != mdeElement)
                {
                    if (childXmlName.equals(mdeElement.getXmlElementName()))
                    {
                        ret = childList;
                        break;
                    }
                }
            }
        }

        return ret;
    }


    /**
     * Provides map<attribName,attribute> attributes of the element.
     * 
     * @return Map<String, Object>
     */
    @Override
    public Map<String, Object> getAttribs() throws Exception
    {
        if (null == m_mapAttributes)
        {
            collect();
        }

        return m_mapAttributes;
    }

    /**
     * Provides the value of element's attribute.
     * 
     * @param String
     *            name
     * @return Object value
     * 
     * @throws Exception
     */
    @Override
    public Class<?> getAttributeValueType(String name) throws Exception
    {
        if (null == name)
        {
            throw new NullPointerException("attributeName is null");
        }

        if (null == m_mapAttributesFields)
        {
            collect();
        }

        if (!this.m_mapAttributesFields.containsKey(name))
        {
            throw new IllegalArgumentException("XML attribute '" + name + "' is not defined for this XML element");
        }

        Field f = this.m_mapAttributesFields.get(name);
        if (null == f)
        {
            throw new NullPointerException("attribute field is null");
        }

        return f.getType();
    }

    /**
     * Provides the value of element's attribute.
     * 
     * @param String
     *            name
     * @return Object value
     * 
     * @throws Exception
     */
    @Override
    public Object getAttributeValue(final String name) throws Exception
    {
        Object ret = null;

        if (null == name)
        {
            throw new NullPointerException("attributeName is null");
        }

        if (null == m_mapAttributesFields)
        {
            collect();
        }

        if (!this.m_mapAttributesFields.containsKey(name))
        {
            throw new IllegalArgumentException("XML attribute '" + name + "' is not defined for this XML element");
        }

        Field f = this.m_mapAttributesFields.get(name);
        if (null == f)
        {
            throw new NullPointerException("attribute field is null");
        }

        f.setAccessible(true);
        ret = f.get(this);
        f.setAccessible(false);

        if (ret instanceof List)
        {
            ret = list2String((List<?>)ret);
        }
        return ret;
    }

    /**
     * Allows to set the value of element's attribute.
     * 
     * @param String
     *            name
     * @param String
     *            value
     * 
     * @throws Exception
     */
    @Override
    public void setAttributeValue(final String name, final String value) throws Exception
    {
        if (null == name)
        {
            throw new NullPointerException("attributeName is null");
        }

        if (null == m_mapAttributesFields)
        {
            collect();
        }

        if (!this.m_mapAttributesFields.containsKey(name))
        {
            throw new IllegalArgumentException("XML attribute '" + name + "' is not defined for this XML element");
        }

        if (isAttributeRequired(name))
        {
            if (null == value)
            {
                throw new NullPointerException("attribute value cannot be set to 'null' if the attribute is required");
            }
        }

        if (null != value && AttributeUtil.hasAttributeConstrain(name))
        {
            AttributeUtil.validateAttributeValue(name, value);
        }

        Field f = this.m_mapAttributesFields.get(name);
        if (null == f)
        {
            throw new NullPointerException("attribute field is null");
        }

        Object val = value;

        Class<?> clazz = f.getType();
        if (String.class == clazz)
        {
        }
        else if (List.class == clazz)
        {
            if (value instanceof String)
            {
                Class<?> parametrizedType = ReflectionUtil.getFirstParametrizedType(f.getGenericType());

                String tmp = ((String) value).replace("[", "");
                tmp = tmp.replace("]", "");
                String[] listValues = tmp.split(",");

                if (null != listValues)
                {
                    List<Object> list = new ArrayList<Object>();
                    for (String s : listValues)
                    {
                        if (String.class == parametrizedType)
                        {
                            list.add(s);
                        }
                        else
                        {
                            throw new Exception("Unsupported attribute type '" + clazz.getSimpleName()
                            + "' with parameterized type '" + parametrizedType.getSimpleName() + "'.");
                        }
                    }

                    if (null != list && !list.isEmpty())
                    {
                        val = list;
                    }
                }
            }
        }
        else
        {
            throw new Exception("Unsupported attribute value '" + clazz.getSimpleName() + "'.");
        }

        f.setAccessible(true);
        f.set(this, val);
        f.setAccessible(false);

        updateAttribute(name, f, val);
    }

    @Override
    public boolean isChildRequired(String fieldName) throws Exception
    {
        boolean ret = false;

        if (null == this.m_mapRequiredChildren)
        {
            collect();
        }

        Boolean required = this.m_mapRequiredChildren.get(fieldName);

        if (null != required)
        {
            ret = required;
        }

        return ret;
    }

    @Override
    public boolean isAttributeRequired(String attributeName) throws Exception
    {
        boolean ret = false;

        if (null == this.m_mapRequiredAttributes)
        {
            collect();
        }

        Boolean required = this.m_mapRequiredAttributes.get(attributeName);

        if (null != required)
        {
            ret = required;
        }

        return ret;
    }

    /**
     * Traverse meta data editor model
     */
    private void traverseNoInitNotify(String fieldName, INotify notify) throws Exception
    {
        if (null == this.m_mapAllChildElements)
        {
            collect();
        }

        for (Entry<String, Object> child : this.m_mapAllChildElements.entrySet())
        {
            if (null != child && null != child.getKey() && null != child.getValue())
            {
                if (child.getValue() instanceof MdeElement)
                {
                    MdeElement mdeElement = (MdeElement) child.getValue();
                    {
                        mdeElement.traverse(child.getKey(), notify);
                    }
                }
                else if (child.getValue() instanceof List)
                {
                    List<?> list = (List<?>) child.getValue();
                    traverse(child.getKey(), list, notify);
                }
                else if (child.getValue() instanceof JAXBElement<?>)
                {
                    JAXBElement<?> jaxbElement = (JAXBElement<?>) child.getValue();
                    traverse(this, child.getKey(), jaxbElement, notify);
                }
                else
                {
                    traverse(this, child.getKey(), child.getValue(), notify);
                }
            }
        }
    }

    /**
     * Traverse meta data editor model
     */
    @Override
    public void traverse(String fieldName, INotify notify) throws Exception
    {
        notify.startElement(this, null);

        traverseNoInitNotify(fieldName, notify);

        notify.endElement(this);
    }

    /**
     * Traverse meta data editor model, find the first global index
     */
    @Override
    public void traverseInitIndex(String fieldName, INotify notify) throws Exception
    {
        notify.startElement(this, getMyGlobalIndex());

        traverseNoInitNotify(fieldName, notify);

        notify.endElement(this);
    }

    @Override
    public IMdeElement createChild(final String name, final String choose, final INotify notify) throws AmbiguityException, Exception
    {
        if (null == this.m_mapChildrenFields)
        {
            collect();
        }

        String fieldName = name;
        Field f = this.m_mapChildrenFields.get(name);
        if (null == f)
        {
            fieldName = null;
            // the child field name not found but it still can be OK in case it comes from either JAXB annotation or XSD substitutions
            for (Entry<String, Field> entryField : this.m_mapChildrenFields.entrySet())
            {
                fieldName = getFieldNameFromXmlName(name, entryField.getValue());
                if (null != fieldName)
                {
                    f = entryField.getValue();
                    break;
                }
            }

            if (null == f)
            {
                throw new NullPointerException("The child '" + name + "' cannot be created in element '" + getXmlElementType() + "'.");
            }
        }

        return ElementFactory.createChildElement(this, fieldName, choose, f, notify);
    }

    private String getFieldNameFromXmlElementRef(final String xmlElementName, final Field f, final XmlElementRef xmlElementRef) throws Exception
    {
        String ret = null;

        if (null != xmlElementRef)
        {
            String xmlElementRefName = xmlElementRef.name();
            if (xmlElementRefName.equals(xmlElementName))
            {
                ret = f.getName();
            }
            else
            {
                if (null == this.m_parent)
                {
                    throw new NullPointerException("Parent is null, cannot resolve the package name.");
                }

                List<Entry<Object, Method>> factoryMethods = JAXBEx.getAllSuitableFactoryMethods(xmlElementRefName, this.m_parent.getClass().getPackage());
                if (null != factoryMethods && !factoryMethods.isEmpty())
                {
                    Map<String, Entry<Object, Method>> options = SelectionUtil.getOptions(factoryMethods);
                    for (String key : options.keySet())
                    {
                        if (key.equals(xmlElementName))
                        {
                            ret = f.getName();
                            break;
                        }
                    }
                }
                else
                {
                    throw new Exception("No suitable JAXB factory method for XmlElementRef.name() '" + xmlElementRefName + "'.");
                }
            }
        }

        return ret;
    }

    private String getFieldNameFromXmlName(final String xmlElementName, final Field f) throws Exception
    {
        String ret = null;

        for (;;)
        {
            XmlElement xmlElement = AnnotationUtil.getXmlElement(f);
            if (null != xmlElement)
            {
                if (xmlElement.name().equals(xmlElementName))
                {
                    ret = f.getName();
                }
                break;
            }

            if (AnnotationUtil.isXmlElementChoice(f, xmlElementName))
            {
                ret = f.getName();
                break;
            }

            XmlElementRef xmlElementRef = AnnotationUtil.getXmlElementRef(f);
            if (null != xmlElementRef)
            {
                ret = getFieldNameFromXmlElementRef(xmlElementName, f, xmlElementRef);
                break;
            }

            XmlElementRefs xmlElementRefs = AnnotationUtil.getXmlElementRefs(f);
            if (null != xmlElementRefs)
            {
                if (null != xmlElementRefs && null != xmlElementRefs.value())
                {
                    for (XmlElementRef xmlElementRef2 : xmlElementRefs.value())
                    {
                        ret = getFieldNameFromXmlElementRef(xmlElementName, f, xmlElementRef2);
                        if (null != ret)
                        {
                            break;
                        }
                    }
                }
            }

            break;
        }

        return ret;
    }

    private void removeEmptyList(final Field f, List<?> list) throws IllegalArgumentException, IllegalAccessException
    {
        if (list.isEmpty())
        {
            f.setAccessible(true);
            f.set(this, null);
            f.setAccessible(false);

            this.m_mapChildListElements.put(f.getName(), null);
            this.m_mapAllChildElements.put(f.getName(), null);

            if (Debug.enabled) System.out.println("the empty list deleted");
        }
    }

    @Override
    public void deleteChild(IMdeElement childElement) throws Exception
    {
        if (null == childElement)
        {
            throw new NullPointerException("childElement is null");
        }

        String fieldName = ((MdeElement) childElement).getFieldName();

        if (!this.m_mapAllChildElements.containsKey(fieldName))
        {
            throw new IllegalArgumentException(
                    "child '" + fieldName + "' is not supported be this element '" + getXmlElementType() + "'");
        }

        Object objChild = this.m_mapAllChildElements.get(fieldName);
        if (null == objChild)
        {
            throw new NullPointerException("child '" + fieldName + "' is currently not available and cannot be delted");
        }

        Field f = this.m_mapChildrenFields.get(fieldName);
        if (null == f)
        {
            throw new NullPointerException(
                    "missing filed of child '" + fieldName + "' in element '" + getXmlElementType() + "'");
        }

        if (this.m_mapChildListElements.containsKey(fieldName))
        {
            List<?> list = this.m_mapChildListElements.get(fieldName);
            for (Object obj : list)
            {
                if (obj instanceof MdeElement)
                {
                    MdeElement element = (MdeElement) obj;
                    if (childElement.equals(element))
                    {
                        list.remove(childElement);
                        if (Debug.enabled) System.out.println("child '" + fieldName + "' in a list deleted");

                        removeEmptyList(f, list);
                        break;
                    }
                }
                else if (obj instanceof JAXBElement)
                {
                    JAXBElement<?> jaxbElement = ((MdeElement)childElement).getJaxbElement();
                    if (obj.equals(jaxbElement))
                    {
                        list.remove(obj);
                        if (Debug.enabled) System.out.println("child '" + fieldName + "' in a list deleted");

                        removeEmptyList(f, list);
                        break;
                    }
                }
                else
                {
                    if (childElement instanceof MdeSubstElement)
                    {
                        MdeSubstElement element = (MdeSubstElement)childElement;
                        Object obj2 = element.getElementValue();
                        if (obj == obj2)
                        {
                            list.remove(obj);
                            this.removeSubstitute(element);
                            if (Debug.enabled) System.out.println("child '" + fieldName + "' in a list deleted");

                            removeEmptyList(f, list);
                            break;
                        }
                    }
                    else
                    {
                        throw new Exception("Cannot delete child '" + childElement.getXmlElementName() 
                        + "' from the list '" + this.m_parent.getXmlElementName() 
                        + "'. Unsupported child type '" + childElement.getClass().getSimpleName() + "'.");
                    }
                }
            }
        }
        else
        {
            f.setAccessible(true);
            f.set(this, null);
            f.setAccessible(false);

            this.m_mapChildElements.put(fieldName, null);
            this.m_mapAllChildElements.put(fieldName, null);

            if (Debug.enabled) System.out.println("simple child '" + fieldName + "' deleted");
        }
    }


    @Override
    public boolean isValue()
    {
        boolean ret = (null != ReflectionUtil.getDeclaredField(this, MdeElement.class, "value"));

        if (!ret && null != this.m_jaxbElement)
        {
            if (AnnotationUtil.isValueType(this.m_jaxbElement.getDeclaredType()))
            {
                ret = true;
            }
        }

        return ret;
    }

    @Override
    public Class<?> getElementValueType() throws Exception
    {
        Class<?> ret = null;

        Field f = ReflectionUtil.getDeclaredField(this, MdeElement.class, "value");
        if (null != f)
        {
            ret = f.getType();
            if (Object.class == ret)
            {
                f.setAccessible(true);
                Object value = f.get(this);
                f.setAccessible(false);
                if (null != value)
                {
                    ret = value.getClass();
                }
            }
        }

        if (null == ret && null != this.m_jaxbElement)
        {
            ret = this.m_jaxbElement.getDeclaredType();
            if (MdeElement.class.isAssignableFrom(ret))
            {
                ret = null;
            }
        }

        if (null != ret && ret.isPrimitive())
        {
            ret = ReflectionUtil.getPrimitiveObjectEquivalent(ret);
        }

        return ret;
    }

    @Override
    public Object getElementValue() throws Exception
    {
        Object ret = null;

        Field f = ReflectionUtil.getDeclaredField(this, MdeElement.class, "value");
        if (null != f)
        {
            f.setAccessible(true);
            ret = f.get(this);
            f.setAccessible(false);
        }

        if (null == ret 
                && null != this.m_jaxbElement 
                && AnnotationUtil.isValueType(this.m_jaxbElement.getDeclaredType()))
        {
            ret = this.m_jaxbElement.getValue();
        }

        return ret;
    }

    protected Object createElementValue(final Field f, Class<?> clazz, final String value) throws Exception
    {
        Object ret = null;

        if (null == clazz)
        {
            // the previous value type is not available
            clazz = f.getType();
        }
        if (String.class == clazz || BigInteger.class == clazz || Integer.class == clazz || Long.class == clazz
                || Short.class == clazz || BigDecimal.class == clazz || Double.class == clazz || Boolean.class == clazz
                || Byte.class == clazz)
        {
            Constructor<?> ctor = clazz.getConstructor(String.class);
            String val = value;
            if (null == val || val.isEmpty())
            {
                if (Number.class.isAssignableFrom(clazz))
                {
                    val = DEFAULT_NUM_VALUE;
                }
                else
                {
                    val = DEFAULT_STR_VALUE;
                }
            }
            ret = ctor.newInstance(val);
        }
        else if (XMLGregorianCalendar.class.isAssignableFrom(clazz))
        {
            ret = TimeUtil.toCalendar(value);
        }
        else if (Duration.class.isAssignableFrom(clazz))
        {
            ret = DatatypeFactory.newInstance().newDuration(value);
        }
        else if (List.class == clazz)
        {
            Class<?> parametrizedType = ReflectionUtil.getFirstParametrizedType(f.getGenericType());

            String tmp = ((String) value).replace("[", "");
            tmp = tmp.replace("]", "");
            String[] listValues = tmp.split(",");

            if (null != listValues)
            {
                List<Object> list = new ArrayList<Object>();
                for (String s : listValues)
                {
                    if (Double.class == parametrizedType)
                    {
                        list.add(new Double(s));
                    }
                    else if (String.class == parametrizedType)
                    {
                        list.add(s);
                    }
                    else
                    {
                        throw new Exception("Unssuported value type '" + clazz.getSimpleName()
                        + "' with parameterized type '" + parametrizedType.getSimpleName() + "'.");
                    }
                }
                ret = list;
            }
        }
        else
        {
            throw new IllegalArgumentException("Unsupported value type '" + clazz.getSimpleName() + "'");
        }

        return ret;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setElementValue(final Object value) throws Exception
    {
        Field f = ReflectionUtil.getDeclaredField(this, MdeElement.class, "value");
        if (null != f)
        {
            Class<?> clazz = f.getType();
            Object typedValue = value;

            if (List.class == clazz)
            {
                Type type = f.getGenericType();
                Class<?> parametrizedType = ReflectionUtil.getFirstParametrizedType(type);

                if (null != value)
                {
                    if (List.class.isAssignableFrom(value.getClass()))
                    {
                        Class<?> objectParametrizedType = ReflectionUtil.getFirstParametrizedType(value.getClass());
                        if (parametrizedType != objectParametrizedType)
                        {
                            throw new IllegalArgumentException("The filed parametrized type of the list is '" 
                                    + parametrizedType.getSimpleName() + "' but the object parametrized type of the lits is '" 
                                    + objectParametrizedType.getSimpleName() + "'.");
                        }
                    }
                    else if (value instanceof String)
                    {
                        typedValue = createElementValue(f, null, String.class.cast(value));
                    }
                    else
                    {
                        throw new Exception("Unsuported value type '" + value.getClass() + "' for the element '" 
                                + getXmlElementName() + "' value list.");
                    }
                }
                else
                {
                    // create an empty list
                    typedValue = new ArrayList();
                }
            }
            else if(null == value || value instanceof String)
            {
                typedValue = createElementValue(f, null, String.class.cast(value));
            }

            f.setAccessible(true);
            f.set(this, typedValue);
            f.setAccessible(false);
        }
        else if (null != this.m_jaxbElement && this.m_jaxbElement.getDeclaredType().isAssignableFrom(value.getClass()))
        {
            f = this.m_jaxbElement.getClass().getDeclaredField("value");
            f.setAccessible(true);
            f.set(this.m_jaxbElement, value);
            f.setAccessible(false);
        }
        else
        {
            throw new Exception("The XML element '" + getXmlElementName() + "' has no value.");
        }
    }

    public void updateSimpleChild(Field f, MdeElement element) throws Exception
    {
        String fieldName = f.getName();

        if (null != element)
        {
            if (element.getFieldName() != fieldName)
            {
                element.setFieldName(fieldName);
            }
            if (this != element.getParent())
            {
                element.setParent(this);
            }
        }

        this.m_mapChildElements.put(fieldName, element);
        this.m_mapAllChildElements.put(fieldName, element);
        this.m_mapChildrenFields.put(fieldName, f);
        this.m_mapRequiredChildren.put(fieldName, AnnotationUtil.isXmlElementRequired(f));
    }

    public void updateListChild(Field f, List<?> list) throws Exception
    {
        String fieldName = f.getName();

        if (null != list)
        {
            for (Object obj : list)
            {
                MdeElement element = null;

                if (obj instanceof MdeElement)
                {
                    element = (MdeElement)obj;
                }
                else if (obj instanceof JAXBElement<?>)
                {
                    JAXBElement<?> jaxbElement = (JAXBElement<?>) obj;
                    if (jaxbElement.getValue() instanceof MdeElement)
                    {
                        element = (MdeElement)jaxbElement.getValue();
                    }
                }

                if (null != element)
                {
                    if (element.getFieldName() != fieldName)
                    {
                        element.setFieldName(fieldName);
                    }
                    if (this != element.getParent())
                    {
                        element.setParent(this);
                    }
                }
            }
        }

        this.m_mapChildListElements.put(fieldName, list);
        this.m_mapAllChildElements.put(fieldName, list);
        this.m_mapChildrenFields.put(fieldName, f);
        this.m_mapRequiredChildren.put(fieldName, AnnotationUtil.isXmlElementRequired(f));
    }

    public void updateAttribute(String name, Field f, Object object)
    {
        this.m_mapAttributes.put(name, object);
        this.m_mapAttributesFields.put(name, f);
        this.m_mapRequiredAttributes.put(name, AnnotationUtil.isXmlAttributeRequired(f));
    }

    public void addSubstitute(MdeSubstElement elm)
    {
        if (null == this.m_substitutedChildren)
        {
            this.m_substitutedChildren = new ArrayList<MdeSubstElement>();
        }
        this.m_substitutedChildren.add(elm);
    }

    public void removeSubstitute(MdeSubstElement elm)
    {
        if (null != this.m_substitutedChildren)
        {
            this.m_substitutedChildren.remove(elm);

            if (this.m_substitutedChildren.isEmpty())
            {
                this.m_substitutedChildren = null;
            }
            else
            {
                int i = 0;
                for (MdeSubstElement e : m_substitutedChildren)
                {
                    e.updateIndex(i++);
                }
            }
        }
    }

    public Integer getNewChildIndex(String fieldName)
    {
        Integer ret = null;

        int index = 0;
        Iterator<Entry<String, Object>> it = this.m_mapAllChildElements.entrySet().iterator();
        while (it.hasNext())
        {
            Entry<String, Object> e = it.next();

            if (Debug.enabled) System.out.println("index " + index + " '" + e.getKey() + "'");
            if (e.getValue() instanceof List<?>)
            {
                List<?> list = (List<?>) e.getValue();
                index += list.size();

                if (e.getKey().equals(fieldName))
                {
                    ret = new Integer(index);
                    if (Debug.enabled) System.out.println("*** INDEX " + index + " '" + fieldName + "'");
                    break;
                }
            }
            else if (e.getKey().equals(fieldName))
            {
                ret = new Integer(index);
                if (Debug.enabled) System.out.println("*** INDEX " + index + " '" + fieldName + "'");
                break;
            }
            else if (null != e.getValue())
            {
                index++;
            }
        }

        return ret;
    }

    @SuppressWarnings("unchecked")
    @Override
    public IMdeElement insertChild(IMdeElement child, String fieldName, Integer index, INotify notify) throws Exception
    {
        IMdeElement ret = null;

        if (null == child)
        {
            throw new NullPointerException("insertion of a null child is not allowed");
        }

        MdeElement mdeChild = (MdeElement) child;
        mdeChild.setParent(this);
        mdeChild.collect();

        String childXmlName = mdeChild.getXmlElementName();

        if (null == m_mapChildrenFields)
        {
            collect();
        }

        Field f = this.m_mapChildrenFields.get(fieldName);
        if (null == f)
        {
            throw new Exception("The child '" + childXmlName + "' cannot be inserted to parent '" + getXmlElementName()
            + "' field '" + fieldName + "'");
        }

        mdeChild.setFieldName(fieldName);

        if (this.m_mapChildListElements.containsKey(fieldName))
        {
            boolean newList = false;

            List<?> list = this.m_mapChildListElements.get(fieldName);
            if (null == list)
            {
                list = new ArrayList<IMdeElement>();
                newList = true;
            }

            Class<?> listParametrizedClass = ReflectionUtil.getFirstParametrizedType(f.getGenericType());
            if (null == listParametrizedClass)
            {
                throw new Exception("Missing parametrized class in the list.");
            }

            if (listParametrizedClass.isAssignableFrom(child.getClass()))
            {
                if (!listParametrizedClass.equals(child.getClass()))
                {
                    JAXBEx.canBeInserted(childXmlName, getXmlElementName(), f, getClass().getPackage());
                }

                if (null == index || index < 0 || list.size() <= index)
                {
                    ((List<IMdeElement>) list).add(child);
                }
                else
                {
                    ret = ((List<IMdeElement>) list).set(index, child);
                }
            }
            else if (JAXBElement.class.equals(listParametrizedClass))
            {
                JAXBEx.canBeInsertedAsJaxbElement(childXmlName, getXmlElementName(), f, getClass().getPackage());

                JAXBElement<?> jaxbElement = JAXBEx.createJAXBElement(child);
                if (jaxbElement.getValue() instanceof MdeElement)
                {
                    ((MdeElement) jaxbElement.getValue()).setJaxbElement(jaxbElement, "");
                }

                if (index < 0 || list.size() <= index)
                {
                    ((List<JAXBElement<?>>) list).add(jaxbElement);
                }
                else
                {
                    JAXBElement<?> old = ((List<JAXBElement<?>>) list).set(index, jaxbElement);
                    if (null != old && old.getValue() instanceof MdeElement)
                    {
                        ret = (MdeElement) old.getValue();
                    }
                }
            }
            else
            {
                throw new Exception("The child '" + childXmlName + "' cannot be inserted to parent '"
                        + getXmlElementName() + "' because of child's type '" + child.getClass()
                        + "' is not supported by list's type '" + listParametrizedClass.getName() + "'");
            }

            if (newList)
            {
                f.setAccessible(true);
                f.set(this, list);
                f.setAccessible(false);
            }
        }
        else if (this.m_mapChildElements.containsKey(fieldName))
        {
            Class<?> fieldClass = f.getType();

            if (fieldClass.isAssignableFrom(child.getClass()))
            {
                if (!fieldClass.equals(child.getClass()))
                {
                    JAXBEx.canBeInserted(childXmlName, getXmlElementName(), f, getClass().getPackage());
                }

                f.setAccessible(true);
                f.set(this, child);
                f.setAccessible(false);

                ret = this.m_mapChildElements.put(childXmlName, child);
            }
            else if (JAXBElement.class.equals(fieldClass))
            {
                JAXBEx.canBeInsertedAsJaxbElement(childXmlName, getXmlElementName(), f, getClass().getPackage());

                JAXBElement<?> jaxbElement = JAXBEx.createJAXBElement(child);
                if (jaxbElement.getValue() instanceof MdeElement)
                {
                    ((MdeElement) jaxbElement.getValue()).setJaxbElement(jaxbElement, "");
                }

                f.setAccessible(true);
                f.set(this, jaxbElement);
                f.setAccessible(false);

                ret = this.m_mapChildElements.put(fieldName, child);
            }
            else
            {
                throw new Exception("The child '" + childXmlName + "' cannot be inserted to the parent '"
                        + getXmlElementName() + "' because of the unsupported type '" + fieldClass.getName() + "'");
            }
        }
        else
        {
            throw new Exception(
                    "The child '" + childXmlName + "' cannot be inserted to the parent '" + getXmlElementName() + "'");
        }

        return ret;
    }

    @SuppressWarnings("unchecked")
    protected Object insertChildListElement(final JAXBElement<?> jaxbElement, final String childXmlName,
            final String fieldName, final Field f, final Integer index, final INotify notify) throws Exception
    {
        Object ret = null;

        Object child = jaxbElement.getValue();

        boolean newList = false;

        List<?> list = this.m_mapChildListElements.get(fieldName);
        if (null == list)
        {
            list = new ArrayList<IMdeElement>();
            newList = true;
        }

        if (child instanceof MdeElement)
        {
            ((MdeElement) child).setParent(this);
            ((MdeElement) child).setFieldName(fieldName);
        }

        Class<?> listParametrizedClass = ReflectionUtil.getFirstParametrizedType(f.getGenericType());
        if (null == listParametrizedClass)
        {
            throw new Exception("Missing parametrized class in the list.");
        }

        if (listParametrizedClass.isAssignableFrom(child.getClass()))
        {
            if (!listParametrizedClass.equals(child.getClass()))
            {
                JAXBEx.canBeInserted(childXmlName, getXmlElementName(), f, getClass().getPackage());
            }

            if (index < 0 || list.size() <= index)
            {
                ((List<Object>) list).add(child);
            }
            else
            {
                ret = ((List<Object>) list).set(index, child);
            }
        }
        else if (JAXBElement.class.equals(listParametrizedClass))
        {
            JAXBEx.canBeInsertedAsJaxbElement(childXmlName, getXmlElementName(), f, getClass().getPackage());

            if (index < 0 || list.size() <= index)
            {
                ((List<JAXBElement<?>>) list).add(jaxbElement);
            }
            else
            {
                JAXBElement<?> old = ((List<JAXBElement<?>>) list).set(index, jaxbElement);
                if (null != old && old.getValue() instanceof MdeElement)
                {
                    ret = (MdeElement) old.getValue();
                }
            }
        }
        else
        {
            throw new Exception("The child '" + childXmlName + "' cannot be inserted to parent '" + getXmlElementName()
            + "' because of child's type '" + child.getClass() + "' is not supported by list's type '"
            + listParametrizedClass.getName() + "'");
        }

        if (newList)
        {
            f.setAccessible(true);
            f.set(this, list);
            f.setAccessible(false);
        }

        return ret;
    }

    protected Object insertChildElement(final JAXBElement<?> jaxbElement, final String childXmlName,
            final String fieldName, final Field f, final INotify notify) throws Exception
    {
        Object ret = null;

        Object child = jaxbElement.getValue();

        Class<?> fieldClass = f.getType();

        if (fieldClass.isAssignableFrom(child.getClass()))
        {
            if (!fieldClass.equals(child.getClass()))
            {
                JAXBEx.canBeInserted(childXmlName, getXmlElementName(), f, getClass().getPackage());
            }

            f.setAccessible(true);
            f.set(this, child);
            f.setAccessible(false);

            MdeElement mdeElement = new MdeSubstElement(fieldName, this, f, child);
            ret = this.m_mapChildElements.put(childXmlName, mdeElement);
        }
        else if (JAXBElement.class.equals(fieldClass))
        {
            JAXBEx.canBeInsertedAsJaxbElement(childXmlName, getXmlElementName(), f, getClass().getPackage());

            f.setAccessible(true);
            f.set(this, jaxbElement);
            f.setAccessible(false);

            MdeElement mdeElement = new MdeSubstElement(fieldName, this, f, child);
            ret = this.m_mapChildElements.put(fieldName, mdeElement);
            mdeElement.setJaxbElement(jaxbElement, "");
        }
        else
        {
            throw new Exception("The child '" + childXmlName + "' cannot be inserted to the parent '"
                    + getXmlElementName() + "' because of the unsupported type '" + fieldClass.getName() + "'");
        }

        return ret;
    }

    protected Object insertChild(final JAXBElement<?> jaxbElement, final String fieldName, final Field f,
            final Integer index, final INotify notify) throws Exception
    {
        Object ret = null;

        String childXmlName = jaxbElement.getName().getLocalPart();

        if (this.m_mapChildListElements.containsKey(fieldName))
        {
            ret = insertChildListElement(jaxbElement, childXmlName, fieldName, f, index, notify);
        }
        else if (this.m_mapChildElements.containsKey(fieldName))
        {
            ret = insertChildElement(jaxbElement, childXmlName, fieldName, f, notify);
        }
        else
        {
            throw new Exception(
                    "The child '" + childXmlName + "' cannot be inserted to the parent '" + getXmlElementName() + "'");
        }

        return ret;
    }

    protected Object insertChild(final Document domDocument, final String fieldName, final Field f, final Integer index,
            final INotify notify) throws Exception
    {
        Object ret = null;

        if (null == domDocument)
        {
            throw new NullPointerException("insertion of null DOM document is not allowed");
        }

        Element domElement = domDocument.getDocumentElement();

        if (null == domElement)
        {
            throw new NullPointerException("insertion of empty DOM document is not allowed");
        }

        String localName = domElement.getLocalName();

        Class<?> targetClass = null;
        List<Class<?>> classes = JAXBEx.getSubstitutingClasses(f, getClass().getPackage());
        if (null != classes && !classes.isEmpty())
        {
            if (1 == classes.size())
            {
                targetClass = classes.get(0);
            }
            else
            {
                for (Class<?> clazz : classes)
                {
                    String xmlType = AnnotationUtil.getXmlType(clazz);

                    if (xmlType.contains(localName))
                    {
                        targetClass = clazz;
                        break;
                    }
                }
            }
        }

        if (null == targetClass)
        {
            throw new Exception("Couldn't found a JAXB generated class for the conversion.");
        }

        if (Debug.enabled) System.out.println(
                "trying to convert the DOM document to the instance of the class '" + targetClass.getName() + "'");
        JAXBElement<?> jaxbElement = Model.convert(domDocument, targetClass, false);

        if (jaxbElement.getValue() instanceof MdeElement)
        {
            ret = insertChild((MdeElement) jaxbElement.getValue(), fieldName, index, notify);
        }
        else
        {
            ret = insertChild(jaxbElement, fieldName, f, index, notify);
        }
        return ret;
    }

    @Override
    public Object insertChild(String xmlFragment, String fieldName, Integer index, INotify notify) throws Exception
    {
        if (null == xmlFragment)
        {
            throw new NullPointerException("insertion of a null XML fragment is not allowed");
        }

        if (null == m_mapChildrenFields)
        {
            collect();
        }

        Field f = this.m_mapChildrenFields.get(fieldName);
        if (null == f)
        {
            throw new Exception("The XML fragment cannot be inserted to parent '" + getXmlElementName() + "' field '"
                    + fieldName + "'");
        }

        Document domDocument = DomUtil.createDocumentFromXmlFragment(xmlFragment);

        return insertChild(domDocument, fieldName, f, index, notify);
    }

    @Override
    public Object insertChild(final Element domElement, final String fieldName, final Integer index,
            final INotify notify) throws Exception
    {
        if (null == domElement)
        {
            throw new NullPointerException("insertion of a null DOM element is not allowed");
        }

        if (null == m_mapChildrenFields)
        {
            collect();
        }

        Field f = this.m_mapChildrenFields.get(fieldName);
        if (null == f)
        {
            throw new Exception("The XML fragment cannot be inserted to parent '" + getXmlElementName() + "' field '"
                    + fieldName + "'");
        }

        Document domDocument = DomUtil.createDocumentFromElement(domElement);

        return insertChild(domDocument, fieldName, f, index, notify);
    }

    @Override
    public String getXmlFragment() throws Exception
    {
        if (Debug.enabled) System.out.println("XML fragment of '" + getXmlElementName() + "'");
        return Model.toString(this);
    }

    public String getChildFieldName(final String childXmlName) throws Exception
    {
        String ret = null;

        if (null == childXmlName)
        {
            throw new NullPointerException("childXmlName is null");
        }

        if (null == m_mapChildrenFields)
        {
            collect();
        }

        for (Entry<String, Field> entry : this.m_mapChildrenFields.entrySet())
        {
            String fieldName = entry.getKey();

            if (childXmlName.equals(fieldName))
            {
                ret = fieldName;
                break;
            }
            else
            {
                Field f = entry.getValue();

                XmlElement xmlElement = AnnotationUtil.getXmlElement(f);
                if (null != xmlElement)
                {
                    if (childXmlName.equals(xmlElement.name()))
                    {
                        ret = fieldName;
                        break;
                    }
                }

                XmlElements xmlElements = AnnotationUtil.getXmlElements(f);
                if (null != xmlElements && null != xmlElements.value())
                {
                    for (XmlElement choiceXmlElement : xmlElements.value())
                    {
                        if (childXmlName.equals(choiceXmlElement.name()))
                        {
                            ret = fieldName;
                            break;
                        }
                    }

                }

                XmlElementRef xmlElementRef = AnnotationUtil.getXmlElementRef(f);
                if (null != xmlElementRef)
                {
                    List<Entry<Object, Method>> methods = JAXBEx.getFactoryMethods(xmlElementRef.name(),
                            this.getClass().getPackage());
                    for (Entry<Object, Method> methodEntry : methods)
                    {
                        Method method = methodEntry.getValue();
                        if (null != method)
                        {
                            XmlElementDecl xmlElementDecl = AnnotationUtil.getXmlElementDecl(method);
                            if (null != xmlElementDecl)
                            {
                                if (childXmlName.equals(xmlElementDecl.name()))
                                {
                                    ret = fieldName;
                                    break;
                                }
                            }
                        }
                    }
                }

                XmlElementRefs xmlElementRefs = AnnotationUtil.getXmlElementRefs(f);
                if (null != xmlElementRefs && null != xmlElementRefs.value())
                {
                    for (XmlElementRef choiceXmlElementRef : xmlElementRefs.value())
                    {
                        List<Entry<Object, Method>> methods = JAXBEx.getFactoryMethods(choiceXmlElementRef.name(),
                                this.getClass().getPackage());
                        for (Entry<Object, Method> methodEntry : methods)
                        {
                            Method method = methodEntry.getValue();
                            if (null != method)
                            {
                                XmlElementDecl xmlElementDecl = AnnotationUtil.getXmlElementDecl(method);
                                if (null != xmlElementDecl)
                                {
                                    if (childXmlName.equals(xmlElementDecl.name()))
                                    {
                                        ret = fieldName;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return ret;
    }

    public Integer getMyIndex() throws Exception
    {
        Integer ret = null;

        if (null != this.m_parent)
        {
            Map<String, List<?>> multiChildrenMap = this.m_parent.getChildListElements();
            if (null != multiChildrenMap)
            {
                List<?> childList = multiChildrenMap.get(this.m_fieldName);
                if (null != childList)
                {
                    for (Integer index = 0; index < childList.size(); index++)
                    {
                        Object child = childList.get(index);
                        if (this.equals(child))
                        {
                            ret = index;
                        }
                    }
                }
            }
        }

        return ret;
    }

    public Integer getMyGlobalIndex() throws Exception
    {
        Integer ret = null;

        if (null != this.m_parent)
        {
            Map<String, Object> allChildrenMap = this.m_parent.m_mapAllChildElements;
            if (null != allChildrenMap)
            {
                Object me = allChildrenMap.get(this.m_fieldName);
                if (null != me)
                {
                    Collection<Object> allChildren = allChildrenMap.values();
                    Integer index = 0;
                    for (Object child : allChildren)
                    {
                        if (null != child)
                        {
                            if (me.equals(child))
                            {
                                ret = index;
                                break;
                            }
                            index++;
                        }
                    }

                    if (me instanceof List)
                    {
                        ret += getMyIndex();
                    }
                }
                else
                {
                    throw new Exception("parent doesn't contain me");
                }
            }
        }

        return ret;
    }

    public String getPath() throws Exception
    {
        String ret = "/" + getXmlElementName();

        Integer index = getMyIndex();
        if (null != index)
        {
            ret += "[" + index + "]";
        }

        if (null != this.m_parent)
        {
            ret = this.m_parent.getPath() + ret;
        }

        return ret;
    }

    public void validateInspire() throws Exception
    {
        ModelVersion modelVersion = Model.getModelVersion(getClass());

        validateInspire(modelVersion);
    }

    public void validateInspire(final ModelVersion modelVersion) throws Exception
    {
        if (null == this.m_mapAllChildElements)
        {
            collect();
        }

        InspireConstraints.check(this, modelVersion);

        for (Object objChild : this.m_mapAllChildElements.values())
        {
            if (null != objChild)
            {
                if (objChild instanceof List)
                {
                    List<?> listChild = (List<?>) objChild;
                    for (Object objListChild : listChild)
                    {
                        if (objListChild instanceof MdeElement)
                        {
                            ((MdeElement) objListChild).validateInspire(modelVersion);
                        }
                    }
                }
                else if (objChild instanceof IMdeElement)
                {
                    ((MdeElement) objChild).validateInspire(modelVersion);
                }
            }
        }
    }

    public static String list2String(List<?> list) throws Exception
    {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++)
        {
            Object obj = list.get(i);
            if (obj instanceof IMdeElement)
            {
                sb.append(((IMdeElement)obj).getElementValue().toString());
            }
            else
            {
                sb.append(obj.toString());
            }

            if (i < list.size() - 1)
            {
                sb.append(", ");
            }
        }
        sb.append("]");

        return sb.toString();

    }

    protected void makeCollected() throws Exception
    {
        if (null == m_mapAllChildElements)
        {
            collect();
        }
    }

    @Override
    public void accept(final IMdeVisitor visitor) throws Exception
    {
        if (null != visitor)
        {
            makeCollected();

            // indicate the begin of the visit
            visitor.beginVisit(this);

            // visit the simple children
            for (IMdeElement child : this.m_mapChildElements.values())
            {
                if (null != child)
                {
                    child.accept(visitor);
                }
            }

            // visit the list children
            for (List<?> list : this.m_mapChildListElements.values())
            {
                if (null != list)
                {
                    for (Object child : list)
                    {
                        if (child instanceof IMdeElement)
                        {
                            ((IMdeElement)child).accept(visitor);
                        }
                        else if (child instanceof JAXBElement)
                        {
                            // visit also IMdeElements wrapped in a JAXBElement
                            Object value = ((JAXBElement<?>)child).getValue();
                            if (value instanceof IMdeElement)
                            {
                                ((IMdeElement)value).accept(visitor);
                            }
                        }
                    }
                }
            }

            if (null != this.m_substitutedChildren && !this.m_substitutedChildren.isEmpty())
            {
                // visit also children wrapped by the MdeSubstElement
                for (IMdeElement child : this.m_substitutedChildren)
                {
                    if (null != child)
                    {
                        child.accept(visitor);
                    }
                }
            }

            // indicate the end of the visit
            visitor.endVisit(this);
        }
    }

    public int getSimpleChildCount()
    {
        int ret = 0;

        try
        {
            makeCollected();

            for (IMdeElement child : this.m_mapChildElements.values())
            {
                if (null != child)
                {
                    ret++;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return ret;
    }

    public int getListChildCount()
    {
        int ret = 0;

        try
        {
            makeCollected();

            for (List<?> list : this.m_mapChildListElements.values())
            {
                if (null != list)
                {
                    ret += list.size();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return ret;
    }

    public int getSubstChildCount()
    {
        int ret = 0;

        try
        {
            makeCollected();

            if (null != this.m_substitutedChildren)
            {
                ret = this.m_substitutedChildren.size();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return ret;
    }

    public String toString()
    {
        String xmlElementName = "unknown";
        
        try
        {
            xmlElementName = getXmlElementName();
        }
        catch(Exception e)
        {
            // do nothing
        }
                
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(xmlElementName);
        sb.append(", simple children count: ");
        sb.append(getSimpleChildCount());
        sb.append(", list children count: ");
        sb.append(getListChildCount());
        sb.append(", subst children count: ");
        sb.append(getSubstChildCount());
        sb.append("]");

        return sb.toString();
    }
}
