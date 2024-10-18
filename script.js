var console = {
	log: function(data) {
		print(data);
	}
}

var apicall = {
	globalProperties: {},
	environmentProperties: {},
	collectionProperties: {},
	setGlobalVariable: function(name, value) {
		this.globalProperties[name] = value;
	},
	getGlobalVariable: function(name) {
		return this.globalProperties[name];
	},
	setEnvironmentVariable: function(name, value) {
		this.environmentProperties[name] = value;
	},
	getEnvironmentVariable: function(name) {
		return this.environmentProperties[name];
	},
	setCollectionVariable: function(name, value) {
		this.collectionProperties[name] = value;
	},
	getCollectionVariable: function(name) {
		return this.collectionProperties[name];
	}
}

var pm = {
	globals: {
		set: function(name, value) {
			apicall.setGlobalVariable(name, value);
		},
		unset: function(name, value) {
			
		},
		get: function(name) {
			return apicall.getGlobalVariable(name);
		}
	},
	environment: {
		set: function(name, value) {
			apicall.setEnvironmentVariable(name, value);
		},
		unset: function(name, value) {
			
		},
		get: function(name) {
			return apicall.getEnvironmentVariable(name);
		}
	},
	collectionVariables: {
		set: function(name, value) {
			apicall.setCollectionVariable(name, value);
		},
		unset: function(name, value) {
			
		},
		get: function(name) {
			return apicall.getCollectionVariable(name);
		}
	}
}

function xdxd(params) {
	responseBody = params.responseBody;
	request = {};
	var responseCode = {};
	
	if (params.responseCode != undefined) {
		apicall.responseCode = JSON.parse(params.responseCode);
		responseCode = apicall.responseCode;
	}
	if (params.request != undefined) {
		apicall.request = JSON.parse(params.request);
		request = apicall.request;
		if (request.data != undefined) {
			request.data = request.data.replace(/[\u0000-\u001F\u007F-\u009F]/g, "")
		}
	}
	apicall.globalProperties = JSON.parse(params.globalVariables);
	apicall.environmentProperties = JSON.parse(params.environmentVariables);
	apicall.collectionProperties = JSON.parse(params.collectionVariables);
	apicall.iteration = {};
	tests = [];
	if (params.iteration != undefined && params.iteration != null) {
		params.iteration = JSON.parse(params.iteration);
		apicall.iteration = params.iteration;
		apicall.iterationData = JSON.parse(params.iteration.data);
	}
	postman = apicall;
	scriptCode
	return apicall;
}