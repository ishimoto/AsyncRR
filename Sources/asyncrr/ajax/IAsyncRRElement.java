package asyncrr.ajax;

import com.webobjects.appserver.*;

public interface IAsyncRRElement {

	public Object valueForBinding(String name, WOComponent component);

	public Object valueForBinding(String name, Object defaultValue, WOComponent component);

	public WOActionResults handleRequest(WORequest request, WOContext context);
}
