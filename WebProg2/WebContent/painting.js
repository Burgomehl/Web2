  var shape = 0;
                var color = "rgb(255,0,0)";
                function changeShape(i){
                    shape = i;
                }
                function changeColor(i){
                    color = i;
                    console.log(i)
                    sendMessageToMessageBox(i);
                }
                // Die Canvas-Funktion beim Laden der Seite aufrufen
                if(window.addEventListener){
                    addEventListener("load", drawCanvas, false);
                }
                // Das Canvas-Element
                function drawCanvas(){
                    var canvas = document.getElementById('testcanvas1');
                    var context = canvas.getContext('2d');
                    document.getElementById("clear").addEventListener('click',function(){
                        context.clearRect(0,0,canvas.width,canvas.height);
                    },false);
                    // Cursorposition
                    var x, y,a,b;
                    a=0;
                    var rect = canvas.getBoundingClientRect();
                    canvas.onmousemove = function(e){
                        x = e.clientX-canvas.offsetLeft-rect.left;
                        y = e.clientY-canvas.offsetTop-rect.top;
                        paint();
                    }
                    // Malen
                    var active = false;
                        canvas.onmousedown = function(){
                            if(shape==0){
                                active = true; a=0; b=0;
                            }else{
                                a = x;
                                b = y;
                            }
                        }
                        canvas.onmouseup = function(){
                            if(shape==0){
                                active = false; a=0; b=0;
                            }else{
                                active = true;
                            }
                        }
                    function paint(){
                        context.fillStyle = color;
                        context.strokeStyle = color;
                        context.beginPath();
                            if(shape==0){
                                if(active){
                                context.moveTo(x,y);
                                if(a!=0){
                                    context.lineTo(a,b);
                                }
                                a = x;
                                b = y;
                            }
                            }else if (shape == 1) {
                                if(active){
                                    context.rect(a,b,x-a,y-b);
                                    active = false;
                                    var text= "a "+a+" b "+b+" x-a "+(x-a)+" y-b "+(y-b);
                                    var content = {
                    						type : "RECTANGLE",
                    						content : text
                    					};
                    				sendJSONBack("HISTORY",content);
                                }
                            }else if (shape == 2) {
                                if(active){
                                    context.arc(x, y, Math.sqrt((x-a)*(x-a)+(y-b)*(y-b)), 0, 2* Math.PI, true);
                                    active = false;
                                    var text= "x, y, Math.sqrt((x-a)*(x-a)+(y-b)*(y-b)) x:"+x+" y:"+y+"a und b: "+a+"/"+b;
                                    var content = {
                    						type : "ELLIPSE",
                    						content : text
                    					};
                    				sendJSONBack("HISTORY",content);
                                }
                            }
                            context.closePath();
                            context.stroke();
                    }
                    // Default-Farbe
                }