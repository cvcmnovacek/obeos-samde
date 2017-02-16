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

package filehandling;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.portlet.PortletResponse;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.w3c.dom.Node;

import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;
import com.liferay.portal.util.PortalUtil;

import esa.obeos.metadataeditor.model.api.Model;
import esa.obeos.metadataeditor.model.api.IMdeElement;
import global.ConfigurationConstants;
import global.GUIrefs;
import global.Master;
//import resource.TestExportResource;
//import resource.TestWrapper;
import utilities.DOMUtilities;
import utilities.StringUtilities;

@ManagedBean
@SessionScoped
public class FileDownloadBean implements Serializable {

	// Logger
	//private static final Logger logger = LoggerFactory.getLogger(TestExportResource.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 2567589928207268078L;
	private boolean validateBeforeSave = true;
	//private boolean automaticUpdateRevisionDate = true;
	private StreamedContent file;

	private String tempFilePath;
	private String tempFileName;
	
//	String sampleLink = "SAMDE-0.1.0/rest/mde/getMDList";
//	public String getSampleLink()
//	{
//	
//		return sampleLink;
//	}
//	public void setSampleLink(String sampleLink)
//	{
//	
//		this.sampleLink = sampleLink;
//	}

	//TestWrapper testwrapper; 

//	public TestWrapper getTestwrapper()
//	{
//		return testwrapper;
//	}
//	public void setTestwrapper(TestWrapper testwrapper)
//	{
//		this.testwrapper = testwrapper;
//	}

	public FileDownloadBean(){
//		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
//		System.out.println("$$$$$$$ FileDownloadBean() ####################################");
//		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
//	   //TestExportResource testResource = new TestExportResource();
//	   //String exportResourceURL = testResource.getRequestPath();
//	   //logger.info("exportResourceURL: " + exportResourceURL);
//	   //testwrapper = new TestWrapper(exportResourceURL);
//	      InputStream stream = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/tmp/example1.xml");
//	        file = new DefaultStreamedContent(stream, "application/txt", "EXAMPLE.xml");
	//wrappedData.add(new CustomerWrapper(customer, exportResourceURL));
	}

//	public String getExportResourceURL()
//	{
//		//return testwrapper.getExportResourceURL();
//	}

//	public void dummy() {
//		System.out.println("FileDownloadBean.dummy");
//		GUIrefs.displayAlert("FileDownloadBean.dummy");
//		try {
//		  this.file = prepDownload();
//		}
//		catch(Exception e)
//		{
//			GUIrefs.displayAlert(StringUtilities.escapeQuotes(e.getMessage()));
//			System.out.println(e.getStackTrace());
//		}
//	}
//	public void getDownloadFileStreamContent()
//	{
//		System.out.println("FileDownloadBean.getDownloadFileStreamContent");
//	    try
//	    {
//	        PortletResponse portletResponse = (PortletResponse) FacesContext.getCurrentInstance().getExternalContext()
//	                .getResponse();
//	        HttpServletResponse res = PortalUtil.getHttpServletResponse(portletResponse);
//	        res.setHeader("Content-Disposition", "attachment; filename=\"ahoj.txt\"");//
//	        res.setHeader("Content-Transfer-Encoding", "binary");
//	        res.setContentType("application/txt");
//	        res.getOutputStream().write("ahoj".getBytes());
//	        res.flushBuffer();
//
//	        GUIrefs.displayAlert("SUCCESS");
//
//	    }
//	    catch (IOException ioe)
//	    {
//	    	GUIrefs.displayAlert("FAILED");
//	        //logger.error("An error occurred uploading the file: " + ioe.getMessage(), ioe);
//	    }
//	}
//	
//	public StreamedContent prepDownload() throws Exception {
//		StreamedContent download=new DefaultStreamedContent();
//		    //File file = new File("C:\\file.csv");
//		    InputStream input = new ByteArrayInputStream("blablabla".getBytes());
//		    //ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
//		    download=new DefaultStreamedContent(input, "application/txt", "SAMPLEFILE.txt");
//		    System.out.println("PREP = " + download.getName());
//		return download;
//		}
//	
//	public void downloadCurrentMDObject() throws Exception {
//		//InputStream stream = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream(tempFilePath);
//		FileData filedata = Master.documentsStore.getDisplayFile();
//		Node contents = filedata.getRootNode();
//		InputStream stream = new ByteArrayInputStream( DOMUtilities.node2String(contents).getBytes());
//		file = new DefaultStreamedContent(stream, "text/txt", filedata.getDisplayname());
//	}
//	
//	public void downloadTest(){
//	   try
//	{
//		InputStream stream = new FileInputStream("/home/uuuu/sampleFiles/requNmbrs.txt");
//		file = new DefaultStreamedContent(stream, "text/txt", "beautifulTestFile.txt");
//	}
//	catch (Exception e)
//	{
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//	}
//
//	public void onSave() {
//		System.out.println("FileDownloadBean.onSave()");
//		// MIMetadataType metaData =
//		// Master.documentsStore.getCurrentMetaDataObject();
//		IMdeElement metaDataRoot = Master.documentsStore.getCurrentMetaDataObject();
//
//		if (null != metaDataRoot) {
//			tempFileName = "MI_Metadata_" + (new Date()).toString() + ".xml";
//			tempFilePath = "/tmp/obeosTestSave/" + tempFileName;
//			File file = new File(tempFilePath);
//			try {
//				Model.save(file, metaDataRoot, validateBeforeSave);
//				System.out.println("executed GMI.save(...)");
//			} catch (Exception e) {
//				System.out.println("Exception on trying to save file " + Master.documentsStore.activeFileId);
//				// try again without validating
//				try {
//					Model.save(file, metaDataRoot, false);
//					GUIrefs.displayAlert("validation failed, but saved file without validation");
//				} catch (Exception e2) {
//					GUIrefs.displayAlert("Can\'t save file even without validation");
//				}
//			}
//			//downloadCurrentMDObject();
//		} else {
//			System.out.println("FileDownloadBean.onSave(): active metaData is null!");
//		}
//	}
//	
//	//added in August 
//	//throws BridgeException and FacesException
//	public void downloadFile() {
//
//	    File file = new File("/tmp/example1.xml");
//	    HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();  
//
//	    response.setHeader("Content-Disposition", "attachment;filename=sth cryptic");  
//	    response.setContentLength((int) file.length());  
//	    ServletOutputStream out = null;  
//	    try {  
//	        FileInputStream input = new FileInputStream(file);  
//	        byte[] buffer = new byte[1024];  
//	        out = response.getOutputStream();  
//	        int i = 0;  
//	        while ((i = input.read(buffer)) != -1) {  
//	            out.write(buffer);  
//	            out.flush();  
//	        }  
//	        FacesContext.getCurrentInstance().getResponseComplete();  
//	    } catch (IOException err) {  
//	        err.printStackTrace();  
//	    } finally {  
//	        try {  
//	            if (out != null) {  
//	                out.close();  
//	            }  
//	        } catch (IOException err) {  
//	            err.printStackTrace();  
//	        }  
//	    }  
//
//	}
//	public StreamedContent getFile2() throws Exception {
//
//		System.out.println("getFile2");
//		// 1. initialize the fileInputStream
//		 File localfile = new File("/tmp/example1.xml");
//		 FileInputStream fis = new FileInputStream(localfile);
//
//		 // 2. get Liferay's ServletResponse
//		 PortletResponse portletResponse = (PortletResponse) FacesContext
//		   .getCurrentInstance().getExternalContext().getResponse();
//		 HttpServletResponse res = PortalUtil
//		   .getHttpServletResponse(portletResponse);
//		 res.setHeader("Content-Disposition", "attachment; filename=\""
//		   + "downloaded-ex1.xml" + "\"");//
////		 res.setHeader("Content-Transfer-Encoding", "text");
//		 res.setContentType("application/xml");
//		 res.flushBuffer();
//
//		 // 3. write the file into the outputStream
//		 OutputStream out = res.getOutputStream();
//		 byte[] buffer = new byte[4096];
//		 int bytesRead;
//		 while ((bytesRead = fis.read(buffer)) != -1) {
//		  out.write(buffer, 0, bytesRead);
//		  buffer = new byte[4096];
//		 }
//
//		 // 4. return null to this method
//		 return null;
//		}
	

	// setters and getters
	public boolean isValidateBeforeSave() {
		return validateBeforeSave;
	}

	public void setValidateBeforeSave(boolean validateBeforeSave) {
		this.validateBeforeSave = validateBeforeSave;
	}

	public boolean isAutomaticUpdateRevisionDate() {
		return ConfigurationConstants.automaticUpdateRevisionDate;
	}

	public void setAutomaticUpdateRevisionDate(boolean automaticUpdateRevisionDate) {
		//this.automaticUpdateRevisionDate = automaticUpdateRevisionDate;
		ConfigurationConstants.automaticUpdateRevisionDate = automaticUpdateRevisionDate;
	}

	public String getTempFileName() {
		return tempFileName;
	}

	public void setTempFileName(String tempFileName) {
		this.tempFileName = tempFileName;
	}

	public StreamedContent getFile() {
		return file;
	}

	public void setFile(StreamedContent file) {
		this.file = file;
	}

	public String getTempFilePath() {
		return tempFilePath;
	}

	public void setTempFilePath(String tempFilePath) {
		this.tempFilePath = tempFilePath;
	}

}
