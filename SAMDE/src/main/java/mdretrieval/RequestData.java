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

package mdretrieval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

//this class represents the data corresponding to one Url-object, including the query template
//and the list of possible 
public class RequestData {
	private String urlType;
	private String template="string var for template";
	private String urlHeader;
	private String rel; // if this can assume only values "results" or
						// "collection", turn into enum

	public RequestData(String urlType, String template, String rel){
		this.urlType = urlType;
		this.template = template;
		analyseTemplate();
		this.rel = rel;
	}
	//parameter names showing up in template ; name, value 
	Map<String,String> parametersFromTemplate=new HashMap<String,String>();
	//param name -> object encapsulating options and constraints
	Map<String, ParameterObject> substitutionsForTemplate=new HashMap<String,ParameterObject>();//TODO:check number
	List<ParameterObject> parameterObjects = new ArrayList<ParameterObject>();
	

	private void analyseTemplate() 
	{

		if (null != template) 
		{
			urlHeader = template.substring(0,template.indexOf('&'));
			String workString = template.substring(template.indexOf('&')+1, template.length());
			StringTokenizer tokenizer = new StringTokenizer(workString, "&");
			
			while(tokenizer.hasMoreTokens())
			{
				//currToken should be a string of shape 'startRecord={startIndex?}'
				String currToken = tokenizer.nextToken();
				int i = currToken.indexOf('=');
				String name = currToken.substring(0, i);
				String placeholder = currToken.substring(i+1);
				parametersFromTemplate.put(name, placeholder);
				substitutionsForTemplate.put(name, null);
			}
		}
	}
	
	//add parameters from the url template for which no options were specified in 
	//the OSDD
	public void addRemainingParametersFromTemplate(){
		
		for(Entry<String, ParameterObject> e : substitutionsForTemplate.entrySet()){
			if(null==e.getValue()){ 
				ParameterObject po = new ParameterObject();
				//po.setName(e.getKey());
				String v = parametersFromTemplate.get(e.getKey());

//				po.setValue("");
//				po.setPlaceholder(v);
//				po.setDisplayLabel(v.replace("?", ""));
//				po.setPattern(".*");
//				po.setMinInclusive(null);
//				po.setMaxInclusive(null);
				po.initParams(e.getKey(), v, v.replace("?", ""), ".*", null, null);
				parameterObjects.add(po);
			}
			
		}
		
	}
	public void addParameterObject(String name, ParameterObject paramObj)
	{
		substitutionsForTemplate.put(name, paramObj);//remove
		parameterObjects.add(paramObj);
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder("RequestData: ");
		builder.append("urlType: "+ urlType);
		builder.append("\ntemplate: "+ template);
		builder.append("\nrel: "+ rel);
		
		builder.append("\nnmbr of params possible to substitute in template: "+ substitutionsForTemplate.size());
		
		
		for(Map.Entry<String, ParameterObject> e : substitutionsForTemplate.entrySet()){
			builder.append("\n"+ e.getKey() + " ; begin PARAMETER_OBJECT" + e.getValue() + "end PARAMETER_OBJECT\n");
		}
		return builder.toString();
	}
	
	
	// setters and getters

	public String getUrlType() {
		return urlType;
	}

	public void setUrlType(String urlType) {
		this.urlType = urlType;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getUrlHeader() {
		return urlHeader;
	}

	public void setUrlHeader(String urlHeader) {
		this.urlHeader = urlHeader;
	}

	public List<ParameterObject> getParameterObjects() {
		return parameterObjects;
	}
	public void setParameterObjects(List<ParameterObject> parameterObjects) {
		this.parameterObjects = parameterObjects;
	}
	public Map<String, String> getParametersFromTemplate() {
		return parametersFromTemplate;
	}
	public void setParametersFromTemplate(Map<String, String> parametersFromTemplate) {
		this.parametersFromTemplate = parametersFromTemplate;
	}
	public String getRel() {
		return rel;
	}

	public void setRel(String rel) {
		this.rel = rel;
	}

}
