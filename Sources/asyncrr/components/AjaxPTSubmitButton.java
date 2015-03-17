package asyncrr.components;

import asyncrr.ajax.*;

import com.webobjects.appserver.*;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.foundation.*;

import er.extensions.appserver.ajax.ERXAjaxApplication;
import er.extensions.components._private.ERXWOForm;
import er.extensions.foundation.ERXProperties;

public class AjaxPTSubmitButton extends AsyncRRElement {

	// MS: If you change this value, make sure to change it in ERXAjaxApplication
	public static final String KEY_AJAX_SUBMIT_BUTTON_NAME = "AJAX_SUBMIT_BUTTON_NAME";
	// MS: If you change this value, make sure to change it in ERXAjaxApplication and in wonder.js
	public static final String KEY_PARTIAL_FORM_SENDER_ID = "_partialSenderID";

	public AjaxPTSubmitButton(String name, NSDictionary<String, WOAssociation> associations, WOElement children) {
		super(name, associations, children);
	}

	@Override
	protected String[] requiredScriptResources() {
		return AsyncRRElement.SCRIPT_RESOURCES_PROTOTYPE;
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
			String updateContainerID = AjaxPTUpdateContainer.updateContainerID(this, component);
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
		// PROTOTYPE OPTIONS
		NSMutableArray<AsyncRROption> ajaxOptionsArray = new NSMutableArray<AsyncRROption>();
		ajaxOptionsArray.addObject(new AsyncRROption("onComplete", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("onSuccess", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("onFailure", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("onLoading", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("evalScripts", AsyncRROption.BOOLEAN));
		ajaxOptionsArray.addObject(new AsyncRROption("insertion", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("asynchronous", AsyncRROption.BOOLEAN));
		String name = nameInContext(component.context(), component);
		NSMutableDictionary<String, String> options = AsyncRROption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
		fillInAjaxOptions(this, component, name, options);
		return options;
	}

	public static void fillInAjaxOptions(IAsyncRRElement element, WOComponent component, String submitButtonName,
			NSMutableDictionary<String, String> options) {
		String systemDefaultFormSerializer = "Form.serializeWithoutSubmits";
		String defaultFormSerializer = ERXProperties.stringForKeyWithDefault("er.ajax.formSerializer", systemDefaultFormSerializer);
		String formSerializer = (String) element.valueForBinding("formSerializer", defaultFormSerializer, component);
		if (!defaultFormSerializer.equals(systemDefaultFormSerializer)) {
			// _fs = formSerializer (but short)
			options.setObjectForKey(formSerializer, "_fs");
		}
		// _asbn = AJAX_SUBMIT_BUTTON_NAME (but short)
		options.setObjectForKey("'" + submitButtonName + "'", "_asbn");
		// PROTOTYPE OPTIONS
		// default to true in javascript
		if ("true".equals(options.objectForKey("asynchronous"))) {
			options.removeObjectForKey("asynchronous");
		}
		// PROTOTYPE OPTIONS
		// default to true in javascript
		if ("true".equals(options.objectForKey("evalScripts"))) {
			options.removeObjectForKey("evalScripts");
		}
		AjaxPTUpdateContainer.expandInsertionFromOptions(options, element, component);
	}

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
		String onClickBefore = (String) valueForBinding("onClickBefore", component);
		if (onClickBefore != null) {
			onClickBuffer.append("if (");
			onClickBuffer.append(onClickBefore);
			onClickBuffer.append(") {");
		}
		String updateContainerID = AjaxPTUpdateContainer.updateContainerID(this, component);
//		// PROTOTYPE EFFECTS
//		String beforeEffect = (String) valueForBinding("beforeEffect", component);
//		if (beforeEffect != null) {
//			onClickBuffer.append("new ");
//			onClickBuffer.append(AjaxPTUpdateLink.fullEffectName(beforeEffect));
//			onClickBuffer.append("('");
//			String beforeEffectID = (String) valueForBinding("beforeEffectID", component);
//			if (beforeEffectID == null) {
//				beforeEffectID = AjaxPTUpdateContainer.currentUpdateContainerID();
//				if (beforeEffectID == null) {
//					beforeEffectID = updateContainerID;
//				}
//			}
//			onClickBuffer.append(beforeEffectID);
//			onClickBuffer.append("', { ");
//			String beforeEffectDuration = (String) valueForBinding("beforeEffectDuration", component);
//			if (beforeEffectDuration != null) {
//				onClickBuffer.append("duration: ");
//				onClickBuffer.append(beforeEffectDuration);
//				onClickBuffer.append(", ");
//			}
//			onClickBuffer.append("queue:'end', afterFinish: function() {");
//		}
		String replaceID = (String) valueForBinding("replaceID", component);
		String id = (updateContainerID == null) ? replaceID : updateContainerID;
		if (id != null) {
			onClickBuffer.append("ASB.update('" + id + "',");
		} else {
			onClickBuffer.append("ASB.request(");
		}
		onClickBuffer.append(formReference);
		if (valueForBinding("functionName", component) != null) {
			onClickBuffer.append(",additionalParams");
		} else {
			onClickBuffer.append(",null");
		}
		onClickBuffer.append(',');
		NSMutableDictionary<String, String> options = createAjaxOptions(component);
		if (replaceID != null) {
			options.setObjectForKey("true", ERXAjaxApplication.KEY_REPLACED);
		}
//		AjaxPTUpdateLink.addEffect(options, (String) valueForBinding("effect", component), id,
//				(String) valueForBinding("effectDuration", component));
//		String afterEffectID = (String) valueForBinding("afterEffectID", component);
//		if (afterEffectID == null) {
//			afterEffectID = AjaxPTUpdateContainer.currentUpdateContainerID();
//			if (afterEffectID == null) {
//				afterEffectID = id;
//			}
//		}
//		AjaxPTUpdateLink.addEffect(options, (String) valueForBinding("afterEffect", component), afterEffectID,
//				(String) valueForBinding("afterEffectDuration", component));
		AjaxPTOptions.appendToBuffer(options, onClickBuffer, context);
		onClickBuffer.append(')');
		String onClick = (String) valueForBinding("onClick", component);
		if (onClick != null) {
			onClickBuffer.append(';');
			onClickBuffer.append(onClick);
		}
//		if (beforeEffect != null) {
//			onClickBuffer.append("}});");
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
			boolean useButtonTag = ERXProperties.booleanForKeyWithDefault(
					"er.extensions.foundation.ERXPatcher.DynamicElementsPatches.SubmitButton.useButtonTag", false);
			if (showButton) {
				elementName = useButtonTag ? "button" : "input";
				response.appendContentString("<" + elementName + " ");
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
				if (isATag) {
					response.appendContentString("<a href = \"javascript:void(0)\" ");
				} else {
					response.appendContentString("<" + elementName + " ");
				}
			}
			appendTagAttributeToResponse(response, "class", valueForBinding("class", component));
			appendTagAttributeToResponse(response, "style", valueForBinding("style", component));
			appendTagAttributeToResponse(response, "id", valueForBinding("id", component));
			appendTagAttributeToResponse(response, "title", valueForBinding("title", component));
			if (functionName == null) {
				appendTagAttributeToResponse(response, "onclick", onClickBuffer.toString());
			} else {
				appendTagAttributeToResponse(response, "onclick", functionName + "()");
			}
			if (showButton && !useButtonTag) {
				response.appendContentString(" />");
			} else {
				response.appendContentString(">");
				if (hasChildrenElements()) {
					appendChildrenToResponse(response, context);
				} else {
					response.appendContentString((String) valueForBinding("value", component));
				}
				response.appendContentString("</" + elementName + ">");
			}
		}
		super.appendToResponse(response, context);
	}

//	@Override
//	protected void addRequiredWebResources(WOResponse res, WOContext context) {
//		addScriptResourceInHead(context, res, "prototype.js");
//		//addScriptResourceInHead(context, res, "effects.js");
//		addScriptResourceInHead(context, res, "prototype_wo.js");
//	}

	@Override
	public WOActionResults invokeAction(WORequest worequest, WOContext wocontext) {
		WOActionResults result = null;
		WOComponent wocomponent = wocontext.component();
		String nameInContext = nameInContext(wocontext, wocomponent);
		boolean shouldHandleRequest = (!disabledInComponent(wocomponent) && wocontext.wasFormSubmitted())
				&& ((wocontext.isMultipleSubmitForm() && nameInContext.equals(worequest.formValueForKey(KEY_AJAX_SUBMIT_BUTTON_NAME))) || !wocontext
						.isMultipleSubmitForm());
		if (shouldHandleRequest) {
			String updateContainerID = AjaxPTUpdateContainer.updateContainerID(this, wocomponent);
			AjaxPTUpdateContainer.setUpdateContainerID(worequest, updateContainerID);
			wocontext.setActionInvoked(true);
			result = handleRequest(worequest, wocontext);
			ERXAjaxApplication.enableShouldNotStorePage();
		}
		return result;
	}
}
