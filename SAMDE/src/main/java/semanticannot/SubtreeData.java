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

package semanticannot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import esa.obeos.metadataeditor.model.api.IMdeElement;
import global.GUIrefs;

public class SubtreeData
{
    private IMdeElement enclosingElement;
    private Map<String, Integer> elementCounters;

    SubtreeData(IMdeElement element)
    {
        enclosingElement = element;
    }

    // <relative xpath, value>
    protected Map<String, Object> dataCarriers = new HashMap<String, Object>();

    public Object getParameter(String key)
    {
        Object ret = null;
        // return parameters.get(key);
        try
        {
            if (key.endsWith(DataCollectingVisitor.VAL_TOKEN))
            {
                Object imdeElement = dataCarriers.get(key);
                if (imdeElement instanceof IMdeElement)
                {
                    ret = ((IMdeElement) imdeElement).getElementValue();
                }
            }
            else if (key.contains("/" + DataCollectingVisitor.ATTR_TOKEN + "/"))
            {
                Object imdeElement = dataCarriers.get(key);
                if(imdeElement instanceof IMdeElement)
                {
                    Map<String,Object> attribs = ((IMdeElement) imdeElement).getAttribs();
                    int index = key.lastIndexOf('/');
                    String attribName = key.substring(index+1, key.length());
                    ret = attribs.get(attribName);
                }
            }
        }
        catch (Exception e)
        {
             GUIrefs.displayAlert("Debugging: exception in SubtreeData");
        }
        return ret;
    }
    
    public Object getDataCarrier(String key)
    {
       return dataCarriers.get(key); 
    }

    public void addDataCarrier(String key, Object val)
    {
        dataCarriers.put(key, val);
    }

    public Map.Entry<String, Object> firstEntryForStringValue(String searchValue)
    {
        Map.Entry<String, Object> ret = null;
        for (Map.Entry<String, Object> entry : dataCarriers.entrySet())
        {
            Object value = getParameter(entry.getKey());//entry.getValue();
            if (value instanceof String)
            {
                if (((String) value).equals(searchValue))
                {
                    ret = entry;
                    break;
                }
            }
        }
        return ret;
    }
    public List<Map.Entry<String, Object>> allEntriesForStringValue(String searchValue)
    {
        List<Map.Entry<String, Object>> ret = null;
        for (Map.Entry<String, Object> entry : dataCarriers.entrySet())
        {
            Object value = getParameter(entry.getKey());//entry.getValue();
            if (value instanceof String)
            {
                if (((String) value).equalsIgnoreCase(searchValue))
                {
                    if(null==ret)
                    {
                        ret = new ArrayList<Map.Entry<String, Object>>();
                    }
                    ret.add(entry);
                }
            }
        }
        return ret;
    }

    public String getType()
    {
        String name = "unknown";
        try
        {
            name = enclosingElement.getXmlElementName();
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return name;
    }

    // setters and getters
    public IMdeElement getEnclosingElement()
    {
        return enclosingElement;
    }

    public void setEnclosingElement(IMdeElement enclosingElement)
    {
        this.enclosingElement = enclosingElement;
    }

    public int getElementCounts(String name)
    {
        int ret = 0;
        if (null != this.elementCounters && this.elementCounters.get(name) != null)
        {
            ret = elementCounters.get(name).intValue();
        }
        return ret;
    }

    public Map<String, Integer> getElementCounters()
    {
        return elementCounters;
    }

    public void copyElementCounters(Map<String, Integer> elementCounters)
    {
        if (null == this.elementCounters)
        {
            this.elementCounters = new HashMap<String, Integer>();
        }
        else
        {
            this.elementCounters.clear();
        }
        for (Entry<String, Integer> e : elementCounters.entrySet())
        {
            this.elementCounters.put(e.getKey(), e.getValue());
        }
    }
    
    public static int eltIndexFromKey(String key, String eltName)
    {
        String s = eltName + "[";
        int index1 = key.indexOf(s) + s.length();
        String rest = key.substring(index1, key.length());
        String nmbr = rest.substring(0,rest.indexOf("]"));
        return Integer.parseInt(nmbr);
    }

    // returns "type" of element, i.e. a path of
    // MI_Metadata/identificationInfo[i]/MD_DataIdentification/descriptiveKeywords[j]/MD_Keywords/keyword[k]/Anchor/
    // returns
    // MD_Keywords/keyword/Anchor
    // if descriptiveKeywords equals enclosingElement.getXmlElementName()
    // TODO: implement or discard
    public static String pathNormaliser(String xpath)
    {
        return "";
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("type of subtree: " + this.getType() + "\n");
        try
        {
            builder.append("(" + this.enclosingElement.getPath() + ")\n");
        }
        catch (Exception e)
        {
        }

        for (Entry<String, Object> e : dataCarriers.entrySet())
        {
            builder.append(e.getKey() + "; " + e.getValue() + "\n");
        }
        builder.append("end subtree data\n");
        return builder.toString();
    }


}
