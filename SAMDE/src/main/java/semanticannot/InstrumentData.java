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

import java.util.List;

//instrument data for display purposes
public class InstrumentData
{
    private Concept citationTitle;
    private List<Concept> identifiers;

    public Concept getCitationTitle()
    {
        return citationTitle;
    }
    public void setCitationTitle(Concept citationTitle)
    {
        this.citationTitle = citationTitle;
    }
    public List<Concept> getIdentifiers()
    {
        return identifiers;
    }
    public void setIdentifiers(List<Concept> identifiers)
    {
        this.identifiers = identifiers;
    }
    public int getNmbrOfIdentifiers()
    {
        if(identifiers!=null)
        {
            return identifiers.size();
        }
        else
        {
            return 0;
        }
    }
    public Concept getFirstIdentifier()
    {
        if(identifiers !=null)
        {
            return identifiers.get(0);
        }
        else
        {
            return null;
        }
    }

}