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

package tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.xml.bind.MarshalException;

import org.primefaces.context.RequestContext;

import esa.obeos.metadataeditor.model.api.IMdeElement;
import esa.obeos.metadataeditor.model.api.Model;
import esa.obeos.metadataeditor.model.api.exc.InspireConstraintException;
import esa.obeos.metadataeditor.model.impl.MdeElement;
import global.GUIrefs;
import global.Master;
import utilities.StringUtilities;

@ManagedBean
@SessionScoped
public class ValidatorBean implements Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = -8600411685115169848L;

    private boolean includeInspireConstraints = true;
    private IMdeElement modelRoot;
    private List<String> containerIds;
    private List<String> validationMsgs;

    private int nmbrOfInvalidFiles = 0;


    public void validateOnlyActiveFile()
    {

        validateActiveFile();

        String msg;
        if (!validationMsgs.isEmpty())
        {
            msg = validationMsgs.get(0);
        }
        else
        {
            msg = "Document is valid. Validation against Inspire constraints was switched ";
            msg += includeInspireConstraints ? "on" : "off";
        }
        GUIrefs.displayAlert(StringUtilities.escapeQuotes(msg));

    }

    public void validateActiveFile()
    {

        // initialisation is needed in any case for potential later use in
        // validateAll()
        validationMsgs = new ArrayList<String>();

        String message = validate(modelRoot);
        String alertMsg = StringUtilities.escapeQuotes(message);
        if (!message.equals(""))
        {
            // GUIrefs.displayAlert(alertMsg);
        //    System.out.println(message);
            validationMsgs.add(message);
        }

    }

    public void validateAll()
    {

        validateActiveFile();// initialises list 'validationMsgs'

        if (validationMsgs != null && validationMsgs.size() > 0)
        {
            nmbrOfInvalidFiles = 1;
        }
        else
        {
            nmbrOfInvalidFiles = 0;
        }

        for (String id : containerIds)
        {
            for (Entry<String, IMdeElement> entry : Master.service.getModels(id).entrySet())
            {
                String res = validate(entry.getValue());
                validationMsgs.add(res);
                if (!res.equals(""))
                {
                    nmbrOfInvalidFiles++;
                }
            }
        }
        String msg = "";
        if (nmbrOfInvalidFiles > 0)
        {
            msg += nmbrOfInvalidFiles;
            String rest = nmbrOfInvalidFiles == 1 ? " file is invalid" : " files are invalid";

            msg += rest;
        }
        else
        {
            msg += "All selected files are valid!";
            msg += " Validation against Inspire constraints was switched ";
            msg += includeInspireConstraints ? "on" : "off";
        }
        validationMsgs.add(msg);

        GUIrefs.updateComponent("infoList");
        RequestContext.getCurrentInstance().execute("PF('infoDlg').show()");
    }

    private String validate(IMdeElement model)
    {
        return ValidatorBean.validate(model, includeInspireConstraints);

    }

    public static String validate(IMdeElement model, boolean includeInspire)
    {

        if (null != model)
        {
            String fileId = "null";
            try
            {
                IMdeElement fileIdentifier = model.getChildByXmlName("fileIdentifier");
                fileId = (String) fileIdentifier.getChildByXmlName("CharacterString").getElementValue();
            }
            catch (NullPointerException ne)
            {
                //element fileIdentifier is not in a proper state, so leave fileId=null 
            }
            catch (Exception e)
            {
                
            }
            try
            {

                Model.validate(model, includeInspire);
            }
            catch (Exception e)
            {
                // display error messages
                // GUIrefs.displayAlert("Document is not valid: \n" +
                // e.getMessage());
                String cause = "";
                String res = "";

                if (e instanceof InspireConstraintException)
                {
                    cause = " ( violation of INSPIRE constraints ) ";

                }
                else
                {
                    res = handleMarshalException(e,
                            "Document with fileIdentifier \'" + fileId + "\' is not valid - validation terminated with MarshalException");
                }

                if (res.equals(""))
                {
                    res = "Document with fileIdentifier \'" + fileId + "\' is not valid " + cause + e.getMessage();
                }
                return res;
            }
        }
        else
        {
            // GUIrefs.displayAlert("Error: nothing to validate!");
            return "Error: nothing to validate!";
        }
        return "";
    }

    public static String handleMarshalException(Exception exc, String defaultMsg)
    {
        String msg = exc.getMessage();
        if (msg == null && exc instanceof MarshalException)
        {
            Throwable le = ((MarshalException) exc).getLinkedException();
            if (null != le && null != le.getMessage())
            {
                msg = le.getMessage();
            }
            else
            {
                if (null != defaultMsg)
                {
                    msg = defaultMsg;
                }
                else
                {
                    msg = "MarshalException";
                }
            }
        }
        // String escapedMsg = StringUtilities.escapeQuotes(msg);
        // GUIrefs.displayAlert(escapedMsg);
        return msg;
    }

    public boolean isIncludeInspireConstraints()
    {

        return includeInspireConstraints;
    }

    public void setIncludeInspireConstraints(boolean includeInspireConstraints)
    {

        this.includeInspireConstraints = includeInspireConstraints;
    }

    public List<String> getValidationMsgs()
    {

        return validationMsgs;
    }

    public void setValidationMsgs(List<String> validationMsgs)
    {

        this.validationMsgs = validationMsgs;
    }

    public List<String> getContainerIds()
    {

        return containerIds;
    }

    public void setContainerIds(List<String> containerIds)
    {

        this.containerIds = containerIds;
    }

    public IMdeElement getModelRoot()
    {

        return modelRoot;
    }

    public void setModelRoot(IMdeElement modelRoot)
    {

        this.modelRoot = modelRoot;
    }

}
