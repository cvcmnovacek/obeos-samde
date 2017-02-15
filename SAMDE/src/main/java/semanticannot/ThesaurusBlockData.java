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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ThesaurusBlockData extends AnnotationBlockData
{
    // <normalised key, number of occurrences >
    // private Map<String,int> requiredFields;

    private Map<String, String> suggestions = new LinkedHashMap<String, String>();


    public ThesaurusBlockData()
    {
        initMappings();
    }

    public void initMappings()
    {
        requiredFields = new LinkedHashMap<String, Boolean>();
        requiredFields.put(ktcCodeListAttr, false);
        requiredFields.put(ktcCodeListValueAttr, false);
        requiredFields.put(thesaurusTitle, false);
        requiredFields.put(thesaurusHrefAttrib, false);
        requiredFields.put(date, false);
        requiredFields.put(dateTypeCode, false);
        requiredFields.put(dtcCodeListAttr, false);
        requiredFields.put(dtcCodeListValueAttr, false);
        requiredFields.put(dtcCodeListValueAttr, false);
        // do the rest of the initialisation

        suggestions.put(dtcCodeListValueAttr, "publication");
        suggestions.put(dateTypeCode, "publication");
        suggestions.put(ktcCodeListValueAttr, "theme");
    }

    private DescriptiveKeywordsAnnotationObject dkao;
    // map to accomodate required fields that were not in the file
    // private Map<String, String> remainingFields;

    public static String ktcCodeListAttr = "MD_Keywords/type/MD_KeywordTypeCode/A/codeList";
    public static String ktcCodeListValueAttr = "MD_Keywords/type/MD_KeywordTypeCode/A/codeListValue";

    public static String thesaurusTitle = "MD_Keywords/thesaurusName/CI_Citation/title/Anchor/V";
    public static String thesaurusHrefAttrib = "MD_Keywords/thesaurusName/CI_Citation/title/Anchor/A/href";

    public static String datePath = "MD_Keywords/thesaurusName/CI_Citation/date[0]/CI_Date";
    public static String date = datePath + "/date/Date/V";
    public static String dateTypeCode = datePath + "/dateType/CI_DateTypeCode/V";
    public static String dtcCodeListAttr = datePath + "/dateType/CI_DateTypeCode/A/codeList";
    public static String dtcCodeListValueAttr = datePath + "/dateType/CI_DateTypeCode/A/codeListValue";

    // public static String dtcLeafValue = "publication";

    public Map<String, String> getSuggestions()
    {
        return suggestions;
    }

    public DescriptiveKeywordsAnnotationObject getDkao()
    {
        return dkao;
    }

    public void setDkao(DescriptiveKeywordsAnnotationObject dkao)
    {
        this.dkao = dkao;
    }

    public Map<String, String[]> getRemainingFields()
    {
        Map<String, String[]> remainingFields = null;
        for (Entry<String, Boolean> entry : requiredFields.entrySet())
        {
            if (entry.getValue().booleanValue() == false)
            {
                if (remainingFields == null)
                {
                    remainingFields = new LinkedHashMap<String, String[]>();
                }
                String val = suggestions.get(entry.getKey());
                if (val == null)
                {
                    val = "";
                }
                String[] valArr = { DescriptiveKeywordsAnnotationObject.displaystringFromKey(entry.getKey()), val };
                remainingFields.put(entry.getKey(), valArr);
            }
        }
        return remainingFields;
    }

}
