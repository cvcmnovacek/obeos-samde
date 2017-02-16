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

package esa.obeos.metadataeditor.model.impl;

import java.util.List;

import esa.obeos.metadataeditor.model.api.IMdeElement;
import esa.obeos.metadataeditor.model.api.ModelVersion;
import esa.obeos.metadataeditor.model.api.exc.InspireConstraintException;

public class InspireConstraints
{
    private static final String MI_Metadata = "MI_Metadata";

    private static final String CI_ResponsibleParty = "CI_ResponsibleParty";

    private static final String MD_DataIdentification = "MD_DataIdentification";

    private static final String CI_Citation = "CI_Citation";

    private static final String MD_Resolution = "MD_Resolution";

    private static final String DQ_DataQuality = "DQ_DataQuality";

    private static final String LI_Lineage = "LI_Lineage";

    private static final String MD_Constraints = "MD_Constraints";

    private static final String MD_LegalConstraints = "MD_LegalConstraints";


    public static void check(final IMdeElement element, final ModelVersion modelVersion) throws InspireConstraintException
    {
        if (null != element)
        {
            switch (modelVersion)
            {
            case Beta:

                if (element instanceof esa.obeos.metadataeditor.model.xsd.beta.gmd.CICitationType)
                {
                    checkCiCitation((esa.obeos.metadataeditor.model.xsd.beta.gmd.CICitationType)element);
                }
                else if (element instanceof esa.obeos.metadataeditor.model.xsd.beta.gmd.CIResponsiblePartyType)
                {
                    checkCiResponsibleParty((esa.obeos.metadataeditor.model.xsd.beta.gmd.CIResponsiblePartyType)element);
                }
                else if (element instanceof esa.obeos.metadataeditor.model.xsd.beta.gmd.MDResolutionType)
                {
                    checkMdResolution((esa.obeos.metadataeditor.model.xsd.beta.gmd.MDResolutionType)element);
                }
                else if (element instanceof esa.obeos.metadataeditor.model.xsd.beta.gmd.DQDataQualityType)
                {
                    checkDqDataQuality((esa.obeos.metadataeditor.model.xsd.beta.gmd.DQDataQualityType)element);
                }
                else if (element instanceof esa.obeos.metadataeditor.model.xsd.beta.gmd.LILineageType)
                {
                    checkLiLineage((esa.obeos.metadataeditor.model.xsd.beta.gmd.LILineageType)element);
                }
                else if (element instanceof esa.obeos.metadataeditor.model.xsd.beta.gmd.MDDataIdentificationType)
                {
                    checkMdDataIdentification((esa.obeos.metadataeditor.model.xsd.beta.gmd.MDDataIdentificationType)element);
                }
                else if (element instanceof esa.obeos.metadataeditor.model.xsd.beta.gmd.MDConstraintsType)
                {
                    checkMdConstraints((esa.obeos.metadataeditor.model.xsd.beta.gmd.MDConstraintsType)element);
                }
                else if (element instanceof esa.obeos.metadataeditor.model.xsd.beta.gmd.MDLegalConstraintsType)
                {
                    checkMdLegalConstraints((esa.obeos.metadataeditor.model.xsd.beta.gmd.MDLegalConstraintsType)element);
                }
                else if (element instanceof esa.obeos.metadataeditor.model.xsd.beta.gmi.MIMetadataType)
                {
                    checkMiMetadata((esa.obeos.metadataeditor.model.xsd.beta.gmi.MIMetadataType)element);
                }
                break;

            case Rel2015:

                if (element instanceof esa.obeos.metadataeditor.model.xsd.rel2015.gmd.CICitationType)
                {
                    checkCiCitation((esa.obeos.metadataeditor.model.xsd.rel2015.gmd.CICitationType)element);
                }
                else if (element instanceof esa.obeos.metadataeditor.model.xsd.rel2015.gmd.CIResponsiblePartyType)
                {
                    checkCiResponsibleParty((esa.obeos.metadataeditor.model.xsd.rel2015.gmd.CIResponsiblePartyType)element);
                }
                else if (element instanceof esa.obeos.metadataeditor.model.xsd.rel2015.gmd.MDResolutionType)
                {
                    checkMdResolution((esa.obeos.metadataeditor.model.xsd.rel2015.gmd.MDResolutionType)element);
                }
                else if (element instanceof esa.obeos.metadataeditor.model.xsd.rel2015.gmd.DQDataQualityType)
                {
                    checkDqDataQuality((esa.obeos.metadataeditor.model.xsd.rel2015.gmd.DQDataQualityType)element);
                }
                else if (element instanceof esa.obeos.metadataeditor.model.xsd.rel2015.gmd.LILineageType)
                {
                    checkLiLineage((esa.obeos.metadataeditor.model.xsd.rel2015.gmd.LILineageType)element);
                }
                else if (element instanceof esa.obeos.metadataeditor.model.xsd.rel2015.gmd.MDDataIdentificationType)
                {
                    checkMdDataIdentification((esa.obeos.metadataeditor.model.xsd.rel2015.gmd.MDDataIdentificationType)element);
                }
                else if (element instanceof esa.obeos.metadataeditor.model.xsd.rel2015.gmd.MDConstraintsType)
                {
                    checkMdConstraints((esa.obeos.metadataeditor.model.xsd.rel2015.gmd.MDConstraintsType)element);
                }
                else if (element instanceof esa.obeos.metadataeditor.model.xsd.rel2015.gmd.MDLegalConstraintsType)
                {
                    checkMdLegalConstraints((esa.obeos.metadataeditor.model.xsd.rel2015.gmd.MDLegalConstraintsType)element);
                }
                else if (element instanceof esa.obeos.metadataeditor.model.xsd.rel2015.gmi.MIMetadataType)
                {
                    checkMiMetadata((esa.obeos.metadataeditor.model.xsd.rel2015.gmi.MIMetadataType)element);
                }
                break;

            default:
                break;
            }
        }
    }

    private static void checkMiMetadata(final esa.obeos.metadataeditor.model.xsd.beta.gmi.MIMetadataType miMetadata) throws InspireConstraintException
    {
        checkField(miMetadata.getFileIdentifier(), "fileIdentifier", MI_Metadata);
        checkField(miMetadata.getLanguage(), "language", MI_Metadata);
        checkField(miMetadata.getCharacterSet(), "characterSet", MI_Metadata);
        checkField(miMetadata.getHierarchyLevel(), "hierarchyLevel", MI_Metadata);
        checkField(miMetadata.getHierarchyLevelName(), "hierarchyLevelName", MI_Metadata);
        checkField(miMetadata.getMetadataStandardName(), "metadataStandardName", MI_Metadata);
        checkField(miMetadata.getMetadataStandardVersion(), "metadataStandardVersion", MI_Metadata);
        checkField(miMetadata.getDataQualityInfo(), "dataQualityInfo", MI_Metadata);
    }

    private static void checkMiMetadata(final esa.obeos.metadataeditor.model.xsd.rel2015.gmi.MIMetadataType miMetadata) throws InspireConstraintException
    {
        checkField(miMetadata.getFileIdentifier(), "fileIdentifier", MI_Metadata);
        checkField(miMetadata.getLanguage(), "language", MI_Metadata);
        checkField(miMetadata.getCharacterSet(), "characterSet", MI_Metadata);
        checkField(miMetadata.getHierarchyLevel(), "hierarchyLevel", MI_Metadata);
        checkField(miMetadata.getHierarchyLevelName(), "hierarchyLevelName", MI_Metadata);
        checkField(miMetadata.getMetadataStandardName(), "metadataStandardName", MI_Metadata);
        checkField(miMetadata.getMetadataStandardVersion(), "metadataStandardVersion", MI_Metadata);
        checkField(miMetadata.getDataQualityInfo(), "dataQualityInfo", MI_Metadata);
    }

    private static void checkCiResponsibleParty(final esa.obeos.metadataeditor.model.xsd.beta.gmd.CIResponsiblePartyType ciResponsibleParty) throws InspireConstraintException
    {
        checkField(ciResponsibleParty.getIndividualName(), "individualName", CI_ResponsibleParty);
        checkField(ciResponsibleParty.getOrganisationName(), "organisationName", CI_ResponsibleParty);
        checkField(ciResponsibleParty.getPositionName(), "positionName", CI_ResponsibleParty);
        checkField(ciResponsibleParty.getContactInfo(), "contactInfo", CI_ResponsibleParty);
        checkField(ciResponsibleParty.getRole(), "role", CI_ResponsibleParty);
    }

    private static void checkCiResponsibleParty(final esa.obeos.metadataeditor.model.xsd.rel2015.gmd.CIResponsiblePartyType ciResponsibleParty) throws InspireConstraintException
    {
        checkField(ciResponsibleParty.getIndividualName(), "individualName", CI_ResponsibleParty);
        checkField(ciResponsibleParty.getOrganisationName(), "organisationName", CI_ResponsibleParty);
        checkField(ciResponsibleParty.getPositionName(), "positionName", CI_ResponsibleParty);
        checkField(ciResponsibleParty.getContactInfo(), "contactInfo", CI_ResponsibleParty);
        checkField(ciResponsibleParty.getRole(), "role", CI_ResponsibleParty);
    }

    private static void checkMdDataIdentification(final esa.obeos.metadataeditor.model.xsd.beta.gmd.MDDataIdentificationType mdDataIdentification) throws InspireConstraintException
    {
        checkField(mdDataIdentification.getPointOfContact(), "pointOfContact", MD_DataIdentification);
        checkField(mdDataIdentification.getGraphicOverview(), "graphicOverview", MD_DataIdentification);
        checkField(mdDataIdentification.getResourceConstraints(), "resourceConstraints", MD_DataIdentification);
        checkField(mdDataIdentification.getDescriptiveKeywords(), "descriptiveKeywords", MD_DataIdentification);
        checkField(mdDataIdentification.getCharacterSet(), "characterSet", MD_DataIdentification);
        checkField(mdDataIdentification.getTopicCategory(), "topicCategory", MD_DataIdentification);
        checkField(mdDataIdentification.getSpatialRepresentationType(), "spatialRepresentationType", MD_DataIdentification);
        checkField(mdDataIdentification.getSpatialResolution(), "spatialResolution", MD_DataIdentification);
        checkField(mdDataIdentification.getExtent(), "extent", MD_DataIdentification);
    }

    private static void checkMdDataIdentification(final esa.obeos.metadataeditor.model.xsd.rel2015.gmd.MDDataIdentificationType mdDataIdentification) throws InspireConstraintException
    {
        checkField(mdDataIdentification.getPointOfContact(), "pointOfContact", MD_DataIdentification);
        checkField(mdDataIdentification.getGraphicOverview(), "graphicOverview", MD_DataIdentification);
        checkField(mdDataIdentification.getResourceConstraints(), "resourceConstraints", MD_DataIdentification);
        checkField(mdDataIdentification.getDescriptiveKeywords(), "descriptiveKeywords", MD_DataIdentification);
        checkField(mdDataIdentification.getCharacterSet(), "characterSet", MD_DataIdentification);
        checkField(mdDataIdentification.getTopicCategory(), "topicCategory", MD_DataIdentification);
        checkField(mdDataIdentification.getSpatialRepresentationType(), "spatialRepresentationType", MD_DataIdentification);
        checkField(mdDataIdentification.getSpatialResolution(), "spatialResolution", MD_DataIdentification);
        checkField(mdDataIdentification.getExtent(), "extent", MD_DataIdentification);
    }

    private static void checkCiCitation(final esa.obeos.metadataeditor.model.xsd.beta.gmd.CICitationType ciCitation) throws InspireConstraintException
    {
        checkField(ciCitation.getIdentifier(), "identifier", CI_Citation);
    }

    private static void checkCiCitation(final esa.obeos.metadataeditor.model.xsd.rel2015.gmd.CICitationType ciCitation) throws InspireConstraintException
    {
        checkField(ciCitation.getIdentifier(), "identifier", CI_Citation);
    }

    private static void checkMdResolution(final esa.obeos.metadataeditor.model.xsd.beta.gmd.MDResolutionType mdResolution) throws InspireConstraintException
    {
        checkFieldType(mdResolution, esa.obeos.metadataeditor.model.xsd.beta.gco.DistancePropertyType.class, "distance", MD_Resolution);
    }

    private static void checkMdResolution(final esa.obeos.metadataeditor.model.xsd.rel2015.gmd.MDResolutionType mdResolution) throws InspireConstraintException
    {
        checkFieldType(mdResolution, esa.obeos.metadataeditor.model.xsd.beta.gco.DistancePropertyType.class, "distance", MD_Resolution);
    }

    private static void checkDqDataQuality(final esa.obeos.metadataeditor.model.xsd.beta.gmd.DQDataQualityType dqDataQuality) throws InspireConstraintException
    {
        checkField(dqDataQuality.getReport(), "report", DQ_DataQuality);
        checkField(dqDataQuality.getLineage(), "lineage", DQ_DataQuality);
    }

    private static void checkDqDataQuality(final esa.obeos.metadataeditor.model.xsd.rel2015.gmd.DQDataQualityType dqDataQuality) throws InspireConstraintException
    {
        checkField(dqDataQuality.getReport(), "report", DQ_DataQuality);
        checkField(dqDataQuality.getLineage(), "lineage", DQ_DataQuality);
    }

    private static void checkLiLineage(final esa.obeos.metadataeditor.model.xsd.beta.gmd.LILineageType liLineage) throws InspireConstraintException
    {
        checkField(liLineage.getStatement(), "statement", LI_Lineage);
    }

    private static void checkLiLineage(final esa.obeos.metadataeditor.model.xsd.rel2015.gmd.LILineageType liLineage) throws InspireConstraintException
    {
        checkField(liLineage.getStatement(), "statement", LI_Lineage);
    }

    private static void checkMdConstraints(final esa.obeos.metadataeditor.model.xsd.beta.gmd.MDConstraintsType mdConstraints) throws InspireConstraintException
    {
        checkField(mdConstraints.getUseLimitation(), "useLimitation", MD_Constraints);
    }

    private static void checkMdConstraints(final esa.obeos.metadataeditor.model.xsd.rel2015.gmd.MDConstraintsType mdConstraints) throws InspireConstraintException
    {
        checkField(mdConstraints.getUseLimitation(), "useLimitation", MD_Constraints);
    }

    private static void checkMdLegalConstraints(final esa.obeos.metadataeditor.model.xsd.beta.gmd.MDLegalConstraintsType mdLegalConstraints) throws InspireConstraintException
    {
        checkField(mdLegalConstraints.getAccessConstraints(), "accessConstraints", MD_LegalConstraints);
        checkField(mdLegalConstraints.getOtherConstraints(), "otherConstraints", MD_LegalConstraints);
        checkEqualityFieldsSizes(mdLegalConstraints.getAccessConstraints(), "accessConstraints", 
                mdLegalConstraints.getOtherConstraints(), "otherConstraints", MD_LegalConstraints);
    }

    private static void checkMdLegalConstraints(final esa.obeos.metadataeditor.model.xsd.rel2015.gmd.MDLegalConstraintsType mdLegalConstraints) throws InspireConstraintException
    {
        checkField(mdLegalConstraints.getAccessConstraints(), "accessConstraints", MD_LegalConstraints);
        checkField(mdLegalConstraints.getOtherConstraints(), "otherConstraints", MD_LegalConstraints);
        checkEqualityFieldsSizes(mdLegalConstraints.getAccessConstraints(), "accessConstraints", 
                mdLegalConstraints.getOtherConstraints(), "otherConstraints", MD_LegalConstraints);
    }

    private static void checkField(final Object fieldValue, final String fieldName, final String parentName)
    {
        if (null == fieldValue
                || (fieldValue instanceof List && ((List<?>)fieldValue).isEmpty()))
        {
            throw new InspireConstraintException("missing '" + fieldName +"' in '" + parentName + "'");
        }
    }

    private static void checkFieldType(final Object fieldValue, final Class<?> demandedClass, final String fieldName, final String parentName)
    {
        checkField(fieldValue, fieldName, parentName);
        Class<?> actualClass = fieldValue.getClass();
        if (actualClass.equals(demandedClass))
        {
            throw new InspireConstraintException("invalid type '" + actualClass.getName() 
                + "' of '" + fieldName + "' in '" + parentName + "', the type must be '" + demandedClass + "'");
        }
    }

    private static void isList(final Object fieldValue, final String fieldName, String parentName)
    {
        if (fieldValue instanceof List)
        {
            throw new InspireConstraintException("cannot check size of '" + fieldName +"' in '" + parentName + "', the field is not a list");
        }
    }

    private static void checkEqualityFieldsSizes(final Object field1Value, final String field1Name, final Object field2Value, final String field2Name, String parentName)
    {
        isList(field1Value, field1Name, parentName);
        isList(field2Value, field2Name, parentName);

        int size1 = ((List<?>)field1Value).size();
        int size2 = ((List<?>)field2Value).size();

        if (size1 != size2)
        {
            throw new InspireConstraintException("The field '" + field1Name +"' has size [" + size1 + "]" 
                    + " while the field '" + field2Name +"' has size [" + size2 + "] in + '"
                    + parentName + "', the fields' sizes must be the same");
        }
    }
}
