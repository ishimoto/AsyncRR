package asyncrr.components;

import asyncrr.ajax.*;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

import er.extensions.appserver.ajax.ERXAjaxApplication;
import er.extensions.foundation.ERXPropertyListSerialization;

public class AjaxJQUpdateLink extends AsyncRRElement {

	public AjaxJQUpdateLink(String name, NSDictionary<String, WOAssociation> associations, WOElement children) {
		super(name, associations, children);
	}

	@Override
	protected String[] requiredScriptResources() {
		return AsyncRRElement.SCRIPT_RESOURCES_JQUERY;
	}

	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		WOComponent component = context.component();
		boolean disabled = booleanValueForBinding("disabled", false, component);
		String updateContainerID = AjaxJQUpdateContainer.updateContainerID(this, component);
		AjaxPTUpdateContainer.setUpdateContainerID(request, updateContainerID);
		WOActionResults results = null;
		if (!disabled) {
			results = (WOActionResults) valueForBinding("action", component);
		}
		if (ERXAjaxApplication.isAjaxReplacement(request)) {
			AjaxPTUtils.setPageReplacementCacheKey(context, (String) valueForBinding("replaceID", component));
		} else if (results == null || booleanValueForBinding("ignoreActionResponse", false, component)) {
			String script = (String) valueForBinding("onClickServer", component);
			if (script != null) {
				WOResponse response = AjaxPTUtils.createResponse(request, context);
				AjaxPTUtils.appendScriptHeaderIfNecessary(request, response);
				response.appendContentString(script);
				AjaxPTUtils.appendScriptFooterIfNecessary(request, response);
				results = response;
			}
		} else if (updateContainerID != null) {
			AjaxPTUtils.setPageReplacementCacheKey(context, updateContainerID);
		}
		return results;
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		WOComponent component = context.component();
		NSDictionary<String, String> options = _options(component);
		boolean disabled = booleanValueForBinding("disabled", false, component);
		Object stringValue = valueForBinding("string", component);
		String functionName = (String) valueForBinding("functionName", component);
		if (functionName == null) {
			String elementName;
			boolean button = booleanValueForBinding("button", false, component);
			if (button) {
				elementName = "input";
			} else {
				elementName = stringValueForBinding("elementName", "a", component);
			}
			boolean isATag = "a".equalsIgnoreCase(elementName);
			boolean renderTags = (isATag);
			if (true) {
				response.appendContentString("<");
				response.appendContentString(elementName);
				if (button) {
					appendTagAttributeToResponse(response, "type", Boolean.valueOf(button));
				}
				if (isATag) {
					appendTagAttributeToResponse(response, "href", "javascript:void(0)");
				}
				appendTagAttributeToResponse(response, "title", valueForBinding("title", component));
				appendTagAttributeToResponse(response, "value", valueForBinding("value", component));
				appendTagAttributeToResponse(response, "class", valueForBinding("class", component));
				appendTagAttributeToResponse(response, "style", valueForBinding("style", component));
				appendTagAttributeToResponse(response, "id", valueForBinding("id", component));
				appendTagAttributeToResponse(response, "accesskey", valueForBinding("accesskey", component));
				appendTagAttributeToResponse(response, "data-wonder-id", "AUL");
				appendTagAttributeToResponse(response, "data-wonder-options",
						ERXPropertyListSerialization.jsonStringFromPropertyList(options));
				if (disabled) {
					appendTagAttributeToResponse(response, "disabled", Boolean.TRUE);
				}
				if (button) {
					if (stringValue != null) {
						appendTagAttributeToResponse(response, "value", stringValue);
					}
					if (disabled) {
						response.appendContentString(">");
					}
				}
				response.appendContentString(">");
			}
			if (stringValue != null && !button) {
				response.appendContentHTMLString(stringValue.toString());
			}
			appendChildrenToResponse(response, context);
			if (renderTags) {
				response.appendContentString("</");
				response.appendContentString(elementName);
				response.appendContentString(">");
			}
		} else {
			// TODO ... interesting.
		}
		super.appendToResponse(response, context);
	}

	protected NSDictionary<String, String> _options(WOComponent component) {
		NSMutableArray<AsyncRROption> ajaxOptionsArray = new NSMutableArray<AsyncRROption>();
		ajaxOptionsArray.addObject(new AsyncRROption("async", AsyncRROption.BOOLEAN));
		ajaxOptionsArray.addObject(new AsyncRROption("cache", AsyncRROption.BOOLEAN));
		ajaxOptionsArray.addObject(new AsyncRROption("callback", AsyncRROption.FUNCTION_2));
		ajaxOptionsArray.addObject(new AsyncRROption("complete", AsyncRROption.FUNCTION_2));
		ajaxOptionsArray.addObject(new AsyncRROption("delegate", AsyncRROption.STRING));
		NSDictionary<String, String> options = AsyncRROption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
		options.takeValueForKey(AjaxPTUtils.ajaxComponentActionUrl(component.context()), "url");
		String updateContainerID = AjaxJQUpdateContainer.updateContainerID(this, component);
		options.takeValueForKey(updateContainerID, "updateContainer");
		options.takeValueForKey(component.context().contextID() + "." + component.context().elementID(), "elementID");
		return options;
	}

//	@Override
//	protected void addRequiredWebResources(WOResponse response, WOContext context) {
//		AjaxJQUtils.addScriptResourceInHead(context, response, "JQuery", AjaxJQUtils.JQUERY_JS);
//		AjaxJQUtils.addScriptResourceInHead(context, response, "JQuery", AjaxJQUtils.JQUERY_WONDER_JS);
//	}
}
