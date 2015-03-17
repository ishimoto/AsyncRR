package asyncrr.ajax;

import java.util.Enumeration;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

public class AjaxPTOptions extends WODynamicElement {

	private NSMutableDictionary<String, WOAssociation> _bindings;
	private WOElement _children;

	public AjaxPTOptions(String name, NSDictionary<String, WOAssociation> bindings, WOElement children) {
		super(name, bindings, children);
		_bindings = bindings.mutableClone();
		_children = children;
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		response.appendContentCharacter('{');
		NSMutableDictionary<String, WOAssociation> options = _bindings;
		WOAssociation optionsBinding = _bindings.objectForKey("options");
		if (optionsBinding != null) {
			NSDictionary passedInOptions = (NSDictionary) optionsBinding.valueInComponent(context.component());
			if (passedInOptions != null) {
				options = passedInOptions.mutableClone();
				options.addEntriesFromDictionary(_bindings);
			}
		}
		_appendToResponse(options, response, context);
		if (_children != null) {
			_children.appendToResponse(response, context);
		}
		response.appendContentCharacter('}');
	}

	/**
	 * Adds JSON formatted key-value pairs from options to end of response content. Does not adds the surrounding "{" and "}" signifying a
	 * dictionary / object.
	 * 
	 * @param options
	 *                dictionary of key-value pairs, intended to have come from AjaxOption
	 * @param response
	 *                WOResponse to add JSON formatted key-value pairs to
	 * @param context
	 *                WOContext to provide WOComponent to resolve binding values in
	 */
	private static void _appendToResponse(NSDictionary<String, ?> options, WOResponse response, WOContext context) {
		StringBuffer sb = new StringBuffer();
		_appendToBuffer(options, sb, context);
		response.appendContentString(sb.toString());
	}

	/**
	 * Adds JSON formatted key-value pairs from options to end of response content. Does not adds the surrounding "{" and "}" signifying a
	 * dictionary / object.
	 * 
	 * @param options
	 *                dictionary of key-value pairs, intended to have come from AjaxOption
	 * @param stringBuffer
	 *                StringBuffer to add JSON formatted key-value pairs to
	 * @param context
	 *                WOContext to provide WOComponent to resolve binding values in
	 */
	private static void _appendToBuffer(NSDictionary<String, ?> options, StringBuffer stringBuffer, WOContext context) {
		if (options != null) {
			WOComponent component = context.component();
			boolean hasPreviousOptions = false;
			Enumeration<String> bindingsEnum = options.keyEnumerator();
			while (bindingsEnum.hasMoreElements()) {
				String bindingName = bindingsEnum.nextElement();
				if (!"options".equals(bindingName)) {
					Object bindingValue = options.objectForKey(bindingName);
					// This is needed for the double step to resolve the value for ^ notation
					if (bindingValue instanceof WOAssociation) {
						WOAssociation association = (WOAssociation) bindingValue;
						bindingValue = association.valueInComponent(component);
					}
					if (bindingValue != null) {
						if (hasPreviousOptions) {
							stringBuffer.append(", ");
						}
						stringBuffer.append(bindingName);
						stringBuffer.append(':');
						stringBuffer.append(bindingValue.toString());
						hasPreviousOptions = true;
					}
				}
			}
		}
	}

	/**
	 * Adds JSON formatted key-value pairs from options to end of response content. Adds the surrounding "{" and "}" signifying a dictionary /
	 * object.
	 * 
	 * @param options
	 *                dictionary of key-value pairs, intended to have come from AjaxOption
	 * @param stringBuffer
	 *                StringBuffer to add JSON formatted key-value pairs to
	 * @param context
	 *                WOContext to provide WOComponent to resolve binding values in
	 */
	public static void appendToBuffer(NSDictionary<String, ?> options, StringBuffer stringBuffer, WOContext context) {
		stringBuffer.append('{');
		_appendToBuffer(options, stringBuffer, context);
		stringBuffer.append('}');
	}

	/**
	 * Adds JSON formatted key-value pairs from options to end of response content. Adds the surrounding "{" and "}" signifying a dictionary /
	 * object.
	 * 
	 * @param options
	 *                dictionary of key-value pairs, intended to have come from AjaxOption
	 * @param response
	 *                WOResponse to add JSON formatted key-value pairs to
	 * @param context
	 *                WOContext to provide WOComponent to resolve binding values in
	 */
	public static void appendToResponse(NSDictionary<String, ?> options, WOResponse response, WOContext context) {
		response.appendContentCharacter('{');
		_appendToResponse(options, response, context);
		response.appendContentCharacter('}');
	}
}
