package asyncrr.ajax;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

public class AsyncRROption {

	public static final Type DEFAULT = new Type(0);
	public static final Type STRING = new Type(1);
	public static final Type SCRIPT = new Type(2);
	public static final Type NUMBER = new Type(3);
	public static final Type ARRAY = new Type(4);
	public static final Type STRING_ARRAY = new Type(5);
	public static final Type BOOLEAN = new Type(6);
	public static final Type STRING_OR_ARRAY = new Type(7);
	public static final Type DICTIONARY = new Type(8);
	public static final Type FUNCTION = new Type(9); // Function with no args
	public static final Type FUNCTION_1 = new Type(9); // Function with one arg
	public static final Type FUNCTION_2 = new Type(9); // Function with two args
	public static final Type FUNCTION_3 = new Type(10); // Function with three args

	private String _name;
	private Type _type;
	private String _bindingName;
	private Object _defaultValue;

	public static class Type {
		private int _number;

		// This class might make more sense on AjaxValue?
		public Type(int number) {
			_number = number;
		}
	}

	public AsyncRROption(String name) {
		this(name, name, null, DEFAULT);
	}

	public AsyncRROption(String name, Type type) {
		this(name, name, null, type);
	}

	public AsyncRROption(String name, Object defaultValue, Type type) {
		this(name, name, defaultValue, type);
	}

	public AsyncRROption(String name, String bindingName, Object defaultValue, Type type) {
		_name = name;
		_bindingName = bindingName;
		_type = type;
		_defaultValue = defaultValue;
	}

	public String name() {
		return _name;
	}

	public Type type() {
		return _type;
	}

	public AsyncRRValue valueForObject(Object obj) {
		return new AsyncRRValue(_type, obj);
	}

	public Object defaultValue() {
		return _defaultValue;
	}

	/*
	 * Bridge to an AjaxComponent.
	 */
	protected Object valueInComponent(WOComponent component) {
		Object value = component.valueForBinding(_bindingName);
		if (value instanceof WOAssociation) {
			WOAssociation association = (WOAssociation) value;
			value = association.valueInComponent(component);
		}
		if (value == null) {
			value = _defaultValue;
		}
		return value;
	}

	/*
	 * Bridge to an AjaxDynamicElement.
	 */
	protected Object valueInComponent(WOComponent component, NSDictionary<String, ? extends WOAssociation> associations) {
		Object value = null;
		if (associations != null) {
			value = associations.objectForKey(_bindingName);
			// This is needed for the double step to resolve the value for ^ notation
			if (value instanceof WOAssociation) {
				WOAssociation association = (WOAssociation) value;
				value = association.valueInComponent(component);
			}
		}
		if (value == null) {
			value = _defaultValue;
		}
		return value;
	}

	/**
	 * Evaluates this AjaxOption on a WOComponent and adds the name and JavaScript formatted value to dictionary.
	 * 
	 * @param component
	 *                WOComponent to get binding value from
	 * @param dictionary
	 *                mutable dictionary to add key-value pair to
	 */
	public void addToDictionary(WOComponent component, NSMutableDictionary<String, String> dictionary) {
		Object value = valueInComponent(component);
		String strValue = valueForObject(value).javascriptValue();
		if (strValue != null) {
			dictionary.setObjectForKey(strValue, _name);
		}
	}

	/**
	 * Evaluates this AjaxOption on a WODynamicElement and adds the name and JavaScript formatted value to dictionary.
	 * 
	 * @param component
	 *                WOComponent to get binding value from
	 * @param associations
	 *                dictionary of associations to get WOAssocation providing value from
	 * @param dictionary
	 *                mutable dictionary to add key-value pair to
	 */
	protected void addToDictionary(WOComponent component, NSDictionary<String, ? extends WOAssociation> associations,
			NSMutableDictionary<String, String> dictionary) {
		Object value = valueInComponent(component, associations);
		String strValue = valueForObject(value).javascriptValue();
		if (strValue != null) {
			dictionary.setObjectForKey(strValue, _name);
		}
	}

	/**
	 * @param ajaxOptions
	 *                list of AjaxOption to evaluate on component
	 * @param component
	 *                WOComponent to get binding value from
	 * 
	 * @return dictionary produced by evaluating the array of AjaxOption on a WOComponent and adding the resulting name and JavaScript formatted
	 *         values
	 */
	public static NSMutableDictionary<String, String> createAjaxOptionsDictionary(NSArray<AsyncRROption> ajaxOptions, WOComponent component) {
		NSMutableDictionary<String, String> optionsDictionary = new NSMutableDictionary<String, String>();
		for (AsyncRROption ajaxOption : ajaxOptions) {
			ajaxOption.addToDictionary(component, optionsDictionary);
		}
		return optionsDictionary;
	}

	/**
	 * @param ajaxOptions
	 *                list of AjaxOption to evaluate on component
	 * @param component
	 *                WOComponent to get binding value from
	 * @param associations
	 *                dictionary of associations to get WOAssocation providing value from
	 * 
	 * @return dictionary produced by evaluating the array of AjaxOption on a WOComponent and adding the resulting name and JavaScript formatted
	 *         values
	 */
	public static NSMutableDictionary<String, String> createAjaxOptionsDictionary(NSArray<AsyncRROption> ajaxOptions, WOComponent component,
			NSDictionary<String, ? extends WOAssociation> associations) {
		NSMutableDictionary<String, String> optionsDictionary = new NSMutableDictionary<String, String>();
		for (AsyncRROption ajaxOption : ajaxOptions) {
			ajaxOption.addToDictionary(component, associations, optionsDictionary);
		}
		return optionsDictionary;
	}
}
