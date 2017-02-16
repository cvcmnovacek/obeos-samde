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

//class the instances of which are used to save data extracted from
//search responses
public class AnnotationObject
{
	//private String keyword;
	private Concept concept;
	

    private String aboutAttribute;
	private String descriptionAboutAttribute;
	//assumption: if 'baseAttribute' is empty all URLs as in 'descriptionAboutAttribute'
	//'broaderConcepts' and 'narrowerConcepts' are taken to be complete (absolute)
	private String baseAttribute;
	private String definition;
	private List<Concept> broaderConcepts;
	private List<Concept> narrowerConcepts;
	
	private List<AnnotationObject> broaderAnnotObjects;
	private List<AnnotationObject> narrowerAnnotObjects;
	
	private boolean inTable = false;

	public void addBroaderConcept(Concept concept)
	{
	    if(null==broaderConcepts)
	    {
	        broaderConcepts = new ArrayList<Concept>();
	    }
	    broaderConcepts.add(concept);
	}
	public void addNarrowerConcept(Concept concept)
	{
	    if(null==narrowerConcepts)
	    {
	        narrowerConcepts = new ArrayList<Concept>();
	    }
	    narrowerConcepts.add(concept);
	}
	
	public void addBroaderAnnotObjects(AnnotationObject b)
	{
	    if(broaderAnnotObjects==null)
	    {
	        broaderAnnotObjects = new ArrayList<AnnotationObject>();
	    }
	    broaderAnnotObjects.add(b);
	}

	public void addNarrowerAnnotObjects(AnnotationObject n)
	{
	    if(narrowerAnnotObjects==null)
	    {
	        narrowerAnnotObjects = new ArrayList<AnnotationObject>();
	    }
	    narrowerAnnotObjects.add(n);
	}

	//setters and getters 
	public String getKeyword()
	{
	    if(null!=concept)
	    {
	    	return concept.getConceptName();
	    }
	    return "";
	}

	public void setKeyword(String keyword)
	{
	    String uri = "";
		if(baseAttribute!= null  && !baseAttribute.equals(""))
		{
            uri = baseAttribute + aboutAttribute;	    
		}
		else if(null!=descriptionAboutAttribute && !descriptionAboutAttribute.equals(""))
		{
		    uri = descriptionAboutAttribute;
		}
	    if(null==this.concept)
	    {
	        this.concept = new Concept(keyword,uri);
	    }
	    else
	    {
		   this.concept.setConceptName(keyword);
		   this.concept.setConceptURI(uri);
	    }
	}
	public String getKeywordUri()
	{
	    if(null!=concept)
	    {
	    	return concept.getConceptURI();
	    }
	    return "";
	}
    public Concept getConcept()
    {
        return concept;
    }
    public void setConcept(Concept concept)
    {
        this.concept = concept;
    }
	public String getBaseAttribute()
	{
		return baseAttribute;
	}

	public void setBaseAttribute(String baseAttribute)
	{
		this.baseAttribute = baseAttribute;
	}

	public String getAboutAttribute()
	{
		return aboutAttribute;
	}

	public void setAboutAttribute(String aboutAttribute)
	{
		this.aboutAttribute = aboutAttribute;
	}
    public String getDescriptionAboutAttribute()
    {
        return descriptionAboutAttribute;
    }
    public void setDescriptionAboutAttribute(String descriptionAboutAttribute)
    {
        this.descriptionAboutAttribute = descriptionAboutAttribute;
    }
    public String getDefinition()
    {
        return definition;
    }
    public void setDefinition(String definition)
    {
        this.definition = definition;
    }
    public List<Concept> getBroaderConcepts()
    {
        return broaderConcepts;
    }
    public List<Concept> getNarrowerConcepts()
    {
        return narrowerConcepts;
    }
    public List<AnnotationObject> getBroaderAnnotObjects()
    {
        return broaderAnnotObjects;
    }
    public void setBroaderAnnotObjects(List<AnnotationObject> broaderAnnotObjects)
    {
        this.broaderAnnotObjects = broaderAnnotObjects;
    }
    public List<AnnotationObject> getNarrowerAnnotObjects()
    {
        return narrowerAnnotObjects;
    }
    public void setNarrowerAnnotObjects(List<AnnotationObject> narrowerAnnotObjects)
    {
        this.narrowerAnnotObjects = narrowerAnnotObjects;
    }
    public boolean isInTable()
    {
        return inTable;
    }
    public void setInTable(boolean inTable)
    {
        this.inTable = inTable;
    }
}
