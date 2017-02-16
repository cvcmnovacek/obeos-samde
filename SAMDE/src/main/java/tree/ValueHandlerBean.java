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

package tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import org.primefaces.context.RequestContext;

import global.GUIrefs;
import global.Master;

@ManagedBean
@SessionScoped
public class ValueHandlerBean implements Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    String selectedTreeNodeName = "";


    String selectedTextValue = "(empty)";
    Date selectedDateValue;
    // Enum selectedEnumValue;
    String selectedEnumConstant = "";
    List<String> enumConstants = new ArrayList<String>();
    String attributeType = "";
    String selectedAttributeName = "";
    String selectedAttributeValue = "";
    String leafContentsType = "";
    List<String> childNames = new ArrayList<String>();// finally remove!
    List<DataTableEntry> childRecords = new ArrayList<DataTableEntry>();
    Map<String, String> attributeMap = new LinkedHashMap<String, String>();

    DataTableEntry selectedChildRecord;

    //
    // String testString = "This is a string from ValueHandlerBean";
    private boolean notGoodForLookup = true;


    // public ValueHandlerBean(){
    // System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
    // System.out.println("Constructor of ValueHandlerBean!");
    // System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
    // }

    @SuppressWarnings("unused")
    public void resetAllValues()
    {

        selectedTextValue = "(empty)";
        selectedEnumConstant = "";
        if (enumConstants == null)
        {
            System.out.println("ValueHandlerBean.resetAllValues(): enumConstants=null");
        }
        enumConstants.clear();
        selectedDateValue = null;
        selectedAttributeName = "";
        selectedAttributeValue = "";
        leafContentsType = "";
        selectedTreeNodeName = "";
        if (childNames == null)
        {
            if (Master.DEBUG_LEVEL > Master.LOW)
            {
                System.out.println("ValueHandlerBean.resetAllValues(): childNames=null");
            }
        }
        else
        {
            childNames.clear();
        }
        if (attributeMap == null)
        {
            if (Master.DEBUG_LEVEL > Master.LOW)
            {
                System.out.println("ValueHandlerBean.resetAllValues(): attributeMap=null");
            }
        }
        else
        {
            attributeMap.clear();
        }
        if (childRecords != null)
        {
            childRecords.clear();
        }
    }

    // setters and getters


    public String getSelectedTextValue()
    {
        return selectedTextValue;
    }

    public void setSelectedTextValue(String selectedTextValue)
    {
        this.selectedTextValue = selectedTextValue;
    }

    public Date getSelectedDateValue()
    {
        return selectedDateValue;
    }

    public void setSelectedDateValue(Date selectedDateValue)
    {
        this.selectedDateValue = selectedDateValue;
    }

    public String getSelectedEnumConstant()
    {
        return selectedEnumConstant;
    }

    public void setSelectedEnumConstant(String selectedEnumConstant)
    {
        this.selectedEnumConstant = selectedEnumConstant;
    }

    public List<String> getEnumConstants()
    {
        return enumConstants;
    }

    public void setEnumConstants(List<String> enumConstants)
    {
        this.enumConstants = enumConstants;
    }

    public String getLeafContentsType()
    {
        return leafContentsType;
    }

    public void setLeafContentsType(String leafContentsType)
    {
        this.leafContentsType = leafContentsType;
    }

    public List<String> getChildNames()
    {
        return childNames;
    }

    public void setChildNames(List<String> childNames)
    {
        this.childNames = childNames;
    }

    public String getSelectedAttributeName()
    {
        return selectedAttributeName;
    }

    public void setSelectedAttributeName(String selectedAttributeName)
    {
        this.selectedAttributeName = selectedAttributeName;
    }

    public String getSelectedAttributeValue()
    {
        return selectedAttributeValue;
    }

    public void setSelectedAttributeValue(String selectedAttributeValue)
    {
        this.selectedAttributeValue = selectedAttributeValue;
    }

    public boolean isNotGoodForLookup()
    {
        return notGoodForLookup;
    }

    public void setNotGoodForLookup(boolean notGoodForLookup)
    {
        this.notGoodForLookup = notGoodForLookup;
    }

    public Map<String, String> getAttributeMap()
    {
        return attributeMap;
    }

    public void setAttributeMap(Map<String, String> attributeMap)
    {
        this.attributeMap = attributeMap;
    }

    public DataTableEntry getSelectedChild()
    {
        return selectedChildRecord;
    }

    public void setSelectedChild(DataTableEntry selectedChild)
    {
        this.selectedChildRecord = selectedChild;
        // childlistCM.buildContextMenu(selectedChild);
        // RequestContext.getCurrentInstance().update(GUIrefs.getClientId(GUIrefs.childlistCM));
    }

    public void printSelectedChild()
    {
        System.out.println("********@@@@@@@@@@@@****VHB: " + selectedChildRecord);
    }

    public List<DataTableEntry> getChildRecords()
    {
        return childRecords;
    }

    public void setChildRecords(List<DataTableEntry> childRecords)
    {
        this.childRecords = childRecords;
    }

    public DataTableEntry getSelectedChildRecord()
    {
        return selectedChildRecord;
    }

    public void setSelectedChildRecord(DataTableEntry selectedChildRecord)
    {
        this.selectedChildRecord = selectedChildRecord;
    }

    // public String getTestString() {
    // return testString;
    // }
    // public void setTestString(String testString) {
    // this.testString = testString;
    // }
    public String getSelectedTreeNodeName()
    {
        return selectedTreeNodeName;
    }

    public void setSelectedTreeNodeName(String selectedTreeNodeName)
    {
        this.selectedTreeNodeName = selectedTreeNodeName;
    }

    // debugging
    public void testmethod()
    {
        System.out.println("################################");
        System.out.println("ValueHandlerBean.testmethod()");
        System.out.println("################################");
    }
}
