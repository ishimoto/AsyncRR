
var Wonder = Wonder || {};

var AOD;

/* Simple JavaScript Inheritance
* By John Resig http://ejohn.org/
* MIT Licensed.
*/

// Inspired by base2 and Prototype

(function() {
	var initializing = false, fnTest = /xyz/.test(function() { xyz; }) ? /\b_super\b/ : /.*/;
	// The base Class implementation (does nothing)
	this.Class = function() {};
	// Create a new Class that inherits from this class
	Class.extend = function(prop) {
		var _super = this.prototype;
		// Instantiate a base class (but only create the instance,
		// don't run the init constructor)
		initializing = true;
		var prototype = new this();
		initializing = false;
		// Copy the properties over onto the new prototype
		for (var name in prop) {
			// Check if we're overwriting an existing function
			prototype[name] = typeof prop[name] == "function" && typeof _super[name] == "function" && fnTest.test(prop[name]) ?
				(function(name, fn){
					return function() {
						var tmp = this._super;
						// Add a new ._super() method that is the same method
						// but on the super-class
						this._super = _super[name];
						// The method only need to be bound temporarily, so we
						// remove it when we're done executing
						var ret = fn.apply(this, arguments);
						this._super = tmp;
						return ret;
					};
				}) (name, prop[name]) : prop[name];
		}
		// The dummy class constructor
		function Class() {
			// All construction is actually done in the init method
			if (!initializing && this.init)
			this.init.apply(this, arguments);
		}
		// Populate our constructed prototype object
		Class.prototype = prototype;
		// Enforce the constructor to be what we expect
		Class.prototype.constructor = Class;
		// And make this class extendable
		Class.extend = arguments.callee;
		return Class;
	};
}) ();

(function($) {

	String.prototype.addQueryParameters = function(additionalParameters) {
		if (additionalParameters) {
			return this + (this.match(/\?/) ? '&' : '?') + additionalParameters;
		}
		return this;
	};

	var AjaxOnDemand = {
		loadCSS: function(css) {
			var link = document.createElement("link");
			link.setAttribute("rel", "stylesheet");
			link.setAttribute("type", "text/css");
			link.setAttribute("href", css);
			if (typeof link != "undefined") {
				document.getElementsByTagName("head")[0].appendChild(link);
			}
		},
		loadedCSS: function(request) {
			var inlineStyle = new Element("style", {"type": "text/css"});
			inlineStyle.appendChild(document.createTextNode(request.responseText));
			document.getElementsByTagName('HEAD')[0].appendChild(inlineStyle);
		}
	};

	AOD = AjaxOnDemand;

	Wonder.settings = {
		verbose: 0
	};

	Wonder.BreakException = {};
	Wonder.PeriodicalRegistry = {};
	Wonder.nextGuid = 1;
	Wonder.components = [];

	Wonder.log = function(msg, lvl) {
		lvl = lvl || 1;
		if (lvl >= Wonder.settings.verbose) {
			try {
				console.log(msg);
			} catch(err) {
			}
		}
	};

	Wonder.error = function(msg) {
		try {
			console.error(msg);
		} catch(err) {
		}
	};

	Wonder.Page = {
		initialize: function(ctx) {
			$(ctx).find("[data-wonder-id]").each(function(index, element) {
				var wonderID = $(element).attr('data-wonder-id');
				try {
					var component = eval(Wonder[wonderID]);
					new component(element);
				} catch (err) {
					Wonder.error("Unable to initialize component with wonder id: " + wonderID);
				}
			});
		},
		addComponent: function(element, obj) {
			element.data()['component'] = obj;
		},
		getComponent: function(element) {
			var obj = null;
			if (typeof element === 'string') element = $("#" + element);
			try {
				var obj = element.data()['component'];
			} catch (err) {
				Wonder.log("Object does not exist: " + element.attr('data-wonder-id'));
			}
			return obj;
		}
	};

	Wonder.AjaxElement = Class.extend({
		options: null,
		init: function(element) {
			this.options = $.parseJSON(element.attr('data-wonder-options'));
			var options = this.options;
			// Lame hack to return strings to booleans
			$.each(this.options, function(key, value) {
				if (value === "true" || value === "false") {
					options[key] = value == "true";
				}
			});
		},
		// override me if you need to do something
		// with the component when removing from the DOM.
		destroy: function() {
		}
	});

	Wonder.AjaxComponent = Wonder.AjaxElement.extend({
		delegate: null,
		element: null,
		url: null,
		init: function(element) {
			this._super(element);
			if (this.options.delegate) {
				this.delegate = Wonder.delegates[this.options.delegate];
			}
		},
		mightUpdate: function(target, caller) {
			if (this.currentDelegate) {
				return this.currentDelegate.mightUpdate(target, caller);
			}
		},
		willUpdate: function(target, caller) {
			if (this.currentDelegate) {
				return this.currentDelegate.willUpdate(target, caller);
			}
		},
		processUpdate: function(target, caller, remoteData, callback) {
			var self = this;
			$.when(self.mightUpdate(target,caller)).then(function() {
				$.when(self.willUpdate(target, caller)).then(function() {
					$.when(self.loadResponse(target, remoteData)).then(function() {
						$.when(self.didUpdate(target, caller)).then(function() {
							$.when(self.handleFinish(target)).done(function() {
								if (callback) {
									if (typeof callback == 'function') {
										callback.call();
									} else {
										eval("var fn = (" + callback + ")");
										fn(target, caller);
									}
								}
							});
						});
					});
				});
			});
		},
		_updateTarget: function(target, remoteData) {
			target.find('[data-wonder-id]').each(function(index, element) {
				var obj = Wonder.Page.getComponent($(element));
				if (obj) {
					obj.destroy();
				}
			});
			return target.html(remoteData);
		},
		didUpdate: function(target, caller) {
			if (this.currentDelegate) {
				return this.currentDelegate.didUpdate(target, caller);
			}
		},
		loadResponse: function(target, remoteData) {
			this._updateTarget(target, remoteData);
			var scripts = target.children("script[type='text/wonder']");
			return this._loadScripts(scripts, 0);
		},
		_loadScripts: function(scripts, index) {
			var self = this;
			var nextInsert = scripts[index];
			if (nextInsert != undefined) {
				var script = $(nextInsert).attr('src');
				return $.ajax({
					url:script, async: false, dataType: 'script',
					success: function() {
						index++;
						self._loadScripts(scripts, index);
					}
				});
			}
		},
		updateFailed: function(target, caller, callback) {
			if (this.currentDelegate) {
				return this.currentDelegate.updateFailed(target, caller);
			}
		},
		handleFinish: function(target) {
			Wonder.Page.initialize(target);
		}
	});

	var AjaxOptions = {
		defaultOptions: function(additionalOptions) {
			var options = {type: 'GET', async: true, evalScripts: true};
			return $.extend(options, additionalOptions);
		}
	};

	Wonder.AjaxUpdateContainer = Wonder.AjaxComponent.extend({
		currentDelegate: null,
		init: function(element) {
			this.element = $(element);
			this._super(this.element);
			this.url = this.options['updateUrl'];
			if (this.options['minTimeout']) {
				this.registerPeriodic(
					this.options['canStop'] ? this.options['canStop'] : null,
					this.options['stopped'] ? this.options['stopped'] : null
				);
			} else {
				Wonder.Page.addComponent(this.element, this);
				if (this.options['observeFields']) {
					var observeFields = this.options['observeFields'].split(",");
					for (var i = 0; i < observeFields.length; i++) {
						var field = $("#" + $.trim(observeFields[i]));
						if (field) {
							field.bind("change", { updateContainer: this.element }, function(event) {
								var form = this.form;
								var actionUrl = form.action;
								actionUrl = actionUrl.replace(/\/wo\//, '/ajax/');
								var caller = $(this);
								var updateContainer = Wonder.Page.getComponent(event.data.updateContainer.attr("id"));
								var data = {};
								var formFieldName = caller.attr('name');
								if (caller.prop("tagName") == 'INPUT' && caller.prop('type') == 'checkbox') {
									data[formFieldName] = caller.prop('checked') ? "" : "false";
								} else {
									data[formFieldName] = caller.val();
								}
								data['_partialSenderID'] = formFieldName;
								updateContainer.update(actionUrl, caller, null, data);
							});
						}
					}
				}
				if (this.options['subscribes']) {
					var subscribedEvents = this.options['subscribes'].split(",");
					for (var i = 0; i < subscribedEvents.length; i++) {
						var eventName = $.trim(subscribedEvents[i]);
						$(document).on(eventName, { updateContainer: this.element }, function(event) {
							var updateContainer = Wonder.Page.getComponent(event.data.updateContainer.attr("id"));
							updateContainer.update(this.url, this.element, {'delegate' : updateContainer.delegate}, null);
						});
					}
				}
			}
		},
		registerPeriodic: function(canStop, stopped) {
			this.currentDelegate = this.delegate;
			if (this.currentDelegate) {
				this.options['beforeSend'] = $.proxy(function(xhr) {
					return this.mightUpdate(this.element, this.element);
				}, this);
				this.options['error'] = $.proxy(function(xhr, textStatus) {
					this.updateFailed(this.element, this.element);
				}, this);
			}
			if (!canStop) {
				if (!Wonder.PeriodicalRegistry[this.element.attr('id')]) {
					Wonder.PeriodicalRegistry[this.element.attr('id')] = $.PeriodicalUpdater(this.url, this.options, $.proxy(function(remoteData, success, xhr, handle) {
						if (success) {
							handle.pause();
							this.processUpdate(this.element, this.element, remoteData, handle.pause);
						}
					}, this));
				}
			}
		},
		update: function(url, caller, options, data) {
			var updateUrl = url || this.url;
			var options = options || this.options;
			this.currentDelegate = options['delegate'] || this.delegate;
			var aCaller = caller || this.element;
			var id = this.element.attr("id");
			if (options && options['_r']) {
				updateUrl = updateUrl.addQueryParameters('_r='+ id);
			} else {
				updateUrl = updateUrl.addQueryParameters('_u=' + id);
			}
			options['context'] = this;
			options['data'] = data;
			options['beforeSend'] = function(xhr, settings) {
				if (this.currentDelegate) {
					return this.currentDelegate.mightUpdate(this.element, aCaller);
				}
			};
			options['success'] = function(data, textStatus, xhr) {
				this.processUpdate(this.element, aCaller, data, options.callback);
			};
			options['error'] = function(xhr, textStatus, errorThrown) {
				if (this.currentDelegate) {
					this.currentDelegate.updateFailed(this.element, aCaller);
				}
			};
			$.ajax(updateUrl, options);
		}
		/*
		_update: function(id, options) {
		var updateElement = $("#" + id);
		if (updateElement == null) {
		alert('There is no element on this page with the id "' + id + '".');
		}
		var actionUrl = updateElement.attr('data-updateUrl');
		if (options && options['_r']) {
		actionUrl = actionUrl.addQueryParameters('_r='+ id);
		}
		else {
		actionUrl = actionUrl.addQueryParameters('_u=' + id);
		}
		actionUrl = actionUrl.addQueryParameters(new Date().getTime());
		$.get(actionUrl, $.extend(AjaxOptions.defaultOptions(options), {
		}));
		}
		*/
	});

	Wonder.AUC = Wonder.AjaxUpdateContainer;

	Wonder.AjaxUpdateLink = Wonder.AjaxElement.extend({
		init: function(element) {
			var element = $(element);
			this._super(element);
			element.bind("click", { options: this.options }, this.update);
		},
		update: function(event) {
			var caller = $(this);
			var options = event.data.options;
			var updateContainer = Wonder.Page.getComponent(options['updateContainer']);
			if (updateContainer == null) {
				alert('There is no element on this page with the id "' + options['updateContainer'] + '".');
			} else {
				if (!caller.attr('disabled')) {
					var elementID = options['elementID'];
					var actionUrl = updateContainer.url;
					if (elementID) {
						actionUrl = actionUrl.replace(/[^\/]+$/, elementID);
					}
					updateContainer.update(actionUrl, caller, options);
				}
			}
		}
	});

	Wonder.AUL = Wonder.AjaxUpdateLink;

	Wonder.Validate = Wonder.AjaxElement.extend({
		init: function(element) {
			var element = $(element);
			this._super(element);
			Wonder.Page.addComponent(element, element.validate(this.options));
		}
	});

	Wonder.MaskedInput = Wonder.AjaxElement.extend({
		init: function(element) {
			var element = $(element);
			this._super(element);
			element.mask(this.options['mask']);
		}
	});

	/*
	TODO: WIll revisit this later. More appropriate if you want the whole profile vs. just the image.
	Wonder.Gravatar = Wonder.AjaxElement.extend({
	init: function(element) {
	var element = $(element);
	this._super(element);
	$.gravatar($.extend(this.options,
	{
	success: function(profile) {
	element.attr('src', profile.thumbnailUrl);
	},
	error: function() {
	alert('hi');
	element.attr('src', "http://www.gravatar.com/avatar/205e460b479e2e5b48aec07710c08d50?f=y");
	}
	}
	));
	}
	});
	*/

	Wonder.AjaxSubmitButton = Wonder.AjaxElement.extend({
		PartialFormSenderIDKey: '_partialSenderID',
		AjaxSubmitButtonNameKey: 'AJAX_SUBMIT_BUTTON_NAME',
		init: function(element){
			var element = $(element);
			this._super(element);
			element.bind("click", { options: this.options }, this.update);
		},
		defaultOptions: function(additionalOptions) {
			var options = AjaxOptions.defaultOptions(additionalOptions);
			options['type'] = 'POST';
			options['cache'] = false;
			return options;
		},
		update: function(event) {
			var caller = $(this);
			var options = event.data.options;
			var targetId = options['updateContainer'];
			var target = Wonder.Page.getComponent($("#" + targetId));
			if (target == null) {
				alert("There is no element on this page with the id " + targetId);
			} else {
				var form = options['formName'] ? document.getElementsByName(options['formName'])[0] : caller[0].form;
				if (!form) {
					form = caller.closest("form").get(0);
				}
				var actionUrl = Wonder.ASB.prototype.generateActionUrl(targetId, form, options);
				var settings = Wonder.ASB.prototype.defaultOptions(options);
				var data = Wonder.ASB.prototype.processOptions(form, {
					'_asbn': options['_asbn']
				});
				target.update(actionUrl, caller, settings, data['parameters']);
			}
		},
		processOptions: function(form, options) {
			var processedOptions = null;
			if (options != null) {
				processedOptions = $.extend({}, options);
				var ajaxSubmitButtonName = processedOptions['_asbn'];
				if (ajaxSubmitButtonName != null) {
					processedOptions['_asbn'] = null;
					var parameters = processedOptions['parameters'];
					if (parameters === undefined || parameters == null) {
						var formSerializer = processedOptions['_fs'];
						var serializedForm = $(form).serialize();
						processedOptions['parameters'] = serializedForm + '&' + Wonder.ASB.prototype.AjaxSubmitButtonNameKey +
							'=' + ajaxSubmitButtonName;
					} else {
						processedOptions['parameters'] = parameters + '&' + Wonder.ASB.prototype.AjaxSubmitButtonNameKey +
							'=' + ajaxSubmitButtonName;
					}
				}
			}
			// processedOptions = Wonder.ASB.prototype.defaultOptions(processedOptions);
			return processedOptions;
		},
		generateActionUrl: function(id, form, options) {
			var actionUrl = form.action;
			actionUrl = actionUrl.replace(/\/wo\//, '/ajax/');
			if (id != null) {
				if (options && options['_r']) {
					actionUrl = actionUrl.addQueryParameters('_r=' + id);
				} else {
					actionUrl = actionUrl.addQueryParameters('_u=' + id);
				}
			}
			actionUrl = actionUrl.addQueryParameters(new Date().getTime());
			return actionUrl;
		}
	});

	Wonder.ASB = Wonder.AjaxSubmitButton;

	Wonder.AjaxTabPanel = {
		switchTab: function(target, caller) {
			caller.attr('disabled', '');
			caller.parent().attr('class', 'active');
			caller.parent().siblings().each(function(index, element) {
				$(element).children().first().removeAttr('disabled');
				$(element).removeAttr('class');
			});
		}
	};

	Wonder.SelectBox = Wonder.AjaxElement.extend({
		init: function(element) {
			var element = $(element);
			this._super(element);
			element.selectBox(this.options);
			Wonder.Page.addComponent(element, element.data('selectBox'));
		}
	});

	Wonder.Select2 = Wonder.AjaxElement.extend({
		init: function(element) {
			var element = $(element);
			this._super(element);
			element.select2(this.options);
			Wonder.Page.addComponent(element, element.data('select2'));
		},
		destroy: function() {
			this.element.select2("destroy");
		}
	});

	/*
	Wonder.AjaxObserveField = Wonder.AjaxElement.extend({
	init: function(element) {
	var element = $(element);
	this._super(element);
	var options = $.parseJSON(self.attr('data-wonder-options'));
	var observeFieldId = options['observeFieldId'];
	var elementToObserve = $("#" + observeFieldId);
	if (! elementToObserve) {
	alert("Unable to observe element with id: " + observeFieldId + ". The element does not exist.");
	} else {
	Wonder.Page.addEvent(elementToObserve, "change", this.update.partial(element));
	}
	},
	update: function() {
	}
	});
	Wonder.AOF = Wonder.AjaxObserveField;
	*/

	Wonder.delegates = {};
	Wonder.delegates.debug = function() {}
	Wonder.delegates.debug.prototype.mightUpdate = function(target, caller) {
		Wonder.log("Might update...", 1);
	};
	Wonder.delegates.debug.prototype.willUpdate = function(target, caller) {
		Wonder.log("Will update...", 1);
	};
	Wonder.delegates.debug.prototype.didUpdate = function(target, caller) {
		Wonder.log("Did update...", 1);
	};
	Wonder.delegates.debug.prototype.updateFailed = function(target, caller) {
		Wonder.log("There was an error...", 1);
	};
	Wonder.delegates.fade = new Wonder.delegates.debug();
	Wonder.delegates.fade.willUpdate = function(target, caller) {
		return $(target).fadeOut();
	};
	Wonder.delegates.fade.didUpdate = function(target, caller) {
		return $(target).fadeIn();
	};
	Wonder.delegates.slide = new Wonder.delegates.debug();
	Wonder.delegates.slide.willUpdate = function(target, caller) {
		return $(target).slideUp();
	};
	Wonder.delegates.slide.didUpdate = function(target, caller) {
		return $(target).slideDown();
	};
	Wonder.delegates.asb = new Wonder.delegates.debug();
	Wonder.delegates.asb.mightUpdate = function(target, caller) {
		return Wonder.Page.getComponent(caller.parents("form:first")).form();
	};
	$(window).load(function() {
		Wonder.Page.initialize(document);
	});

}) (jQuery);
