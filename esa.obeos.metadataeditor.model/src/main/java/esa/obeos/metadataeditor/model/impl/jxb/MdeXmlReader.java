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

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.StreamReaderDelegate;

import esa.obeos.metadataeditor.model.api.ModelVersion;

public class MdeXmlReader extends StreamReaderDelegate
{
    private static final int ATTRIB_XMLNS_INDEX = 0;

    private XMLStreamReader reader = null;

    private ModelVersion modelVersion = ModelVersion.Invalid;

    private static MdeNamespacePrefixMapper mapper = new MdeNamespacePrefixMapper();

    public MdeXmlReader(XMLStreamReader reader, ModelVersion modelVersion)
    {
        super(reader);
        this.reader = reader;
        this.modelVersion = modelVersion;
    }

     @Override
     public String getNamespaceURI()
     {
         String ret = this.reader.getNamespaceURI();

         if (null == ret)
         {
             int fEventType = this.reader.getEventType();
             if(fEventType == XMLEvent.START_ELEMENT || fEventType == XMLEvent.END_ELEMENT)
             {
                 String localName = this.reader.getLocalName();
                 int pos = localName.indexOf(':');
                 if (-1 != pos)
                 {
                     String prefix = localName.substring(0, pos);
                     if (null != prefix && !prefix.isEmpty())
                     {
                         ret = mapper.getNamespaceUri(prefix, this.modelVersion);
                     }
                 }
                 else
                 {
                     if(fEventType == XMLEvent.START_ELEMENT)
                     {
                         if ( 0 < this.reader.getAttributeCount() )
                         {
                             // to get
                             ret = this.reader.getAttributeValue(ATTRIB_XMLNS_INDEX);
                         }
                     }
                 }
             }
         }

         return ret;
     }

     @Override
     public String getLocalName()
     {
         String ret = this.reader.getLocalName();

         int fEventType = this.reader.getEventType();
         if(fEventType == XMLEvent.START_ELEMENT || fEventType == XMLEvent.END_ELEMENT)
         {
             int pos = ret.indexOf(':');
             if (-1 != pos)
             {
                 String prefix = ret.substring(0, pos);
                 if (null != prefix && !prefix.isEmpty())
                 {
                     ret = ret.substring(pos+1);
                 }
             }
         }

         return ret;
     }
     
     @Override
     public String getAttributeNamespace(int index) {
           String ret = this.reader.getAttributeNamespace(index);

           if (null == ret) {
                  int fEventType = this.reader.getEventType();
                  if (fEventType == XMLEvent.START_ELEMENT || fEventType == XMLEvent.ATTRIBUTE) {
                         String prefix = this.reader.getAttributePrefix(index);
                         if (prefix != null && !prefix.isEmpty()) {
                                ret = mapper.getNamespaceUri(prefix, this.modelVersion);
                         }
                  }
           }

           return ret;
     }

}
