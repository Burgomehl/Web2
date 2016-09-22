var activeElements = [];
var elementsToAnimate = [], animatedElements = [];
var webSocket = new WebSocket('ws://localhost:8080/WebProg2/websocket/robot');
//var webSocket = new WebSocket('ws://195.37.49.24/sos16_01/websocket/robot');
var isAnimated = false;
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
	console.log("error: "+event.data);
}

function cleanAll() {
	cleanCanvas();
	var myNode = document.getElementById("log");
	while (myNode.firstChild) {
		myNode.removeChild(myNode.firstChild);
	}
	animatedElements = [];
	elementsToAnimate = [];
	activeElements = [];
}

function cleanCanvas() {
	var canvas = document.getElementById('testcanvas1');
	var context = canvas.getContext('2d');
	context.clearRect(0, 0, canvas.width, canvas.height);
}

function cleanById(ids) {
	var myNode = document.getElementById("log");
	for (i = 0; i < ids.length; ++i) {
		var nodeToDelete = document.getElementById(ids[i]);
		myNode.removeChild(nodeToDelete);
	}
}

function onMessage(event) {
	var obj = JSON.parse(event.data);
	if (obj.type == "HISTORY") {
		createHistoryObject(obj.content);
		drawObject(obj.content);
	} else if (obj.type == "CLEANCANVAS") {
		cleanCanvas();
	}else if (obj.type == "CLEANUP") {
		cleanAll();
	} else {
		sendMessageToMessageBox(obj.name + ":" + obj.content);
	}
}

function sendMessageToMessageBox(string) {
	document.getElementById('messages').innerHTML += "<div>" + string
			+ "</div>";
}

function chatfunction() {
	var content = document.getElementById("userinput").value;
	sendJSONBack("TEXT", content);
	document.getElementById("userinput").value = "";
	return false;
}

function changeAtt(e) {
	if (e.classList.contains("active")) {
		e.setAttribute("class", "history inActive");
		var index = activeElements.indexOf(e);
		activeElements.splice(index, 1);
		var name = document.getElementById("name").textContent;
		if(e.innerHTML.indexOf(name) != -1){
			index = elementsToAnimate.indexOf(e);
			elementsToAnimate.splice(index, 1);
		}
	} else {
		e.setAttribute("class", "active");
		activeElements.push(e.getAttribute("id"));
		var name = document.getElementById("name").textContent;
		if(e.innerHTML.indexOf(name) != -1){
			elementsToAnimate.push(e.getAttribute("id"));
		}
	}
}

function animate() {
//	var id = setInterval(frame, 10);
	function frame() {
		if (animatedElements.length > 0 && isAnimated) {
			var text = {
				ids : animatedElements
			}
			sendJSONBack("CLEANCANVAS",null);
			sendJSONBack("ANIMATE", text);
			window.requestAnimationFrame(frame);
		} else {
//			clearInterval(id);
			isAnimated = false;
		}
	}
	window.requestAnimationFrame(frame);
}

function checkAnimate(e) { // Sideeffect -> nur markierte Objekte werden animiert.
	animatedElements = [];
	console.log(activeElements);
	for(i = 0; i < elementsToAnimate.length; ++i){
		var ele = document.getElementById(elementsToAnimate[i]);
		ele.setAttribute("class", "history animated");
		animatedElements.push(elementsToAnimate[i]);
		var index = activeElements.indexOf(ele);
		activeElements.splice(index, 1);
	}
	console.log(activeElements);
	if (!isAnimated) {
		animate();
		isAnimated = true;
	}
}

function stopAnimation(){
	animatedElements = [];
	isAnimated = false;
}

function createHistoryObject(content) {
	if (document.getElementById(JSON.stringify(content.id)) == undefined) {
		var div = document.createElement("div");
		div.setAttribute("onclick", "changeAtt(this)");
		div.setAttribute("class", "history inActive");
		div.setAttribute("id", JSON.stringify(content.id));
		var text = JSON.stringify(content.content);
		div.appendChild(document.createTextNode(content.name + ":" + text + ":"
				+ content.type));
		document.getElementById("log").appendChild(div);
	}
}

function deleteObjectByIds() {
	var text = {
		ids : activeElements
	}
	cleanCanvas();
	cleanById(activeElements);
	activeElements = [];
	sendJSONBack("DELETEBYID", text);
}

function sendJSONBack(type, content) {
	var name = document.getElementById("name").textContent;
	var cont = {
		type : type,
		name : name,
		content : content
	};
	webSocket.send(JSON.stringify(cont));
}

function saveUsername() {
	var username = document.getElementById("username").value;
	if(username != ""){
		var userNode = document.createTextNode(username);
		document.getElementById("name").appendChild(userNode);
		document.getElementById("start").style.visibility = "hidden";
	}else{
		alert("Nutzernamen eingeben");
	}
}