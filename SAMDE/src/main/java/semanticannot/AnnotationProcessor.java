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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.primefaces.model.TreeNode;

import esa.obeos.metadataeditor.model.api.IMdeElement;
import esa.obeos.metadataeditor.model.xsd.beta.gmd.AbstractMDIdentificationType;
import esa.obeos.metadataeditor.model.xsd.beta.gmd.MDIdentificationPropertyType;
import esa.obeos.metadataeditor.model.xsd.beta.gmd.MDKeywordsPropertyType;
import esa.obeos.metadataeditor.model.xsd.beta.gmi.MIAcquisitionInformationPropertyType;
import esa.obeos.metadataeditor.model.xsd.beta.gmi.MIAcquisitionInformationType;
import esa.obeos.metadataeditor.model.xsd.beta.gmi.MIInstrumentPropertyType;
import global.Master;
import tree.NodeContents;
import tree.TreeBean;

public class AnnotationProcessor implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 857546293688671023L;
    public static final String noThesaurus = "<free keywords>";


    public static Map<String, List<DescriptiveKeywordsAnnotationObject>> descrKeywordDataFromModel(
            IMdeElement modelRoot)
    {
        List<MDKeywordsPropertyType> allDescriptiveKeywords;
        try
        {
            allDescriptiveKeywords = AnnotationProcessor.collectDescriptiveKeywordElements(modelRoot);
            return AnnotationProcessor.collectDescrKeywordData(allDescriptiveKeywords, null);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static List<MDKeywordsPropertyType> collectDescriptiveKeywordElements(IMdeElement modelRoot) throws Exception
    {
        List<MDKeywordsPropertyType> allDescriptiveKeywords = new ArrayList<MDKeywordsPropertyType>();
        List<?> idInfoElements = modelRoot.getChildrenByXmlName("identificationInfo");
        for (Object obj : idInfoElements)
        {
            if (obj instanceof MDIdentificationPropertyType)
            {
                MDIdentificationPropertyType identificationInfo = (MDIdentificationPropertyType) obj;

                JAXBElement<? extends AbstractMDIdentificationType> jaxbElmAbstractMDIdentification = identificationInfo
                        .getAbstractMDIdentification();
                AbstractMDIdentificationType abstractMDIdentificationType = jaxbElmAbstractMDIdentification.getValue();
                List<MDKeywordsPropertyType> descriptiveKeywords = abstractMDIdentificationType
                        .getDescriptiveKeywords();
                allDescriptiveKeywords.addAll(descriptiveKeywords);

            }
        }
        return allDescriptiveKeywords;
    }

    public static Map<String, List<DescriptiveKeywordsAnnotationObject>> collectDescrKeywordData(
            List<MDKeywordsPropertyType> allDescriptiveKeywords, List<TreeNode> nodeList) throws Exception
    {
        DataCollectingVisitor keywordVisitor = new DataCollectingVisitor();
        keywordVisitor.setLocalRootName("descriptiveKeywords");
        keywordVisitor.addCounter("keyword");

        Map<String, List<DescriptiveKeywordsAnnotationObject>> annotObjects = new HashMap<String, List<DescriptiveKeywordsAnnotationObject>>();

        int counter = 0;
        for (IMdeElement dc : allDescriptiveKeywords)
        {
            keywordVisitor.reset();
            dc.accept(keywordVisitor);
            List<SubtreeData> descrKeywordData = keywordVisitor.getAllDatasets();

            if (Master.DEBUG_LEVEL > Master.LOW)
                System.out.println("nmbr of descrKeyword-elts found: " + descrKeywordData.size());// is
                                                                                                  // one
            for (SubtreeData sd : descrKeywordData)
            {
                DescriptiveKeywordsAnnotationObject dkao = new DescriptiveKeywordsAnnotationObject(sd);
                if (null != nodeList)
                {
                    System.out.println(sd);
                    /// VERIFICATION AND DEBUGGING
                    //////////////////////////////

                    if (Master.DEBUG_LEVEL > Master.LOW)
                    {
                        TreeNode tmpNode = nodeList.get(counter);
                        IMdeElement el1 = ((NodeContents) tmpNode.getData()).getElement();
                        IMdeElement el2 = sd.getEnclosingElement();
                        System.out.println("Element identity check: el1.equals(el2)? " + el1.equals(el2));
                    }
                    //////////////////////////////////
                    dkao.setLocalRootTreeNode(nodeList.get(counter++));

                }
                String thesaurusURI = dkao.getThesaurusURI();
                if (thesaurusURI.equals(""))
                {
                    thesaurusURI = noThesaurus;
                }
                if (annotObjects.get(thesaurusURI) != null)
                {
                    annotObjects.get(thesaurusURI).add(dkao);
                    if (annotObjects.get(thesaurusURI).size() > 1)
                    {
                        System.out.println(
                                "Thesaurus " + thesaurusURI + "has multiple occurrences - consider merging them!");
                        // DEBUG OUTPUT

                        if (Master.DEBUG_LEVEL > Master.LOW)
                        {
                            for (DescriptiveKeywordsAnnotationObject dko : annotObjects.get(thesaurusURI))
                            {
                                System.out.println(dko);
                            }
                        }
                        ///////////////////
                    }
                }
                else
                {
                    List<DescriptiveKeywordsAnnotationObject> list = new ArrayList<DescriptiveKeywordsAnnotationObject>();
                    list.add(dkao);
                    annotObjects.put(thesaurusURI, list);
                }
            }

        }
        return annotObjects;
    }

    public static List<List<? extends SubtreeDataEnvelope>> collectPlatformAndInstrumentData(IMdeElement modelRoot)
            throws Exception
    {
        List<List<? extends SubtreeDataEnvelope>> ret = null;
        List<SubtreeDataEnvelope> firstLevelInstrRet = null;
        List<SubtreeDataEnvelope> platformEntries = null; // new
                                                          // ArrayList<SubtreeDataEnvelope>();

        List<?> acquInfoList = modelRoot.getChildrenByXmlName("acquisitionInformation");
        if (null == acquInfoList)
        {
            return null;
        }
        else
        {
            ret = new ArrayList<List<? extends SubtreeDataEnvelope>>();
            for (Object obj : acquInfoList)
            {
                if (obj instanceof MIAcquisitionInformationPropertyType)
                {
                    MIAcquisitionInformationType ai = ((MIAcquisitionInformationPropertyType) obj)
                            .getMIAcquisitionInformation();

                    // first collect instrument - objects that are directly
                    // beneath MI_AcquisitionInformation
                    // and not embedded in a platform-node
                    List<?> firstLevelInstruments = ai.getChildrenByXmlName("instrument");

                    DataCollectingVisitor collectingVisitor = new DataCollectingVisitor();
                    collectingVisitor.setLocalRootName("instrument");
                    collectingVisitor.addCounter("MI_SensorTypeCode");

                    if (null != firstLevelInstruments)
                    {
                        firstLevelInstrRet = new ArrayList<SubtreeDataEnvelope>();
                        for (Object obj2 : firstLevelInstruments)
                        {
                            if (obj2 instanceof MIInstrumentPropertyType)
                            {
                                collectingVisitor.reset();
                                MIInstrumentPropertyType instr = (MIInstrumentPropertyType) obj2;
                                instr.accept(collectingVisitor);
                                SubtreeData instrumentData = collectingVisitor.getAllDatasets().get(0);

                                if (Master.DEBUG_LEVEL > Master.LOW)
                                    System.out.println(instrumentData);
                                // create some InstrumentAnnotationObject and
                                // add it to list
                                SubtreeDataEnvelope instrEnv = new InstrumentExtractorImpl(instrumentData);
                                firstLevelInstrRet.add(instrEnv);
                            }
                        }
                    }

                    // now process platform nodes
                    collectingVisitor.setLocalRootName("platform");
                    collectingVisitor.reset();
                    collectingVisitor.addCounter("instrument");
                    ai.accept(collectingVisitor);

                    List<SubtreeData> platformList = collectingVisitor.getAllDatasets();
                    // boolean first = true;
                    for (SubtreeData sd : platformList)
                    {
                        if (platformEntries == null)
                        {
                            platformEntries = new ArrayList<SubtreeDataEnvelope>();
                        }

                        if (Master.DEBUG_LEVEL > Master.LOW)
                            System.out.println(sd);
                        SubtreeDataEnvelope sdEnv = new PlatformAnnotationObject(sd);
                        platformEntries.add(sdEnv);
                    }

                }
            }
            ret.add(firstLevelInstrRet);
            ret.add(platformEntries);
        }
        return ret;
    }

    public static TreeNode setTreeNodeForPlatformElement(PlatformAnnotationObject pao, TreeNode root)
    {
        IMdeElement platform = pao.getBlockData().getEnclosingElement();
        IMdeElement miAcquInfo = platform.getParent();
        IMdeElement acquInfo = miAcquInfo.getParent();

        TreeNode acquInfoNode = TreeBean.getPFTreeChild(root, acquInfo);
        TreeNode miAcquInfoNode = TreeBean.getPFTreeChild(acquInfoNode, miAcquInfo);

        TreeNode platformNode = TreeBean.getPFTreeChild(miAcquInfoNode, platform);
        pao.setLocalRootTreeNode(platformNode);
        return platformNode;
    }


    public static List<Integer> containsKeyword(String thesaurusURI, Concept kw,
            Map<String, List<DescriptiveKeywordsAnnotationObject>> annotationMap)
    {
        List<Integer> ret = new ArrayList<Integer>();
        String key = thesaurusURI;
        if (key.equals(""))
        {
            key = noThesaurus;
        }
        List<DescriptiveKeywordsAnnotationObject> dkaos = annotationMap.get(key);
        if (dkaos == null)
        {
            return ret;
        }
        else
        {
            for (DescriptiveKeywordsAnnotationObject dkao : dkaos)
            {
                if (thesaurusURI.equals(""))
                {
                    ret.add(dkao.containsFreeKeyword(kw.getConceptName()));
                }
                else
                {
                    ret.add(dkao.containsKeyword(kw));
                }
            }
        }
        return ret;
    }

    public static int nmbrPositiveEntries(List<Integer> list)
    {
        int ret = 0;
        for (Integer i : list)
        {
            if (i.intValue() > 0)
            {
                ret++;
            }
        }
        return ret;
    }
}
