var canvas = document.getElementById('testcanvas1');
var ctx = canvas.getContext('2d');
var color = "rgb(255,0,0)";
var xRoute = [], yRoute = [];
var x,y,a,b,actualPosX, actualPosY,shape,active = false,isPolygonActive=false;

if (window.addEventListener) {
	addEventListener("load", drawCanvas, false);
	document.getElementById("deleteHistory").addEventListener('click',
			function() {
				sendJSONBack("DELETE", "ALL");
				cleanAll();
			}, false);
	document.getElementById("colorChanger").addEventListener('change',
			function(){
		color = document.getElementById("colorChanger").value;
		ctx.fillStyle = color;
		ctx.strokeStyle = color;
	}, false);
	document.getElementById("userinput").onkeyup = function(event){
		if(event.key == "Enter"){
			chatfunction();
		}
	}
	canvas.onmouseover = function(e){
		canvas.focus();
	}
}


function drawCanvas(){
	ctx.fillStyle = color;
	ctx.strokeStyle = color;
}

function changeShape(i) {
	shape = i;
}

function Line(x,y,width,height){
	this.x = x;
	this.y = y;
	this.a = width;
	this.b = height;
	this.draw = function(){
		ctx.beginPath();
		ctx.moveTo(this.x,this.y);
		ctx.lineTo(this.a,this.b);
		ctx.stroke();
	}
	this.sendJson = function(){
		var name = document.getElementById("name").textContent;
		var content = {
			type : "LINE",
			name : name,
			content : {
				color : color,
				x : this.x,
				y : this.y,
				a : this.a,
				b : this.b
			}
		};
		sendJSONBack("HISTORY", content);
	}
}

function Rectangle(x,y,width,height){
	this.x = x;
	this.y = y;
	this.a = width;
	this.b = height;
	this.draw = function(){
		ctx.strokeRect(this.x,this.y,this.a-this.x,this.b-this.y);
	}
	this.sendJson = function(){
		var name = document.getElementById("name").textContent;
		var content = {
			type : "RECTANGLE",
			name : name,
			content : {
				color : color,
				x : this.x,
				y : this.y,
				a : this.a,
				b : this.b
			}
		};
		sendJSONBack("HISTORY", content);
	}
}

function Ellipse(x,y,width,height){
	this.x = x;
	this.y = y;
	this.a = width;
	this.b = height;
	this.draw = function(){
		ctx.beginPath();
		ctx.arc(this.x, this.y, Math.sqrt((this.x - this.a) * (this.x - this.a) + (this.y - this.b)* (this.y - this.b)), 0, 2 * Math.PI, true);
		ctx.stroke();
	}
	this.sendJson = function(){
		var name = document.getElementById("name").textContent;
		var rad = Math.sqrt((this.x - this.a) * (this.x - this.a) + (this.y - this.b) * (this.y - this.b));
		var content = {
			type : "ELLIPSE",
			name : name,
			content : {
				color : color,
				x : this.x,
				y : this.y,
				a : this.a,
				b : this.b,
				rad : rad
			}
		};
		sendJSONBack("HISTORY", content);
	}
}

function Polygon(xRoute,yRoute){
	this.xRoute = xRoute;
	this.yRoute = yRoute;
	this.draw = function(){
		ctx.beginPath();
		ctx.moveTo(this.xRoute[0],this.yRoute[0]);
		for(i = 1; i < this.xRoute.length; ++i){
				ctx.lineTo(this.xRoute[i],this.yRoute[i]);
		}
		ctx.closePath();
		ctx.stroke();
	}
	this.sendJson = function(){
		var name = document.getElementById("name").textContent;
		var content = {
			type : "POLYGON",
			name : name,
			content : {
				color : color,
				aElements : this.xRoute,
				bElements : this.yRoute
			}
		};
		sendJSONBack("HISTORY", content);
	}
}

function Snake(xRoute,yRoute){
	this.xRoute = xRoute;
	this.yRoute = yRoute;
	this.draw = function(){
		ctx.beginPath();
		ctx.moveTo(this.xRoute[0],this.yRoute[0]);
		for(i = 1; i < this.xRoute.length; ++i){
			if(Math.sqrt(Math.pow((this.xRoute[i]-this.xRoute[i-1]),2)+Math.pow((this.yRoute[i]-this.yRoute[i-1]),2))>15){
				ctx.moveTo(this.xRoute[i],this.yRoute[i]);
			}else{
				ctx.lineTo(this.xRoute[i],this.yRoute[i]);
			}
		}
		ctx.stroke();
	}
	this.sendJson = function(){
		var name = document.getElementById("name").textContent;
		var content = {
			type : "SNAKE",
			name : name,
			content : {
				color : color,
				aElements : this.xRoute,
				bElements : this.yRoute
			}
		};
		sendJSONBack("HISTORY", content);
	}
}

canvas.onmousedown = function(e) {
	x = actualPosX;
	y = actualPosY;
	if(shape == 0){
		active = true;
	}
}
canvas.onmousemove = function(e) { 
	var rect = canvas.getBoundingClientRect();
	actualPosX = e.clientX - canvas.offsetLeft - rect.left + document.body.scrollLeft;
	actualPosY = e.clientY - canvas.offsetTop - rect.top + document.body.scrollTop;	
	if(active){
		xRoute.push(actualPosX);
		yRoute.push(actualPosY);
	}
}
canvas.onkeydown = function(e){
	console.log("onkeydown");
	if(e.ctrlKey){
		isPolygonActive = true;
	}
}
canvas.onkeyup = function(e){
	console.log("onkeyup");
	if(isPolygonActive){
		isPolygonActive = false;
		var pol = new Polygon(xRoute,yRoute);
		pol.draw();
		pol.sendJson();
		xRoute = [];
		yRoute = [];
	}
}
canvas.onmouseup = function(e) {
	active = false;
	a = actualPosX;
	b = actualPosY;
	switch(shape){
	case 0:
		var sna = new Snake(xRoute,yRoute);
		sna.draw();
		sna.sendJson();
		xRoute = [];
		yRoute = [];
		break;
	case 1:
		var rec = new Rectangle(x,y,a,b);
		rec.draw();
		rec.sendJson();
		break;
	case 2:
		var ell = new Ellipse(x,y,a,b);
		ell.draw();
		ell.sendJson();
		break;
	case 3:
		var li = new Line(x,y,a,b);
		li.draw();
		li.sendJson();
		break;
	case 4:
		if(isPolygonActive){
			xRoute.push(actualPosX);
			yRoute.push(actualPosY);
		}
		break;
	}
}

function drawObject(obj) {
	var obj2 = obj.content;
	ctx.fillStyle = obj2.color;
	ctx.strokeStyle = obj2.color;
	switch (obj.type) {
	case "RECTANGLE":
		var rec = new Rectangle(obj2.a, obj2.b, obj2.x, obj2.y);
		rec.draw();
		break;
	case "ELLIPSE":
		var ell = new Ellipse(obj2.x, obj2.y, obj2.a, obj2.b);
		ell.draw();
		break;
	case "SNAKE":
		var sna = new Snake(obj2.aElements,obj2.bElements);
		sna.draw();
		break;
	case "LINE":
		var li = new Line(obj2.a, obj2.b, obj2.x, obj2.y);
		li.draw();
		break;
	case "POLYGON":
		var pol = new Polygon(obj2.aElements,obj2.bElements);
		pol.draw();
		break;
	}
}

