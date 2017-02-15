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

package tree;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.primefaces.model.TreeNode;

import esa.obeos.metadataeditor.model.api.IMdeElement;
import global.GUIrefs;
import global.Master;
import utilities.StringUtilities;

@ManagedBean
@SessionScoped
public class ElementProcessingBean implements Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = 3590896378345944036L;

    private String infoContentsType = "";

    private SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    TreeNode selectedNode;
    IMdeElement element;

    MultipleFileEditingBean multipleFileEditor;

    Map<String, IMdeElement> uniqueChildren;
    Map<String, java.util.List<?>> listChildren;

    public void reset()
    {

        selectedNode = null;
        infoContentsType = "";
        dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        uniqueChildren = null;
        listChildren = null;
    }

    public void processSelectedElement(ValueHandlerBean valueHandler)
    {

        if (element.isValue())
        {
            valueHandler.childRecords.clear();
            try
            {
                processLeaf(valueHandler);
            }
            catch (Exception e)
            {
                if (Master.DEBUG_LEVEL > Master.LOW)
                    System.out.println(e.getLocalizedMessage());
            }
        }
        else
        { // not a leaf node
            valueHandler.setNotGoodForLookup(true);
            valueHandler.leafContentsType = "";
            listOfChildRecords(element, valueHandler);
        }
        populateAttributeMap(valueHandler.attributeMap);
    }

    void processLeaf(ValueHandlerBean valueHandler) throws Exception
    {

        String output = "ElementProcessingBean.processLeaf  ";

        if (Master.DEBUG_LEVEL > Master.LOW)
            System.out.println("ElementProcessingBean.findNodeType; element.getClass() =  " + element.getClass());

        if (null != element)
        {
            if (element.getXmlElementName().equals("Anchor") && element.getAttributeValue("href") != null)
            {
                valueHandler.setNotGoodForLookup(false);
            }
            else
            {
                valueHandler.setNotGoodForLookup(true);
            }
            GUIrefs.updateComponent("nodecontentsform");
            this.infoContentsType = "XML Element Type: " + element.getXmlElementType();

            if (element.isValue())
            {
                Class<?> clazz = element.getElementValueType();

                if (null != clazz)
                {

                    valueHandler.leafContentsType = "String";


                    if (Master.DEBUG_LEVEL > Master.LOW)
                        System.out.println("clazz.getSimpleName(): " + clazz.getSimpleName());

                    Object value = element.getElementValue();
                    if (String.class == clazz)
                    {
                        if (null != value)
                        {
                            valueHandler.selectedTextValue = value.toString();
                        }
                        else
                        {
                            valueHandler.selectedTextValue = "(empty)";
                        }
                        valueHandler.leafContentsType = "String";
                    }

                    if (Duration.class == clazz)
                    {
                        if (null != value)
                        {
                            valueHandler.selectedTextValue = value.toString();
                        }
                        else
                        {
                            try
                            {
                                Duration duration = DatatypeFactory.newInstance().newDuration(0);
                                valueHandler.selectedTextValue = duration.toString();
                            }
                            catch (DatatypeConfigurationException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                    else if (XMLGregorianCalendar.class.isAssignableFrom(clazz))
                    {
                        valueHandler.leafContentsType = "date";
                        Date date = null;
                        try
                        {
                            date = dt.parse(value.toString());
                        }
                        catch (ParseException e)
                        {
                            date = new Date();
                        }
                        valueHandler.selectedDateValue = date;
                    }
                    else if (clazz.isEnum())
                    {
                        valueHandler.leafContentsType = "enum";
                        valueHandler.enumConstants.clear();
                        Enum<?> enumValue = (Enum<?>) value;
                        for (Object obj : clazz.getEnumConstants())
                        {
                            if (obj == enumValue)
                            {
                                valueHandler.selectedEnumConstant = obj.toString();
                            }
                            Enum<?> enumConst = (Enum<?>) obj;
                            valueHandler.enumConstants.add(enumConst.name());
                            // index++;
                        }
                        // this.enums.select(selectIndex);
                        // this.enums.setVisible(true);
                    }
                    else
                    {

                        System.out
                                .println("not a proper String nor a Date nor an Enum, so set leafContentsType=String");
                        valueHandler.leafContentsType = "String";
                        if (null != value)
                        {
                            String strValue = "";
                            if (value instanceof IMdeElement)
                            {
                                Field f = clazz.getDeclaredField("value");
                                if (null != f)
                                {
                                    f.setAccessible(true);
                                    Object obj = f.get(value);
                                    f.setAccessible(false);

                                    if (null != obj)
                                    {
                                        strValue = obj.toString();
                                    }
                                }
                            }
                            else
                            {
                                strValue = value.toString();
                            }

                            valueHandler.selectedTextValue = strValue;
                        }
                        else
                        {
                            valueHandler.selectedTextValue = "";
                        }
                    }
                }
            }
            else // element is not a leaf node
            {
                valueHandler.leafContentsType = "";
            }
        }

        if (Master.DEBUG_LEVEL > Master.LOW)
            System.out.println("summary: " + output + " -> leafContentsType= " + valueHandler.leafContentsType);
    }

    public void populateAttributeMap(Map<String, String> attributeMap)
    {

        attributeMap.clear();
        Map<String, Object> attributes = null;
        try
        {
            attributes = element.getAttribs();
            if (attributes != null && attributes.size() > 0)
            {
                for (Entry<String, Object> attribute : attributes.entrySet())
                {
                    attributeMap.put(attribute.getKey(),
                            attributeDisplayName(element, attribute.getKey(), attribute.getValue()));
                }
            }
            else
            {
                attributeMap.clear();
            }
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    void setValue(IMdeElement element, ValueHandlerBean valueHandler)
    {


        if (Master.DEBUG_LEVEL > Master.LOW)
        {
            System.out.println("ElementProcessingBean.setValue; received parameter valueHandler");
            System.out.println("> > > > valueHandler.selectedTextValue: " + valueHandler.selectedTextValue);
            System.out.println("> > > > valueHandler.selectedDateValue: " + valueHandler.selectedDateValue);
            System.out.println("> > > > valueHandler.selectedEnumConstant: " + valueHandler.selectedEnumConstant);
        }

        boolean coeditLater = false;
        String stringValue = "";
        try
        {
            if (valueHandler.leafContentsType.equals("String"))
            {

                Class<?> clazz = element.getElementValueType();
                if (Duration.class == clazz)
                {

                    Duration duration = DatatypeFactory.newInstance().newDuration(valueHandler.selectedTextValue);
                    element.setElementValue(duration);
                    stringValue = duration.toString();

                }
                else
                {

                    setValue(valueHandler.selectedTextValue, clazz);

                    coeditLater = true;
                    // stringValue = valueHandler.selectedTextValue;
                }
            }

            else if (valueHandler.leafContentsType.equals("date"))
            {

                GregorianCalendar gregorianCalendar = new GregorianCalendar();

                gregorianCalendar.setTime(valueHandler.selectedDateValue);
                XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance()
                        .newXMLGregorianCalendar(gregorianCalendar);

                element.setElementValue(xmlGregorianCalendar);
                stringValue = xmlGregorianCalendar.toString();
            }

            else if (valueHandler.leafContentsType.equals("enum"))
            {

                Class<?> clazz = element.getElementValueType();

                Enum<?> enumValue = null;
                for (Object obj : clazz.getEnumConstants())
                {
                    Enum<?> enumConst = (Enum<?>) obj;
                    if (valueHandler.selectedEnumConstant.equals(enumConst.name()))
                    {
                        enumValue = enumConst;
                        break;
                    }
                }

                if (null == enumValue)
                {
                    // throw new Exception("Enum<" + clazz.getName() + ">
                    // doesn't support value '" + strValue + "'" );
                    throw new Exception("Enum<" + clazz.getName() + "> doesn't support value '"
                            + valueHandler.selectedEnumConstant + "'");
                }
                element.setElementValue(enumValue);
                stringValue = enumValue.toString();
            }

            if ((!coeditLater) && multipleFileEditor.isCoeditSelection()
                    && multipleFileEditor.getSelectedFiles().size() > 1)
            {
                multipleFileEditor.coeditNodeValue(element.getPath(), stringValue);
            }

        }
        catch (Exception exc)
        {
        }
    }

    private void setValue(final String strValue, final Class<?> clazz) throws Exception
    {

        try
        {
            IMdeElement element = ((NodeContents) selectedNode.getData()).getElement();
            // Object value = element.createElementValue(clazz, strValue);
            element.setElementValue(strValue);
            if (multipleFileEditor.isCoeditSelection() && multipleFileEditor.getSelectedFiles().size() > 1)
            {
                multipleFileEditor.coeditNodeValue(element.getPath(), strValue);
            }
        }
        catch (NumberFormatException e)
        {
            throw new Exception("value '" + strValue + "' cannot be parsed to '" + clazz.getSimpleName() + "'");
        }
    }

    public void setAttribute(IMdeElement element, ValueHandlerBean valueHandler)
    {

        try
        {

            if (Master.DEBUG_LEVEL > Master.LOW)
                System.out.println("in fct ElementProcessingBean.setAttribute: attribValue.toString()="
                        + valueHandler.selectedAttributeValue);
            if (null != valueHandler.selectedAttributeName)
            {
                Class<?> clazz = element.getAttributeValueType(valueHandler.selectedAttributeName);
                if (String.class == clazz)
                {
                    if ("NULL".equals(valueHandler.selectedAttributeValue))
                    {
                        valueHandler.selectedAttributeValue = "";
                    }
                    element.setAttributeValue(valueHandler.selectedAttributeName, valueHandler.selectedAttributeValue);
                    if (multipleFileEditor.isCoeditSelection() && multipleFileEditor.getSelectedFiles().size() > 1)
                    {
                        multipleFileEditor.coeditAttributeValue(element, valueHandler.selectedAttributeName,
                                valueHandler.selectedAttributeValue);
                    }
                }
                else
                {
                    String msg = "unsupported attribute type '" + clazz.getSimpleName() + "'";
                    GUIrefs.displayAlert(StringUtilities.escapeQuotes(msg));
                    throw new Exception(msg);
                }

            }
        }
        catch (Exception exc)
        {

            if (Master.DEBUG_LEVEL > Master.LOW)
            {
                System.out.println("ElementProcessingBean: Error on trying to set attribute");
                System.out.println(exc.getLocalizedMessage());
            }
            GUIrefs.displayAlert(StringUtilities.escapeQuotes(exc.getMessage()));
        }
        populateAttributeMap(valueHandler.attributeMap);
    }

    private void processElement(IMdeElement element, String name, Object obj, List<String> list, String optionalAppend)
    {

        StringBuilder itemText = new StringBuilder(name);

        try
        {
            if (element.isChildRequired(name))
            {
                itemText.append(" (REQUIRED)");
            }
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (obj instanceof String)
        {
            itemText.append("(" + obj.toString() + ")");
        }

        if (null == obj)
        {
            itemText.append(" (NULL)");
        }

        itemText.append(optionalAppend);

        list.add(itemText.toString());
    }

    private DataTableEntry generateChildRecord(IMdeElement parent, String fieldname, Object object) throws Exception
    {

        DataTableEntry childRecord = new DataTableEntry();
        childRecord.required = parent.isChildRequired(fieldname);

        if (object instanceof IMdeElement)
        {

            IMdeElement child = (IMdeElement) object;

            try
            {
                childRecord.elementName = child.getXmlElementName();
                childRecord.singleOccurrence = true;
                childRecord.exists = (null != object);// true
                childRecord.creatable = (null == object); // false
                childRecord.deletable = (!parent.isChildRequired(fieldname) && childRecord.exists);
                // childRecord.xpath to be implemented
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else if (object instanceof List<?>)
        {
            List<?> childlist = (List<?>) object;
            if (!childlist.isEmpty())
            {
                if (childlist.get(0) instanceof IMdeElement)
                {
                    IMdeElement child = (IMdeElement) childlist.get(0);
                    childRecord.elementName = child.getXmlElementName();
                    childRecord.singleOccurrence = false;
                    childRecord.exists = true;
                    childRecord.creatable = true; // assume there is no upper
                                                  // bound
                    childRecord.deletable = (!parent.isChildRequired(fieldname) || childlist.size() > 1);
                }
                else if (childlist.get(0) instanceof JAXBElement<?>)
                {
                    JAXBElement<?> jaxbelt = (JAXBElement<?>) childlist.get(0);
                    if (jaxbelt.getValue() instanceof IMdeElement)
                    {
                        IMdeElement child = (IMdeElement) jaxbelt.getValue();

                        try
                        {
                            childRecord.elementName = child.getXmlElementName();
                            childRecord.singleOccurrence = false;
                            childRecord.exists = true;
                            childRecord.creatable = true;
                            childRecord.deletable = (!parent.isChildRequired(fieldname) || childlist.size() > 1);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        else if (object == null)
        {
            childRecord.elementName = fieldname;
            childRecord.singleOccurrence = parent.getChildElements().containsKey(fieldname);
            childRecord.exists = false;
            childRecord.creatable = true;
            childRecord.deletable = false;
        }
        else
        {
            String type = object.getClass().getSimpleName();
            GUIrefs.displayAlert("Element" + type + " is not supported");
        }

        return childRecord;

    }

    public void updateChildRecord(DataTableEntry childRecord, IMdeElement element, String childName, Object obj,
            boolean singleOccurrence)
    {

        try
        {
            if (singleOccurrence)
            {// child from map element.getChildElements
                IMdeElement modified = element.getChildElements().get(childName);
                childRecord.creatable = (null == modified);
                childRecord.required = element.isChildRequired(childName);
                childRecord.exists = (null != obj);
                childRecord.elementName = childName;
                childRecord.singleOccurrence = singleOccurrence;
            }
            // childRecord.xpath to be implemented
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // TODO: don't need to pass complete valueHandler, childRecords-list is
    // enough
    public void listOfChildRecords(IMdeElement element, ValueHandlerBean valueHandler)
    {

        valueHandler.childRecords.clear();
        try
        {
            for (Entry<String, IMdeElement> child : element.getChildElements().entrySet())
            {
                DataTableEntry entry = generateChildRecord(element, child.getKey(), child.getValue());
                valueHandler.childRecords.add(entry);
            }
            for (Entry<String, java.util.List<?>> child : element.getChildListElements().entrySet())
            {

                DataTableEntry entry = generateChildRecord(element, child.getKey(), child.getValue());

                entry.deletable = (null == child.getValue()) ? false
                        : (!element.isChildRequired(child.getKey()) || child.getValue().size() > 1);

                entry.creatable = true; // TODO: replace by entry.creatable =
                                        // (#Occurrences <
                                        // element.maxOccurrences());
                valueHandler.childRecords.add(entry);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    String attributeDisplayName(IMdeElement element, String name, Object obj)
    {

        StringBuilder itemText = new StringBuilder(name);
        try
        {
            if (element.isAttributeRequired(name))
            {
                itemText.append(" (REQUIRED)");
            }
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (null == obj)
        {
            itemText.append(" (NULL)");
        }
        else
        {
            itemText.append(" ( " + obj.toString() + ")");
        }
        return itemText.toString();
    }

    public TreeNode getSelectedNode()
    {

        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode)
    {

        this.selectedNode = selectedNode;
    }

    public IMdeElement getElement()
    {

        return element;
    }

    public void setElement(IMdeElement element)
    {

        this.element = element;
    }

    public MultipleFileEditingBean getMultipleFileEditor()
    {

        return multipleFileEditor;
    }

    public void setMultipleFileEditor(MultipleFileEditingBean multipleFileEditor)
    {

        this.multipleFileEditor = multipleFileEditor;
    }

    public String getInfoContentsType()
    {

        return infoContentsType;
    }

    public void setInfoContentsType(String infoContentsType)
    {

        this.infoContentsType = infoContentsType;
    }

    public SimpleDateFormat getDt()
    {

        return dt;
    }

    public void setDt(SimpleDateFormat dt)
    {

        this.dt = dt;
    }

    // debugging
    void printList(List<String> list)
    {

        for (String a : list)
        {
            System.out.println(a);
        }
    }

//    public void testMethod()
//    {
//
//        System.out.println("**************************************");
//        System.out.println("ElementProcessingBean.testMethod()");
//        System.out.println("**************************************");
//    }

}
