var activeElements = [];
var elementsToAnimate = [], animatedElements = [];
var animatedFrameContext;
var webSocket = new WebSocket('ws://localhost:8080/WebProg2/websocket/robot');
// var webSocket = new WebSocket('ws://195.37.49.24/sos16_01/websocket/robot');
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

webSocket.onclose = function(event) {
	onClose(event);
}

function onClose(event) {
	sendMessageToMessageBox('Connection closed');
}

function onOpen(event) {
	sendMessageToMessageBox('Connection open');
}

function onError(event) {
	console.log("error: " + event.data);
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

function cleanAfterId() {
	cleanAfterBeforeSelected("DELETEAFTER");
}

function cleanBeforeId() {
	cleanAfterBeforeSelected("DELETEBEFORE");
}

function cleanAfterBeforeSelected(type) {
	if (activeElements.length == 1) {
		var text = {
			id : activeElements[0]
		}
		sendJSONBack(type, text);
		cleanAll();
	} else {
		sendMessageToMessageBox("Error: Select just one Element");
	}
}

function onMessage(event) {
	var obj = JSON.parse(event.data);
	if (obj.type == "HISTORY") {
		createHistoryObject(obj.content);
		drawObject(obj.content);
	} else if (obj.type == "CLEANCANVAS") {
		cleanCanvas();
	} else if (obj.type == "CLEANUP") {
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
		if (animatedElements.indexOf(e.getAttribute("id")) != -1) {
			console.log("start " + animatedElements + " id "
					+ e.getAttribute("id") + "   "
					+ animatedElements.indexOf(e.getAttribute("id")));
			e.setAttribute("class", "history inActive animated");
		} else {
			e.setAttribute("class", "history inActive");
		}
		var index = activeElements.indexOf(e.getAttribute("id"));
		activeElements.splice(index, 1);
		var name = document.getElementById("name").textContent;
		if (e.innerHTML.indexOf(name) != -1) {
			index = elementsToAnimate.indexOf(e.getAttribute("id"));
			elementsToAnimate.splice(index, 1);
		}
	} else {
		e.setAttribute("class", "active");
		activeElements.push(e.getAttribute("id"));
		var name = document.getElementById("name").textContent;
		if (e.innerHTML.indexOf(name) != -1) {
			elementsToAnimate.push(e.getAttribute("id"));
		}
	}
}

function animate() {
	function frame() {
		if (animatedElements.length > 0 && isAnimated) {
			var text = {
				ids : animatedElements
			}
			sendJSONBack("CLEANCANVAS", null);
			sendJSONBack("ANIMATE", text);
			animatedFrameContext = window.requestAnimationFrame(frame);
		} else {
			isAnimated = false;
			window.cancelAnimationFrame(animatedFrameContext);
		}
	}
	animatedFrameContext = window.requestAnimationFrame(frame);
}

function checkAnimate(e) { // conflict ?
	console.log("try to animated this objects " + elementsToAnimate);
	elementsToAnimate = elementsToAnimate.filter(function(x) {
		return animatedElements.indexOf(x) < 0
	})
	console.log("elments left " + elementsToAnimate);
	for (i = 0; i < elementsToAnimate.length; ++i) {
		var ele = document.getElementById(elementsToAnimate[i]);
		if (ele != null) {
			ele.setAttribute("class", "history animated");
		}
		console.log("push element to animated " + elementsToAnimate[i]);
		animatedElements.push(elementsToAnimate[i]);
	}
	if (!isAnimated) {
		console.log("go animation go");
		isAnimated = true;
		animate();
	}
	activeElements = [];
	elementsToAnimate = [];
}

function stopAnimation() {
	for (i = 0; i < activeElements.length; ++i) {
		var ele = document.getElementById(activeElements[i]);
		ele.setAttribute("class", "history inActive");
		var index = animatedElements.indexOf(activeElements[i]);
		animatedElements.splice(index, 1);
	}
	var text = {
		ids : activeElements
	}
	sendJSONBack("REMOVEANIMTEDFLAG", text);
	elementsToAnimate = [];
	activeElements = [];
}

function createHistoryObject(content) {
	if (document.getElementById(JSON.stringify(content.id)) == undefined) {
		var div = document.createElement("div");
		div.setAttribute("onclick", "changeAtt(this)");
		div.setAttribute("id", JSON.stringify(content.id));
		var text = JSON.stringify(content.content);
		if (content.type == "SNAKE") {
			text = "length: " + content.content.aElements.length;
		}
		div.appendChild(document.createTextNode(content.name + ": "
				+ content.type + ": " + text));
		document.getElementById("log").appendChild(div);
		if (content.animated) {
			console.log("animated "
					+ document.getElementById("name").textContent + " / "
					+ content.name);
			div.setAttribute("class", "history inActive animated");
			if (document.getElementById("name").textContent == content.name) {
				console.log("go animation " + JSON.stringify(content.id));
				elementsToAnimate.push(JSON.stringify(content.id));
				checkAnimate();
			}
		} else {
			div.setAttribute("class", "history inActive");
		}
	}
}

function deleteObjectByIds() {
	var text = {
		ids : activeElements
	}
	sendJSONBack("DELETEBYID", text);
	cleanById(activeElements);
	activeElements = [];
	elementsToAnimate = [];
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
	if (username != "") {
		var userNode = document.createTextNode(username);
		document.getElementById("name").appendChild(userNode);
		document.getElementById("start").style.visibility = "hidden";
		sendJSONBack("LOGEDIN", null);
	} else {
		alert("Nutzernamen eingeben");
	}
}