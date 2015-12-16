var express = require('express');
var bodyParser = require('body-parser');
var cookieParser = require('cookie-parser');
var path = require('path');
var fs = require('fs');
var http = require('http');
var request = require('request');

var app = express();
app.use(bodyParser());
app.use(cookieParser());

app.get('/', function(req, resp) {
	try {
		fsPath = path.join(__dirname, 'public', 'index.html');
		fileContent = fs.readFileSync(fsPath, 'utf8');
		fileContent = fileContent.replace(/{{version}}/g, process.env.DEMOAPP_VERSION);
		fileContent = fileContent.replace(/{{color}}/g, process.env.DEMOAPP_COLOR);
		resp.send(fileContent);
	} catch (e) {
		resp.status(500).send(e);
	}
});

app.get('/status', function(req, resp) {
	var status = process.env.DEMOAPP_STATUS_INIT;
	var respCode = status.substring(0, 3);
	var respText = status.substring(4);
	try {
		var respObj = JSON.parse(respText);
		resp.status(respCode).json(respObj);
	} catch (e) {
		resp.status(respCode).send(respText);
	}
});

app.get('/test', function(req, outerResp) {
	var stage = process.env.DEMOTEST_STAGE;
	
	var host = req.query.host;
	if (!host && process.env.SERVICE_REGISTRY_HOSTNAME) {
		// For the service registry route, add an extra app name to the path, 
		// which will likely be there if this app has been deployed using appformer.
		host = process.env.SERVICE_REGISTRY_HOSTNAME + ':8080/demoapp';
	}
	if (!host) {
		host = process.env.DEMOTEST_APP_LINK_ALIAS;
	}
	
	request.get({
		url: 'http://' + host + '/test/' + stage,
        agentOptions: {
            rejectUnauthorized: false
        },
		json: true,
	}, function(error, resp, body) {
		if (error || resp.body.error) {
			var errMsg = error;
			if (resp && resp.body) {
				errMsg =  JSON.stringify(resp.body);
			}
			console.error("Test setup failed: " + errMsg);
			outerResp.status(500).send({ state: 500, desc: errMsg });
		} else {
			console.info("Ran tests. Response: " + JSON.stringify(resp.body));
			outerResp.json(resp.body);
		}
	}).on('error', function(err) {
		console.log("Test request failed: " + err);
		outerResp.status(500).send({ state: 500, desc: err });
	});
});

// Serves all other static content under public
app.use('/', express.static(path.join(__dirname, 'public')));

var server_port = process.env.OPENSHIFT_NODEJS_PORT || 8000;
var server_ip = process.env.OPENSHIFT_NODEJS_IP || '127.0.0.1';

var server = app.listen(server_port, server_ip, function() {
    console.log("Listening on server_port " + server_port + " and server ip:" +server_ip);
});
