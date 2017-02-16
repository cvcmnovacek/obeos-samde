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

import java.util.Iterator;

import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.primefaces.context.RequestContext;

public class GUIrefs {

	public static final String tree = "uitree";
	public static final String pTree = "treeform:uitree";
	//public static final String treeContextMenu = "treeCM";
	//public static final String pTreeContextMenu = "treeform:treeCM";
	//public static final String childlistCM = "CMchilddatalist";
	//public static final String pChildlistCM = "nodeselectionform:CMchilddatalist";

    //public static final String treeContextMenu = "contextform:treeCM";
	public static final String childList = "childdatalist";
	//public static final String pChildList = "nodeselectionform:childdatalist";
	public static final String attributeList = "attrList";
	public static final String pAttributeList = "nodecontentsform:attrList";
	public static final String attributeLabel = "attrLbl";
	public static final String pAttributeLabel = "nodecontentsform:attrLbl";
	
	public static final String wvFileDispDlg = "fileDispDlg";
	public static final String fileDispDlg = "pfFileDispDlg";
	public static final String filesToShow = "filetoshowlist";
	
	public static final String fileDropDownList = "filelist";
	
	public static final String fragmentTextArea = "textarea";
	public static final String fragmentTextPanel = "textfieldpanel";
	public static final String fragmentLabel = "fragmentLabel";
	
	public static final String osRequestForm = "osRequestForm";
	public static final String nmbrOfFilesBeingEdited = "selectionSizeIndicator";

	public static final String errorDlg = "errorForm";
    
	public static String getClientId(String componentId) {
        FacesContext context = FacesContext.getCurrentInstance();
        UIViewRoot root = context.getViewRoot();

        UIComponent c = findComponent(root, componentId);
        return c.getClientId(context);
      }

      /**
       * Finds component with the given id
       */
      private static UIComponent findComponent(UIComponent c, String id) {
        if (id.equals(c.getId())) {
          return c;
        }
        Iterator<UIComponent> kids = c.getFacetsAndChildren();
        while (kids.hasNext()) {
          UIComponent found = findComponent(kids.next(), id);
          if (found != null) {
            return found;
          }
        }
        return null;
      }
      
      public static void updateComponent(String compName){
		RequestContext.getCurrentInstance().update(GUIrefs.getClientId(compName));
      }
      
      public static void displayAlert(String message){
    	  StringBuilder builder = new StringBuilder("alert('");
    	  builder.append(message);
    	  builder.append("');");
    	  RequestContext.getCurrentInstance().execute(builder.toString());
      }
}
