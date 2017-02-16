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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.primefaces.model.TreeNode;

import esa.obeos.metadataeditor.model.api.IMdeElement;
import semanticannot.Concept.LevelOfAgreement;

public class DescriptiveKeywordsAnnotationObject extends SubtreeDataEnvelope implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 5420172351387163917L;
    
    public static final String thesaurusURIKey = "MD_Keywords/thesaurusName/CI_Citation/title/Anchor/"
            + DataCollectingVisitor.ATTR_TOKEN + "/href";
    public static final String thesaurusTitleKey = "MD_Keywords/thesaurusName/CI_Citation/title/Anchor/"
            + DataCollectingVisitor.VAL_TOKEN;

    // private int numberOfKeywords;
    private List<Concept> keywordList;

    public DescriptiveKeywordsAnnotationObject(SubtreeData sd)
    {
        // this.blockData = sd;
        super(sd);
    }


    public String getThesaurusURI()
    {
        String ret = "";
        Object thesaurusURIobj = blockData.getParameter(DescriptiveKeywordsAnnotationObject.thesaurusURIKey);
        if (null != thesaurusURIobj)
        {
            if (thesaurusURIobj instanceof String)
            {
                ret = (String) thesaurusURIobj;
            }
            else
            {
                // error
                System.out.println("Error: thesaurus URI is not a String but " + thesaurusURIobj.getClass());
            }
        }
        return ret;
    }

    public String getThesaurusTitle()
    {
        String ret = "";
        Object thesaurusTitleObj = blockData.getParameter(DescriptiveKeywordsAnnotationObject.thesaurusTitleKey);
        if (thesaurusTitleObj instanceof String)
        {
            ret = (String) thesaurusTitleObj;
        }
        else
        {
            // error
            System.out.println("Error: thesaurus title is not a String");
        }
        return ret;
    }

    public int getNmbrOfKeywords()
    {
        int ret = 0;
        if (null != keywordList)
        {
            ret = keywordList.size();
        }
        else
        {
            ret = this.blockData.getElementCounters().get("keyword");
        }
        return ret;
    }

    public List<Concept> getKeywordList()
    {
        if (keywordList == null)
        {
            buildKeywordList();
        }
        return keywordList;
    }
    
    public int containsFreeKeyword(String keyword)
    {
        int ret = 0;
        if (keywordList == null)
        {
            buildKeywordList();
        }
        for(Concept c : keywordList)
        {
            if(c.getConceptName().equals(keyword))
            {
                ret++;
            }
        }
       return ret; 
    }
    public int containsKeyword(Concept query)
    {
        int ret = 0;
        if (keywordList == null)
        {
            buildKeywordList();
        }
        for(Concept c : keywordList)
        {

            if( (!c.getConceptURI().equals("") && c.compare(query)==LevelOfAgreement.URLSEQUAL)
                    ||c.compare(query)==LevelOfAgreement.EQUAL)
            {
                ret++;
            }
        }
       return ret; 
    }

    private void buildKeywordList()
    {
        if (null == keywordList)
        {
            keywordList = new ArrayList<Concept>();
        }
        Object currVal = null;
        Object currHref = "";
        for (String k : blockData.dataCarriers.keySet())
        {
            if (k.startsWith("MD_Keywords/keyword") && k.endsWith(valToken))
            {
                currVal = blockData.getParameter(k);
                String uriKey = k.replace(valToken, attrToken + "/href");
                currHref = blockData.getParameter(uriKey);

                Concept c = new Concept();

                if (null != currHref && currHref instanceof String)
                {
                    c.setConceptURI((String) currHref);
                }


                if (currVal instanceof String)
                {
                    c.setConceptName((String) currVal);
                }
                keywordList.add(c);
            }
        }
    }
    
    public Set<String> getKeywordSet()
    {
        Set<String> set = new HashSet<String>();
        for(Concept c : getKeywordList())
        {
            set.add(c.getConceptName());
        }
        return set;
    }

    public String getKeywordUri(int i)
    {
        String uriOfithKW = (String) blockData.getParameter(keywordUriKey(i));
        return uriOfithKW;
    }


    public static String displaystringFromKey(String key)
    {
        String ret = "";
        int index1 = ("MD_keywords/").length();
        ret = key.substring(index1, key.length());
        if (key.contains(DataCollectingVisitor.VAL_TOKEN))
        {
            ret = ret.replace("/" + DataCollectingVisitor.VAL_TOKEN, " leaf value");
        }
        if (key.contains("/" + DataCollectingVisitor.ATTR_TOKEN + "/"))
        {
            int lastSlash = key.lastIndexOf('/');
            int lastAttrToken = ret.lastIndexOf("/" + DataCollectingVisitor.ATTR_TOKEN);

            String attrName = key.substring(lastSlash + 1, key.length());
            
            
            ret = ret.substring(0, lastAttrToken) + " attribute \'" + attrName + "\'";
        }
        return ret;
    }

    public static String attributeNameFromKey(String key)
    {
        String attrName = null;
        if (key.contains("/" + DataCollectingVisitor.ATTR_TOKEN + "/"))
        {
            int lastSlash = key.lastIndexOf('/');

            attrName = key.substring(lastSlash + 1, key.length());
        }
        return attrName;
    }

    public Map<String, String[]> getUserEditableThesaurusParams(Map<String, Boolean> foundParameters)
    {
        Map<String, String[]> displayNamesOfEditableParams = new LinkedHashMap<String, String[]>();
        List<String> keyList = new ArrayList<String>(blockData.dataCarriers.keySet());
        // for(String key : blockData.dataCarriers.keySet())
        Collections.sort(keyList);
        for (String key : keyList)
        {
            if (!key.contains("keyword["))
            {
                String[] pair = { displaystringFromKey(key), (String) blockData.getParameter(key) };
                displayNamesOfEditableParams.put(key, pair);
                if (foundParameters.get(key) != null)
                {
                    foundParameters.put(key, true);
                }
            }
        }
        return displayNamesOfEditableParams;
    }

    public void saveUserChangesOfThesaurusParams(Map<String, String[]> newParams)
    {
        List<String> errorMsgs = new ArrayList<String>();
        for (Entry<String, String[]> entry : newParams.entrySet())
        {
            String key = entry.getKey();
            String newValue = entry.getValue()[1];
            Object imdeElement = blockData.getDataCarrier(key);
            if (imdeElement instanceof IMdeElement)
            {
                try
                {
                    if (key.endsWith(valToken))
                    {
                        ((IMdeElement) imdeElement).setElementValue(newValue);
                    }
                    else if(key.contains("/"+ attrToken + "/"))
                    {
                        String attributeName = attributeNameFromKey(key);
                        ((IMdeElement) imdeElement).setAttributeValue(attributeName, newValue);
                    }

                }
                catch (Exception e)
                {
                    errorMsgs.add(e.getMessage());
                }
            }
        }
    }


//    public boolean contains(String kw)
//    {
//        if (null != keywordList)
//        {
//            return keywordList.contains(kw);
//        }
//        else
//        {
//            return getKeywordList().contains(kw);
//        }
//    }

    private String keywordUriKey(int i)
    {
        StringBuilder builder = new StringBuilder("MD_Keywords/keyword[");
        builder.append(i);
        builder.append("]/Anchor/A/href");

        return builder.toString();
    }

    @Override
    public String toString()
    {
        return this.blockData.toString();
    }
}
