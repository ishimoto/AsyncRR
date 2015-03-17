package asyncrr.ajax;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.appserver.ajax.ERXAjaxApplication;
import er.extensions.components.ERXDynamicElement;

public abstract class AsyncRRElement extends ERXDynamicElement implements IAsyncRRElement {

	protected abstract String[] requiredScriptResources();

	// -----

	public static final String[] SCRIPT_RESOURCES_JQUERY = new String[] { "jquery.js", "jquery_wo.js" };
	public static final String[] SCRIPT_RESOURCES_MOOTOOLS = new String[] { "mootools.js", "mootools_wo.js" };
	public static final String[] SCRIPT_RESOURCES_PROTOTYPE = new String[] { "prototype.js", "prototype_wo.js" };

	// -----

	public AsyncRRElement(String name, NSDictionary<String, WOAssociation> associations, WOElement template) {
		super(name, associations, template);
	}

	public AsyncRRElement(String name, NSDictionary<String, WOAssociation> associations, NSMutableArray<WOElement> children) {
		super(name, associations, children);
	}

	/*
	 * Execute the request, if it's coming from our action, then invoke the ajax handler and put the key <code>AJAX_REQUEST_KEY</code> in the
	 * request userInfo dictionary (<code>request.userInfo()</code>).
	 */
	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		WOActionResults result = null;
		if (shouldHandleRequest(request, context)) {
			result = handleRequest(request, context);
			ERXAjaxApplication.enableShouldNotStorePage();
			if (ERXAjaxApplication.shouldIgnoreResults(request, context, result)) {
				log.warn("An Ajax request attempted to return the page, which is almost certainly an error.");
				result = null;
			}
			if (result == null && !ERXAjaxApplication.isAjaxReplacement(request)) {
				result = AjaxPTUtils.createResponse(request, context);
			}
		} else if (hasChildrenElements()) {
			result = super.invokeAction(request, context);
		}
		return result;
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		final String[] resources = requiredScriptResources();
		for (int i = 0; i < resources.length; i++)
			ERXResponseRewriter.addScriptResourceInHead(response, context, null, resources[i]);
	}

	/*
	 * Override this method and return an update container ID this element should react on.
	 */
	protected String _containerID(WOContext context) {
		return null;
	}

	/*
	 * Checks if the current request should be handled by this element.
	 */
	protected boolean shouldHandleRequest(WORequest request, WOContext context) {
		return AjaxPTUtils.shouldHandleRequest(request, context, _containerID(context));
	}
}
