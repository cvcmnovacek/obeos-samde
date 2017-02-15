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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.primefaces.context.RequestContext;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import global.GUIrefs;
import global.Master;
import utilities.StringUtilities;

@ManagedBean
@SessionScoped
public class CatalogueBean implements Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = -975854115344760839L;
    private String osddUrlString;
    private Document osddDoc;

    private String queryUrl;

    private OsddProcessor osddProcessor = new OsddProcessor();

    private List<ParameterObject> parameterList = new ArrayList<ParameterObject>();

    // RequestData for user-selected url-template
    RequestData requestData = null;


    /////////////////////////
    private String selectedOption;

    public String getSelectedOption()
    {
        return selectedOption;
    }

    public void setSelectedOption(String selectedOption)
    {
        this.selectedOption = selectedOption;
    }
    /////////////////////////

    private String format = "application/atom+xml";
    private String resultType = "collection";

    public void fetchOsddAndBuildDoc()
    {// throws FileNotFoundException {//for
     // the local variant

        osddDoc = buildOsddDoc(FileFetcher.fetchFileFromUrl(osddUrlString));
        osddProcessor.setOsddDoc(osddDoc);
        // osddProcessor.extractAndProcessUrlNodes();
    }

    public void selectUrl()
    {
        requestData = osddProcessor.extractAndProcessUrlNodes(format, resultType);
        if (null == requestData)
        {
            GUIrefs.displayAlert("Reading of OSDD failed.");
            return;
        }
        this.parameterList = requestData.getParameterObjects();

        GUIrefs.updateComponent(GUIrefs.osRequestForm);
        RequestContext.getCurrentInstance().execute("PF('osRequestDlg').show();");
    }

    public Document buildOsddDoc(InputStream instream)
    {

        Document osddDoc = null;

        DocumentBuilder dBuilder;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        try
        {
            dBuilder = dbf.newDocumentBuilder();
            osddDoc = dBuilder.parse(new InputSource(instream));
        }
        catch (IOException ioex)
        {
            GUIrefs.displayAlert("Cannot read imported source -IOException!");
            ioex.printStackTrace();
        }
        catch (ParserConfigurationException pce)
        {
            // TODO Auto-generated catch block
            pce.printStackTrace();
        }
        catch (SAXException saxEx)
        {
            GUIrefs.displayAlert("SAX Exception while parsing OSDD");
            saxEx.printStackTrace();
        }
        return osddDoc;
    }

    public void generateUrl()
    {

        StringBuilder builder = new StringBuilder(requestData.getUrlHeader());
        for (ParameterObject o : this.parameterList)
        {

            if (Master.DEBUG_LEVEL > Master.LOW)
            {
                System.out.println(o.name + ";" + o.value + "; " + o.textValue);
            }

            if (!StringUtilities.isEmpty(o.value))
            {
                builder.append("&" + o.name + "=" + o.value);
                assert (StringUtilities.isEmpty(o.textValue));
            }
            else if (!StringUtilities.isEmpty(o.textValue))
            {
                builder.append("&" + o.name + "=" + o.textValue);
            }

        }
        this.queryUrl = builder.toString();
    }

    // setters and getters
    public boolean isDocumentExists()
    {
        return (osddDoc != null);
    }

    public String getOsddUrlString()
    {
        return osddUrlString;
    }

    public void setOsddUrlString(String osddUrlString)
    {
        this.osddUrlString = osddUrlString;
    }

    public String getQueryUrl()
    {
        return queryUrl;
    }

    public void setQueryUrl(String queryUrl)
    {
        this.queryUrl = queryUrl;
    }

    public String getFormat()
    {
        return format;
    }

    public void setFormat(String format)
    {
        this.format = format;
    }

    public String getResultType()
    {
        return resultType;
    }

    public void setResultType(String resultType)
    {
        this.resultType = resultType;
    }

    public RequestData getRequestData()
    {
        return requestData;
    }

    public void setRequestData(RequestData requestData)
    {
        this.requestData = requestData;
    }

    public List<ParameterObject> getParameterList()
    {
        return parameterList;
    }

    public void setParameterList(List<ParameterObject> parameterList)
    {
        this.parameterList = parameterList;
    }

    // debugging
    public void printParameterList()
    {
        System.out.println("CatalogueBean.printParameters");
        for (ParameterObject o : parameterList)
        {
            System.out.println(o);
        }
    }

//    public void greetingsFromCatalogueBean()
//    {
//        System.out.println("CatalogueBean.greetingsFromCatalogueBean()");
//        GUIrefs.displayAlert("greetings from catalogue bean");
//        // System.out.println(this.selectedParameterObject);
//        System.out.println("selectedOption: " + this.selectedOption);
//    }

}
