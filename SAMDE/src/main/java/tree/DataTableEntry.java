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

public class DataTableEntry implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1533578025658112353L;

	private static final String iconYes = "ui-icon-check";
	private static final String iconNo = "ui-icon-close";
	String elementName;
	//String xpath;
	boolean required;
	String requStr;
	boolean exists;
	String existsStr;
	boolean creatable;
	String creatStr;
	boolean deletable;
	String delStr;
	boolean singleOccurrence;
	String multiplStr;
	int numberOfInstances; //yet to be filled properly
	
	
	@Override
	public String toString(){
		StringBuilder strBuilder = new StringBuilder("DataTableEntry - ");
		strBuilder.append(elementName);
		strBuilder.append("; requ = " + required);
		strBuilder.append("; creat = " + creatable);
		strBuilder.append("; del = " + deletable);
		return strBuilder.toString();
	}
	public String getElementName() {
		return elementName;
	}
	public void setElementName(String elementName) {
		this.elementName = elementName;
	}
	public boolean isRequired() {
		return required;
	}
	public void setRequired(boolean required) {
		this.required = required;
	}
	public boolean isCreatable() {
		return creatable;
	}
	public void setCreatable(boolean creatable) {
		this.creatable = creatable;
	}
	public boolean isDeletable() {
		return deletable;
	}
	public void setDeletable(boolean deletable) {
		this.deletable = deletable;
	}
	
	
	public String getRequStr() {
		//requStr = required ? "required" : "optional";
		requStr = required ? iconYes : iconNo;
		return requStr;
	}
	public String getExistsStr() {
		//existsStr = exists? "exists":"null";
		existsStr = exists? iconYes : iconNo;
		return existsStr;
	}
	public String getMultiplStr() {
		//multiplStr = singleOccurrence ? "once" : "multiple";
		multiplStr = singleOccurrence ? "1x" : ">1x";
		return multiplStr;
	}
public int getNumberOfInstances() {
		return numberOfInstances;
	}
	public void setNumberOfInstances(int numberOfInstances) {
		this.numberOfInstances = numberOfInstances;
	}
	//	public void setXpath(String xpath) {
//		this.xpath = xpath;
//	}
	public boolean isExists() {
		return exists;
	}
	public void setExists(boolean exists) {
		this.exists = exists;
	}
	public boolean isSingleOccurrence() {
		return singleOccurrence;
	}
	public void setSingleOccurrence(boolean singleOccurrence) {
		this.singleOccurrence = singleOccurrence;
	}
}
