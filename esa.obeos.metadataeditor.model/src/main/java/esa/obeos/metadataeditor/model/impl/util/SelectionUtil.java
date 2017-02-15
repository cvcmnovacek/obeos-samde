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

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElements;

import esa.obeos.metadataeditor.model.api.exc.AmbiguityException;

public class SelectionUtil
{
    private SelectionUtil()
    {    
    }
    
    public static Map<String, Entry<Object, Method>> getOptions(final List<Entry<Object, Method>> methods) throws Exception
    {
        if (null != methods && methods.isEmpty())
        {
            throw new Exception("list of methods is null or empty");
        }

        Map<String, Entry<Object, Method>> ret = new LinkedHashMap<String, Entry<Object, Method>>();
        
        for (Entry<Object, Method> method : methods)
        {
            XmlElementDecl xmlElementDecl = AnnotationUtil.getXmlElementDecl(method.getValue());
            
            if (null != xmlElementDecl && null != xmlElementDecl.name())
            {
                ret.put(xmlElementDecl.name(), method);
            }
        }

        return ret;
    }

    public static Entry<Object, Method> select(final List<Entry<Object, Method>> methods, final String choose) throws Exception
    {
        Map<String, Entry<Object, Method>> options = getOptions(methods);

        if (null == choose)
        {
            throw new AmbiguityException(options.keySet());
        }

        Entry<Object, Method> ret = options.get(choose);
        if (null == ret)
        {
            throw new Exception("invalid element selection '" + choose + "'");
        }

        return ret;
    }

    public static Class<?> select(final Set<Class<?>> classes, final String choose) throws Exception
    {
        if (null != classes && classes.isEmpty())
        {
            throw new Exception("annotation XmlElements is null or empty");
        }
        
        Map<String, Class<?>> options = new LinkedHashMap<String, Class<?>>();
        
        for (Class<?> clazz : classes)
        {
            options.put(clazz.getSimpleName(), clazz);
        }

        if (null == choose)
        {
            throw new AmbiguityException(options.keySet());
        }

        Class<?> ret = options.get(choose);
        if (null == ret)
        {
            throw new Exception("invalid element selection '" + choose + "'");
        }
        
        return ret;
    }
    
    public static Class<?> select(XmlElements xmlElements, String choose) throws Exception
    {
        if (null != xmlElements && null != xmlElements.value()
                && 0 == xmlElements.value().length)
        {
            throw new Exception("annotation XmlElements is null or empty");
        }
        
        Map<String, Class<?>> options = new LinkedHashMap<String, Class<?>>();
        
        for (XmlElement xmlElement : xmlElements.value())
        {
            options.put(xmlElement.name(), xmlElement.type());
        }

        if (null == choose)
        {
            throw new AmbiguityException(options.keySet());
        }

        Class<?> ret = options.get(choose);
        if (null == ret)
        {
            throw new Exception("invalid element selection '" + choose + "'");
        }
        
        return ret;
    }
    
    public static XmlElementRef select(XmlElementRefs xmlElementRefs, String choose) throws Exception
    {
        if (null != xmlElementRefs && null != xmlElementRefs.value()
                && 0 == xmlElementRefs.value().length)
        {
            throw new Exception("annotation XmlElements is null or empty");
        }
        
        Map<String, XmlElementRef> options = new LinkedHashMap<String, XmlElementRef>();
        
        for (XmlElementRef xmlElementRef : xmlElementRefs.value())
        {
            options.put(xmlElementRef.name(), xmlElementRef);
        }

        if (null == choose)
        {
            throw new AmbiguityException(options.keySet());
        }

        XmlElementRef ret = options.get(choose);
        if (null == ret)
        {
            throw new Exception("invalid element selection '" + choose + "'");
        }
        
        return ret;
    }
}
