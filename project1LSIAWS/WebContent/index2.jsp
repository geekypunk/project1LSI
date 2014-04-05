<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<div>
<br/>
<div><b>Server Message :</b> <p id="serverMsgDisplay"></p></div>
<b>Cookie Value :</b> <p id="serverName"></p>
<b>Cookie Expiration time :</b> <p id="cookieExpTime"></p>
</div>
<br/>
<div> <button type="button" id="replace">Replace</button> 
<input type="text" name="message" maxlength="30" id="serverMsgInput"/> <br/>
<button type="button" id="refresh">Refresh</button> <br/> 
<button type="button" id="logout">Logout</button> </div>
<script src="js/jquery-2.1.0.min.js"></script>
<script src="js/jquery-ui-1.10.4.custom.min.js"></script>
<script type="text/javascript">
$(function() {
    //This script is auto invoked when the page loads
    $.ajax({
		    url : "SessionManager",
		    type: "GET",
		    dataType : "text",
		    data : {},
		    success: function(data, textStatus, jqXHR)
		    {
		    	var responseParts = data.split("|");
		    	$("#serverMsgDisplay").effect("highlight", {}, 1500);
		        $('#serverMsgDisplay').text(responseParts[0]);
		        $("#cookieExpTime").effect("highlight", {}, 1500);
		        $('#cookieExpTime').text(responseParts[1]);
		        $("#serverName").effect("highlight", {}, 1500);
		        $('#serverName').text(responseParts[2]);

		    },
		    error: function (jqXHR, textStatus, errorThrown)
		    {
		 		alert(errorThrown);
		    }
	});
});
$( "#replace" ).click(function() {
	var oldMessage = $('#serverMsgDisplay').text().trim();
	var newMessage = $('#serverMsgInput').val().trim();
	if(newMessage.length ===0){
		alert("Please type a new message");
	}
	else if(oldMessage === newMessage){
		alert("This message looks the same. Please enter a new one!");
	}
	else {
	
		//Limiting the new message size to 30 characters
		if(newMessage.length>30){
			newMessage = newMessage.substring(0,29);
		}
		
		$.ajax({
			    url : "SessionManager",
			    type: "GET",
			    dataType : "text",
			    data : {
			    	param : "replace",
			    	message:newMessage
			    },
			    success: function(data, textStatus, jqXHR)
			    {
			       
			    	var responseParts = data.split("|");
		    	 	$("#serverMsgDisplay").effect("highlight", {}, 1500);
		       	 	$('#serverMsgDisplay').text(responseParts[0]);
		       	 	$("#cookieExpTime").effect("highlight", {}, 1500);
		      	  	$('#cookieExpTime').text(responseParts[1]);
		     	   	$("#serverName").effect("highlight", {}, 1500);
		      	  	$('#serverName').text(responseParts[2]);
			    },
			    error: function (jqXHR, textStatus, errorThrown)
			    {
			 		alert(errorThrown);
			    }
		});
		
	}
});
$( "#refresh" ).click(function() {
	$.ajax({
		    url : "SessionManager",
		    type: "GET",
		    dataType : "text",
		    data : {
		    	param : "refresh"
		    },
		    success: function(data, textStatus, jqXHR)
		    {
		    	var responseParts = data.split("|");
		    	//$("#serverMsgDisplay").effect("highlight", {}, 1500);
		       	$('#serverMsgDisplay').text(responseParts[0]);
		       	$("#cookieExpTime").effect("highlight", {}, 1500);
		      	$('#cookieExpTime').text(responseParts[1]);
		     	$("#serverName").effect("highlight", {}, 1500);
		      	$('#serverName').text(responseParts[2]);
		    },
		    error: function (jqXHR, textStatus, errorThrown)
		    {
		 		alert(errorThrown);
		    }
	});
});

$( "#logout" ).click(function() {
	$.ajax({
		    url : "SessionManager",
		    type: "GET",
		    dataType : "text",
		    data : {
		    	param : "logout"
		    },
		    success: function(data, textStatus, jqXHR)
		    {
		    	alert("You have been logged out! This page will be refreshed and you will be logged back in with a new session!");
		    	window.location.reload();
		    	
		    },
		    error: function (jqXHR, textStatus, errorThrown)
		    {
		 		alert(errorThrown);
		    }
	});

});
</script>
</body>
</html>