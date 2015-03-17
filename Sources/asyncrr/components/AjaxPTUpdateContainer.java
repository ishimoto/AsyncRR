package asyncrr.components;

import asyncrr.ajax.*;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

import er.extensions.appserver.ERXWOContext;
import er.extensions.appserver.ajax.ERXAjaxApplication;
import er.extensions.foundation.ERXValueUtilities;

public class AjaxPTUpdateContainer extends AsyncRRElement {

	private static final String CURRENT_UPDATE_CONTAINER_ID_KEY = "er.ajax.AjaxUpdateContainer.currentID";

	public AjaxPTUpdateContainer(String name, NSDictionary<String, WOAssociation> associations, WOElement children) {
		super(name, associations, children);
	}

	@Override
	protected String[] requiredScriptResources() {
		return AsyncRRElement.SCRIPT_RESOURCES_PROTOTYPE;
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

	/**
	 * Adds all required resources.
	 */
//	@Override
//	protected void addRequiredWebResources(WOResponse response, WOContext context) {
//		addScriptResourceInHead(context, response, "prototype.js");
//		//addScriptResourceInHead(context, response, "effects.js");
//		addScriptResourceInHead(context, response, "prototype_wo.js");
//	}

	protected boolean shouldRenderContainer(WOComponent component) {
		return !booleanValueForBinding("optional", false, component) || currentUpdateContainerID() == null;
	}

	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		if (shouldRenderContainer(context.component())) {
			String previousUpdateContainerID = currentUpdateContainerID();
			try {
				setCurrentUpdateContainerID(_containerID(context));
				super.takeValuesFromRequest(request, context);
			} finally {
				setCurrentUpdateContainerID(previousUpdateContainerID);
			}
		} else {
			super.takeValuesFromRequest(request, context);
		}
	}

	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		WOActionResults results;
		if (shouldRenderContainer(context.component())) {
			String previousUpdateContainerID = currentUpdateContainerID();
			try {
				setCurrentUpdateContainerID(_containerID(context));
				results = super.invokeAction(request, context);
			} finally {
				setCurrentUpdateContainerID(previousUpdateContainerID);
			}
		} else {
			results = super.invokeAction(request, context);
		}
		return results;
	}

	private NSDictionary<String, String> createAjaxOptions(WOComponent component) {
		// PROTOTYPE OPTIONS
		NSMutableArray<AsyncRROption> ajaxOptionsArray = new NSMutableArray<AsyncRROption>();
		ajaxOptionsArray.addObject(new AsyncRROption("frequency", AsyncRROption.NUMBER));
		ajaxOptionsArray.addObject(new AsyncRROption("decay", AsyncRROption.NUMBER));
		ajaxOptionsArray.addObject(new AsyncRROption("onLoading", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("onComplete", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("onSuccess", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("onFailure", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("onException", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("insertion", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("evalScripts", Boolean.TRUE, AsyncRROption.BOOLEAN));
		ajaxOptionsArray.addObject(new AsyncRROption("asynchronous", Boolean.TRUE, AsyncRROption.BOOLEAN));
		ajaxOptionsArray.addObject(new AsyncRROption("method", "get", AsyncRROption.STRING));
		ajaxOptionsArray.addObject(new AsyncRROption("evalScripts", Boolean.TRUE, AsyncRROption.BOOLEAN));
		ajaxOptionsArray.addObject(new AsyncRROption("parameters", AsyncRROption.STRING));
		NSMutableDictionary<String, String> options = AsyncRROption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
		expandInsertionFromOptions(options, this, component);
		return options;
	}

	public static void expandInsertionFromOptions(NSMutableDictionary<String, String> options, IAsyncRRElement element, WOComponent component) {
		// PROTOTYPE EFFECTS
		String insertionDuration = (String) element.valueForBinding("insertionDuration", component);
		String beforeInsertionDuration = (String) element.valueForBinding("beforeInsertionDuration", component);
		if (beforeInsertionDuration == null) {
			beforeInsertionDuration = insertionDuration;
		}
		String afterInsertionDuration = (String) element.valueForBinding("afterInsertionDuration", component);
		if (afterInsertionDuration == null) {
			afterInsertionDuration = insertionDuration;
		}
		String insertion = options.objectForKey("insertion");
		String expandedInsertion = expandInsertion(insertion, beforeInsertionDuration, afterInsertionDuration);
		if (expandedInsertion != null) {
			options.setObjectForKey(expandedInsertion, "insertion");
		}
	}

	public static String expandInsertion(String originalInsertion, String beforeDuration, String afterDuration) {
		// PROTOTYPE EFFECTS
		String expandedInsertion = originalInsertion;
		if (originalInsertion != null && originalInsertion.startsWith("Effect.")) {
			String effectPairName = originalInsertion.substring("Effect.".length());
			expandedInsertion = "AUC.insertionFunc('" + effectPairName + "', " + beforeDuration + "," + afterDuration + ")";
		}
		return expandedInsertion;
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
		if ("true".equals(mutableOptions.objectForKey("asynchronous"))) {
			mutableOptions.removeObjectForKey("asynchronous");
		}
		return mutableOptions;
	}

	public NSMutableDictionary<String, String> createObserveFieldOptions(WOComponent component) {
		NSMutableArray<AsyncRROption> ajaxOptionsArray = new NSMutableArray<AsyncRROption>();
		ajaxOptionsArray.addObject(new AsyncRROption("observeFieldFrequency", AsyncRROption.NUMBER));
		NSMutableDictionary<String, String> options = AsyncRROption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
		return options;
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
			String previousUpdateContainerID = currentUpdateContainerID();
			try {
				String elementName = (String) valueForBinding("elementName", "div", component);
				String id = _containerID(context);
				setCurrentUpdateContainerID(_containerID(context));
				response.appendContentString("<" + elementName + " ");
				appendTagAttributeToResponse(response, "id", id);
				appendTagAttributeToResponse(response, "class", valueForBinding("class", component));
				appendTagAttributeToResponse(response, "style", valueForBinding("style", component));
				appendTagAttributeToResponse(response, "data-updateUrl", AjaxPTUtils.ajaxComponentActionUrl(context));
				// appendTagAttributeToResponse(response, "woElementID", context.elementID());
				response.appendContentString(">");
				if (hasChildrenElements()) {
					appendChildrenToResponse(response, context);
				}
				response.appendContentString("</" + elementName + ">");
				super.appendToResponse(response, context);
				NSDictionary<String, String> options = createAjaxOptions(component);
				Object frequency = valueForBinding("frequency", component);
				String observeFieldID = (String) valueForBinding("observeFieldID", component);
				boolean skipFunction = frequency == null && observeFieldID == null
						&& booleanValueForBinding("skipFunction", false, component);
				if (!skipFunction) {
					AjaxPTUtils.appendScriptHeader(response);
					if (frequency != null) {
						// try to convert to a number to check whether it is 0
						boolean isNotZero = true;
						try {
							float numberFrequency = ERXValueUtilities.floatValue(frequency);
							if (numberFrequency == 0.0) {
								// set this only to false if it can be converted to 0
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
							response.appendContentString("AUC.registerPeriodic('" + id + "'," + canStop + "," + stopped
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
					response.appendContentString("AUC.register('" + id + "'");
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

	@Override
	protected String _containerID(WOContext context) {
		String id = (String) valueForBinding("id", context.component());
		if (id == null) {
			id = ERXWOContext.safeIdentifierName(context, false);
		}
		return id;
	}

	public static String updateContainerID(WORequest request) {
		return (String) ERXWOContext.contextDictionary().objectForKey(ERXAjaxApplication.KEY_UPDATE_CONTAINER_ID);
	}

	public static void setUpdateContainerID(WORequest request, String updateContainerID) {
		if (updateContainerID != null) {
			ERXWOContext.contextDictionary().setObjectForKey(updateContainerID, ERXAjaxApplication.KEY_UPDATE_CONTAINER_ID);
		}
	}

	public static boolean hasUpdateContainerID(WORequest request) {
		return updateContainerID(request) != null;
	}

	public static String currentUpdateContainerID() {
		return (String) ERXWOContext.contextDictionary().objectForKey(CURRENT_UPDATE_CONTAINER_ID_KEY);
	}

	public static void setCurrentUpdateContainerID(String updateContainerID) {
		if (updateContainerID == null) {
			ERXWOContext.contextDictionary().removeObjectForKey(CURRENT_UPDATE_CONTAINER_ID_KEY);
		} else {
			ERXWOContext.contextDictionary().setObjectForKey(updateContainerID, CURRENT_UPDATE_CONTAINER_ID_KEY);
		}
	}

	public static String updateContainerID(AsyncRRElement element, WOComponent component) {
		return updateContainerID(element, "updateContainerID", component);
	}

	public static String updateContainerID(AsyncRRElement element, String bindingName, WOComponent component) {
		String updateContainerID = (String) element.valueForBinding("updateContainerID", component);
		return updateContainerID(updateContainerID);
	}

	public static String updateContainerID(String updateContainerID) {
		if ("_parent".equals(updateContainerID)) {
			updateContainerID = currentUpdateContainerID();
		}
		return updateContainerID;
	}

	/**
	 * Creates or updates Ajax response so that the indicated AUC will get updated when the response is processed in the browser.
	 * Adds JavaScript like <code>AUC.update('SomeContainerID');</code>
	 * 
	 * @param updateContainerID
	 *                the HTML ID of the element implementing the AUC
	 * @param context
	 *                WOContext for response
	 */
	public static void updateContainerWithID(String updateContainerID, WOContext context) {
		String containerID = "'" + updateContainerID + "'";
		AjaxPTUtils.javascriptResponse("AUC.update(" + containerID + ");", context);
	}

	/**
	 * Creates or updates Ajax response so that the indicated AUC will get updated when the response is processed in the browser.
	 * If the container element does not exist, does nothing.
	 * Adds JavaScript like <code>if ( $('SomeContainerID') != null ) AUC.update('SomeContainerID');</code>
	 * 
	 * @param updateContainerID
	 *                the HTML ID of the element implementing the AUC
	 * @param context
	 *                WOContext for response
	 */
	public static void safeUpdateContainerWithID(String updateContainerID, WOContext context) {
		String containerID = "'" + updateContainerID + "'";
		AjaxPTUtils.javascriptResponse("if ( $(" + containerID + ") != null ) AUC.update(" + containerID + ");", context);
	}
}
