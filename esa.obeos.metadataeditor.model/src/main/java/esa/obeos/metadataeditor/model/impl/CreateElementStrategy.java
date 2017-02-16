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

import esa.obeos.metadataeditor.model.api.Debug;
import esa.obeos.metadataeditor.model.impl.internal.api.IElementStrategy;
import esa.obeos.metadataeditor.model.impl.util.AnnotationUtil;

public class CreateElementStrategy implements IElementStrategy
{

    @Override
    public void processElement(Field f, MdeElement parent) throws Exception
    {
        String fieldName = f.getName();

        if (Debug.enabled) System.out.println("*** creating element '" + fieldName + "', type: " + f.getType());

        if (List.class.isAssignableFrom(f.getType()))
        {
            parent.updateListChild(f, null);
        }
        else
        {
            parent.updateSimpleChild(f, null);
        }
    }

    @Override
    public void processAttribute(Field f, MdeElement parent) throws Exception
    {
        String attribName = AnnotationUtil.getXmlAttributeName(f);

        if (Debug.enabled) System.out.println("*** creating attribute '" + attribName + "', type: " + f.getType());

        if (AnnotationUtil.isXmlAttributeRequired(f))
        {                        
            ElementFactory.createAttribute(parent, attribName, f);
        }
        else
        {
            parent.updateAttribute(attribName, f, null);
        }
    }

    @Override
    public void processValue(Field f, MdeElement parent) throws Exception
    {
        if (Debug.enabled) System.out.println("*** creating value '" + f.getName() + "', type: " + f.getType());

        ElementFactory.createValue(parent, f);
    }
}
