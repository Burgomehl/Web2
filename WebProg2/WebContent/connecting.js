var webSocket = new WebSocket(
				'ws://localhost:8080/WebProg2/websocket'
			);
			webSocket.onerror = function(event){
				onError(event)
			};
			
			webSocket.onopen = function(event){
				onOpen(event)
			};
			
			webSocket.onmessage = function(event){
				onMessage(event)
			};
			
			function onOpen(event){
				sendMessageToMessageBox('Connection established');
			}
			
			function onError(event){
				alert(event.data);
			}
			
			function onMessage(event){
				sendMessageToMessageBox(event.data);
			}
			
			function sendMessageToMessageBox(string){
				document.getElementById('messages').innerHTML += "<div>"+string+"</div>" ;
			}
			
			function start(){
				var text = document.getElementById("userinput").value;
				
				var content = {
						type : "TEXT",
						content : text
					};
				sendJSONBack("TEXT",content);
				return false;
			}
			
			function sendJSONBack(type,content){
				if(type == "HISTORY"){
					document.getElementById("log").innerHTML +=  "<div class='history'>"+JSON.stringify(content)+"</div>";
				}
				var cont = {
						type:type,
						content: content
				};
				webSocket.send(JSON.stringify(cont));
			}