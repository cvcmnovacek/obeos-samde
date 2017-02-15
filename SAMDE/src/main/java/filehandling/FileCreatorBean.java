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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.xml.parsers.ParserConfigurationException;

import org.primefaces.context.RequestContext;
import org.primefaces.model.UploadedFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import esa.obeos.metadataeditor.jaxrs.service.DiagnosticException;
import esa.obeos.metadataeditor.model.api.IMdeElement;
import esa.obeos.metadataeditor.model.api.Model;
import esa.obeos.metadataeditor.model.api.ModelType;
import esa.obeos.metadataeditor.model.api.ModelVersion;
import global.GUIrefs;
import global.Master;
import global.ServerConstants;
import global.ServerSideTemplates;
import utilities.DOMUtilities;
import utilities.StreamUtilities;
import utilities.StringUtilities;

@ManagedBean
@ViewScoped
public class FileCreatorBean implements Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = -6965800042411364936L;
    private ModelType modelType;
    private ModelVersion modelVersion;
    public final ModelType gmiType = ModelType.GMI;
    public final ModelType gmdType = ModelType.GMD;
    public final ModelVersion betaVersion = ModelVersion.Beta;
    public final ModelVersion releaseVersion = ModelVersion.Rel2015;
    // (path on server , template description)
    private Map<String, String> predefinedTemplates;
    // nmbr of templates that will be offered
    private int nmbrTemplates;
    // user variable of number of files to generate
    int numberToGenerate;
    private String pathChosenTemplate = "";
    private String givenFileName = "";
    private String setId;
    private String text = "";
    private boolean newFileAsDisplay = true;
    private boolean validTemplate = false;
    private UploadedFile uploadedTemplate = null;

    public void reset()
    {
        uploadedTemplate = null;
    }

    @PostConstruct
    public void init()
    {

        predefinedTemplates = ServerSideTemplates.availableTemplates;
        nmbrTemplates = predefinedTemplates.size();
    }


    // public void createFileSeries()
    @SuppressWarnings("unused")
    private FileData createFileSeriesOfEmptyFiles()
    {

        // debug
        if (Master.DEBUG_LEVEL > Master.LOW)
        {
            printParams();
            System.out.println(
                    "FileCreatorBean.createFileSeriesOfEmptyFiles() - create " + getNumberToGenerate() + " new files");
        }

        // create first model in ModelsHolder container
        // MetadataResultType result01;
        FileData visualFD = null;
        try
        {

            byte[] byteArr = null;
            List<Document> domlist = null;
            for (int i = 0; i < numberToGenerate; i++)
            {
                IMdeElement model = Model.create(modelType, modelVersion);
                String pathNewFile = ServerConstants.PATH_TO_UPLOADS + "/" + givenFileName + "_" + i;

                if (i == 0)
                {
                    Model.save(new File(pathNewFile), model, false);
                    byteArr = StreamUtilities.inputStreamToByteArray(new FileInputStream(pathNewFile));
                    domlist = DOMUtilities.domsFromByteArray(byteArr, numberToGenerate);
                }

                FileData fd = Master.documentsStore.createAndAddNewTempfile(new ByteArrayInputStream(byteArr),
                        givenFileName + "_" + i, "xml", false);
                fd.setModelRoot(model);
                fd.setRootNode(domlist.get(i));
                fd.setModelVersion(this.modelVersion);

                if (i == 0)
                {
                    visualFD = fd;
                }

            }
        }
        catch (DiagnosticException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return visualFD;
    }

    public void getAvailableTemplates()
    {

        this.predefinedTemplates = ServerSideTemplates.getAvailableTemplates(modelType, modelVersion);
        this.nmbrTemplates = predefinedTemplates.size();
        GUIrefs.updateComponent("templatelist");
    }

    private FileData createTemplateSeriesFromPath()
    {

        FileData visualFD = null;
        try
        {
            byte[] byteArr = StreamUtilities.inputStreamToByteArray(new FileInputStream(pathChosenTemplate));
            visualFD = createTemplateSeriesFromBytes(byteArr, false);
        }
        catch (IOException e)
        {
            GUIrefs.displayAlert("Error on creation of file series.");

        }
        return visualFD;

    }

    public void replaceSelectedFilesByTextfieldContents()
    {
        this.numberToGenerate = Master.documentsStore.selectedFiles.size();
        this.validTemplate = false;
        byte[] bytes = this.text.getBytes();
        Master.documentsStore.selectedFiles.clear();
        createTemplateSeriesFromBytes(bytes, true);
    }

    private FileData createTemplateSeriesFromBytes(byte[] byteArr, boolean addToSelectedFiles)
    {

        // copy relevant data from visualised file
        // this.modelType =
        // Master.documentsStore.getDisplayFile().getRootType();
        // this.modelVersion =
        // Master.documentsStore.getDisplayFile().getModelVersion();
        // String s = Master.documentsStore.getDisplayFile().getDisplayname();
        // this.givenFileName = s.substring(0, s.indexOf('_'));
        FileData visualFD = null;
        // GUIrefs.updateComponent();

        try
        {

            for (int i = 0; i < numberToGenerate; i++)
            {

                FileData fd = Master.documentsStore.createAndAddNewTempfile(
                        // FileUtils.openInputStream(new
                        // File(pathChosenTemplate))
                        new ByteArrayInputStream(byteArr), givenFileName + "_" + i, "xml", true);

                IMdeElement model = Model.load(new ByteArrayInputStream(byteArr), modelType, modelVersion,
                        validTemplate);
                fd.setModelRoot(model);

                fd.setRootNode(Model.convert(model, false));

                fd.setModelVersion(this.modelVersion);

                if (i == 0)
                {
                    visualFD = fd;
                }
                if (addToSelectedFiles)
                {
                    Master.documentsStore.selectedFiles.add(fd);
                }

            }
        }
        catch (ParserConfigurationException e)
        {
            GUIrefs.displayAlert(StringUtilities.escapeQuotes(e.getMessage()));
            e.printStackTrace();
        }
        catch (IOException e2)
        {
            GUIrefs.displayAlert(StringUtilities.escapeQuotes(e2.getMessage()));
            e2.printStackTrace();
        }
        catch (SAXException e3)
        {
            GUIrefs.displayAlert(StringUtilities.escapeQuotes(e3.getMessage()));
            e3.printStackTrace();

        }
        catch (Exception e4)
        {
            GUIrefs.displayAlert(StringUtilities.escapeQuotes(e4.getMessage()));
            e4.printStackTrace();
        }

        return visualFD;
    }

    public void createFileSeries()
    {

        FileData visualFD = null;

        if (!this.pathChosenTemplate.equals(""))
        {
            visualFD = createTemplateSeriesFromPath();
            pathChosenTemplate = "";

        }
        else if (null != this.uploadedTemplate)
        {
            try
            {
                byte[] byteArr = StreamUtilities.inputStreamToByteArray(uploadedTemplate.getInputstream());
                visualFD = createTemplateSeriesFromBytes(byteArr, false);
                uploadedTemplate = null;
            }
            catch (IOException e)
            {
                GUIrefs.displayAlert(
                        "Error on reading from uploaded template:" + StringUtilities.escapeQuotes(e.getMessage()));
                e.printStackTrace();
            }
        }
        else
        {
            visualFD = createFileSeriesOfEmptyFiles();
        }

        if (newFileAsDisplay)
        {
            Master.documentsStore.activeFileId = visualFD.getCompletePath();
            Master.documentsStore.selectedFiles.add(visualFD);
        }
        GUIrefs.updateComponent("selectionPanel");
        GUIrefs.updateComponent("containerNmbrPanel");
        GUIrefs.updateComponent("filetoshowlist");

    }

    public void info()
    {
        GUIrefs.displayAlert("uploadedTemplate=" + uploadedTemplate.getFileName() + "\n" + "template path= "
                + this.pathChosenTemplate + "\n");
        RequestContext.getCurrentInstance().execute("PF('namingNewFileDlg').show();");
    }

    // setters and getters

    public boolean isNewFileAsDisplay()
    {

        return newFileAsDisplay;
    }

    public void setNewFileAsDisplay(boolean newFileAsDisplay)
    {

        this.newFileAsDisplay = newFileAsDisplay;
    }

    public boolean isValidTemplate()
    {

        return validTemplate;
    }

    public void setValidTemplate(boolean validTemplate)
    {

        this.validTemplate = validTemplate;
    }

    public ModelType getModelType()
    {

        return modelType;
    }

    public void setModelType(ModelType modelType)
    {

        this.modelType = modelType;
    }

    public ModelVersion getModelVersion()
    {

        return modelVersion;
    }

    public void setModelVersion(ModelVersion modelVersion)
    {

        this.modelVersion = modelVersion;
    }

    public int getNmbrTemplates()
    {

        return nmbrTemplates;
    }

    public String getText()
    {

        return text;
    }

    public void setText(String textrepresentation)
    {

        this.text = textrepresentation;
    }

    public ModelType getGmiType()
    {

        return gmiType;
    }

    public ModelType getGmdType()
    {

        return gmdType;
    }

    public ModelVersion getBetaVersion()
    {

        return betaVersion;
    }

    public ModelVersion getReleaseVersion()
    {

        return releaseVersion;
    }

    public Map<String, String> getPredefinedTemplates()
    {

        return predefinedTemplates;
    }

    public void setPredefinedTemplates(Map<String, String> predefinedTemplates)
    {

        this.predefinedTemplates = predefinedTemplates;
    }

    public String getPathChosenTemplate()
    {

        return pathChosenTemplate;
    }

    public void setPathChosenTemplate(String pathChosenTemplate)
    {

        this.pathChosenTemplate = pathChosenTemplate;

        // try
        // {
        // setNewDisplayFile();
        // }
        // catch (FileNotFoundException e)
        // {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // catch (IOException e)
        // {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
    }

    public String getGivenFileName()
    {

        return givenFileName;
    }

    public void setGivenFileName(String givenFileName)
    {

        this.givenFileName = givenFileName;
    }

    public int getNumberToGenerate()
    {

        return numberToGenerate;
    }

    public void setNumberToGenerate(int numberToGenerate)
    {

        this.numberToGenerate = numberToGenerate;
    }

    public String getSetId()
    {

        return setId;
    }

    public UploadedFile getUploadedTemplate()
    {

        return uploadedTemplate;
    }

    public void setUploadedTemplate(UploadedFile uploadedTemplate)
    {

        this.uploadedTemplate = uploadedTemplate;
    }

    public void setSetId(String setId)
    {

        this.setId = setId;
    }

    // debugging
    public void printParams()
    {

        String type = "";
        String version = "";
        if (modelType == ModelType.GMD)
        {
            type = "GMD";
        }
        else if (modelType == ModelType.GMI)
        {
            type = "GMI";
        }
        if (modelVersion == ModelVersion.Beta)
        {
            version = "Beta";
        }
        else if (modelVersion == ModelVersion.Rel2015)
        {
            version = "Release";
        }
        System.out.println("FileCreatorBean.printParams(): ModelType= " + type + "; version= " + version);
        String msg = "Creating new file of type " + type + ", schema set version " + version;
        // GUIrefs.displayAlert(msg);

    }

}
