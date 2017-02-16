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

package mdretrieval;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.primefaces.context.RequestContext;

import filehandling.FileData;
import filehandling.FilePreprocessor;
import global.GUIrefs;
import global.Master;
import utilities.StringUtilities;

@ManagedBean
@SessionScoped
public class UrlLoaderBean implements Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = 6203157309305052236L;
    private String url;

    public void loadMdFromUrl()
    {
        // debug
        GUIrefs.displayAlert(" INFO: trying to load from URL " + url);
        InputStream instream = FileFetcher.fetchFileFromUrl(url);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            org.apache.commons.io.IOUtils.copy(instream, baos);
            byte[] bytes = baos.toByteArray();
            String extension = "xml";
            String filename = UUID.randomUUID().toString();
            List<FileData> resultList = FilePreprocessor.extractMDFilesFromXMLEmbedding(bytes, filename, extension);
            if (null != resultList && resultList.size() > 0)
            {
                GUIrefs.updateComponent(GUIrefs.fileDispDlg);
                GUIrefs.updateComponent(GUIrefs.filesToShow);

                StringBuilder sb = new StringBuilder();
                for (FileData fd : resultList)
                {
                    if (sb.length() != 0)
                    {
                        sb.append(',');
                    }
                    sb.append(fd.getSourceUrl());
                }
                sb.insert(0, "fireBasicEvent(\'MDEeditedMD\' ,{url:\'[");
                sb.append("]\'});");

                String command = sb.toString();

                if (Master.DEBUG_LEVEL > Master.LOW)
                    System.out.println(command);
                RequestContext.getCurrentInstance().execute(command);
                RequestContext.getCurrentInstance().execute("PF('fileDispDlg').show();");
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
            GUIrefs.displayAlert(StringUtilities.escapeQuotes(e.getMessage()));
        }

    }

    // setters and getters
    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }
}
