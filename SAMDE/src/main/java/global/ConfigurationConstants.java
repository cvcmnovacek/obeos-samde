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

package global;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import esa.obeos.metadataeditor.model.api.ModelType;
import esa.obeos.metadataeditor.model.api.ModelVersion;


public class ConfigurationConstants
{

    private static String PROPERTIES_PATH = "/srv/samde/samde.properties";

    private static FileReader fr = null;
    public static Properties props = null; // TODO: should initialise with
                                           // defaults instead.

    public static ModelType modelType = ModelType.GMI;
    public static ModelVersion modelVersion = ModelVersion.Beta;

    public static int nmbrFilesToProcessSimult = 3;

    public static boolean automaticUpdateRevisionDate = true;

    public static List<String> plainXmlRootNames = new ArrayList<String>();

    static
    {
        plainXmlRootNames.add("MI_Metadata");
        plainXmlRootNames.add("MD_Metadata");

        try
        {
            fr = new FileReader(PROPERTIES_PATH);
            props = new Properties();
            props.load(fr);
            fr.close();

            if (Master.DEBUG_LEVEL > Master.NONE)
            {
                System.out.println("Using properties from " + PROPERTIES_PATH);
            }
        }
        catch (IOException e)
        {
            System.err.println(PROPERTIES_PATH + ": Cannot load properties, using defaults wherever possible.");
            System.err.println("Exception: " + e);
            props = null;
        }

    }


}
