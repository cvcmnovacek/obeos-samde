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

package utilities;

//the implementation of difference and indexOfDifference is copied from Apache Commons
public class StringUtilities
{

	public static String difference(String str1, String str2)
	{

		if (str1 == null)
		{
			return str2;
		}
		if (str2 == null)
		{
			return str1;
		}
		int at = indexOfDifference(str1, str2);
		if (at == -1)
		{
			return "";
		}
		return str2.substring(at);
	}

	public static int indexOfDifference(String str1, String str2)
	{

		if (str1 == str2)
		{
			return -1;
		}
		if (str1 == null || str2 == null)
		{
			return 0;
		}
		int i;
		for (i = 0; i < str1.length() && i < str2.length(); ++i)
		{
			if (str1.charAt(i) != str2.charAt(i))
			{
				break;
			}
		}
		if (i < str2.length() || i < str1.length())
		{
			return i;
		}
		return -1;
	}

	public static boolean isEmpty(String s)
	{

		if (null == s || s.equals(""))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public static String escapeQuotes(String s)
	{

		String res = "";
		if (!(s==null))
		{
			res = s.replace("'", "\\'");
			res = res.replace("\"", "\\\\\"");
			res = res.replace("\n", "\\n");
		}
		else
		{
			res = "";
		}

		return res;
	}
	
	public static String truncateURI(String URI)
	{
        int i = URI.indexOf("//");
        String result=URI;
        if(i > 0  && URI.substring(0,i-1).equals("http")){
           int i2 = URI.indexOf("/", i+2);
           if(i2 != -1)
           {
        	   int end = Math.min(i2+1, URI.length()-1);
               result = URI.substring(0,end);
           }
        }
        return result;
	}
}
