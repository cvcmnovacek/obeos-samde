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

package esa.obeos.metadataeditor.jaxrs.client;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class NamespaceContextImpl implements NamespaceContext
{

    @Override
    public String getNamespaceURI(String prefix)
    {
        String ret = null;

        if (prefix == null) 
        {
            throw new IllegalArgumentException("No prefix provided!");
        }
        else if("gmi".equals(prefix)) 
        {
            ret = "http://www.isotc211.org/2005/gmi";
        }
        else if("gmd".equals(prefix)) 
        {
            ret = "http://www.isotc211.org/2005/gmd";
        }
        else if("gco".equals(prefix)) 
        {
            ret = "http://www.isotc211.org/2005/gco";
        }
        else if("gml".equals(prefix)) 
        {
            ret = "http://www.opengis.net/gml/3.2";
        }
        else 
        {
            ret = XMLConstants.NULL_NS_URI;
        }      

        return ret;
    }

    @Override
    public String getPrefix(String namespaceURI)
    {
        String ret = null;

        if (namespaceURI == null) 
        {
            throw new IllegalArgumentException("No namespaceURI provided!");
        }
        else if("http://www.isotc211.org/2005/gmi".equals(namespaceURI)) 
        {
            ret = "gmi";
        }
        else if("http://www.isotc211.org/2005/gmd".equals(namespaceURI)) 
        {
            ret = "gmd";
        }
        else if("http://www.isotc211.org/2005/gco".equals(namespaceURI)) 
        {
            ret = "gco";
        }
        else if("http://www.opengis.net/gml/3.2".equals(namespaceURI)) 
        {
            ret = "gml";
        }
        else 
        {
            ret = "";
        }      

        return ret;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Iterator getPrefixes(String namespaceURI)
    {
        return null;    
    }
}
