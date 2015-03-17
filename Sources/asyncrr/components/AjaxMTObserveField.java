package asyncrr.components;

import asyncrr.ajax.*;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

import er.extensions.appserver.ERXWOContext;
import er.extensions.appserver.ajax.ERXAjaxApplication;

public class AjaxMTObserveField extends AsyncRRElement {

	public AjaxMTObserveField(String name, NSDictionary<String, WOAssociation> associations, WOElement children) {
		super(name, associations, children);
	}

	@Override
	protected String[] requiredScriptResources() {
		return AsyncRRElement.SCRIPT_RESOURCES_MOOTOOLS;
	}

	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		WOResponse response = AjaxPTUtils.createResponse(request, context);
		return response;
	}

	private NSMutableDictionary<String, String> createAjaxOptions(WOComponent component) {
		NSMutableArray<AsyncRROption> ajaxOptionsArray = new NSMutableArray<AsyncRROption>();
		ajaxOptionsArray.addObject(new AsyncRROption("observeFieldFrequency", AsyncRROption.NUMBER));
		ajaxOptionsArray.addObject(new AsyncRROption("observeDelay", AsyncRROption.NUMBER));
		ajaxOptionsArray.addObject(new AsyncRROption("onCancel", AsyncRROption.FUNCTION));
		ajaxOptionsArray.addObject(new AsyncRROption("onComplete", AsyncRROption.FUNCTION));
		ajaxOptionsArray.addObject(new AsyncRROption("onException", AsyncRROption.FUNCTION));
		ajaxOptionsArray.addObject(new AsyncRROption("onFailure", AsyncRROption.FUNCTION));
		ajaxOptionsArray.addObject(new AsyncRROption("onRequest", AsyncRROption.FUNCTION));
		ajaxOptionsArray.addObject(new AsyncRROption("onSuccess", AsyncRROption.FUNCTION_2));
		ajaxOptionsArray.addObject(new AsyncRROption("async", AsyncRROption.BOOLEAN));
		ajaxOptionsArray.addObject(new AsyncRROption("evalScripts", AsyncRROption.BOOLEAN));
		ajaxOptionsArray.addObject(new AsyncRROption("useSpinner", AsyncRROption.BOOLEAN));
		ajaxOptionsArray.addObject(new AsyncRROption("spinnerTarget", AsyncRROption.STRING));
		ajaxOptionsArray.addObject(new AsyncRROption("spinnerOptions", AsyncRROption.DICTIONARY));
		NSMutableDictionary<String, String> options = AsyncRROption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
		return options;
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		super.appendToResponse(response, context);
		WOComponent component = context.component();
		String observeFieldID = (String) valueForBinding("observeFieldID", component);
		String updateContainerID = AjaxMTUpdateContainer.updateContainerID(this, component);
		NSMutableDictionary<String, String> options = createAjaxOptions(component);
		boolean fullSubmit = booleanValueForBinding("fullSubmit", false, component);
		boolean observeFieldDescendents;
		if (observeFieldID != null) {
			observeFieldDescendents = false;
		} else {
			observeFieldDescendents = true;
			observeFieldID = (String) valueForBinding("id", component);
			if (observeFieldID == null) {
				observeFieldID = ERXWOContext.safeIdentifierName(context, false);
			}
			String elementName = (String) valueForBinding("elementName", component);
			if (elementName == null) {
				elementName = "div";
			}
			response.appendContentString("<" + elementName + " id = \"" + observeFieldID + "\"");
			String className = stringValueForBinding("class", component);
			if (className != null && className.length() > 0) {
				response.appendContentString(" class=\"" + className + "\"");
			}
			String style = stringValueForBinding("style", component);
			if (style != null && style.length() > 0) {
				response.appendContentString(" style=\"" + style + "\"");
			}
			response.appendContentString(">");
			if (hasChildrenElements()) {
				appendChildrenToResponse(response, context);
			}
			response.appendContentString("</" + elementName + ">");
		}
		AjaxPTUtils.appendScriptHeader(response);
		AjaxMTObserveField.appendToResponse(response, context, this, observeFieldID, observeFieldDescendents, updateContainerID, fullSubmit,
				options);
		AjaxPTUtils.appendScriptFooter(response);
	}

	public static void appendToResponse(WOResponse response, WOContext context, AsyncRRElement element, String observeFieldID,
			boolean observeDescendentFields, String updateContainerID, boolean fullSubmit, NSMutableDictionary<String, String> options) {
		WOComponent component = context.component();
		String submitButtonName = nameInContext(context, component, element);
		NSMutableDictionary<String, String> observerOptions = new NSMutableDictionary<String, String>();
		if (options != null) {
			observerOptions.addEntriesFromDictionary(options);
		}
		AjaxMTSubmitButton.fillInAjaxOptions(element, component, submitButtonName, observerOptions);
		Object observeFieldFrequency = observerOptions.removeObjectForKey("observeFieldFrequency");
		if (observeDescendentFields) {
			response.appendContentString("MTASB.observeDescendentFields");
		} else {
			response.appendContentString("MTASB.observeField");
		}
		Object observeDelay = observerOptions.removeObjectForKey("observeDelay");
		response.appendContentString("(" + AjaxPTUtils.quote(updateContainerID) + ", " + AjaxPTUtils.quote(observeFieldID) + ", "
				+ observeFieldFrequency + ", " + (!fullSubmit) + ", " + observeDelay + ", ");
		AjaxPTOptions.appendToResponse(observerOptions, response, context);
		response.appendContentString(");");
	}

//	@Override
//	protected void addRequiredWebResources(WOResponse response, WOContext context) {
//		AjaxMTUtils.addScriptResourceInHead(context, context.response(), "MooTools", AjaxMTUtils.MOOTOOLS_CORE_JS);
//		//AjaxMTUtils.addScriptResourceInHead(context, context.response(), "MooTools", AjaxMTUtils.MOOTOOLS_MORE_JS);
//		Boolean useSpinner = (Boolean) valueForBinding("useSpinner", Boolean.FALSE, context.component());
//		if (useSpinner) {
//			Boolean useDefaultSpinnerClass = (Boolean) valueForBinding("defaultSpinnerClass", Boolean.TRUE, context.component());
//			if (useDefaultSpinnerClass) {
//				AjaxMTUtils.addStylesheetResourceInHead(context, context.response(), "MooTools",
//						"scripts/plugins/spinner/spinner.css");
//			}
//		}
//		AjaxMTUtils.addScriptResourceInHead(context, context.response(), "MooTools", AjaxMTUtils.MOOTOOLS_WONDER_JS);
//	}

	public static String nameInContext(WOContext context, WOComponent component, AsyncRRElement element) {
		return (String) element.valueForBinding("name", context.elementID(), component);
	}

	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		WOActionResults result = null;
		WOComponent wocomponent = context.component();
		String nameInContext = nameInContext(context, wocomponent, this);
		boolean shouldHandleRequest = !context.wasActionInvoked() && context.wasFormSubmitted()
				&& nameInContext.equals(ERXAjaxApplication.ajaxSubmitButtonName(request));
		if (shouldHandleRequest) {
			String updateContainerID = AjaxMTUpdateContainer.updateContainerID(this, wocomponent);
			AjaxPTUpdateContainer.setUpdateContainerID(request, updateContainerID);
			context.setActionInvoked(true);
			result = (WOActionResults) valueForBinding("action", wocomponent);
			if (result == null) {
				result = handleRequest(request, context);
			}
			ERXAjaxApplication.enableShouldNotStorePage();
		} else {
			result = invokeChildrenAction(request, context);
		}
		return result;
	}
}
