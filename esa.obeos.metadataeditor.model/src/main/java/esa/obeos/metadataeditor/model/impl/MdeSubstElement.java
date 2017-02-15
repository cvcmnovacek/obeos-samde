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

package esa.obeos.metadataeditor.model.impl;

import java.lang.reflect.Field;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlTransient;

import esa.obeos.metadataeditor.model.impl.util.ReflectionUtil;


@XmlTransient
public class MdeSubstElement extends MdeElement
{
    /** The value field */
    protected Field m_valueField = null;

    protected int index = -1;
    

    /**
     * Constructor @
     * @throws IllegalAccessException 
     */
    public MdeSubstElement(String fieldName, MdeElement parent, Field valueField, Object value)
    {
        super(fieldName, parent);
        
        if (null == fieldName)
        {
            throw new IllegalArgumentException("field name cannot be null");
        }

        if (null == parent)
        {
            throw new IllegalArgumentException("parent canot be null");
        }

        if (null == valueField)
        {
            throw new IllegalArgumentException("valueField canot be null");
        }

        this.m_valueField = valueField;

        setAnnotationName(this.m_valueField, value);
    }

    public MdeSubstElement(String fieldName, MdeElement parent, Field valueField, Object value, int index)
    {
        this(fieldName, parent, valueField, value);
        this.index = index;
    }

    @Override
    public boolean isValue()
    {
        return true;
    }

    @Override
    public Class<?> getElementValueType() throws Exception
    {
        Class<?> ret = this.m_valueField.getType();

        if (JAXBElement.class == ret && null != this.m_jaxbElement)
        {
            ret = m_jaxbElement.getDeclaredType();
        }
        else if (List.class.isAssignableFrom(ret))
        {
            ret = ReflectionUtil.getFirstParametrizedType(this.m_valueField.getGenericType());
        }

        if (Object.class == ret)
        {
            this.m_valueField.setAccessible(true);
            Object value = this.m_valueField.get(this.m_parent);
            this.m_valueField.setAccessible(false);
            if (null != value)
            {
                ret = value.getClass();
            }
        }

        return ret;
    }

    @Override
    public Object getElementValue() throws Exception
    {
        Object ret = null;

        if (null != this.m_jaxbElement)
        {
                Field f = this.m_jaxbElement.getClass().getDeclaredField("value");
                f.setAccessible(true);
                ret = f.get(this.m_jaxbElement);
                f.setAccessible(false);
        }
        else
        {
            this.m_valueField.setAccessible(true);
            Class<?> clazz = this.m_valueField.getType();
            if (List.class.isAssignableFrom(clazz))
            {
                List<?> list = (List<?>)this.m_valueField.get(this.m_parent);
                ret = list.get(this.index);
            }
            else
            {
                ret = this.m_valueField.get(this.m_parent);
            }
            this.m_valueField.setAccessible(false);
        }

        return ret;
    }

    @Override
    public void setElementValue(Object value) throws Exception
    {

        if (null != this.m_jaxbElement)
        {
            if (this.m_jaxbElement.getDeclaredType().isAssignableFrom(value.getClass()))
            {
                Field f = this.m_jaxbElement.getClass().getDeclaredField("value");
                f.setAccessible(true);
                f.set(this.m_jaxbElement, value);
                f.setAccessible(false);
            }
            else
            {
                throw new Exception("Cannot assign '" + value.getClass() + "' to 'JAXBElemen<"
                        + this.m_jaxbElement.getDeclaredType().getSimpleName() + ">'");
            }
        }
        else
        {
            this.m_valueField.setAccessible(true);

            Class<?> clazz = this.m_valueField.getType();
            if (List.class.isAssignableFrom(clazz))
            {
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>)this.m_valueField.get(this.m_parent);
                list.set(this.index, value);
            }
            else
            {
                Object typedValue = value;
                if (value instanceof String )
                {
                    if (Object.class == clazz)
                    {
                        Object oldValue = this.m_valueField.get(this.m_parent);
                        if (null != oldValue)
                        {
                            clazz = oldValue.getClass();
                        }
                    }

                    typedValue = createElementValue(this.m_valueField, clazz, String.class.cast(value));
                }

                this.m_valueField.set(this.m_parent, typedValue);
            }
            this.m_valueField.setAccessible(false);
        }

        setAnnotationName(this.m_valueField, value);
    }

    public void updateIndex(int index)
    {
        this.index = index;
    }
}
