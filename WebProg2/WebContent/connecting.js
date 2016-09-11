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
	console.log(event.data);
	var obj = JSON.parse(event.data);
	console.log(obj.type);
	if(obj.type == "HISTORY"){
		createHistoryObject(obj.content.content+":"+obj.content.type);
	}else{
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
	content = name+":"+text;
	sendJSONBack("TEXT", content);
	return false;
}

function changeAtt(e) {
	if (e.classList.contains("active")) {
		e.setAttribute("class", "history inActive");
		var index = activeElements.indexOf(e);
		activeElements.splice(index,1);
		console.log(activeElements);
	} else {
		e.setAttribute("class", "active");
		activeElements.push(e);
	}
}

function createHistoryObject(content){
	var div = document.createElement("div");
	div.setAttribute("onclick", "changeAtt(this)");
	div.setAttribute("class", "history inActive");
	var text = JSON.stringify(content);
	div.appendChild(document.createTextNode(text));
	document.getElementById("log").appendChild(div);
}

function sendJSONBack(type, content) {
	if (type == "HISTORY") {
		createHistoryObject(content.content+":"+content.type);
	}
	var cont = {
		type : type,
		content : content
	};
	webSocket.send(JSON.stringify(cont));
}

function saveUsername(){
	var username = document.getElementById("username").value;
	var userNode = document.createTextNode(username);
	document.getElementById("name").appendChild(userNode);
	document.getElementById("start").style.visibility = "hidden";
}