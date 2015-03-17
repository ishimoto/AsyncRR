package asyncrr.components;

import asyncrr.ajax.*;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

import er.extensions.appserver.ERXWOContext;
import er.extensions.foundation.ERXPropertyListSerialization;

public class AjaxJQUpdateContainer extends AsyncRRElement {

	public AjaxJQUpdateContainer(String name, NSDictionary<String, WOAssociation> associations, WOElement children) {
		super(name, associations, children);
	}

	@Override
	protected String[] requiredScriptResources() {
		return AsyncRRElement.SCRIPT_RESOURCES_JQUERY;
	}

	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		WOComponent component = context.component();
		String id = _containerID(context);
		if (associations().objectForKey("action") != null) {
			@SuppressWarnings("unused")
			WOActionResults results = (WOActionResults) valueForBinding("action", component);
		}
		WOResponse response = AjaxPTUtils.createResponse(request, context);
		AjaxPTUtils.setPageReplacementCacheKey(context, id);
		if (hasChildrenElements()) {
			appendChildrenToResponse(response, context);
		}
		//TODO
		/*
		String onRefreshComplete = (String) valueForBinding("onRefreshComplete", component);
		if(onRefreshComplete != null) {
		AjaxUtils.appendScriptHeader(response);
		response.appendContentString(onRefreshComplete);
		AjaxUtils.appendScriptFooter(response);
		}
		*/
		//TODO

		// XXX schmied	if (AjaxJQModalDialog.isInDialog(context)) {
		//AjaxUtils.appendScriptHeader(response);
		//response.appendContentString("AMD.contentUpdated();");
		//AjaxUtils.appendScriptFooter(response);
		//		}
		return null;
	}

	public static String updateContainerID(AsyncRRElement element, WOComponent component) {
		return AjaxPTUpdateContainer.updateContainerID(element, component);
	}

	@Override
	protected String _containerID(WOContext context) {
		String id = (String) valueForBinding("id", context.component());
		if (id == null) {
			id = ERXWOContext.safeIdentifierName(context, false);
		}
		return id;
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		WOComponent component = context.component();
		if (!shouldRenderContainer(component)) {
			if (hasChildrenElements()) {
				appendChildrenToResponse(response, context);
			}
			super.appendToResponse(response, context);
		} else {
			String previousUpdateContainerID = AjaxPTUpdateContainer.currentUpdateContainerID();
			try {
				NSDictionary options = _options(component);
				String elementName = (String) valueForBinding("elementName", "div", component);
				AjaxPTUpdateContainer.setCurrentUpdateContainerID(_containerID(context));
				response.appendContentString("<" + elementName);
				String id = _containerID(context);
				appendTagAttributeToResponse(response, "id", id);
				appendTagAttributeToResponse(response, "class", valueForBinding("class", component));
				appendTagAttributeToResponse(response, "style", valueForBinding("style", component));
				appendTagAttributeToResponse(response, "data-wonder-id", "AUC");
				appendTagAttributeToResponse(response, "data-wonder-options",
						ERXPropertyListSerialization.jsonStringFromPropertyList(options));
				response.appendContentString(">");
				if (hasChildrenElements()) {
					appendChildrenToResponse(response, context);
				}
				response.appendContentString("</" + elementName + ">");
				super.appendToResponse(response, context);
				//	Object frequency = valueForBinding("minTimeout", component);
				//	String observeFieldID = (String) valueForBinding("observeFieldID", component);
				//	boolean skipFunction = frequency == null && observeFieldID == null && booleanValueForBinding("skipFunction", false, component);
				/*
				* if(! skipFunction && AjaxUtils.isAjaxRequest(context.request())) {
				AjaxUtils.appendScriptHeader(response);
				response.appendContentString("WOnder.AUC.initialize($j(\"#" + id + "\"));");
				AjaxUtils.appendScriptFooter(response);
				}
				*/
				/*
				if(! skipFunction) {
				AjaxUtils.appendScriptHeader(response);
				if(frequency != null) {
				boolean isNotZero = true;
				try {
				float numberFrequency = ERXValueUtilities.floatValue(frequency);
				if(numberFrequency == 0.0) {
				isNotZero = false;
				}
				}
				catch(RuntimeException e) {
				throw new IllegalStateException("Error parsing float from value: <" + frequency + ">");
				}
				if(isNotZero) {
				boolean canStop = false;
				boolean stopped = false;
				if(associations().objectForKey("stopped") != null) {
				canStop = true;
				stopped = booleanValueForBinding("stopped", false, component);
				}
				response.appendContentString("AUC.registerPeriodic('" + id + "'," + canStop + "," + stopped + ",");
				AjaxOptions.appendToResponse(options, response, context);
				response.appendContentString(");");
				}
				}
				if(observeFieldID != null) {
				boolean fullSubmit = booleanValueForBinding("fullSubmit", false, component);
				AjaxObserveField.appendToResponse(response, context, this, observeFieldID, false, id, fullSubmit, createObserveFieldOptions(component));
				}
				response.appendContentString("AUC.register('" + id + "'");
				NSDictionary nonDefaultOptions = AjaxUpdateContainer.removeDefaultOptions(options);
				if(nonDefaultOptions.count() > 0) {
				response.appendContentString(", ");
				AjaxOptions.appendToResponse(nonDefaultOptions, response, context);
				}
				response.appendContentString(");");
				AjaxUtils.appendScriptFooter(response);
				}
				*/
			} finally {
				AjaxPTUpdateContainer.setCurrentUpdateContainerID(previousUpdateContainerID);
			}
		}
	}

//	@Override
//	protected void addRequiredWebResources(WOResponse response, WOContext context) {
//		AjaxJQUtils.addScriptResourceInHead(context, response, "JQuery", AjaxJQUtils.JQUERY_JS);
//		if (hasBinding("minTimeout")) {
//			AjaxJQUtils.addScriptResourceInHead(context, response, "JQuery", "javascript/plugins/periodical/jquery.periodicalupdater.js");
//		}
//		AjaxJQUtils.addScriptResourceInHead(context, response, "JQuery", AjaxJQUtils.JQUERY_WONDER_JS);
//	}

	protected boolean shouldRenderContainer(WOComponent component) {
		return !booleanValueForBinding("optional", false, component) || AjaxPTUpdateContainer.currentUpdateContainerID() == null;
	}

	protected NSDictionary<String, String> _options(WOComponent component) {
		NSMutableArray<AsyncRROption> ajaxOptionsArray = new NSMutableArray<AsyncRROption>();
		ajaxOptionsArray.addObject(new AsyncRROption("async", AsyncRROption.BOOLEAN));
		ajaxOptionsArray.addObject(new AsyncRROption("cache", AsyncRROption.BOOLEAN));
		ajaxOptionsArray.addObject(new AsyncRROption("complete", AsyncRROption.FUNCTION_2));
		ajaxOptionsArray.addObject(new AsyncRROption("contents", AsyncRROption.DICTIONARY));
		ajaxOptionsArray.addObject(new AsyncRROption("contentType", AsyncRROption.STRING));
		ajaxOptionsArray.addObject(new AsyncRROption("delegate", AsyncRROption.STRING));
		ajaxOptionsArray.addObject(new AsyncRROption("minTimeout", AsyncRROption.NUMBER));
		ajaxOptionsArray.addObject(new AsyncRROption("maxTimeout", AsyncRROption.NUMBER));
		ajaxOptionsArray.addObject(new AsyncRROption("multiplier", AsyncRROption.NUMBER));
		ajaxOptionsArray.addObject(new AsyncRROption("maxCalls", AsyncRROption.NUMBER));
		ajaxOptionsArray.addObject(new AsyncRROption("observeFields", AsyncRROption.ARRAY));
		ajaxOptionsArray.addObject(new AsyncRROption("headers", AsyncRROption.ARRAY));
		NSDictionary<String, String> options = AsyncRROption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
		options.takeValueForKey(AjaxPTUtils.ajaxComponentActionUrl(component.context()), "updateUrl");
		if (hasBinding("subscribes")) {
			options.takeValueForKey(stringValueForBinding("subscribes", component), "subscribes");
		}
		return options;
	}

	public NSMutableDictionary<String, String> createObserveFieldOptions(WOComponent component) {
		NSMutableArray<AsyncRROption> ajaxOptionsArray = new NSMutableArray<AsyncRROption>();
		ajaxOptionsArray.addObject(new AsyncRROption("observeFieldFrequency", AsyncRROption.NUMBER));
		NSMutableDictionary<String, String> options = AsyncRROption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
		return options;
	}

	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		WOActionResults results;
		if (shouldRenderContainer(context.component())) {
			String previousUpdateContainerID = AjaxPTUpdateContainer.currentUpdateContainerID();
			try {
				AjaxPTUpdateContainer.setCurrentUpdateContainerID(_containerID(context));
				results = super.invokeAction(request, context);
			} finally {
				AjaxPTUpdateContainer.setCurrentUpdateContainerID(previousUpdateContainerID);
			}
		} else {
			results = super.invokeAction(request, context);
		}
		return results;
	}

	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		if (shouldRenderContainer(context.component())) {
			String previousUpdateContainerID = AjaxPTUpdateContainer.currentUpdateContainerID();
			try {
				AjaxPTUpdateContainer.setCurrentUpdateContainerID(_containerID(context));
				super.takeValuesFromRequest(request, context);
			} finally {
				AjaxPTUpdateContainer.setCurrentUpdateContainerID(previousUpdateContainerID);
			}
		} else {
			super.takeValuesFromRequest(request, context);
		}
	}
}
