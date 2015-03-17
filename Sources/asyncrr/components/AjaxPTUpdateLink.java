package asyncrr.components;

import java.net.MalformedURLException;

import asyncrr.ajax.*;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

import er.extensions.appserver.ERXRequest;
import er.extensions.appserver.ajax.ERXAjaxApplication;
import er.extensions.components.ERXComponentUtilities;
import er.extensions.foundation.ERXMutableURL;

public class AjaxPTUpdateLink extends AsyncRRElement {

	public AjaxPTUpdateLink(String name, NSDictionary<String, WOAssociation> associations, WOElement children) {
		super(name, associations, children);
	}

	@Override
	protected String[] requiredScriptResources() {
		return AsyncRRElement.SCRIPT_RESOURCES_PROTOTYPE;
	}

	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		WOComponent component = context.component();
		boolean disabled = booleanValueForBinding("disabled", false, component);
		String updateContainerID = AjaxPTUpdateContainer.updateContainerID(this, component);
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

	private String onClick(WOContext context, boolean generateFunctionWrapper) {
		WOComponent component = context.component();
		NSMutableDictionary<String, String> options = createAjaxOptions(component);
		StringBuffer onClickBuffer = new StringBuffer();
		String onClick = (String) valueForBinding("onClick", component);
		String onClickBefore = (String) valueForBinding("onClickBefore", component);
		String updateContainerID = AjaxPTUpdateContainer.updateContainerID(this, component);
		String functionName = (String) valueForBinding("functionName", component);
		String function = (String) valueForBinding("function", component);
		String replaceID = (String) valueForBinding("replaceID", component);
//		// PROTOTYPE EFFECTS
//		AjaxPTUpdateLink.addEffect(options, (String) valueForBinding("effect", component), updateContainerID,
//				(String) valueForBinding("effectDuration", component));
		String afterEffectID = (String) valueForBinding("afterEffectID", component);
		if (afterEffectID == null) {
			afterEffectID = AjaxPTUpdateContainer.currentUpdateContainerID();
			if (afterEffectID == null) {
				afterEffectID = updateContainerID;
			}
		}
//		// PROTOTYPE EFFECTS
//		AjaxPTUpdateLink.addEffect(options, (String) valueForBinding("afterEffect", component), afterEffectID,
//				(String) valueForBinding("afterEffectDuration", component));
		// PROTOTYPE EFFECTS
		String beforeEffect = (String) valueForBinding("beforeEffect", component);
		WOAssociation directActionNameAssociation = (WOAssociation) associations().valueForKey("directActionName");
		if (beforeEffect == null && updateContainerID != null && directActionNameAssociation == null && replaceID == null && function == null
				&& onClick == null && onClickBefore == null) {
			NSDictionary<String, String> nonDefaultOptions = AjaxPTUpdateContainer.removeDefaultOptions(options);
			onClickBuffer.append("AUL.");
			if (generateFunctionWrapper) {
				onClickBuffer.append("updateFunc");
			} else {
				onClickBuffer.append("update");
			}
			onClickBuffer.append("('");
			onClickBuffer.append(updateContainerID);
			onClickBuffer.append("', ");
			AjaxPTOptions.appendToBuffer(nonDefaultOptions, onClickBuffer, context);
			onClickBuffer.append(", '");
			onClickBuffer.append(context.contextID());
			onClickBuffer.append('.');
			onClickBuffer.append(context.elementID());
			onClickBuffer.append('\'');
			// if (generateFunctionWrapper) {
			// onClickBuffer.append(", additionalParams");
			// }
			onClickBuffer.append(')');
			onClickBuffer.append(';');
		} else {
			if (generateFunctionWrapper) {
				onClickBuffer.append("function(additionalParams) {");
			}
			if (onClickBefore != null) {
				onClickBuffer.append("if (");
				onClickBuffer.append(onClickBefore);
				onClickBuffer.append(") {");
			}
//			// PROTOTYPE EFFECTS
//			if (beforeEffect != null) {
//				onClickBuffer.append("new ");
//				onClickBuffer.append(AjaxPTUpdateLink.fullEffectName(beforeEffect));
//				onClickBuffer.append("('");
//				String beforeEffectID = (String) valueForBinding("beforeEffectID", component);
//				if (beforeEffectID == null) {
//					beforeEffectID = AjaxPTUpdateContainer.currentUpdateContainerID();
//					if (beforeEffectID == null) {
//						beforeEffectID = updateContainerID;
//					}
//				}
//				onClickBuffer.append(beforeEffectID);
//				onClickBuffer.append("', { ");
//				String beforeEffectDuration = (String) valueForBinding("beforeEffectDuration", component);
//				if (beforeEffectDuration != null) {
//					onClickBuffer.append("duration: ");
//					onClickBuffer.append(beforeEffectDuration);
//					onClickBuffer.append(", ");
//				}
//				onClickBuffer.append("queue:'end', afterFinish: function() {");
//			}
			String actionUrl = null;
			if (directActionNameAssociation != null) {
				actionUrl = context._directActionURL((String) directActionNameAssociation.valueInComponent(component),
						ERXComponentUtilities.queryParametersInComponent(associations(), component),
						ERXRequest.isRequestSecure(context.request()), 0, false).replaceAll("&amp;", "&");
			} else {
				actionUrl = AjaxPTUtils.ajaxComponentActionUrl(context);
			}
			if (replaceID != null) {
				try {
					ERXMutableURL tempActionUrl = new ERXMutableURL(actionUrl);
					tempActionUrl.addQueryParameter(ERXAjaxApplication.KEY_REPLACED, "true");
					actionUrl = tempActionUrl.toExternalForm();
				} catch (MalformedURLException e) {
					throw NSForwardException._runtimeExceptionForThrowable(e);
				}
			}
			actionUrl = "'" + actionUrl + "'";
			if (functionName != null) {
				actionUrl = actionUrl + ".addQueryParameters(additionalParams)";
			}
			if (function != null) {
				onClickBuffer.append("return " + function + "(" + actionUrl + ")");
			} else {
				// PROTOTYPE FUNCTIONS
				if (replaceID == null) {
					if (updateContainerID == null) {
						onClickBuffer.append("new Ajax.Request(" + actionUrl + ", ");
						AjaxPTOptions.appendToBuffer(options, onClickBuffer, context);
						onClickBuffer.append(')');
					} else {
						onClickBuffer.append("new Ajax.Updater('" + updateContainerID + "', " + actionUrl + ", ");
						AjaxPTOptions.appendToBuffer(options, onClickBuffer, context);
						onClickBuffer.append(')');
					}
				} else {
					onClickBuffer.append("new Ajax.Updater('" + replaceID + "', " + actionUrl + ", ");
					AjaxPTOptions.appendToBuffer(options, onClickBuffer, context);
					onClickBuffer.append(')');
				}
			}
			if (onClick != null) {
				onClickBuffer.append(';');
				onClickBuffer.append(onClick);
			}
			if (beforeEffect != null) {
				onClickBuffer.append("}});");
			}
			if (onClickBefore != null) {
				onClickBuffer.append('}');
			}
			if (generateFunctionWrapper) {
				onClickBuffer.append('}');
			}
		}
		return onClickBuffer.toString();
	}

//	// PROTOTYPE EFFECTS
//	public static void addEffect(NSMutableDictionary options, String effect, String updateContainerID, String duration) {
//		if (effect != null) {
//			if (options.objectForKey("onSuccess") != null) {
//				throw new WODynamicElementCreationException("You cannot specify both an effect and a custom onSuccess function.");
//			}
//			if (updateContainerID == null) {
//				throw new WODynamicElementCreationException("You cannot specify an effect without an updateContainerID.");
//			}
//			StringBuilder effectBuffer = new StringBuilder();
//			effectBuffer.append("function() { new " + AjaxPTUpdateLink.fullEffectName(effect) + "('" + updateContainerID
//					+ "', { queue:'end'");
//			if (duration != null) {
//				effectBuffer.append(", duration: ");
//				effectBuffer.append(duration);
//			}
//			effectBuffer.append("}) }");
//			options.setObjectForKey(effectBuffer.toString(), "onSuccess");
//		}
//	}

//	// PROTOTYPE EFFECTS
//	public static String fullEffectName(String effectName) {
//		String fullEffectName;
//		if (effectName == null) {
//			fullEffectName = null;
//		} else if (effectName.indexOf('.') == -1) {
//			fullEffectName = "Effect." + ERXStringUtilities.capitalize(effectName);
//		} else {
//			fullEffectName = effectName;
//		}
//		return fullEffectName;
//	}

	// PROTOTYPE OPTIONS
	private NSMutableDictionary<String, String> createAjaxOptions(WOComponent component) {
		NSMutableArray<AsyncRROption> ajaxOptionsArray = new NSMutableArray<AsyncRROption>();
		ajaxOptionsArray.addObject(new AsyncRROption("onLoading", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("onComplete", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("onSuccess", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("onFailure", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("onException", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("evalScripts", AsyncRROption.BOOLEAN));
		ajaxOptionsArray.addObject(new AsyncRROption("insertion", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("asynchronous", AsyncRROption.BOOLEAN));
		NSMutableDictionary<String, String> options = AsyncRROption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
		options.setObjectForKey("'get'", "method");
		if (options.objectForKey("asynchronous") == null) {
			options.setObjectForKey("true", "asynchronous");
		}
		if (options.objectForKey("evalScripts") == null) {
			options.setObjectForKey("true", "evalScripts");
		}
		AjaxPTUpdateContainer.expandInsertionFromOptions(options, this, component);
		return options;
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		WOComponent component = context.component();
		boolean disabled = booleanValueForBinding("disabled", false, component);
		Object stringValue = valueForBinding("string", component);
		String functionName = (String) valueForBinding("functionName", component);
		if (functionName == null) {
			String elementName;
			boolean button = booleanValueForBinding("button", false, component);
			if (button) {
				elementName = "input";
			} else {
				elementName = (String) valueForBinding("elementName", "a", component);
			}
			boolean isATag = "a".equalsIgnoreCase(elementName);
			boolean renderTags = (!disabled || !isATag);
			if (renderTags) {
				response.appendContentString("<");
				response.appendContentString(elementName);
				response.appendContentString(" ");
				if (button) {
					appendTagAttributeToResponse(response, "type", "button");
				}
				if (isATag) {
					appendTagAttributeToResponse(response, "href", "javascript:void(0);");
				}
				if (!disabled) {
					appendTagAttributeToResponse(response, "onclick", onClick(context, false));
				}
				appendTagAttributeToResponse(response, "title", valueForBinding("title", component));
				appendTagAttributeToResponse(response, "value", valueForBinding("value", component));
				appendTagAttributeToResponse(response, "class", valueForBinding("class", component));
				appendTagAttributeToResponse(response, "style", valueForBinding("style", component));
				appendTagAttributeToResponse(response, "id", valueForBinding("id", component));
				appendTagAttributeToResponse(response, "accesskey", valueForBinding("accesskey", component));
				if (button) {
					if (stringValue != null) {
						appendTagAttributeToResponse(response, "value", stringValue);
					}
					if (disabled) {
						response.appendContentString(" disabled");
					}
				}
				// appendTagAttributeToResponse(response, "onclick",
				// onClick(context));
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
			AjaxPTUtils.appendScriptHeader(response);
			response.appendContentString(functionName);
			response.appendContentString(" = ");
			response.appendContentString(onClick(context, true));
			AjaxPTUtils.appendScriptFooter(response);
		}
		super.appendToResponse(response, context);
	}

//	@Override
//	protected void addRequiredWebResources(WOResponse res, WOContext context) {
//		addScriptResourceInHead(context, res, "prototype.js");
//		//addScriptResourceInHead(context, res, "effects.js");
//		//addScriptResourceInHead(context, res, "wonder.js");
//		addScriptResourceInHead(context, res, "prototype_wo.js");
//	}
}
