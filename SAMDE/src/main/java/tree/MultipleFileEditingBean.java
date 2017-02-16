//------------------------------------------------------------------------------
//
// Project: OBEOS METADATA EDITOR
// Authors: Natascha Neumaerker, Siemens Convergence Creators, Prague (CZ)
//          Milan Novacek, Siemens Convergence Creators, Prague (CZ)
//          Radim Zajonc, Siemens Convergence Creators, Prague (CZ)
//          Stanislav Kascak, Siemens Convergence Creators
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.xml.bind.MarshalException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.primefaces.model.TreeNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.liferay.portal.util.PortalUtil;

import esa.obeos.metadataeditor.common.util.TimeUtil;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.DownloadMetadataResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.MetadataCotainerType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.MetadataResultType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.ModelElementType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.NodeArgumentType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.NodeAttributeArgumentType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.NodeValueArgumentType;
import esa.obeos.metadataeditor.jaxrs.api.datatypes.SeverityType;
import esa.obeos.metadataeditor.jaxrs.client.MetadataEditorJaxRsClient;
import esa.obeos.metadataeditor.jaxrs.service.DiagnosticException;
import esa.obeos.metadataeditor.jaxrs.service.MDEUtil;
import esa.obeos.metadataeditor.model.api.IMdeElement;
import esa.obeos.metadataeditor.model.api.Model;
import esa.obeos.metadataeditor.model.api.ModelType;
import esa.obeos.metadataeditor.model.api.ModelVersion;
import filehandling.FileData;
import filehandling.FileSelectionContainer;
import global.GUIrefs;
import global.Master;
import utilities.FileUtilities;
import utilities.StringUtilities;

@ManagedBean
@SessionScoped
public class MultipleFileEditingBean implements Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = -100144993564695271L;
    public static boolean coeditSelection = true;
    private int selectionSize = 0;

    private List<FileData> selectedFiles = Master.documentsStore.selectedFiles;

    private Map<Path, FileData> fileDataList = Master.documentsStore.collection;

    // Ids of containers containing metadata to be edited
    public static List<String> containerIds = new ArrayList<String>();

    private Map<String, FileSelectionContainer> containerMap = new HashMap<String, FileSelectionContainer>();
    private Map<String, String> downloadLinkMap = new LinkedHashMap<String, String>();

    @ManagedProperty(value = "#{validatorBean}")
    private ValidatorBean validator;

    FileData visualisedFile;
    private String visualisedFileName = "<file being edited>";

    private String nmbrOfFilesBeingEdited = "";

    private String containerLink = "";
    private String containerLinkLabel = "";

    public void reset()
    {

        selectedFiles.clear();
        fileDataList.clear();
        selectionSize = 0;
        containerIds.clear();
        containerMap.clear();
        visualisedFile = null;
        visualisedFileName = "<file being edited>";
        nmbrOfFilesBeingEdited = "";
        GUIrefs.updateComponent(GUIrefs.filesToShow);
    }

    public void propagateFileSelection()
    {

        if (visualisedFile != null)
        {
            this.visualisedFileName = visualisedFile.getDisplayname();
            Master.documentsStore.activeFileId = visualisedFile.getCompletePath();
            this.nmbrOfFilesBeingEdited = "1 file is selected for editing";

            GUIrefs.updateComponent(GUIrefs.nmbrOfFilesBeingEdited);
        }
    }

    public void getVisualFileFromMaster()
    {

        this.visualisedFile = Master.documentsStore.getDisplayFile();
        this.visualisedFileName = visualisedFile.getDisplayname();
    }

    private boolean filetypeCheck()
    {

        ModelType t = this.visualisedFile.getRootType();
        ModelVersion v = this.visualisedFile.getModelVersion();
        boolean consistent = true;
        for (FileData fd : this.selectedFiles)
        {
            if (fd.getRootType() != t || fd.getModelVersion() != v)
            {
                consistent = false;
                break;
            }
        }
        return consistent;
    }

    // creates MD container for visual file without inserting its id into the
    // id-list
    private MetadataCotainerType createContainerForVisualFile() throws Exception
    {

        MetadataCotainerType containerVis = new MetadataCotainerType();

        FileData vfd = Master.documentsStore.getDisplayFile();
        Document doc = Model.convert(vfd.getModelRoot(), false);

        ModelElementType me = new ModelElementType();
        me.setFileName(vfd.getDisplayname());
        me.setAny(doc.getDocumentElement());

        containerVis.getMetadata().add(me);
        containerVis.setType(MDEUtil.convert(vfd.getRootType()));
        containerVis.setVersion(MDEUtil.convert(vfd.getModelVersion()));

        return containerVis;
    }


    private void closeContainers()
    {
        for (String id : this.containerIds)
        {
            Master.service.closeMD(id);
        }
    }

    // creates one single metadata container which does NOT contain the
    // visualised file
    public void createSingleMetadataContainerFromSelection() throws Exception
    {
        // start with consistency check: all files in selection and visualised
        // file
        // need to share the same root type (GMI/GMD)
        if (!filetypeCheck())
        {
            GUIrefs.displayAlert(
                    "Invalid selection: all selected files have to share the same root element (GMI or GMD)");
            this.selectedFiles.clear();
            this.selectedFiles.add(visualisedFile);
            GUIrefs.updateComponent(GUIrefs.fileDropDownList);
            return;
        }

        cancelFileSelection();

        MetadataCotainerType container = new MetadataCotainerType();
        FileSelectionContainer fsContainer = new FileSelectionContainer();
        // contains only FileData objects for MD-objects in container, i.e.
        // without visualised file
        fsContainer.fileSelection = new ArrayList<FileData>();

        boolean fTypeVersionSet = false;

        for (int i = 0; i < selectedFiles.size(); i++)
        {
            FileData fd = selectedFiles.get(i);
            // skip visualisedFile as it has been processed in advance
            if (fd.getCompletePath() == Master.documentsStore.activeFileId)
            {
                continue;
            }
            // this assumes that the DOM in fd is up-to-date
            Element e = fd.getRootNode().getDocumentElement();

            if (!fTypeVersionSet)
            {
                container.setType(MDEUtil.convert(fd.getRootType()));
                container.setVersion(MDEUtil.convert(fd.getModelVersion()));
                fTypeVersionSet = true;
            }

            ModelElementType me = new ModelElementType();
            me.setFileName(fd.getDisplayname());
            me.setAny(e);

            container.getMetadata().add(me);
            fsContainer.fileSelection.add(fd);
        }

        if (!container.getMetadata().isEmpty())
        {
            fsContainer.hasBeenTouched = false;
            MetadataResultType mrt = Master.service.uploadMD(container, false);
            containerIds.add(mrt.getMetadataId());
            fsContainer.id = mrt.getMetadataId();
            containerMap.put(mrt.getMetadataId(), fsContainer);
        }
        else
        {
            // GUIrefs.displayAlert("createSingleMetadataContainerFromSelection:
            // selection contains only 1 file!");
        }

        if (null != containerIds && containerIds.size() > 0)
        {
            for (FileData fd : Master.documentsStore.selectedFiles)
            {
                if (fd.getCompletePath().equals(Master.documentsStore.activeFileId))
                {
                    continue;
                }

                fd.setModelRoot(Master.service.getModel(containerIds.get(0), fd.getDisplayname()));
            }
        }
        org.primefaces.context.RequestContext.getCurrentInstance().execute("loadAnnotationFromResource();");
    }


    public void checkIfDispFileSet()
    {
        System.out.println("CURRENT DISP FILE: " + visualisedFile.getCompletePath());
    }

    public void onChangeDispFile()
    {
        // if display file is not in selection yet add it
        if (!Master.documentsStore.selectedFiles.contains(visualisedFile))
        {
            Master.documentsStore.addToSelection(visualisedFile);
        }
        updateFileDataVisualFile();

        try
        {
            if (null != visualisedFile)
            {
                Master.documentsStore.activeFileId = visualisedFile.getCompletePath();

                createSingleMetadataContainerFromSelection();
            }
        }
        catch (DiagnosticException e)
        {

            GUIrefs.displayAlert(StringUtilities.escapeQuotes(e.getMessage()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            GUIrefs.displayAlert(StringUtilities.escapeQuotes(e.getMessage()));
        }

    }

    // updates FileData object associated to visualised file after selecting a
    // new file as the visualised one, i.e. it converts the model to a DOM and
    // overwrites the temp file with the current version of the model
    private void updateFileDataVisualFile()
    {
        // if in Master.documentsStore no active file is set, there was none
        // before so nothing
        // needs to be written back to temp files
        if (null == Master.documentsStore.activeFileId || null == Master.documentsStore.getDisplayFile())
        {
            return;
        }
        // write Model/DOM back to temp files
        String visualPath = Master.documentsStore.activeFileId.toString();
        FileData previousVisFile = Master.documentsStore.getDisplayFile();

        try
        {
            if (previousVisFile.getModelRoot() == null)
            {
                return;
            }
            Document doc = Model.convert(previousVisFile.getModelRoot(), false);
            previousVisFile.setRootNode(doc);
            FileUtilities.replaceFile(previousVisFile.getModelRoot(), visualPath);
        }
        catch (Exception e)
        {
            String defMsg = "An exception";

            boolean handled = false;
            if (null != previousVisFile.getModelRoot())
            {
                try
                {
                    defMsg = "Failed to convert model '" + previousVisFile.getModelRoot().getXmlElementName()
                            + "' to DOM document";

                    handled = true;
                }
                catch (Exception e1)
                {
                }
            }

            if (!handled)
            {
                defMsg = "Failed to convert a model to DOM document";
            }


            String msg = ValidatorBean.handleMarshalException(e, defMsg);
            String escapedMsg = StringUtilities.escapeQuotes(msg);
            GUIrefs.displayAlert(escapedMsg);
        }


        // String msg = e.getMessage();
        // if (null == msg && e instanceof MarshalException)
        // {
        // Throwable le = ((MarshalException) e).getLinkedException();
        // if (null != le && null != le.getMessage())
        // {
        // msg = le.getMessage();
        // }
        // else
        // {
        // boolean handled = false;
        //
        // if (null != previousVisFile.getModelRoot())
        // {
        // try
        // {
        // msg = "Failed to convert model '" +
        // previousVisFile.getModelRoot().getXmlElementName()
        // + "' to DOM document";
        //
        // handled = true;
        // }
        // catch(Exception e1)
        // {
        // }
        // }
        //
        // if(!handled)
        // {
        // msg = "Failed to convert a model to DOM document";
        // }
        // }
        // }
        // String escapedMsg = StringUtilities.escapeQuotes(msg);
        // GUIrefs.displayAlert(escapedMsg);
        // e.printStackTrace();
        // }
    }


    // updates the DOM versions of the metadata files in the selection and
    // writes the current
    // state of the model back to the temp file
    private void cancelFileSelection() throws ParserConfigurationException, SAXException, IOException
    {

        for (String id : containerIds)
        {
            FileSelectionContainer fsContainer = containerMap.get(id);

            if (null == fsContainer)
            {
                return;
            }

            for (FileData currFD : fsContainer.fileSelection)
            {
                IMdeElement modelRoot = Master.service.getModel(id, currFD.getDisplayname());

                try
                {
                    currFD.setRootNode(null);
                    currFD.setModelRoot(modelRoot);
                    FileUtilities.replaceFile(currFD.getModelRoot(), currFD.getCompletePath().toString());
                }
                catch (Exception e)
                {
                    GUIrefs.displayAlert(StringUtilities.escapeQuotes(e.getMessage()));
                    e.printStackTrace();
                }
            }
        }
        closeContainers();
        containerMap.clear();
        containerIds.clear();
        // added 12.01.
        // Master.documentsStore.selectedFiles.clear();
        // if(Master.documentsStore.getDisplayFile()!=null)
        // {
        // Master.documentsStore.selectedFiles.add(Master.documentsStore.getDisplayFile());
        // }
    }

    // for each metadataContainer in service.modelsMap, insert
    // NodeArgumentType
    // object corresponding to childElt -- EXCLUDE visualisedFile!!
    public void cocreateChildren(IMdeElement childElt, IMdeElement parentElt, String childname)
    {

        try
        {
            // String xmlFragment = childElt.getXmlFragment();
            // get id's from createMetadataContainersFromSelection
            String path = parentElt.getPath();

            for (String id : containerIds)
            {
                NodeArgumentType arg = new NodeArgumentType();
                arg.setMetadataId(id);
                arg.setPath(path);
                // difference child and parent
                // extracts only the child name, potentially including the
                // position [n] among all occurrences
                String nodeName = StringUtilities.difference(parentElt.getPath(), childElt.getPath()).substring(1);
                arg.setNodeName(nodeName);

                MetadataResultType result = Master.service.insertNode(arg);
                String msg = "insertion into container " + id + " was " + result.getResult() + "; ";
                if (null != result.getDiagnostic())
                {
                    msg += result.getDiagnostic().getMesage();
                    GUIrefs.displayAlert(StringUtilities.escapeQuotes(msg));
                }
            }
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void insertNodeInContainers(NodeArgumentType nodeArg)
    {
        try
        {
            // String xmlFragment = childElt.getXmlFragment();
            // get id's from createMetadataContainersFromSelection

            for (String id : containerIds)
            {
                nodeArg.setMetadataId(id);

                MetadataResultType result = Master.service.insertNode(nodeArg);
                String msg = "insertion into container " + id + " was " + result.getResult() + "; ";
                if (null != result.getDiagnostic())
                {
                    msg += result.getDiagnostic().getMesage();
                    GUIrefs.displayAlert(StringUtilities.escapeQuotes(msg));
                }
            }
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void codeleteChildren(IMdeElement childElt, IMdeElement parentElt)
    {
        try
        {
            String path = parentElt.getPath();

            for (String id : containerIds)
            {
                MetadataResultType result = Master.service.deleteNode(id, path);
                String msg = "deletion in container " + id + " was " + result.getResult() + "; ";
                if (null != result.getDiagnostic())
                {
                    msg += result.getDiagnostic().getMesage();
                    GUIrefs.displayAlert(StringUtilities.escapeQuotes(msg));
                }
            }
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void coinsertXmlFragment(IMdeElement parentElt, String childname, String xmlFragment)
    {

        try
        {
            // get id's from createMetadataContainersFromSelection
            String path = parentElt.getPath();

            for (String id : containerIds)
            {
                NodeArgumentType arg = new NodeArgumentType();
                arg.setMetadataId(id);
                arg.setPath(path);
                arg.setXmlFragment(xmlFragment);
                arg.setNodeName(childname);// necessary?

                MetadataResultType result = Master.service.insertNode(arg);
                String msg = "insertion into container " + id + " was " + result.getResult() + "; ";
                if (null != result.getDiagnostic())
                {
                    msg += result.getDiagnostic().getMesage();
                    GUIrefs.displayAlert(StringUtilities.escapeQuotes(msg));
                }
            }

        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void coeditNodeValue(String path, String value)
    {

        try
        {
            // String path = mdeElt.getPath();

            for (String id : containerIds)
            {
                NodeValueArgumentType argument = new NodeValueArgumentType();
                argument.setMetadataId(id);
                argument.setPath(path);
                argument.setNodeValue(value);

                MetadataResultType result = Master.service.setNodeValue(argument);
                String msg = "setting value " + value + " in container " + id + " was " + result.getResult() + "; ";
                if (null != result.getDiagnostic())
                {
                    msg += result.getDiagnostic().getMesage();
                    GUIrefs.displayAlert(StringUtilities.escapeQuotes(msg));
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void coeditAttributeValue(IMdeElement mdeElt, String name, String value)
    {

        try
        {
            String path = mdeElt.getPath();

            for (String id : containerIds)
            {
                NodeAttributeArgumentType argument = new NodeAttributeArgumentType();
                argument.setMetadataId(id);
                argument.setPath(path);
                argument.setAttributeName(name);
                argument.setAttributeValue(value);

                MetadataResultType result = Master.service.setNodeAttribute(argument);

                String msg = "setting value" + value + "in container " + id + " was " + result.getResult() + "; ";
                if (null != result.getDiagnostic())
                {
                    msg += result.getDiagnostic().getMesage();
                    GUIrefs.displayAlert(StringUtilities.escapeQuotes(msg));
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void onValidate()
    {

        validator.setContainerIds(containerIds);
        validator.setModelRoot(visualisedFile.getModelRoot());
    }

    // sets /MI_Metadata/dateStamp
    // or /MD_Metadata/dateStamp
    public void coSetDateStamp()
    {
        for (FileData fd : Master.documentsStore.selectedFiles)
        {
            if (fd.getCompletePath().equals(Master.documentsStore.activeFileId))
            {
                continue;
            }
            IMdeElement root = fd.getModelRoot();
            try
            {

                Map<String, IMdeElement> childmap = root.getChildElements();
                IMdeElement dateStampElt = childmap.get("dateStamp");

                if (null == dateStampElt)
                {
                    dateStampElt = root.createChild("dateStamp", null, null);
                }
                IMdeElement dateTimeElt = dateStampElt.getChildByXmlName("DateTime");
                if (null == dateTimeElt)
                {
                    dateTimeElt = dateStampElt.getChildByXmlName("Date");
                }
                if (null == dateTimeElt)
                {
                    dateTimeElt = dateStampElt.createChild("dateOrDateTime", "DateTime", null);
                }

                if (dateTimeElt.getXmlElementName().equals("DateTime"))
                {
                    dateTimeElt.setElementValue(TimeUtil.convert(new Date()));
                }
                else
                {
                    Calendar c = Calendar.getInstance();
                    Date d = c.getTime();
                    SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");

                    dateTimeElt.setElementValue(f.format(d));
                }
            }
            catch (Exception e)
            {
                Master.errorBean.addMessage(fd.getDisplayname(), e.getMessage());
            }
        }
    }


    // String path = "";
    // ModelType t = this.visualisedFile.getRootType();
    // if (t == ModelType.GMI)
    // {
    // path = "/MI_Metadata/dateStamp/DateTime";
    // }
    // else if (t == ModelType.GMD)
    // {
    // path = "/MD_Metadata/dateStamp/DateTime";
    // }
    // String contents = TimeUtil.convert(new Date()).toString();
    // coeditNodeValue(path, contents);

    // }

    private void addContainerToRest(MetadataEditorJaxRsClient client, MetadataCotainerType container)
    {
        try
        {
            try
            {
                MetadataResultType resultType = client.uploadMD(container);
                if (SeverityType.SUCCESS == resultType.getResult())
                {
                    int port = PortalUtil.getPortalPort(false);

                    containerLink = "http://localhost:" + port + "/SAMDE-0.1.0/rest/mde/downloadZippedMD/"
                            + resultType.getMetadataId();
                    containerLinkLabel = "MD_container_" + resultType.getMetadataId();
                    this.downloadLinkMap.put(containerLinkLabel, containerLink);
                }
                else
                {
                    String msg = resultType.getDiagnostic().getMesage();
                    GUIrefs.displayAlert("Error in trying to download container; " + StringUtilities.escapeQuotes(msg));
                }
            }
            catch (Exception e)
            {
                GUIrefs.displayAlert(StringUtilities.escapeQuotes(e.getMessage()));
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        GUIrefs.updateComponent("downloadForm");

    }

    public void saveActiveFile() throws FileNotFoundException, IOException, TransformerException
    {

        downloadLinkMap.clear();

        try
        {
            updateFileDataVisualFile();

            MetadataEditorJaxRsClient client = new MetadataEditorJaxRsClient("localhost",
                    PortalUtil.getPortalPort(false), "/SAMDE-0.1.0/rest/mde");

            MetadataCotainerType container = createContainerForVisualFile();
            addContainerToRest(client, container);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        GUIrefs.updateComponent("downloadForm");
    }

    public void saveAllContainersAndActiveFile()
    {

        // remove links to prior downloads
        downloadLinkMap.clear();
        updateFileDataVisualFile();

        try
        {
            MetadataEditorJaxRsClient client = new MetadataEditorJaxRsClient("localhost",
                    PortalUtil.getPortalPort(false), "/SAMDE-0.1.0/rest/mde");

            boolean fAdded = false;
            for (String id : containerIds)
            {
                DownloadMetadataResultType downloadResultType = Master.service.downloadMD(id, false);
                if (SeverityType.SUCCESS == downloadResultType.getResult())
                {
                    MetadataCotainerType container = downloadResultType.getMetadataCotainerType();
                    if (!fAdded)
                    {
                        // add visualised file to container
                        ModelElementType me = new ModelElementType();
                        me.setFileName(visualisedFile.getDisplayname());
                        me.setAny(visualisedFile.getRootNode().getDocumentElement());

                        container.getMetadata().add(me);

                        fAdded = true;
                    }


                    addContainerToRest(client, container);
                }
                else
                {
                    if (null != downloadResultType && null != downloadResultType.getDiagnostic())
                    {
                        String msg = StringUtilities.escapeQuotes(downloadResultType.getDiagnostic().getMesage());
                        GUIrefs.displayAlert(msg);
                    }
                }
            }
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        GUIrefs.updateComponent("downloadForm");
        GUIrefs.updateComponent("uploadToCatalogForm");

    }

    // TODO: test!!
    public void unloadSelection()
    {
        for (FileData fd : this.selectedFiles)
        {
            this.fileDataList.remove(fd.getCompletePath());
            fd.setModelRoot(null);
            fd.setRootNode(null);
            try
            {
                Files.delete(fd.getCompletePath());
            }
            catch (Exception e)
            {
                System.out.println("MultipleFileEditingBean.unloadSelection: file to be deleted is not there!");
            }

        }
        this.selectedFiles.clear();
        GUIrefs.updateComponent(GUIrefs.fileDropDownList);
        GUIrefs.updateComponent("selectionSizeIndicator");
        org.primefaces.context.RequestContext.getCurrentInstance().execute("PF('fileDispDlg').show();");
    }

    public void unloadFile(String filename)
    {

    }

    // setters and getters

    public boolean isCoeditSelection()
    {

        return coeditSelection;
    }

    public void setCoeditSelection(boolean coeditSelection)
    {

        this.coeditSelection = coeditSelection;
    }

    public List<String> getContainerIds()
    {

        return containerIds;
    }

    public void setContainerIds(List<String> containerIds)
    {

        this.containerIds = containerIds;
    }

    public Map<String, String> getDownloadLinkMap()
    {

        return downloadLinkMap;
    }

    public void setDownloadLinkMap(Map<String, String> downloadLinkMap)
    {

        this.downloadLinkMap = downloadLinkMap;
    }

    public String getNmbrOfFilesBeingEdited()
    {

        String be = selectedFiles.size() > 1 ? "are" : "is";
        String files = selectedFiles.size() > 1 ? "files" : "file";
        nmbrOfFilesBeingEdited = this.visualisedFile == null ? ""
                : selectedFiles.size() + " " + files + " " + be + " selected for editing.";
        return nmbrOfFilesBeingEdited;
    }

    public void setNmbrOfFilesBeingEdited(String nmbrOfFilesBeingEdited)
    {

        this.nmbrOfFilesBeingEdited = nmbrOfFilesBeingEdited;
    }

    public ValidatorBean getValidator()
    {

        return validator;
    }

    public void setValidator(ValidatorBean validator)
    {

        this.validator = validator;
    }

    public FileData getVisualisedFile()
    {

        return visualisedFile;
    }

    public void setVisualisedFile(FileData visualisedFile)
    {

        this.visualisedFile = visualisedFile;
        // Master.documentsStore.addToSelection(visualisedFile);
    }

    public String getVisualisedFileName()
    {

        if (visualisedFile != null)
        {
            this.visualisedFileName = this.visualisedFile.getDisplayname();
        }
        return visualisedFileName;
    }

    public void setVisualisedFileName(String visualisedFileName)
    {

        this.visualisedFileName = visualisedFileName;
    }

    public String getContainerLink()
    {

        return containerLink;
    }

    public void setContainerLink(String containerLink)
    {

        this.containerLink = containerLink;
    }

    public String getContainerLinkLabel()
    {

        return containerLinkLabel;
    }

    public void setContainerLinkLabel(String linkString)
    {

        this.containerLinkLabel = linkString;
    }

    public Map<Path, FileData> getFileDataList()
    {

        return fileDataList;
    }

    public void setFileDataList(Map<Path, FileData> fileDataList)
    {

        this.fileDataList = fileDataList;
    }

    public List<FileData> getSelectedFiles()
    {

        return selectedFiles;
    }

    public void setSelectedFiles(List<FileData> selectedFiles)
    {
        this.selectedFiles = selectedFiles;
        Master.documentsStore.selectedFiles = selectedFiles;

        @SuppressWarnings("unused")
        int diff = this.selectedFiles.size() - Master.documentsStore.selectedFiles.size();
    }

    public int getSelectionSize()
    {

        return selectionSize;
    }


    public void uploadToCatalog()
    {
        Map<FileData, Exception> errors = new HashMap<>();

        for (FileData fd : selectedFiles)
        {
            try
            {
                URL uploadUrl = new URL(fd.getUploadUrl());
                if (uploadUrl.getProtocol().equals("http"))
                {
                    HttpURLConnection connection = (HttpURLConnection) uploadUrl.openConnection();
                    connection.setDoOutput(true);

                    /*
                     * It was agreed with Spacebel that file update will happen
                     * with Content-Type: text/xml
                     */
                    connection.setRequestProperty("Content-Type", "text/xml");

                    /*
                     * At the moment it is expected that user will enter
                     * username/password as part of url, i.e. in form
                     * http://<username>:<password>@url. If it should be entered
                     * from configuration in future, this is how to enter it
                     * using basic authentication.
                     */
                    // String basicAuth =
                    // Base64.getEncoder().encodeToString((username + ":" +
                    // password).getBytes());
                    // connection.setRequestProperty("authorization", "Basic " +
                    // basicAuth);

                    /*
                     * It was agreed with Spacebel that file update will happen
                     * using POST
                     */
                    connection.setRequestMethod("POST");
                    connection.connect();
                    try (OutputStream os = connection.getOutputStream())
                    {
                        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(fd.getRootNode()),
                                new StreamResult(os));
                        os.flush();
                        os.close();
                        if (!(connection.getResponseCode() >= 200 && connection.getResponseCode() < 300))
                        {
                            throw new IOException("Server returned " + connection.getResponseCode() + " - "
                                    + connection.getResponseMessage());
                        }

                        if (Master.DEBUG_LEVEL > Master.NONE)
                        {
                            System.out.println("Metadata written to: " + uploadUrl + " with response "
                                    + connection.getResponseCode() + " - " + connection.getResponseMessage());
                        }
                    }
                }
                else if (uploadUrl.getProtocol().equals("file"))
                {
                    try (OutputStream os = new FileOutputStream(new File(uploadUrl.toURI())))
                    {
                        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(fd.getRootNode()),
                                new StreamResult(os));
                    }
                }
                else
                {
                    throw new IllegalArgumentException("Unsupported url type: " + fd.getUploadUrl());
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                errors.put(fd, e);
            }
        }

        if (!errors.isEmpty())
        {
            StringBuilder sb = new StringBuilder();
            for (Entry<FileData, Exception> entry : errors.entrySet())
            {
                if (sb.length() != 0)
                {
                    sb.append("\\n");
                }
                sb.append(entry.getKey().getDisplayname());
                sb.append('-');
                sb.append(entry.getValue().getMessage());
            }
            GUIrefs.displayAlert("Some uploads failed\\n" + sb.toString());
        }
        else
        {
            GUIrefs.displayAlert("All uploads successfull");
        }
    }
    // debugging
//    public void dummyMethod()
//    {
//
//        System.out.println("@@@@@@@@@@@@@@@@@@@MultipleFileEditingBean.dummyMethod called!-- to show:"
//                + visualisedFile.getFilename());
//    }

    public void printSelectionList()
    {

        for (FileData fd : selectedFiles)
        {
            System.out.println(fd.getDisplayname() + ";" + fd.getCompletePath());
        }
    }


}
