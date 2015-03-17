package asyncrr;

import com.webobjects.appserver.*;
import com.webobjects.foundation._NSUtilities;

import er.extensions.appserver.*;

public class Application extends ERXApplication {

	public static void main(String[] argv) {
		ERXApplication.main(argv, Application.class);
	}

	public Application() {
		ERXApplication.log.info("Welcome to " + name() + " !");

		// everything is utf-8
		WOMessage.setDefaultEncoding("UTF8");
		WOMessage.setDefaultURLEncoding("UTF8");
		ERXMessageEncoding.setDefaultEncoding("UTF8");
		ERXMessageEncoding.setDefaultEncodingForAllLanguages("UTF8");

		setAllowsConcurrentRequestHandling(true);
	}

	@Override
	public void appendToResponse(final WOResponse aResponse, final WOContext aContext) {
		super.appendToResponse(aResponse, aContext);
		if (_NSUtilities.UTF8StringEncoding.equals(aResponse.contentEncoding()))
			aResponse.setHeader("text/html; charset=UTF-8", "Content-Type");
	}

	@Override
	public WOResponse createResponseInContext(final WOContext aContext) {
		final WOResponse r = super.createResponseInContext(aContext);
		r.setContentEncoding(_NSUtilities.UTF8StringEncoding);
		return r;
	}
}
