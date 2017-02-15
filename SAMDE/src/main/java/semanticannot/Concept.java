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
import java.util.Objects;

public class Concept implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = -6922035105583721316L;
    private String conceptURI="";
    private String conceptName="";
    
    public enum LevelOfAgreement{
        NONE(0), NAMESEQUAL(1), URLSEQUAL(2), EQUAL(3);
        
        private final int val;
        LevelOfAgreement(int n){this.val=n;}
        public int getValue(){return this.val;}
    }
    
    public Concept()
    {
    }
    public Concept(String name,String uri)
    {
        this.conceptName=name;
        this.conceptURI=uri;
    }
    
    public String getConceptURI()
    {
        return conceptURI;
    }
    public void setConceptURI(String conceptURI)
    {
        this.conceptURI = conceptURI;
    }
    public String getConceptName()
    {
        return conceptName;
    }
    public void setConceptName(String conceptName)
    {
        this.conceptName = conceptName;
    }
    //returns true if Concept has a non-trivial entry
    public boolean isEmpty()
    {
        if(null!=conceptName && !conceptName.equals(""))
        {
            return false;
        }
        if(null!=conceptURI && !conceptURI.equals(""))
        {
            return false;
        }
        return true;
    }
    
    public LevelOfAgreement compare(Concept c)
    {
        int ret = LevelOfAgreement.NONE.getValue();
        
        if(c.getConceptName()==null || c.getConceptName().equals(""))
        {
           if(conceptName == null || conceptName.equals("")) 
           {
               ret |= LevelOfAgreement.NAMESEQUAL.getValue();
           }
        }
        else {
            if(c.getConceptName().equals(conceptName))
            {
               ret |= LevelOfAgreement.NAMESEQUAL.getValue();
            }
        }
        if(c.getConceptURI()==null || c.getConceptURI().equals(""))
        {
            if(conceptURI==null || conceptURI.equals(""))
            {
                ret |= LevelOfAgreement.URLSEQUAL.getValue();
            }
        }
        else{
            if(c.getConceptURI().equals(conceptURI))
            {
                ret |= LevelOfAgreement.URLSEQUAL.getValue();
            }
        }
        return LevelOfAgreement.values()[ret];
        
    }
    
    @Override
    public boolean equals(Object p2)
    {
       if(! (p2 instanceof Concept) ) return false;
       if(p2==this) return true;
       if(this.conceptURI.equals( ((Concept) p2).conceptURI) && this.conceptName.equals(((Concept) p2).conceptName)) return true;
       return false;
    }

    @Override
    public int hashCode()
    {
       return Objects.hash(conceptName,conceptURI);
    }

    
    @Override
    public String toString()
    {
        return "(" + conceptURI + "; " + conceptName + ")" ;
    }
    public void reset()
    {
        this.conceptName="";
        this.conceptURI=""; 
    }

}
