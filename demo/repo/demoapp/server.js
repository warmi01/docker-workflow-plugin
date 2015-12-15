var express = require('express');
var bodyParser = require('body-parser');
var cookieParser = require('cookie-parser');
var path = require('path');
var fs = require('fs');
var http = require('http');

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

app.get('/test/:stage', function(req, resp) {
	var stage = req.params.stage;
	var envVarName = "DEMOAPP_TEST_RESULT_" + stage.toUpperCase();
	var status = process.env[envVarName];
	if (!status) {
		resp.status(500).send("Missing env. var " + envVarName + ". Unable to return test response.");
		return;
	}
	var respCode = status.substring(0, 3);
	var respText = status.substring(4);
	try {
		var respObj = JSON.parse(respText);
		resp.status(respCode).json(respObj);
	} catch (e) {
		resp.status(respCode).send(respText);
	}
});

// Serves all other static content under public
app.use('/', express.static(path.join(__dirname, 'public')));

var server_port = process.env.OPENSHIFT_NODEJS_PORT || 8000;
var server_ip = process.env.OPENSHIFT_NODEJS_IP || '127.0.0.1';

var server = app.listen(server_port, server_ip, function() {
    console.log("Listening on server_port " + server_port + " and server ip:" +server_ip);
});
