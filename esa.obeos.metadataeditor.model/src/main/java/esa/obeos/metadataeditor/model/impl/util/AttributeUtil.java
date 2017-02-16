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

package esa.obeos.metadataeditor.model.impl.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttributeUtil
{
    private static Map<String, List<?>> constrains = null;
    
    private static void initConstrains()
    {
        constrains = new HashMap<String, List<?>>();

        Pattern anyURIPattern = Pattern.compile("([a-zA-Z][a-zA-Z0-9\\-\\+\\.]*:|\\.\\./|\\./|#).*");

        
        // attribute 'uom' constrains
        
        List<Pattern> uomPatterns = new ArrayList<Pattern>();
        
        Pattern UomSymbolPattern = Pattern.compile("[^: \n\r\t]+");
        uomPatterns.add(UomSymbolPattern);
        uomPatterns.add(anyURIPattern);

        constrains.put("uom", uomPatterns);

        // attribute 'href' constrains

        List<Pattern> hrefPatterns = new ArrayList<Pattern>();
        hrefPatterns.add(anyURIPattern);

        constrains.put("href", hrefPatterns);

        // attribute 'role' constrains

        List<Pattern> rolePatterns = new ArrayList<Pattern>();
        rolePatterns.add(anyURIPattern);

        constrains.put("role", rolePatterns);

        // attribute 'arcrole' constrains

        List<Pattern> arcrolePatterns = new ArrayList<Pattern>();
        arcrolePatterns.add(anyURIPattern);

        constrains.put("arcrole", arcrolePatterns);

        // attribute 'show' constrains

        List<String> showValues = new ArrayList<String>();
        showValues.add("new");
        showValues.add("replace");
        showValues.add("embed");
        showValues.add("other");
        showValues.add("none");

        constrains.put("show", showValues);
    }
    
    public static boolean hasAttributeConstrain(String attributeName)
    {
        if (null == constrains)
        {
            initConstrains();
        }
        
        return constrains.containsKey(attributeName);
    }

    private static List<?> getAtteributeConstrains(String attributeName)
    {
        if (null == constrains)
        {
            initConstrains();
        }
        
        return constrains.get(attributeName);
    }
    
    @SuppressWarnings("unchecked")
    public static void validateAttributeValue(String attributeName, String attributeValue) throws Exception
    {
        if (null == attributeName)
        {
            throw new NullPointerException("attributeName is null");
        }
        
        if (null == attributeValue)
        {
            throw new NullPointerException("attributeValue is null");
        }
        
        if (hasAttributeConstrain(attributeName))
        {
            
            List<?> constrains = getAtteributeConstrains(attributeName);

            if (null == constrains || constrains.isEmpty())
            {
                throw new Exception("constarains for attribute '" + attributeName + "' are corrupted");
            }
            
            Object obj = constrains.iterator().next();
            
            if (obj instanceof Pattern)
            {
                validateAttributeValueAgainstPatterns(attributeName, attributeValue, (List<Pattern>)constrains);
            }
            else if (obj instanceof String)
            {
                validateAttributeValueAgainstValues(attributeName, attributeValue, (List<String>)constrains);
            }
        }
    }

    public static void validateAttributeValueAgainstPatterns(String attributeName, String attributeValue, List<Pattern> patterns) throws Exception
    {
        boolean matches = false;
        StringBuilder msg = new StringBuilder("attribute value doesn't match any pattern(s)");

        for ( Pattern p : patterns)
        {
            Matcher matcher = p.matcher(attributeValue);
            matches = matcher.matches();
            if (matches)
            {
                break;
            }
        
            msg.append(" '");
            msg.append(p.pattern());
            msg.append("'");
        }
        
        if (!matches)
        {
            throw new Exception(msg.toString());
        }
    }

    public static void validateAttributeValueAgainstValues(String attributeName, String attributeValue, List<String> values) throws Exception
    {
        boolean matches = false;
        StringBuilder msg = new StringBuilder("attribute value doesn't match any of value");

        for ( String s : values)
        {
            matches = attributeValue.equals(s);
            if (matches)
            {
                break;
            }
        
            msg.append(" '");
            msg.append(s);
            msg.append("'");
        }
        
        if (!matches)
        {
            throw new Exception(msg.toString());
        }
    }
}
