package asyncrr.ajax;

import com.webobjects.appserver.*;

public abstract class AjaxPTResponseAppender {

	public abstract void appendToResponse(WOResponse response, WOContext context);
}
