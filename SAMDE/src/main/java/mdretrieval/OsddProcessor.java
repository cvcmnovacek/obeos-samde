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

package mdretrieval;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import global.GUIrefs;
import global.ServerConstants;

public class OsddProcessor implements Serializable{

	// List<RequestData> requestDataList = new ArrayList<RequestData>();// ex: 4
	// elements
	// corr.
	// to
	// Url's
	// with
	// templates
	// mapping: parameter name-> selected value --- after user has made his
	// choice
	// Map<String, String> paramMappings = new HashMap<String, String>();

	/**
	 * 
	 */
	private static final long serialVersionUID = 7932404139140826726L;
	// mapping: parameter name-> list of possible values --- extracting from
	// OSDD for what
	// Map<String, List<String>> userChoices = new HashMap<String,
	// List<String>>();
	Document osddDoc;

	public RequestData extractAndProcessUrlNodes(String typeArg, String relArg) 
	{
		NodeList urlnodes;
		RequestData ret = null;
		if (null != osddDoc) {
			urlnodes = osddDoc.getElementsByTagNameNS("*","Url");

			if (urlnodes != null && urlnodes.getLength() > 0) 
			{
				for (int i = 0; i < urlnodes.getLength(); i++) 
				{
					Node tmpnode = urlnodes.item(i);
					if (tmpnode.getNodeType() == Node.ELEMENT_NODE) 
					{
						Element e = (Element) tmpnode;
						String type = e.getAttribute("type");
						String rel = e.getAttribute("rel");

						if (type.equals(typeArg) && rel.equals(relArg)) 
						{
							String template = e.getAttribute("template");
							ret = new RequestData(type, template, rel);

							NodeList paramNodes = e.getChildNodes();
							if (null != paramNodes && paramNodes.getLength() > 0) 
							{
								for (int j = 0; j < paramNodes.getLength(); j++) 
								{
									ParameterObject params = processParameterChild(paramNodes.item(j));
									if (null != params) 
									{
										ret.addParameterObject(params.getName(), params);
									}
								}
							}
							ret.addRemainingParametersFromTemplate();
							return ret;
						}
					} // end if ELEMENT_NODE
				}
			}
		} else {
			GUIrefs.displayAlert("Cannot process data from OSDD - document is null");
		}
		return ret;
	}

	private ParameterObject processParameterChild(Node node) 
	{
		ParameterObject params = null;

		if (node.getNodeType() == Node.ELEMENT_NODE) {
			params = new ParameterObject();
			Element e = (Element) node;
			//String value = e.getAttribute("value");
			String displayLabel =  e.getAttribute("value");
			String name = e.getAttribute("name");
			String pattern = e.getAttribute("pattern");

			String minInclusive = e.getAttribute("minInclusive");
			String maxInclusive = e.getAttribute("maxInclusive");
			Integer iMinInclusive = null;
			Integer iMaxInclusive = null;

//			params.setName(name);
//			params.setValue("");
//			params.setDisplayLabel(displayLabel);
//			params.setPlaceholder(displayLabel);
//			params.setPattern(pattern);

			if (!minInclusive.equals("")) {
				iMinInclusive = new Integer(Integer.parseInt(minInclusive));
			}
			if (!maxInclusive.equals("")) {
				iMaxInclusive = new Integer(Integer.parseInt(maxInclusive));
			}
			params.initParams(name, displayLabel, displayLabel, pattern, iMinInclusive, iMaxInclusive);
//			params.setMinInclusive(iMinInclusive);
//			params.setMaxInclusive(iMaxInclusive);

			NodeList options = e.getChildNodes();

			if (options != null && options.getLength() > 0) {
				params.initOptionsMap();
				//params.addOption("void", "");
				for (int j = 0; j < options.getLength(); j++) {
					Node opNode = options.item(j);
					if (opNode.getNodeType() == Node.ELEMENT_NODE) {
						Element optElt = (Element) opNode;
						String opValue = optElt.getAttribute("value");
						String opLabel = optElt.getAttribute("label");
						params.addOption(opLabel,opValue);
					}

				}
			}
		}
		return params;
	}

	public void extractQueryTemplates() {

	}

	public String buildOSQueryFromParams() {
		String ret = "";
		// URLEncoder.encode(reworkedTemplateString, "UTF-8");
		return ret;
	}

	// setters and getters
	public Document getOsddDoc() {
		return osddDoc;
	}

	public void setOsddDoc(Document osddDoc) {
		this.osddDoc = osddDoc;
	}

}
