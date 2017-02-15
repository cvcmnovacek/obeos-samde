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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import esa.obeos.metadataeditor.jaxrs.client.MetadataEditorJaxRsClient;
import esa.obeos.metadataeditor.model.api.IMdeElement;
import global.GUIrefs;
import utilities.XmlFormatter;

@ManagedBean
@SessionScoped
public class BuildingBlockCollectionBean implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 4778422024819077754L;

    // <key,block contents>
    private Map<String, String> buildingBlocks = new HashMap<String, String>();

    // <elementType, listOfKeys>
    private Map<String, List<String>> blocksPerType = new HashMap<String, List<String>>();

    // <building, type>
    // private Map<String, String> lookupType = new HashMap<String, String>();

    private String currentBuildingBlock = "";
    private String selectedKey = "";
    private String currentName = "";
    private String key = "";
    private String selectedBlockFromAll = "";


    private IMdeElement selectedElement;
    private List<String> currentSuggestions;

    private boolean onlyMatchingOnes = true;

    private MetadataEditorJaxRsClient client;

    @PostConstruct
    public void init()
    {
        try
        {
            client = new MetadataEditorJaxRsClient("localhost", 8080, "/SAMDE-0.1.0/rest/mde");
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public void addBuildingBlock()
    {
        if (!currentName.equals("") && !currentBuildingBlock.equals(""))
        {
            buildingBlocks.put(currentName, currentBuildingBlock);
            

            try
            {
                String blockType = selectedElement.getXmlElementName();
                List<String> typelist = blocksPerType.get(blockType);
                if (null == typelist)
                {
                    typelist = new ArrayList<String>();
                    blocksPerType.put(blockType, typelist);
                }
                typelist.add(currentName);

                this.client.addBuildingBlock(currentName, blockType, currentBuildingBlock);
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            GUIrefs.updateComponent("downloadBBList");
        }
        else
        {
            GUIrefs.displayAlert("empty name or building block!");
        }
    }

    public void suggestBuildingBlocks()
    {
        if (null != selectedElement)
        {
            try
            {
                String type = selectedElement.getXmlElementName();
                currentSuggestions = blocksPerType.get(type);
            }
            catch(Exception e)
            {
                String msg = "";
                if (null != e.getMessage())
                {
                    msg = e.getMessage();
                }

                GUIrefs.displayAlert("selection failed " + msg);
            }
        }
        else
        {
            GUIrefs.displayAlert("selection not set in BuildingBlockCollectionBean");
        }
    }


    // setters and getters

    public Map<String, String> getBuildingBlocks()
    {
        return buildingBlocks;
    }

    public void setBuildingBlocks(Map<String, String> buildingBlocks)
    {
        this.buildingBlocks = buildingBlocks;
    }

    public String getCurrentBuildingBlock()
    {
        return currentBuildingBlock;
    }

    public void setCurrentBuildingBlock(String currentBuildingBlock)
    {
        this.currentBuildingBlock = currentBuildingBlock;
    }

    public String getCurrentBlockContents()
    {
        // String ret="";
        String currentBlockContents;
        if (this.onlyMatchingOnes)
        {
            // currentBlockContents =
            // this.buildingBlocks.get(currentBuildingBlock);
            currentBlockContents = this.buildingBlocks.get(selectedKey);
            // currentBlockContents = currentBuildingBlock;
        }
        else
        {
            currentBlockContents = this.selectedBlockFromAll;
        }

        return currentBlockContents;
    }

    public boolean checkTypeMatch()
    {
        boolean ret = true;
        if (!this.onlyMatchingOnes)
        {
            ret = false;
            
            try
            {
                String type = selectedElement.getXmlElementName();
                currentSuggestions = blocksPerType.get(type);

                List<String> blocksOfGivenType = this.blocksPerType.get(type);
                for (String b : blocksOfGivenType)
                {
                    if (selectedBlockFromAll.equals(this.buildingBlocks.get(b)))
                    {
                        ret = true;
                    }
                }
            }
            catch(Exception e)
            {
                String msg = "";
                if (null != e.getMessage())
                {
                    msg = e.getMessage();
                }

                GUIrefs.displayAlert("selection failed " + msg);
            }

            
        }
        return ret;
    }

    public List<String> getCurrentSuggestions()
    {
        return currentSuggestions;
    }

    public void setCurrentSuggestions(List<String> currentSuggestions)
    {
        this.currentSuggestions = currentSuggestions;
    }

    public String getCurrentName()
    {
        return currentName;
    }

    public void setCurrentName(String currentName)
    {
        this.currentName = currentName;
    }

    public String getKey()
    {

        return key;
    }

    public void setKey(String key)
    {

        this.key = key;
    }


    public boolean isOnlyMatchingOnes()
    {
        return onlyMatchingOnes;
    }

    public void setOnlyMatchingOnes(boolean onlyMatchingOnes)
    {
        this.onlyMatchingOnes = onlyMatchingOnes;
        GUIrefs.updateComponent("panelAll");
        GUIrefs.updateComponent("panelMatch");
        GUIrefs.updateComponent("previewPanel");
    }

    public IMdeElement getSelectedElement()
    {
        return selectedElement;
    }


    public void setSelectedElement(IMdeElement selectedElement)
    {
        this.selectedElement = selectedElement;
    }


    public String getSelectedKey()
    {
        return selectedKey;
    }

    public void setSelectedKey(String selectedKey)
    {
        this.selectedKey = selectedKey;
    }

    public int getNmbrOfBlocks()
    {
        return buildingBlocks.size();
    }

    public String getSelectedBlockFromAll()
    {
        return selectedBlockFromAll;
    }

    public void setSelectedBlockFromAll(String selectedBlockFromAll)
    {
        this.selectedBlockFromAll = selectedBlockFromAll;
    }


    // debugging

    public void printInfo()
    {
        String type = "unknown";
        
        try
        {
            type = selectedElement.getXmlElementName();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
       
        System.out.println("currently sel. elt: " + type);
        System.out.println("currentBuildingBlock: " + currentBuildingBlock);
        System.out.println("currentSuggestions: " + this.currentSuggestions);
        System.out.println("currentName: " + this.currentName);
        System.out.println("selectedKey: " + this.selectedKey);
        System.out.println("currentBuildingBlockContents: " + buildingBlocks.get(selectedKey));
        System.out.println("selectedBlockFromAll: " + this.selectedBlockFromAll);
    }

//    public void onlyMatchingTestmethod()
//    {
//        System.out.println("currentBuildingBlock: " + currentBuildingBlock);
//        System.out.println("currentSuggestions: " + this.currentSuggestions);
//        System.out.println("currentName: " + this.currentName);
//        System.out.println("selectedKey: " + this.selectedKey);
//        System.out.println("currentBuildingBlockContents: " + buildingBlocks.get(selectedKey));
//        System.out.println("selectedBlockFromAll: " + this.selectedBlockFromAll);
//
//    }

    public void printStatus()
    {
        System.out.println("show only matching ones: " + this.onlyMatchingOnes);
    }

    public void checkBoxChanged()
    {
        System.out.println("checkBoxChanged()");
    }

    public void submitted()
    {
        System.out.println("Submitted the following selection:");
        System.out.println(this.currentBuildingBlock);
    }
}
