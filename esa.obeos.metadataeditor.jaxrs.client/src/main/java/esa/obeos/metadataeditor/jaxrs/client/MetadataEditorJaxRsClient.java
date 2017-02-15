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

package esa.obeos.metadataeditor.jaxrs.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.w3c.dom.Element;

import esa.obeos.metadataeditor.jaxrs.api.datatypes.BuildingBlockArgumentType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.BuildingBlockInfoType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.BuildingBlockResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.DoubleMetadataResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.DownloadMetadataResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.GetAllowedChildrenResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.GetBuildingBlockInfoListResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.GetMetadataListResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.MetadataCotainerType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.MetadataResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.MetadataType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.MetadataVersionType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.NodeArgumentType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.NodeAttributeArgumentType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.NodeType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.NodeValueArgumentType;

/**
 * Simple Meta data editor JAX-RS Client
 *
 */
public class MetadataEditorJaxRsClient
{
    /** the user name */
    protected String user;
    
    protected Client client;

    protected WebTarget webTarget;
    
    /**
     * Initializes the app.
     *
     * @throws Exception
     *          the exception
     **/
    public MetadataEditorJaxRsClient(String host, int port, String path) throws Exception 
    {
        if( null == host || host.isEmpty() )
        {
            throw new IllegalArgumentException("invalid 'host'");
        }
        
        if( port < 0 )
        {
            throw new IllegalArgumentException("invalid 'port'");
        }
        
        if( null == path || path.isEmpty() )
        {
            throw new IllegalArgumentException("invalid 'path'");
        }
        
        StringBuilder urlBuilder = new StringBuilder("http://");

        urlBuilder.append(host);
        urlBuilder.append(':');
        urlBuilder.append(String.valueOf(port));
        urlBuilder.append(path);

        this.client = ClientBuilder.newClient();
        this.webTarget = this.client.target(urlBuilder.toString());
    }

    public MetadataResultType createMD(MetadataType type, MetadataVersionType version, String fileName) throws Exception
    {
        MetadataResultType ret = null;

        WebTarget webTarget = this.webTarget.path("createMD").path(type.name()).path(version.name()).path(fileName);

        Builder builder = webTarget.request(MediaType.APPLICATION_XML);

        ret = builder.post(null, MetadataResultType.class);

        if( null != ret.getDiagnostic() )
        {
            System.out.println(ret.getDiagnostic().getCode());
            System.out.println(ret.getDiagnostic().getMesage());
        }

        return ret;
    }

    public MetadataResultType uploadMD(MetadataCotainerType metadataContainer) throws Exception
    {
        MetadataResultType ret = null;

        WebTarget webTarget = this.webTarget.path("uploadMD");

        Builder builder = webTarget.request(MediaType.APPLICATION_XML);

        ret = builder.post(Entity.xml(metadataContainer), MetadataResultType.class);

        if( null != ret.getDiagnostic() )
        {
            System.out.println(ret.getDiagnostic().getCode());
            System.out.println(ret.getDiagnostic().getMesage());
        }

        return ret;
    }

    public DoubleMetadataResultType editMD(String fileUrl) throws Exception
    {
        DoubleMetadataResultType ret = null;

        WebTarget webTarget = this.webTarget.path("editMD").queryParam("fileUrl", fileUrl);

        Builder builder = webTarget.request(MediaType.APPLICATION_XML);

        ret = builder.post(Entity.entity(null, "application/text"), DoubleMetadataResultType.class);

        if( null != ret.getDiagnostic() )
        {
            System.out.println(ret.getDiagnostic().getCode());
            System.out.println(ret.getDiagnostic().getMesage());
        }

        return ret;
    }

    public GetMetadataListResultType getMDList() throws Exception
    {
        GetMetadataListResultType ret = null;

        WebTarget webTarget = this.webTarget.path("getMDList");

        Builder builder = webTarget.request(MediaType.APPLICATION_XML);

        ret = builder.get(GetMetadataListResultType.class);

        if( null != ret.getDiagnostic() )
        {
            System.out.println(ret.getDiagnostic().getCode());
            System.out.println(ret.getDiagnostic().getMesage());
        }

        return ret;
    }

    public DownloadMetadataResultType downloadMD(String metadataId) throws Exception
    {
        DownloadMetadataResultType ret = null;

        WebTarget webTarget = this.webTarget.path("downloadMD").path(metadataId);

        Builder builder = webTarget.request(MediaType.APPLICATION_XML);

        ret = builder.get(DownloadMetadataResultType.class);

        if( null != ret.getDiagnostic() )
        {
            System.out.println(ret.getDiagnostic().getCode());
            System.out.println(ret.getDiagnostic().getMesage());
        }

        return ret;
    }

    public MetadataResultType closeMD(String metadataId) throws Exception
    {
        MetadataResultType ret = null;

        WebTarget webTarget = this.webTarget.path("closeMD").path(metadataId);

        Builder builder = webTarget.request(MediaType.APPLICATION_XML);

        ret = builder.delete(MetadataResultType.class);

        if( null != ret.getDiagnostic() )
        {
            System.out.println(ret.getDiagnostic().getCode());
            System.out.println(ret.getDiagnostic().getMesage());
        }

        return ret;
    }

    public GetAllowedChildrenResultType getAllowedNodes(String metadataId, String path) throws Exception
    {
        GetAllowedChildrenResultType ret = null;

        WebTarget webTarget = this.webTarget.path("getAllowedNodes").path(metadataId).path(path);

        Builder builder = webTarget.request(MediaType.APPLICATION_XML);

        ret = builder.get(GetAllowedChildrenResultType.class);

        if( null != ret.getDiagnostic() )
        {
            System.out.println(ret.getDiagnostic().getCode());
            System.out.println(ret.getDiagnostic().getMesage());
        }

        return ret;
    }

    public MetadataResultType insertNode(String metadataId, String path, String nodeName) throws Exception
    {
        MetadataResultType ret = null;

        WebTarget webTarget = this.webTarget.path("insertNode");

        Builder builder = webTarget.request(MediaType.APPLICATION_XML);

        NodeArgumentType argument = new NodeArgumentType();
        argument.setMetadataId(metadataId);
        argument.setPath(path);
        argument.setNodeName(nodeName);
        
        ret = builder.put(Entity.xml(argument), MetadataResultType.class);

        if( null != ret.getDiagnostic() )
        {
            System.out.println(ret.getDiagnostic().getCode());
            System.out.println(ret.getDiagnostic().getMesage());
        }

        return ret;
    }

    public MetadataResultType insertNode(String metadataId, String path, Element element, Integer index) throws Exception
    {
        MetadataResultType ret = null;

        WebTarget webTarget = this.webTarget.path("insertNode");

        Builder builder = webTarget.request(MediaType.APPLICATION_XML);

        NodeType node = new NodeType();
        node.setAny(element);
        
        NodeArgumentType argument = new NodeArgumentType();
        argument.setMetadataId(metadataId);
        argument.setPath(path);
        argument.setNodeType(node);
        argument.setIndex(index);
        
        ret = builder.put(Entity.xml(argument), MetadataResultType.class);

        if( null != ret.getDiagnostic() )
        {
            System.out.println(ret.getDiagnostic().getCode());
            System.out.println(ret.getDiagnostic().getMesage());
        }

        return ret;
    }
    
    public MetadataResultType deleteNode(String metadataId, String path) throws Exception
    {
        MetadataResultType ret = null;

        WebTarget webTarget = this.webTarget.path("deleteNode").path(metadataId).path(path);

        Builder builder = webTarget.request(MediaType.APPLICATION_XML);

        ret = builder.delete(MetadataResultType.class);

        if( null != ret.getDiagnostic() )
        {
            System.out.println(ret.getDiagnostic().getCode());
            System.out.println(ret.getDiagnostic().getMesage());
        }

        return ret;
    }

    public MetadataResultType setNodeValue(String metadataId, String path, String value) throws Exception
    {
        MetadataResultType ret = null;

        WebTarget webTarget = this.webTarget.path("setNodeValue");

        Builder builder = webTarget.request(MediaType.APPLICATION_XML);

        NodeValueArgumentType argument = new NodeValueArgumentType();
        argument.setMetadataId(metadataId);
        argument.setPath(path);
        argument.setNodeValue(value);
        
        ret = builder.put(Entity.xml(argument), MetadataResultType.class);

        if( null != ret.getDiagnostic() )
        {
            System.out.println(ret.getDiagnostic().getCode());
            System.out.println(ret.getDiagnostic().getMesage());
        }

        return ret;
    }

    public MetadataResultType setNodeAttribute(String metadataId, String path, String attribName, String attribValue) throws Exception
    {
        MetadataResultType ret = null;

        WebTarget webTarget = this.webTarget.path("setNodeAttribute");

        Builder builder = webTarget.request(MediaType.APPLICATION_XML);

        NodeAttributeArgumentType argument = new NodeAttributeArgumentType();
        argument.setMetadataId(metadataId);
        argument.setPath(path);
        argument.setAttributeName(attribName);
        argument.setAttributeValue(attribValue);
        
        ret = builder.put(Entity.xml(argument), MetadataResultType.class);

        if( null != ret.getDiagnostic() )
        {
            System.out.println(ret.getDiagnostic().getCode());
            System.out.println(ret.getDiagnostic().getMesage());
        }

        return ret;
    }

    public BuildingBlockResultType addBuildingBlock(String name, String type, String body) throws Exception
    {
        BuildingBlockResultType ret = null;

        WebTarget webTarget = this.webTarget.path("addBuildingBlock");

        Builder builder = webTarget.request(MediaType.APPLICATION_XML);

        BuildingBlockInfoType info = new BuildingBlockInfoType();
        info.setName(name);
        info.setType(type);

        BuildingBlockArgumentType argument = new BuildingBlockArgumentType();
        argument.setBuildingBlockInfoType(info);
        argument.setBody(body);

        ret = builder.put(Entity.xml(argument), BuildingBlockResultType.class);

        if( null != ret.getDiagnostic() )
        {
            System.out.println(ret.getDiagnostic().getCode());
            System.out.println(ret.getDiagnostic().getMesage());
        }

        return ret;
    }

    public GetBuildingBlockInfoListResultType getBuildingBlockInfoList() throws Exception
    {
        GetBuildingBlockInfoListResultType ret = null;

        WebTarget webTarget = this.webTarget.path("getBuildingBlockInfoList");

        Builder builder = webTarget.request(MediaType.APPLICATION_XML);

        ret = builder.get(GetBuildingBlockInfoListResultType.class);

        if( null != ret.getDiagnostic() )
        {
            System.out.println(ret.getDiagnostic().getCode());
            System.out.println(ret.getDiagnostic().getMesage());
        }

        return ret;
    }

    public BuildingBlockResultType deleteBuildingBlock(String name) throws Exception
    {
        BuildingBlockResultType ret = null;

        WebTarget webTarget = this.webTarget.path("deleteBuildingBlock").path(name);

        Builder builder = webTarget.request(MediaType.APPLICATION_XML);

        ret = builder.delete(BuildingBlockResultType.class);

        if( null != ret.getDiagnostic() )
        {
            System.out.println(ret.getDiagnostic().getCode());
            System.out.println(ret.getDiagnostic().getMesage());
        }

        return ret;
    }
}
