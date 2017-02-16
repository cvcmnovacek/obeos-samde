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

package esa.obeos.metadataeditor.model.api;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import esa.obeos.metadataeditor.model.api.exc.AmbiguityException;

public interface IMdeElement
{
    public void traverse(String fieldName, INotify notify) throws Exception;

    public void traverseInitIndex(String fieldName, INotify notify) throws Exception;

    public String getXmlElementName() throws Exception;
    
    public String getAnnotationName() throws Exception;
    
    public String getFieldName() throws Exception;
    
    public String getXmlElementType();
    
    public boolean isRequired() throws Exception;

    public IMdeElement getParent();

    public Map<String, IMdeElement> getChildElements() throws Exception;
    
    public Map<String, List<?>> getChildListElements() throws Exception;

    public IMdeElement getChildByXmlName(String childXmlName) throws Exception;

    public IMdeElement getChildByXmlName(String childXmlName, Integer index) throws Exception;

    public List<?> getChildrenByXmlName(String childXmlName) throws Exception;

    public Map<String, Object> getAttribs() throws Exception;
      
    public boolean isAttributeRequired(String attributeName) throws Exception;

    public Class<?> getAttributeValueType(String name) throws Exception;

    public Object getAttributeValue(final String name) throws Exception;
    
    public void setAttributeValue(final String name, final String value) throws Exception;
    
    public boolean isChildRequired(String fieldName) throws Exception;

    public List<String> getCreatableChildNames(boolean includeChoices) throws Exception;

    public boolean hasMultipleOccurrences() throws Exception;

    public int getChildOcurrences(String childName) throws Exception;

    public IMdeElement createChild(String name, String choose, INotify notify) throws AmbiguityException, Exception;

    public IMdeElement insertChild(final IMdeElement child, final String fieldName, final Integer index, final INotify notify) throws Exception;

    public Object insertChild(final Element domElement, final String fieldName, final Integer index, final INotify notify) throws Exception;

    public Object insertChild(final String xmlFragment, final String fieldName, final Integer index, final INotify notify) throws Exception;
    
	public Integer getMyIndex() throws Exception;

	public Integer getMyGlobalIndex() throws Exception;

	public void deleteChild(IMdeElement childElement) throws Exception;
    
    public boolean isValue();

    public Class<?> getElementValueType() throws Exception;

    public Object getElementValue() throws Exception;

    public void setElementValue(Object value) throws Exception;

    public String getXmlFragment() throws Exception;
    
    public String getChildFieldName(final String childXmlName) throws Exception;

    public String getPath() throws Exception;

    public void validateInspire() throws Exception;

    public void accept(final IMdeVisitor visitor) throws Exception;

    public String toString();
}
