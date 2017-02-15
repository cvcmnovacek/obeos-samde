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

import java.io.Serializable;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.primefaces.context.RequestContext;

@ManagedBean
@SessionScoped
public class KwInsertionConflictBean implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 8041110414593185264L;
    private String conflictMessage;
    private Concept existingConcept;
    private Concept newConcept;
    
    private String alternative1;
    private String alternative2;
    
    private String lastChosenAlternative;
    
    private Map.Entry<String, Object> conflictingEntry;
    
    private int chosen = 0;

    public boolean compareWithKwToAdd(Concept kwToAdd, DescriptiveKeywordsAnnotationObject dkao)
    {
        boolean ret = false;
        String existingLabelKey = conflictingEntry.getKey().replace("A/href", "V");
        Object label = dkao.getBlockData().getParameter(existingLabelKey);
        if (label instanceof String)
        {
            if (kwToAdd.getConceptName().equals((String) label))
            {
                ret = true;
            }
            else
            {
                prepareMsg((String)label, kwToAdd);
            }

        }
        return ret; 
    }
    
    public int getKeywordIndex()
    {
        String key =conflictingEntry.getKey();
        int start = key.indexOf("keyword[") + 8;
        int end = key.indexOf("]");
        String index = key.substring(start, end);
        return Integer.parseInt(index);
    }

    public void prepareMsg(String exLabel, Concept novum)
    {
        conflictMessage = "The annotation block you selected for insertion has an entry \n";
        conflictMessage += "\"" + novum.getConceptURI() + "\" with keyword \n\"" + exLabel + "\";\n the lookup result for the concept URI"
                + " is " + novum.getConceptName() + ".\n";
        conflictMessage += "Do you want to overwrite the existing concept in the file or discard the new one?";
        
        alternative1 = "Overwrite";
        alternative2 = "Discard";
    }
    public void choose(int i)
    {
        this.chosen = i;
        this.lastChosenAlternative = i==1 ? alternative1 : alternative2;
    }
    public int getChoice()
    {
        return this.chosen;
    }
    public void setConflictingEntry(Map.Entry<String, Object> entry)
    {
        this.conflictingEntry = entry;
    }
    public Map.Entry<String, Object> getConflictingEntry()
    {
        return conflictingEntry;
    }
    public String getConflictMessage()
    {
        return conflictMessage;
    }
    public void setConflictMessage(String conflictMessage)
    {
        this.conflictMessage = conflictMessage;
    }
    public Concept getExistingConcept()
    {
        return existingConcept;
    }
    public void setExistingConcept(Concept existingConcept)
    {
        this.existingConcept = existingConcept;
    }
    public Concept getNewConcept()
    {
        return newConcept;
    }
    public void setNewConcept(Concept newConcept)
    {
        this.newConcept = newConcept;
    }
    public String getAlternative1()
    {
        return alternative1;
    }
    public void setAlternative1(String alternative1)
    {
        this.alternative1 = alternative1;
    }
    public String getAlternative2()
    {
        return alternative2;
    }
    public void setAlternative2(String alternative2)
    {
        this.alternative2 = alternative2;
    }

    public String getLastChosenAlternative()
    {
        return lastChosenAlternative;
    }

    public void setLastChosenAlternative(String lastChosenAlternative)
    {
        this.lastChosenAlternative = lastChosenAlternative;
    }

}
