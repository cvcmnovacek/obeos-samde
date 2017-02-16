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

@ManagedBean
@SessionScoped
public class Dimensions implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6466747537681039718L;
	//private int treescrollheight = 200;//not used so far
	private int tablePanelWidth = 500;
	private int tablePanelHeight = 300;
	private int tablePanelHeight_reduced = 300-50;
	
	
	//setters and getters
	public int getTablePanelWidth()
	{
	
		return tablePanelWidth;
	}
	public void setTablePanelWidth(int tablePanelWidth)
	{
	
		this.tablePanelWidth = tablePanelWidth;
	}
	public int getTablePanelHeight()
	{
	
		return tablePanelHeight;
	}
	public void setTablePanelHeight(int tablePanelHeight)
	{
	
		this.tablePanelHeight = tablePanelHeight;
	}
	public int getTablePanelHeight_reduced()
	{
	
		return tablePanelHeight_reduced;
	}
	public void setTablePanelHeight_reduced(int tablePanelHeight_reduced)
	{
	
		this.tablePanelHeight_reduced = tablePanelHeight_reduced;
	}

}
