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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import global.Master;
import semanticannot.Concept.LevelOfAgreement;

//all data values in an instrument block that are processed (not necessarily displayed
//and used for classification)
public class InstrumentBlockData
{

    private Concept title;
    private String date;
    private String dtcCodeListAttr;
    private String dtcCodeListValueAttr;
    private String dtcLeafValue;

    private List<Concept> identifiers;

    private boolean sensorTypeCode = true;


    public void reset()
    {
        if (null != title)
        {
            title.reset();
        }
        if(null!=identifiers)
        {
            identifiers.clear();
        }
        dtcCodeListAttr = "";
        dtcCodeListValueAttr = "";
        dtcLeafValue = "";
        date = "";
    }

    public void addIdentifier(Concept c)
    {
        if (null == identifiers)
        {
            identifiers = new ArrayList<Concept>();
        }
        if (!c.isEmpty() && !identifiers.contains(c))
        {
          identifiers.add(c);
        }
    }

    public Concept getTitle()
    {
        return title;
    }

    public void setTitle(Concept title)
    {
        this.title = title;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public String getDtcCodeListAttr()
    {
        return dtcCodeListAttr;
    }

    public void setDtcCodeListAttr(String dtcCodeListAttr)
    {
        this.dtcCodeListAttr = dtcCodeListAttr;
    }

    public String getDtcCodeListValueAttr()
    {
        return dtcCodeListValueAttr;
    }

    public void setDtcCodeListValueAttr(String dtcCodeListValueAttr)
    {
        this.dtcCodeListValueAttr = dtcCodeListValueAttr;
    }

    public String getDtcLeafValue()
    {
        return dtcLeafValue;
    }

    public void setDtcLeafValue(String dtcLeafValue)
    {
        this.dtcLeafValue = dtcLeafValue;
    }

    public List<Concept> getIdentifiers()
    {
        return identifiers;
    }

    public void setIdentifiers(List<Concept> identifiers)
    {
        this.identifiers = identifiers;
    }

    public boolean isSensorTypeCode()
    {
        return sensorTypeCode;
    }

    public void setSensorTypeCode(boolean sensorTypeCode)
    {
        this.sensorTypeCode = sensorTypeCode;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("title: " + title);
        builder.append("\ndate: " + date);
        builder.append("\ndateTypeCode-codeList: " + dtcCodeListAttr);
        builder.append("\ndateTypeCode-codeListValue: " + dtcCodeListValueAttr);
        builder.append("\ndateTypeCode-leaf value: " + dtcLeafValue);

        builder.append("\nidentifiers: " + identifiers);

        return builder.toString();
    }
    
    public static boolean compareInstrumentTitles(Concept newTitle, Concept fromFile, String filename)
    {
        boolean duplicate = false;
        if (newTitle.compare(fromFile) == LevelOfAgreement.URLSEQUAL && !newTitle.getConceptURI().equals(""))
        {
            Master.errorBean.appendMessage(filename,
                    "File " + filename + " already contains instrument whose title has the same URI.");
            duplicate = true;


        }
        if (newTitle.compare(fromFile) == LevelOfAgreement.EQUAL)
        {
            Master.errorBean.appendMessage(filename,
                    "File " + filename + " already contains instrument with the same title");
            duplicate = true;
        }
        return duplicate;
    }

    public static boolean compareIdentifierLists(List<Concept> idlist1, List<Concept> idlist2)
    {
        boolean ret = true;
        Set<Concept> set1 = new HashSet<Concept>(idlist1);
        Set<Concept> set2 = new HashSet<Concept>(idlist2);
        if (set1.equals(set2))
        {
            ret = true;
        }
        return ret;
    }
}
