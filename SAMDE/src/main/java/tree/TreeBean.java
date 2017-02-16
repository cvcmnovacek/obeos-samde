//------------------------------------------------------------------------------
//
// Project: OBEOS METADATA EDITOR
// Authors: Natascha Neumaerker, Siemens Convergence Creators, Prague (CZ)
//          Milan Novacek, Siemens Convergence Creators, Prague (CZ)
//          Radim Zajonc, Siemens Convergence Creators, Prague (CZ)
//          Stanislav Kascak, Siemens Convergence Creators
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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.context.RequestContext;
import org.primefaces.model.TreeNode;

import esa.obeos.metadataeditor.common.util.TimeUtil;
import esa.obeos.metadataeditor.model.api.IMdeElement;
import esa.obeos.metadataeditor.model.api.exc.AmbiguityException;
import filehandling.FileData;
import filehandling.FilePreprocessor;
import global.GUIrefs;
import global.Master;
import semanticannot.AnnotationInserterBean;

@ManagedBean
@SessionScoped
public class TreeBean implements Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = 514573044244874892L;
    private TreeNode root;
    private TreeNode selectedNode;

    Object obj;


    private PFTreeBuilderFromModel treeBuilder = new PFTreeBuilderFromModel();

    @ManagedProperty(value = "#{elementSelectionBean}")
    ElementSelectionBean selectionBean;

    @ManagedProperty(value = "#{elementProcessingBean}")
    ElementProcessingBean processingBean;

    @ManagedProperty(value = "#{multipleFileEditingBean}")
    private MultipleFileEditingBean multipleFileEditor;

    @ManagedProperty(value = "#{valueHandlerBean}")
    private ValueHandlerBean valueHandler;

    @ManagedProperty(value = "#{textViewBean}")
    private TextViewBean textViewBean;

    @ManagedProperty(value = "#{annotationInserterBean}")
    private AnnotationInserterBean annotationInserter;


    @PostConstruct
    public void init()
    {

        // reset();
        // this.setFileAndBuildTree();
        // System.out.println("TreeBean.setFileAndBuildTree: number of files: "
        // +
        // Master.documentsStore.collection.size());
        // GlobalParameters.validateOnBuild = true;
    }


    public void reset()
    {


        if (Master.DEBUG_LEVEL > Master.LOW)
            System.out.println("called TreeBean.reset()");
        root = null;
        selectedNode = null;

        StringBuilder sb = new StringBuilder();
        for (FileData fd : Master.documentsStore.collection.values())
        {
            if (sb.length() != 0)
            {
                sb.append(',');
            }
            sb.append(fd.getSourceUrl() != null ? fd.getSourceUrl() : fd.getDisplayname());
        }
        sb.insert(0, "fireBasicEvent(\'MDEclosedMD\' ,{urlOrFilename:\'[");
        sb.append("]\'});");

        String command = sb.toString();
        System.out.println(command);
        RequestContext.getCurrentInstance().execute(command);
        Master.documentsStore.reset();
        multipleFileEditor.reset();
        selectionBean.reset();
        processingBean.reset();
        textViewBean.reset();
        valueHandler.resetAllValues();
        annotationInserter.reset();

        GUIrefs.updateComponent("checkboxform");
        GUIrefs.updateComponent("mainPanelGrid");
        GUIrefs.updateComponent("tabview");
        GUIrefs.updateComponent("childdatalist");
        GUIrefs.updateComponent("annotationMainForm");
        redrawTree();

    }

    public void buildTree()
    {

        root = treeBuilder.treeFromModel();
        if (root != null)
        {
            initAnnotationsInserter();
            TreeNode visRoot = root.getChildren().get(0);
            visRoot.setSelected(true);
            visRoot.setExpanded(true);
            visRoot.setSelected(false);
            redrawTree();
        }
        else
        {
            GUIrefs.displayAlert("building of tree failed");
        }

    }

    public void initAnnotationsInserter()
    {
        // System.out.println("TreeBean.initAnnotationsInserter");
        TreeNode visibleRoot = this.root.getChildren().get(0);
        NodeContents rootCont = (NodeContents) visibleRoot.getData();
        annotationInserter.setVisibleRoot(visibleRoot);
        annotationInserter.setModelRoot(rootCont.getElement());
        annotationInserter.setTreeBuilder(treeBuilder);
        annotationInserter.setMultipleFileEditor(multipleFileEditor);
    }

    public void refresh()
    {

        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        try
        {
            ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
        }
        catch (IOException e)
        {
            GUIrefs.displayAlert("IOException in TreeBean refresh");
            e.printStackTrace();
        }
    }

    public void addChild() throws Exception
    {
        String childname = valueHandler.getSelectedChild().elementName;
        if (null != selectedNode)
        {
            IMdeElement parentElement = ((NodeContents) selectedNode.getData()).getElement();
            treeBuilder.setTreeParent(selectedNode);
            // RequestContext.getCurrentInstance().execute("saveScrollPos()");
            try
            {
                IMdeElement newChild = parentElement.createChild(childname, null, treeBuilder);

                // newly created node is new selection
                // TODO: put this into a function which is called from GUI once
                // e.g. in eltChoiceDialog/OK Button/action
                // addition is completed
                selectedNode.setExpanded(true);
                selectedNode = treeBuilder.getTreeParent();
                selectedNode.setSelected(true);
                selectedNode.setExpanded(true);
                if (multipleFileEditor.isCoeditSelection() && multipleFileEditor.getSelectedFiles().size() > 1)
                {
                    multipleFileEditor.cocreateChildren(newChild, parentElement, childname);
                }
            }
            catch (AmbiguityException e)
            {
                // from here on, selectionBean is controlled from the GUI to
                // handle the rest
                selectionBean.prepareForChildSelection(childname, e.getOptions(), parentElement, treeBuilder);
                if (multipleFileEditor.isCoeditSelection() && multipleFileEditor.getSelectedFiles().size() > 1)
                {
                    selectionBean.prepareForCocreation(multipleFileEditor);
                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            processingBean.listOfChildRecords(parentElement, valueHandler);
            RequestContext.getCurrentInstance().update(GUIrefs.getClientId(GUIrefs.childList));

            if (treeBuilder.getLastCreatedChild() != null)
            {
                /*
                 * Custom JS code to move scroll bar in tree view to display
                 * just added element. RequestContext.scrollTo() is not good in
                 * this case as it moves the whole page, not the view in the
                 * tree container.
                 */
                // String treeClientId = GUIrefs.getClientId(GUIrefs.tree);
                // String addedNodeClientId = treeClientId + ":" +
                // treeBuilder.getLastCreatedChild().getRowKey();
                // RequestContext.getCurrentInstance()
                // .execute("PrimeFaces.scrollInView("
                // + "$(PrimeFaces.escapeClientId('" + treeClientId + "')), "
                // + "$(PrimeFaces.escapeClientId('" + addedNodeClientId +
                // "')))");
                TreeBean.scrollToNewNode(treeBuilder);
            }
        }
        redrawTree();
    }

    public void deleteChild() throws Exception
    {

        String childname = valueHandler.selectedChildRecord.elementName;
        NodeContents nodeContents = (NodeContents) selectedNode.getData();
        List<TreeNode> children = selectedNode.getChildren();
        IMdeElement childToDie = null;
        for (TreeNode n : children)
        {
            NodeContents c = (NodeContents) n.getData();
            if (c.getFieldname() == childname)
            {
                if (c.getElement() instanceof IMdeElement)
                {
                    childToDie = (IMdeElement) c.getElement();
                    nodeContents.getElement().deleteChild(childToDie);
                    if (multipleFileEditor.isCoeditSelection() && multipleFileEditor.getSelectedFiles().size() > 1)
                    {
                        multipleFileEditor.codeleteChildren(childToDie, nodeContents.getElement());
                    }
                }
                else
                {
                    throw new Exception("a parent tree item doesn't contain data 'IMdeElement'");
                }
                removeNodeFromTree(n);
                TreeBean.scrollToTreeNode(selectedNode);

                processingBean.listOfChildRecords(nodeContents.getElement(), valueHandler);
                RequestContext.getCurrentInstance().update(GUIrefs.getClientId(GUIrefs.childList));
                redrawTree();
                break;
            }
        }
    }

    public static void removeNodeFromTree(TreeNode n)
    {

        n.getChildren().clear();
        n.getParent().getChildren().remove(n);
        n.setParent(null);
        n = null;
    }

    public void redrawTree()
    {

        String result = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
                .get(GUIrefs.tree);
        // RequestContext.getCurrentInstance().update(GUIrefs.tree);
        // RequestContext.getCurrentInstance().update("_SAMDE_WAR_SAMDEesaobeosportlet_:treeform");
        RequestContext.getCurrentInstance().update(GUIrefs.getClientId("treeform"));
    }

    // before attempting to delete checks if node is deletable
    public void onCheckedDelete() throws Exception
    {

        NodeContents elementContents = (NodeContents) selectedNode.getData();
        NodeContents parentContents = (NodeContents) selectedNode.getParent().getData();
        IMdeElement elt = (IMdeElement) elementContents.getElement();

        if (!elt.isRequired() || elt.hasMultipleOccurrences())
        {
            // deletion of data in tree:
            if (parentContents.getElement() instanceof IMdeElement)
            {
                IMdeElement parent = (IMdeElement) parentContents.getElement();
                if (multipleFileEditor.isCoeditSelection() && multipleFileEditor.getSelectedFiles().size() > 1)
                {
                    multipleFileEditor.codeleteChildren(elementContents.getElement(), parent);
                }
                parent.deleteChild(elementContents.getElement());
                TreeNode parentNode = selectedNode.getParent();
                removeNodeFromTree(selectedNode);

                TreeBean.scrollToTreeNode(parentNode);
                selectedNode = parentNode;
                selectedNode.setSelected(true);

                // selectedNode = root.getChildren().get(0);
                processingBean.listOfChildRecords(parentContents.getElement(), valueHandler);
                RequestContext.getCurrentInstance().update(GUIrefs.getClientId(GUIrefs.tree));
                RequestContext.getCurrentInstance().update(GUIrefs.getClientId(GUIrefs.childList));
            }
            else
            {
                throw new Exception("a parent tree item doesn't contain data 'IMdeElement'");
            }
        }
        else
        {
            String errMsg = "Element " + elt.getXmlElementName() + " cannot be deleted!";
            RequestContext.getCurrentInstance().execute("alert(\"" + errMsg + "\")");
        }
    }

    public void onNodeSelect()
    {
        try
        {


            valueHandler.resetAllValues();

            NodeContents c = (NodeContents) selectedNode.getData();// NullPointerException
                                                                   // (?)
            IMdeElement element = c.getElement();
            valueHandler.setSelectedTreeNodeName(element.getXmlElementName());

            // contextMenuBean.buildContextMenu(element);

            processingBean.setElement(element);

            processingBean.setSelectedNode(this.selectedNode);
            processingBean.processSelectedElement(this.valueHandler);
            processingBean.setMultipleFileEditor(multipleFileEditor);

            textViewBean.setSelectedNode(selectedNode);
            textViewBean.setTreeBuilder(treeBuilder);
            textViewBean.setMultipleFileEditor(multipleFileEditor);

            RequestContext.getCurrentInstance().update(GUIrefs.getClientId(GUIrefs.attributeList));
            RequestContext.getCurrentInstance().update(GUIrefs.getClientId(GUIrefs.childList));
            RequestContext.getCurrentInstance().update(GUIrefs.getClientId(GUIrefs.fragmentTextPanel));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void showAttribute()
    {

        IMdeElement element = ((NodeContents) selectedNode.getData()).getElement();
        try
        {
            Object value = element.getAttribs().get(valueHandler.selectedAttributeName);
            if (null == value)
            {
                valueHandler.selectedAttributeValue = "null";

                if (Master.DEBUG_LEVEL > Master.LOW)
                    System.out.println("selectedAttributeValue=" + valueHandler.selectedAttributeValue);
            }
            else
            {
                Class<?> clazz = value.getClass();
                if (String.class == clazz)
                {
                    valueHandler.selectedAttributeValue = (String) value;
                    if (Master.DEBUG_LEVEL > Master.LOW)
                        System.out.println("selectedAttributeValue=" + valueHandler.selectedAttributeValue);
                }
                else
                {
                    System.out.println("attribute value is not a string -> not implemented");
                    // not implemented yet
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        // RequestContext.getCurrentInstance().update(GUIrefs.attributeLabel);
        RequestContext.getCurrentInstance().update(GUIrefs.getClientId(GUIrefs.attributeLabel));
    }

    public void onSetValue()
    {
        //System.out.println("TreeBean.onSetValue()");
        IMdeElement element = ((NodeContents) selectedNode.getData()).getElement();
        processingBean.setValue(element, this.valueHandler);

    }

    public void onSetAttribute()
    {
        //System.out.println( "TreeBean.onSetAttribute() -- this.selectedAttributeName=" + valueHandler.selectedAttributeName);
        //System.out.println( "TreeBean.onSetAttribute() -- this.selectedAttributeValue=" + valueHandler.selectedAttributeValue);
        IMdeElement element = ((NodeContents) selectedNode.getData()).getElement();

        processingBean.setAttribute(element, valueHandler);
    }

    public void setDateStamp()
    {

        IMdeElement root = Master.documentsStore.getDisplayFile().getModelRoot();
        try
        {

            Map<String, IMdeElement> childmap = root.getChildElements();
            IMdeElement dateStampElt = childmap.get("dateStamp");
            TreeNode visibleRoot = this.root.getChildren().get(0);

            if (null == dateStampElt)
            {
                treeBuilder.setTreeParent(visibleRoot);
                dateStampElt = root.createChild("dateStamp", null, treeBuilder);

            }
            IMdeElement dateTimeElt = dateStampElt.getChildByXmlName("DateTime");
            if (null == dateTimeElt)
            {
                dateTimeElt = dateStampElt.getChildByXmlName("Date");
            }
            if (null == dateTimeElt)
            {
                dateTimeElt = dateStampElt.createChild("dateOrDateTime", "DateTime", treeBuilder);
                // selectedNode = treeBuilder.getLastCreatedChild();
            }

            if (dateTimeElt.getXmlElementName().equals("DateTime"))
            {
                dateTimeElt.setElementValue(TimeUtil.convert(new Date()));
            }
            else
            {
                Calendar c = Calendar.getInstance();
                Date d = c.getTime();
                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");

                dateTimeElt.setElementValue(f.format(d));
                // c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) +
                // "-" + c.get(Calendar.DAY_OF_MONTH));
            }
            selectedNode = getDateTimeNode();

            if (multipleFileEditor.isCoeditSelection())
            {
                multipleFileEditor.coSetDateStamp();
            }

        }
        catch (Exception e)
        {
            // GUIrefs.displayAlert(e.getMessage());
            Master.errorBean.addMessage(Master.documentsStore.getDisplayFile().getDisplayname(), e.getMessage());
            e.printStackTrace();
        }
        Master.errorBean.handleErrors();
    }

    public static void scrollToNewNode(PFTreeBuilderFromModel treeBuilder)
    {
        /*
         * Custom JS code to move scroll bar in tree view to display just added
         * element. RequestContext.scrollTo() is not good in this case as it
         * moves the whole page, not the view in the tree container.
         */
        String treeClientId = GUIrefs.getClientId(GUIrefs.tree);
        String addedNodeClientId = treeClientId + ":" + treeBuilder.getLastCreatedChild().getRowKey();
        RequestContext.getCurrentInstance().execute("PrimeFaces.scrollInView(" + "$(PrimeFaces.escapeClientId('"
                + treeClientId + "')), " + "$(PrimeFaces.escapeClientId('" + addedNodeClientId + "')))");
    }

    public static void scrollToTreeNode(TreeNode treeNode)
    {
        String treeClientId = GUIrefs.getClientId(GUIrefs.tree);
        String nodeId = treeClientId + ":" + treeNode.getRowKey();
        RequestContext.getCurrentInstance().execute("PrimeFaces.scrollInView(" + "$(PrimeFaces.escapeClientId('"
                + treeClientId + "')), " + "$(PrimeFaces.escapeClientId('" + nodeId + "')))");

    }

    public void expandCurrentSubtree()
    {
        // TreeBean.expandSubtree(selectedNode);
        TreeBean.collapseOrExpandSubtree(selectedNode, true);
        selectedNode.setSelected(true);
        scrollToTreeNode(selectedNode);
        redrawTree();
    }

    public static void expandSubtree(TreeNode start)
    {
        start.setSelected(true);
        start.setExpanded(true);
        start.setSelected(false);

        for (TreeNode n : start.getChildren())
        {
            expandSubtree(n);
        }

    }

    public void collapseCurrentSubtree()
    {
        TreeBean.collapseOrExpandSubtree(selectedNode, false);
        selectedNode.setSelected(true);
        scrollToTreeNode(selectedNode);
        redrawTree();
    }

    public static void collapseOrExpandSubtree(TreeNode start, boolean expanded)
    {
        start.setSelected(true);
        start.setExpanded(expanded);
        start.setSelected(false);

        for (TreeNode n : start.getChildren())
        {
            collapseOrExpandSubtree(n, expanded);
        }
    }

    public static void collapseSubtree(TreeNode start)
    {
        start.setSelected(true);
        start.setExpanded(false);
        start.setSelected(false);

        for (TreeNode n : start.getChildren())
        {
            collapseSubtree(n);
        }
    }

    public static TreeNode getPFTreeChild(TreeNode parent, IMdeElement element)
    {

        TreeNode child = null;
        for (TreeNode n : parent.getChildren())
        {
            NodeContents contents = (NodeContents) n.getData();
            if (contents.getElement().equals(element))
            {
                child = n;
                break;
            }
        }
        return child;
    }

    // function to trigger building of tree from singleFileUploadDlg.xhtml
    public void communicateDispFileAndBuildTree()
    {
        multipleFileEditor.getVisualFileFromMaster();
        this.buildTree();
    }

    // setters and getters

    public TreeNode getSelectedNode()
    {

        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode)
    {

        this.selectedNode = selectedNode;
    }

    public TreeNode getRoot()
    {

        return root;
    }

    public void setRoot(TreeNode root)
    {

        this.root = root;
    }

    public ElementSelectionBean getSelectionBean()
    {

        return selectionBean;
    }

    public void setSelectionBean(ElementSelectionBean selectionBean)
    {

        this.selectionBean = selectionBean;
    }

    public MultipleFileEditingBean getMultipleFileEditor()
    {

        return multipleFileEditor;
    }

    public void setMultipleFileEditor(MultipleFileEditingBean multipleFileEditor)
    {

        this.multipleFileEditor = multipleFileEditor;
    }

    public AnnotationInserterBean getAnnotationInserter()
    {

        return annotationInserter;
    }

    public void setAnnotationInserter(AnnotationInserterBean annotationInserter)
    {

        this.annotationInserter = annotationInserter;
    }

    public ValueHandlerBean getValueHandler()
    {

        return valueHandler;
    }

    public void setValueHandler(ValueHandlerBean valueHandler)
    {

        this.valueHandler = valueHandler;
    }

    public ElementProcessingBean getProcessingBean()
    {

        return processingBean;
    }

    public void setProcessingBean(ElementProcessingBean processingBean)
    {

        this.processingBean = processingBean;
    }

    public TextViewBean getTextViewBean()
    {

        return textViewBean;
    }

    public void setTextViewBean(TextViewBean textViewBean)
    {
        this.textViewBean = textViewBean;
    }

    public void setObj(Object obj)
    {
        //System.out.println("TreeBean.setObj");
        //System.out.println(obj.getClass().getName());
        this.obj = obj;
    }

    public Object getObj()
    {
        return obj;
    }


    // debugging methods

    public void scrollToDateStamp()
    {
        TreeNode dateStampNode = getDateTimeNode();
        TreeBean.scrollToTreeNode(dateStampNode);
    }

    public void scrollToRoot()
    {
        TreeBean.scrollToTreeNode(root.getChildren().get(0));
    }

    public void printMap(Map<String, Object> map)
    {

        System.out.println("print attribute map:");
        for (Entry<String, Object> e : map.entrySet())
        {
            System.out.println(e.getKey() + ";" + e.getValue());
        }
        System.out.println("end print attribute map");
    }

    public void testMethod()
    {

        System.out.println("TreeBean.testMethod()");
        // FacesContext fc = FacesContext.getCurrentInstance();
        // Map<String,Object> params =
        // fc.getExternalContext().getRequestParameterMap();
        // String componentId = params.get("componentId");
    }

    public void onNodeExpand()
    {

        System.out.println("TreeBean.onNodeExpand()");
    }

    /**
     * Used only for debugging to quickly load a default test file.
     * Not used for production.
     */
    public void setFileAndBuildTree()
    {

        Path folder = Paths.get("/home/uuuu/workspaceOBEOS/git/esa.obeos.metadataeditor.model/src/test/resources");
        Path filepath = Paths.get(
                // "/home/uuuu/workspaceOBEOS/git/esa.obeos.metadataeditor.model/src/test/resources/2JCLX9A3_extr1");
                "/home/uuuu/spacebelFiles/DatasetSeries/v4JMLwlB");
        // "/home/uuuu/spacebelFiles/DatasetSeries/aRBPgrHe");
        // FileData filedata = new FileData("2JCLX9A3_extr1", folder, filepath);
        // Master.documentsStore.fileDataList.add(filedata);
        // Master.documentsStore.addFile(filedata);
        try
        {
            // Master.documentsStore.createAndAddNewTempfile(new
            // FileInputStream(new File(filepath.toString())),
            // "2JCLX9A3_extr1", "xml", true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            org.apache.commons.io.IOUtils.copy(new FileInputStream(filepath.toString()), baos);
            byte[] bytes = baos.toByteArray();
            List<FileData> result = FilePreprocessor.extractMDFilesFromXMLEmbedding(bytes, "2JCLX9A3", "xml");
            multipleFileEditor.setVisualisedFile(result.get(0));
            multipleFileEditor.propagateFileSelection();
            GUIrefs.updateComponent("checkboxpanel");
            /// this.initAnnotationsInserter();
            // GUIrefs.displayAlert("annotationInserter==null? " +
            /// (this.annotationInserter == null));
        }
        catch (FileNotFoundException e)
               {
        	e.printStackTrace();
        	GUIrefs.displayAlert("in TreeBean.setFileAndBuildTree: could not build the tree");
        }
        catch (IOException e)
        {
        	e.printStackTrace();
        }
        buildTree();
    }

    private TreeNode getDateTimeNode()
    {
        TreeNode node = null;
        try
        {
            TreeNode visRoot = this.root.getChildren().get(0);
            node = visRoot;
            List<TreeNode> firstLevelChildren = visRoot.getChildren();
            for (TreeNode n : firstLevelChildren)
            {
                IMdeElement elt = ((NodeContents) n.getData()).getElement();
                if (elt.getXmlElementName().equals("dateStamp"))
                {
                    node = n;
                    break;
                }
            }
            node = node.getChildren().get(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return node;
    }
}
