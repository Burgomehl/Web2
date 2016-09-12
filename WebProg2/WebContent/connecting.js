var activeElements = [];
var webSocket = new WebSocket('ws://localhost:8080/WebProg2/websocket');
webSocket.onerror = function(event) {
	onError(event)
};

webSocket.onopen = function(event) {
	onOpen(event)
};

webSocket.onmessage = function(event) {
	onMessage(event)
};

function onOpen(event) {
	sendMessageToMessageBox('Connection established');
}

function onError(event) {
	alert(event.data);
}

function onMessage(event) {
	var obj = JSON.parse(event.data);
	if (obj.type == "HISTORY") {
		createHistoryObject(obj.content);
		drawObject(obj.content);
	} else {
		sendMessageToMessageBox(obj.content);
	}
}

function sendMessageToMessageBox(string) {
	document.getElementById('messages').innerHTML += "<div>" + string
			+ "</div>";
}

function start() {
	var text = document.getElementById("userinput").value;
	var name = document.getElementById("name").textContent;
	content = name + ":" + text;
	sendJSONBack("TEXT", content);
	return false;
}

function changeAtt(e) {
	if (e.classList.contains("active")) {
		e.setAttribute("class", "history inActive");
		var index = activeElements.indexOf(e);
		activeElements.splice(index, 1);
	} else {
		e.setAttribute("class", "active");
		activeElements.push(e.getAttribute("id"));
	}
}

function createHistoryObject(content) {
	var div = document.createElement("div");
	div.setAttribute("onclick", "changeAtt(this)");
	div.setAttribute("class", "history inActive");
	div.setAttribute("id", JSON.stringify(content.id));
	var text = JSON.stringify(content.content);
	div.appendChild(document.createTextNode(text + ":" + content.type));
	document.getElementById("log").appendChild(div);
}

function deleteObjectByIds() {
	var text = {
		ids : activeElements
	}
	activeElements = [];
	sendJSONBack("DELETEBYID", text);
	var canvas = document.getElementById('testcanvas1');
	var context = canvas.getContext('2d');
	context.clearRect(0, 0, canvas.width, canvas.height);
	var myNode = document.getElementById("log");
	while (myNode.firstChild) {
		myNode.removeChild(myNode.firstChild);
	}
}

function sendJSONBack(type, content) {
	var cont = {
		type : type,
		content : content
	};
	webSocket.send(JSON.stringify(cont));
}

function saveUsername() {
	var username = document.getElementById("username").value;
	var userNode = document.createTextNode(username);
	document.getElementById("name").appendChild(userNode);
	document.getElementById("start").style.visibility = "hidden";
}