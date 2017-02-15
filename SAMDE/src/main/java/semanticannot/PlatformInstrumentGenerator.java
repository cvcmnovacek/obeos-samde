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


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import esa.obeos.metadataeditor.model.api.IMdeElement;

public class PlatformInstrumentGenerator
{
    public static List<IMdeElement> addNewInstrumentToPlatform(IMdeElement miPlatform, boolean sensorTypeCode)
            throws Exception
    {

        // create instrument/MI_Instrument
        IMdeElement instrument = miPlatform.createChild("instrument", null, null);

        return createMiInstrument(instrument, sensorTypeCode);
    }

    public static List<IMdeElement> createMiInstrument(IMdeElement instrument, boolean sensorTypeCode) throws Exception
    {

        List<IMdeElement> dataNodes = new ArrayList<IMdeElement>();
        IMdeElement miInstrument = instrument.createChild("MI_Instrument", null, null);

        // create elements 'citation' and 'type'
        IMdeElement citation = miInstrument.createChild("citation", null, null);
        IMdeElement type = miInstrument.createChild("type", null, null);
        if (sensorTypeCode)
        {
            type.createChild("MI_SensorTypeCode", null, null);
        }
        // expand 'citation'
        IMdeElement ciCitation = citation.createChild("CI_Citation", null, null);
        IMdeElement title = ciCitation.createChild("title", null, null);
        dataNodes.add(title);

        IMdeElement date = ciCitation.createChild("date", null, null);
        dataNodes.addAll(createDateSubtree(date));

        dataNodes.add(ciCitation);

        dataNodes.add(instrument);

        return dataNodes;
    }

    public static List<IMdeElement> newInstrument(IMdeElement miAcquInfo, boolean sensorTypeCode) throws Exception
    {
        IMdeElement instrument = miAcquInfo.createChild("instrument", null, null);
        return createMiInstrument(instrument, sensorTypeCode);
    }

    public static IMdeElement createCharacterStringOrAnchor(IMdeElement parent, Concept c) throws Exception
    {
        IMdeElement ret = null;
        if (c.getConceptURI().equals(""))
        {
            ret = parent.createChild("characterString", "CharacterString", null);
            ret.setElementValue(c.getConceptName());
        }
        else
        {
            ret = parent.createChild("characterString", "Anchor", null);
            ret.setAttributeValue("href", c.getConceptURI());
            ret.setElementValue(c.getConceptName());
        }
        return ret;
    }

    public static List<IMdeElement> createDateSubtree(IMdeElement date) throws Exception
    {
        IMdeElement ciDate = date.createChild("CI_Date", null, null);
        IMdeElement int_date = ciDate.createChild("date", null, null);
        IMdeElement dDate = int_date.createChild("dateOrDateTime", "Date", null);
        IMdeElement dateType = ciDate.createChild("dateType", null, null);
        IMdeElement dateTypeCode = dateType.createChild("CI_DateTypeCode", null, null);
        List<IMdeElement> ret = new ArrayList<IMdeElement>();
        ret.add(dDate);
        ret.add(dateTypeCode);
        return ret;
    }

    public static List<IMdeElement> createNewPlatformElement(IMdeElement acquisitionInfo) throws Exception
    {
        IMdeElement miAcqu = acquisitionInfo.getChildByXmlName("MI_AcquisitionInformation");
        if (miAcqu == null)
        {
            miAcqu = acquisitionInfo.createChild("MI_AcquisitionInformation", null, null);
        }
        IMdeElement platform = miAcqu.createChild("platform", null, null);
        IMdeElement miPlatform = platform.createChild("MI_Platform", null, null);

        IMdeElement identifier = miPlatform.createChild("identifier", null, null);
        IMdeElement description = miPlatform.createChild("description", null, null);

        IMdeElement mdIdentifier = identifier.createChild("mdIdentifier", "MD_Identifier", null);
        IMdeElement code = mdIdentifier.createChild("code", null, null);

        List<IMdeElement> dataNodes = new ArrayList<IMdeElement>();
        // dataNodes.add(identifier);
        dataNodes.add(platform);
        dataNodes.add(description);
        dataNodes.add(code);

        return dataNodes;
    }

    public static IMdeElement findOrCreateAcquisitionInfo(IMdeElement modelRoot) throws Exception
    {
        IMdeElement acquInfo = modelRoot.getChildByXmlName("acquisitionInformation", 0);
        if (acquInfo == null)
        {
            acquInfo = modelRoot.createChild("acquisitionInformation", null, null);
        }
        return acquInfo;
    }

    public static IMdeElement findOrCreateMIAcquisInfo(IMdeElement modelRoot) throws Exception
    {
        IMdeElement acquInfo = findOrCreateAcquisitionInfo(modelRoot);
        IMdeElement miAcquInfo = acquInfo.getChildByXmlName("MI_AcquisitionInformation");
        if (miAcquInfo == null)
        {
            miAcquInfo = acquInfo.createChild("MI_AcquisitionInformation", null, null);
        }
        return miAcquInfo;
    }

    public static void fillInDataForNewInstrument(List<IMdeElement> dataList, InstrumentBlockData instrumentBlockData)
            throws Exception
    {
        for (IMdeElement element : dataList)
        {
            String key = element.getXmlElementName();
            try
            {
                if (element.getXmlElementName().equals("title"))
                {
                    createCharacterStringOrAnchor(element, instrumentBlockData.getTitle());
                }
                else if (element.getXmlElementName().equals("Date"))
                {
                    element.setElementValue(instrumentBlockData.getDate());
                }
                else if (element.getXmlElementName().equals("CI_DateTypeCode"))
                {
                    element.setAttributeValue("codeList", instrumentBlockData.getDtcCodeListAttr());
                    element.setAttributeValue("codeListValue", instrumentBlockData.getDtcCodeListValueAttr());
                    element.setElementValue(instrumentBlockData.getDtcLeafValue());
                }
                else if (element.getXmlElementName().equals("CI_Citation"))
                {
                    if (instrumentBlockData.getIdentifiers() != null)
                    {
                        for (Concept c : instrumentBlockData.getIdentifiers())
                        {
                            IMdeElement identifier = element.createChild("identifier", null, null);
                            IMdeElement mdIdentifier = identifier.createChild("mdIdentifier", "MD_Identifier", null);
                            IMdeElement code = mdIdentifier.createChild("code", null, null);
                            createCharacterStringOrAnchor(code, c);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void fillInNewPlatform(List<IMdeElement> dataList, PlatformBlockData platformBlockData)
            throws Exception
    {
        for (IMdeElement element : dataList)
        {
            if (element.getXmlElementName().equals("code"))
            {
                Concept c = platformBlockData.getPlatformIdentifier();
                try
                {
                    createCharacterStringOrAnchor(element, c);
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            else if (element.getXmlElementName().equals("description"))
            {
                try
                {
                    Concept c = new Concept(platformBlockData.getDescription(), "");
                    createCharacterStringOrAnchor(element, c);
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }


}
