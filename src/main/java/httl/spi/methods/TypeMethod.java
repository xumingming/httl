/*
 * Copyright 2011-2012 HTTL Team.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package httl.spi.methods;

import httl.spi.Formatter;
import httl.util.ClassUtils;
import httl.util.DateUtils;
import httl.util.LocaleUtils;
import httl.util.NumberUtils;
import httl.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * TypeMethod. (SPI, Singleton, ThreadSafe)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class TypeMethod {

	private Formatter<Object> formatter;

	private TimeZone timeZone;

	private String dateFormat;

	private String numberFormat;

	private String outputEncoding;

	private String[] importPackages;

	/**
	 * httl.properties: formatter=httl.spi.formatters.DateFormatter
	 */
	public void setFormatter(Formatter<Object> formatter) {
		this.formatter = formatter;
	}

	/**
	 * httl.properties: time.zone=+8
	 */
	public void setTimeZone(String timeZone) {
		this.timeZone = TimeZone.getTimeZone(timeZone);
	}

	/**
	 * httl.properties: date.format=yyyy-MM-dd HH:mm:ss
	 */
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	/**
	 * httl.properties: number.format=###,##0.###
	 */
	public void setNumberFormat(String numberFormat) {
		this.numberFormat = numberFormat;
	}

	/**
	 * httl.properties: output.encoding=UTF-8
	 */
	public void setOutputEncoding(String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

	/**
	 * httl.properties: import.packages=java.util
	 */
	public void setImportPackages(String[] importPackages) {
		this.importPackages = importPackages;
	}

	public static Locale toLocale(String name) {
		return LocaleUtils.getLocale(name);
	}

	public static boolean toBoolean(Object value) {
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		return value == null ? false : toBoolean(String.valueOf(value));
	}

	public static char toChar(Object value) {
		if (value instanceof Character) {
			return (Character) value;
		}
		return value == null ? '\0' : toChar(String.valueOf(value));
	}

	public static byte toByte(Object value) {
		if (value instanceof Number) {
			return ((Number) value).byteValue();
		}
		return value == null ? 0 : toByte(String.valueOf(value));
	}

	public static short toShort(Object value) {
		if (value instanceof Number) {
			return ((Number) value).shortValue();
		}
		return value == null ? 0 : toShort(String.valueOf(value));
	}

	public static int toInt(Object value) {
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		return value == null ? 0 : toInt(String.valueOf(value));
	}

	public static long toLong(Object value) {
		if (value instanceof Number) {
			return ((Number) value).longValue();
		}
		return value == null ? 0 : toLong(String.valueOf(value));
	}

	public static float toFloat(Object value) {
		if (value instanceof Number) {
			return ((Number) value).floatValue();
		}
		return value == null ? 0 : toFloat(String.valueOf(value));
	}

	public static double toDouble(Object value) {
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		}
		return value == null ? 0 : toDouble(String.valueOf(value));
	}

	public static Class<?> toClass(Object value) {
		if (value instanceof Class) {
			return (Class<?>) value;
		}
		return value == null ? null : toClass(String.valueOf(value));
	}

	public static boolean toBoolean(String value) {
		return value == null || value.length() == 0 ? false : Boolean.parseBoolean(value);
	}

	public static char toChar(String value) {
		return value == null || value.length() == 0 ? '\0' : value.charAt(0);
	}

	public static byte toByte(String value) {
		return value == null || value.length() == 0 ? 0 : Byte.parseByte(value);
	}

	public static short toShort(String value) {
		return value == null || value.length() == 0 ? 0 : Short.parseShort(value);
	}

	public static int toInt(String value) {
		return value == null || value.length() == 0 ? 0 : Integer.parseInt(value);
	}

	public static long toLong(String value) {
		return value == null || value.length() == 0 ? 0 : Long.parseLong(value);
	}

	public static float toFloat(String value) {
		return value == null || value.length() == 0 ? 0 : Float.parseFloat(value);
	}

	public static double toDouble(String value) {
		return value == null || value.length() == 0 ? 0 : Double.parseDouble(value);
	}

	public static Class<?> toClass(String value) {
		return value == null || value.length() == 0 ? null : ClassUtils.forName(value);
	}

	@SuppressWarnings("unchecked")
	public <T> T[] toArray(Collection<T> value, String type) {
		Class<T> cls = (Class<T>) ClassUtils.forName(importPackages, type);
		if (value == null) {
			return (T[]) Array.newInstance(cls, 0);
		}
		return (T[]) value.toArray((Object[])Array.newInstance(cls, value.size()));
	}

	public Date toDate(String value) {
		try {
			return value == null || value.length() == 0 ? null : DateUtils.parse(value, dateFormat, timeZone);
		} catch (Exception e) {
			try {
				return DateUtils.parse(value, "yyyy-MM-dd");
			} catch (Exception e2) {
				return DateUtils.parse(value, "yyyy-MM-dd HH:mm:ss");
			}
		}
	}

	public Date toDate(String value, String format) {
		return value == null || value.length() == 0 ? null : DateUtils.parse(value, format, timeZone);
	}

	public static Date toDate(String value, String format, String timeZone) {
		return value == null || value.length() == 0 ? null : DateUtils.parse(value, format, timeZone == null ? null : TimeZone.getTimeZone(timeZone));
	}

	public String toString(Date value) {
		return value == null ? null : DateUtils.format(value, dateFormat, timeZone);
	}

	public String format(Date value, String format) {
		return value == null ? null : DateUtils.format(value, format, timeZone);
	}

	public static String format(Date value, String format, String timeZone) {
		return value == null ? null : DateUtils.format(value, format, timeZone == null ? null : TimeZone.getTimeZone(timeZone));
	}

	public static String toString(boolean value) {
		return String.valueOf(value);
	}

	public static String toString(char value) {
		return String.valueOf(value);
	}

	public String toString(byte value) {
		return format(Byte.valueOf(value), numberFormat);
	}

	public String toString(short value) {
		return format(Short.valueOf(value), numberFormat);
	}

	public String toString(int value) {
		return format(Integer.valueOf(value), numberFormat);
	}

	public String toString(long value) {
		return format(Long.valueOf(value), numberFormat);
	}

	public String toString(float value) {
		return format(Float.valueOf(value), numberFormat);
	}

	public String toString(double value) {
		return format(Double.valueOf(value), numberFormat);
	}

	public String toString(Number value) {
		return format(value, numberFormat);
	}

	public String toString(byte[] value) {
		try {
			return value == null ? null : (outputEncoding == null 
					? new String(value) : new String(value, outputEncoding));
		} catch (UnsupportedEncodingException e) {
			return new String(value);
		}
	}

	public String toString(Object value) {
		if (value == null)
			return null;
		if (value instanceof String)
			return (String) value;
		if (value instanceof Number)
			return toString((Number) value);
		if (value instanceof Date)
			return toString((Date) value);
		if (value instanceof byte[])
			return toString((byte[]) value);
		if (formatter != null)
			return formatter.format(value);
		return StringUtils.toString(value);
	}

	public static String format(byte value, String format) {
		return format(Byte.valueOf(value), format);
	}

	public static String format(short value, String format) {
		return format(Short.valueOf(value), format);
	}

	public static String format(int value, String format) {
		return format(Integer.valueOf(value), format);
	}

	public static String format(long value, String format) {
		return format(Long.valueOf(value), format);
	}

	public static String format(float value, String format) {
		return format(Float.valueOf(value), format);
	}

	public static String format(double value, String format) {
		return format(Double.valueOf(value), format);
	}

	public static String format(Number value, String format) {
		return value == null ? null : NumberUtils.format(value, format);
	}

}