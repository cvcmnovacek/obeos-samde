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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;

import org.primefaces.context.RequestContext;
import org.primefaces.model.TreeNode;

import esa.obeos.metadataeditor.model.api.IMdeElement;
import esa.obeos.metadataeditor.model.api.exc.AmbiguityException;
import global.GUIrefs;
import global.Master;

@ManagedBean
@SessionScoped
public class ElementSelectionBean implements Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = -1091194268170414315L;
    private List<String> possibleElements;// = new ArrayList<String>();
    private String selectedElement;

    // Set<String> options;
    private String childname;
    private IMdeElement parent;
    private PFTreeBuilderFromModel builder;
    private TreeNode newlyCreatedNode = null;

    // non-injected bean to which only a reference is obtained if needed
    private MultipleFileEditingBean multipleFileEditor = null;

    private boolean choiceSubmitted = false;

    public ElementSelectionBean()
    {
        possibleElements = new ArrayList<String>();
    }

    public void reset()
    {
        possibleElements.clear();
        selectedElement = "";
        childname = "";
        parent = null;
        builder = null;
        newlyCreatedNode = null;
        multipleFileEditor = null;
        choiceSubmitted = false;
    }

    public void showOptions(Set<String> options)
    {

        if (Master.DEBUG_LEVEL > Master.LOW)
            System.out.println("invoked ElementSelectionBean.showOptions(<options>)");
        printSet(options);
        elementListFromOptions(options);
        selectedElement = options.iterator().next();

        // without Dialog Framework
        RequestContext.getCurrentInstance().update(GUIrefs.getClientId("eltlist"));
        RequestContext.getCurrentInstance().execute("PF('selectionDlg').show()");

    }

    public void prepareForChildSelection(String childname, Set<String> options, IMdeElement parent,
            PFTreeBuilderFromModel builder)
    {
        this.childname = childname;
        this.parent = parent;
        this.builder = builder;
        showOptions(options);
    }

    public void prepareForCocreation(MultipleFileEditingBean multiEditor)
    {
        multipleFileEditor = multiEditor;
    }

    public void completeChildCreation()
    {
        try
        {
            IMdeElement newChild = parent.createChild(childname, selectedElement, builder);
            this.newlyCreatedNode = builder.getTreeParent();
            TreeBean.scrollToNewNode(builder);
            if (null != multipleFileEditor)
            {
                multipleFileEditor.cocreateChildren(newChild, this.parent, childname);
            }

        }
        catch (AmbiguityException e)
        {
        }
        catch (Exception e)
        {
            if (Master.DEBUG_LEVEL > Master.LOW)
                e.printStackTrace();
        }
    }

    public void onSelect(AjaxBehaviorEvent e) throws Exception
    {

        if (Master.DEBUG_LEVEL > Master.LOW)
        {
            System.out.println(e);
            System.out.println("selectedElement: " + selectedElement);
        }
    }

    private void elementListFromOptions(Set<String> options)
    {

        possibleElements.clear();
        selectedElement = "";
        for (String s : options)
        {
            possibleElements.add(s);
        }
    }

    public void submitChoice()
    {
        choiceSubmitted = true;
    }

    // public void propagateSelection() {
    // elementSelected = true;
    // }

    public List<String> listFromSet(Set<String> options)
    {
        List<String> ret = new ArrayList<String>();
        for (String s : options)
        {
            ret.add(s);
        }
        return ret;
    }

    // setters and getters
    public String getSelectedElement()
    {
        return selectedElement;
    }

    public void setSelectedElement(String selectedElement)
    {
        // Master.dataStore.setNewChildName(selectedElement);
        this.selectedElement = selectedElement;
    }

    public List<String> getPossibleElements()
    {
        return possibleElements;
    }

    public void setPossibleElements(List<String> possibleElements)
    {
        this.possibleElements = possibleElements;
    }

    public boolean isChoiceSubmitted()
    {
        return choiceSubmitted;
    }

    public void setChoiceSubmitted(boolean choiceSubmitted)
    {
        this.choiceSubmitted = choiceSubmitted;
    }

    // public boolean isElementSelected() {
    // return elementSelected;
    // }
    //
    // public void setElementSelected(boolean elementSelected) {
    // this.elementSelected = elementSelected;
    // }

    // debugging
    public void testShow()
    {
        RequestContext.getCurrentInstance().execute("alert('This onload script is added from backing bean.')");
        RequestContext.getCurrentInstance().execute("PF('selectionDlg').show()");
        System.out.println("ElementSelectionBean.testShow()");
        // RequestContext.getCurrentInstance().openDialog("choiceDialog");
        RequestContext.getCurrentInstance().openDialog("VerySimpleDialog");
    }

    public void dummy()
    {
        System.out.println("ElementSelectionBean.dummy()");
        System.out.println("Currently selected: " + this.selectedElement);
    }

    public void printSet(Set<String> set)
    {
        for (String s : set)
        {
            System.out.println(s);
        }
    }
}
