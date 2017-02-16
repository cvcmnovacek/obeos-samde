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

package esa.obeos.metadataeditor.jaxrs.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import esa.obeos.metadataeditor.jaxrs.api.datatypes.BuildingBlockArgumentType;
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
import esa.obeos.metadataeditor.jaxrs.api.datatypes.NodeValueArgumentType;

/**
 * Defines the MetadataEditorService interface.
 *
 */
public interface MetadataEditorService
{
    @POST
    @Path("createMD/{type}/{version}/{fileName}")
    public MetadataResultType createMD(@PathParam("type") MetadataType type, 
            @PathParam("version") MetadataVersionType version,
            @PathParam("fileName") String fileName);

    @POST
    @Path("uploadMD")
    @Consumes({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
    public MetadataResultType uploadMD(MetadataCotainerType metadataContainer);

    @GET
    @Path("getMDList")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
    public GetMetadataListResultType getMDList();

    @POST
    @Path("editMD")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
    public DoubleMetadataResultType editMD(@QueryParam("fileUrl") String fileUrl);

    @GET
    @Path("downloadMD/{metadataId}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
    public DownloadMetadataResultType downloadMD(@PathParam("metadataId") String metadataId);

    @DELETE
    @Path("closeMD/{metadataId}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
    public MetadataResultType closeMD(@PathParam("metadataId") String metadataId);

    @GET
    @Path("getAllowedNodes/{metadataId}/{path : (.*)?}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
    public GetAllowedChildrenResultType getAllowedNodes(@PathParam("metadataId") String metadataId,
            @PathParam("path") String path);

    @PUT
    @Path("insertNode")
    @Consumes({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
    public MetadataResultType insertNode(NodeArgumentType argument);

    @DELETE
    @Path("deleteNode/{metadataId}/{path : (.*)?}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
    public MetadataResultType deleteNode(@PathParam("metadataId") String metadataId,
            @PathParam("path") String path);

    @PUT
    @Path("setNodeValue")
    @Consumes({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
    public MetadataResultType setNodeValue(NodeValueArgumentType argument);

    @PUT
    @Path("setNodeAttribute")
    @Consumes({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
    public MetadataResultType setNodeAttribute(NodeAttributeArgumentType argument);

    @GET
    @Path("downloadZippedMD/{metadataId}")
    @Produces("application/zip")
    public Response downloadZippedMD(@PathParam("metadataId") String metadataId);

    @GET
    @Path("getOSDD")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
    public Response getOSDD(@QueryParam("fileName") String fileName);

    @PUT
    @Path("addBuildingBlock")
    @Consumes({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
    public BuildingBlockResultType addBuildingBlock(BuildingBlockArgumentType argument);

    @GET
    @Path("getBuildingBlockInfoList")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
    public GetBuildingBlockInfoListResultType getBuildingBlockInfoList();

    @GET
    @Path("downloadBuildingBlock/{name}")
    @Produces(MediaType.APPLICATION_XML)
    public Response downloadBuildingBlock(@PathParam("name") String name);

    @GET
    @Path("downloadBuildingBlocks")
    @Produces("application/zip")
    public Response downloadBuildingBlocks();

    @DELETE
    @Path("deleteBuildingBlock/{name}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
    public BuildingBlockResultType deleteBuildingBlock(@PathParam("name") String name);
}
