<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="com.cs5300.proj1a.servlets.*"%>
<%@ page import="com.cs5300.proj1a.utils.*"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>CS5300 Project1</title>
<link rel="stylesheet" href="css/main.css">
<!-- Bootstrap-->
<link rel="stylesheet" href="bootstrap/css/bootstrap.min.css">

<!-- Optional theme -->
<link rel="stylesheet" href="bootstrap/css/bootstrap-theme.min.css">


<script src="js/jquery-2.1.0.min.js"></script>
<script src="bootstrap/js/bootstrap.min.js"></script>

</head>
<body>
	<div id="loading"></div>
	<div id='screen'></div>
	
	<div id="main" style="margin-left: 10px">
		<br />
		<div>
			<b>Server Message :</b> <br />
			<p id="serverMsgDisplay"></p>
		</div>
		<div>
			<b>Local Server :</b> <br />
			<p id="localServer"><%=Utils.SERVER_IP %></p>
		</div>
		<div>
			<b>Primary Server :</b> <br />
			<p id="primaryServer"></p>
		</div>
		<div>
			<b>Backup Server :</b> <br />
			<p id="backupServer"></p>
		</div>
		<div>
			<b>Data found in:</b> <br />
			<p id="foundServer"></p>
		</div>
		<b>Session ID</b> <br />
		<p id="serverName"></p>
		<div>
			<b>Version</b> <br />
			<p id="version"></p>
		</div>
		<b>Expiration time :</b>
		<p id="cookieExpTime"></p>
		<div>
			<b>Bootstrap View :</b> <br />
			<p id="bootStrapView"></p>
			<br /> <b>Server View :</b> <br />
			<p id="serverView"></p>
			<br />
		</div>
		<button class="btn btn-default btn-sm" id="refreshViews">
			<span class="glyphicon glyphicon-refresh"></span> Reload Views
		</button>
				
			<div class="col-lg-4">
				<div class="input-group">
					<span class="input-group-btn">
						<button class="btn btn-default btn-sm" type="button" id="replace">
							<span class=" glyphicon glyphicon-upload"></span> Replace
						</button>
					</span> <input type="text" name="message" maxlength="30"
						id="serverMsgInput" class="form-control">
				</div>
			</div>
		
	

		<button class="btn btn-default btn-sm" id="refresh">
			<span class="glyphicon glyphicon-refresh"></span> Refresh
		</button>
		&nbsp;


		<button class="btn btn-default btn-sm" id="logout">
			<span class="glyphicon glyphicon-log-out"></span> Logout
		</button>





	</div>

	<script src="js/jquery-ui-1.10.4.custom.min.js"></script>

	<script type="text/javascript">
$(function() {
    //This script is auto invoked when the page loads
   // $('#screen').css({ opacity: 0.7, 'width':$(document).width(),'height':$(document).height()});
    
    $('#loading').html(
    		'<img src="images/loading.GIF"> Loading!!...'
    );
   
    $.ajax({
		    url : "SessionManager",
		    type: "GET",
		    dataType : "text",
		    data : {},
		    success: function(data, textStatus, jqXHR)
		    {
		    	$('#loading').css('display', 'none');
		    	var response = data.split("@");
		    	var responseParts = response[0].split("|");
		    	$("#serverMsgDisplay").effect("highlight", {}, 1500);
		        $('#serverMsgDisplay').text(responseParts[0]);
		        $("#cookieExpTime").effect("highlight", {}, 1500);
		        $('#cookieExpTime').text(responseParts[1]);
		        $("#serverName").effect("highlight", {}, 1500);
		        $('#serverName').text(responseParts[2]);
		        var parts = response[1].split("#");
		        $("#version").effect("highlight", {}, 1500);
		        $('#version').text(parts[1]);
		        $("#primaryServer").effect("highlight", {}, 1500);
		        $('#primaryServer').text(parts[2]);
		        $("#backupServer").effect("highlight", {}, 1500);
		        $('#backupServer').text(parts[3]);
		        $("#foundServer").effect("highlight", {}, 1500);
		        $('#foundServer').text(parts[4]);
		        loadViews();

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
		$('#loading').html(
		    		'<img src="images/loading.GIF"> Loading!!...'
		);
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
			    	$('#loading').css('display', 'none');
			    	var response = data.split("@");
			    	var responseParts = response[0].split("|");
			    	$("#serverMsgDisplay").effect("highlight", {}, 1500);
			        $('#serverMsgDisplay').text(responseParts[0]);
			        $("#cookieExpTime").effect("highlight", {}, 1500);
			        $('#cookieExpTime').text(responseParts[1]);
			        $("#serverName").effect("highlight", {}, 1500);
			        $('#serverName').text(responseParts[2]);
			        var parts = response[1].split("#");
			        $("#version").effect("highlight", {}, 1500);
			        $('#version').text(parts[1]);
			        $("#primaryServer").effect("highlight", {}, 1500);
			        $('#primaryServer').text(parts[2]);
			        $("#backupServer").effect("highlight", {}, 1500);
			        $('#backupServer').text(parts[3]);
			        $("#foundServer").effect("highlight", {}, 1500);
			        $('#foundServer').text(parts[4]);
			    },
			    error: function (jqXHR, textStatus, errorThrown)
			    {
			 		alert(errorThrown);
			    }
		});
		
	}
});
$( "#refresh" ).click(function() {
	$('#loading').html(
    		'<img src="images/loading.GIF"> Loading!!...'
	);
	$.ajax({
		    url : "SessionManager",
		    type: "GET",
		    dataType : "text",
		    data : {
		    	param : "refresh"
		    },
		    success: function(data, textStatus, jqXHR)
		    {
		    	$('#loading').css('display', 'none');
		    	var response = data.split("@");
		    	var responseParts = response[0].split("|");
		    	$("#serverMsgDisplay").effect("highlight", {}, 1500);
		        $('#serverMsgDisplay').text(responseParts[0]);
		        $("#cookieExpTime").effect("highlight", {}, 1500);
		        $('#cookieExpTime').text(responseParts[1]);
		        $("#serverName").effect("highlight", {}, 1500);
		        $('#serverName').text(responseParts[2]);
		        var parts = response[1].split("#");
		        $("#version").effect("highlight", {}, 1500);
		        $('#version').text(parts[1]);
		        $("#primaryServer").effect("highlight", {}, 1500);
		        $('#primaryServer').text(parts[2]);
		        $("#backupServer").effect("highlight", {}, 1500);
		        $('#backupServer').text(parts[3]);
		        $("#foundServer").effect("highlight", {}, 1500);
		        $('#foundServer').text(parts[4]);
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
$( "#refreshViews" ).click(function() {
	loadViews();

});
function loadViews(){
	$('#loading').html(
    		'<img src="images/loading.GIF"> Loading!!...'
	);
    $.ajax({
	    url : "GetViews",
	    type: "GET",
	    dataType : "text",
	    data : {
	    	param : "bootStrapView"
	    },
	    success: function(data, textStatus, jqXHR)
	    {
	    	if(data==="NoCookie"){
	    		
	    		alert("Invalid session. New Session will be generated!");
		    	window.location.reload();
		    	
	    	}else{
	    		$('#loading').css('display', 'none');
		    	var responseParts = data.split("|");
		      	$("#bootStrapView").effect("highlight", {}, 1500);
		      	$('#bootStrapView').text(responseParts[0]);
		      	$("#serverView").effect("highlight", {}, 1500);
		      	$('#serverView').text(responseParts[1]);
	    	}
	      	
	    },
	    error: function (jqXHR, textStatus, errorThrown)
	    {
	 		alert(errorThrown);
	    }
	});
}
</script>
</body>
</html>