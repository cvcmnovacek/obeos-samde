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

package navigation;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.primefaces.component.tabview.TabView;
import org.primefaces.context.RequestContext;
import org.primefaces.event.TabChangeEvent;

import global.GUIrefs;

@ManagedBean
@SessionScoped
public class NavigatorBean implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -681488375462621209L;
	private final int tableIndex = 0;
	private final int valuesIndex = 1;
	private final int textIndex = 2;
	private final int annotationIndex = 3;

	private String top = "top";
	private int someWidth = 300;

	private int tabIndex = 0;
	private TabView tabview = new TabView();



	public void onTabChange(TabChangeEvent event)
	{

		String id = event.getTab().getId();

		if(id.equals("childTab"))
		{
		   tabIndex = tableIndex;	
		}
		else if(id.equals("valuesTab"))
		{
		   tabIndex = valuesIndex;	
		}
		else if(id.equals("textTab"))
		{
		   tabIndex = textIndex;	
		}
		else if(id.equals("annotationTab"))
		{
			tabIndex = annotationIndex;
			RequestContext.getCurrentInstance().execute("loadAnnotationFromResource();");
		}
		else
		{
			GUIrefs.displayAlert("Error: tab-id " + id + " cannot be processed");
			tabIndex = 0;
		}
	}

	public void setIndex(int index)
	{
		tabIndex = index; 
	}
	
	public void switchToAnnotationView()
	{
	   this.tabIndex =  annotationIndex;
	   RequestContext.getCurrentInstance().execute("loadAnnotationFromResource();");
	   GUIrefs.updateComponent("tabview");
	}

//	public void switchToTextView()
//	{
//
//		GUIrefs.displayAlert("switchToTextView - tabIndex=" + tabIndex);
//		tabview.setActiveIndex(this.textIndex);
//		this.tabIndex = this.textIndex;
//		GUIrefs.displayAlert("new tabIndex=" + tabIndex);
//		GUIrefs.updateComponent("tabview");
//		GUIrefs.updateComponent("mainPanelGrid");
//	}
	// setters and getters

	public String getTop()
	{

		return top;
	}

	public void setTop(String top)
	{

		this.top = top;
	}

	public TabView getTabview()
	{

		return tabview;
	}

	public void setTabview(TabView tabview)
	{

		this.tabview = tabview;
	}

	public int getTabIndex()
	{

		return tabIndex;
	}

	public void setTabIndex(int tabIndex)
	{

		//GUIrefs.displayAlert("setTabIndex: " + tabIndex);
		this.tabIndex = tabIndex;
	}

	public int getSomeWidth()
	{

		return someWidth;
	}

	public void setSomeWidth(int someWidth)
	{

		this.someWidth = someWidth;
	}

}
