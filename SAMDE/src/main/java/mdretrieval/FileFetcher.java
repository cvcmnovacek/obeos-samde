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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import global.GUIrefs;
import global.Master;
import global.ServerConstants;
import utilities.StringUtilities;


public class FileFetcher
{

    public static InputStream fetchFileFromUrl(String urlString)
    {

        InputStream inputStream = null;

        Proxy proxy = ServerConstants.isProxyEnabled
                ? new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ServerConstants.hostname, ServerConstants.port))
                : Proxy.NO_PROXY;

        URL url;
        try
        {
            url = new URL(urlString);
            url.openConnection();

            if (proxy != Proxy.NO_PROXY && proxy.type() != Proxy.Type.DIRECT)
            {
                inputStream = url.openConnection(proxy).getInputStream();
            }
            else
            {
                inputStream = url.openConnection().getInputStream();
            }
        }
        catch (MalformedURLException e)
        {
            GUIrefs.displayAlert("Invalid URL " + urlString + "- \\n malformed expression");
            e.printStackTrace();
        }
        catch (IOException ioe)
        {
            GUIrefs.displayAlert("Cannot read from " + StringUtilities.escapeQuotes(urlString) + "- IOException");
            ioe.printStackTrace();
        }

        return inputStream;
    }

    public static String[] loadStringFromURL(String destinationURL, boolean acceptRDF) throws IOException
    {
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        URL url = new URL(destinationURL);

        if (ServerConstants.isProxyEnabled)
        {
            Proxy proxy = new Proxy(Proxy.Type.HTTP,
                    new InetSocketAddress(ServerConstants.hostname, ServerConstants.port));
            urlConnection = (HttpURLConnection) url.openConnection(proxy);
        }
        else
        {
            urlConnection = (HttpURLConnection) url.openConnection();
        }

        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Accept", "application/rdf+xml");
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);

        inputStream = urlConnection.getInputStream();

        String[] ret = new String[2];
        try
        {
            ret[0] = IOUtils.toString(inputStream);
            ret[1] = urlConnection.getHeaderField("Content-Type");

            if (Master.DEBUG_LEVEL > Master.LOW)
            {
                System.out.println(" Content-type: " + urlConnection.getHeaderField("Content-Type"));
            }
        }
        finally
        {
            IOUtils.closeQuietly(inputStream);
        }

        if (Master.DEBUG_LEVEL > Master.LOW)
            System.out.println("Done reading " + destinationURL);
        return ret;

    }
}
