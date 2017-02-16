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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.xml.bind.JAXBElement;

import org.codehaus.plexus.util.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.w3c.dom.Document;

import esa.obeos.metadataeditor.jaxrs.api.datatypes.NodeArgumentType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.NodeType;
import esa.obeos.metadataeditor.model.api.IMdeElement;
import esa.obeos.metadataeditor.model.api.Model;
import esa.obeos.metadataeditor.model.api.exc.AmbiguityException;
import esa.obeos.metadataeditor.model.xsd.beta.gmd.AbstractMDIdentificationType;
import esa.obeos.metadataeditor.model.xsd.beta.gmd.MDIdentificationPropertyType;
import esa.obeos.metadataeditor.model.xsd.beta.gmd.MDKeywordsPropertyType;
import filehandling.FileData;
import global.ConfigurationConstants;
import global.GUIrefs;
import global.Master;
import semanticannot.Concept.LevelOfAgreement;
import tree.MultipleFileEditingBean;
import tree.NodeContents;
import tree.PFTreeBuilderFromModel;
import tree.TreeBean;
import utilities.StringUtilities;

@ManagedBean
@SessionScoped
public class AnnotationInserterBean implements Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = 5383086814471414575L;
    private IMdeElement modelRoot;
    private TreeNode visibleRoot;
    private PFTreeBuilderFromModel builder;
    private boolean isFreeKeywords = true;


    private String annotationType = "";
    // uri used for classification
    private String thesaurusURI = "";

    private String thesaurusName = "";

    // data values in annotation block apart from keywords
    private ThesaurusBlockData thesaurusBlockData = new ThesaurusBlockData();

    private PlatformBlockData platformBlockData = new PlatformBlockData();

    private InstrumentBlockData instrumentBlockData = new InstrumentBlockData();

    private Map<String, String[]> userEditableParams = null;

    private DescriptiveKeywordsAnnotationObject blockIdentifiedForAddition = null;

    private Concept kwToAdd = null;

    private DescriptiveKeywordsAnnotationObject selectedExistingAnnotationBlock;
    private TreeNode newDescriptiveKwNode;
    private IMdeElement newDescriptiveKwElt;

    private Concept newInstrIdentifier = new Concept("", "");

    private MultipleFileEditingBean multipleFileEditor = null;

    private Map<String, String[]> paramsNotFound = null;

    @ManagedProperty(value = "#{dictionaryBean}")
    DictionaryBean dictionaryBean;

    @ManagedProperty(value = "#{annotationAdministratorBean}")
    AnnotationAdministratorBean annotationAdmin;

    @ManagedProperty(value = "#{kwInsertionConflictBean}")
    KwInsertionConflictBean kwInsertionConflictBean;


    public void reset()
    {
        modelRoot = null;
        visibleRoot = null;

        dictionaryBean.reset();
        annotationAdmin.reset();
        GUIrefs.updateComponent("tablePanel");
    }

    public void processAnnotationCreationRequest()
    {
        if (annotationType.equals("platformIdentifier"))
        {
            if (dictionaryBean.getSelectedAnnotObjects().size() > 1)
            {
                GUIrefs.displayAlert("Only one annotation object allowed for identifier parameter.");
                return;
            }
            else if (dictionaryBean.getSelectedAnnotObjects().size() == 1)
            {
                Concept c = dictionaryBean.getSelectedAnnotObjects().get(0).getConcept();
                platformBlockData.setPlatformIdentifier(c);
                completePlatformCreation();
            }
        }
        else if (annotationType.equals("descriptiveKeywords"))
        {
            processRequestForNewKwBlockFromLookupRequest();
            RequestContext.getCurrentInstance().execute("PF('userEditableThesaurusParamsDlg').show();");
        }
        else if (annotationType.equals("instrumentTitle"))
        {
            if (dictionaryBean.getSelectedAnnotObjects().size() > 1)
            {
                GUIrefs.displayAlert("Only one annotation object allowed for instrument title!");

                RequestContext.getCurrentInstance().execute("PF('voidExtractDlg').hide();");
                return;
            }
            else if (dictionaryBean.getSelectedAnnotObjects().size() == 1)
            {
                Concept c = dictionaryBean.getSelectedAnnotObjects().get(0).getConcept();
                instrumentBlockData.setTitle(c);

                RequestContext.getCurrentInstance().execute("PF('voidExtractDlg').hide();");
                GUIrefs.updateComponent("instrTitleTxt");
            }
        }
        else if (annotationType.equals("instrumentIdentifier"))
        {
            for (AnnotationObject ao : dictionaryBean.getSelectedAnnotObjects())
            {
                Concept c = ao.getConcept();
                instrumentBlockData.addIdentifier(c);
            }
            GUIrefs.updateComponent("instrIdentifierTbl");
            RequestContext.getCurrentInstance().execute("PF('voidExtractDlg').hide();");
        }
    }

    // called in annotSearchReplyProcessingDlg.xhtml
    public void processRequestForNewKwBlockFromLookupRequest()
    {
        if (thesaurusURI.equals(""))
        {
            GUIrefs.displayAlert("Thesaurus URI must not be empty!");
            // RequestContext.getCurrentInstance().execute("PF('annotationDetailsFormDlg').show();");//
            // TODO
        }
        else
        {
            thesaurusBlockData.markFieldAsCompleted(ThesaurusBlockData.thesaurusHrefAttrib);

            List<DescriptiveKeywordsAnnotationObject> possibleBlocksToAddTo = annotationAdmin
                    .getSpecificAnnotationDispFile(thesaurusURI);
            if (possibleBlocksToAddTo == null || possibleBlocksToAddTo.size() == 0)
            {
                paramsNotFound = thesaurusBlockData.getRemainingFields();
                createAnnotationNode();// removed 13.01.
                GUIrefs.updateComponent("paramsNotFound");
                GUIrefs.updateComponent("userEditableParams");
                return;
            }
            else if (possibleBlocksToAddTo.size() > 1)
            {
                GUIrefs.displayAlert(
                        "There is more than one block in which the new annotation could be added, so I'm simply"
                                + "appending to the first one.");
                // just take first one
            }
            // else if (possibleBlocksToAddTo.size() == 1)
            // {
            blockIdentifiedForAddition = possibleBlocksToAddTo.get(0);
            userEditableParams = blockIdentifiedForAddition
                    .getUserEditableThesaurusParams(thesaurusBlockData.getRequiredFields());
            paramsNotFound = thesaurusBlockData.getRemainingFields();
            GUIrefs.updateComponent("userEditableParams");
            GUIrefs.updateComponent("paramsNotFound");
            // }
        }
    }

    // called from parameter-editing-dialogue
    public void createKeywordsAndUpdateParams()
    {
        if (blockIdentifiedForAddition != null)
        {
            blockIdentifiedForAddition.saveUserChangesOfThesaurusParams(userEditableParams);
            Set<Concept> existingKeywords = new HashSet<Concept>();
            existingKeywords.addAll(blockIdentifiedForAddition.getKeywordList());
            String thesUri = blockIdentifiedForAddition.getThesaurusURI();
            List<DescriptiveKeywordsAnnotationObject> candidates = Master.documentsStore.getDisplayFile()
                    .getKeywordAnnotation().get(thesUri);

            for (DescriptiveKeywordsAnnotationObject d : candidates)
            {
                if (d == blockIdentifiedForAddition)
                    continue;

                existingKeywords.addAll(d.getKeywordList());
            }


            try
            {
                IMdeElement descrKwElt = blockIdentifiedForAddition.getBlockData().getEnclosingElement();
                TreeNode descrKwNode = blockIdentifiedForAddition.getLocalRootTreeNode();

                createNodesForMissingParams(descrKwElt, descrKwNode);
                IMdeElement mdKeywords = descrKwElt.getChildByXmlName("MD_Keywords");
                TreeNode mdKeywordsNode = TreeBean.getPFTreeChild(descrKwNode, mdKeywords);

                // System.out.println("about to call
                // appendSelectedAnnotationObjectsToMDKeywords : ");
                // System.out.println("mdKeywords.getPath() = " +
                // mdKeywords.getPath());
                appendSelectedAnnotationObjectsToMDkeywords(mdKeywordsNode, mdKeywords, existingKeywords);

                // add in other files of selection as well if applicable
                appendAnnotationObjectsInSelection(mdKeywords);
            }
            catch (Exception e)
            {
                GUIrefs.displayAlert("Error on trying to write parameters");
                e.printStackTrace();
            }
        }
        else // new block -> no duplicity check needed
        {
            try
            {
                createNodesForMissingParams(newDescriptiveKwElt, newDescriptiveKwNode);
                // 13.01. ; rev. 15./16.01.
                IMdeElement mdKeywords = newDescriptiveKwElt.getChildByXmlName("MD_Keywords");
                TreeNode mdKwNode = TreeBean.getPFTreeChild(newDescriptiveKwNode, mdKeywords);
                appendSelectedAnnotationObjectsToMDkeywords(mdKwNode, mdKeywords, null);

                appendAnnotationObjectsInSelection(mdKeywords);
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        GUIrefs.updateComponent("annotationTbl");
    }

    private void appendAnnotationObjectsInSelection(IMdeElement mdKeywords)
    {
        for (FileData fd : Master.documentsStore.selectedFiles)
        {
            if (fd.getCompletePath().equals(Master.documentsStore.activeFileId))
            {
                continue;
            }
            // identify node in current file
            Map<String, List<DescriptiveKeywordsAnnotationObject>> annot = fd.getKeywordAnnotation();
            try
            {
                String thesaurusUri = AnnotationAdministratorBean.thesaurusUriFromMdKeywords(mdKeywords);
                List<DescriptiveKeywordsAnnotationObject> candidatesToAppendTo = annot.get(thesaurusUri);
                if (candidatesToAppendTo == null)
                {
                    // create new annotation block in current file - just
                    // copy all info from mdKeywords from display file; in this
                    // case no duplicity check is necessary
                    IMdeElement mdDataIdentification = MultiAnnotatorUtils.findOrCreateMD_DataId(fd.getModelRoot());
                    IMdeElement newDescriptiveKeywords = mdDataIdentification.createChild("descriptiveKeywords", null,
                            null);
                    MultiAnnotatorUtils.appendCopyOfIMdeElement(mdKeywords, newDescriptiveKeywords, 0,
                            ConfigurationConstants.modelType, ConfigurationConstants.modelVersion);
                    // remove all keywords that were inherited from the display
                    // file and are not among those to append
                    int i = 0;
                    IMdeElement mdKwInNewBlock = newDescriptiveKeywords.getChildByXmlName("MD_Keywords");
                    IMdeElement currKeyword = mdKwInNewBlock.getChildByXmlName("keyword", i++);
                    while (currKeyword != null)
                    {
                        String uri = (String) currKeyword.getAttributeValue("href");
                        Set<String> keywordUrisFromAnnotationObjects = dictionaryBean
                                .getUrisOfSelectedAnnotationObjects();
                        if (!keywordUrisFromAnnotationObjects.isEmpty()
                                && !keywordUrisFromAnnotationObjects.contains(uri))
                        {
                            // delete keyword in copied block:
                            mdKwInNewBlock.deleteChild(currKeyword);
                        }
                        currKeyword = newDescriptiveKeywords.getChildByXmlName("MD_Keywords")
                                .getChildByXmlName("keyword", i++);
                    }

                }
                else
                {
                    Set<Concept> allKeywordsForThesaurus = new HashSet<Concept>();
                    for (DescriptiveKeywordsAnnotationObject dkao : candidatesToAppendTo)
                    {
                        allKeywordsForThesaurus.addAll(dkao.getKeywordList());
                    }
                    // System.out.println("about to call
                    // appendSelectedAnnotationObjectsToMDKeywords : ");
                    // System.out.println("mdKeywords.getPath() = " +
                    // mdKeywords.getPath());
                    appendSelectedAnnotationObjectsToMDkeywords(null, mdKeywords, allKeywordsForThesaurus);
                }

            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


        }
        annotationAdmin.descrKeywordDataFromDispFile();
        annotationAdmin.descrKeywordDataForSelection();
        GUIrefs.updateComponent("annotationTbl");

    }

    private void createNodesForMissingParams(IMdeElement descrKwElt, TreeNode descrKwNode) throws Exception
    {
        IMdeElement mdKwElt = descrKwElt.getChildByXmlName("MD_Keywords");
        TreeNode mdKwNode = null;
        if (null != descrKwNode)
        {
            mdKwNode = TreeBean.getPFTreeChild(descrKwNode, mdKwElt);
        }

        TreeNode ciCitationNode = findOrCreateThesNameNode(mdKwNode);
        // setThesaurusUriAndGetAnchor(ciCitationNode);
        if (paramsNotFound == null)
        {
            return;
        }

        for (Entry<String, String[]> entry : paramsNotFound.entrySet())
        {
            if (!(entry.getValue() == null) && !(entry.getValue()[1].equals("")))
            {
                String path = entry.getKey();
                String[] value = entry.getValue();

                if (path.equals(ThesaurusBlockData.ktcCodeListAttr)
                        || path.equals(ThesaurusBlockData.ktcCodeListValueAttr))
                {
                    IMdeElement typeElt = mdKwElt.getChildByXmlName("type");
                    TreeNode typeNode;

                    if (null == typeElt)
                    {
                        builder.setTreeParent(mdKwNode);
                        typeElt = mdKwElt.createChild("type", null, builder);
                        typeNode = TreeBean.getPFTreeChild(mdKwNode, typeElt);
                        builder.setTreeParent(typeNode);
                    }
                    IMdeElement mdKwTC = typeElt.getChildByXmlName("MD_KeywordTypeCode");
                    if (null == mdKwTC)
                    {
                        mdKwTC = typeElt.createChild("MD_KeywordTypeCode", null, builder);

                    }
                    if (path.equals(ThesaurusBlockData.ktcCodeListAttr))
                    {
                        mdKwTC.setAttributeValue("codeList", value[1]);
                    }
                    else if (path.equals(ThesaurusBlockData.ktcCodeListValueAttr))
                    {
                        mdKwTC.setAttributeValue("codeListValue", value[1]);
                    }

                }
                else if (path.startsWith(ThesaurusBlockData.datePath))
                {

                    TreeNode ciDateNode = findOrCreateCiDateNode(ciCitationNode);
                    builder.setTreeParent(ciDateNode);
                    IMdeElement ciDate = ((NodeContents) ciDateNode.getData()).getElement();
                    if (path.equals(ThesaurusBlockData.date))
                    {
                        IMdeElement interiorDate = ciDate.getChildByXmlName("date");
                        if (interiorDate == null)
                        {
                            interiorDate = ciDate.createChild("date", null, builder);
                        }
                        TreeNode interiorDateNode = TreeBean.getPFTreeChild(ciDateNode, interiorDate);
                        builder.setTreeParent(interiorDateNode);


                        IMdeElement dateOrDT_date = interiorDate.getChildByXmlName("Date");
                        if (dateOrDT_date == null)
                        {
                            dateOrDT_date = interiorDate.createChild("dateOrDateTime", "Date", builder);
                        }
                        TreeNode dtNode = TreeBean.getPFTreeChild(interiorDateNode, dateOrDT_date);
                        dateOrDT_date.setElementValue(value[1]);
                    }
                    else // CI_DateTypeCode
                    {
                        builder.setTreeParent(ciDateNode);
                        IMdeElement dateType = ciDate.getChildByXmlName("dateType");
                        if (dateType == null)
                        {
                            dateType = ciDate.createChild("dateType", null, builder);
                        }
                        TreeNode dateTypeNode = TreeBean.getPFTreeChild(ciDateNode, dateType);
                        builder.setTreeParent(dateTypeNode);
                        IMdeElement dateTypeCode = dateType.getChildByXmlName("CI_DateTypeCode");
                        if (dateTypeCode == null)
                        {
                            dateTypeCode = dateType.createChild("CI_DateTypeCode", null, builder);
                        }
                        TreeNode dateTypeCodeNode = TreeBean.getPFTreeChild(dateTypeNode, dateTypeCode);
                        if (path.equals(ThesaurusBlockData.dateTypeCode))
                        {
                            dateTypeCode.setElementValue(value[1]);
                        }
                        if (path.equals(ThesaurusBlockData.dtcCodeListAttr))
                        {
                            dateTypeCode.setAttributeValue("codeList", value[1]);
                        }
                        if (path.equals(ThesaurusBlockData.dtcCodeListValueAttr))
                        {
                            dateTypeCode.setAttributeValue("codeListValue", value[1]);
                        }
                    }

                }
                else if (path.equals(ThesaurusBlockData.thesaurusTitle))
                {
                    IMdeElement anchor = setThesaurusUriAndGetAnchor(ciCitationNode, null);
                    anchor.setElementValue(value[1]);

                }
            }
        }
    }

    public void processCreationRequest(Map.Entry<String, DescriptiveKeywordsAnnotationObject> entry)
    {

        if (Master.DEBUG_LEVEL > Master.LOW)
            System.out.println(entry.getKey() + "; " + entry.getValue().getKeywordList());

        this.selectedExistingAnnotationBlock = entry.getValue();

        if (entry.getKey().equals(AnnotationProcessor.noThesaurus))
        {
            dictionaryBean.clearFreeKwList();
            GUIrefs.updateComponent("freeKeywordsForm");
            RequestContext.getCurrentInstance().execute("PF('freeKeywordsDlg').show()");
        }
        else
        {
            // open dialog that lets user enter concept uri and concept
            GUIrefs.updateComponent("thesaurusFromEntry");
            RequestContext.getCurrentInstance().execute("PF('keywordsAddingDlg').show()");
        }
    }

    public void onMenuFreeKeywordsClicked()
    {
        FileData disp = Master.documentsStore.getDisplayFile();
        if (disp != null && disp.getKeywordAnnotation() != null)
        {
            annotationAdmin.annotationFromSelection();
        }
        dictionaryBean.clearFreeKwList();
        resetSelectedExistingAnnotationBlock();
        GUIrefs.updateComponent("freeKeywordsForm");
    }

    public void resetSelectedExistingAnnotationBlock()
    {
        this.selectedExistingAnnotationBlock = null;
    }


    public void addFreeKeywords()
    {
        if (null != selectedExistingAnnotationBlock)
        {
            addFreeKeywordsToExistingBlock();
        }
        else
        {
            createAnnotationNode();
        }
        annotationAdmin.descrKeywordDataFromDispFile();
        annotationAdmin.descrKeywordDataForSelection();
        GUIrefs.updateComponent("annotationTbl");
    }

    public void addFreeKeywordsToExistingBlock()
    {

        String thesaurusURI = this.selectedExistingAnnotationBlock.getThesaurusURI();
        // free keywords
        if (thesaurusURI.equals(""))
        {


            IMdeElement mdKeywords;
            try
            {
                mdKeywords = selectedExistingAnnotationBlock.getBlockData().getEnclosingElement()
                        .getChildByXmlName("MD_Keywords");

                TreeNode descrKwNode = selectedExistingAnnotationBlock.getLocalRootTreeNode();
                if (null != descrKwNode)
                {
                    TreeNode mdKeywordsNode = TreeBean.getPFTreeChild(descrKwNode, mdKeywords);
                    builder.setTreeParent(mdKeywordsNode);
                    // remove potential duplicates
                    Set<String> keywordSet = new HashSet<String>(dictionaryBean.getFreeKeywords());
                    for (String kw : keywordSet)
                    {

                        // do a check first if free keyword is contained in
                        // document first
                        // and abort insertion operation if so
                        List<Integer> occurrenceList = AnnotationProcessor.containsKeyword(thesaurusURI,
                                new Concept(kw, ""), Master.documentsStore.getDisplayFile().getKeywordAnnotation());
                        if (AnnotationProcessor.nmbrPositiveEntries(occurrenceList) > 0)
                        {
                            return;
                        }
                        IMdeElement keyword = mdKeywords.createChild("keyword", null, builder);
                        builder.setTreeParent(builder.getLastCreatedChild());
                        IMdeElement kwElt = keyword.createChild("characterString", "CharacterString", builder);
                        kwElt.setElementValue(kw);
                        builder.setTreeParent(mdKeywordsNode);

                        if (MultipleFileEditingBean.coeditSelection)
                        {
                            MultiAnnotatorUtils.addFreeKeywordsInSelection(selectedExistingAnnotationBlock, keywordSet);
                        }

                        annotationAdmin.annotationFromSelection();
                        GUIrefs.updateComponent("annotationTbl");
                        GUIrefs.updateComponent("firstLevelInstrumentsDT");
                        GUIrefs.updateComponent("platformTbl");
                        dictionaryBean.getFreeKeywords().clear();
                        GUIrefs.updateComponent("keywordlist");
                    }
                }
                else
                {
                    GUIrefs.displayAlert("Error - no tree node found for insertion");
                    return;
                }
                resetSelectedExistingAnnotationBlock();
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    public void tryToInsertKwInSelectedBlock()
    {
        checkedKeywordInsertionForDispFile(selectedExistingAnnotationBlock);

        tryToInsertKwInFileSelection(selectedExistingAnnotationBlock);

        kwToAdd.reset();
    }

    public void processReplyFromChoiceDialogue()
    {
        if (kwInsertionConflictBean.getChoice() == 1)
        {
            // Overwrite
            try
            {
                IMdeElement leafInDispFile = overwriteExistingKeywordWithKwToAdd(selectedExistingAnnotationBlock);
                // 19.01.
                if (MultipleFileEditingBean.coeditSelection)
                {
                    annotationAdmin.setLeafForLookup(leafInDispFile);
                    Map<String, String> errorMsgs = annotationAdmin.tryReplacementInSelection();
                    annotationAdmin.setLeafForLookup(null);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
        else if (kwInsertionConflictBean.getChoice() == 2)
        {
            // Discard, i.e. do nothing
        }
        else
        {
            // Error
        }

        // in any case attempt of insertion completed
        kwToAdd.reset();

        annotationAdmin.descrKeywordDataFromDispFile();
        annotationAdmin.descrKeywordDataForSelection();
        GUIrefs.updateComponent("annotationTbl");
    }

    public IMdeElement overwriteExistingKeywordWithKwToAdd(DescriptiveKeywordsAnnotationObject dkao) throws Exception
    {
        IMdeElement dkElt = dkao.getBlockData().getEnclosingElement();
        IMdeElement mdKwElt = dkElt.getChildByXmlName("MD_Keywords");
        IMdeElement kwElt = mdKwElt.getChildByXmlName("keyword", kwInsertionConflictBean.getKeywordIndex());
        IMdeElement leaf = kwElt.getChildByXmlName("Anchor");
        Object uri = leaf.getAttributeValue("href");
        Object label = leaf.getElementValue();
        if (uri instanceof String && label instanceof String)
        {
            String diff = StringUtils.difference((String) uri, kwToAdd.getConceptURI());
            leaf.setElementValue(kwToAdd.getConceptName());
        }
        return leaf;
    }

    public void checkedKeywordInsertionForDispFile(DescriptiveKeywordsAnnotationObject dkao)
    {
        if (kwToAdd.getConceptName() == null || kwToAdd.getConceptName().equals(""))
        {
            DictionaryBean.conceptLookup(kwToAdd);
            GUIrefs.updateComponent("annotationForm");

            if (kwToAdd.getConceptName().equals(""))
            {
                GUIrefs.displayAlert("No concept label found - please check the concept ID!");
                return;
            }
        }
        Map.Entry<String, Object> entry = dkao.getBlockData().firstEntryForStringValue(kwToAdd.getConceptURI());
        if (null != entry)
        {
            // compare values:
            kwInsertionConflictBean.setConflictingEntry(entry);
            if (!kwInsertionConflictBean.compareWithKwToAdd(kwToAdd, dkao))
            {
                GUIrefs.updateComponent("kwVersionSelectionForm");

                // reset lastChosenAlternative in kwInsertionConflictBean to
                // make sure it has the current
                // value when being checked during co-insertion
                kwInsertionConflictBean.setLastChosenAlternative("");

                RequestContext.getCurrentInstance().execute("PF('kwVersionSelectionDlg').show()");
            }
        }
        else
        {
            // keyword is not in the current block so add it
            addKeywordToBlock(dkao, true);
        }
    }

    public void tryToInsertKwInFileSelection(DescriptiveKeywordsAnnotationObject selectedExistingAnnotationBlock)
    {
        String thesaurusUri = selectedExistingAnnotationBlock.getThesaurusURI();
        String thesaurusTitle = selectedExistingAnnotationBlock.getThesaurusTitle();
        for (FileData fd : Master.documentsStore.selectedFiles)
        {
            if (fd.getCompletePath().equals(Master.documentsStore.activeFileId))
            {
                continue;
            }
            // find block corresponding to selectedExistingAnnotationBlock if
            // any
            Map<String, List<DescriptiveKeywordsAnnotationObject>> annot = fd.getKeywordAnnotation();
            List<DescriptiveKeywordsAnnotationObject> candidates = annot.get(thesaurusUri);
            if (candidates != null && !candidates.isEmpty())
            {
                DescriptiveKeywordsAnnotationObject blockToDealWith = null;// candidates.get(0);
                if (candidates.size() >= 1)
                {
                    // if there are more blocks for the same thesaurus, check
                    // them all
                    // whether they already contain the keyword to add
                    for (DescriptiveKeywordsAnnotationObject d : candidates)
                    {
                        // i>0 if keyword URIs coincide
                        int i = d.containsKeyword(kwToAdd);
                        if (i > 0)
                        {
                            blockToDealWith = d;
                            break;
                        }
                    }
                    if (blockToDealWith != null)
                    {
                        Entry<String, Object> entry = blockToDealWith.getBlockData()
                                .firstEntryForStringValue(kwToAdd.getConceptURI());
                        if (entry.getValue() instanceof IMdeElement)
                        {
                            IMdeElement keyword = (IMdeElement) entry.getValue();

                            String value = "";
                            try
                            {
                                if (keyword.getElementValue() instanceof String)
                                {
                                    value = (String) keyword.getElementValue();
                                    if (kwInsertionConflictBean.getLastChosenAlternative()
                                            .equalsIgnoreCase("overwrite"))
                                    {
                                        keyword.setElementValue(kwToAdd.getConceptName());
                                    }
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                    else
                    {
                        blockToDealWith = candidates.get(0);

                        // keyword is not in the current block so add it
                        addKeywordToBlock(blockToDealWith, false);
                    }
                }
                else // candidates is empty, i.e. there is no block for the
                     // current thesaurus
                {
                    // create new block for 'kwToAdd'
                    try
                    {
                        IMdeElement mdKeywords = MultiAnnotatorUtils.appendToMdKeywords(null, fd.getModelRoot(),
                                kwToAdd.getConceptName(), kwToAdd.getConceptURI());
                        // create thesaurus entry
                        IMdeElement ciCitation = findOrCreateThesName(mdKeywords);
                        IMdeElement anchor = setThesaurusUriAndGetAnchor(null, ciCitation);
                        anchor.setElementValue(thesaurusTitle);
                        anchor.setAttributeValue("href", thesaurusUri);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }

        }

    }


    public void checkConcept(Concept toCheck, DescriptiveKeywordsAnnotationObject dkao)
    {
        if (null != toCheck && null != toCheck.getConceptURI() && !toCheck.getConceptURI().equals(""))
        {
            // Concept c = new Concept("", toCheck.getConceptURI());
            setConceptToAdd_uri(toCheck.getConceptURI());

            DictionaryBean.conceptLookup(kwToAdd);

            Map.Entry<String, Object> entry = dkao.getBlockData().firstEntryForStringValue(kwToAdd.getConceptURI());

            // compare values:
            if (!kwToAdd.getConceptName().equals(toCheck.getConceptName()))
            {

                this.selectedExistingAnnotationBlock = dkao;
                kwInsertionConflictBean.setConflictingEntry(entry);
                kwInsertionConflictBean.setAlternative1("Overwrite");
                kwInsertionConflictBean.setAlternative2("Discard");
                String msg = "Lookup gave different result: ";
                msg += "\'" + kwToAdd.getConceptName() + "\' versus \'" + toCheck.getConceptName() + "\' in the file.";
                msg += "\n Would you like to discard the lookup result or to overwrite the concept name in the file?";
                kwInsertionConflictBean.setConflictMessage(msg);
                GUIrefs.updateComponent("kwVersionSelectionForm");
                RequestContext.getCurrentInstance().execute("PF('kwVersionSelectionDlg').show()");
            }

        }
    }

    public void deleteKeywordFromBlockAndUpdateGUI(Concept toDelete, DescriptiveKeywordsAnnotationObject dkao)
            throws Exception
    {
        deleteKeywordFromBlock(toDelete, dkao);

        for (FileData fd : Master.documentsStore.selectedFiles)
        {
            if (fd.getCompletePath().equals(Master.documentsStore.activeFileId))
            {
                continue;
            }

            Map<String, List<DescriptiveKeywordsAnnotationObject>> annot = fd.getKeywordAnnotation();
            String annotKey = dkao.getThesaurusURI();
            if (annotKey.equals(""))
            {
                annotKey = AnnotationProcessor.noThesaurus;
            }
            List<DescriptiveKeywordsAnnotationObject> candidates = annot.get(annotKey);
            if (null != candidates && candidates.size() > 0)
            {
                for (DescriptiveKeywordsAnnotationObject d : candidates)
                {
                    int i = deleteKeywordFromBlock(toDelete, d);
                    if (i == 1)
                    {
                        // testsave
                        Model.save(new File("/tmp/debugData/TEST" + fd.getFilename()), fd.getModelRoot(), false);
                    }
                }

            }
        }
        rereadAnnotationAndUpdateGUI();

    }

    public static int deleteKeywordFromBlock(Concept toDelete, DescriptiveKeywordsAnnotationObject dkao)
            throws Exception
    {
        // return value indicates if something has been deleted
        int ret = 0;
        List<Concept> kwList = dkao.getKeywordList();

        IMdeElement enclosingElt = dkao.getBlockData().getEnclosingElement();


        List<Entry<String, Object>> candidateList = dkao.getBlockData()
                .allEntriesForStringValue(toDelete.getConceptName());

        IMdeElement mdKeywords = enclosingElt.getChildByXmlName("MD_Keywords");
        if (kwList.size() > 1)
        {
            // delete keyword in descriptiveKeyword block
            if (null != candidateList && candidateList.size() > 0)
            {


                for (int i = 0; i < candidateList.size(); i++)
                {
                    String keyName = candidateList.get(i).getKey();
                    String keyUrl = keyName.replace(DataCollectingVisitor.VAL_TOKEN,
                            DataCollectingVisitor.ATTR_TOKEN + "/href");
                    String url = (String) dkao.getBlockData().getParameter(keyUrl);
                    Object obj = candidateList.get(i).getValue();
                    if (obj instanceof IMdeElement)
                    {
                        IMdeElement elt = (IMdeElement) obj;
                        if ((url == null && toDelete.getConceptURI().equals(""))
                                || (url != null && url.equals(toDelete.getConceptURI())))
                        {
                            // extract index from keyName:
                            int index = SubtreeData.eltIndexFromKey(keyName, "keyword");
                            IMdeElement keyword = mdKeywords.getChildByXmlName("keyword", index);
                            mdKeywords.deleteChild(keyword);
                            TreeNode descrKwNode = dkao.getLocalRootTreeNode();
                            if (null != descrKwNode)
                            {
                                TreeNode mdKwNode = TreeBean.getPFTreeChild(descrKwNode, mdKeywords);
                                TreeNode kwNode = TreeBean.getPFTreeChild(mdKwNode, keyword);
                                TreeBean.removeNodeFromTree(kwNode);
                            }
                            // delete only first instance of keyword; if it
                            // shows up more than once, leave other
                            // instances in descriptiveKeywords-block
                            return 1;
                        }
                        else
                        {
                            // not the keyword to delete -> exit loop
                            continue;
                        }

                    }
                }
            }
        }
        else
        {
            // verify that block really contains concept toDelete
            // and delete whole block

            if (candidateList == null || candidateList.get(0) == null)
            {
                GUIrefs.displayAlert("candidateList null or empty");
                ret = 0;
            }
            else
            {
                String keyName = candidateList.get(0).getKey();

                String keyUrl = keyName.replace(DataCollectingVisitor.VAL_TOKEN,
                        DataCollectingVisitor.ATTR_TOKEN + "/href");
                String url = (String) dkao.getBlockData().getParameter(keyUrl);
                // IMdeElement keyword = mdKeywords.getChildByXmlName("keyword",
                // 0);

                if ((url == null && toDelete.getConceptURI().equals(""))
                        || (url != null && url.equals(toDelete.getConceptURI())))
                {
                    enclosingElt.getParent().deleteChild(enclosingElt);
                    ret = 1;

                    TreeNode descrKwNode = dkao.getLocalRootTreeNode();
                    // TODO: make sure this is null in case of non-display file
                    if (null != descrKwNode)
                    {
                        TreeBean.removeNodeFromTree(descrKwNode);
                    }

                }
            }
        }
        return ret;
    }

    public void deletePlatformInSelection(PlatformAnnotationObject pao)
    {
        Master.errorBean.clearMessages();
        IMdeElement platform = pao.getBlockData().getEnclosingElement();
        Concept platfId = pao.getPlatformIdentifier();
        TreeNode platfNode = pao.getLocalRootTreeNode();
        deletePlatform(platform, platfNode, Master.documentsStore.getDisplayFile().getDisplayname());

        for (FileData fd : Master.documentsStore.selectedFiles)
        {
            if (Master.documentsStore.activeFileId.equals(fd.getCompletePath()))
            {
                continue;
            }
            Map<Concept, List<PlatformAnnotationObject>> platfMap = fd.getPlatformAnnotation();

            List<PlatformAnnotationObject> list = platfMap == null ? null : platfMap.get(pao.getPlatformIdentifier());
            PlatformAnnotationObject takeThisOne = null;
            if (list != null)
            {
                for (PlatformAnnotationObject p : list)
                {
                    if (p.getPlatformIdentifier().compare(platfId) == LevelOfAgreement.EQUAL)
                    {
                        boolean instrCoincide = true;
                        for (InstrumentData idat : pao.getInstrumentList())
                        {
                            if (!p.containsInstrument(idat.getCitationTitle(), idat.getIdentifiers(),
                                    Master.errorBean.getErrorMessages(), fd.getFilename()))
                            {
                                instrCoincide = false;
                            }
                        }
                        if (instrCoincide)
                        {
                            takeThisOne = p;
                            break;
                        }
                    }
                }
            }
            if (null != takeThisOne)
            {
                deletePlatform(takeThisOne.getBlockData().getEnclosingElement(), null, fd.getDisplayname());
            }
        }
        Master.errorBean.handleErrors();
        annotationAdmin.platformInstrDataFromDispFile();
        AnnotationAdministratorBean.platformInstrDataFromSelection();

        GUIrefs.updateComponent(GUIrefs.tree);
        GUIrefs.updateComponent("platformTbl");
    }

    public void deletePlatform(IMdeElement platform, TreeNode platfNode, String filename)
    {
        try
        {
            IMdeElement acquInfo = platform.getParent();
            acquInfo.deleteChild(platform);
            if (platfNode != null)
            {
                TreeNode parent = platfNode.getParent();
                parent.getChildren().remove(platfNode);
            }
            if (acquInfo.getChildElements() != null && !acquInfo.getChildElements().isEmpty())
            {
                IMdeElement root = acquInfo.getParent();
                root.deleteChild(acquInfo);
            }
        }
        catch (Exception e)
        {
            Master.errorBean.appendMessage(filename, "Error on deletion of platform: " + e.getMessage());

        }
    }


    public void rereadAnnotationAndUpdateGUI()
    {
        // reread semantic annotation from file
        annotationAdmin.annotationFromSelection();
        GUIrefs.updateComponent("annotationTbl");
        GUIrefs.updateComponent(GUIrefs.tree);
    }

    private void addKeywordToBlock(DescriptiveKeywordsAnnotationObject dkao, boolean synchGuiTree)
    {
        PFTreeBuilderFromModel builderRef = synchGuiTree ? this.builder : null;

        TreeNode dkNode = dkao.getLocalRootTreeNode();
        IMdeElement dkElt = dkao.getBlockData().getEnclosingElement();
        IMdeElement mdKWElt;
        if (!synchGuiTree && null == this.kwToAdd || this.kwToAdd.isEmpty())
        {
            GUIrefs.displayAlert("No data to insert!");
            return;
        }
        try
        {
            mdKWElt = dkElt.getChildByXmlName("MD_Keywords");

            TreeNode mkKWnode = null;
            if (dkNode != null && synchGuiTree)
            {
                mkKWnode = TreeBean.getPFTreeChild(dkNode, mdKWElt);
                builder.setTreeParent(mkKWnode);
            }
            IMdeElement kwElt = mdKWElt.createChild("keyword", null, builderRef);
            if (synchGuiTree)
            {
                builder.setTreeParent(builder.getLastCreatedChild());
            }
            IMdeElement leaf = kwElt.createChild("characterString", "Anchor", builderRef);
            leaf.setElementValue(kwToAdd.getConceptName());
            leaf.setAttributeValue("href", kwToAdd.getConceptURI());

            if (synchGuiTree)
            {
                this.annotationAdmin.descrKeywordDataFromDispFile();
                GUIrefs.updateComponent(GUIrefs.tree);
                GUIrefs.updateComponent("annotationTbl");
            }
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    // create descriptiveKeywords annotation node from scratch
    public void createAnnotationNode()
    {
        try
        {
            if (annotationAdmin.getRetrievalOption().equals("requestFromVoID"))
            {
                isFreeKeywords = false;
            }

            TreeNode mdDataIdentificationNode = findOrCreateDataIdInfoInVisTree();

            IMdeElement mdDataIdentification = ((NodeContents) mdDataIdentificationNode.getData()).getElement();
            // no matter if keywords exist or not, create new descriptiveKeyword
            // element
            builder.setTreeParent(mdDataIdentificationNode);
            IMdeElement descrKeywords = mdDataIdentification.createChild("descriptiveKeywords", null, builder);

            IMdeElement rootOfAnnotationSubtree = descrKeywords;

            TreeNode descrKeywordsNode = builder.getLastCreatedChild();
            builder.setTreeParent(descrKeywordsNode);

            newDescriptiveKwElt = descrKeywords;
            newDescriptiveKwNode = descrKeywordsNode;

            IMdeElement mdKeywords = descrKeywords.createChild("MD_Keywords", null, builder);
            builder.setTreeParent(builder.getLastCreatedChild());
            TreeNode mdKeywordsNode = builder.getLastCreatedChild();


            // this is used in call from "addFreeKeywords"
            if (isFreeKeywords)
            {
                // just create keyword elements
                for (String kw : dictionaryBean.getFreeKeywords())
                {
                    IMdeElement keyword = mdKeywords.createChild("keyword", null, builder);
                    builder.setTreeParent(builder.getLastCreatedChild());
                    IMdeElement kwElt = keyword.createChild("characterString", "CharacterString", builder);
                    kwElt.setElementValue(kw);
                    builder.setTreeParent(mdKeywordsNode);
                }
                if (multipleFileEditor.isCoeditSelection() && Master.documentsStore.selectedFiles.size() > 1)
                {
                    // cocreateAnnotationSubtree(rootOfAnnotationSubtree);
                    MultiAnnotatorUtils.addFreeKeywordsInSelection(blockIdentifiedForAddition,
                            new HashSet<String>(dictionaryBean.getFreeKeywords()));
                }
            }
            else
            {
                // TreeNode anchorNode =
                // createThesaurusTitleAnchor(mdKeywordsNode);
                TreeNode ciCitationNode = findOrCreateThesNameNode(mdKeywordsNode);
                setThesaurusUriAndGetAnchor(ciCitationNode, null);
                // coediting is done in createKeywordsAndUpdateParams


                // removed the following on 13.01.
                // appendSelectedAnnotationObjectsToMDkeywords(mdKeywordsNode,
                // mdKeywords,
                // null/* no already existing kws */);

                // appendAnnotationObjectsInSelection(mdKeywords);
            }
            GUIrefs.updateComponent(GUIrefs.tree);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            GUIrefs.displayAlert(StringUtilities.escapeQuotes(e.getMessage()));
        }
    }

    private void cocreateAnnotationSubtree(IMdeElement rootOfAnnotationSubtree) throws Exception
    {
        Document domOfAnnotationSubtree = Model.convert(rootOfAnnotationSubtree, false);
        NodeArgumentType arg = new NodeArgumentType();
        NodeType nodeType = new NodeType();
        nodeType.setAny(domOfAnnotationSubtree.getDocumentElement());
        arg.setNodeType(nodeType);
        arg.setPath(rootOfAnnotationSubtree.getParent().getPath());
        multipleFileEditor.insertNodeInContainers(arg);
    }

    private TreeNode findOrCreateDataIdInfoInVisTree() throws Exception
    {
        IMdeElement idInfo = modelRoot.getChildByXmlName("identificationInfo", 0);
        // IMdeElement rootOfAnnotationSubtree = null;
        if (null == idInfo)
        {
            if (null != builder)
            {
                builder.setTreeParent(visibleRoot);
            }

            idInfo = modelRoot.createChild("identificationInfo", null, builder);
            // rootOfAnnotationSubtree = idInfo;
        }
        TreeNode idInfoNode = TreeBean.getPFTreeChild(visibleRoot, idInfo);
        IMdeElement mdDataIdentification = idInfo.getChildByXmlName("MD_DataIdentification");
        if (null == mdDataIdentification)
        {
            if (null != builder)
            {
                builder.setTreeParent(idInfoNode);
            }

            mdDataIdentification = idInfo.createChild("abstractMDIdentification", "MD_DataIdentification", builder);

        }
        TreeNode mdDataIdentificationNode = TreeBean.getPFTreeChild(idInfoNode, mdDataIdentification);
        builder.setTreeParent(mdDataIdentificationNode);
        return mdDataIdentificationNode;
    }

    private TreeNode findOrCreateThesNameNode(TreeNode mdKeywordsNode) throws Exception
    {
        builder.setTreeParent(mdKeywordsNode);
        IMdeElement mdKeywords = ((NodeContents) mdKeywordsNode.getData()).getElement();
        IMdeElement thesName = mdKeywords.getChildByXmlName("thesaurusName");
        if (thesName == null)
        {
            thesName = mdKeywords.createChild("thesaurusName", null, builder);
        }
        TreeNode thesNameNode = TreeBean.getPFTreeChild(mdKeywordsNode, thesName);
        builder.setTreeParent(thesNameNode);
        IMdeElement ciCitation = thesName.getChildByXmlName("CI_Citation");
        if (ciCitation == null)
        {
            ciCitation = thesName.createChild("CI_Citation", null, builder);
        }
        return TreeBean.getPFTreeChild(thesNameNode, ciCitation);
    }

    private TreeNode createThesaurusTitleAnchor(TreeNode mdKeywordsNode) throws Exception
    {
        TreeNode thesNameNode = findOrCreateThesNameNode(mdKeywordsNode);
        builder.setTreeParent(thesNameNode);

        IMdeElement thesName = ((NodeContents) thesNameNode.getData()).getElement();
        IMdeElement title = thesName.createChild("title", null, builder);
        TreeNode titleNode = builder.getLastCreatedChild();
        builder.setTreeParent(titleNode);
        title.createChild("characterString", "Anchor", builder);
        return builder.getLastCreatedChild();

    }

    private static IMdeElement findOrCreateThesName(IMdeElement mdKeywords) throws Exception
    {
        IMdeElement thesName = mdKeywords.getChildByXmlName("thesaurusName");
        if (thesName == null)
        {
            thesName = mdKeywords.createChild("thesaurusName", null, null);
        }
        IMdeElement ciCitation = thesName.getChildByXmlName("CI_Citation");
        if (ciCitation == null)
        {
            ciCitation = thesName.createChild("CI_Citation", null, null);
        }
        return ciCitation;
    }

    // changed - 10.01. -> test again
    private IMdeElement setThesaurusUriAndGetAnchor(TreeNode ciCitationNode, IMdeElement ciCitationElt) throws Exception
    {
        PFTreeBuilderFromModel builderRef = null;
        if (null == ciCitationElt)
        {
            builderRef = builder;
        }
        IMdeElement ciCitation = ciCitationElt == null ? ((NodeContents) ciCitationNode.getData()).getElement()
                : ciCitationElt;
        IMdeElement title = ciCitation.getChildByXmlName("title");
        if (builderRef != null)
        {
            builderRef.setTreeParent(ciCitationNode);
        }
        if (null == title)
        {
            title = ciCitation.createChild("title", null, builderRef);
        }
        if (builderRef != null)
        {
            TreeNode titleNode = TreeBean.getPFTreeChild(ciCitationNode, title);
            builderRef.setTreeParent(titleNode);
        }
        IMdeElement anchor = title.getChildByXmlName("Anchor");
        if (null == anchor)
        {
            anchor = title.createChild("characterString", "Anchor", builderRef);
        }
        anchor.setAttributeValue("href", thesaurusURI);
        return anchor;
    }

    private TreeNode findOrCreateCiDateNode(TreeNode ciCitationNode) throws Exception
    {
        IMdeElement ciCitation = ((NodeContents) ciCitationNode.getData()).getElement();
        builder.setTreeParent(ciCitationNode);
        IMdeElement dateElt = ciCitation.getChildByXmlName("date", 0);
        if (dateElt == null)
        {
            dateElt = ciCitation.createChild("date", null, builder);
        }
        TreeNode dateNode = TreeBean.getPFTreeChild(ciCitationNode, dateElt);
        builder.setTreeParent(dateNode);
        IMdeElement ciDate = dateElt.getChildByXmlName("CI_Date");
        if (ciDate == null)
        {
            ciDate = dateElt.createChild("CI_Date", null, builder);
        }
        TreeNode ciDateNode = TreeBean.getPFTreeChild(dateNode, ciDate);
        return ciDateNode;
    }

    private void appendSelectedAnnotationObjectsToMDkeywords(TreeNode mdKeywordsNode, IMdeElement mdKeywords,
            Set<Concept> alreadyExisting) throws Exception
    {
        PFTreeBuilderFromModel builderRef = mdKeywordsNode != null ? builder : null;
        for (AnnotationObject ao : dictionaryBean.getSelectedAnnotObjects())
        {
            if (builderRef != null)
            {
                builderRef.setTreeParent(mdKeywordsNode);
            }

            if (null == alreadyExisting || !alreadyExisting.contains(new Concept(ao.getKeyword(), ao.getKeywordUri())))
            {

                if (Master.DEBUG_LEVEL > Master.LOW)
                    System.out.println(mdKeywords.getPath());
                IMdeElement keyword = mdKeywords.createChild("keyword", null, builderRef);

                if (builderRef != null)
                {
                    builderRef.setTreeParent(builder.getLastCreatedChild());
                }

                IMdeElement kwElt = keyword.createChild("characterString", "Anchor", builderRef);
                kwElt.setElementValue(ao.getKeyword());
                kwElt.setAttributeValue("href", ao.getKeywordUri());
            }
        }

        // reread annotation and update GUI
        annotationAdmin.annotationFromSelection();
        GUIrefs.updateComponent("annotationMainForm");
    }


    // return both list of TreeNodes and its corresponding elements
    @SuppressWarnings("unused")
    public static List<List<?>> collectDescriptiveKeywordNodes(TreeNode treeRoot) throws Exception
    {
        List<MDKeywordsPropertyType> allDescriptiveKeywords = new ArrayList<MDKeywordsPropertyType>();
        List<TreeNode> descrKeywordNodes = new ArrayList<TreeNode>();
        IMdeElement modelRoot = ((NodeContents) treeRoot.getData()).getElement();
        List<?> idInfoElements = modelRoot.getChildrenByXmlName("identificationInfo");

        for (Object obj : idInfoElements)
        {
            if (obj instanceof MDIdentificationPropertyType)
            {
                MDIdentificationPropertyType identificationInfo = (MDIdentificationPropertyType) obj;
                TreeNode node = TreeBean.getPFTreeChild(treeRoot, identificationInfo);

                JAXBElement<? extends AbstractMDIdentificationType> jaxbElmAbstractMDIdentification = identificationInfo
                        .getAbstractMDIdentification();
                AbstractMDIdentificationType abstractMDIdentificationType = jaxbElmAbstractMDIdentification.getValue();
                TreeNode node2 = TreeBean.getPFTreeChild(node, abstractMDIdentificationType);
                List<MDKeywordsPropertyType> descriptiveKeywords = abstractMDIdentificationType
                        .getDescriptiveKeywords();
                allDescriptiveKeywords.addAll(descriptiveKeywords);
                for (IMdeElement e : allDescriptiveKeywords)
                {
                    TreeNode n = TreeBean.getPFTreeChild(node2, e);
                    descrKeywordNodes.add(n);
                }

            }
        }
        // final check - just for verification and debugging
        if (Master.DEBUG_LEVEL > Master.LOW)
        {
            for (int i = 0; i < descrKeywordNodes.size(); i++)
            {
                MDKeywordsPropertyType k = allDescriptiveKeywords.get(i);
                IMdeElement e = ((NodeContents) descrKeywordNodes.get(i).getData()).getElement();
                System.out.println("MDKeywordsPropertyType - object equals IMdeElement ? " + k.equals(e));
            }
        }
        /////////// end debugging
        List<List<?>> returnList = new ArrayList<List<?>>();
        returnList.add(descrKeywordNodes);
        returnList.add(allDescriptiveKeywords);
        return returnList;
    }

    public void continuePlatformCreation()
    {

        if (annotationAdmin.getRetrievalOption().equals("lookupId"))
        {
            DictionaryBean.conceptLookup(platformBlockData.getPlatformIdentifier());
            GUIrefs.updateComponent("newPlatformForm");
        }
        else if (annotationAdmin.getRetrievalOption().equals("requestFromVoID"))
        {
            RequestContext.getCurrentInstance().execute("PF('voidExtractDlg').show();");
            return;
        }
        completePlatformCreation();
    }

    public void completePlatformCreation()
    {
        try
        {
            IMdeElement firstAcquInfo = PlatformInstrumentGenerator.findOrCreateAcquisitionInfo(modelRoot);
            List<IMdeElement> dataNodes = PlatformInstrumentGenerator.createNewPlatformElement(firstAcquInfo);
            PlatformInstrumentGenerator.fillInNewPlatform(dataNodes, platformBlockData);

            if (MultipleFileEditingBean.coeditSelection)
            {
                for (FileData fd : Master.documentsStore.selectedFiles)
                {
                    if (fd.getCompletePath().equals(Master.documentsStore.activeFileId))
                    {
                        continue;
                    }
                    IMdeElement root_s = fd.getModelRoot();

                    IMdeElement firstAcquInfo_s = PlatformInstrumentGenerator.findOrCreateAcquisitionInfo(root_s);
                    List<IMdeElement> dataNodes_s = PlatformInstrumentGenerator
                            .createNewPlatformElement(firstAcquInfo_s);
                    PlatformInstrumentGenerator.fillInNewPlatform(dataNodes_s, platformBlockData);
                }
            }

            // graphical tree:
            TreeNode acquNode = TreeBean.getPFTreeChild(visibleRoot, firstAcquInfo);
            if (null == acquNode)
            {
                builder.setTreeParent(visibleRoot);
                firstAcquInfo.traverse(firstAcquInfo.getFieldName(), builder);
            }
            else
            {
                IMdeElement miAcquInfo = firstAcquInfo.getChildByXmlName("MI_AcquisitionInformation");
                TreeNode miAcquInfoNode = TreeBean.getPFTreeChild(acquNode, miAcquInfo);
                if (null == miAcquInfoNode)
                {
                    builder.setTreeParent(acquNode);
                    miAcquInfo.traverse(miAcquInfo.getFieldName(), builder);
                }
                else
                {
                    // first element of list is platform element
                    builder.setTreeParent(miAcquInfoNode);
                    IMdeElement platform = dataNodes.get(0);
                    platform.traverse(platform.getFieldName(), builder);
                }
            }
            RequestContext.getCurrentInstance().execute("PF('newPlatformDlg').hide();");
            GUIrefs.updateComponent(GUIrefs.tree);
            // reset platformBlockData:
            // platformBlockData = new PlatformBlockData();
            platformBlockData.reset();
            annotationAdmin.platformInstrDataFromDispFile();
            annotationAdmin.platformInstrDataFromSelection();
            GUIrefs.updateComponent("platformTbl");
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void collectDataForNewInstrumentRequest()
    {
        // clear dialogue data
        if (instrumentBlockData != null)
        {
            instrumentBlockData.reset();
        }
        GUIrefs.updateComponent("newInstrumentForm");
        RequestContext.getCurrentInstance().execute("PF('newInstrumentDlg').show();");
    }

    public void createNewInstrument()
    {
        PlatformAnnotationObject pao = annotationAdmin.getPlatformToAddInstrumentTo();

        Concept platfIdentifier = null;
        Master.errorBean.clearMessages();

        if (pao != null)
        {
            platfIdentifier = pao.getPlatformIdentifier();
            try
            {
                List<InstrumentData> instrList = pao.getInstrumentList();
                if (!pao.containsInstrument(instrumentBlockData, Master.errorBean.getErrorMessages(),
                        Master.documentsStore.getDisplayFile().getDisplayname()))
                {
                    AnnotationProcessor.setTreeNodeForPlatformElement(pao, this.visibleRoot);
                    TreeNode platformNode = pao.getLocalRootTreeNode();

                    if (Master.DEBUG_LEVEL > Master.LOW)
                        System.out.println(this.instrumentBlockData);
                    IMdeElement platform = pao.getBlockData().getEnclosingElement();

                    IMdeElement miPlatform = platform.getChildByXmlName("MI_Platform");
                    List<IMdeElement> dataNodes = PlatformInstrumentGenerator.addNewInstrumentToPlatform(miPlatform,
                            isSensorTypeCode());
                    PlatformInstrumentGenerator.fillInDataForNewInstrument(dataNodes, this.instrumentBlockData);

                    TreeNode miPlatfNode = TreeBean.getPFTreeChild(platformNode, miPlatform);
                    IMdeElement instrument = dataNodes.get(dataNodes.size() - 1);

                    builder.setTreeParent(miPlatfNode);
                    instrument.traverse(instrument.getFieldName(), builder);

                    GUIrefs.updateComponent(GUIrefs.tree);
                }
                // reset instrumentBlockData:
                // instrumentBlockData = new InstrumentBlockData();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            // annotationAdmin.setPlatformToAddInstrumentTo(null);
        }
        else// new stand-alone instrument
        {

            if (Master.DEBUG_LEVEL > Master.LOW)
                System.out.println(this.instrumentBlockData);
            List<InstrumentData> list = annotationAdmin.getFirstLevelInstrDispFile();
            String filename = Master.documentsStore.getDisplayFile().getDisplayname();
            Concept newTitle = instrumentBlockData.getTitle();
            boolean duplicate = false;
            if (null != list)
            {
                for (InstrumentData idat : list)
                {
                    boolean duplicateTitles = InstrumentBlockData.compareInstrumentTitles(newTitle,
                            idat.getCitationTitle(), filename);
                    boolean duplicateIdentifiers = InstrumentBlockData.compareIdentifierLists(idat.getIdentifiers(),
                            instrumentBlockData.getIdentifiers());
                    if (duplicateTitles && duplicateIdentifiers)
                    {
                        duplicate = true;
                    }
                }
            }
            if (!duplicate)
            {
                try
                {
                    IMdeElement miAcquInfo = PlatformInstrumentGenerator.findOrCreateMIAcquisInfo(modelRoot);
                    List<IMdeElement> dataNodes = PlatformInstrumentGenerator.newInstrument(miAcquInfo,
                            isSensorTypeCode());
                    PlatformInstrumentGenerator.fillInDataForNewInstrument(dataNodes, this.instrumentBlockData);

                    TreeNode acquNode = TreeBean.getPFTreeChild(visibleRoot, miAcquInfo.getParent());
                    TreeNode miAcquNode = null;
                    // TreeNode instrNode = null;
                    IMdeElement instr = dataNodes.get(dataNodes.size() - 1);
                    IMdeElement rootOfTraversal = null;
                    if (null == acquNode)
                    {
                        builder.setTreeParent(visibleRoot);
                        rootOfTraversal = miAcquInfo.getParent();
                    }
                    else
                    {
                        builder.setTreeParent(acquNode);
                        rootOfTraversal = miAcquInfo;
                        miAcquNode = TreeBean.getPFTreeChild(acquNode, miAcquInfo);
                        if (null != miAcquNode)
                        {
                            builder.setTreeParent(miAcquNode);
                            rootOfTraversal = instr;
                            // instrNode = TreeBean.getPFTreeChild(miAcquNode,
                            // dataNodes.get(dataNodes.size() - 1));
                        }
                    }
                    rootOfTraversal.traverse(rootOfTraversal.getFieldName(), builder);
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (MultipleFileEditingBean.coeditSelection)
            {
                try
                {
                    createNewInstrumentForSelection(platfIdentifier);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            GUIrefs.updateComponent(GUIrefs.tree);

            annotationAdmin.platformInstrDataFromDispFile();
            annotationAdmin.platformInstrDataFromSelection();

            GUIrefs.updateComponent("firstLevelInstrumentsDT");
            GUIrefs.updateComponent("platformTbl");
            GUIrefs.updateComponent("newInstrumentForm");

            // reset data specific to the insertion operation completed
            instrumentBlockData.reset();
            annotationAdmin.setPlatformToAddInstrumentTo(null);

        }
    }


    public void createNewInstrumentForSelection(Concept platformIdentifier) throws Exception
    {
        if (null != platformIdentifier && !platformIdentifier.isEmpty())
        {
            for (FileData fd : Master.documentsStore.selectedFiles)
            {
                if (fd.getCompletePath().equals(Master.documentsStore.activeFileId))
                {
                    continue;
                }
                // find out if appropriate platform exists in file
                Map<Concept, List<PlatformAnnotationObject>> platfMap = fd.getPlatformAnnotation();
                List<PlatformAnnotationObject> paos = null;
                for (Entry<Concept, List<PlatformAnnotationObject>> e : platfMap.entrySet())
                {
                    Concept.LevelOfAgreement equ = e.getKey().compare(platformIdentifier);
                    switch (equ)
                    {
                    case NONE:
                        continue;
                    case EQUAL:
                        paos = e.getValue();
                        break;
                    case NAMESEQUAL:
                    case URLSEQUAL:
                        // maybe process in a future version
                        break;
                    }
                }
                if (paos == null || paos.isEmpty())
                {
                    // there is no appropriate platform, so it's pointless to
                    // add this instrument
                    continue;
                }
                else
                {
                    // add the instrument to the first matching platform
                    // provided it does not exist so far (for the very same
                    // platform)
                    PlatformAnnotationObject pao = paos.get(0);
                    boolean duplicate = pao.containsInstrument(instrumentBlockData, Master.errorBean.getErrorMessages(),
                            fd.getDisplayname());
                    if (!duplicate)
                    {
                        IMdeElement platform = pao.getBlockData().getEnclosingElement();
                        IMdeElement miPlatform = platform.getChildByXmlName("MI_Platform");

                        List<IMdeElement> dataNodes = PlatformInstrumentGenerator.addNewInstrumentToPlatform(miPlatform,
                                isSensorTypeCode());
                        PlatformInstrumentGenerator.fillInDataForNewInstrument(dataNodes, this.instrumentBlockData);
                    }
                }
            }

            this.instrumentBlockData.reset();
            GUIrefs.updateComponent("platformTbl");
        }
        else // stand-alone instrument
        {
            for (FileData fd : Master.documentsStore.selectedFiles)
            {
                if (fd.getCompletePath().equals(Master.documentsStore.activeFileId))
                {
                    continue;
                }
                Map<Concept, List<InstrumentData>> instrMap = fd.getInstrumentAnnotation();
                boolean duplicate = false;
                boolean duplicateTitle = false;
                boolean duplicateIdentifiers = false;
                if (instrMap != null && instrMap.size() > 0)
                {
                    Set<Concept> keys = instrMap.keySet();
                    for (Concept k : keys)
                    {
                        duplicateTitle = InstrumentBlockData.compareInstrumentTitles(instrumentBlockData.getTitle(), k,
                                fd.getDisplayname());
                        List<InstrumentData> list = instrMap.get(k);
                        duplicateIdentifiers = false;
                        if (list != null)
                        {
                            for (InstrumentData d : list)
                            {
                                duplicateIdentifiers = InstrumentBlockData.compareIdentifierLists(
                                        instrumentBlockData.getIdentifiers(), d.getIdentifiers());
                            }
                        }
                    }
                    duplicate = (duplicateTitle && duplicateIdentifiers);
                }
                if (!duplicate)
                {
                    IMdeElement miAcquInfo = PlatformInstrumentGenerator.findOrCreateMIAcquisInfo(fd.getModelRoot());
                    List<IMdeElement> dataNodes = PlatformInstrumentGenerator.newInstrument(miAcquInfo,
                            isSensorTypeCode());
                    PlatformInstrumentGenerator.fillInDataForNewInstrument(dataNodes, this.instrumentBlockData);
                }
            }
            this.instrumentBlockData.reset();
            if (!Master.errorBean.isEmpty())
            {
                GUIrefs.updateComponent("errorForm");
                RequestContext.getCurrentInstance().execute("PF('errorDlg').show();");
            }
            GUIrefs.updateComponent("firstLevelInstrumentsDT");
            GUIrefs.updateComponent("newInstrumentForm");
        }
    }

    public void propagateAnnotationType(String type)
    {
        this.annotationType = type;
        GUIrefs.updateComponent("searchReplySubmitForm");
        GUIrefs.updateComponent("thesaurusUriTxt");
    }

    public void evaluateLookupData()
    {
        annotationAdmin.getLookupData().lookup();
        String name = annotationAdmin.getLookupData().getConcept().getConceptName();
        String uri = annotationAdmin.getLookupData().getConcept().getConceptURI();
        switch (annotationAdmin.getLookupData().getDataFieldName())
        {
        case "instrumentTitle":
            Concept title = new Concept(name, uri);
            setInstrumentTitle(title);
            GUIrefs.updateComponent("instrTitleTxt");
            break;
        case "instrumentIdentifier":
            this.newInstrIdentifier.setConceptName(name);
            this.newInstrIdentifier.setConceptURI(uri);
            GUIrefs.updateComponent("newInstrIdText");
            break;
        }
        annotationAdmin.getLookupData().reset();

    }

    public void printPlatformBlock()
    {
        System.out.println(this.platformBlockData);
    }

    public void clearKwToAdd()
    {
        this.kwToAdd.setConceptName("");
        this.kwToAdd.setConceptURI("");
    }

    public void addIdentifierAndClearField()
    {
        this.instrumentBlockData.addIdentifier(newInstrIdentifier);
        this.newInstrIdentifier = new Concept("", "");
        GUIrefs.updateComponent("newInstrIdText");
    }

    public boolean isThesaurusUriNeeded()
    {
        boolean ret = false;
        if (this.annotationType.equals("descriptiveKeywords"))
        {
            ret = true;
        }
        else if (this.annotationType.equals("platformIdentifier"))
        {
            ret = false;
        }
        GUIrefs.updateComponent("thesaurusUriTxt");
        return ret;
    }

    public boolean isAdditionalParamsRequired()
    {
        if (paramsNotFound != null && paramsNotFound.size() > 0)
        {
            return true;
        }
        return false;
    }

    public void printUserEditableParams()
    {
        if (userEditableParams != null)
        {
            for (Entry<String, String[]> entry : userEditableParams.entrySet())
            {
                System.out.println(entry.getKey());
                System.out.println("[" + entry.getValue()[0] + " ; " + entry.getValue()[1] + "]\n");
            }
        }
    }

    public void setConceptToAdd_name(String name)
    {
        if (null == this.kwToAdd)
        {
            kwToAdd = new Concept();
        }
        kwToAdd.setConceptName(name);
    }

    public void setConceptToAdd_uri(String uri)
    {
        if (null == this.kwToAdd)
        {
            kwToAdd = new Concept();
        }
        kwToAdd.setConceptURI((uri));
    }

    public Map<String, String[]> getUserEditableParams()
    {
        return userEditableParams;
    }

    public void setUserEditableParams(Map<String, String[]> userEditableParams)
    {
        this.userEditableParams = userEditableParams;
    }

    public DescriptiveKeywordsAnnotationObject getBlockIdentifiedForAddition()
    {
        return blockIdentifiedForAddition;
    }

    public void setBlockIdentifiedForAddition(DescriptiveKeywordsAnnotationObject blockIdentifiedForAddition)
    {
        this.blockIdentifiedForAddition = blockIdentifiedForAddition;
    }

    public String getConceptToAdd_name()
    {
        String ret = "";
        if (this.kwToAdd != null)
        {
            ret = this.kwToAdd.getConceptName();
        }
        return ret;
    }

    public String getConceptToAdd_uri()
    {
        String ret = "";
        if (this.kwToAdd != null)
        {
            ret = this.kwToAdd.getConceptURI();
        }
        return ret;
    }

    public void setModelRoot(IMdeElement element)
    {
        this.modelRoot = element;
        this.annotationAdmin.setModelRoot(modelRoot);
    }

    public void setVisibleRoot(TreeNode visRoot)
    {
        this.visibleRoot = visRoot;
        this.annotationAdmin.setVisibleRoot(visRoot);
    }

    public void setMultipleFileEditor(MultipleFileEditingBean multipleFileEditor)
    {
        this.multipleFileEditor = multipleFileEditor;
    }

    public KwInsertionConflictBean getKwInsertionConflictBean()
    {
        return kwInsertionConflictBean;
    }

    public void setKwInsertionConflictBean(KwInsertionConflictBean kwInsertionConflictBean)
    {
        this.kwInsertionConflictBean = kwInsertionConflictBean;
    }

    public DictionaryBean getDictionaryBean()
    {
        return dictionaryBean;
    }

    public void setDictionaryBean(DictionaryBean dictionaryBean)
    {
        this.dictionaryBean = dictionaryBean;
    }

    public boolean isFreeKeywords()
    {
        return isFreeKeywords;
    }

    public void setFreeKeywords(boolean isFreeKeywords)
    {
        this.isFreeKeywords = isFreeKeywords;
    }


    public DescriptiveKeywordsAnnotationObject getSelectedExistingAnnotationBlock()
    {
        return selectedExistingAnnotationBlock;
    }

    public AnnotationAdministratorBean getAnnotationAdmin()
    {
        return annotationAdmin;
    }

    public void setAnnotationAdmin(AnnotationAdministratorBean annotationAdmin)
    {
        this.annotationAdmin = annotationAdmin;
    }

    // public MultiAnnotatorBean getMultiAnnotator()
    // {
    // return multiAnnotator;
    // }
    //
    // public void setMultiAnnotator(MultiAnnotatorBean multiAnnotator)
    // {
    // this.multiAnnotator = multiAnnotator;
    // }

    public void setTreeBuilder(PFTreeBuilderFromModel builder)
    {
        this.builder = builder;
    }

    public String getThesaurusURI()
    {
        return thesaurusURI;
    }

    public void setThesaurusURI(String thesaurusURI)
    {
        this.thesaurusURI = thesaurusURI.replaceAll("\\s+", "");
    }

    public Map<String, String[]> getParamsNotFound()
    {
        return paramsNotFound;
    }

    public void setParamsNotFound(Map<String, String[]> paramsNotFound)
    {
        this.paramsNotFound = paramsNotFound;
    }

    public String getThesaurusName()
    {
        return thesaurusName;
    }

    public void setThesaurusName(String thesaurusName)
    {
        this.thesaurusName = thesaurusName;
    }

    public PlatformBlockData getPlatformBlockData()
    {
        return platformBlockData;
    }

    public void setPlatformBlockData(PlatformBlockData platformBlockData)
    {
        this.platformBlockData = platformBlockData;
    }

    public InstrumentBlockData getInstrumentBlockData()
    {
        return instrumentBlockData;
    }

    public void setInstrumentBlockData(InstrumentBlockData instrumentBlockData)
    {
        this.instrumentBlockData = instrumentBlockData;
    }

    public String getPlatformIdentifierUri()
    {
        return this.platformBlockData.getPlatformIdentifier().getConceptURI();
    }

    public void setPlatformIdentifierUri(String uri)
    {
        this.platformBlockData.getPlatformIdentifier().setConceptURI(uri);
    }

    public String getPlatformIdentifierName()
    {
        return this.platformBlockData.getPlatformIdentifier().getConceptName();
    }

    public void setPlatformIdentifierName(String name)
    {
        this.platformBlockData.getPlatformIdentifier().setConceptName(name);
    }

    public Concept getNewInstrIdentifier()
    {
        return newInstrIdentifier;
    }

    public void setNewInstrIdentifier(Concept newInstrIdentifier)
    {
        this.newInstrIdentifier = newInstrIdentifier;
        this.instrumentBlockData.addIdentifier(newInstrIdentifier);
    }

    public void setInstrumentTitle(Concept instrTitle)
    {
        this.instrumentBlockData.setTitle(instrTitle);
    }

    public Concept getInstrumentTitle()
    {
        return this.instrumentBlockData.getTitle();
    }

    public String getInstrumentTitleName()
    {
        if (this.instrumentBlockData.getTitle() != null)
        {
            return this.instrumentBlockData.getTitle().getConceptName();
        }
        return "";
    }

    public void setInstrumentTitleName(String name)
    {

        if (this.instrumentBlockData.getTitle() != null)
        {
            this.instrumentBlockData.getTitle().setConceptName(name);
        }
        else
        {
            this.instrumentBlockData.setTitle(new Concept(name, ""));
        }
    }

    public boolean isSensorTypeCode()
    {
        return this.instrumentBlockData.isSensorTypeCode();
    }

    public void setSensorTypeCode(boolean stc)
    {
        this.instrumentBlockData.setSensorTypeCode(stc);
    }
}
