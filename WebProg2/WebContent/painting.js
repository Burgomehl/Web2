var shape = 0;
var color = "rgb(255,0,0)";
var snakeAElements = [];
var snakeBElements = [];
function changeShape(i) {
	shape = i;
}
function changeColor(i) {
	color = i;
	sendMessageToMessageBox(i);
}
// Die Canvas-Funktion beim Laden der Seite aufrufen
if (window.addEventListener) {
	addEventListener("load", drawCanvas, false);
}
// Das Canvas-Element
function drawCanvas() {
	var canvas = document.getElementById('testcanvas1');
	var context = canvas.getContext('2d');
	document.getElementById("clear").addEventListener('click', function() {}, false);
	document.getElementById("deleteHistory").addEventListener('click',
			function() {
				context.clearRect(0, 0, canvas.width, canvas.height);
				sendJSONBack("DELETE", "ALL");
				var myNode = document.getElementById("log");
				while (myNode.firstChild) {
					myNode.removeChild(myNode.firstChild);
				}
			}, false);
	
	// Cursorposition
	var x, y, a, b;
	a = 0;
	var rect = canvas.getBoundingClientRect();
	canvas.onmousemove = function(e) {
		x = e.clientX - canvas.offsetLeft - rect.left
				+ document.body.scrollLeft;
		y = e.clientY - canvas.offsetTop - rect.top + document.body.scrollTop;
		paint();
	}
	// Malen
	var active = false;
	canvas.onmousedown = function() {
		if (shape == 0) {
			active = true;
			a = 0;
			b = 0;
		} else {
			a = x;
			b = y;
		}
	}
	canvas.onmouseup = function() {
		if (shape == 0) {
			active = false;
			a = 0;
			b = 0;
			var name = document.getElementById("name").textContent;
			var content = {
				type : "SNAKE",
				name : name,
				content : {
					color : color,
					aElements : snakeAElements,
					bElements : snakeBElements
				}
			};
			sendJSONBack("HISTORY", content);
			snakeAElements = [];
			snakeBElements = [];
		} else {
			active = true;
		}
	}
	
	
	function paint() {
		context.fillStyle = color;
		context.strokeStyle = color;
		context.beginPath();
		switch (shape) {
		case 0:
			if (active) {
				context.moveTo(x, y);
				if (a != 0) {
					context.lineTo(a, b);
				}
				a = x;
				b = y;
				snakeAElements.push(a);
				snakeBElements.push(b);
			}
			break;
		case 1:
			if (active) {
				var tempX = x - a;
				var tempY = y - b;
				context.rect(a, b, tempX, tempY);
				active = false;
				var name = document.getElementById("name").textContent;
				var content = {
					type : "RECTANGLE",
					name : name,
					content : {
						color : color,
						x : tempX,
						y : tempY,
						a : a,
						b : b
					}
				};
				sendJSONBack("HISTORY", content);
			}
			break;
		case 2:
			if (active) {
				context.arc(x, y, Math.sqrt((x - a) * (x - a) + (y - b)
						* (y - b)), 0, 2 * Math.PI, true);
				active = false;
				var name = document.getElementById("name").textContent;
				var rad = Math.sqrt((x - a) * (x - a) + (y - b) * (y - b));
				var content = {
					type : "ELLIPSE",
					name : name,
					content : {
						color : color,
						x : x,
						y : y,
						a : a,
						b : b,
						rad : rad
					}
				};
				sendJSONBack("HISTORY", content);
			}
			break;
		case 3:
			if(active){
				context.moveTo(a,b);
				context.lineTo(x,y);
				active = false;
			}
			break;
		case 4:
			
			break;

		}
		context.closePath();
		context.stroke();
	}
	// Default-Farbe
}

function drawObject(obj) {
	var canvas = document.getElementById('testcanvas1');
	var context = canvas.getContext('2d');
	var obj2 = obj.content;
	context.fillStyle = obj2.color;
	context.strokeStyle = obj2.color;
	switch (obj.type) {
	case "RECTANGLE":
		context.strokeRect(obj2.a, obj2.b, obj2.x, obj2.y);
		break;
	case "ELLIPSE":
		context.arc(obj2.x, obj2.y, obj2.rad, 0, 2 * Math.PI, true);
		break;
	case "SNAKE":
		context.moveTo(obj2.aElements[0],obj2.bElements[0]);
		context.beginPath();
		for (i = 1; i < obj2.aElements.length; ++i) {
			context.lineTo(obj2.aElements[i], obj2.bElements[i]);
		}
		break;
	}

	context.closePath();

}