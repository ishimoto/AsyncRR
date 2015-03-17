package asyncrr.components;

import asyncrr.ajax.*;

import com.webobjects.appserver.*;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.foundation.*;

import er.extensions.appserver.ERXWOContext;
import er.extensions.appserver.ajax.*;
import er.extensions.components._private.ERXWOForm;
import er.extensions.foundation.*;

public class AjaxJQSubmitButton extends AsyncRRElement {

	public AjaxJQSubmitButton(String name, NSDictionary<String, WOAssociation> associations, WOElement children) {
		super(name, associations, children);
	}

	@Override
	protected String[] requiredScriptResources() {
		return AsyncRRElement.SCRIPT_RESOURCES_JQUERY;
	}

	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		WOComponent component = context.component();
		WOActionResults result = (WOActionResults) valueForBinding("action", component);
		if (ERXAjaxApplication.isAjaxReplacement(request)) {
			AjaxPTUtils.setPageReplacementCacheKey(context, stringValueForBinding("replaceID", component));
		} else if (result == null || booleanValueForBinding("ignoreActionResponse", false, component)) {
			WOResponse response = AjaxPTUtils.createResponse(request, context);
			String onClickServer = stringValueForBinding("onClickServer", component);
			if (onClickServer != null) {
				AjaxPTUtils.appendScriptHeader(response);
				response.appendContentString(onClickServer);
				AjaxPTUtils.appendScriptFooter(response);
			}
			result = response;
		} else {
			String updateContainerID = AjaxPTUpdateContainer.updateContainerID(this, component);
			if (updateContainerID != null) {
				AjaxPTUtils.setPageReplacementCacheKey(context, updateContainerID);
			}
		}
		return result;
	}

//	@Override
//	protected void addRequiredWebResources(WOResponse response, WOContext context) {
//		AjaxJQUtils.addScriptResourceInHead(context, response, "JQuery", AjaxJQUtils.JQUERY_JS);
//		AjaxJQUtils.addScriptResourceInHead(context, response, "JQuery", AjaxJQUtils.JQUERY_WONDER_JS);
//	}

	public String nameInContext(WOContext context, WOComponent component) {
		return stringValueForBinding("name", context.elementID(), component);
	}

	public boolean disabledInComponent(WOComponent component) {
		return booleanValueForBinding("disabled", false, component);
	}

	protected NSDictionary<String, String> _options(WOComponent component) {
		NSMutableArray<AsyncRROption> ajaxOptionsArray = new NSMutableArray<AsyncRROption>();
		ajaxOptionsArray.addObject(new AsyncRROption("async", AsyncRROption.BOOLEAN));
		ajaxOptionsArray.addObject(new AsyncRROption("cache", AsyncRROption.BOOLEAN));
		ajaxOptionsArray.addObject(new AsyncRROption("callback", AsyncRROption.FUNCTION_2));
		ajaxOptionsArray.addObject(new AsyncRROption("delegate", AsyncRROption.STRING));
		ajaxOptionsArray.addObject(new AsyncRROption("onBeforeClick", AsyncRROption.STRING));
		ajaxOptionsArray.addObject(new AsyncRROption("formName", AsyncRROption.STRING));
		NSMutableDictionary<String, String> options = AsyncRROption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
		options.takeValueForKey(AjaxPTUtils.ajaxComponentActionUrl(component.context()), "url");
		String updateContainerID = AjaxPTUpdateContainer.updateContainerID(this, component);
		options.takeValueForKey(updateContainerID, "updateContainer");
		String name = nameInContext(component.context(), component);
		AjaxJQSubmitButton.fillInAjaxOptions(this, component, name, options);
		return options;
	}

	public static void fillInAjaxOptions(IAsyncRRElement element, WOComponent component, String submitButtonName,
			NSMutableDictionary<String, String> options) {
		String systemDefaultFormSerializer = "Form.serializeWithoutSubmits";
		String defaultFormSerializer = ERXProperties.stringForKeyWithDefault("er.ajax.formSerializer", systemDefaultFormSerializer);
		String formSerializer = (String) element.valueForBinding("formSerializer", defaultFormSerializer, component);
		if (!defaultFormSerializer.equals(systemDefaultFormSerializer)) {
			options.setObjectForKey(formSerializer, "_fs");
		}
		options.setObjectForKey("'" + submitButtonName + "'", "_asbn");
		if ("true".equals(options.objectForKey("async"))) {
			options.removeObjectForKey("async");
		}
		if ("true".equals(options.objectForKey("evalScripts"))) {
			options.removeObjectForKey("evalScripts");
		}
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		WOComponent component = context.component();
		NSDictionary<String, String> options = _options(component);
		String functionName = stringValueForBinding("functionName", null, component);
		String formName = stringValueForBinding("formName", component);
		boolean showUI = (functionName == null || booleanValueForBinding("showUI", false, component));
		boolean showButton = showUI && booleanValueForBinding("button", true, component);
		String formReference;
		if ((!showButton || functionName != null) && formName == null) {
			formName = ERXWOForm.formName(context, null);
			if (formName == null) {
				throw new WODynamicElementCreationException(
						"If button = false or functionName is not null, the containing form must have an explicit name.");
			}
		}
		if (formName == null) {
			formReference = "this.form";
		} else {
			formReference = "document." + formName;
		}
		String updateContainerID = AjaxPTUpdateContainer.updateContainerID(this, component);
		String replaceID = stringValueForBinding("replaceID", component);
		if (showUI) {
			boolean disabled = disabledInComponent(component);
			String elementName = stringValueForBinding("elementName", "a", component);
			boolean useButtonTag = ERXProperties.booleanForKeyWithDefault(
					"er.extensions.foundation.ERXPatcher.DynamicElementsPatches.SubmitButton.useButtonTag", false);
			if (showButton) {
				elementName = useButtonTag ? "button" : "input";
				response.appendContentString("<" + elementName);
				appendTagAttributeToResponse(response, "type", "button");
				String name = nameInContext(context, component);
				appendTagAttributeToResponse(response, "name", name);
				appendTagAttributeToResponse(response, "value", valueForBinding("value", component));
				appendTagAttributeToResponse(response, "accesskey", valueForBinding("accesskey", component));
				if (disabled) {
					appendTagAttributeToResponse(response, "disabled", "disabled");
				}
			} else {
				boolean isATag = "a".equalsIgnoreCase(elementName);
				response.appendContentString("<" + elementName);
				if (isATag) {
					appendTagAttributeToResponse(response, "href", "javascript:void(0)");
				}
			}
			appendTagAttributeToResponse(response, "class", valueForBinding("class", component));
			appendTagAttributeToResponse(response, "style", valueForBinding("style", component));
			appendTagAttributeToResponse(response, "id", valueForBinding("id", component));
			appendTagAttributeToResponse(response, "title", valueForBinding("title", component));
			if (!disabled) {
				appendTagAttributeToResponse(response, "data-wonder-id", "ASB");
				appendTagAttributeToResponse(response, "data-wonder-options",
						ERXPropertyListSerialization.jsonStringFromPropertyList(options));
			}
			if (showButton && !useButtonTag) {
				response.appendContentString("/>");
			} else {
				response.appendContentString(">");
				if (hasChildrenElements()) {
					appendChildrenToResponse(response, context);
				} else {
					response.appendContentString(stringValueForBinding("value", component));
				}
				response.appendContentString("</" + elementName + ">");
			}
		}
		super.appendToResponse(response, context);
	}

	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		WOActionResults result = null;
		WOComponent component = context.component();
		String nameInContext = nameInContext(context, component);
		String requestName = (String) request.formValueForKey(AjaxPTSubmitButton.KEY_AJAX_SUBMIT_BUTTON_NAME);
		if (requestName != null) {
			requestName = requestName.replace("'", "");
		}
		boolean shouldHandleRequest = (!disabledInComponent(component) && context.wasFormSubmitted())
				&& ((context.isMultipleSubmitForm() && nameInContext.equals(requestName)) || !context.isMultipleSubmitForm());
		if (shouldHandleRequest) {
			String updateContainerID = AjaxPTUpdateContainer.updateContainerID(this, component);
			AjaxPTUpdateContainer.setUpdateContainerID(request, updateContainerID);
			context.setActionInvoked(true);
			result = handleRequest(request, context);
			ERXWOContext.contextDictionary().takeValueForKey(ERXAjaxSession.DONT_STORE_PAGE, ERXAjaxSession.DONT_STORE_PAGE);
		}
		return result;
	}
}
