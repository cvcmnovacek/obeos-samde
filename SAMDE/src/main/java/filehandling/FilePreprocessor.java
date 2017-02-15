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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import global.ConfigurationConstants;
import global.GUIrefs;
import global.Master;
import mdretrieval.FileFetcher;
import utilities.DOMUtilities;
import utilities.StringUtilities;

public class FilePreprocessor
{

    public static List<FileData> extractMDFilesFromXMLEmbedding(byte[] bytes, String basename, String extension)
    {

        List<FileData> returnList = new ArrayList<FileData>();
        try
        {
            FileUtils.writeByteArrayToFile(new File("/tmp/debugData/instreamBeforeExtraction.xml"), bytes);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder dBuilder = dbf.newDocumentBuilder();

            Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(bytes)));

            if (Master.DEBUG_LEVEL > Master.LOW)
            {
                System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
            }

            NodeList entryElements = doc.getElementsByTagNameNS("http://www.w3.org/2005/Atom", "entry");
            for (int i = 0; i < entryElements.getLength(); i++)
            {
                Element entryElement = (Element) entryElements.item(i);
                FileData fd = null;
                /*
                 * First try to find a <link> element pointing to Metadata as
                 * this should be included by default
                 */
                if (fd == null)
                {
                    NodeList linkElements = entryElement.getElementsByTagNameNS("http://www.w3.org/2005/Atom", "link");
                    String iso19139Link = null;
                    String iso19139_2Link = null;
                    for (int j = 0; j < linkElements.getLength(); j++)
                    {
                        Element linkElement = (Element) linkElements.item(j);
                        String relAttrValue = linkElement.getAttribute("rel");
                        String typeAttributeValue = linkElement.getAttribute("type");
                        if (relAttrValue != null && relAttrValue.equals("alternate") && typeAttributeValue != null)
                        {
                            switch (typeAttributeValue)
                            {
                            case "application/vnd.iso.19139+xml":
                                iso19139Link = linkElement.getAttribute("href");
                                break;
                            case "application/vnd.iso.19139-2+xml":
                                iso19139_2Link = linkElement.getAttribute("href");
                                break;
                            }
                        }
                    }
                    /* iso19139-2 gets priority */
                    String url = iso19139_2Link != null ? iso19139_2Link : iso19139Link;
                    if (url != null)
                    {
                        try (InputStream is = FileFetcher.fetchFileFromUrl(url))
                        {
                            Document doc2 = dBuilder.parse(new InputSource(is));
                            fd = processMDList(doc2.getDocumentElement(), extension, url);
                        }
                    }
                }
                /*
                 * Fallback to finding Metadata embedded directly in <entry>.
                 * There will be either MI_Metadata or MD_Metadata, not both
                 */
                if (fd == null)
                {
                    fd = processMDList(entryElement.getElementsByTagName("gmi:MI_Metadata").item(0), extension, null);
                }
                if (fd == null)
                {
                    fd = processMDList(entryElement.getElementsByTagName("gmd:MD_Metadata").item(0), extension, null);
                }

                if (fd != null)
                {
                    returnList.add(fd);
                }
            }
        }
        catch (Exception e)
        {
            // TODO: handle exception

            if (Master.DEBUG_LEVEL > Master.LOW)
                System.out.println(e.getLocalizedMessage());
            GUIrefs.displayAlert("Error in FilePreprocessor.extractMDFilesFromXMLEmbedding"
                    + StringUtilities.escapeQuotes(e.getMessage()));
        }
        return returnList;
    }

    public static String extractFileIdentifierFromNode(Node node) throws Exception
    {
        String fileIdentifierString = "";
        Node fileIdentifier = null;
        if (node.getNodeType() == Node.ELEMENT_NODE)
        {
            fileIdentifier = ((Element) node).getElementsByTagName("gmd:fileIdentifier").item(0);
            if (null != fileIdentifier)
            {
                Node charStr = null;
                if (fileIdentifier.getNodeType() == Node.ELEMENT_NODE)
                {
                    charStr = ((Element) fileIdentifier).getElementsByTagName("gco:CharacterString").item(0);
                    fileIdentifierString = charStr.getFirstChild().getTextContent();
                }
                else
                {
                    throw new Exception("Invalid 'fileIdentifier' in current file!");
                }
            }
        }
        return fileIdentifierString;
    }

    public static FileData processMDList(Node node, String extension, String sourceUrl)
    {
        if (node == null)
        {
            return null;
        }
        try
        {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            String basename = extractFileIdentifierFromNode(node);
            return processNode(node, transformer, basename, extension, sourceUrl);
        }
        catch (Exception e)
        {

            if (Master.DEBUG_LEVEL > Master.LOW)
                System.out.println(StringUtilities.escapeQuotes(e.getMessage()));
        }
        // FIXME: exception should probably be thrown
        return null;
    }

    private static FileData processNode(Node node, Transformer trafo, String name, String extension, String sourceUrl)
            throws TransformerException
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Source input = new DOMSource(node);

        Result outputTarget = new StreamResult(outputStream);
        trafo.transform(input, outputTarget);
        InputStream is = new ByteArrayInputStream(outputStream.toByteArray());

        FileData fileData = Master.documentsStore.createAndAddNewTempfile(is, name, extension, false);
        fileData.setRootNode(DOMUtilities.domFromNode(node));
        fileData.setModelVersion(ConfigurationConstants.modelVersion);
        fileData.setSourceUrl(sourceUrl);

        return fileData;
    }
}
