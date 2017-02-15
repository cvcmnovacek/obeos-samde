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

import java.net.URL;

import esa.obeos.metadataeditor.model.api.ModelType;
import esa.obeos.metadataeditor.model.api.ModelVersion;
import esa.obeos.metadataeditor.model.api.Schemas;
import esa.obeos.metadataeditor.model.impl.MdeElement;

public class ToModel
{
    private URL schema = null;
    
    private Class<? extends MdeElement> clazz = null;
    
    public ToModel(final ModelType modelType, final ModelVersion modelVersion, final boolean validate)
    {
        switch (modelType)
        {
        case GMD:

            switch (modelVersion)
            {
            case Beta:
                if (validate)
                {
                    this.schema = Schemas.urlGmdBetaSchema;
                }
                this.clazz = esa.obeos.metadataeditor.model.xsd.beta.gmd.MDMetadataType.class;
                break;

            case Rel2015:
                if (validate)
                {
                    this.schema = Schemas.urlGmdRel2015Schema;
                }
                this.clazz = esa.obeos.metadataeditor.model.xsd.rel2015.gmd.MDMetadataType.class;
                break;

            default:
                break;
            }
            break;

        case GMI:
            
            switch (modelVersion)
            {
            case Beta:
                if (validate)
                {
                    this.schema = Schemas.urlGmiBetaSchema;
                }
                this.clazz = esa.obeos.metadataeditor.model.xsd.beta.gmi.MIMetadataType.class;
                break;

            case Rel2015:
                if (validate)
                {
                    this.schema = Schemas.urlGmiRel2015Schema;
                }
                this.clazz = esa.obeos.metadataeditor.model.xsd.rel2015.gmi.MIMetadataType.class;
                break;

            default:
                break;
            }
            break;

        default:
            break;
        }
    }

    public URL getSchema()
    {
        return this.schema;
    }

    public Class<? extends MdeElement> getClazz()
    {
        return this.clazz;
    }
}
