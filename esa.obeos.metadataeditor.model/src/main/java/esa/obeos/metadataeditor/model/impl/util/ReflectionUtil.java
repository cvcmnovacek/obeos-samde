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

package esa.obeos.metadataeditor.model.impl.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;


public class ReflectionUtil
{
    /** Gets the class hierarchy 
     * 
     * @return List<Class<?>>
     * 
     * */
    public static List<Class<?>> getClassHierarchy(Object obj, Class<?> ignoreClass)
    {
        List<Class<?>> ret = new ArrayList<Class<?>>();

        if (null != obj)
        {
            Class<?> clazz = obj.getClass();

            while (null != clazz && (ignoreClass != clazz))
            {
                ret.add(0, clazz);
                clazz = clazz.getSuperclass();
            }        
        }
        return ret;
    }

    public static Field getDeclaredField(final Object obj, final Class<?> ignoreClass, final String fieldName)
    {
        Field ret = null;
    
        if (null != obj && null != fieldName)
        {
            List<Class<?>> classes = getClassHierarchy(obj, ignoreClass);
            for (Class<?> clazz : classes)
            {
                try
                {
                    ret = clazz.getDeclaredField(fieldName);
                    if (null != ret)
                    {
                        break;
                    }
                }
                catch (NoSuchFieldException e)
                {
                    continue;
                }
            }
        }
        
        return ret;
    }
    
    public static Class<?> getFirstParametrizedType(Type type) throws Exception
    {
        Class<?> ret = null;

        if (type instanceof ParameterizedType)
        {
            ParameterizedType parameterizedType = (ParameterizedType)type;
            Type[] types =  parameterizedType.getActualTypeArguments();
            if (null == types || types.length < 1)
            {
                throw new Exception("missing parameterized type");
            }

            if (types[0] instanceof WildcardType)
            {
                Type[] lower = ((WildcardType)types[0]).getLowerBounds();
                if (null != lower && lower.length > 1)
                {
                    String lowerTypeName = getTypeName(lower[0]);
                    
                    if (!("?".equals(lowerTypeName)))
                    {
                        ret = Class.forName(lowerTypeName);
                    }
                }

                if (null == ret)
                {
                    Type[] upper = ((WildcardType)types[0]).getUpperBounds();
                    if (null != upper && upper.length > 0)
                    {
                        String upperTypeName = getTypeName(upper[0]);

                        if (!(Object.class.getName().equals(upperTypeName)))
                        {
                            ret = Class.forName(upperTypeName);
                        }
                    }
                }
            }

            if (null == ret)
            {
                String typeName = getTypeName(types[0]);

                if( !("?".equals(typeName)) )
                {
                    ret = Class.forName(typeName);
                }
            }
        }

        return ret;
    }

    public static String getTypeName(final Type type)
    {
        String ret = null;
        
        if( null != type )
        {
            ret = type.toString();
            int index = ret.indexOf(' ');
            if( index > 0 )
            {
                // remove type kind (e.g. 'class')
                ret = ret.substring(index + 1);
            }
        }
        
        return ret;
    }
    

    public static void initPrimitive(Object fieldContainer, Field f) throws Exception
    {
        f.setAccessible(true);
        if (byte.class == f.getType())
        {
            f.setByte(fieldContainer, (byte)0);
        }
        else if (short.class == f.getType())
        {
            f.setShort(fieldContainer, (short)0);
        }
        else if (int.class == f.getType())
        {
            f.setInt(fieldContainer, 0);
        }
        else if (long.class == f.getType())
        {
            f.setLong(fieldContainer, 0);
        }
        else if (float.class == f.getType())
        {
            f.setFloat(fieldContainer, 0);
        }
        else if (double.class == f.getType())
        {
            f.setDouble(fieldContainer, 0);
        }
        else if (boolean.class == f.getType())
        {
            f.setBoolean(fieldContainer, false);
        }
        else if (char.class == f.getType())
        {
            f.setChar(fieldContainer, '\0');
        }
        f.setAccessible(false);
    }

    public static Object getPrimitiveValue(Object fieldContainer, Field f) throws Exception
    {
        Object ret = null;
        
        if (null == fieldContainer)
        {
            throw new NullPointerException("fieldContainer is null");
        }
        
        if (null == f)
        {
            throw new NullPointerException("field is null");
        }
        
        f.setAccessible(true);
        if (byte.class == f.getType())
        {
            ret = new Byte(f.getByte(fieldContainer));
        }
        else if (short.class == f.getType())
        {
            ret = new Short(f.getShort(fieldContainer));
        }
        else if (int.class == f.getType())
        {
            ret = new Integer(f.getInt(fieldContainer));
        }
        else if (long.class == f.getType())
        {
            ret = new Long(f.getLong(fieldContainer));
        }
        else if (float.class == f.getType())
        {
            ret = new Float(f.getFloat(fieldContainer));
        }
        else if (double.class == f.getType())
        {
            ret = new Double(f.getDouble(fieldContainer));
        }
        else if (boolean.class == f.getType())
        {
            ret = new Boolean(f.getBoolean(fieldContainer));
        }
        else if (char.class == f.getType())
        {
            ret = new Character(f.getChar(fieldContainer));
        }
        f.setAccessible(false);
        
        return ret;
    }

    public static Class<?> getPrimitiveObjectEquivalent(final Class<?> clazz) throws Exception
    {
        Class<?> ret = null;
        
        if (null == clazz)
        {
            throw new NullPointerException("clazz is null");
        }
        
        if (!clazz.isPrimitive())
        {
            throw new IllegalArgumentException("clazz is not a primitive type");
        }
        
        if (byte.class == clazz)
        {
            ret = Byte.class;
        }
        else if (short.class == clazz)
        {
            ret = Short.class;
        }
        else if (int.class == clazz)
        {
            ret = Integer.class;
        }
        else if (long.class == clazz)
        {
            ret = Long.class;
        }
        else if (float.class == clazz)
        {
            ret = Float.class;
        }
        else if (double.class == clazz)
        {
            ret = Double.class;
        }
        else if (boolean.class == clazz)
        {
            ret = Boolean.class;
        }
        else if (char.class == clazz)
        {
            ret = Character.class;
        }
        else
        {
            throw new Exception("unsupported primitive type '" + clazz.getSimpleName() + "'");
        }
        
        return ret;
    }
    
    public static void setPrimitiveValue(Object fieldContainer, Field f, Object value) throws Exception
    {
        f.setAccessible(true);
        if (byte.class == f.getType())
        {
            if (value instanceof Byte)
            {
                f.setByte(fieldContainer, ((Byte)value).byteValue());
            }
            else
            {
                throw new IllegalArgumentException("cannot set value '" + value.getClass().getSimpleName() 
                        + "' to a filed w/ type '" + f.getType().getSimpleName() + "'");
            }
        }
        else if (short.class == f.getType())
        {
            if (value instanceof Short)
            {
                f.setShort(fieldContainer, ((Short)value).shortValue());
            }
            else
            {
                throw new IllegalArgumentException("cannot set value '" + value.getClass().getSimpleName() 
                        + "' to a filed w/ type '" + f.getType().getSimpleName() + "'");
            }
        }
        else if (int.class == f.getType())
        {
            if (value instanceof Integer)
            {
                f.setInt(fieldContainer, ((Integer)value).intValue());
            }
            else
            {
                throw new IllegalArgumentException("cannot set value '" + value.getClass().getSimpleName() 
                        + "' to a filed w/ type '" + f.getType().getSimpleName() + "'");
            }
        }
        else if (long.class == f.getType())
        {
            if (value instanceof Long)
            {
                f.setLong(fieldContainer, ((Long)value).longValue());
            }
            else
            {
                throw new IllegalArgumentException("cannot set value '" + value.getClass().getSimpleName() 
                        + "' to a filed w/ type '" + f.getType().getSimpleName() + "'");
            }
        }
        else if (float.class == f.getType())
        {
            if (value instanceof Float)
            {
                f.setFloat(fieldContainer, ((Float)value).floatValue());
            }
            else
            {
                throw new IllegalArgumentException("cannot set value '" + value.getClass().getSimpleName() 
                        + "' to a filed w/ type '" + f.getType().getSimpleName() + "'");
            }
        }
        else if (double.class == f.getType())
        {
            if (value instanceof Double)
            {
                f.setDouble(fieldContainer, ((Double)value).doubleValue());
            }
            else
            {
                throw new IllegalArgumentException("cannot set value '" + value.getClass().getSimpleName() 
                        + "' to a filed w/ type '" + f.getType().getSimpleName() + "'");
            }
        }
        else if (boolean.class == f.getType())
        {
            if (value instanceof Boolean)
            {
                f.setBoolean(fieldContainer, ((Boolean)value).booleanValue());
            }
            else
            {
                throw new IllegalArgumentException("cannot set value '" + value.getClass().getSimpleName() 
                        + "' to a filed w/ type '" + f.getType().getSimpleName() + "'");
            }
        }
        else if (char.class == f.getType())
        {
            if (value instanceof Character)
            {
                f.setChar(fieldContainer, ((Character)value).charValue());
            }
            else
            {
                throw new IllegalArgumentException("cannot set value '" + value.getClass().getSimpleName() 
                        + "' to a filed w/ type '" + f.getType().getSimpleName() + "'");
            }
        }
        f.setAccessible(false);
        
    }

}
