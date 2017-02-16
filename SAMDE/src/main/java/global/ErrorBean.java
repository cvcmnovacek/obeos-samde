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

package global;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.primefaces.context.RequestContext;

@ManagedBean
@SessionScoped
public class ErrorBean implements Serializable
{
    public ErrorBean()
    {
        Master.errorBean = this;
    }
    
    
    private Map<String,String> errorMessages = new HashMap<String,String>();
    
    public void handleErrors()
    {
        if(!isEmpty())
        {
            updateErrorDlg();
            RequestContext.getCurrentInstance().execute("PF('errorMsgDlg').show()");
        }
        clearMessages();
    }
    
 
    public void addMessage(String key, String value)
    {
        errorMessages.put(key, value);
    }
    public void appendMessage(String key, String value)
    {
        String extMsg = errorMessages.get(key) == null ? "" : errorMessages.get(key) + "\n" + value;
        errorMessages.put(key, value);
    }
    public void clearMessages()
    {
        errorMessages.clear();
    }
    
    public boolean isEmpty()
    {
        return (errorMessages.size() == 0);
    }

    public void updateErrorDlg()
    {
        GUIrefs.updateComponent("errorTable");
    }
    public Map<String, String> getErrorMessages()
    {
        return errorMessages;
    }

    public void setErrorMessages(Map<String, String> errorMessages)
    {
        this.errorMessages = errorMessages;
    }

}
