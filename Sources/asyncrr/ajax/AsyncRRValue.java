package asyncrr.ajax;

import java.util.Enumeration;

import com.webobjects.foundation.*;

import er.ajax.AjaxOption;

public class AsyncRRValue {

	private AsyncRROption.Type _type;
	private Object _value;

	public AsyncRRValue(Object value) {
		this(AsyncRROption.DEFAULT, value);
	}

	/**
	 * Creates AjaxValue for value with the indicated type. If type is AjaxOption.DEFAULT, then
	 * the actual type will be inferred if value is String, Number, Boolean, NSArray, NSDictionary,
	 * or AjaxValue (if value is an AjaxValue then both type and value are taken from value).
	 * 
	 * @see #AjaxValue(Object)
	 * @see AjaxOption.Type
	 * 
	 * @param type
	 *                one of AjaxOption.Type constants from AjaxOption
	 * @param value
	 *                the value to make into an AjaxValue
	 */
	public AsyncRRValue(AsyncRROption.Type type, Object value) {
		_type = type;
		_value = value;
		if (type == AsyncRROption.DEFAULT) {
			if (value instanceof String) {
				_type = AsyncRROption.STRING;
			} else if (value instanceof Number) {
				_type = AsyncRROption.NUMBER;
			} else if (value instanceof Boolean) {
				_type = AsyncRROption.BOOLEAN;
			} else if (value instanceof NSArray) {
				_type = AsyncRROption.ARRAY;
			} else if (value instanceof NSDictionary) {
				_type = AsyncRROption.DICTIONARY;
			} else if (value instanceof AsyncRRValue) {
				_type = ((AsyncRRValue) value)._type;
				_value = ((AsyncRRValue) value)._value;
			}
		}
	}

	public AsyncRROption.Type type() {
		return _type;
	}

	public Object value() {
		return _value;
	}

	/**
	 * @param obj
	 *                Object to convert to String and escape
	 * @return obj converted to a string and escaped for use as a quoted JS string
	 */
	public static String javaScriptEscaped(Object obj) {
		String escapedValue = String.valueOf(obj);
		escapedValue = escapedValue.replaceAll("\\\\", "\\\\\\\\");
		escapedValue = escapedValue.replaceAll("'", "\\\\'");
		// Handle line breaks
		escapedValue = escapedValue.replaceAll("\\r\\n", "\\\\n");
		escapedValue = escapedValue.replaceAll("\\n", "\\\\n");
		escapedValue = "'" + escapedValue + "'"; // XXX schmied: comment out in JQ?
		return escapedValue;
	}

	/**
	 * @return a String representing this AjaxValue in a form suitable for use in JavaScript
	 */
	public String javascriptValue() {
		String strValue;
		AsyncRROption.Type type = _type;
		if (type == AsyncRROption.STRING_OR_ARRAY) {
			if (_value == null) {
				type = AsyncRROption.STRING;
			} else if (_value instanceof NSArray) {
				type = AsyncRROption.ARRAY;
			} else if (_value instanceof String) {
				strValue = (String) _value;
				if (strValue.startsWith("[")) {
					type = AsyncRROption.ARRAY;
				} else {
					type = AsyncRROption.STRING;
				}
			}
		}
		if (_value == null || _value == NSKeyValueCoding.NullValue) {
			strValue = null;
		} else if (type == AsyncRROption.STRING) {
			strValue = javaScriptEscaped(_value);
		} else if (type == AsyncRROption.NUMBER) {
			strValue = _value.toString();
		} else if (type == AsyncRROption.ARRAY) {
			if (_value instanceof NSArray) {
				NSArray<?> arrayValue = (NSArray<?>) _value;
				StringBuilder sb = new StringBuilder();
				sb.append('[');
				Enumeration<?> objEnum = arrayValue.objectEnumerator();
				while (objEnum.hasMoreElements()) {
					Object o = objEnum.nextElement();
					sb.append(new AsyncRRValue(o).javascriptValue());
					if (objEnum.hasMoreElements()) {
						sb.append(',');
					}
				}
				sb.append(']');
				strValue = sb.toString();
			} else {
				strValue = _value.toString();
			}
		} else if (type == AsyncRROption.DICTIONARY) {
			if (_value instanceof NSDictionary) {
				NSDictionary<?, ?> dictValue = (NSDictionary<?, ?>) _value;
				StringBuilder sb = new StringBuilder();
				sb.append('{');
				Enumeration<?> keyEnum = dictValue.keyEnumerator();
				while (keyEnum.hasMoreElements()) {
					Object key = keyEnum.nextElement();
					Object value = dictValue.objectForKey(key);
					sb.append(new AsyncRRValue(key).javascriptValue());
					sb.append(':');
					sb.append(new AsyncRRValue(value).javascriptValue());
					if (keyEnum.hasMoreElements()) {
						sb.append(',');
					}
				}
				sb.append('}');
				strValue = sb.toString();
			} else {
				strValue = _value.toString();
			}
		} else if (type == AsyncRROption.STRING_ARRAY) {
			if (_value instanceof NSArray) {
				NSArray<?> arrayValue = (NSArray<?>) _value;
				int count = arrayValue.count();
				if (count == 1) {
					strValue = new AsyncRRValue(AsyncRROption.STRING, arrayValue.objectAtIndex(0)).javascriptValue();
				} else if (count > 0) {
					StringBuilder sb = new StringBuilder();
					sb.append('[');
					Enumeration<?> objEnum = arrayValue.objectEnumerator();
					while (objEnum.hasMoreElements()) {
						Object o = objEnum.nextElement();
						sb.append(new AsyncRRValue(AsyncRROption.STRING, o).javascriptValue());
						if (objEnum.hasMoreElements()) {
							sb.append(',');
						}
					}
					sb.append(']');
					strValue = sb.toString();
				} else {
					strValue = "[]";
				}
			} else {
				strValue = _value.toString();
			}
		} else if (type == AsyncRROption.SCRIPT) {
			strValue = _value.toString();
		} else if (type == AsyncRROption.FUNCTION) {
			strValue = "function() {" + _value.toString() + "}";
		} else if (type == AsyncRROption.FUNCTION_1) {
			strValue = "function(v) {" + _value.toString() + "}";
		} else if (type == AsyncRROption.FUNCTION_2) {
			strValue = "function(v1, v2) {" + _value.toString() + "}";
		} else if (type == AsyncRROption.FUNCTION_3) {
			strValue = "function(v1, v2, v3) {" + _value.toString() + "}";
		} else {
			strValue = _value.toString();
		}
		return strValue;
	}
}
