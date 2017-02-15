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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.xml.bind.JAXBElement;

import org.primefaces.context.RequestContext;
import org.primefaces.model.TreeNode;
import org.primefaces.model.UploadedFile;

import esa.obeos.metadataeditor.model.api.IMdeElement;
import global.ConfigurationConstants;
import global.GUIrefs;
import global.Master;
import utilities.StringUtilities;
import utilities.XmlFormatter;

@ManagedBean
@SessionScoped
public class TextViewBean implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4303237721081767748L;
	IMdeElement selectedElement;
	TreeNode selectedNode;
	String textRepresentation;

	UploadedFile file;
	InputStream instream;

	PFTreeBuilderFromModel treeBuilder;
	MultipleFileEditingBean multipleFileEditor;

	@ManagedProperty(value="#{buildingBlockCollectionBean}")
	private BuildingBlockCollectionBean blockBean;
	
	public void reset()
	{
		selectedElement=null;
		selectedNode=null;
		textRepresentation="";
		file=null;
		instream=null;
	}

	public void insertXmlFragment()
	{
		try
		{
	        // selected node is up to date as it was set from TreeBean
	        String elementName = selectedElement.getXmlElementName();
	        IMdeElement parent = null;
	        if(ConfigurationConstants.plainXmlRootNames.contains(elementName))
	        {
	            //TODO:
	           //replace documents
	            //call dialogue and ask if user really wants to do this
	            //positive reply: create n times new document from textfield contents
	            GUIrefs.displayAlert("This operation would replace the whole document and all documents "
	                    + "in the selection by the text in the textfield, but it\\\'s not implemented yet");
	            RequestContext.getCurrentInstance().execute("PF('confirmationDlg').show()");
	            //int howMany = Math.max(1,Master.documentsStore.selectedFiles.size());
	            return;
	        }
	        else
	        {
	          parent = selectedElement.getParent();
	        }
	        
	        String fieldName = selectedElement.getFieldName();
	        Integer index = selectedElement.getMyIndex();
			assert (parent != null);
			assert (fieldName != null);
			parent.insertChild(textRepresentation, fieldName, index, null);
			if (multipleFileEditor.isCoeditSelection() && multipleFileEditor.getSelectedFiles().size() > 1)
			{
				multipleFileEditor.coinsertXmlFragment(parent, fieldName, textRepresentation);

			}
			// is the rest necessary at all?
			IMdeElement newElement = null;
			if (index == null)
			{
				newElement = parent.getChildElements().get(fieldName);
			}
			else
			{
				List<?> childlist = parent.getChildListElements().get(fieldName);
				Object newObject = childlist.get(index);
				if (newObject instanceof IMdeElement)
				{
					newElement = (IMdeElement) newObject;
				}
				else if (newObject instanceof JAXBElement)
				{
					JAXBElement<?> jaxbElt = (JAXBElement) newObject;
					if (jaxbElt.getValue() instanceof IMdeElement)
					{
						newElement = (IMdeElement) jaxbElt.getValue();
					}
					else
					{
						GUIrefs.displayAlert("New child is of unsupported JAXBElement type!");
						return;
					}
				}
			}
			selectedElement = newElement;
			///
			// NodeContents c = (NodeContents) selectedNode.getData();
			// c.element = newElement;
			// selectedNode.getChildren().clear();
			TreeNode parentNode = selectedNode.getParent();
			deleteChild(parentNode, selectedNode);
			treeBuilder.setTreeParent(parentNode);
			newElement.traverseInitIndex(newElement.getFieldName(), treeBuilder);
			selectedNode = null;
			GUIrefs.updateComponent(GUIrefs.tree);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			String msg = e.getMessage();
				
			//GUIrefs.displayAlert("Insertion of xml fragment failed: " + StringUtilities.escapeQuotes(e.getMessage()));
			e.printStackTrace();
		}
	}

	public void loadText()
	{

		GUIrefs.displayAlert("TextViewBean.loadText()");
		//System.out.println("TextViewBean.loadText()");
		if (file != null)
		{
			
			try
			{
				instream = file.getInputstream();
			}
			catch (IOException e)
			{
				GUIrefs.displayAlert("Cannot read input stream of file - IOException");
			}
		}

		if (instream != null)
		{
			this.textRepresentation = XmlFormatter.format(XmlFormatter.convertStreamToString(instream));
			GUIrefs.updateComponent(GUIrefs.fragmentTextArea);
		}
		// GUIrefs.displayAlert(" still in loadText()");
		// RequestContext.getCurrentInstance().execute("PF('wvTextViewDlg').show();");
	}

	public void takeBuildingBlock()
	{
		//this.textRepresentation = this.blockBean.getCurrentBuildingBlock();
	    if(!this.blockBean.isOnlyMatchingOnes())
	    {
	        if(!this.blockBean.checkTypeMatch())
	        {
	           GUIrefs.displayAlert("Note: the selected block is not of the right type, so writing it back to the document will be rejected! "); 
	        }
	    }
		  this.textRepresentation = this.blockBean.getCurrentBlockContents();

	}

	private void deleteChild(TreeNode parent, TreeNode child)
	{

		List<TreeNode> list = parent.getChildren();
		list.remove(child);

		// debugging: print children list after removal
		List<TreeNode> list2 = parent.getChildren();
		for (TreeNode n : list2)
		{
			NodeContents c = (NodeContents) n.getData();
		}
	}

	public void printStringRepresentation()
	{

		try
		{
			System.out.println(selectedElement.getXmlFragment());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// setters and getters
	public IMdeElement getSelectedElement()
	{
		return selectedElement;
	}
	public void setSelectedElement(IMdeElement selectedElement)
	{
		this.selectedElement = selectedElement;
		this.blockBean.setSelectedElement(selectedElement);
	}


	public String getFieldName()
	{
	    String ret = "";

		if (null != selectedElement)
		{
			GUIrefs.updateComponent(GUIrefs.fragmentLabel);
			try
            {
                ret = selectedElement.getFieldName();
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		}

		return ret;
	}

	public String getPath() throws Exception
	{

		if(null!= selectedElement)
		{
			return selectedElement.getPath();
		}
		else
		{
			return "";
		}
	}


	public TreeNode getSelectedNode()
	{

		return selectedNode;
	}

	public void setTreeBuilder(PFTreeBuilderFromModel treeBuilder)
	{

		this.treeBuilder = treeBuilder;
	}

	public void setSelectedNode(TreeNode selectedNode)
	{

		this.selectedNode = selectedNode;
		NodeContents c = (NodeContents) selectedNode.getData();
		selectedElement = c.getElement();
		this.blockBean.setSelectedElement(selectedElement);
		try
		{
			textRepresentation = selectedElement.getXmlFragment();
			GUIrefs.updateComponent(GUIrefs.fragmentTextArea);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public MultipleFileEditingBean getMultipleFileEditor()
	{

		return multipleFileEditor;
	}

	public void setMultipleFileEditor(MultipleFileEditingBean multipleFileEditor)
	{

		this.multipleFileEditor = multipleFileEditor;
	}

	public UploadedFile getFile()
	{

		return file;
	}

	public void setFile(UploadedFile file)
	{

		this.file = file;
	}

	public String getTextRepresentation()
	{

		return textRepresentation;
	}

	public void setTextRepresentation(String textRepresentation)
	{

		this.textRepresentation = textRepresentation;
	}

	public BuildingBlockCollectionBean getBlockBean()
	{
	
		return blockBean;
	}

	public void setBlockBean(BuildingBlockCollectionBean blockBean)
	{
	
		this.blockBean = blockBean;
	}

	public InputStream getInstream()
	{

		return instream;
	}

	public void setInstream(InputStream instream)
	{

		this.instream = instream;
	}
}
