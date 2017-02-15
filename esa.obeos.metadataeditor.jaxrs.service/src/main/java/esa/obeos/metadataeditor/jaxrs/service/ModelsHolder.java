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

package esa.obeos.metadataeditor.jaxrs.service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import esa.obeos.metadataeditor.model.api.IMdeElement;
import esa.obeos.metadataeditor.model.api.ModelType;
import esa.obeos.metadataeditor.model.api.ModelVersion;

public class ModelsHolder
{
    private String id;
    
    private Map<String, IMdeElement> modelMap;

    private ModelType type;
    
    private ModelVersion version;
    
    private Date creationDate;
    
    private Date uploadDate;
    
    
    public ModelsHolder()
    {
        this.modelMap = null;
        this.type = ModelType.Invalid;
        this.version = ModelVersion.Invalid;
        this.creationDate = null;
        this.uploadDate = null;
    }
 
    public String getMetadataId()
    {
        return this.id;
    }

    public void setMetadataId(String id)
    {
        this.id = id;
    }
    
    public Map<String, IMdeElement> getModels()
    {
        return this.modelMap;
    }
    
    public void setModel(String fileName, IMdeElement model)
    {
        this.modelMap = new LinkedHashMap<String, IMdeElement>();
        this.modelMap.put(fileName, model);
    }
    
    public void setModelList(Map<String, IMdeElement> modelMap)
    {
        this.modelMap = modelMap;
    }
    
    public ModelType getType()
    {
        return this.type;
    }
    
    public void setType(ModelType type)
    {
        this.type = type;
    }
    
    public ModelVersion getVersion()
    {
        return this.version;
    }
    
    public void setVersion(ModelVersion version)
    {
        this.version = version;
    }
    
    public Date getCreationDate()
    {
        return this.creationDate;
    }
    
    public void setCreationDate(Date creationDate)
    {
        this.creationDate = creationDate;
    }
    
    public Date getUploadDate()
    {
        return this.uploadDate;
    }
    
    public void setUploadDate(Date uploadDate)
    {
        this.uploadDate = uploadDate;
    }
}
