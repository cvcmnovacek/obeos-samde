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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.apache.commons.io.IOUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.model.UploadedFile;

import global.GUIrefs;
import global.Master;
import mdretrieval.FileFetcher;
import utilities.StreamUtilities;
import utilities.StringUtilities;


@ManagedBean
@SessionScoped
public class DictionaryBean implements Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = 2671427121830353881L;
    private UploadedFile voidFile;
    private int nmbrDatasets = 0;
    private String selectedURIspace = "";
    private RDFDataset selectedDataset;
    private RDFProcessor rdfParser = new RDFProcessor();
    private Map<String, RDFDataset> datasets;
    private String statusMsg = "No VoID file found - upload first!";
    private String aboutAttribute;
    private String endpoint;
    private String description;
    private String lookupEndpoint;
    private String conceptURI;
    private String selectedRDFdatasetURI = "";
    private String osddURL = "";
    private String template = "";

    private boolean addMoreInfo = false;

    private boolean IMMEDIATE_UPLOAD_FOR_DEBUGGING = false;
    private boolean noOSDD = true;
    private boolean noLookupEndpoint = true;

    private List<AnnotationObject> annotationObjList;
    private List<AnnotationObject> selectedAnnotObjects = new ArrayList<AnnotationObject>();
    private List<AnnotationObject> broaderList;
    private List<AnnotationObject> narrowerList;

    private List<String> freeKeywords = new ArrayList<String>();
    private String keywordToAdd = "";

    private RdfReplyPreprocessor rdfReplyPreprocessor;
    private String defaultUrl;

    public void reset()
    {
        if (annotationObjList != null)
        {
            annotationObjList.clear();
        }
        selectedAnnotObjects.clear();
        freeKeywords.clear();
        if (datasets != null)
        {
            datasets.clear();
        }
        voidFile = null;
        nmbrDatasets = 0;
        selectedURIspace = "";
        selectedDataset = null;
        statusMsg = "No VoID file found - upload first!";
        aboutAttribute = "";
        endpoint = "";
        description = "";
        lookupEndpoint = "";
        conceptURI = "";
        selectedRDFdatasetURI = "";
        osddURL = "";
        template = "";
        GUIrefs.updateComponent("voidInfo");
    }

    public void clearFreeKwList()
    {
        freeKeywords.clear();
    }

    public DictionaryBean()
    {
        if (IMMEDIATE_UPLOAD_FOR_DEBUGGING)
        {
            parseVoidFile();
            statusMsg = "";
        }
    }

    /**
     * Used only for debugging to quickly load a default test file.
     * Not used for production.
     */
    public void parseVoidFile()
    {


        String fileAsString;
        if (IMMEDIATE_UPLOAD_FOR_DEBUGGING)
        {
            byte[] contents = null;
            try
            {
                contents = StreamUtilities.inputStreamToByteArray(
                        new FileInputStream("/home/uuuu/spacebelFiles/DatasetSeries/void2_gcmd.xml"));
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
               e.printStackTrace();
            }
            fileAsString = new String(contents, Charset.defaultCharset());
        }
        else
        {
            printFilename();
            if (null == voidFile)
            {
                GUIrefs.displayAlert("error: no VoID file");
                return;
            }

            fileAsString = new String(voidFile.getContents(), Charset.defaultCharset());

        }
        try
        {
            rdfParser.processRDF(fileAsString);
            this.datasets = rdfParser.getDataSets();
            this.nmbrDatasets = datasets.size();

            GUIrefs.updateComponent("voidInfo");
        }
        catch (Exception e)
        {
            GUIrefs.displayAlert("Error on reading VoID file: " + StringUtilities.escapeQuotes(e.getMessage()));
            e.printStackTrace();
        }
    }

    public void queryFromLookupEndpoint()
    {
        if (conceptURI.isEmpty())
        {
            GUIrefs.displayAlert("Please enter a concept URI");
            return;
        }

        if (this.lookupEndpoint.isEmpty())
        {
            GUIrefs.displayAlert("No Lookup endpoint string " + "This operation is not supported by this thesaurus");
            return;
        }

        String combinedURL = this.lookupEndpoint + conceptURI;

        if (Master.DEBUG_LEVEL > Master.LOW)
            System.out.println("looking up concept: " + combinedURL);

        queryConcept(combinedURL);
    }

    public void sendTemplateAsQuery()
    {
        if (null != template && !template.equals(""))
        {
            queryConcept(template);
        }
        else
        {
            GUIrefs.displayAlert("No search template available");
        }
    }

    public void queryConcept(String queryString)
    {
        String[] conceptDetail = { "", "" };
        try
        {
            conceptDetail = FileFetcher.loadStringFromURL(queryString, true);
            if (conceptDetail[1].contains("application/rdf+xml") || conceptDetail[1].contains("application/xml"))
            {

                if (Master.DEBUG_LEVEL > Master.LOW)
                {
                    System.out.println(
                            conceptDetail[1] + "---> detected/assumed type is rdf - preprocessing of file started");
                    System.out.println(conceptDetail[0]);
                }
                rdfReplyPreprocessor = new RdfReplyPreprocessor();
                rdfReplyPreprocessor.processRdfReply(conceptDetail[0], true);
                this.annotationObjList = rdfReplyPreprocessor.getAnnotationsList();
                lookupBroaderNarrowerAndMarkAsTableMembers();
                deriveDefaultUrl();
                GUIrefs.updateComponent("annotationForm");
                RequestContext.getCurrentInstance().execute("PF('annotSearchReplyProcessingDlg').show();");
            }
            else
            {
                // System.out.println(conceptDetail[1] + "---> assuming " +
                // conceptDetail[1] + " is json");
                GUIrefs.displayAlert("Info: processing of non-rdf reply currently not implemented - reply is "
                        + StringUtilities.escapeQuotes(conceptDetail[0]));
            }
        }
        catch (IOException exc)
        {

            if (Master.DEBUG_LEVEL > Master.LOW)
            {
                System.out.println("Lookup failed:\n" + exc);
                exc.printStackTrace();
            }
            GUIrefs.displayAlert(exc.toString() + "; Thesaurus lookup failed.");
            return;
        }
        catch (Exception e)
        {
            GUIrefs.displayAlert(StringUtilities.escapeQuotes(e.getMessage()));
            if (Master.DEBUG_LEVEL > Master.LOW)
            {
                e.printStackTrace();
            }
        }

    }

    private void deriveDefaultUrl()
    {
        if (null == annotationObjList || annotationObjList.size() == 0)
        {
            defaultUrl = "placeholder - insert valid url";
        }
        else
        {
            AnnotationObject ao = annotationObjList.get(0);
            defaultUrl = StringUtilities.truncateURI(ao.getKeywordUri());
        }
    }

    public String getDefaultUrl()
    {
        return defaultUrl;
    }

    public void addToTable(AnnotationObject obj)
    {
        if (!obj.isInTable())
        {
            lookupBroaderNarrowerAndMarkAsTableMembers(obj);
            this.annotationObjList.add(obj);
        }
        GUIrefs.updateComponent("annotationForm");
    }

    private void lookupBroaderNarrowerAndMarkAsTableMembers(AnnotationObject ao)
    {

        ao.setInTable(true);
        if (null != ao.getBroaderConcepts())
        {
            for (Concept broader : ao.getBroaderConcepts())
            {
                AnnotationObject b = conceptLookup(broader);
                ao.addBroaderAnnotObjects(b);
            }
        }

        if (null != ao.getNarrowerConcepts())
        {
            for (Concept narrower : ao.getNarrowerConcepts())
            {
                AnnotationObject n = conceptLookup(narrower);
                ao.addNarrowerAnnotObjects(n);
            }
        }

        GUIrefs.updateComponent("annotationForm");
    }

    private void lookupBroaderNarrowerAndMarkAsTableMembers()
    {

        for (AnnotationObject ao : annotationObjList)
        {
            lookupBroaderNarrowerAndMarkAsTableMembers(ao);
        }
    }

    public static AnnotationObject conceptLookup(Concept concept)
    {
        AnnotationObject ret = null;
        String[] conceptDetail = { "", "" };
        try
        {
            conceptDetail = FileFetcher.loadStringFromURL(concept.getConceptURI(), true);
            if (conceptDetail[1].contains("application/rdf+xml") || conceptDetail[1].contains("application/xml"))
            {

                if (Master.DEBUG_LEVEL > Master.LOW)
                {
                    System.out.println(
                            conceptDetail[1] + "---> detected/assumed type is rdf - preprocessing of file started");
                    System.out.println(conceptDetail[0]);
                }
                RdfReplyPreprocessor rdfReplyPreprocessor = new RdfReplyPreprocessor();
                rdfReplyPreprocessor.processRdfReply(conceptDetail[0], true);
                // this.annotationObjList.addAll(rdfReplyPreprocessor.getAnnotationsList());
                ret = rdfReplyPreprocessor.getAnnotationsList().get(0);
                // the following assumes that only one concept was processed:
                concept.setConceptName(rdfReplyPreprocessor.getPrefLabel());
            }
            else
            {
                // System.out.println(conceptDetail[1] + "---> assuming " +
                // conceptDetail[1] + " is json");
                GUIrefs.displayAlert("Info: processing of non-rdf reply currently not implemented - reply is "
                        + StringUtilities.escapeQuotes(conceptDetail[0]));
            }
        }
        catch (IOException exc)
        {

            if (Master.DEBUG_LEVEL > Master.LOW)
            {
                System.out.println("Lookup failed:\n" + exc);
                exc.printStackTrace();
            }
            GUIrefs.displayAlert(exc.toString() + "; Thesaurus lookup failed.");

            if (conceptDetail[0].equals(""))
            {
                ret = null;
            }
        }
        catch (Exception e)
        {
            GUIrefs.displayAlert(StringUtilities.escapeQuotes(e.getMessage()));

            if (Master.DEBUG_LEVEL > Master.LOW)
            {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public void printAnnotationObjects()
    {
        for (AnnotationObject obj : selectedAnnotObjects)
        {
            System.out.println("selected: " + obj.getKeyword());
        }
    }


    public void showDialog()
    {

        if (Master.DEBUG_LEVEL > Master.LOW)
        {
            System.out.println("DictionaryBean.showDialog()");
            System.out.println("filename: " + this.voidFile.getFileName());
        }
        RequestContext.getCurrentInstance().execute("PF('voidExtractDlg').show();");
    }

    public void processOSDD()
    {
        try
        {
            // String[] osddStr = FileFetcher.loadStringFromURL(this.osddURL,
            // true);
            String osddStr = IOUtils.toString(FileFetcher.fetchFileFromUrl(this.osddURL));

            if (Master.DEBUG_LEVEL > Master.LOW)
            {
                System.out.println(osddStr);
            }
            OSDDProcessor osddProcessor = new OSDDProcessor();
            osddProcessor.processOSDD(osddStr, true);
            this.template = osddProcessor.getUrlTemplateAttribute();
            if (this.template == null)
            {
                // try again non-namespace-aware
                osddProcessor.processOSDD(osddStr, false);
                this.template = osddProcessor.getUrlTemplateAttribute();
            }
            if (this.template == null)
            {
                GUIrefs.displayAlert("Error: did not find query template!");
                return;
            }
            GUIrefs.updateComponent("searchTemp");


            if (Master.DEBUG_LEVEL > Master.LOW)
                System.out.println(template);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            GUIrefs.displayAlert(StringUtilities.escapeQuotes(e.getMessage()));
        }
    }


    public void updateData()
    {
        GUIrefs.updateComponent("description");
        GUIrefs.updateComponent("aboutAttribute");
        GUIrefs.updateComponent("rdfDataForm");
    }


    public void assignVariables()
    {
        if (null != selectedDataset)
        {
            this.aboutAttribute = this.selectedDataset.aboutAttribute;
            this.endpoint = this.selectedDataset.uriLookupEndpointResource;
            this.description = this.selectedDataset.descriptionChars;
            this.lookupEndpoint = this.selectedDataset.uriLookupEndpointResource;
            this.osddURL = this.selectedDataset.getOpenSearchDescriptionAttribute();

            if (!this.lookupEndpoint.equals(""))
            {
                this.noLookupEndpoint = false;
            }
            if (!this.osddURL.equals(""))
            {
                this.noOSDD = false;
            }
        }
        else
        {
            GUIrefs.displayAlert("Could not extract relevant information from VoID");
        }
    }

    public Set<String> getUrisOfSelectedAnnotationObjects()
    {
        Set<String> ret = new HashSet<String>();
        for (AnnotationObject ao : selectedAnnotObjects)
        {
            ret.add(ao.getKeywordUri());
        }
        return ret;
    }

    // if this in the end is not used in conjunction with the dictionaries,
    // move to separate class
    public void addFreeKeyword()
    {
        this.freeKeywords.add(this.keywordToAdd);
        this.keywordToAdd = "";
        GUIrefs.updateComponent("kwInputBox");
    }

    public void removeFreeKeyword(String keyword)
    {
        this.freeKeywords.remove(keyword);
    }

    public void insertAllFreeKeywords()
    {

    }

    public UploadedFile getVoidFile()
    {
        return voidFile;
    }

    public void setVoidFile(UploadedFile voidFile)
    {

        this.voidFile = voidFile;
        this.statusMsg = "";
        GUIrefs.updateComponent("statusMsg");
    }

    public int getNmbrDatasets()
    {
        return nmbrDatasets;
    }

    public void setNmbrDatasets(int nmbrDatasets)
    {
        this.nmbrDatasets = nmbrDatasets;
    }

    public RdfReplyPreprocessor getRdfReplyPreprocessor()
    {
        return rdfReplyPreprocessor;
    }

    public String getSelectedURIspace()
    {
        return selectedURIspace;
    }

    public void setSelectedURIspace(String selectedURIspace)
    {
        this.selectedURIspace = selectedURIspace;
    }

    public boolean isAddMoreInfo()
    {
        return addMoreInfo;
    }

    public boolean isNoOSDD()
    {

        return noOSDD;
    }

    public void setNoOSDD(boolean noOSDD)
    {

        this.noOSDD = noOSDD;
    }

    public boolean isNoLookupEndpoint()
    {

        return noLookupEndpoint;
    }

    public void setNoLookupEndpoint(boolean noLookupEndpoint)
    {

        this.noLookupEndpoint = noLookupEndpoint;
    }

    public void setAddMoreInfo(boolean addMoreInfo)
    {

        this.addMoreInfo = addMoreInfo;
    }

    public List<String> getFreeKeywords()
    {

        return freeKeywords;
    }

    public void setFreeKeywords(List<String> freeKeywords)
    {

        this.freeKeywords = freeKeywords;
    }

    public String getKeywordToAdd()
    {

        return keywordToAdd;
    }

    public void setKeywordToAdd(String keywordToAdd)
    {

        this.keywordToAdd = keywordToAdd;
    }

    public String getAboutAttribute()
    {

        return aboutAttribute;
    }

    public void setAboutAttribute(String aboutAttribute)
    {

        this.aboutAttribute = aboutAttribute;
    }

    public String getEndpoint()
    {

        return endpoint;
    }

    public void setEndpoint(String endpoint)
    {

        this.endpoint = endpoint;
    }

    public String getDescription()
    {

        return description;
    }

    public void setDescription(String description)
    {

        this.description = description;
    }

    public List<AnnotationObject> getSelectedAnnotObjects()
    {

        return selectedAnnotObjects;
    }

    public void setSelectedAnnotObjects(List<AnnotationObject> selectedAnnotObjects)
    {

        this.selectedAnnotObjects = selectedAnnotObjects;
    }

    public String getConceptURI()
    {

        return conceptURI;
    }

    public void setConceptURI(String conceptURI)
    {

        this.conceptURI = conceptURI;
    }

    public String getLookupEndpoint()
    {

        return lookupEndpoint;
    }

    public void setLookupEndpoint(String lookupEndpoint)
    {

        this.lookupEndpoint = lookupEndpoint;
    }

    public Map<String, RDFDataset> getDatasets()
    {

        return datasets;
    }

    public String getTemplate()
    {

        return template;
    }

    public void setTemplate(String template)
    {

        this.template = template;
    }

    public List<AnnotationObject> getAnnotationObjList()
    {
        return annotationObjList;
    }

    public void setAnnotationObjList(List<AnnotationObject> annotationObjList)
    {

        this.annotationObjList = annotationObjList;
    }

    public void setDatasets(Map<String, RDFDataset> datasets)
    {

        this.datasets = datasets;
    }

    public RDFDataset getSelectedDataset()
    {

        return selectedDataset;
    }

    public void setSelectedDataset(RDFDataset selectedDataset)
    {

        this.selectedDataset = selectedDataset;
    }

    public String getSelectedRDFdatasetURI()
    {

        return selectedRDFdatasetURI;
    }

    public void setSelectedRDFdatasetURI(String selectedRDFdatasetURI)
    {

        this.selectedRDFdatasetURI = selectedRDFdatasetURI;
        if (null != this.datasets)
        {
            this.selectedDataset = this.datasets.get(selectedRDFdatasetURI);
            assignVariables();
            updateData();
        }
    }

    public String getOsddURL()
    {

        return osddURL;
    }

    public void setOsddURL(String osddURL)
    {

        this.osddURL = osddURL;
    }

    public String getStatusMsg()
    {

        return statusMsg;
    }

    public void setStatusMsg(String statusMsg)
    {

        this.statusMsg = statusMsg;
    }


    public void printFilename()
    {
        if (voidFile == null)
        {
            GUIrefs.displayAlert("uploaded file is null!");
        }
        else
        {

            if (Master.DEBUG_LEVEL > Master.LOW)
                System.out.println(voidFile.getFileName());
        }
    }

    public void testdownload()
    {
        InputStream instream = FileFetcher
                .fetchFileFromUrl("http://localhost:8080/SAMDE-0.1.0/rest/mde/getOSDD?fileName=gcmd-osdd.xml");
        try
        {
            Files.copy(instream, new File("/tmp/debugData/testfile").toPath());
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
