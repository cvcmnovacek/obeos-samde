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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.bind.UnmarshalException;

import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.util.TreeUtils;

//import com.sun.faces.facelets.util.Path; ???
import java.nio.file.Path;

import esa.obeos.metadataeditor.model.api.INotify;
import filehandling.FileData;
import esa.obeos.metadataeditor.model.api.Model;
import esa.obeos.metadataeditor.model.api.IMdeElement;
import global.Master;
import utilities.StringUtilities;
import global.ConfigurationConstants;
import global.GUIrefs;
import global.GlobalParameters;

public class PFTreeBuilderFromModel implements INotify, Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = 6214689066313096373L;
    private TreeNode root;
    private TreeNode treeParent;
    private TreeNode lastCreatedChild;

    public void reset()
    {
        root = null;
        treeParent = null;
        lastCreatedChild = null;
    }

    public PFTreeBuilderFromModel()
    {
        // root = new DefaultTreeNode(new NodeContents("root", null), null);
        // root.setExpanded(true);
        // treeParent = root;
    }

    public void initialiseRoot()
    {
        root = new DefaultTreeNode(new NodeContents("root", null), null);
        root.setExpanded(true);
        treeParent = root;
    }

    @SuppressWarnings("unused")
    @Override
    public void startElement(IMdeElement element, Integer index)
    {
        TreeNodeComparator comparator = new TreeNodeComparator();
        if (null == treeParent.getChildren() || null == index)
        {
            startElement(element);
        }
        else if (treeParent.getChildren().size() < (index + 1))
        {
            startElement(element);
        }
        else
        {
            try
            {
                String xmlName = element.getXmlElementName();

                NodeContents newChildContents = null;
                int i = 0;
                for (TreeNode t : treeParent.getChildren())
                {
                    if (index.intValue() == i)
                    {
                        newChildContents = new NodeContents(xmlName, element);
                        newChildContents.setIndex(i++);
                    }
                    NodeContents c = (NodeContents) t.getData();
                    c.setIndex(i++);
                }
                if (null != newChildContents)
                {
                    new DefaultTreeNode(newChildContents, treeParent);
                }

                TreeUtils.sortNode(treeParent, comparator);

                if (Master.DEBUG_LEVEL > Master.LOW)
                {
                    System.out.println("PFTreeBuilder: childlist after insertion of element " + xmlName);
                    for (TreeNode t : treeParent.getChildren())
                    {
                        NodeContents c = (NodeContents) t.getData();
                        System.out.println(c);
                    }
                }

                treeParent = treeParent.getChildren().get(index);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void startElement(IMdeElement element)
    {
        try
        {
            String fieldName = element.getXmlElementName();
            NodeContents c = new NodeContents(fieldName, element);
            TreeNode newChild = new DefaultTreeNode(c, treeParent);
            // newChild.setSelected(true);
            // newChild.setExpanded(true);
            // newChild.setSelected(false);

            treeParent = newChild;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void endElement(IMdeElement element)
    {
        if (treeParent != null)
        {
            lastCreatedChild = treeParent;
            treeParent = treeParent.getParent();
        }
        else
        {
            // System.out.println("treeParent null!");
        }
    }

    @SuppressWarnings("unused")
    public TreeNode treeFromModel()
    {
        initialiseRoot();
        FileData fileData = Master.documentsStore.getDisplayFile();
        Path fullPath = null;
        if (null != fileData)
        {
            fullPath = fileData.getCompletePath();

            if (Master.DEBUG_LEVEL > Master.LOW)
                System.out.println("treeFromModel: fullPath.toString() = " + fullPath.toString());
        }
        else
        {
            GUIrefs.displayAlert("Cannot build tree: no file selected for visualisation!");
            return null;
        }
        IMdeElement metaDataRoot = fileData.getModelRoot();

        try
        {
            File inputFile = new File(fullPath.toString());

            // check if metaDataRoot has already bean built, otherwise load it
            // from file
            if (null == metaDataRoot)
            {
                try
                {
                    metaDataRoot = Model.load(inputFile, fileData.getRootType(), fileData.getModelVersion(),
                            GlobalParameters.validateOnBuild);
                }
                catch (Exception e)
                {
                    GUIrefs.displayAlert(
                            "Loading failed probably due to validation failure -- trying to rebuild without validation");
                    try
                    {
                        metaDataRoot = Model.load(inputFile, fileData.getRootType(), fileData.getModelVersion(), false);
                    }
                    catch (Exception e2)
                    {
                        GUIrefs.displayAlert("Loading failed even without validation - maybe the file is damaged?");
                    }

                }
                fileData.setModelRoot(metaDataRoot);
                assert (Master.documentsStore.collection != null);
            }

            // traverse GMI file
            metaDataRoot.traverse("metaData", this);
        }
        catch (Exception e)
        {

            if (Master.DEBUG_LEVEL > Master.LOW)
                System.out.println("Exception in treeFromModel()");
            if (e instanceof UnmarshalException)
            {
                String msg = "Error in file " + fileData.getDisplayname() + "; "
                        + StringUtilities.escapeQuotes(e.getMessage())
                        + "; disable validation and try reloading file(s) in question";
                GUIrefs.displayAlert(msg);
            }
            e.printStackTrace();
        }

        return root;
    }

    // public void collapsingOrExpanding(TreeNode n, boolean option) {
    // n.setSelected(true);
    // if (n.getChildren().size() == 0) {
    // n.setSelected(false);
    // } else {
    // for (TreeNode s : n.getChildren()) {
    // collapsingOrExpanding(s, option);
    // }
    // n.setExpanded(option);
    // n.setSelected(false);
    // }
    // }

    // setters and getters
    public TreeNode getTreeParent()
    {
        return treeParent;
    }

    public void setTreeParent(TreeNode treeParent)
    {
        this.treeParent = treeParent;
    }

    public TreeNode getLastCreatedChild()
    {

        return lastCreatedChild;
    }

    public void setLastCreatedChild(TreeNode lastCreatedChild)
    {

        this.lastCreatedChild = lastCreatedChild;
    }

}
