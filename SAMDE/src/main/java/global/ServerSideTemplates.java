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
import java.util.HashMap;
import java.util.Map;

import esa.obeos.metadataeditor.model.api.ModelType;
import esa.obeos.metadataeditor.model.api.ModelVersion;

public class ServerSideTemplates {
	
	public static String templateDir = (ConfigurationConstants.props == null)?
			"/home/obeos/data/sampleFiles/MDtemplates/" :
				ConfigurationConstants.props.getProperty("TemplateDir");
	
	public static Map<String,String> availableTemplates; //= new ArrayList<String>(Arrays.asList((templateDir+"simple.xml")));
	static{
		availableTemplates = new HashMap<String,String>();
		//availableTemplates.put( (templateDir+"simple.xml"), "basic");
//		for(File filepath : (new File(templateDir)).listFiles())
//		{
//			availableTemplates.put(filepath.getAbsolutePath(), filepath.getName());
//		}
	}
	
	public static Map<String, String> getAvailableTemplates(ModelType type, ModelVersion version)
	{
		
		for(File filepath : (new File(templateDir + version.name() + "/" + type.name())).listFiles())
		{
			availableTemplates.put(filepath.getAbsolutePath(), filepath.getName());
		}
		return availableTemplates;
		
	}

}
