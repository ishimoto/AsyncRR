package asyncrr.components;

import com.webobjects.appserver.*;
import com.webobjects.foundation.NSArray;

import er.extensions.components.ERXComponent;

public class Main extends ERXComponent {

	private static final long serialVersionUID = 1L;

	private static final String TYPE_JQUERY = "AsyncRR JQuery";
	private static final String TYPE_MOOTOOLS = "AsyncRR MooTools";
	private static final String TYPE_PROTOTYPE = "AsyncRR Prototype";
	private static final String TYPE_WONDER_MOOTOOLS = "Wonder MooTools";
	private static final String TYPE_WONDER_PROTOTYPE = "Wonder Prototype";

	private static final NSArray<String> FRUITS = new NSArray<String>("Apple", "Banana", "Cranbarry");

	public static final NSArray<String> types = new NSArray<String>(TYPE_JQUERY, TYPE_MOOTOOLS, TYPE_PROTOTYPE, TYPE_WONDER_MOOTOOLS,
			TYPE_WONDER_PROTOTYPE);

	public String type, fullString, addString, fruitInside, fruitOutside;

	public Main(WOContext context) {
		super(context);
		fullString = "";
		addString = "blah";
		type = TYPE_JQUERY;
	}

	public NSArray<String> types() {
		return types;
	}

	public NSArray<String> fruits() {
		return FRUITS;
	}

	public boolean isTypeJQuery() {
		return TYPE_JQUERY.equals(type);
	}

	public boolean isTypeMooTools() {
		return TYPE_MOOTOOLS.equals(type);
	}

	public boolean isTypePrototype() {
		return TYPE_PROTOTYPE.equals(type);
	}

	public boolean isTypeWonderMooTools() {
		return TYPE_WONDER_MOOTOOLS.equals(type);
	}

	public boolean isTypeWonderPrototype() {
		return TYPE_WONDER_PROTOTYPE.equals(type);
	}

	public WOActionResults actionSelectType() {
		return null;
	}

	public WOActionResults actionAdd() {
		fullString = fullString + addString;
		return null;
	}

	public WOActionResults actionClear() {
		System.out.println(">> clear");
		fullString = "";
		return null;
	}

	public WOActionResults actionFruitInside() {
		System.out.println(">> inside");
		addString = fruitInside;
		return null;
	}

	public WOActionResults actionFruitOutside() {
		System.out.println(">> outside");
		addString = fruitOutside;
		return null;
	}
}
