package asyncrr.ajax;

public class AjaxJQResponseRewriter {

	/*
	 * Adds a script tag with a correct resource URL into the HTML head tag if
	 * it isn't already present in the response, or inserts an Ajax OnDemand tag
	 * if the current request is an Ajax request.
	 * 
	 * @param response
	 *                the response
	 * @param context
	 *                the context
	 * @param framework
	 *                the framework that contains the file
	 * @param fileName
	 *                the name of the javascript file to add
	 */
//	public static void addScriptResourceInHead(WOResponse response, WOContext context, String framework, String fileName) {
//		boolean appendTypeAttribute = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXResponseRewriter.javascriptTypeAttribute",
//				false);
//		String scriptStartTag;
//		if (appendTypeAttribute) {
//			scriptStartTag = "<script type=\"text/javascript\" src=\"";
//		} else {
//			scriptStartTag = "<script src=\"";
//		}
//		String scriptEndTag = "\"></script>";
//		String fallbackStartTag;
//		String fallbackEndTag;
//		if (ERXAjaxApplication.isAjaxRequest(context.request()) && ERXProperties.booleanForKeyWithDefault("er.extensions.loadOnDemand", true)) {
//			if (!ERXAjaxApplication.isAjaxReplacement(context.request())
//					|| ERXProperties.booleanForKeyWithDefault("er.extensions.loadOnDemandDuringReplace", false)) {
//				fallbackStartTag = "<script type=\"text/wonder\" src=\"";
//				fallbackEndTag = "\"></script>";
//			} else {
//				fallbackStartTag = null;
//				fallbackEndTag = null;
//			}
//		} else {
//			fallbackStartTag = null;
//			fallbackEndTag = null;
//		}
//		ERXResponseRewriter.addResourceInHead(response, context, framework, fileName, scriptStartTag, scriptEndTag, fallbackStartTag,
//				fallbackEndTag, TagMissingBehavior.Inline);
//	}
}
