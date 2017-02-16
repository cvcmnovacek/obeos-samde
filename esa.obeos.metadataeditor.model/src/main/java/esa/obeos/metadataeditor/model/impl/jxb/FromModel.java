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

package esa.obeos.metadataeditor.model.impl.jxb;

import java.net.URL;

import javax.xml.bind.JAXBElement;

import esa.obeos.metadataeditor.model.api.IMdeElement;
import esa.obeos.metadataeditor.model.api.Schemas;

public class FromModel
{
    private static esa.obeos.metadataeditor.model.xsd.beta.gmi.ObjectFactory gmiBetaObjectfactory = 
            new esa.obeos.metadataeditor.model.xsd.beta.gmi.ObjectFactory();

    private static esa.obeos.metadataeditor.model.xsd.rel2015.gmi.ObjectFactory gmiRel2015Objectfactory = 
            new esa.obeos.metadataeditor.model.xsd.rel2015.gmi.ObjectFactory();

    private static esa.obeos.metadataeditor.model.xsd.beta.gmd.ObjectFactory gmdBetaObjectfactory = 
            new esa.obeos.metadataeditor.model.xsd.beta.gmd.ObjectFactory();

    private static esa.obeos.metadataeditor.model.xsd.rel2015.gmd.ObjectFactory gmdRel2015Objectfactory = 
            new esa.obeos.metadataeditor.model.xsd.rel2015.gmd.ObjectFactory();

    private URL schema = null;
    
    private JAXBElement<?> jaxbElement = null;

    public FromModel(final IMdeElement metaData, final boolean validate)
    {
        if (metaData instanceof esa.obeos.metadataeditor.model.xsd.beta.gmi.MIMetadataType)
        {
            if (validate)
            {
                this.schema = Schemas.urlGmiBetaSchema;
            }
            this.jaxbElement = gmiBetaObjectfactory.createMIMetadata(
                    (esa.obeos.metadataeditor.model.xsd.beta.gmi.MIMetadataType)metaData);
        }
        else if (metaData instanceof esa.obeos.metadataeditor.model.xsd.rel2015.gmi.MIMetadataType)
        {
            if (validate)
            {
                this.schema = Schemas.urlGmiRel2015Schema;
            }
            this.jaxbElement = gmiRel2015Objectfactory.createMIMetadata(
                    (esa.obeos.metadataeditor.model.xsd.rel2015.gmi.MIMetadataType)metaData);
        }
        else if (metaData instanceof esa.obeos.metadataeditor.model.xsd.beta.gmd.MDMetadataType)
        {
            if (validate)
            {
                this.schema = Schemas.urlGmdBetaSchema;
            }
            this.jaxbElement = gmdBetaObjectfactory.createMDMetadata(
                    (esa.obeos.metadataeditor.model.xsd.beta.gmd.MDMetadataType)metaData);
        }
        else if (metaData instanceof esa.obeos.metadataeditor.model.xsd.rel2015.gmd.MDMetadataType)
        {
            if (validate)
            {
                this.schema = Schemas.urlGmdRel2015Schema;
            }
            this.jaxbElement = gmdRel2015Objectfactory.createMDMetadata(
                    (esa.obeos.metadataeditor.model.xsd.rel2015.gmd.MDMetadataType)metaData);
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
