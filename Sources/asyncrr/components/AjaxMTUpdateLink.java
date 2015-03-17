package asyncrr.components;

import asyncrr.ajax.*;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

import er.extensions.appserver.ajax.ERXAjaxApplication;
import er.extensions.components.ERXComponentUtilities;

public class AjaxMTUpdateLink extends AsyncRRElement { //AjaxPTUpdateLink {

	public AjaxMTUpdateLink(String name, NSDictionary<String, WOAssociation> associations, WOElement children) {
		super(name, associations, children);
	}

	@Override
	protected String[] requiredScriptResources() {
		return AsyncRRElement.SCRIPT_RESOURCES_MOOTOOLS;
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

	private NSMutableDictionary<String, String> createAjaxOptions(WOComponent component) {
		NSMutableArray<AsyncRROption> ajaxOptionsArray = new NSMutableArray<AsyncRROption>();
		ajaxOptionsArray.addObject(new AsyncRROption("async", AsyncRROption.BOOLEAN));
		ajaxOptionsArray.addObject(new AsyncRROption("onRequest", AsyncRROption.FUNCTION));
		ajaxOptionsArray.addObject(new AsyncRROption("onComplete", AsyncRROption.FUNCTION));
		ajaxOptionsArray.addObject(new AsyncRROption("onSuccess", AsyncRROption.FUNCTION_2));
		ajaxOptionsArray.addObject(new AsyncRROption("onFailure", AsyncRROption.FUNCTION));
		ajaxOptionsArray.addObject(new AsyncRROption("onException", AsyncRROption.SCRIPT));
		ajaxOptionsArray.addObject(new AsyncRROption("evalScripts", AsyncRROption.BOOLEAN));
		ajaxOptionsArray.addObject(new AsyncRROption("useSpinner", AsyncRROption.BOOLEAN));
		ajaxOptionsArray.addObject(new AsyncRROption("spinnerTarget", AsyncRROption.STRING));
		ajaxOptionsArray.addObject(new AsyncRROption("spinnerOptions", AsyncRROption.DICTIONARY));
		NSMutableDictionary<String, String> options = AsyncRROption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
		options.setObjectForKey("'get'", "method");
		if (options.objectForKey("async") == null) {
			options.setObjectForKey("true", "async");
		}
		if (options.objectForKey("evalScripts") == null) {
			options.setObjectForKey("true", "evalScripts");
		}
		AjaxPTUpdateContainer.expandInsertionFromOptions(options, this, component);
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

	private String onClick(WOContext context, boolean generateFunctionWrapper) {
		WOComponent component = context.component();
		NSMutableDictionary<String, String> options = createAjaxOptions(component);
		StringBuffer onClickBuffer = new StringBuffer();
		String onClick = (String) valueForBinding("onClick", component);
		String onClickBefore = (String) valueForBinding("onClickBefore", component);
		String updateContainerID = (String) valueForBinding("updateContainerID", component);
		String functionName = (String) valueForBinding("functionName", component);
		String function = (String) valueForBinding("function", component);
		String replaceID = (String) valueForBinding("replaceID", component);
//		String effect = (String) valueForBinding("effect", component);
//		String afterEffect = (String) valueForBinding("afterEffect", component);
//		String beforeEffect = (String) valueForBinding("beforeEffect", component);
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
		String beforeEffect = null; // XXX schmied
		WOAssociation directActionNameAssociation = (WOAssociation) associations().valueForKey("directActionName");
		if (beforeEffect == null && updateContainerID != null && directActionNameAssociation == null && replaceID == null && function == null
				&& onClick == null && onClickBefore == null) {
			NSDictionary<String, String> nonDefaultOptions = AjaxMTUpdateContainer.removeDefaultOptions(options);
			onClickBuffer.append("MTAUL.").append(generateFunctionWrapper ? "updateFunc" : "update").append("('")
					.append(updateContainerID).append("', ");
			AjaxPTOptions.appendToBuffer(nonDefaultOptions, onClickBuffer, context);
			onClickBuffer.append(", '").append(context.contextID()).append('.').append(context.elementID()).append('\'').append(')')
					.append(';');
		} else {
			if (generateFunctionWrapper) {
				onClickBuffer.append("function(additionalParams) {");
			}
			if (onClickBefore != null) {
				onClickBuffer.append("if (").append(onClickBefore).append(") { ");
			}
//			// EFFECTS
//			if (beforeEffect != null) {
//				String beforeEffectID = (String) valueForBinding("beforeEffectID", component);
//				String beforeEffectDuration = (String) valueForBinding("beforeEffectDuration", component);
//				String beforeEffectProperty = (String) valueForBinding("beforeEffectProperty", component);
//				String beforeEffectStart = (String) valueForBinding("beforeEffectStart", component);
//				if (beforeEffectID == null) {
//					beforeEffectID = AjaxPTUpdateContainer.currentUpdateContainerID() != null ? AjaxPTUpdateContainer
//							.currentUpdateContainerID() : updateContainerID;
//				}
//				if (beforeEffect.equals("tween")) {
//					if (beforeEffectDuration != null) {
//						onClickBuffer.append("$('").append(beforeEffectID).append("').set('tween', { duration: '")
//								.append(beforeEffectDuration)
//								.append("', property: '" + beforeEffectProperty + "' });");
//					} else {
//						onClickBuffer.append("$('").append(beforeEffectID)
//								.append("').set('tween', { property: '" + beforeEffectProperty + "' });");
//					}
//					onClickBuffer.append("$('").append(beforeEffectID).append("').get('tween').start(").append(beforeEffectStart)
//							.append(").chain(function() {");
//				} else if (beforeEffect.equals("morph")) {
//					if (beforeEffectDuration != null) {
//						onClickBuffer.append("$('").append(beforeEffectID).append("').set('morph', { duration: '")
//								.append(beforeEffectDuration).append("' });");
//					}
//					onClickBuffer.append("$('").append(beforeEffectID)
//							.append("').get('morph').start('." + beforeEffectStart + "'").append(").chain(function() {");
//				} else if (beforeEffect.equals("slide")) {
//					String mode = (String) valueForBinding("effectSlideMode", component);
//					String transition = (String) valueForBinding("beforeEffectTransition", component);
//					onClickBuffer.append("$('").append(beforeEffectID).append("').set('slide'");
//					if (beforeEffectDuration != null || mode != null) {
//						onClickBuffer.append(", { ");
//						if (beforeEffectDuration != null) {
//							onClickBuffer.append("duration: '").append(beforeEffectDuration).append('\'')
//									.append(mode != null || transition != null ? "," : "");
//						}
//						if (mode != null) {
//							onClickBuffer.append("mode: '").append(mode).append('\'')
//									.append(transition != null ? "," : "");
//						}
//						if (transition != null) {
//							onClickBuffer.append("transition: ").append(transition);
//						}
//						onClickBuffer.append('}');
//					}
//					onClickBuffer.append("); $('").append(beforeEffectID).append("').get('slide').slide")
//							.append(ERXStringUtilities.capitalize(beforeEffectProperty)).append("().chain(function() {");
//				} else if (beforeEffect.equals("highlight")) {
//					if (beforeEffectDuration != null) {
//						onClickBuffer.append("$('").append(beforeEffectID).append("').set('tween', { duration: '")
//								.append(beforeEffectDuration).append("', property: 'background-color'});");
//					} else {
//						onClickBuffer.append("$('").append(beforeEffectID)
//								.append("').set('tween', { property: 'background-color' });");
//					}
//					onClickBuffer.append("$('").append(updateContainerID).append("').get('tween').start('")
//							.append(beforeEffectProperty != null ? beforeEffectProperty : "#ffff88', '#ffffff")
//							.append("').chain(function() { ");
//				}
//			}
			String actionUrl = null;
			if (directActionNameAssociation != null) {
				actionUrl = context.directActionURLForActionNamed((String) directActionNameAssociation.valueInComponent(component),
						ERXComponentUtilities.queryParametersInComponent(associations(), component)).replaceAll("&amp;", "&");
			} else {
				actionUrl = AjaxPTUtils.ajaxComponentActionUrl(context);
			}
			actionUrl = "'" + actionUrl + "'";
			if (functionName != null) {
				actionUrl = actionUrl + ".addQueryParameters(additionalParams);";
			}
			if (function != null) {
				onClickBuffer.append("return " + function + "(" + actionUrl + ")");
			} else {
				options.setObjectForKey(actionUrl, "url");
				if (replaceID == null) {
					if (updateContainerID == null) {
						onClickBuffer.append("new Request(");
						AjaxPTOptions.appendToBuffer(options, onClickBuffer, context);
						onClickBuffer.append(").send();");
					} else {
						options.takeValueForKey("'" + updateContainerID + "'", "update");
						onClickBuffer.append("new Request.HTML(");
						AjaxPTOptions.appendToBuffer(options, onClickBuffer, context);
						onClickBuffer.append(").send();");
					}
				} else {
					options.takeValueForKey("'" + replaceID + "'", "update");
					onClickBuffer.append("new Request.HTML(");
					AjaxPTOptions.appendToBuffer(options, onClickBuffer, context);
					onClickBuffer.append(").send();");
				}
			}
			if (onClick != null) {
				onClickBuffer.append(';').append(onClick);
			}
			if (beforeEffect != null) {
				onClickBuffer.append("});");
			}
			if (onClickBefore != null) {
				onClickBuffer.append(" } ");
			}
			if (generateFunctionWrapper) {
				onClickBuffer.append('}');
			}
		}
		return onClickBuffer.toString();
	}

//	public static void addEffect(NSMutableDictionary options, String effect, String updateContainerID, String effectProperty, String effectStart,
//			String duration, String mode) {
//		if (effect != null) {
//			if (options.objectForKey("onSuccess") != null) {
//				throw new WODynamicElementCreationException("You cannot specify both an effect and a custom onSuccess function.");
//			}
//			if (updateContainerID == null) {
//				throw new WODynamicElementCreationException("You cannot specify an effect without an updateContainerID.");
//			}
//			StringBuilder effectBuffer = new StringBuilder();
//			effectBuffer.append("function() { ");
//			if (effect.equals("tween")) {
//				if (duration != null) {
//					effectBuffer.append("$('").append(updateContainerID).append("').set('tween', { duration: '").append(duration)
//							.append("', property: '").append(effectProperty).append("' });");
//				} else {
//					effectBuffer.append("$('").append(updateContainerID).append("').set('tween', { property: '")
//							.append(effectProperty).append("' });");
//				}
//				effectBuffer.append("$('").append(updateContainerID).append("').get('tween').start(" + effectStart + ");");
//			} else if (effect.equals("morph")) {
//				if (duration != null) {
//					effectBuffer.append("$('").append(updateContainerID).append("').set('morph', { duration: '").append(duration)
//							.append("'});");
//				}
//				effectBuffer.append("$('").append(updateContainerID).append("').get('morph').start('." + effectStart + "');");
//			} else if (effect.equals("slide")) {
//				effectBuffer.append("$('").append(updateContainerID).append("').get('slide').slide")
//						.append(ERXStringUtilities.capitalize(effectProperty)).append("(); ");
//			} else if (effect.equals("highlight")) {
//				effectBuffer.append("$('").append(updateContainerID).append("').highlight(")
//						.append(effectProperty != null ? effectProperty : "").append(");");
//			}
//			effectBuffer.append('}');
//			options.setObjectForKey(effectBuffer.toString(), "onSuccess");
//		}
//	}
}
