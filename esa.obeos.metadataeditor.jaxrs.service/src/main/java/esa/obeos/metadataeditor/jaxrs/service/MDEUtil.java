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

package esa.obeos.metadataeditor.jaxrs.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import esa.obeos.metadataeditor.jaxrs.api.datatypes.DiagnosticCodeType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.MetadataType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.MetadataVersionType;
import esa.obeos.metadataeditor.model.api.ModelType;
import esa.obeos.metadataeditor.model.api.ModelVersion;

public class MDEUtil
{
    private static Pattern nodeNamePattern = Pattern.compile("^[a-zA-Z_]+[a-zA-Z_0-9]*(\\[[0-9]+\\])?");

    private MDEUtil()
    {
    }

    public static void checkNodeName(String nodeName) throws DiagnosticException
    {
        if( null == nodeName )
        {
            throw new DiagnosticException(DiagnosticCodeType.INVALID_NODE_NAME, "nodeName is null");
        }

        if( nodeName.isEmpty() )
        {
            throw new DiagnosticException(DiagnosticCodeType.INVALID_NODE_NAME, "nodeName is empty");
        }

        Matcher nodeNameMatcher = nodeNamePattern.matcher(nodeName);
        if( !nodeNameMatcher.find() )
        {
            throw new DiagnosticException(DiagnosticCodeType.INVALID_NODE_NAME, "nodeName does not match to the '" 
                    + nodeNamePattern.toString() + "' regex pattern");
        }
    }

    public static void checkValue(String value) throws DiagnosticException
    {
        if( null == value )
        {
            throw new DiagnosticException(DiagnosticCodeType.INVALID_VALUE, "value is null");
        }

        if( value.isEmpty() )
        {
            throw new DiagnosticException(DiagnosticCodeType.INVALID_VALUE, "value is empty");
        }
    }

    public static void checkAttribute(String attribute) throws DiagnosticException
    {
        if( null == attribute )
        {
            throw new DiagnosticException(DiagnosticCodeType.INVALID_ATTRIBUTE, "attribute is null");
        }

        if( attribute.isEmpty() )
        {
            throw new DiagnosticException(DiagnosticCodeType.INVALID_ATTRIBUTE, "attribute is empty");
        }
    }

    public static ModelType convert(MetadataType type) throws DiagnosticException
    {
        ModelType ret = ModelType.Invalid;

        switch( type )
        {
        case GMD:
            ret = ModelType.GMD;
            break;
        case GMI:
            ret = ModelType.GMI;
            break;
        default:
            throw new DiagnosticException(DiagnosticCodeType.INVALID_METADATA_TYPE);        
        }

        return ret;
    }

    public static MetadataType convert(ModelType type) throws DiagnosticException
    {
        MetadataType ret = MetadataType.INVALID;

        switch( type )
        {
        case GMD:
            ret = MetadataType.GMD;
            break;
        case GMI:
            ret = MetadataType.GMI;
            break;
        default:
            throw new DiagnosticException(DiagnosticCodeType.INVALID_METADATA_TYPE);        
        }

        return ret;
    }

    public static ModelVersion convert(MetadataVersionType version) throws DiagnosticException
    {
        ModelVersion ret = ModelVersion.Invalid;

        switch( version )
        {
        case BETA:
            ret = ModelVersion.Beta;
            break;

        case REL_2015:
            ret = ModelVersion.Rel2015;
            break;

        default:
            throw new DiagnosticException(DiagnosticCodeType.INVALID_METADATA_VERSION);        
        }

        return ret;
    }

    public static MetadataVersionType convert(ModelVersion version) throws DiagnosticException
    {
        MetadataVersionType ret = MetadataVersionType.INVALID;

        switch( version )
        {
        case Beta:
            ret = MetadataVersionType.BETA;
            break;

        case Rel2015:
            ret = MetadataVersionType.REL_2015;
            break;

        default:
            throw new DiagnosticException(DiagnosticCodeType.INVALID_METADATA_VERSION);        
        }

        return ret;
    }
}
