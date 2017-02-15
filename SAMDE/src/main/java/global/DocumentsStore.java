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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import esa.obeos.metadataeditor.model.api.IMdeElement;
import filehandling.FileData;

public class DocumentsStore
{

	// public List<FileData> fileDataList; //really needed?
	public List<FileData> selectedFiles = new ArrayList<FileData>();
	// public Map<Path, MIMetadataType> collection = new HashMap<Path,
	// MIMetadataType>();
	public Map<Path, FileData> collection = new HashMap<Path, FileData>();
	public Path activeFileId = null;

	public DocumentsStore()
	{
		// fileDataList = new ArrayList<FileData>();
		// selectedFiles = new ArrayList<FileData>();
	}

	public IMdeElement getCurrentMetaDataObject()
	{

		FileData filedata = getDisplayFile();
		return filedata.getModelRoot();
	}

	public FileData getDisplayFile()
	{
		FileData ret = null;
		if(null != activeFileId)
		{
			ret =  collection.get(activeFileId);
		}

		return  ret;
	}

	public FileData createAndAddNewTempfile(InputStream source, String basename, String extension,
			boolean setAsNewDisplay)
	{

		// String filename = predefinedTemplates.get(pathChosenTemplate);
		Path folder = Paths.get(ServerConstants.PATH_TO_UPLOADS);
		Path filepath = null;
		FileData ret = null;
		try
		{
			filepath = Files.createTempFile(folder, basename + "-", "." + extension);
			Files.copy(source, filepath, StandardCopyOption.REPLACE_EXISTING);
			FileData newFileData = new FileData(basename, folder, filepath);
			ret = newFileData;

			collection.put(filepath, newFileData);
			if (setAsNewDisplay)
			{
				this.activeFileId = filepath;
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	public void addFile(FileData fd)
	{

		// fileDataList.add(fd);
		collection.put(fd.getCompletePath(), fd);
	}

	public void removeFile(FileData fd)
	{

		// fileDataList.remove(fd);
		collection.remove(fd.getCompletePath());
	}

	public void addToSelection(FileData fd)
	{

		boolean alreadyThere = false;
		for (FileData f : selectedFiles)
		{
			if (f.getCompletePath().equals(fd.getCompletePath()))
			{
				alreadyThere = true;
			}
		}
		if (!alreadyThere)
		{
			selectedFiles.add(fd);
		}
	}

	public void removeFromSelection(FileData fd)
	{

		selectedFiles.remove(fd);
	}

	public void reset()
	{
	    for(Entry<Path, FileData> entry : collection.entrySet())
	    {
	          File file = new File(entry.getKey().toString());     
	          if(!file.delete())
	          {
	              System.out.println("deletion of file " + entry.getKey().toString() + " failed");
	          }
	    }

		collection.clear();
		selectedFiles.clear();
	}

}
