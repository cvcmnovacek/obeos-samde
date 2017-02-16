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
import java.util.List;

import javax.xml.bind.JAXBElement;

import esa.obeos.metadataeditor.model.impl.util.AnnotationUtil;
import esa.obeos.metadataeditor.model.api.Debug;
import esa.obeos.metadataeditor.model.impl.internal.api.IElementStrategy;

public class CollectElementStrategy implements IElementStrategy
{
    @Override
    public void processElement(Field f, MdeElement parent)
    {
        try
        {
            String fieldName = f.getName();

            if (Debug.enabled) System.out.println("*** fieldName: " + fieldName + ", type: " + f.getType());

            f.setAccessible(true);
            Object objChild = f.get(parent);
            f.setAccessible(false);

            if (MdeElement.class.isAssignableFrom(f.getType()))
            {
                MdeElement element = ((MdeElement)objChild);
                if (null != element)
                {
                    element.setAnnotationName(f);
                }

                parent.updateSimpleChild(f, element);
            }
            else if (JAXBElement.class.isAssignableFrom(f.getType()) )
            {   
                MdeElement element = null;
                JAXBElement<?> jaxbElement = (JAXBElement<?>)objChild;
                if (null != jaxbElement)
                {
                    if (MdeElement.class.isAssignableFrom(jaxbElement.getDeclaredType()))
                    {
                        element = (MdeElement)jaxbElement.getValue();
                        element.setAnnotationName(f);
                        element.setJaxbElement(jaxbElement, "");
                    }
                    else
                    {
                        element = new MdeSubstElement(fieldName, parent, f, null);
                        element.setJaxbElement(jaxbElement, "");
                    }
                }

                parent.updateSimpleChild(f, element);
            }
            else if (List.class.isAssignableFrom(f.getType()))
            {
                List<?> listChildren = (List<?>)objChild;
                if( null != listChildren )
                {
                    for( Object obj : listChildren )
                    {
                        if( obj instanceof MdeElement )
                        {
                            ((MdeElement)obj).setAnnotationName(f);
                        }
                        else if( obj instanceof JAXBElement<?> )
                        {
                            JAXBElement<?> jaxbElement = (JAXBElement<?>)obj;
                            if( jaxbElement.getValue() instanceof MdeElement )
                            {
                                ((MdeElement)jaxbElement.getValue()).setAnnotationName(f);
                            }
                        }
                    }
                }

                parent.updateListChild(f, listChildren);
            }        
            else
            {
                MdeElement element = null;

                if (null != objChild)
                {
                    element = new MdeSubstElement(fieldName, parent, f, objChild);
                }
                parent.updateSimpleChild(f, element);
            }
        }
        catch (Exception e)
        {
            System.err.println("### EXCEPTION CollectElementStrategy#processElement(): " + e.getMessage());
        }
    }

    @Override
    public void processAttribute(Field f, MdeElement parent)
    {
        try
        {
            String attribName = AnnotationUtil.getXmlAttributeName(f);

            if (Debug.enabled) System.out.println("*** attributeName: " + attribName);

            f.setAccessible(true);
            Object value = f.get(parent);
            f.setAccessible(false);

            parent.updateAttribute(attribName, f, value);
        }
        catch (Exception e)
        {
            System.err.println("### EXCEPTION CollectElementStrategy#processAttribute(): " + e.getMessage());
        }
    }

    @Override
    public void processValue(Field f, MdeElement parent)
    {
        try
        {
            if (Debug.enabled) System.out.println("*** valueName: " + f.getName());
        }
        catch (Exception e)
        {
            System.err.println("### EXCEPTION CollectElementStrategy#processValue(): " + e.getMessage());
        }
    }
}
