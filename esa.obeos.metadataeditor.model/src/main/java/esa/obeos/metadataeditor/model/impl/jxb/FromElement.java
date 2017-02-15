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

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map.Entry;

import javax.xml.bind.JAXBElement;

import esa.obeos.metadataeditor.model.api.IMdeElement;
import esa.obeos.metadataeditor.model.api.Schemas;
import esa.obeos.metadataeditor.model.impl.MdeElement;

public class FromElement
{
    private URL schema = null;
    
    private JAXBElement<?> jaxbElement = null;

    public FromElement(final IMdeElement element, final boolean validate) throws Exception
    {
        if( null == element )
        {
            throw new NullPointerException("element is null");
        }

        Class<?> clazz = element.getClass();
        
        if (validate)
        {
            this.schema = Schemas.getSchema(clazz.getPackage());
        }

//        Entry<Object, Method> pair = JAXBEx.getFactoryMethod(clazz);
//        
//        if( null == pair || null == pair.getKey() || null == pair.getValue() )
//        {
//            throw new Exception("No factory method for '" + clazz.getName() + "'");
//        }
//
//        Object objectFactory = pair.getKey();
//        Method method = pair.getValue();
//
//        this.jaxbElement = (JAXBElement<?>)method.invoke(objectFactory, element);
   
        this.jaxbElement = ((MdeElement)element).getJaxbElement();
        
        if (null == this.jaxbElement)
        {
        	this.jaxbElement = JAXBEx.convert(element);
        }
    
    }

    public URL getSchema()
    {
        return this.schema;
    }

    public JAXBElement<?> getJaxbElement()
    {
        return this.jaxbElement;
    }
}
