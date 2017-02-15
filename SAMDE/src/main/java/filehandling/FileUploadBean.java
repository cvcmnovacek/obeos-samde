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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.apache.commons.io.FilenameUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import global.GUIrefs;
import global.GlobalParameters;
import global.Master;

@ManagedBean
@SessionScoped
public class FileUploadBean implements Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = 5559515122453343085L;

    // The map 'fileDataList' and the list 'selectedFiles' are local copies that
    // differ from the global ones in that the visualised file is not a member
    // of the local list as it is
    // processed separately as far as the graphical display is concerned

    private boolean uploadEnabled = true;

    private boolean uploadedSth = false;

    // private boolean validateOnUpload = true;

    private boolean fireOpenEvent = true;

    private byte[] byteArray;

    // uploadedFile in single upload mode
    private UploadedFile singleFile;

    @PostConstruct
    public void init()
    {
        setValidateOnUpload(true);
    }

    public void fileUploadListener(FileUploadEvent e)
    {

        UploadedFile file = e.getFile();

        if (Master.DEBUG_LEVEL > Master.LOW)
            System.out.println("Uploaded file is " + file.getFileName() + " (" + file.getSize() + ")");

        String filename = FilenameUtils.getBaseName(file.getFileName());
        String extension = FilenameUtils.getExtension(file.getFileName());
        extension = extension.equals("") ? "xml" : extension;

        byte[] bytes = file.getContents();
        List<FileData> result = FilePreprocessor.extractMDFilesFromXMLEmbedding(bytes, filename, extension);
        // this.addFilesToDisplayList(result); //!!!!!
        this.uploadedSth = true;
        if (fireOpenEvent)
        {
            StringBuilder sb = new StringBuilder();
            for (FileData fd : result)
            {
                if (sb.length() != 0)
                {
                    sb.append(',');
                }
                sb.append(fd.getDisplayname());
            }
            sb.insert(0, "fireBasicEvent(\'MDEuploadedMD\' ,{filename:\'[");
            sb.append("]\'});");
            String command = sb.toString();

            if (Master.DEBUG_LEVEL > Master.LOW)
                System.out.println(command);
            RequestContext.getCurrentInstance().execute(command);
        }


        RequestContext.getCurrentInstance().update(GUIrefs.getClientId(GUIrefs.fileDispDlg));
        GUIrefs.updateComponent(GUIrefs.filesToShow);
        // String command = "doFireEvent(\'file upload event\',\'" +
        // file.getFileName() + "\' );";
        // RequestContext.getCurrentInstance().execute(command);
    }

    public void fileUploadToByteArray(FileUploadEvent e)
    {

        UploadedFile file = e.getFile();

        try
        {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            org.apache.commons.io.IOUtils.copy(file.getInputstream(), baos);
            this.byteArray = baos.toByteArray();

        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }

    }

    public void update()
    {
        // RequestContext.getCurrentInstance().update(GUIrefs.getClientId(GUIrefs.fileDispDlg));
        String executionStr = GUIrefs.wvFileDispDlg + ".show();";
        RequestContext.getCurrentInstance().execute(executionStr);
    }

    // public void singleUpload()
    // {
    //
    // GUIrefs.displayAlert("FileUploadBean.singleUpload");
    // if (null != singleFile)
    // {
    // System.out.println("uploaded file " + singleFile.getFileName() + "(" +
    // singleFile.getSize() + ")");
    // GUIrefs.displayAlert("uploaded file " + singleFile.getFileName() + "(" +
    // singleFile.getSize() + ")");
    // }
    // else
    // {
    // GUIrefs.displayAlert("uploaded file is still null");
    // }
    // }


    public byte[] getByteArray()
    {

        return byteArray;
    }

    public void setByteArray(byte[] byteArray)
    {

        this.byteArray = byteArray;
    }

    public UploadedFile getSingleFile()
    {

        return singleFile;
    }

    public void setSingleFile(UploadedFile singleFile)
    {

        this.singleFile = singleFile;
    }

    public boolean isValidateOnUpload()
    {

        // return validateOnUpload;
        return GlobalParameters.validateOnBuild;
    }

    public void setValidateOnUpload(boolean validateOnUpload)
    {

        // this.validateOnUpload = validateOnUpload;
        GlobalParameters.validateOnBuild = validateOnUpload;
    }

    public void reset()
    {

        this.uploadEnabled = true;
    }

    // setters and getters
    public boolean isUploadEnabled()
    {

        return uploadEnabled;
    }

    public void setUploadEnabled(boolean uploadEnabled)
    {

        this.uploadEnabled = uploadEnabled;
    }

    public boolean isUploadedSth()
    {

        return uploadedSth;
    }

    public void setUploadedSth(boolean uploadedSth)
    {

        this.uploadedSth = uploadedSth;
    }

    // debugging
    public void printGlobalSelectedFiles()
    {

        System.out.println("globally selected files: ");
        if (Master.documentsStore.selectedFiles.size() > 0)
        {
            for (FileData f : Master.documentsStore.selectedFiles)
            {
                System.out.println(f.getFilename());
            }
        }
        else
        {
            System.out.println("no file selected");
        }
    }

    public void printGlobalFileMap()
    {

        System.out.println("FileUploadBean.printGlobalFileMap():");
        for (Entry<Path, FileData> e : Master.documentsStore.collection.entrySet())
        {
            System.out.println(e.getKey());
        }
    }

    public void onCompletion()
    {

        if (Master.DEBUG_LEVEL > Master.LOW)
        {
            System.out.println("FileUploadBean.onCompletion");
            printGlobalFileMap();
        }
    }

}
