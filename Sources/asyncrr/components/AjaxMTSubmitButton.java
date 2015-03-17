package asyncrr.components;

import asyncrr.ajax.*;

import com.webobjects.appserver.*;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.foundation.*;

import er.extensions.appserver.ajax.ERXAjaxApplication;
import er.extensions.components._private.ERXWOForm;
import er.extensions.foundation.ERXProperties;

public class AjaxMTSubmitButton extends AsyncRRElement {

	// MS: If you change this value, make sure to change it in ERXAjaxApplication
	public static final String KEY_AJAX_SUBMIT_BUTTON_NAME = "AJAX_SUBMIT_BUTTON_NAME";
	// MS: If you change this value, make sure to change it in ERXAjaxApplication and in wonder.js
	public static final String KEY_PARTIAL_FORM_SENDER_ID = "_partialSenderID";

	public AjaxMTSubmitButton(String name, NSDictionary<String, WOAssociation> associations, WOElement children) {
		super(name, associations, children);
	}

	@Override
	protected String[] requiredScriptResources() {
		return AsyncRRElement.SCRIPT_RESOURCES_MOOTOOLS;
	}

	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		WOComponent component = context.component();
		WOActionResults result = (WOActionResults) valueForBinding("action", component);
		if (ERXAjaxApplication.isAjaxReplacement(request)) {
			AjaxPTUtils.setPageReplacementCacheKey(context, (String) valueForBinding("replaceID", component));
		} else if (result == null || booleanValueForBinding("ignoreActionResponse", false, component)) {
			WOResponse response = AjaxPTUtils.createResponse(request, context);
			String onClickServer = (String) valueForBinding("onClickServer", component);
			if (onClickServer != null) {
				AjaxPTUtils.appendScriptHeaderIfNecessary(request, response);
				response.appendContentString(onClickServer);
				AjaxPTUtils.appendScriptFooterIfNecessary(request, response);
			}
			result = response;
		} else {
			String updateContainerID = AjaxMTUpdateContainer.updateContainerID(this, component);
			if (updateContainerID != null) {
				AjaxPTUtils.setPageReplacementCacheKey(context, updateContainerID);
			}
		}
		return result;
	}

	public static boolean isAjaxSubmit(WORequest request) {
		return request.formValueForKey(KEY_AJAX_SUBMIT_BUTTON_NAME) != null;
	}

	public boolean disabledInComponent(WOComponent component) {
		return booleanValueForBinding("disabled", false, component);
	}

	public String nameInContext(WOContext context, WOComponent component) {
		return (String) valueForBinding("name", context.elementID(), component);
	}

	private NSMutableDictionary<String, String> createAjaxOptions(WOComponent component) {
		NSMutableArray<AsyncRROption> ajaxOptionsArray = new NSMutableArray<AsyncRROption>();
		ajaxOptionsArray.addObject(new AsyncRROption("onCancel", AsyncRROption.FUNCTION));
		ajaxOptionsArray.addObject(new AsyncRROption("onComplete", AsyncRROption.FUNCTION));
		ajaxOptionsArray.addObject(new AsyncRROption("onException", AsyncRROption.FUNCTION));
		ajaxOptionsArray.addObject(new AsyncRROption("onFailure", AsyncRROption.FUNCTION));
		ajaxOptionsArray.addObject(new AsyncRROption("onRequest", AsyncRROption.FUNCTION));
		ajaxOptionsArray.addObject(new AsyncRROption("onSuccess", AsyncRROption.FUNCTION_2));
		ajaxOptionsArray.addObject(new AsyncRROption("evalScripts", AsyncRROption.BOOLEAN));
		ajaxOptionsArray.addObject(new AsyncRROption("async", AsyncRROption.BOOLEAN));
		ajaxOptionsArray.addObject(new AsyncRROption("useSpinner", AsyncRROption.BOOLEAN));
		ajaxOptionsArray.addObject(new AsyncRROption("spinnerTarget", AsyncRROption.STRING));
		ajaxOptionsArray.addObject(new AsyncRROption("spinnerOptions", AsyncRROption.DICTIONARY));
		String name = nameInContext(component.context(), component);
		NSMutableDictionary<String, String> options = AsyncRROption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
		fillInAjaxOptions(this, component, name, options);
		return options;
	}

//	@Override
//	public void addRequiredWebResources(WOResponse response, WOContext context) {
//		AjaxMTUtils.addScriptResourceInHead(context, context.response(), "MooTools", AjaxMTUtils.MOOTOOLS_CORE_JS);
//		//AjaxMTUtils.addScriptResourceInHead(context, context.response(), "MooTools", AjaxMTUtils.MOOTOOLS_MORE_JS);
//		Boolean useSpinner = (Boolean) valueForBinding("useSpinner", Boolean.FALSE, context.component());
//		if (useSpinner.booleanValue()) {
//			Boolean useDefaultSpinnerClass = (Boolean) valueForBinding("defaultSpinnerClass", Boolean.TRUE, context.component());
//			if (useDefaultSpinnerClass.booleanValue()) {
//				AjaxPTUtils.addStylesheetResourceInHead(context, context.response(), "MooTools",
//						"scripts/plugins/spinner/spinner.css");
//			}
//		}
//		AjaxMTUtils.addScriptResourceInHead(context, context.response(), "MooTools", AjaxMTUtils.MOOTOOLS_WONDER_JS);
//	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		WOComponent component = context.component();
		String functionName = (String) valueForBinding("functionName", null, component);
		String formName = (String) valueForBinding("formName", component);
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
		StringBuffer onClickBuffer = new StringBuffer();
		// JavaScript function to be fired before submit is sent i.e. confirm();
		String onClickBefore = (String) valueForBinding("onClickBefore", component);
		if (onClickBefore != null) {
			onClickBuffer.append("if(").append(onClickBefore).append(") {");
		}
		String updateContainerID = (String) valueForBinding("updateContainerID", component);
		// Needs to be refactored. Same function as MoAjaxUpdateLink
		// Maybe create a helper class with a static function that takes the component as an argument?
		// Could add it to MoAjaxUpdateLink like addEffect.
//		String beforeEffect = (String) valueForBinding("beforeEffect", component);
//		if (beforeEffect != null) {
//			String beforeEffectID = (String) valueForBinding("beforeEffectID", component);
//			String beforeEffectDuration = (String) valueForBinding("beforeEffectDuration", component);
//			String beforeEffectProperty = (String) valueForBinding("beforeEffectProperty", component);
//			String beforeEffectStart = (String) valueForBinding("beforeEffectStart", component);
//			if (beforeEffectID == null) {
//				beforeEffectID = AjaxPTUpdateContainer.currentUpdateContainerID();
//				if (beforeEffectID == null) {
//					beforeEffectID = updateContainerID;
//				}
//			}
//			if (beforeEffect.equals("tween")) {
//				if (beforeEffectDuration != null) {
//					onClickBuffer.append("$('").append(beforeEffectID).append("').set('tween', { duration: '")
//							.append(beforeEffectDuration).append("', property: '" + beforeEffectProperty + "' });");
//				} else {
//					onClickBuffer.append("$('").append(beforeEffectID)
//							.append("').set('tween', { property: '" + beforeEffectProperty + "' });");
//				}
//				onClickBuffer.append("$('").append(beforeEffectID).append("').get('tween').start(").append(beforeEffectStart)
//						.append(").chain(function() {");
//			} else if (beforeEffect.equals("morph")) {
//				if (beforeEffectDuration != null) {
//					onClickBuffer.append("$('").append(beforeEffectID).append("').set('morph', { duration: '")
//							.append(beforeEffectDuration).append("' });");
//				}
//				onClickBuffer.append("$('").append(beforeEffectID).append("').get('morph').start('." + beforeEffectStart + "'")
//						.append(").chain(function() {");
//			} else if (beforeEffect.equals("slide")) {
//				String mode = (String) valueForBinding("effectSlideMode", component);
//				String transition = (String) valueForBinding("beforeEffectTransition", component);
//				onClickBuffer.append("$('").append(beforeEffectID).append("').set('slide'");
//				if (beforeEffectDuration != null || mode != null) {
//					onClickBuffer.append(", { ");
//					if (beforeEffectDuration != null) {
//						onClickBuffer.append("duration: '").append(beforeEffectDuration).append('\'')
//								.append(mode != null || transition != null ? "," : "");
//					}
//					if (mode != null) {
//						onClickBuffer.append("mode: '").append(mode).append('\'').append(transition != null ? "," : "");
//					}
//					if (transition != null) {
//						onClickBuffer.append("transition: ").append(transition);
//					}
//					onClickBuffer.append('}');
//				}
//				onClickBuffer.append("); $('").append(beforeEffectID).append("').get('slide').slide")
//						.append(ERXStringUtilities.capitalize(beforeEffectProperty)).append("().chain(function() {");
//			} else if (beforeEffect.equals("highlight")) {
//				if (beforeEffectDuration != null) {
//					onClickBuffer.append("$('").append(beforeEffectID).append("').set('tween', { duration: '")
//							.append(beforeEffectDuration).append("', property: 'background-color'});");
//				} else {
//					onClickBuffer.append("$('").append(beforeEffectID)
//							.append("').set('tween', { property: 'background-color' });");
//				}
//				onClickBuffer.append("$('").append(updateContainerID).append("').get('tween').start('")
//						.append(beforeEffectProperty != null ? beforeEffectProperty : "#ffff88', '#ffffff")
//						.append("').chain(function() { ");
//			}
//		}
		if (updateContainerID != null) {
			onClickBuffer.append("MTASB.update('").append(updateContainerID).append("', ");
		} else {
			onClickBuffer.append("MTASB.request(");
		}
		onClickBuffer.append(formReference);
		if (valueForBinding("functionName", component) != null) {
			onClickBuffer.append(", additionalParams");
		} else {
			onClickBuffer.append(", null");
		}
		onClickBuffer.append(',');
		NSMutableDictionary<String, String> options = createAjaxOptions(component);
//		String effect = (String) valueForBinding("effect", component);
//		String afterEffect = (String) valueForBinding("afterEffect", component);
//		if (effect != null) {
//			String duration = (String) valueForBinding("effectDuration", component);
//			String property = (String) valueForBinding("effectProperty", component);
//			String start = (String) valueForBinding("effectStart", component);
//			String mode = (String) valueForBinding("effectSlideMode", component);
//			AjaxMTUpdateLink.addEffect(options, effect, updateContainerID, property, start, duration, mode);
//		} else if (afterEffect != null) {
//			String duration = (String) valueForBinding("afterEffectDuration", component);
//			String property = (String) valueForBinding("afterEffectProperty", component);
//			String start = (String) valueForBinding("afterEffectStart", component);
//			String afterEffectID = (String) valueForBinding("afterEffectID", component);
//			String mode = (String) valueForBinding("effectSlideMode", component);
//			if (afterEffectID == null) {
//				afterEffectID = AjaxPTUpdateContainer.currentUpdateContainerID() != null ? AjaxPTUpdateContainer
//						.currentUpdateContainerID() : updateContainerID;
//			}
//			AjaxMTUpdateLink.addEffect(options, afterEffect, afterEffectID, property, start, duration, mode);
//		}
		AjaxPTOptions.appendToBuffer(options, onClickBuffer, context);
		onClickBuffer.append(')');
		String onClick = (String) valueForBinding("onClick", component);
		if (onClick != null) {
			onClickBuffer.append("; ").append(onClick);
		}
//		if (beforeEffect != null) {
//			onClickBuffer.append("}.bind(this));");
//		}
		if (onClickBefore != null) {
			onClickBuffer.append('}');
		}
		if (functionName != null) {
			AjaxPTUtils.appendScriptHeader(response);
			response.appendContentString(functionName + " = function(additionalParams) { " + onClickBuffer + " }\n");
			AjaxPTUtils.appendScriptFooter(response);
		}
		if (showUI) {
			boolean disabled = disabledInComponent(component);
			String elementName = (String) valueForBinding("elementName", "a", component);
			if (showButton) {
				response.appendContentString("<input ");
				appendTagAttributeToResponse(response, "type", "button");
				String name = nameInContext(context, component);
				appendTagAttributeToResponse(response, "name", name);
				appendTagAttributeToResponse(response, "value", valueForBinding("value", component));
				if (disabled) {
					appendTagAttributeToResponse(response, "disabled", "disabled");
				}
			} else {
				boolean isATag = "a".equalsIgnoreCase(elementName);
				if (isATag) {
					response.appendContentString("<a href = \"javascript:void(0)\" ");
				} else {
					response.appendContentString("<" + elementName + " ");
				}
			}
			String classString = (String) valueForBinding("class", component);
			classString = (classString != null) ? classString + " m-a-s-b" : "m-a-s-b";
			appendTagAttributeToResponse(response, "class", classString);
			appendTagAttributeToResponse(response, "style", valueForBinding("style", component));
			appendTagAttributeToResponse(response, "id", valueForBinding("id", component));
			if (functionName == null) {
				appendTagAttributeToResponse(response, "onclick", onClickBuffer.toString());
			} else {
				appendTagAttributeToResponse(response, "onclick", functionName + "()");
			}
			if (showButton) {
				response.appendContentString(" />");
			} else {
				response.appendContentString(">");
				if (hasChildrenElements()) {
					appendChildrenToResponse(response, context);
				}
				response.appendContentString("</" + elementName + ">");
			}
		}
		super.appendToResponse(response, context);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void fillInAjaxOptions(IAsyncRRElement element, WOComponent component, String submitButtonName, NSMutableDictionary options) {
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
		AjaxPTUpdateContainer.expandInsertionFromOptions(options, element, component);
	}

	@Override
	public WOActionResults invokeAction(WORequest worequest, WOContext wocontext) {
		WOActionResults result = null;
		WOComponent wocomponent = wocontext.component();
		String nameInContext = nameInContext(wocontext, wocomponent);
		boolean shouldHandleRequest = (!disabledInComponent(wocomponent) && wocontext.wasFormSubmitted())
				&& ((wocontext.isMultipleSubmitForm() && nameInContext.equals(worequest.formValueForKey(KEY_AJAX_SUBMIT_BUTTON_NAME))) || !wocontext
						.isMultipleSubmitForm());
		if (shouldHandleRequest) {
			String updateContainerID = AjaxMTUpdateContainer.updateContainerID(this, wocomponent);
			AjaxPTUpdateContainer.setUpdateContainerID(worequest, updateContainerID);
			wocontext.setActionInvoked(true);
			result = handleRequest(worequest, wocontext);
			ERXAjaxApplication.enableShouldNotStorePage();
		}
		return result;
	}
}
