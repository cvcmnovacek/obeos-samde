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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.xml.bind.JAXBElement;

import org.primefaces.context.RequestContext;
import org.primefaces.model.TreeNode;
import org.w3c.dom.Document;

import esa.obeos.metadataeditor.model.api.IMdeElement;
import esa.obeos.metadataeditor.model.api.MdePath;
import esa.obeos.metadataeditor.model.api.Model;
import esa.obeos.metadataeditor.model.api.ModelType;
import esa.obeos.metadataeditor.model.api.ModelVersion;
import esa.obeos.metadataeditor.model.xsd.beta.gco.CharacterStringPropertyType;
import esa.obeos.metadataeditor.model.xsd.beta.gmd.AbstractMDIdentificationType;
import esa.obeos.metadataeditor.model.xsd.beta.gmd.MDDataIdentificationType;
import esa.obeos.metadataeditor.model.xsd.beta.gmd.MDIdentificationPropertyType;
import esa.obeos.metadataeditor.model.xsd.beta.gmd.MDKeywordsPropertyType;
import esa.obeos.metadataeditor.model.xsd.beta.gmx.AnchorType;
import filehandling.FileData;
import tree.MultipleFileEditingBean;
import tree.NodeContents;
import utilities.StringUtilities;
import global.ConfigurationConstants;
import global.GUIrefs;
import global.Master;

@ManagedBean
@SessionScoped
public class AnnotationAdministratorBean implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 4665729259717415325L;
    private IMdeElement modelRoot;
    private TreeNode visibleRoot;
    public final ModelType gmiType = ModelType.GMI;
    public final ModelType gmdType = ModelType.GMD;

    private ModelType modelType;
    private String rootElt; // "/MD_Metadata" or "/MI_Metadata"
    // private Map<String, List<IMdeElement>> descriptiveKeywordElements;

    // < thesaurusURI , associated keyword annotation >
    private Map<String, List<DescriptiveKeywordsAnnotationObject>> annotationDispFile;

    private List<List<? extends SubtreeDataEnvelope>> instrPlatformDispFile;
    private List<InstrumentData> firstLevelInstrDispFile;
    private List<PlatformAnnotationObject> platformsDispFile;
    private PlatformAnnotationObject platformToAddInstrumentTo;

    private LookupData lookupData = new LookupData();
    private IMdeElement leafForLookup;
    private String lookupResult = "";


    private String notificationMsg = "";

    private String retrievalOption = "";

    public void annotationFromSelection()
    {
        if (Master.documentsStore.getDisplayFile() != null)
        {
            // all annotation from display file
            annotationFromDispFile();
            // descriptive keyword data for other selected files
            descrKeywordDataForSelection();
            // platform/instrument data for other selected files
            platformInstrDataFromSelection();
        }
    }

    public void annotationFromDispFile()
    {
        descrKeywordDataFromDispFile();
        platformInstrDataFromDispFile();
    }

    public void descrKeywordDataFromDispFile()
    {
        if(visibleRoot==null)
        {
            return;
        }
        List<MDKeywordsPropertyType> allDescrKeywordElts;
        try
        {
            List<List<?>> resultLists = AnnotationInserterBean.collectDescriptiveKeywordNodes(visibleRoot);
            List<TreeNode> treeNodeList = (List<TreeNode>) resultLists.get(0);
            allDescrKeywordElts = (List<MDKeywordsPropertyType>) resultLists.get(1);
            annotationDispFile = AnnotationProcessor.collectDescrKeywordData(allDescrKeywordElts, treeNodeList);// AnnotationProcessor.descrKeywordDataFromModel(modelRoot);
            Master.documentsStore.getDisplayFile().setKeywordAnnotation(annotationDispFile);
            GUIrefs.updateComponent("annotationTbl");
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void showAppropDlg()
    {
        if (this.retrievalOption.equals("requestFromVoID"))
        {
            RequestContext.getCurrentInstance().execute("PF('voidExtractDlg').show();");
        }
        else if (this.retrievalOption.equals("freeKeywords"))
        {
            RequestContext.getCurrentInstance().execute("PF('freeKeywordsDlg').show();");
        }
        // else if(this.retrievalOption.equals("lookupId"))
        // {
        //
        // }
        // else if(this.retrievalOption.equals("provided"))
        // {
        //
        // }
    }

    public void printSelection()
    {
        System.out.println("retrieval Option: " + retrievalOption);
    }

    public boolean isLookup()
    {
        if (retrievalOption.equals("lookupId") || retrievalOption.equals("provided"))
        {
            return true;
        }
        else
            return false;
    }

    public boolean isProvided()
    {
        if (retrievalOption.equals("provided"))
        {
            return true;
        }
        else
            return false;
    }

    public boolean isFieldsCollapsed()
    {
        if (isProvided() || isLookup())
        {
            return false;
        }
        return true;
    }

    public void setLookupField(String fieldName)
    {
        this.lookupData.setDataFieldName(fieldName);
    }

    public void checkAnchor()
    {
        if (leafForLookup != null)
        {
            String uri = "";
            String name = "";
            try
            {
                uri = (String) leafForLookup.getAttributeValue("href");
                name = (String) leafForLookup.getElementValue();
            }
            catch (Exception e)
            {
                GUIrefs.displayAlert("Error on lookup request: " + StringUtilities.escapeQuotes(e.getMessage()));

                if (Master.DEBUG_LEVEL > Master.LOW)
                    e.printStackTrace();
            }
            Concept c = new Concept("", uri);
            AnnotationObject ao = DictionaryBean.conceptLookup(c);
            this.lookupResult = c.getConceptName();
            GUIrefs.updateComponent("anchorLblSelectionForm");
            if (null != ao && !c.getConceptName().equals(name))
            {
                RequestContext.getCurrentInstance().execute("PF('kwCheckReplyDlg').show();");
            }
        }
    }

    public void processAnchorCheckReply(String reply)
    {
        if (reply.equals("replace"))
        {
            try
            {
                leafForLookup.setElementValue(lookupResult);
                GUIrefs.updateComponent("valueLbl");
                GUIrefs.updateComponent("nodecontentsform");
                if (MultipleFileEditingBean.coeditSelection)
                {
                    tryReplacementInSelection();
                }
            }
            catch (Exception e)
            {
                GUIrefs.displayAlert("Error on trying to replace label");
            }
        }
        leafForLookup = null;
        lookupResult = null;
    }

    public String getCurrentAnchorLabel()
    {
        String ret = "";
        try
        {
            if (leafForLookup != null)
            {
                ret = (String) leafForLookup.getElementValue();
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return ret;
    }

    // added 18.01.
    public Map<String, String> tryReplacementInSelection() throws Exception
    {
        Map<String, String> errorReport = null;
        String newValue = (String) leafForLookup.getElementValue();
        for (FileData fd : Master.documentsStore.selectedFiles)
        {
            if (fd.getCompletePath().equals(Master.documentsStore.activeFileId))
            {
                continue;
            }
            IMdeElement root = fd.getModelRoot();
            AnchorCollectingVisitor visitor = new AnchorCollectingVisitor();
            root.accept(visitor);
            for (IMdeElement anchor : visitor.getAnchorList())
            {
                try
                {
                    String uri = (String) anchor.getAttributeValue("href");
                    if (uri != null && uri.equals(leafForLookup.getAttributeValue("href")))
                    {
                        anchor.setElementValue(newValue);
                    }
                }
                catch (Exception e)
                {
                    if (errorReport == null)
                    {
                        errorReport = new HashMap<String, String>();
                    }
                    errorReport.put(fd.getDisplayname(), "Insuccessful replacement");
                }
            }
        }
        return errorReport;
    }

    public void platformInstrDataFromDispFile()
    {
        if(this.modelRoot==null)
        {
            return;
        }
        try
        {
            instrPlatformDispFile = AnnotationProcessor.collectPlatformAndInstrumentData(this.modelRoot);

            if (instrPlatformDispFile == null)
            {
                return;
            }

            List<? extends SubtreeDataEnvelope> tmplist0 = instrPlatformDispFile.get(0);
            if (null != tmplist0)
            {
                firstLevelInstrDispFile = new ArrayList<InstrumentData>();

                for (SubtreeDataEnvelope sde : tmplist0)
                {
                    if (sde instanceof InstrumentExtractorImpl)
                    {
                        InstrumentExtractorImpl instr = (InstrumentExtractorImpl) sde;
                        instr.setPathPrefix("MI_Instrument");
                        firstLevelInstrDispFile.add(instr.getInstrumentData());
                    }
                }
            }

            List<? extends SubtreeDataEnvelope> tmplist = instrPlatformDispFile.get(1);

            if (null != tmplist)
            {
                platformsDispFile = new ArrayList<PlatformAnnotationObject>();

                for (SubtreeDataEnvelope sde : tmplist)
                {
                    if (sde instanceof PlatformAnnotationObject)
                    {
                        platformsDispFile.add((PlatformAnnotationObject) sde);
                    }
                }
            }

        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        GUIrefs.updateComponent("platformTbl");
        GUIrefs.updateComponent("firstLevelInstrumentsDT");
    }

    public static void platformInstrDataFromSelection()
    {
        for (FileData fd : Master.documentsStore.selectedFiles)
        {
            if (fd.getCompletePath().equals(Master.documentsStore.activeFileId))
            {
                continue;
            }
            try
            {
                List<List<? extends SubtreeDataEnvelope>> platformInstrumentsLists = AnnotationProcessor
                        .collectPlatformAndInstrumentData(fd.getModelRoot());

                if (platformInstrumentsLists == null)
                {
                    continue;
                }

                List<? extends SubtreeDataEnvelope> tmplist0 = platformInstrumentsLists.get(0);
                if (null != tmplist0)
                {
                    List<InstrumentData> firstLevelInstr = new ArrayList<InstrumentData>();

                    for (SubtreeDataEnvelope sde : tmplist0)
                    {
                        if (sde instanceof InstrumentExtractorImpl)
                        {
                            InstrumentExtractorImpl instr = (InstrumentExtractorImpl) sde;
                            instr.setPathPrefix("MI_Instrument");
                            firstLevelInstr.add(instr.getInstrumentData());
                        }
                    }
                    fd.setInstrumentAnnotation(getFirstLevelInstrumentsMap(firstLevelInstr));
                }


                List<? extends SubtreeDataEnvelope> tmplist = platformInstrumentsLists.get(1);

                if (null != tmplist)
                {
                    // List<PlatformAnnotationObject> platforms = new
                    // ArrayList<PlatformAnnotationObject>();
                    Map<Concept, List<PlatformAnnotationObject>> platforms = new LinkedHashMap<Concept, List<PlatformAnnotationObject>>();

                    for (SubtreeDataEnvelope sde : tmplist)
                    {
                        if (sde instanceof PlatformAnnotationObject)
                        {
                            PlatformAnnotationObject pao = (PlatformAnnotationObject) sde;
                            Concept c = pao.getPlatformIdentifier();
                            if (platforms.get(c) != null)
                            {
                                platforms.get(c).add(pao);
                            }
                            else
                            {
                                List<PlatformAnnotationObject> platformsForIdentifier = new ArrayList<PlatformAnnotationObject>();
                                platformsForIdentifier.add(pao);
                                platforms.put(c, platformsForIdentifier);
                            }

                        }
                    }
                    fd.setPlatformAnnotation(platforms);
                }

            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    public boolean isStandaloneInstruments()
    {
        if (firstLevelInstrDispFile != null && firstLevelInstrDispFile.size() > 0)
        {
            return true;
        }
        return false;
    }

    // do analysis for all selected files
    public void descrKeywordDataForSelection()
    {
        for (FileData fd : Master.documentsStore.selectedFiles)
        {
            if (fd.getCompletePath().equals(Master.documentsStore.activeFileId))
            {
                continue;
            }
            if (fd.getModelVersion() == null)
            {
                fd.setModelVersion(ConfigurationConstants.modelVersion);
            }

            // if model root is still null, FileData doesn't contain data
            if (fd.getModelRoot() == null)
            {
                GUIrefs.displayAlert("No input data for " + fd.getDisplayname());
                continue;
            }
            Map<String, List<DescriptiveKeywordsAnnotationObject>> annot = AnnotationProcessor
                    .descrKeywordDataFromModel(fd.getModelRoot());
            fd.setKeywordAnnotation(annot);
        }
    }

    public ArrayList<String> getThesaurusURIs()
    {
        return new ArrayList<String>(annotationDispFile.keySet());
    }


    public List<Map.Entry<String, DescriptiveKeywordsAnnotationObject>> getKeywordAnnotObjectEntries()
    {
        List<Map.Entry<String, DescriptiveKeywordsAnnotationObject>> annotList = new ArrayList<Map.Entry<String, DescriptiveKeywordsAnnotationObject>>();

        if (null != annotationDispFile)
        {
            for (Map.Entry<String, List<DescriptiveKeywordsAnnotationObject>> e : annotationDispFile.entrySet())
            {
                if (e.getValue().size() > 1)
                {
                    this.notificationMsg += "The thesaurus " + e.getKey()
                            + " occurs multiple times - consider merging the corresponding annotation elements!\n";
                }
                for (DescriptiveKeywordsAnnotationObject ao : e.getValue())
                {
                    Map.Entry<String, DescriptiveKeywordsAnnotationObject> newEntry = new AbstractMap.SimpleEntry<String, DescriptiveKeywordsAnnotationObject>(
                            e.getKey(), ao);
                    annotList.add(newEntry);
                }
            }
        }
        return annotList;
    }

    private static Map<Concept, List<InstrumentData>> getFirstLevelInstrumentsMap(
            List<InstrumentData> firstLevelInstruments)
    {
        Map<Concept, List<InstrumentData>> instrMap = null;
        if (null != firstLevelInstruments)
        {

            for (InstrumentData idat : firstLevelInstruments)
            {
                Concept key = idat.getCitationTitle();
                if (instrMap == null)
                {
                    instrMap = new HashMap<Concept, List<InstrumentData>>();
                }
                List<InstrumentData> list = instrMap.get(key);
                if (list == null)
                {
                    list = new ArrayList<InstrumentData>();
                }
                list.add(idat);
                instrMap.put(key, list);
            }
        }
        return instrMap;
    }

    public List<DescriptiveKeywordsAnnotationObject> getSpecificAnnotationDispFile(String thesaurusURI)
    {
        return annotationDispFile.get(thesaurusURI);
    }

    public static String thesaurusUriFromMdKeywords(IMdeElement mdKeywords) throws Exception
    {
        IMdeElement thesaurusName = mdKeywords.getChildByXmlName("thesaurusName");
        IMdeElement ciCitation = null;
        IMdeElement title = null;
        IMdeElement anchor = null;
        String thesUri = "";

        if (thesaurusName != null)
        {
            ciCitation = thesaurusName.getChildByXmlName("CI_Citation");
            if (ciCitation != null)
            {
                title = ciCitation.getChildByXmlName("title");
                if (title != null)
                {
                    anchor = title.getChildByXmlName("Anchor");
                    if (anchor != null)
                    {
                        thesUri = (String) anchor.getAttributeValue("href");
                    }
                }

            }
        }
        else
        {
            throw new Exception("Error: no element thesaurusName in current file");
        }
        return thesUri;
    }


    public void reset()
    {
        // TODO
        if (firstLevelInstrDispFile != null)
        {
            this.firstLevelInstrDispFile.clear();
        }
        if (instrPlatformDispFile != null)
        {
            this.instrPlatformDispFile.clear();
        }
        if (annotationDispFile != null)
        {
            this.annotationDispFile.clear();
        }
        if (platformsDispFile != null)
        {
            this.platformsDispFile.clear();
        }
        this.platformToAddInstrumentTo = null;
        this.notificationMsg = "";

        this.retrievalOption = "";

        GUIrefs.updateComponent("annotationTbl");
        GUIrefs.updateComponent("platformTbl");
        GUIrefs.updateComponent("firstLevelInstrumentsDT");
    }


    public IMdeElement getModelRoot()
    {
        return modelRoot;
    }

    public void setModelRoot(IMdeElement modelRoot)
    {
        this.modelRoot = modelRoot;
    }

    public LookupData getLookupData()
    {
        return lookupData;
    }

    public void setLookupData(LookupData lookupData)
    {
        this.lookupData = lookupData;
    }

    public String getLookupResult()
    {
        return lookupResult;
    }

    public void setLookupResult(String lookupResult)
    {
        this.lookupResult = lookupResult;
    }

    public String getLookupUri()
    {
        return lookupData.getConcept().getConceptURI();
    }

    public void setLookupUri(String uri)
    {
        this.lookupData.getConcept().setConceptURI(uri);
    }

    public IMdeElement getLeafForLookup()
    {
        return leafForLookup;
    }

    public void setLeafForLookup(IMdeElement leafForLookup)
    {
        this.leafForLookup = leafForLookup;
    }

    public List<PlatformAnnotationObject> getPlatformsDispFile()
    {
        return platformsDispFile;
    }

    public void setPlatformsDispFile(List<PlatformAnnotationObject> platformsDispFile)
    {
        this.platformsDispFile = platformsDispFile;
    }

    public List<InstrumentData> getFirstLevelInstrDispFile()
    {
        return firstLevelInstrDispFile;
    }

    public TreeNode getVisibleRoot()
    {
        return visibleRoot;
    }

    public void setVisibleRoot(TreeNode visibleRoot)
    {
        this.visibleRoot = visibleRoot;
        this.modelRoot = ((NodeContents) visibleRoot.getData()).getElement();
    }

    public PlatformAnnotationObject getPlatformToAddInstrumentTo()
    {
        return platformToAddInstrumentTo;
    }

    public void setPlatformToAddInstrumentTo(PlatformAnnotationObject platformToAddInstrumentTo)
    {
        this.platformToAddInstrumentTo = platformToAddInstrumentTo;
    }

    public String getRetrievalOption()
    {
        return retrievalOption;
    }

    public void setRetrievalOption(String retrievalOption)
    {
        this.retrievalOption = retrievalOption;
    }

}
