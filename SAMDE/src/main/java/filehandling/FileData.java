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

package filehandling;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import esa.obeos.metadataeditor.model.api.ModelType;
import esa.obeos.metadataeditor.model.api.ModelVersion;
import utilities.StringUtilities;
import esa.obeos.metadataeditor.common.util.DomUtil;
import esa.obeos.metadataeditor.model.api.IMdeElement;
import esa.obeos.metadataeditor.model.api.Model;
import semanticannot.Concept;
import semanticannot.DescriptiveKeywordsAnnotationObject;
import semanticannot.InstrumentData;
import semanticannot.PlatformAnnotationObject;

public class FileData implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 468031005078438089L;
    private String filename;
    private String displayname;
    private Path path; // this is the path to the directory
    private Path completePath; // completePath == path + filename
    private ModelVersion modelVersion;
    private IMdeElement modelRoot = null;
    // private Element rootNode = null;
    private Document rootNode = null;
    private String sourceUrl = null;
    private String uploadUrl = null;

    private Map<String, List<DescriptiveKeywordsAnnotationObject>> keywordAnnotation = null;
    
    // <platform-id, corresponding annotation objects>
    private Map<Concept, List<PlatformAnnotationObject>> platformAnnotation = null;
    
    public Map<Concept, List<InstrumentData>> getInstrumentAnnotation()
    {
        return instrumentAnnotation;
    }

    public void setInstrumentAnnotation(Map<Concept, List<InstrumentData>> instrumentAnnotation)
    {
        this.instrumentAnnotation = instrumentAnnotation;
    }

    private Map<Concept,List<InstrumentData>> instrumentAnnotation = null;
    //optionally
    //private List<InstrumentData> instrumentAnnotation = null;

    public FileData(String filename, Path path, Path completePath)
    {
        this.displayname = filename;
        this.path = path;
        this.completePath = completePath;
        this.filename = StringUtilities.difference(path.toString(), completePath.toString()).substring(1);
    }

    // getters
    public String getFilename()
    {
        return filename;
    }

    public Path getPath()
    {
        return path;
    }

    public Path getCompletePath()
    {
        return completePath;
    }

    // setters
    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    public void setPath(Path path)
    {
        this.path = path;
    }

    public String getDisplayname()
    {
        return displayname;
    }

    public void setDisplayname(String displayname)
    {
        this.displayname = displayname;
    }

    public void setCompletePath(Path completePath)
    {
        this.completePath = completePath;
    }

    public ModelType getRootType()
    {
        switch (rootNode.getDocumentElement().getPrefix())
        {
        case "gmi":
            return ModelType.GMI;
        case "gmd":
            return ModelType.GMD;
        default:
            return ModelType.Invalid;
        }
    }

    public ModelVersion getModelVersion()
    {
        return modelVersion;
    }

    public void setModelVersion(ModelVersion modelVersion)
    {
        this.modelVersion = modelVersion;
    }

    public IMdeElement getModelRoot()
    {
        if (null == modelRoot && null != rootNode)
        {
            try
            {
                modelRoot = Model.convert(rootNode, getRootType(), getModelVersion(), false);
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return modelRoot;
    }

    public void setModelRoot(IMdeElement modelRoot)
    {
        this.modelRoot = modelRoot;
    }

    public Document getRootNode() throws Exception
    {
        if (null == this.rootNode)
        {
            if (null != this.modelRoot)
            {
                this.rootNode = Model.convert(this.modelRoot, false);
            }
            else
            {
                this.rootNode = DomUtil.load(this.completePath.toString());
            }
        }

        return this.rootNode;
    }

    public void setRootNode(Document rootNode)
    {
        this.rootNode = rootNode;
    }

    public Map<String, List<DescriptiveKeywordsAnnotationObject>> getKeywordAnnotation()
    {
        return keywordAnnotation;
    }

    public void setKeywordAnnotation(Map<String, List<DescriptiveKeywordsAnnotationObject>> keywordAnnotation)
    {
        this.keywordAnnotation = keywordAnnotation;
    }

    public Map<Concept, List<PlatformAnnotationObject>> getPlatformAnnotation()
    {
        return platformAnnotation;
    }

    public void setPlatformAnnotation(Map<Concept, List<PlatformAnnotationObject>> platformAnnotation)
    {
        this.platformAnnotation = platformAnnotation;
    }

    public String getSourceUrl()
    {
        return this.sourceUrl;
    }

    public void setSourceUrl(String sourceUrl)
    {
        this.sourceUrl = sourceUrl;
    }

    public String getUploadUrl()
    {
        if (uploadUrl == null)
        {
            /*
             * If the upload URL was not set yet, then we will calculate it from
             * sourceUrl
             * 
             * Calculation below is defined based on communication with
             * Spacebel. Name of document in last part of path is removed and
             * only "base" url is taken and only parentIdentifier in query part
             * is used
             */
            /*-
             * sourceUrl:
             * http://obeos.spacebel.be/eo-catalogue/series/MIP_MW1_AX?parentIdentifier=EOP:ESA:GPOD-EO&httpAccept=application%2Fvnd.iso.19139-2%2Bxml
             * 
             * uploadUrl:
             * http://obeos.spacebel.be/eo-catalogue/series?parentIdentifier=EOP:ESA:GPOD-EO
             */
            try
            {
                URL surl = new URL(sourceUrl);
                String uPath = surl.getPath().substring(0, surl.getPath().lastIndexOf("/"));
                String uQuery = "";
                for (String s : surl.getQuery().split("&"))
                {
                    if (s.startsWith("parentIdentifier"))
                    {
                        uQuery = s;
                        break;
                    }
                }
                URL uurl = new URL(surl.getProtocol(), surl.getHost(), surl.getPort(), uPath + "?" + uQuery);
                uploadUrl = uurl.toString();
            }
            catch (MalformedURLException e)
            {
                // We will do nothing. It will just be empty and will have to be
                // filled in manually
            }
        }
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl)
    {
        this.uploadUrl = uploadUrl;
    }

    @Override
    public String toString()
    {
        return displayname;
    }
    // @Override
    // public String toString(){
    // String ret = "[" + filename +";" + completePath +"]";
    // return ret;
    // }
}
