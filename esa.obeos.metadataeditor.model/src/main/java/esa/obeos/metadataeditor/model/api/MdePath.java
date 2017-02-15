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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import esa.obeos.metadataeditor.model.api.exc.InvalidPathException;

public class MdePath
{
    private static Pattern numberInSqareBracketsPattern = Pattern.compile("\\[(.*?)\\]");

    private static Pattern pathPattern = Pattern.compile("^(\\/?[a-zA-Z_]+[a-zA-Z_0-9]*(\\[[0-9]+\\])?)+$");

    public static void check(String path) throws InvalidPathException
    {
        if( null == path )
        {
            throw new InvalidPathException("The path is null.");
        }

        if( path.isEmpty() )
        {
            throw new InvalidPathException("The path is empty.");
        }

        Matcher pathMatcher = pathPattern.matcher(path);
        if( !pathMatcher.find() )
        {
            throw new InvalidPathException("The path '" + path + "' does not match to regex pattern '" 
                    + pathPattern.toString() + "'.");
        }
    }

    public static int getElementIndex(String pathSegment) throws InvalidPathException
    {
        int ret = -1;

        Matcher matcher = numberInSqareBracketsPattern.matcher(pathSegment);
        if( matcher.find() )
        {
            String strIndex = matcher.group(1);
            try
            {
                ret = Integer.parseInt(strIndex);
            }
            catch( NumberFormatException e)
            {
                throw new InvalidPathException("no integer in the square brackets");
            }
        }

        return ret;
    }

    private static IMdeElement findChildElement(final String childXmlName, final IMdeElement parent, 
            final boolean mustExist) throws Exception
    {
        IMdeElement ret = parent.getChildByXmlName(childXmlName);
        if( null == ret && mustExist )
        {
            throw new InvalidPathException("The parent '" + parent.getXmlElementName() 
                    + "' does not have IMdeElement child '" + childXmlName + "'");
        }

        return ret;
    }

    private static IMdeElement findMultipleChildElement(final String childXmlName, final Integer index, 
            final IMdeElement parent, final boolean mustExist) throws Exception
    {
        IMdeElement ret = parent.getChildByXmlName(childXmlName, index);
        if( null == ret && mustExist )
        {
            throw new InvalidPathException("The parent '" + parent.getXmlElementName() 
            + "' does not have IMdeElement child '" + childXmlName + "[" + index + "]" + "'");
        }

        return ret;
    }

    public static IMdeElement findElement(final String path, final IMdeElement parent, final boolean mustExist) throws Exception
    {
        IMdeElement ret = null;

        if( null != parent )
        {
            String[] pathSegments = path.split("/");
            if( null != pathSegments )
            {
                for( String pathSegment : pathSegments )
                {
                    if( pathSegment.isEmpty() )
                    {
                        continue;
                    }

                    if( null == ret )
                    {
                        String xmlParentName = parent.getXmlElementName();
                        if( null != xmlParentName )
                        {
                            if( pathSegment.equals(xmlParentName) )
                            {
                                ret = parent;
                                continue;
                            }
                            else
                            {
                                throw new InvalidPathException("Unknown parent '" + pathSegment + "'.");
                            }
                        }
                        else
                        {
                            throw new RuntimeException("Unknown parent.");
                        }
                    }

                    int index = getElementIndex(pathSegment);
                    if( 0 <= index )
                    {
                        String childName = pathSegment.substring(0, pathSegment.indexOf("["));
                        ret = findMultipleChildElement(childName, index, ret, mustExist);
                    }
                    else
                    {
                        ret = findChildElement(pathSegment, ret, mustExist);
                    }
                }
            }
        }

        return ret;
    }

    public static String getParentPath(final String path) throws InvalidPathException
    {
        String ret = null;

        if( null != path )
        {
            int lastIndexOfSlash = path.lastIndexOf("/");
            if( lastIndexOfSlash < 0 )
            {
                throw new InvalidPathException("There is no parent of node at path '" + path + "'.");
            }

            ret = path.substring(0, lastIndexOfSlash);
        }

        return ret;
    }

    public static String getElementName(final String path)
    {
        String ret = path;

        if( null != path )
        {
            int lastIndexOfSlash = path.lastIndexOf("/");
            if( lastIndexOfSlash > 0 )
            {
                ret = path.substring(lastIndexOfSlash + 1, path.indexOf("["));
            }
        }

        return ret;
    }
}
