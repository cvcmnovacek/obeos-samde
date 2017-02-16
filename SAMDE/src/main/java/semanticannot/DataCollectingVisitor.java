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

package semanticannot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import esa.obeos.metadataeditor.model.api.IMdeElement;
import esa.obeos.metadataeditor.model.api.IMdeVisitor;

public class DataCollectingVisitor implements IMdeVisitor, Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = -8368298906952307353L;
    public static final String ATTR_TOKEN = "A";
    public static final String VAL_TOKEN = "V";

    protected SubtreeData currentDataset;

    protected List<SubtreeData> allDatasets = new ArrayList<SubtreeData>();

    // counters of specified elements
    protected Map<String, Integer> elementCounters = null;

    // name of root of subtree from which data (=leaf and attribute values) is
    // collected
    protected String localRootName = "";

    protected boolean withinSubtreeOfInterest = false;

    public void reset()
    {
        allDatasets.clear();
        withinSubtreeOfInterest = false;
        currentDataset = null;
    }

    public void addCounter(String key)
    {
        if (null == elementCounters)
        {
            elementCounters = new HashMap<String, Integer>();
        }
        Integer i = 0;
        this.elementCounters.put(key, i);
    }

    public void resetCounter(String key)
    {
        if (null != elementCounters && null != elementCounters.get(key))
        {
            Integer i = 0;
            elementCounters.put(key, i);
        }
    }

    public void resetAllCounters()
    {
        Integer zero = 0;
        if (null != elementCounters)
        {
            for (Entry<String, Integer> e : elementCounters.entrySet())
            {
                elementCounters.put(e.getKey(), zero);
            }
        }
    }

    public void incrementCounter(String key)
    {
        if (null != elementCounters && null != elementCounters.get(key))
        {
            Integer i = elementCounters.get(key) + 1;
            elementCounters.put(key, i);
        }
    }

    @Override
    public void beginVisit(IMdeElement element) throws Exception
    {
        String xmlElementName = element.getXmlElementName();
        if (xmlElementName.equals(localRootName))
        {
            currentDataset = new SubtreeData(element);
            withinSubtreeOfInterest = true;
        }
        if (null != elementCounters && null != elementCounters.get(xmlElementName))
        {
            incrementCounter(xmlElementName);
        }

        if (withinSubtreeOfInterest)
        {
            addNonNullAttributes(element);

            if (element.isValue())
            {
                addLeafValue(element, localRootName);
            }
        }

    }

    @Override
    public void endVisit(IMdeElement element) throws Exception
    {
        if (element.getXmlElementName().equals(localRootName))
        {
            if (null != elementCounters)
            {
                currentDataset.copyElementCounters(elementCounters);
                resetAllCounters();
            }
            allDatasets.add(currentDataset);
            withinSubtreeOfInterest = false;
        }
    }


    public void addLeafValue(IMdeElement element, String firstToken) throws Exception
    {
        //Object value = element.getElementValue();
        String key = DataCollectingVisitor.truncateXpath(element.getPath(), firstToken) + "/" + VAL_TOKEN;

        currentDataset.addDataCarrier(key, element);
    }

    public void addNonNullAttributes(IMdeElement element) throws Exception// String
                                                                          // baseXpath,
                                                                          // Map<String,Object>
                                                                          // inputMap)
    {

        Map<String, Object> inputMap = element.getAttribs();
        String baseXpath = DataCollectingVisitor.truncateXpath(element.getPath(), localRootName);
        for (Entry<String, Object> e : inputMap.entrySet())
        {
            if (null != e.getValue())
            {
                String key = baseXpath + "/" + ATTR_TOKEN + "/" + e.getKey();
                //this.currentDataset.addParameter(key, e.getValue());
                this.currentDataset.addDataCarrier(key, element); 
            }
        }
    }

    public void clearAllDatasets()
    {
        this.allDatasets.clear();
    }

    // remove first token including index [i]
    public static String truncateXpath(String xpath, String firstToken)
    {
        String ret = "";
        int index = xpath.indexOf(firstToken);
        String toErase = xpath.substring(0, index);
        ret = xpath.replace(toErase, "");
        
        index = ret.indexOf("]/");
        if(index != -1)
        {
           toErase = ret.substring(0, index + 2);
        }
        else
        {
            toErase = "";
        }
        return ret.replace(toErase, "");
    }

    public List<SubtreeData> getAllDatasets()
    {
        return allDatasets;
    }

    public String getLocalRootName()
    {
        return localRootName;
    }

    public void setLocalRootName(String localRootName)
    {
        this.localRootName = localRootName;
    }

}
