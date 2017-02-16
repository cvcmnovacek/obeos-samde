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

package semanticannot;

import org.primefaces.model.TreeNode;

public class SubtreeDataEnvelope
{

    protected final String valToken = DataCollectingVisitor.VAL_TOKEN;
    protected final String attrToken = DataCollectingVisitor.ATTR_TOKEN;

    protected SubtreeData blockData;

    // only valid if corresponding file is display file
    protected TreeNode localRootTreeNode = null;

    public SubtreeDataEnvelope(SubtreeData blockData)
    {
        this.blockData = blockData;
    }

    public TreeNode getLocalRootTreeNode()
    {
        return localRootTreeNode;
    }
    

    public Concept getAnchorOrCharString(String firstPart)
    {
        Concept ret = null;
        String key = firstPart + "/CharacterString/V";
        Object v = blockData.getParameter(key);
        if (null != v)
        {
            ret = new Concept((String) v, "");
        }
        else
        {
            key = firstPart + "/Anchor/V";
            String key2 = firstPart + "/Anchor/A/href";
            v = blockData.getParameter(key);
            Object v2 = blockData.getParameter(key2);
            String s1 = (v != null) ? (String) v : "";
            String s2 = (v2 != null) ? (String) v2 : "";
            if (!s1.equals(""))
            {
                ret = new Concept(s1, s2);
            }
        }
        return ret;
    }

    public void setLocalRootTreeNode(TreeNode localRootTreeNode)
    {
        this.localRootTreeNode = localRootTreeNode;
    }


    public SubtreeData getBlockData()
    {
        return blockData;
    }


    public void setBlockData(SubtreeData blockData)
    {
        this.blockData = blockData;
    }

    @Override
    public String toString()
    {
        return this.blockData.toString();
    }

}
