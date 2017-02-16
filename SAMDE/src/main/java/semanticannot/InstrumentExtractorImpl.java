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

import java.util.ArrayList;
import java.util.List;


public class InstrumentExtractorImpl extends SubtreeDataEnvelope implements InstrumentExtractor
{
    // with the pathPrefix the object can be set to be a
    // a stand-alone instrument-object (child of MI_AcquisitionInformation)
    // or an instrument within a platform element (child of MI_platform)
    public String pathPrefix = "MI_Instrument";

    public final String dateTypeCode_val = "/CI_Date/dateType/CI_DateTypeCode/V";
    public final String dateTypeCodeAttr_codeList = "/CI_Date/dateType/CI_DateTypeCode/A/codeList";
    public final String dateTypeCodeAttr_codeListValue = "/CI_Date/dateType/CI_DateTypeCode/A/codeListValue";

    public InstrumentExtractorImpl(SubtreeData sd)
    {
        super(sd);
    }

    @Override
    public Concept citationTitle()
    {
        String key = pathPrefix + "/citation/CI_Citation/title";
        Concept c = getAnchorOrCharString(key);
        return c;
    }

    @Override
    public List<String> citationDateTypeCode(String specifier)
    {
        List<String> ret = new ArrayList<String>();
        String key = pathPrefix + "/citation/CI_Citation/date[0]" + specifier; // dateTypeCode_val;
                                                                               // //***
                                                                               // /CI_Date/dateType/CI_DateTypeCode/V";
        Object obj = blockData.getParameter(key);
        int ind = 0;
        while (obj instanceof String)
        {
            ret.add((String) obj);
            ind++;
            key = pathPrefix + "/citation/CI_Citation/date[" + ind + "]" + specifier; // ***
                                                                                      // /CI_Date/dateType/CI_DateTypeCode/V";
            obj = blockData.getParameter(key);
        }

        return ret;
    }

    @Override
    public List<Concept> citationIdentifiers()
    {
        List<Concept> ret = null;
        String keyBeginning = "";
        for (int i = 0;; i++)
        {
            keyBeginning = pathPrefix + "/citation/CI_Citation/identifier[" + i + "]/MD_Identifier/code";
            Concept c = getAnchorOrCharString(keyBeginning);
            if (null == c)
            {
                break;
            }
            else
            {
                if (0 == i)
                {
                    ret = new ArrayList<Concept>();
                }
                ret.add(c);
            }
        }
        return ret;
    }

    @Override
    public void setPathPrefix(String pref)
    {
        this.pathPrefix = pref;
    }

    @Override
    public InstrumentData getInstrumentData()
    {
        // path needs to be set correctly in advance!
        // setPathPrefix("/MI_Instrument");
        Concept c = citationTitle();
        List<Concept> ids = citationIdentifiers();
        InstrumentData data = null;
        if (!(c == null && ids == null))
        {
            data = new InstrumentData();
            data.setCitationTitle(c);
            data.setIdentifiers(ids);
        }
        return data;
    }

}
