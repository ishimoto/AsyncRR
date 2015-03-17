package asyncrr.components;

import asyncrr.ajax.*;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

import er.extensions.appserver.ERXWOContext;
import er.extensions.appserver.ajax.ERXAjaxApplication;

public class AjaxPTObserveField extends AsyncRRElement {

	public AjaxPTObserveField(String name, NSDictionary<String, WOAssociation> associations, WOElement children) {
		super(name, associations, children);
	}

	@Override
	protected String[] requiredScriptResources() {
		return AsyncRRElement.SCRIPT_RESOURCES_PROTOTYPE;
	}

	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		WOResponse response = AjaxPTUtils.createResponse(request, context);
		return response;
	}

//	@Override
//	protected void addRequiredWebResources(WOResponse response, WOContext context) {
//		addScriptResourceInHead(context, response, "prototype.js");
//		addScriptResourceInHead(context, response, "prototype_wo.js");
//	}

	private NSMutableDictionary<String, String> createAjaxOptions(WOComponent component) {
		// PROTOTYPE OPTIONS
		NSMutableArray<AsyncRROption> ajaxOptionsArray = new NSMutableArray<AsyncRROption>();
		ajaxOptionsArray.addObject(new AsyncRROption("observeFieldFrequency", AsyncRROption.NUMBER));
		ajaxOptionsArray.addObject(new AsyncRROption("observeDelay", AsyncRROption.NUMBER));
		ajaxOptionsArray.addObject(new AsyncRROption("onCreate", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("onLoading", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("onComplete", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("onBeforeSubmit", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("onSuccess", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("onFailure", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("onException", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("insertion", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("evalScripts", AsyncRROption.BOOLEAN));
		NSMutableDictionary<String, String> options = AsyncRROption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
		return options;
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		super.appendToResponse(response, context);
		WOComponent component = context.component();
		String observeFieldID = stringValueForBinding("observeFieldID", component);
		String updateContainerID = AjaxPTUpdateContainer.updateContainerID(this, component);
		NSMutableDictionary<String, String> options = createAjaxOptions(component);
		boolean fullSubmit = booleanValueForBinding("fullSubmit", false, component);
		boolean observeFieldDescendents;
		if (observeFieldID != null) {
			observeFieldDescendents = false;
		} else {
			observeFieldDescendents = true;
			observeFieldID = stringValueForBinding("id", component);
			if (observeFieldID == null) {
				observeFieldID = ERXWOContext.safeIdentifierName(context, false);
			}
			String elementName = stringValueForBinding("elementName", "div", component);
			response.appendContentString("<" + elementName);
			appendTagAttributeToResponse(response, "id", observeFieldID);
			appendTagAttributeToResponse(response, "class", stringValueForBinding("class", component));
			appendTagAttributeToResponse(response, "style", stringValueForBinding("style", component));
			response.appendContentString(">");
			if (hasChildrenElements()) {
				appendChildrenToResponse(response, context);
			}
			response.appendContentString("</" + elementName + ">");
		}
		AjaxPTUtils.appendScriptHeader(response);
		AjaxPTObserveField.appendToResponse(response, context, this, observeFieldID, observeFieldDescendents, updateContainerID, fullSubmit,
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
		AjaxPTSubmitButton.fillInAjaxOptions(element, component, submitButtonName, observerOptions);
		String observeFieldFrequency = observerOptions.removeObjectForKey("observeFieldFrequency");
		if (observeDescendentFields) {
			response.appendContentString("ASB.observeDescendentFields");
		} else {
			response.appendContentString("ASB.observeField");
		}
		String observeDelay = observerOptions.removeObjectForKey("observeDelay");
		response.appendContentString("(");
		response.appendContentString(AjaxPTUtils.quote(updateContainerID));
		response.appendContentString(", ");
		response.appendContentString(AjaxPTUtils.quote(observeFieldID));
		response.appendContentString(", ");
		response.appendContentString(observeFieldFrequency);
		response.appendContentString(", ");
		response.appendContentString(String.valueOf(!fullSubmit));
		response.appendContentString(", ");
		response.appendContentString(observeDelay);
		response.appendContentString(", ");
		AjaxPTOptions.appendToResponse(observerOptions, response, context);
		response.appendContentString(");");
	}

	public static String nameInContext(WOContext context, WOComponent component, AsyncRRElement element) {
		return element.stringValueForBinding("name", context.elementID(), component);
	}

	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		WOActionResults result = null;
		WOComponent component = context.component();
		String nameInContext = nameInContext(context, component, this);
		boolean shouldHandleRequest = !context.wasActionInvoked() && context.wasFormSubmitted()
				&& nameInContext.equals(ERXAjaxApplication.ajaxSubmitButtonName(request));
		if (shouldHandleRequest) {
			String updateContainerID = AjaxPTUpdateContainer.updateContainerID(this, component);
			AjaxPTUpdateContainer.setUpdateContainerID(request, updateContainerID);
			context.setActionInvoked(true);
			result = (WOActionResults) valueForBinding("action", component);
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
