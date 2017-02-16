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
import java.util.Map;

import org.primefaces.model.TreeNode;

import semanticannot.Concept.LevelOfAgreement;

public class PlatformAnnotationObject extends SubtreeDataEnvelope
{
    // private SubtreeData blockData;

    // only valid if corresponding file is display file
    // private TreeNode localRootTreeNode = null;
    private InstrumentExtractor instrumentExtractor;

    private List<InstrumentData> instrumentList=null;

    public PlatformAnnotationObject(SubtreeData sd)
    {
        super(sd);
        instrumentExtractor = new InstrumentExtractorImpl(sd);
    }

    public Concept getPlatformIdentifier()
    {
        Concept ret = new Concept("", "");

        String keyName = "MI_Platform/identifier/MD_Identifier/code/"; // Anchor/"
                                                                       // +
                                                                       // valToken;
        for (String k : blockData.dataCarriers.keySet())
        {
            if (k.startsWith(keyName))
            {
                if (k.contains("Anchor"))
                {
                    if (k.contains(valToken))
                    {
                        ret.setConceptName((String) blockData.getParameter(k));
                    }
                    else if (k.contains("href"))
                    {
                        ret.setConceptURI((String) blockData.getParameter(k));
                    }
                }
                else if (k.contains("CharacterString"))
                {
                    ret.setConceptName((String) blockData.getParameter(k));
                    break;
                }
            }
        }
        return ret;
    }

    public String getPlatformDescription()
    {
        String keyDescription = "MI_Platform/description/CharacterString/" + valToken;
        return (String) blockData.getParameter(keyDescription);
    }

    public List<InstrumentData> getInstrumentList()
    {
        if (instrumentList != null)
        {
            return instrumentList;
        }
        // List<InstrumentData> ret = null;
        int nmbr = blockData.getElementCounters().get("instrument");

        if (nmbr > 0)
        {
            instrumentList = new ArrayList<InstrumentData>();

            for (int i = 0; i < nmbr; i++)
            {
                instrumentExtractor.setPathPrefix("MI_Platform/instrument[" + i + "]/MI_Instrument");
                // Concept c = instrumentExtractor.citationTitle();
                // List<Concept> ids =
                // instrumentExtractor.citationIdentifiers();
                InstrumentData data = instrumentExtractor.getInstrumentData();
                // data.setCitationTitle(c);
                // data.setIdentifiers(ids);
                if (null != data)
                {
                    instrumentList.add(data);
                }
            }
        }
        return instrumentList;
    }

    public boolean containsInstrument(InstrumentBlockData ibd, Map<String,String> errorMsgs, String key)
    {
        return containsInstrument(ibd.getTitle(),ibd.getIdentifiers(),errorMsgs,key);
    }
    public boolean containsInstrument(Concept title, List<Concept> identifiers, Map<String,String> errorMsgs, String key)
    {
        boolean ret = false;
        String msg = "";
        List<InstrumentData> list = getInstrumentList();
        boolean idsEqual = false;
        boolean titlesEqual = false;
        for(InstrumentData idat : list)
        {
            idsEqual = InstrumentBlockData.compareIdentifierLists(identifiers, idat.getIdentifiers());
            titlesEqual = InstrumentBlockData.compareInstrumentTitles(title, idat.getCitationTitle(), key);
            
            ret = idsEqual && titlesEqual;
//            Concept c = idat.getCitationTitle();
//            if(c.compare(title)==LevelOfAgreement.URLSEQUAL || c.compare(title)==LevelOfAgreement.EQUAL)
//            {
//                ret=true;
//                msg += "found instrument";
//                if(c.compare(title)!=LevelOfAgreement.EQUAL)
//                {
//                    msg += " whose title has same URI";
//                }
//                else
//                {
//                    msg += " with identic title";
//                }
//            }
        }
        if(errorMsgs != null)
        {
            String orig = errorMsgs.get(key);
            errorMsgs.put(key, orig + "\n" + msg);
        }
        return ret; 
    }

}
