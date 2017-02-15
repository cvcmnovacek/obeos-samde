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

// (C) Copyright European Space Agency, 2016

package esa.obeos.metadataeditor.common.util;

import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class TimeUtil 
{
    protected TimeUtil() 
    {
    }

    /**
     * Converts a XML Greg. calendar to the Date.
     *
     * @param xmlGreCal
     *			the XML Greg. calendart
     * @return Date
     *			the date
     **/
    public static Date convert(final XMLGregorianCalendar xmlGreCal)
    {
        Date ret = null;

        if( null != xmlGreCal )
        {
            GregorianCalendar cal = xmlGreCal.toGregorianCalendar();
            if( null != cal )
            {
                ret = cal.getTime();
            }
        }		
        return ret;
    }

    /**
     * Converts a XML Greg. calendar to the string.
     *
     * @param xmlGreCal
     *			the XML Greg. calendar
     * @return String
     *			the time representation in the string
     **/
    public static String toString(XMLGregorianCalendar xmlGreCal)
    {
        String ret = new String("");

        if( null != xmlGreCal )
        {
            GregorianCalendar cal = xmlGreCal.toGregorianCalendar();
            if( null != cal )
            {
                ret = toString(cal.getTime());
            }
        }		
        return ret;
    }

    /**
     * Converts the date to the string.
     *
     * @param date
     *			the date
     * @return String
     *			the time representation in the string
     **/
    public static String toString(Date date)
    {
        String ret = new String("");

        if( null != date )
        {
            DateFormat dt = FormatUtil.getDateFormat();
            ret = dt.format(date);
        }

        return ret;
    }

    /**
     * Converts the date to the XML Greg. calendar.
     *
     * @param date
     *			the date
     * @return String
     *			the time representation in the string
     **/
    public static XMLGregorianCalendar convert(final Date date)
    {
        XMLGregorianCalendar ret = null;

        if( null != date )
        {
            try 
            {
                ret = DatatypeFactory.newInstance().newXMLGregorianCalendar(
                        FormatUtil.getDateFormat().format(date));
            } 
            catch (DatatypeConfigurationException e) 
            {
                e.printStackTrace();
            } 
        }		
        return ret;
    }

    public static Date toDate(String date) throws Exception
    {
        Date ret = null;

        try
        {
            ret = FormatUtil.getDateFormat().parse(date);
        }
        catch( Exception e )
        {
            throw new Exception(e.getMessage() + ". Supported date time format is '" + FormatUtil.getDateFormat().toPattern() + "'");
        }

        return ret;
    }

    public static XMLGregorianCalendar toCalendar(String date) throws Exception
    {
        XMLGregorianCalendar ret = null;
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        Date dateTime = toDate(date);
        if( null != dateTime )
        {
            cal.setTime(dateTime);
            ret = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
        }
        return ret;
    }

}
