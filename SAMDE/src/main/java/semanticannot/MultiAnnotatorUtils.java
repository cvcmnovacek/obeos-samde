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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;

import esa.obeos.metadataeditor.model.api.IMdeElement;
import esa.obeos.metadataeditor.model.api.Model;
import esa.obeos.metadataeditor.model.api.ModelType;
import esa.obeos.metadataeditor.model.api.ModelVersion;
import filehandling.FileData;
import global.Master;


public class MultiAnnotatorUtils
{

    public static void insertKeywordsIntoThesaurusBlocksInSelection(IMdeElement descriptiveKeywords,
            IMdeElement keyword)
    {
        // for (FileData fd : Master.documentsStore.selectedFiles)
        // {
        // Map<String, List<DescriptiveKeywordsAnnotationObject>> annot =
        // fd.getKeywordAnnotation();
        // }
    }

    public static void addFreeKeywordsInSelection(DescriptiveKeywordsAnnotationObject dkaoFromDispFile,
            Set<String> keywordSet)
    {
        if (dkaoFromDispFile == null)
        {
            // a new block was created in display file, so just compare which
            // keywords exist already
            // in current file
            for (FileData fd : Master.documentsStore.selectedFiles)
            {
                if (fd.getCompletePath().equals(Master.documentsStore.activeFileId))
                {
                    continue;
                }
                // element to append keywords to
                IMdeElement mdKeywordElt = null;

                appendAllNonDuplicates(keywordSet, mdKeywordElt, fd);

            }
        }
        else// dkao != null
        {
            Set<String> kwsDisp = dkaoFromDispFile.getKeywordSet();//kw 
            for (FileData fd : Master.documentsStore.selectedFiles)
            {
                if(fd.getCompletePath().equals(Master.documentsStore.activeFileId))
                {
                    continue;
                }
                // keywords were added to dkaoFromDispFile in display file
                // -> look if there is a corresponding block
                Map<String, List<DescriptiveKeywordsAnnotationObject>> annotData = fd.getKeywordAnnotation();
                List<DescriptiveKeywordsAnnotationObject> dkaos = annotData.get(AnnotationProcessor.noThesaurus);

                //by default add to first block, so assign first candidate to 'appendToThisOne'
                //and overwrite if better matching block is found
                DescriptiveKeywordsAnnotationObject appendToThisOne = dkaos.get(0);
                for (DescriptiveKeywordsAnnotationObject dkao : dkaos)
                {
                    Set<String> kws = dkao.getKeywordSet();
                    if (kws.equals(kwsDisp))
                    {
                        appendToThisOne = dkao;
                        break;
                    }
                }
                IMdeElement mdKeywords;
                try
                {
                    mdKeywords = appendToThisOne.getBlockData().getEnclosingElement().getChildByXmlName("MD_Keywords");
                    appendAllNonDuplicates(keywordSet, mdKeywords, fd);
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }

    }

    public static IMdeElement findOrCreateMD_DataId(IMdeElement modelRoot) throws Exception
    {
        IMdeElement idInfo = modelRoot.getChildByXmlName("identificationInfo", 0);
        if (idInfo == null)
        {
            idInfo = modelRoot.createChild("identificationInfo", null, null);
        }
        IMdeElement mdDataId = idInfo.getChildByXmlName("MD_DataIdentification");
        if (mdDataId == null)
        {
            mdDataId = idInfo.createChild("abstractMDIdentificationInfo", "MD_DataIdentification", null);
        }
        return mdDataId;
    }
    
   

    public static IMdeElement appendToMdKeywords(IMdeElement mdKeywordElt, IMdeElement root, String kw, String kwUri) throws Exception
    {
        if (mdKeywordElt == null)
        {
            IMdeElement idInfo = findOrCreateMD_DataId(root);
            IMdeElement descriptiveKeywords = idInfo.createChild("descriptiveKeywords", null, null);
            mdKeywordElt = descriptiveKeywords.createChild("MD_Keywords", null, null);
        }
        if(kwUri==null || kwUri.equals(""))
        {
            createCharStrKw(mdKeywordElt, kw);
        }
        else
        {
            createAnchorKw(mdKeywordElt, kw, kwUri);
        }
        return mdKeywordElt;
    }
    
    private static void createCharStrKw(IMdeElement mdKeywordElt,String kw) throws Exception
    {
        IMdeElement keyword = mdKeywordElt.createChild("keyword", null, null);
        IMdeElement charStr = keyword.createChild("characterString", "CharacterString", null);
        charStr.setElementValue(kw);
    }
    private static void createAnchorKw(IMdeElement mdKeywordElt,String kw,String kwUri) throws Exception
    {
        IMdeElement keyword = mdKeywordElt.createChild("keyword", null, null);
        IMdeElement anchor = keyword.createChild("characterString", "Anchor", null);
        anchor.setElementValue(kw);
        anchor.setAttributeValue("href", kwUri);
    }
    
    private static void appendAllNonDuplicates(Set<String> keywordSet, IMdeElement mdKeywordElt, FileData fd)
    {
        for (String kw : keywordSet)
        {
            Map<String, List<DescriptiveKeywordsAnnotationObject>> annotData = fd.getKeywordAnnotation();
            List<Integer> occurrenceList = AnnotationProcessor.containsKeyword(AnnotationProcessor.noThesaurus,
                    new Concept(kw, ""), annotData);
            if (AnnotationProcessor.nmbrPositiveEntries(occurrenceList) > 0)
            {
                // free keyword 'kw' exists already in current file, so don't
                // add it again
                continue;
            }
            else
            {
                try
                {
                    //in case mdKeywordElt was null, the newly generated IMdeElement is assigned
                    //to 'mdKeywordElt';
                    //otherwise 'appendToMdKeyword' returns a reference to the same element it was passed
                    mdKeywordElt = appendToMdKeywords(mdKeywordElt, fd.getModelRoot(), kw,"");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static void appendCopyOfIMdeElement(IMdeElement source, IMdeElement destinationParent, int indexOfNewChild, ModelType type,
            ModelVersion version) throws Exception
    {
        Document srcDoc = Model.convert(source, false);
        //IMdeElement destination = Model.convert(srcDoc, type, version, false);
        destinationParent.insertChild(srcDoc.getDocumentElement(), source.getFieldName(), indexOfNewChild, null);
    }
    
    

}
