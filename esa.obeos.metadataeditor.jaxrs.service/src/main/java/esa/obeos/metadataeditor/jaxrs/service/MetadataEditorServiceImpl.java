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

package esa.obeos.metadataeditor.jaxrs.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.inject.Singleton;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.servlet.ServletContext;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import esa.obeos.metadataeditor.common.util.DomUtil;
import esa.obeos.metadataeditor.common.util.FileUtil;
import esa.obeos.metadataeditor.common.util.TimeUtil;
import esa.obeos.metadataeditor.jaxrs.api.MetadataEditorService;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.AllowedChildrenType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.BuildingBlockArgumentType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.BuildingBlockInfoType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.BuildingBlockResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.ChildNamesListType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.DiagnosticCodeType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.DiagnosticType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.DoubleMetadataResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.DownloadMetadataResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.GetAllowedChildrenResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.GetBuildingBlockInfoListResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.GetMetadataListResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.MetadataCotainerType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.MetadataEntryType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.MetadataResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.MetadataType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.MetadataVersionType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.ModelElementType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.NodeArgumentType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.NodeAttributeArgumentType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.NodeValueArgumentType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.SeverityType;
import esa.obeos.metadataeditor.model.api.IMdeElement;
import esa.obeos.metadataeditor.model.api.MdePath;
import esa.obeos.metadataeditor.model.api.Model;
import esa.obeos.metadataeditor.model.api.ModelType;
import esa.obeos.metadataeditor.model.api.ModelVersion;
import esa.obeos.metadataeditor.model.api.exc.InvalidPathException;
import esa.obeos.metadataeditor.model.impl.jxb.JAXBEx;


/**
 * The class represents JAX-RS Jersey resource implementation of the
 * MetadataEditorService interface.
 * 
 */
@Path("mde")
@Singleton
public class MetadataEditorServiceImpl implements MetadataEditorService
{
    public static final String MI_Metadata = "gmi:MI_Metadata";

    public static final String MD_Metadata = "gmd:MD_Metadata";

    public static final String[] metadata = {MI_Metadata, MD_Metadata};

    private static final String PARAMETER_PROXY_HOST = "proxy.host";

    private static final String PARAMETER_PROXY_PORT = "proxy.port";

    private static final int BUFFER_SIZE = 2048;

    private static final String TMP_DIRECTORY = "/tmp/";

    @Context
    private ServletContext servletContext = null;
    
    private boolean proxyInit = false;

    private String proxyHost = null;

    private int proxyPort = 80;

    private Map<String, ModelsHolder> modelsMap = null;

    // Map<NAME, AbstractMap.SimpleEntry<TYPE, BODY>>
    private Map<String, AbstractMap.SimpleEntry<String, String>> buildBlocksMap;
    
    public MetadataEditorServiceImpl()
    {
        this.modelsMap = Collections.synchronizedMap(new HashMap<String, ModelsHolder>());
        this.buildBlocksMap = Collections.synchronizedMap(new LinkedHashMap<String, AbstractMap.SimpleEntry<String, String>>());
    }

    @Override
    public MetadataResultType createMD(MetadataType type, MetadataVersionType version, String fileName)
    {
        System.out.println("called createMD(" + type.name() + ", " + version.name() + ")");

        MetadataResultType ret = new MetadataResultType();
        ret.setResult(SeverityType.SUCCESS);

        try
        {
            if (null == fileName || fileName.isEmpty())
            {
                throw new DiagnosticException(DiagnosticCodeType.INVALID_ARG, "fileName is null or empty");
            }

            ModelType modelType = MDEUtil.convert(type);
            ModelVersion modelVersion = MDEUtil.convert(version);

            IMdeElement model = Model.create(modelType, modelVersion);

            String metadataId = UUID.randomUUID().toString();

            ModelsHolder modelsHolder = new ModelsHolder();
            modelsHolder.setMetadataId(metadataId);
            modelsHolder.setType(modelType);
            modelsHolder.setVersion(modelVersion);
            modelsHolder.setCreationDate(new Date());
            modelsHolder.setModel(fileName, model);

            this.modelsMap.put(metadataId, modelsHolder);

            ret.setMetadataId(metadataId);

            System.out.println("created '" + metadataId + "'");
        }
        catch (DiagnosticException e)
        {
            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(e.getDiagnostic());

        }
        catch (Exception e)
        {
            DiagnosticType diag = new DiagnosticType();
            diag.setCode(DiagnosticCodeType.UNABLE_TO_CREATE);
            diag.setMesage(e.getMessage());

            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(diag);
        }

        return ret;
    }

    @Override
    public MetadataResultType uploadMD(MetadataCotainerType metadataContainer)
    {
        return uploadMD(metadataContainer, true);
    }

    public MetadataResultType uploadMD(MetadataCotainerType metadataContainer, boolean validate)
    {
        System.out.println("called uploadMD()");

        MetadataResultType ret = new MetadataResultType();
        ret.setResult(SeverityType.SUCCESS);

        try
        {
            if (null == metadataContainer)
            {
                throw new DiagnosticException(DiagnosticCodeType.INVALID_ARG, "metadataContainer is null");
            }

            ModelType modelType = MDEUtil.convert(metadataContainer.getType());
            ModelVersion modelVersion = MDEUtil.convert(metadataContainer.getVersion());

            List<ModelElementType> modelElements = metadataContainer.getMetadata();
            if (null == modelElements)
            {
                throw new DiagnosticException(DiagnosticCodeType.INVALID_METADATA, "modelElements is null");
            }

            if (modelElements.isEmpty())
            {
                throw new DiagnosticException(DiagnosticCodeType.INVALID_METADATA, "modelElements is empty");
            }

            Map<String, IMdeElement> models = new LinkedHashMap<String, IMdeElement>();

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            for (ModelElementType element : modelElements)
            {
                if (null == element.getFileName())
                {
                    throw new DiagnosticException(DiagnosticCodeType.INVALID_METADATA, "fileName is null");
                }

                if (element.getFileName().isEmpty())
                {
                    throw new DiagnosticException(DiagnosticCodeType.INVALID_METADATA, "fileName is is empty");
                }

                if (null == element.getAny())
                {
                    throw new DiagnosticException(DiagnosticCodeType.INVALID_METADATA, "any is null");
                }

                Document doc = docBuilder.newDocument();
                Node node = doc.importNode(element.getAny(), true);
                doc.appendChild(node);

                // DomUtil.printDocument(doc, System.out);
                IMdeElement mdeElement = Model.convert(doc, modelType, modelVersion, false);

                System.out.println("xs:any contains: " + mdeElement.getXmlElementName());

                models.put(element.getFileName(), mdeElement);
            }

            String metadataId = UUID.randomUUID().toString();

            ModelsHolder meodelsHolder = new ModelsHolder();
            meodelsHolder.setMetadataId(metadataId);
            meodelsHolder.setType(modelType);
            meodelsHolder.setVersion(modelVersion);
            meodelsHolder.setUploadDate(new Date());
            meodelsHolder.setModelList(models);

            this.modelsMap.put(metadataId, meodelsHolder);

            ret.setMetadataId(metadataId);
        }
        catch (DiagnosticException e)
        {
            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(e.getDiagnostic());
        }
        catch (Exception e)
        {
            DiagnosticType diag = new DiagnosticType();
            diag.setCode(DiagnosticCodeType.OTHER_REASON);
            if (null == e.getMessage())
            {
                Throwable t = e.getCause();
                if (null != t && null != t.getMessage())
                {
                    diag.setMesage(t.getMessage());
                }
            }
            else
            {
                diag.setMesage(e.getMessage());
            }

            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(diag);
        }

        return ret;
    }

    @Override
    public GetMetadataListResultType getMDList()
    {
        System.out.println("called getMDList()");

        GetMetadataListResultType ret = new GetMetadataListResultType();
        ret.setResult(SeverityType.SUCCESS);

        try
        {
            for (ModelsHolder modelsHolder : this.modelsMap.values())
            {
                MetadataEntryType entry = new MetadataEntryType();

                entry.setId(modelsHolder.getMetadataId());
                entry.setType(MDEUtil.convert(modelsHolder.getType()));
                entry.setVersion(MDEUtil.convert(modelsHolder.getVersion()));
                entry.setCreationTime(TimeUtil.convert(modelsHolder.getCreationDate()));
                entry.setUploadTime(TimeUtil.convert(modelsHolder.getUploadDate()));
                entry.setEdited(false);
                
                ret.getEntry().add(entry);
            }
        }
        catch (Exception e)
        {
            DiagnosticType diag = new DiagnosticType();
            diag.setCode(DiagnosticCodeType.OTHER_REASON);
            diag.setMesage(e.getMessage());

            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(diag);
        }

        return ret;
    }

    private static String createFileName(final ModelType modelType, int index)
    {
        StringBuilder fileName = new StringBuilder(modelType.name());
        fileName.append("_");
        fileName.append(index);
        fileName.append(".xml");

        return fileName.toString();
    }

    private String addModelHolder(final Map<String, IMdeElement> modelMap, final ModelType modeType, final ModelVersion modeVersion)
    {
        String ret = null;

        if (!modelMap.isEmpty())
        {
            ret = UUID.randomUUID().toString();

            ModelsHolder modelsHolder = new ModelsHolder();

            modelsHolder.setMetadataId(ret);
            modelsHolder.setType(modeType);
            modelsHolder.setVersion(modeVersion);
            modelsHolder.setUploadDate(new Date());
            modelsHolder.setModelList(modelMap);

            this.modelsMap.put(ret, modelsHolder);
        }

        return ret;
    }

    @Override
    public DoubleMetadataResultType editMD(String fileUrl)
    {
        System.out.println("called editMD() - fileUrl: " + fileUrl);

        DoubleMetadataResultType ret = new DoubleMetadataResultType();
        ret.setResult(SeverityType.SUCCESS);

        InputStream is = null;

        try
        {
            if (null == fileUrl)
            {
                throw new DiagnosticException(DiagnosticCodeType.INVALID_ARG, "fileUrl is null");
            }

            URL url = null;

            try
            {
                url = new URL(removeHttpAccept(fileUrl));
            }
            catch (Exception e)
            {
                StringBuilder msg = new StringBuilder("Malformed URL '");
                msg.append(fileUrl);
                msg.append("'.");

                if (null != e.getMessage())
                {
                    msg.append(" ");
                    msg.append(e.getMessage());
                }

                throw new DiagnosticException(DiagnosticCodeType.INVALID_ARG, msg.toString());
            }

            if (null != this.servletContext)
            {
                if (!this.proxyInit)
                {
                    this.proxyHost = this.servletContext.getInitParameter(PARAMETER_PROXY_HOST);
                    if (null != this.proxyHost)
                    {
                        String strProxyPort = this.servletContext.getInitParameter(PARAMETER_PROXY_PORT);
                        if (null != strProxyPort)
                        {
                            this.proxyPort = new Integer(strProxyPort).intValue();
                        }
                    }
                    this.proxyInit = true;
                }
            }

            is = FileUtil.fetchFileFromUrl(url, this.proxyHost, this.proxyPort);
            List<Document> mdDocuments = extractMdElements(is);
            is.close();
            is = null;

            if (mdDocuments.isEmpty())
            {
                throw new DiagnosticException(DiagnosticCodeType.INVALID_ARG, "The remote file does not contain the element '"
                        + MD_Metadata + "' nor element '" + MI_Metadata + "'.");
            }
            
            Map<String, IMdeElement> mdModelMap = new LinkedHashMap<String, IMdeElement>();
            Map<String, IMdeElement> miModelMap = new LinkedHashMap<String, IMdeElement>();

            int i = 0;
            for (Document doc : mdDocuments)
            {
                IMdeElement model = null;

                String metadataName = doc.getDocumentElement().getNodeName();

                if (MD_Metadata.equals(metadataName))
                {
                    model = Model.convert(doc, ModelType.GMD, ModelVersion.Beta, false);
                    mdModelMap.put(createFileName(ModelType.GMD, i++), model);
                }
                else if (MI_Metadata.equals(metadataName))
                {
                    model = Model.convert(doc, ModelType.GMI, ModelVersion.Beta, false);
                    miModelMap.put(createFileName(ModelType.GMI, i++), model);
                }
                else
                {
                    throw new DiagnosticException(DiagnosticCodeType.INVALID_ARG, "Unsupported metadata '" + metadataName + "'.");
                }
            }

            String mdMetadataId = addModelHolder(mdModelMap, ModelType.GMD, ModelVersion.Beta);
            String miMetadataId = addModelHolder(miModelMap, ModelType.GMI, ModelVersion.Beta);

            ret.setMdMetadataId(mdMetadataId);
            ret.setMiMetadataId(miMetadataId);
        }
        catch (DiagnosticException e)
        {
            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(e.getDiagnostic());
        }
        catch (Exception e)
        {
            DiagnosticType diag = new DiagnosticType();
            diag.setCode(DiagnosticCodeType.OTHER_REASON);
            if (null == e.getMessage())
            {
                Throwable t = e.getCause();
                if (null != t && null != t.getMessage())
                {
                    diag.setMesage(t.getMessage());
                }
            }
            else
            {
                diag.setMesage(e.getMessage());
            }

            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(diag);
        }
        finally
        {
            closeStreamSuppressException(is);
        }

        return ret;
    }

    protected static String removeHttpAccept(String url)
    {
        StringBuilder sb = new StringBuilder(url);

        int start = sb.indexOf("httpAccept");
        int end = sb.indexOf("&", start);
        sb.replace(start, end + 1, "");
        
        return sb.toString();
    }

    @Override
    public DownloadMetadataResultType downloadMD(String metadataId)
    {
        return downloadMD(metadataId, true, "/tmp");
    }

    public DownloadMetadataResultType downloadMD(String metadataId, boolean validate)
    {
        return downloadMD(metadataId, validate, "/tmp");
    }

    public DownloadMetadataResultType downloadMD(String metadataId, boolean validate, String path)
    {
        System.out.println("called downloadMD()");

        DownloadMetadataResultType ret = new DownloadMetadataResultType();
        ret.setResult(SeverityType.SUCCESS);

        try
        {
            checkMetadataId(metadataId);

            ModelsHolder modelsHolder = modelsMap.get(metadataId);

            MetadataCotainerType metadataContainer = new MetadataCotainerType();
            metadataContainer.setType(MDEUtil.convert(modelsHolder.getType()));
            metadataContainer.setVersion(MDEUtil.convert(modelsHolder.getVersion()));

            Map<String, IMdeElement> models = modelsHolder.getModels();

            for (Entry<String, IMdeElement> element : models.entrySet())
            {
                String fileName = element.getKey();
                Document doc = Model.convert(element.getValue(), false);
                ModelElementType modelElement = new ModelElementType();
                modelElement.setFileName(fileName);
                modelElement.setAny(doc.getDocumentElement());
                metadataContainer.getMetadata().add(modelElement);
            }

            ret.setMetadataCotainerType(metadataContainer);
            ret.setMetadataId(modelsHolder.getMetadataId());

            JAXBEx.marshal(ret, null, new File("/tmp/out_" + modelsHolder.getMetadataId() + ".xml"));
        }
        catch (Exception e)
        {
            DiagnosticType diag = new DiagnosticType();
            diag.setCode(DiagnosticCodeType.OTHER_REASON);

            String msg = e.getMessage();
            if (null == msg)
            {
                Throwable t = e.getCause();
                if (null != t && null != t.getMessage())
                {
                    msg = t.getMessage();
                }
            }
            diag.setMesage(msg);

            ret.setMetadataCotainerType(null);
            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(diag);
        }

        return ret;
    }

    @Override
    public MetadataResultType closeMD(String metadataId)
    {
        System.out.println("called closeMD()");

        MetadataResultType ret = new MetadataResultType();
        ret.setResult(SeverityType.SUCCESS);
        try
        {
            checkMetadataId(metadataId);

            this.modelsMap.remove(metadataId);
            ret.setMetadataId(metadataId);

            System.out.println("closed '" + metadataId + "'");
        }
        catch (DiagnosticException e)
        {
            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(e.getDiagnostic());
        }
        catch (Exception e)
        {
            DiagnosticType diag = new DiagnosticType();
            diag.setCode(DiagnosticCodeType.OTHER_REASON);
            diag.setMesage(e.getMessage());

            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(diag);
        }

        return ret;
    }

    @Override
    public GetAllowedChildrenResultType getAllowedNodes(String metadataId, String path)
    {
        System.out.println("called getAllowedNodes()");

        GetAllowedChildrenResultType ret = new GetAllowedChildrenResultType();
        ret.setResult(SeverityType.SUCCESS);
        try
        {
            // path = URLDecoder.decode(path, "UTF-8");
            checkMetadataId(metadataId);
            MdePath.check(path);

            List<IMdeElement> elements = getElements(metadataId, path, false);

            int id = 1;

            for (IMdeElement element : elements)
            {
                List<String> creatableChildNames = element.getCreatableChildNames(true);
                Map<String, List<?>> multipleOccurChildren = element.getChildListElements();
                for (int i = 0; i < creatableChildNames.size(); i++)
                {
                    String childName = creatableChildNames.get(i);
                    StringBuilder childNameBuilder = new StringBuilder(childName);
                    if (multipleOccurChildren.containsKey(childName))
                    {
                        List<?> children = multipleOccurChildren.get(childName);
                        if (null != children)
                        {
                            int size = children.size();
                            childNameBuilder.append("[");
                            childNameBuilder.append(size);
                            childNameBuilder.append("]");
                            creatableChildNames.set(i, childNameBuilder.toString());
                        }
                        else
                        {
                            childNameBuilder.append("[0]");
                            creatableChildNames.set(i, childNameBuilder.toString());
                        }
                    }
                }

                ChildNamesListType childNames = new ChildNamesListType();
                childNames.getChildName().addAll(creatableChildNames);

                AllowedChildrenType allowedChildren = new AllowedChildrenType();
                allowedChildren.setMetadataId(String.valueOf(id));
                allowedChildren.setChildNames(childNames);

                ret.getAllowedChildren().add(allowedChildren);

                String allowed[] = creatableChildNames.toArray(new String[0]);
                System.out.println("allowed nodes are '" + Arrays.toString(allowed) + "' at '" + path + "' in '"
                        + metadataId + "(" + id + ")'");
                id++;
            }

            ret.setMetadataId(metadataId);
        }
        catch (DiagnosticException e)
        {
            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(e.getDiagnostic());
        }
        catch (NullPointerException e)
        {
            DiagnosticType diag = new DiagnosticType();
            diag.setCode(DiagnosticCodeType.NULL_PTR);

            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(diag);
        }
        catch (InvalidPathException e)
        {
            DiagnosticType diag = new DiagnosticType();
            diag.setCode(DiagnosticCodeType.INVALID_PATH);
            diag.setMesage(e.getMessage());

            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(diag);
        }
        catch (Exception e)
        {
            DiagnosticType diag = new DiagnosticType();
            diag.setCode(DiagnosticCodeType.OTHER_REASON);
            diag.setMesage(e.getMessage());

            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(diag);
        }

        return ret;
    }

    @Override
    public MetadataResultType insertNode(NodeArgumentType argument)
    {
        System.out.println("called insertNode()");

        MetadataResultType ret = new MetadataResultType();
        ret.setResult(SeverityType.SUCCESS);
        try
        {
            Element domElement = null;
            Integer index = null;

            String nodeName = null;
            if (null != argument.getNodeType())
            {
                domElement = argument.getNodeType().getAny();
                if (null == domElement)
                {
                    throw new DiagnosticException(DiagnosticCodeType.INVALID_ARG, "node's any is null");
                }

                nodeName = domElement.getLocalName();
                index = argument.getIndex();
                if (null != index)
                {
                    nodeName += "[" + index + "]";
                }
            }
            else if (null != argument.getXmlFragment())
            {
                String xmlFragment = argument.getXmlFragment();
                domElement = DomUtil.createElementFromXmlFragment(xmlFragment, true);

                nodeName = domElement.getLocalName();
                index = argument.getIndex();
                if (null != index)
                {
                    nodeName += "[" + index + "]";
                }
            }
            else if (null != argument.getNodeName())
            {
                nodeName = argument.getNodeName();
                index = MdePath.getElementIndex(nodeName);
            }
            else
            {
                throw new DiagnosticException(DiagnosticCodeType.INVALID_ARG,
                        "no NodeName nor nodeTypenode nor xmlFragmet provided");
            }

            MDEUtil.checkNodeName(nodeName);

            // if( null != index && index < 0 )
            // {
            // throw new DiagnosticException(DiagnosticCodeType.INVALID_ARG,
            // "node's index cannot be negative");
            // }

            String nodeNameWithouIndex = nodeName;
            int indexOfBracket = nodeName.indexOf("[");
            if (0 < indexOfBracket)
            {
                nodeNameWithouIndex = nodeName.substring(0, indexOfBracket);
            }

            // GetAllowedChildrenResultType getAllowedChildrenResult =
            // getAllowedNodes(argument.getMetadataId(), argument.getPath());
            //
            // int id = 1;
            // for( AllowedChildrenType allowedChildren :
            // getAllowedChildrenResult.getAllowedChildren() )
            // {
            // boolean insertionAllowed = false;
            //
            // for( String childName :
            // allowedChildren.getChildNames().getChildName() )
            // {
            // if( childName.equals(nodeName) )
            // {
            // insertionAllowed = true;
            // break;
            // }
            // }
            //
            // if( !insertionAllowed )
            // {
            // throw new
            // DiagnosticException(DiagnosticCodeType.UNABLE_TO_INSERT,
            // "Insertion of the '" + nodeNameWithouIndex
            // + "' child node to '" + argument.getPath() + "' is not allowed to
            // the '" + id + "' metadata of the '"
            // + argument.getMetadataId() + "' metadata set.");
            // }
            //
            // id++;
            // }

            String fieldName = null;

            List<IMdeElement> elements = getElements(argument.getMetadataId(), argument.getPath(), false);
            if (elements.isEmpty())
            {
                throw new DiagnosticException(DiagnosticCodeType.INVALID_PATH,
                        "There is not a node at the path '" + argument.getPath() + "'");
            }
            for (IMdeElement element : elements)
            {
                try
                {
                    if (null != domElement)
                    {
                        if (null == fieldName)
                        {
                            fieldName = element.getChildFieldName(nodeNameWithouIndex);
                            if (null == fieldName)
                            {
                                throw new DiagnosticException(DiagnosticCodeType.INVALID_ARG,
                                        "The node '" + nodeNameWithouIndex + "' cannot be inserted to the element '"
                                                + element.getXmlElementName() + "'");
                            }
                        }

                        element.insertChild(domElement, fieldName, index, null);
                    }
                    else
                    {
                        element.createChild(nodeNameWithouIndex, nodeNameWithouIndex, null);
                    }
                }
                catch (Exception e)
                {
                    throw new DiagnosticException(DiagnosticCodeType.UNABLE_TO_INSERT,
                            "Insertion of the '" + nodeNameWithouIndex + "' child node to '" + argument.getPath()
                                    + "' failed due '" + e.getMessage() + "'.");
                }
            }

            ret.setMetadataId(argument.getMetadataId());
            System.out.println("inserted node '" + nodeName + "' at '" + argument.getPath() + "' in '"
                    + argument.getMetadataId() + "'");
        }
        catch (DiagnosticException e)
        {
            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(e.getDiagnostic());
        }
        catch (NullPointerException e)
        {
            DiagnosticType diag = new DiagnosticType();
            diag.setCode(DiagnosticCodeType.NULL_PTR);

            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(diag);
        }
        catch (Exception e)
        {
            DiagnosticType diag = new DiagnosticType();
            diag.setCode(DiagnosticCodeType.OTHER_REASON);
            diag.setMesage(e.getMessage());

            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(diag);
        }

        return ret;
    }

    @Override
    public MetadataResultType deleteNode(String metadataId, String path)
    {
        System.out.println("called deleteNode()");

        MetadataResultType ret = new MetadataResultType();
        ret.setResult(SeverityType.SUCCESS);
        try
        {
            checkMetadataId(metadataId);
            MdePath.check(path);

            List<IMdeElement> children = getElements(metadataId, path, false);

            for (IMdeElement childElement : children)
            {
                IMdeElement parentElement = childElement.getParent();
                if (null != parentElement)
                {
                    if (!childElement.isRequired() || childElement.hasMultipleOccurrences())
                    {
                        parentElement.deleteChild(childElement);
                    }
                    else
                    {
                        throw new DiagnosticException(DiagnosticCodeType.UNABLE_TO_DELETE, "The node at '" + path
                                + "' in '" + metadataId + "' is required and cannot be deleted.");
                    }
                }
            }

            ret.setMetadataId(metadataId);
            System.out.println("deleted node at '" + path + "' in '" + metadataId + "'");
        }
        catch (DiagnosticException e)
        {
            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(e.getDiagnostic());
        }
        catch (NullPointerException e)
        {
            DiagnosticType diag = new DiagnosticType();
            diag.setCode(DiagnosticCodeType.NULL_PTR);

            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(diag);
        }
        catch (InvalidPathException e)
        {
            DiagnosticType diag = new DiagnosticType();
            diag.setCode(DiagnosticCodeType.INVALID_PATH);
            diag.setMesage(e.getMessage());

            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(diag);
        }
        catch (Exception e)
        {
            DiagnosticType diag = new DiagnosticType();
            diag.setCode(DiagnosticCodeType.OTHER_REASON);
            diag.setMesage(e.getMessage());

            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(diag);
        }

        return ret;
    }

    @Override
    public MetadataResultType setNodeValue(NodeValueArgumentType argument)
    {
        System.out.println("called setNodeValue()");

        MetadataResultType ret = new MetadataResultType();
        ret.setResult(SeverityType.SUCCESS);
        try
        {
            checkMetadataId(argument.getMetadataId());
            MdePath.check(argument.getPath());
            MDEUtil.checkValue(argument.getNodeValue());

            List<IMdeElement> elements = getElements(argument.getMetadataId(), argument.getPath(), false);

            for (IMdeElement element : elements)
            {
                if (!element.isValue())
                {
                    throw new DiagnosticException(DiagnosticCodeType.UNABLE_TO_SET_VALUE,
                            "The node at '" + argument.getPath() + "' is not a value node");
                }

                element.setElementValue(argument.getNodeValue());
            }

            ret.setMetadataId(argument.getMetadataId());
            System.out.println("set node value '" + argument.getNodeValue() + "' at '" + argument.getPath() + "' in '"
                    + argument.getMetadataId() + "'");
        }
        catch (DiagnosticException e)
        {
            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(e.getDiagnostic());
        }
        catch (NullPointerException e)
        {
            DiagnosticType diag = new DiagnosticType();
            diag.setCode(DiagnosticCodeType.NULL_PTR);

            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(diag);
        }
        catch (InvalidPathException e)
        {
            DiagnosticType diag = new DiagnosticType();
            diag.setCode(DiagnosticCodeType.INVALID_PATH);
            diag.setMesage(e.getMessage());

            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(diag);
        }
        catch (Exception e)
        {
            DiagnosticType diag = new DiagnosticType();
            diag.setCode(DiagnosticCodeType.UNABLE_TO_SET_VALUE);
            diag.setMesage(e.getMessage());

            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(diag);
        }

        return ret;
    }

    @Override
    public MetadataResultType setNodeAttribute(NodeAttributeArgumentType argument)
    {
        System.out.println("called setNodeAttribute()");

        MetadataResultType ret = new MetadataResultType();
        ret.setResult(SeverityType.SUCCESS);
        try
        {
            checkMetadataId(argument.getMetadataId());
            MdePath.check(argument.getPath());
            MDEUtil.checkAttribute(argument.getAttributeName());
            MDEUtil.checkAttribute(argument.getAttributeValue());

            List<IMdeElement> elements = getElements(argument.getMetadataId(), argument.getPath(), false);

            for (IMdeElement element : elements)
            {
                if (!element.getAttribs().containsKey(argument.getAttributeName()))
                {
                    throw new DiagnosticException(DiagnosticCodeType.UNABLE_TO_SET_ATTRIBUTE,
                            "The node at '" + argument.getPath() + "' doesn't have the '" + argument.getAttributeName()
                                    + "' attribute.");
                }

                element.setAttributeValue(argument.getAttributeName(), argument.getAttributeValue());
            }

            ret.setMetadataId(argument.getMetadataId());
            System.out.println("set node attribute value '" + argument.getAttributeValue() + "' of attribute '"
                    + argument.getAttributeName() + "' at '" + argument.getPath() + "' in '" + argument.getMetadataId()
                    + "'");
        }
        catch (DiagnosticException e)
        {
            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(e.getDiagnostic());
        }
        catch (NullPointerException e)
        {
            DiagnosticType diag = new DiagnosticType();
            diag.setCode(DiagnosticCodeType.NULL_PTR);

            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(diag);
        }
        catch (InvalidPathException e)
        {
            DiagnosticType diag = new DiagnosticType();
            diag.setCode(DiagnosticCodeType.INVALID_PATH);
            diag.setMesage(e.getMessage());

            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(diag);
        }
        catch (Exception e)
        {
            DiagnosticType diag = new DiagnosticType();
            diag.setCode(DiagnosticCodeType.UNABLE_TO_SET_VALUE);
            diag.setMesage(e.getMessage());

            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(diag);
        }

        return ret;
    }

    private void checkMetadataId(String metadataId) throws DiagnosticException
    {
        if (null == metadataId)
        {
            throw new DiagnosticException(DiagnosticCodeType.INVALID_ID, "metadataId is null");
        }

        if (metadataId.isEmpty())
        {
            throw new DiagnosticException(DiagnosticCodeType.INVALID_ID, "metadataId is empty");
        }

        if (!this.modelsMap.containsKey(metadataId))
        {
            throw new DiagnosticException(DiagnosticCodeType.INVALID_ID,
                    "metadata w/ provided metadataId does not exist");
        }
    }

    private List<IMdeElement> getElements(final String metadataId, final String path, final boolean mustExist)
            throws Exception
    {
        List<IMdeElement> ret = new ArrayList<IMdeElement>();

        ModelsHolder metadataHolder = this.modelsMap.get(metadataId);

        Collection<IMdeElement> metadataList = metadataHolder.getModels().values();

        for (IMdeElement metadata : metadataList)
        {
            IMdeElement element = MdePath.findElement(path, metadata, mustExist);
            if (null != element)
            {
                ret.add(element);
            }
        }

        return ret;
    }

    public Map<String, IMdeElement> getModels(final String metadataId)
    {
        Map<String, IMdeElement> ret = null;

        ModelsHolder modelHolder = this.modelsMap.get(metadataId);
        if (null != modelHolder)
        {
            ret = modelHolder.getModels();
        }

        return ret;
    }

    public IMdeElement getModel(final String metadataId, String fileName)
    {
        IMdeElement ret = null;

        Map<String, IMdeElement> models = getModels(metadataId);
        if (null != models)
        {
            ret = models.get(fileName);
        }

        return ret;
    }

    public ModelsHolder getModelsHolder(final String metadataId)
    {
        return this.modelsMap.get(metadataId);
    }

    @Override
    public Response getOSDD(String fileName)
    {
        ResponseBuilder response = null;

        String path = "/tmp/uploads/OSDD/" + fileName;
        File osddFile = new File(path);
        if (osddFile.exists() && osddFile.isFile())
        {
            response = Response.ok((Object) osddFile);
            response.header("Content-Disposition", "attachment; filename=" + osddFile.getName());
        }
        else
        {
            response = Response.status(Status.NOT_FOUND).entity("The file '" + fileName + "' does not exist.")
                    .type(MediaType.TEXT_PLAIN);
        }

        return response.build();
    }

    @Override
    public Response downloadZippedMD(String metadataId)
    {
        System.out.println("called downloadZippedMD()");

        ResponseBuilder response = null;

        File metadataFile = null;
        File zipFile = null;
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        FileInputStream fis = null;

        try
        {
            checkMetadataId(metadataId);

            ModelsHolder modelsHolder = this.modelsMap.get(metadataId);

            StringBuilder zipFileName = new StringBuilder();
            zipFileName.append(modelsHolder.getType().name());
            zipFileName.append("_");
            zipFileName.append(modelsHolder.getVersion());
            zipFileName.append("_");
            zipFileName.append(metadataId);

            File tmpDir = new File(TMP_DIRECTORY);
            tmpDir.mkdirs();

            zipFile = File.createTempFile(zipFileName.toString(), ".zip", tmpDir);

            fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(fos);

            Map<String, IMdeElement> models = modelsHolder.getModels();

            byte[] buffer = new byte[BUFFER_SIZE];

            for (Entry<String, IMdeElement> entry : models.entrySet())
            {
                StringBuilder metadataPathFileName = new StringBuilder();
                metadataPathFileName.append(entry.getKey());
                metadataPathFileName.append(".xml");

                metadataFile = new File(metadataPathFileName.toString());
                Model.save(metadataFile, entry.getValue(), false);

                ZipEntry zipEntry = new ZipEntry(metadataFile.getName());
                zos.putNextEntry(zipEntry);
                fis = new FileInputStream(metadataFile.getName());

                int len;
                while ((len = fis.read(buffer)) > 0)
                {
                    zos.write(buffer, 0, len);
                }

                fis.close();
                fis = null;
                zos.closeEntry();
                metadataFile.delete();
                metadataFile = null;
            }

            zos.close();
            zos = null;
            fos.close();
            fos = null;

            response = Response.ok((Object) zipFile);
            response.header("Content-Disposition", "attachment; filename=" + zipFile.getName());
            zipFile.deleteOnExit();
        }
        catch (Exception e)
        {
            if (null != fis)
            {
                closeStreamSuppressException(fis);
            }
            if (null != zos)
            {
                closeStreamSuppressException(zos);
            }
            if (null != fos)
            {
                closeStreamSuppressException(fos);
            }

            if (null != metadataFile && metadataFile.exists())
            {
                metadataFile.delete();
            }

            if (null != zipFile && zipFile.exists())
            {
                zipFile.delete();
            }

            StringBuilder msg = new StringBuilder("Failed to provide the zipped metadata '");
            msg.append(metadataId);
            msg.append("'.");

            if (null != e.getMessage())
            {
                msg.append(" ");
                msg.append(e.getMessage());
            }

            response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(msg.toString()).type(MediaType.TEXT_PLAIN);
        }

        return response.build();
    }

    private static void closeStreamSuppressException(Object obj)
    {
        try
        {
            if (obj instanceof InputStream)
            {
                ((InputStream) obj).close();
            }
            else if (obj instanceof OutputStream)
            {
                ((OutputStream) obj).close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public BuildingBlockResultType addBuildingBlock(BuildingBlockArgumentType argument)
    {
        BuildingBlockResultType ret = new BuildingBlockResultType();
        ret.setResult(SeverityType.SUCCESS);
        try
        {
            BuildingBlockInfoType info = argument.getBuildingBlockInfoType();
            if (null == info)
            {
                throw new DiagnosticException(DiagnosticCodeType.INVALID_ARG, "Invalid building block info");
            }

            String name = info.getName();
            if (null == name || name.isEmpty())
            {
                throw new DiagnosticException(DiagnosticCodeType.INVALID_ARG, "Invalid building block name");
            }

            String type = info.getType();
            if (null == type || type.isEmpty())
            {
                throw new DiagnosticException(DiagnosticCodeType.INVALID_ARG, "Invalid building block type");
            }

            String body = argument.getBody();
            if (null == body || body.isEmpty())
            {
                throw new DiagnosticException(DiagnosticCodeType.INVALID_ARG, "Invalid building block body");
            }

            AbstractMap.SimpleEntry<String, String> newBuildingBlock = 
                    new AbstractMap.SimpleEntry<String, String>(type, body);
            
            Entry<String, String> oldBuildingBlock = this.buildBlocksMap.put(name, newBuildingBlock);
            if (null != oldBuildingBlock)
            {
                System.out.println("The building block w/ name '" + name + "' has been replaced.");
            }
            else
            {
                System.out.println("The new building block w/ name '" + name + "' has been added.");
            }
        }
        catch (DiagnosticException e)
        {
            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(e.getDiagnostic());
        }
        catch (Exception e)
        {
            DiagnosticType diag = new DiagnosticType();
            diag.setCode(DiagnosticCodeType.OTHER_REASON);
            diag.setMesage(e.getMessage());

            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(diag);
        }

        return ret;
   }

    public GetBuildingBlockInfoListResultType getBuildingBlockInfoList()
    {
        GetBuildingBlockInfoListResultType ret = new GetBuildingBlockInfoListResultType();
        ret.setResult(SeverityType.SUCCESS);
        try
        {
            for (Entry<String, AbstractMap.SimpleEntry<String,String>> entry : this.buildBlocksMap.entrySet())
            {
                BuildingBlockInfoType info = new BuildingBlockInfoType();
                info.setName(entry.getKey());
                info.setType(entry.getValue().getKey());
                ret.getBuildingBlockInfoType().add(info);
            }
        }
        catch (Exception e)
        {
            DiagnosticType diag = new DiagnosticType();
            diag.setCode(DiagnosticCodeType.OTHER_REASON);
            diag.setMesage(e.getMessage());

            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(diag);
        }

        return ret;
    }

    protected static File createBuildingBlockFile(String name, Entry<String, String> buildingBlock) throws IOException
    {
        File ret = null;
        FileWriter fw = null;

        try
        {
            ret = File.createTempFile(name, ".xml");

            fw = new FileWriter(ret);
            fw.write(buildingBlock.getValue());
            fw.close();
            fw = null;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw e;
        }
        finally 
        {
            if (null != fw)
            {
                try
                {
                    fw.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return ret;
    }

    @Override
    public Response downloadBuildingBlock(String name)
    {
        System.out.println("called downloadBuildingBlock()");

        ResponseBuilder response = null;

        Entry<String, String> buildingBlock = this.buildBlocksMap.get(name);
        if (null != buildingBlock)
        {
            try
            {
                File buildingBlockFile = createBuildingBlockFile(name, buildingBlock);
                buildingBlockFile.deleteOnExit();
                response = Response.ok((Object) buildingBlockFile);
                response.header("Content-Disposition", "attachment; filename=" + buildingBlockFile.getName());
            }
            catch (IOException e)
            {
                response = Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error: " + e.getMessage())
                        .type(MediaType.TEXT_PLAIN);
                e.printStackTrace();
            }
        }
        else
        {
            response = Response.status(Status.NOT_FOUND).entity("The building block '" + name + "' does not exist.")
                    .type(MediaType.TEXT_PLAIN);
        }

        return response.build();
    }

    @Override
    public Response downloadBuildingBlocks()
    {
        System.out.println("called downloadBuildingBlocks()");

        ResponseBuilder response = null;

        File buildingBlockFile = null;
        File zipFile = null;
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        FileInputStream fis = null;

        try
        {
            zipFile = File.createTempFile("buildingBlocks", ".zip");

            fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(fos);

            byte[] buffer = new byte[BUFFER_SIZE];

            for (Entry<String, AbstractMap.SimpleEntry<String,String>> entry : this.buildBlocksMap.entrySet())
            {
                StringBuilder pathFileName = new StringBuilder();
                pathFileName.append(entry.getKey());
                pathFileName.append(".xml");

                String suffix = entry.getKey();
                if (suffix.length() < 3)
                {
                    // tmp. file needs at least 3 chars length
                    suffix = "___" + suffix;
                }

                buildingBlockFile = createBuildingBlockFile(suffix, entry.getValue());

                ZipEntry zipEntry = new ZipEntry(pathFileName.toString());
                zos.putNextEntry(zipEntry);
                fis = new FileInputStream(buildingBlockFile);

                int len;
                while ((len = fis.read(buffer)) > 0)
                {
                    zos.write(buffer, 0, len);
                }

                fis.close();
                fis = null;
                zos.closeEntry();
                buildingBlockFile.delete();
                buildingBlockFile = null;
            }

            zos.close();
            zos = null;
            fos.close();
            fos = null;

            response = Response.ok((Object) zipFile);
            response.header("Content-Disposition", "attachment; filename=" + zipFile.getName());
            zipFile.deleteOnExit();
        }
        catch (Exception e)
        {
            if (null != fis)
            {
                closeStreamSuppressException(fis);
            }
            if (null != zos)
            {
                closeStreamSuppressException(zos);
            }
            if (null != fos)
            {
                closeStreamSuppressException(fos);
            }

            if (null != buildingBlockFile && buildingBlockFile.exists())
            {
                buildingBlockFile.delete();
            }

            if (null != zipFile && zipFile.exists())
            {
                zipFile.delete();
            }

            StringBuilder msg = new StringBuilder("Failed to provide the all zipped building block files.");

            if (null != e.getMessage())
            {
                msg.append(" ");
                msg.append(e.getMessage());
            }

            response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(msg.toString()).type(MediaType.TEXT_PLAIN);
        }

        return response.build();
    }

    public BuildingBlockResultType deleteBuildingBlock(String name)
    {
        BuildingBlockResultType ret = new BuildingBlockResultType();
        ret.setResult(SeverityType.SUCCESS);
        try
        {
            if (null == name || name.isEmpty())
            {
                throw new DiagnosticException(DiagnosticCodeType.INVALID_ARG, "Invalid building block name");
            }

            Entry<String, String> removedBuildingBlock = this.buildBlocksMap.remove(name);
            if (null == removedBuildingBlock)
            {
                throw new DiagnosticException(DiagnosticCodeType.INVALID_ARG, "The building block '" 
                        + name + "' does not exist.");
            }
        }
        catch (DiagnosticException e)
        {
            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(e.getDiagnostic());
        }
        catch (Exception e)
        {
            DiagnosticType diag = new DiagnosticType();
            diag.setCode(DiagnosticCodeType.OTHER_REASON);
            diag.setMesage(e.getMessage());

            ret.setResult(SeverityType.FAILURE);
            ret.setDiagnostic(diag);
        }

        return ret;
    }

    public List<Document> extractMdElements(InputStream is) throws SAXException, IOException, ParserConfigurationException
    {
        List<Document> ret = new ArrayList<Document>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
        dbf.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbf.newDocumentBuilder();

        Document doc = dBuilder.parse(new InputSource(is));

        if (null != this.servletContext)
        {
            if (!this.proxyInit)
            {
                this.proxyHost = this.servletContext.getInitParameter(PARAMETER_PROXY_HOST);
                if (null != this.proxyHost)
                {
                    String strProxyPort = this.servletContext.getInitParameter(PARAMETER_PROXY_PORT);
                    if (null != strProxyPort)
                    {
                        this.proxyPort = new Integer(strProxyPort).intValue();
                    }
                }
                this.proxyInit = true;
            }
        }

        NodeList entryElements = doc.getElementsByTagNameNS("http://www.w3.org/2005/Atom", "entry");
        for (int i = 0; i < entryElements.getLength(); i++)
        {
            Element entryElement = (Element) entryElements.item(i);

            /*
             * First try to find a <link> element pointing to Metadata as this should be included by default
             */
            NodeList linkElements = entryElement.getElementsByTagNameNS("http://www.w3.org/2005/Atom", "link");
            String iso19139Link = null;
            String iso19139_2Link = null;
            for (int j = 0; j < linkElements.getLength(); j++)
            {
                Element linkElement = (Element) linkElements.item(j);
                String relAttrValue = linkElement.getAttribute("rel");
                String typeAttributeValue = linkElement.getAttribute("type");
                if (relAttrValue != null && relAttrValue.equals("alternate") && typeAttributeValue != null)
                {
                    switch (typeAttributeValue)
                    {
                    case "application/vnd.iso.19139+xml":
                        iso19139Link = linkElement.getAttribute("href");
                        break;
                    case "application/vnd.iso.19139-2+xml":
                        iso19139_2Link = linkElement.getAttribute("href");
                        break;
                    }
                }
            }
            /* iso19139-2 gets priority */
            String url = iso19139_2Link != null ? iso19139_2Link : iso19139Link;
            if (url != null)
            {
                try (InputStream isLink = FileUtil.fetchFileFromUrl(new URL(url), this.proxyHost, this.proxyPort))
                {
                    Document docLink = dBuilder.parse(new InputSource(isLink));
                    System.out.println("found DOM node (link) :" + docLink.getDocumentElement().getNodeName());
                    ret.add(docLink);
                }
            }
            else
            {
                for (String md : metadata) 
                {
                    NodeList nodeList = doc.getElementsByTagName(md);
                    if (0 < nodeList.getLength())
                    {
                        try
                        {
                            Node node = nodeList.item(0);
                            System.out.println("found DOM node :" + node.getNodeName());
                            Document docLocal = DomUtil.createDocumentFromElement(node);
                            ret.add(docLocal);
                            break;
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        return ret;
    }
}
