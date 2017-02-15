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

package global;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import esa.obeos.metadataeditor.jaxrs.service.MetadataEditorServiceImpl;
import filehandling.FileData;
import utilities.DOMUtilities;

public class Master {
	public static DocumentsStore documentsStore;
	public static MetadataEditorServiceImpl service;
	public static ErrorBean errorBean = null;
	public static final int NONE=0;
	public static final int LOW=1;
	public static final int MED=5;
	public static final int HIGH=10;
	public static final int DEBUG_LEVEL = NONE;
	// public static DataStore dataStore;
	static {
		documentsStore = new DocumentsStore();
		service = new MetadataEditorServiceImpl();
		// dataStore = new DataStore();
	
	}

	public static void debugWriteFile(FileData fd) {
		String path = "/tmp/debugData/";
		String filename = fd.getDisplayname();
		//String contSerial;
		try {
			//contSerial = DOMUtilities.node2String(fd.getRootNode());
			//FileWriter fileWriter = new FileWriter(new File(path + filename));
			//fileWriter.write(contSerial);
			//fileWriter.close();
			DOMUtilities.serialiseDOM(DOMUtilities.domFromNode(fd.getRootNode()), path + filename);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
