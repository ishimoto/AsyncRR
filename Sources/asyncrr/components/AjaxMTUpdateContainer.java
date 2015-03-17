package asyncrr.components;

import asyncrr.ajax.*;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

import er.extensions.appserver.ERXWOContext;
import er.extensions.foundation.ERXValueUtilities;

public class AjaxMTUpdateContainer extends AsyncRRElement { //AjaxPTUpdateContainer {

	public AjaxMTUpdateContainer(String name, NSDictionary<String, WOAssociation> associations, WOElement children) {
		super(name, associations, children);
	}

	@Override
	protected String[] requiredScriptResources() {
		return AsyncRRElement.SCRIPT_RESOURCES_MOOTOOLS;
	}

	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		WOComponent component = context.component();
		String id = _containerID(context);
		if (associations().objectForKey("action") != null) {
			@SuppressWarnings("unused")
			WOActionResults results = (WOActionResults) valueForBinding("action", component);
			// ignore results
		}
		WOResponse response = AjaxPTUtils.createResponse(request, context);
		AjaxPTUtils.setPageReplacementCacheKey(context, id);
		if (hasChildrenElements()) {
			appendChildrenToResponse(response, context);
		}
		String onRefreshComplete = (String) valueForBinding("onRefreshComplete", component);
		if (onRefreshComplete != null) {
			AjaxPTUtils.appendScriptHeader(response);
			response.appendContentString(onRefreshComplete);
			AjaxPTUtils.appendScriptFooter(response);
		}
		// XXX schmied	if (AjaxPTModalDialog.isInDialog(context)) {
		//			AjaxPTUtils.appendScriptHeader(response);
		//			response.appendContentString("AMD.contentUpdated();");
		//			AjaxPTUtils.appendScriptFooter(response);
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

//	@Override
//	protected void addRequiredWebResources(WOResponse response, WOContext context) {
//		AjaxMTUtils.addScriptResourceInHead(context, context.response(), "MooTools", AjaxMTUtils.MOOTOOLS_CORE_JS);
//		//AjaxMTUtils.addScriptResourceInHead(context, context.response(), "MooTools", AjaxMTUtils.MOOTOOLS_MORE_JS);
//		Boolean useSpinner = (Boolean) valueForBinding("useSpinner", Boolean.FALSE, context.component());
//		if (useSpinner.booleanValue()) {
//			Boolean useDefaultSpinnerClass = (Boolean) valueForBinding("defaultSpinnerClass", Boolean.TRUE, context.component());
//			if (useDefaultSpinnerClass.booleanValue()) {
//				AjaxPTUtils.addStylesheetResourceInHead(context, context.response(), "MTAjax", "scripts/plugins/spinner/spinner.css");
//			}
//		}
//		AjaxMTUtils.addScriptResourceInHead(context, context.response(), "MooTools", AjaxMTUtils.MOOTOOLS_WONDER_JS);
//	}

	private NSDictionary<String, String> createAjaxOptions(WOComponent component) {
		NSMutableArray<AsyncRROption> ajaxOptionsArray = new NSMutableArray<AsyncRROption>();
		ajaxOptionsArray.addObject(new AsyncRROption("method", AsyncRROption.STRING));
		ajaxOptionsArray.addObject(new AsyncRROption("frequency", AsyncRROption.NUMBER));
		ajaxOptionsArray.addObject(new AsyncRROption("initialDelay", AsyncRROption.NUMBER));
		ajaxOptionsArray.addObject(new AsyncRROption("delay", AsyncRROption.NUMBER));
		ajaxOptionsArray.addObject(new AsyncRROption("limit", AsyncRROption.NUMBER));
		ajaxOptionsArray.addObject(new AsyncRROption("encoding", AsyncRROption.STRING));
		ajaxOptionsArray.addObject(new AsyncRROption("emulation", AsyncRROption.BOOLEAN));
		ajaxOptionsArray.addObject(new AsyncRROption("headers", AsyncRROption.ARRAY));
		ajaxOptionsArray.addObject(new AsyncRROption("isSuccess", AsyncRROption.FUNCTION));
		ajaxOptionsArray.addObject(new AsyncRROption("evalScripts", AsyncRROption.BOOLEAN));
		ajaxOptionsArray.addObject(new AsyncRROption("evalResponse", AsyncRROption.BOOLEAN));
		ajaxOptionsArray.addObject(new AsyncRROption("urlEncoded", AsyncRROption.BOOLEAN));
		ajaxOptionsArray.addObject(new AsyncRROption("async", AsyncRROption.BOOLEAN));
		ajaxOptionsArray.addObject(new AsyncRROption("noCache", AsyncRROption.BOOLEAN));
		ajaxOptionsArray.addObject(new AsyncRROption("onRequest", AsyncRROption.FUNCTION));
		ajaxOptionsArray.addObject(new AsyncRROption("onCancel", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("onComplete", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("onSuccess", AsyncRROption.FUNCTION_2));
		ajaxOptionsArray.addObject(new AsyncRROption("onFailure", AsyncRROption.FUNCTION_1));
		ajaxOptionsArray.addObject(new AsyncRROption("onException", AsyncRROption.FUNCTION_2));
		ajaxOptionsArray.addObject(new AsyncRROption("useSpinner", AsyncRROption.BOOLEAN));
		ajaxOptionsArray.addObject(new AsyncRROption("spinnerTarget", AsyncRROption.STRING));
		ajaxOptionsArray.addObject(new AsyncRROption("spinnerOptions", AsyncRROption.DICTIONARY));
		NSMutableDictionary<String, String> options = AsyncRROption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
		options.setObjectForKey("'get'", "method");
		if (options.objectForKey("evalScripts") == null) {
			options.setObjectForKey("true", "evalScripts");
		}
		AjaxPTUpdateContainer.expandInsertionFromOptions(options, this, component);
		return options;
	}

	public static NSDictionary<String, String> removeDefaultOptions(NSDictionary<String, String> options) {
		// PROTOTYPE OPTIONS
		NSMutableDictionary<String, String> mutableOptions = options.mutableClone();
		if ("'get'".equals(mutableOptions.objectForKey("method"))) {
			mutableOptions.removeObjectForKey("method");
		}
		if ("true".equals(mutableOptions.objectForKey("evalScripts"))) {
			mutableOptions.removeObjectForKey("evalScripts");
		}
		if ("true".equals(mutableOptions.objectForKey("async"))) {
			mutableOptions.removeObjectForKey("async");
		}
		return mutableOptions;
	}

	protected boolean shouldRenderContainer(WOComponent component) {
		return !booleanValueForBinding("optional", false, component) || AjaxPTUpdateContainer.currentUpdateContainerID() == null;
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
				String elementName = (String) valueForBinding("elementName", "div", component);
				String id = _containerID(context);
				AjaxPTUpdateContainer.setCurrentUpdateContainerID(_containerID(context));
				response.appendContentString("<" + elementName + " ");
				appendTagAttributeToResponse(response, "id", id);
				appendTagAttributeToResponse(response, "class", valueForBinding("class", component));
				appendTagAttributeToResponse(response, "style", valueForBinding("style", component));
				appendTagAttributeToResponse(response, "data-updateUrl", AjaxPTUtils.ajaxComponentActionUrl(context));
				response.appendContentString(">");
				if (hasChildrenElements()) {
					appendChildrenToResponse(response, context);
				}
				response.appendContentString("</" + elementName + ">");
// XXX schmied ???		addRequiredWebResources(response, context);
				// super.appendToResponse(response, context);
				NSDictionary<String, String> options = createAjaxOptions(component);
				Object frequency = valueForBinding("frequency", component);
				String observeFieldID = (String) valueForBinding("observeFieldID", component);
				boolean skipFunction = frequency == null && observeFieldID == null
						&& booleanValueForBinding("skipFunction", false, component);
				if (!skipFunction) {
					AjaxPTUtils.appendScriptHeader(response);
					if (frequency != null) {
						boolean isNotZero = true;
						try {
							float numberFrequency = ERXValueUtilities.floatValue(frequency);
							if (numberFrequency == 0.0) {
								isNotZero = false;
							}
						} catch (RuntimeException e) {
							throw new IllegalStateException("Error parsing float from value : <" + frequency + ">");
						}
						if (isNotZero) {
							boolean canStop = false;
							boolean stopped = false;
							if (associations().objectForKey("stopped") != null) {
								canStop = true;
								stopped = booleanValueForBinding("stopped", false, component);
							}
							response.appendContentString("MTAUC.registerPeriodic('" + id + "'," + canStop + "," + stopped
									+ ",");
							AjaxPTOptions.appendToResponse(options, response, context);
							response.appendContentString(");");
						}
					}
					if (observeFieldID != null) {
						boolean fullSubmit = booleanValueForBinding("fullSubmit", false, component);
						AjaxPTObserveField.appendToResponse(response, context, this, observeFieldID, false, id, fullSubmit,
								createObserveFieldOptions(component));
					}
					response.appendContentString("MTAUC.register('" + id + "'");
					NSDictionary<String, String> nonDefaultOptions = AjaxPTUpdateContainer.removeDefaultOptions(options);
					if (nonDefaultOptions.count() > 0) {
						response.appendContentString(", ");
						AjaxPTOptions.appendToResponse(nonDefaultOptions, response, context);
					}
					response.appendContentString(");");
					AjaxPTUtils.appendScriptFooter(response);
				}
			} finally {
				AjaxPTUpdateContainer.setCurrentUpdateContainerID(previousUpdateContainerID);
			}
		}
	}

	public NSMutableDictionary<String, String> createObserveFieldOptions(WOComponent component) {
		NSMutableArray<AsyncRROption> ajaxOptionsArray = new NSMutableArray<AsyncRROption>();
		ajaxOptionsArray.addObject(new AsyncRROption("observeFieldFrequency", AsyncRROption.NUMBER));
		NSMutableDictionary<String, String> options = AsyncRROption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
		return options;
	}
}
