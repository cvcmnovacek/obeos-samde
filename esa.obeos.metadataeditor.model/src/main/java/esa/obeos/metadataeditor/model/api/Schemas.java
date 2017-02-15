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

package esa.obeos.metadataeditor.model.api;

import java.net.URL;

import esa.obeos.metadataeditor.model.api.Model;

public class Schemas
{
    public static URL urlGmdBetaSchema = Model.class.getResource("/esa/obeos/metadataeditor/model/beta/xsd/gmd/gmd.xsd");
    public static URL urlGmdRel2015Schema = Model.class.getResource("/esa/obeos/metadataeditor/model/rel2015/xsd/gmd/gmd.xsd");

    public static URL urlGmiBetaSchema = Model.class.getResource("/esa/obeos/metadataeditor/model/beta/xsd/gmi/gmi.xsd");
    public static URL urlGmiRel2015Schema = Model.class.getResource("/esa/obeos/metadataeditor/model/rel2015/xsd/gmi/gmi.xsd");

    public static URL getSchema(Package pck) throws Exception
    {
        URL ret = null;
        
        if( null == pck )
        {
            throw new NullPointerException("pck is null");
        }
        
        String packageName = pck.getName();
        
        ModelType type = ModelType.GMD;
        
        if( packageName.contains("gmi") )
        {
            type = ModelType.GMI;
        }
        
        ModelVersion version = ModelVersion.Beta;
        
        if( packageName.contains("rel2015") )
        {
            version = ModelVersion.Rel2015;
        }

        switch( type )
        {
        case GMD:
            
            switch( version )
            {
            case Beta:
                ret = urlGmdBetaSchema;
                break;
                
            case Rel2015:
                ret = urlGmdRel2015Schema;
                break;
                
            default:
                break;
            }
            break;

        case GMI:
            
            switch( version )
            {
            case Beta:
                ret = urlGmiBetaSchema;
                break;
                
            case Rel2015:
                ret = urlGmiRel2015Schema;
                break;
                
            default:
                break;
            }
            break;
            
        default:
            break;
        }
        
        if( null == ret )
        {
            throw new Exception("No XSD schema for package '" + pck.getName() + "'"); 
        }
        
        
        return ret;
    }
}
