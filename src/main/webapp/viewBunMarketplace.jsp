<%@ page language="java"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Bun Marketplace</title>
	<script src="http://ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js"></script>
	<link rel="stylesheet" href="//ajax.googleapis.com/ajax/libs/jqueryui/1.11.0/themes/smoothness/jquery-ui.css" />
	<script src="//ajax.googleapis.com/ajax/libs/jqueryui/1.11.0/jquery-ui.min.js"></script>
	<link href="//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css" rel="stylesheet">	
	<script src="//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js"></script>
</head>
<body>


	<div class="page-header">
		<h1>Bun Marketplace <br>
		<small>View Buns from the Baker marketplace, at:
			<%
 out.println("API : " + System.getProperty("marketplace_api_endpoint"));
%>so that you can install them into your resource</small>
			</h1>
	</div>

	<div style="padding-left: 10px;padding-right: 10px; " id="placeholder"></div>
    
    <script>
    
    //var repoMarketplaceURL = 'http://localhost:13000/baker/services/api/repo/buns';
    
    var repoMarketplaceURL = '<% out.print(System.getProperty("marketplace_api_endpoint")); %>/services/api/repo/buns';
    
    var bakerURLatFORGEBoxInstall = 'http://localhost:13001';
    
    $(document).ready(function() {
    	loadData();
  	});
    
    function getUrlVars() {
        var vars = {};
        var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
            vars[key] = value;
        });
        return vars;
    }
    
    
    

    function loadData(){
	  $.getJSON(repoMarketplaceURL, function(data) {
	      var output="<table class='table table-bordered table-condensed ' >"+ 
	      		"<tr>" + 
	   	  	   "<th>ID</th>" +
	    	   "<th>Name</th>" +
	    	   "<th>Status</th>" +
	    	   "<th nowrap >Version <br> (Installed Version)</th>" +
	    	   "<th>shortDescription</th>" +
	    	   "<th>longDescription</th>" +
	    	   "<th>Image</th>" +
	    	   "<th>owner</th>" +
	    	   "<th>action</th>" +
	    	 	"</tr>";
	        for (var i in data) {
	        	var ownerId = -1;
	        	var ownerUsername = 'NULL';
	        	
	        	if (data[i].owner){
	        		ownerId = data[i].owner.id;
	        		ownerUsername = data[i].owner.username;
	        		ownerOrganization = data[i].owner.organization;
	        	}
	        	
	        	//data[i].uuid;
	        	//data[i].packageLocation
	        	
	            output+='<tr>' + 
	            '<td>' + data[i].id + '</td> ' + 
	            '<td><p>' + data[i].name + '</p>'+ '</td> ' +
	            '<td>'+
	            	'<p><span class="label label-default" id="bunStatusLabel'+ data[i].id+'">...</span></p>'+
	            	'<button type="button" id="uninstBtn'+data[i].id+'" style="display:none;" class=" btn btn-primary" NEXTACTION="UNINSTALL" onClick="execInstallButton('+ data[i].id+',\''+data[i].uuid +'\', true);" >Uninstall</button>'+
            	 	'</td> ' + 
	            '<td nowrap>' + data[i].version + '<br><div id="installedVersion'+data[i].id+'"></div></td> ' + 
	            '<td>' + data[i].shortDescription + '</td> ' + 
	            '<td>' + data[i].longDescription + '</td> ' + 
	            '<td><img width="80px" src="' + data[i].iconsrc + '"</td> ' + 
	            '<td nowrap >' + ownerUsername + '<br>('+ownerOrganization+')</td> ' + 
	            '<td><div id="bunaction'+data[i].id+'" bunuuid="'+data[i].uuid+'">'+
	            	'<button type="button" id="execBtn'+data[i].id+'" class="btn btn-primary" onClick="execInstallButton('+ data[i].id+',\''+data[i].uuid +'\');" >waiting action...</button>';
	            	getAvailableAction( data[i].id, data[i].uuid)+
	            	
	            	'</div>'+
	            '</td> ' + 
	            '</tr>';
	        }
	
	        output+='</table>';
	        document.getElementById('placeholder').innerHTML=output;
	  });
	  
	  return false;
    }

    function getAvailableAction(vid, bunUUID){
    	
    	console.log("getAvailableAction for bunUUID="+bunUUID);
    	
    	 
    	$.ajax({
			  url: bakerURLatFORGEBoxInstall+'/services/api/client/ibuns/'+bunUUID,
			  type:"GET",
			  dataType:"json",
			  success: function(dataX){
				  //console.log("success result="+dataX);
				  //console.log("success result status ="+dataX.status);
				  //console.log("success result uuid ="+dataX.uuid);
				   
			      $("#installedVersion"+vid).append('<b>('+dataX.installedVersion+')</b>');
			      
				  if (dataX.status){
						setLabelStatus(vid, dataX.status);		  
						setButtonText(vid, dataX.status);		  
				  }else{
					setButtonText(vid, 'UNINSTALLED');
					setLabelStatus(vid, dataX.status);
				  }
			  },
			  statusCode: {
				  404: function() {
					  console.log("statusCode result= 404 + "+ $("#bunaction"+vid).attr("bunuuid") );
						setLabelStatus(vid, 'UNINSTALLED');
						setButtonText(vid, 'UNINSTALLED');
				  }
				}
			});
    	return "";
    };
    
    
    function setButtonText(vid, vstatus){
    	$("#execBtn"+vid).text(vstatus);
		$("#execBtn"+vid).removeClass().addClass('btn');
    	$("#execBtn"+vid).prop('disabled', false);
    	$("#uninstBtn"+vid).hide();
		
		
    	if (vstatus === 'INIT'){
    		$("#execBtn"+vid).text('INSTALL')
    		$("#execBtn"+vid).addClass('btn-info').attr('NEXTACTION', 'INSTALL');				
    	}
    	else if (vstatus === 'DOWNLOADING'){
    		$("#execBtn"+vid).text('INSTALL')
    		$("#execBtn"+vid).addClass('btn-info').attr('NEXTACTION', 'INSTALL');				
    	}
    	else if (vstatus === 'DOWNLOADED'){
    		$("#execBtn"+vid).text('INSTALL')
    		$("#execBtn"+vid).addClass('btn-info').attr('NEXTACTION', 'INSTALL');				
    	}
    	else if (vstatus === 'INSTALLING'){
    		$("#execBtn"+vid).text('INSTALL')
    		$("#execBtn"+vid).addClass('btn-info').attr('NEXTACTION', 'INSTALL');				
    	}
    	else if (vstatus === 'INSTALLED'){
    		$("#execBtn"+vid).text('START')
    		$("#execBtn"+vid).addClass('btn-success').attr('NEXTACTION', 'START');				
    	}
    	else if (vstatus === 'STARTING'){
    		$("#execBtn"+vid).text('START')
    		$("#execBtn"+vid).addClass('btn-success').attr('NEXTACTION', 'START');			
    	}
    	else if (vstatus === 'STARTED'){
    		$("#execBtn"+vid).text('STOP');
    		$("#execBtn"+vid).addClass('btn-primary').attr('NEXTACTION', 'STOP');
    	}
    	else if (vstatus === 'CONFIGURING'){
    		$("#execBtn"+vid).text('START')
    		$("#execBtn"+vid).addClass('btn-success').attr('NEXTACTION', 'START');		
    	}
    	else if (vstatus === 'STOPPING'){
    		$("#execBtn"+vid).text('STOP')
    		$("#execBtn"+vid).addClass('btn-primary').attr('NEXTACTION', 'STOP');		
    	}
    	else if (vstatus === 'STOPPED'){
    		$("#execBtn"+vid).text('START')
    		$("#execBtn"+vid).addClass('btn-success').attr('NEXTACTION', 'START');	
    		$("#uninstBtn"+vid).show();
    	}
    	else if (vstatus === 'UNINSTALLING'){
    		$("#execBtn"+vid).text('UNINSTALL')
    		$("#execBtn"+vid).addClass('btn-info').attr('NEXTACTION', 'UNINSTALL');				
    	}
    	else if (vstatus === 'UNINSTALLED'){
    		$("#execBtn"+vid).text('INSTALL');
    		$("#execBtn"+vid).addClass('btn-info').attr('NEXTACTION', 'INSTALL');		
    	}
    	else if (vstatus === 'FAILED'){
    		$("#execBtn"+vid).text('INSTALL')
    		$("#execBtn"+vid).addClass('btn-info').attr('NEXTACTION', 'INSTALL');	
    	}
    }
    
    function setLabelStatus(vid, vstatus){

		
		
		$("#bunStatusLabel"+vid).text(vstatus);
		$("#bunStatusLabel"+vid).removeClass().addClass('label');
		
    	if (vstatus === 'INIT')
    		$("#bunStatusLabel"+vid).addClass('label-warning');	
    	else if (vstatus === 'DOWNLOADING')
    		$("#bunStatusLabel"+vid).addClass('label-info');	
    	else if (vstatus === 'DOWNLOADED')
    		$("#bunStatusLabel"+vid).addClass('label-success');	
    	else if (vstatus === 'INSTALLING')
    		$("#bunStatusLabel"+vid).addClass('label-info');	
    	else if (vstatus === 'INSTALLED'){
    		$("#bunStatusLabel"+vid).addClass('label-success');	
    	}
    	else if (vstatus === 'STARTING')
    		$("#bunStatusLabel"+vid).addClass('label-info');	
    	else if (vstatus === 'STARTED'){
    		$("#bunStatusLabel"+vid).addClass('label-success');
			clearInterval(intervalIDTimerLabel);
			setButtonText(vid, vstatus);	
    	}
    	else if (vstatus === 'CONFIGURING')
    		$("#bunStatusLabel"+vid).addClass('label-info');	
    	else if (vstatus === 'STOPPING')
    		$("#bunStatusLabel"+vid).addClass('label-info');	
    	else if (vstatus === 'STOPPED'){
    		$("#bunStatusLabel"+vid).addClass('label-primary');	
			clearInterval(intervalIDTimerLabel);
			setButtonText(vid, vstatus);	
    	}
    	else if (vstatus === 'UNINSTALLING')
    		$("#bunStatusLabel"+vid).addClass('label-info');	
    	else if (vstatus === 'UNINSTALLED'){
    		$("#bunStatusLabel"+vid).text('NOT INSTALLED');
    		$("#bunStatusLabel"+vid).addClass('label-default');	
			clearInterval(intervalIDTimerLabel);
			setButtonText(vid, vstatus);	
    	}
    	else if (vstatus === 'FAILED'){
    		$("#bunStatusLabel"+vid).addClass('label-danger');	
			clearInterval(intervalIDTimerLabel);
			setButtonText(vid, vstatus);	
    	}
    	
    };
    
    function getAndupdateLabelStatus(vid, bunUUID){
		console.log('getAndupdateLabelStatus');
    	$.ajax({
			  url: bakerURLatFORGEBoxInstall+'/services/api/client/ibuns/'+bunUUID,
			  type:"GET",
			  dataType:"json",
			  success: function(dataX){
				  console.log("success result="+dataX);
				  console.log("success result status ="+dataX.status);
				  setLabelStatus(vid, dataX.status);
				  
			  }
    	});
    };
    
    
    var intervalIDTimerLabel;
    
    function execInstallButton(vid, bunUUID, uninstall){
    	var btn = $("#execBtn"+vid);
    	btn.text("Wait...");
    	btn.prop('disabled', true);
		btn.removeClass().addClass('btn');
		
    	if (uninstall){
    		btn = $("#uninstBtn"+vid);		
    	}
    	
		var nextAction = btn.attr('NEXTACTION')

		console.log("NEXT ACTION = "+ nextAction );
		intervalIDTimerLabel = setInterval( function(){getAndupdateLabelStatus(vid, bunUUID)} , 1000 );
    	
    	
		
    	$("#uninstBtn"+vid).hide();
		  
		console.log("execInstallButto nvid = "+vid+", bunUUID="+bunUUID );
		console.log( btn.attr("class") );
		 
		if (nextAction === 'INSTALL'){
			console.log('Will INSTALL');
			 var postData={
						uuid : bunUUID,
						repoUrl : repoMarketplaceURL+'/uuid/' + bunUUID
						};
				// Send the data using post
				$.ajax({
				  url: bakerURLatFORGEBoxInstall+'/services/api/client/ibuns/',
				  type: 'POST',
				  data:JSON.stringify(postData),
				  contentType:"application/json; charset=utf-8",
				  dataType:"json",
				  success: function(dataX){
					  console.log( dataX );
				  }
				});
		} else if (nextAction === 'STOP'){
			console.log('Will STOP');
			// Send the data using post
			$.ajax({
			  url: bakerURLatFORGEBoxInstall+'/services/api/client/ibuns/'+ bunUUID+'/stop',
			  type: 'PUT',
			  contentType:"application/json; charset=utf-8",
			  dataType:"json",
			  success: function(dataX){
				  console.log( dataX );
			  }
			});
		} else if (nextAction === 'START'){
			console.log('Will START');
			// Send the data using post
			$.ajax({
			  url: bakerURLatFORGEBoxInstall+'/services/api/client/ibuns/'+ bunUUID+'/start',
			  type: 'PUT',
			  contentType:"application/json; charset=utf-8",
			  dataType:"json",
			  success: function(dataX){
				  console.log( dataX );
			  }
			});
		} else if (nextAction === 'UNINSTALL'){
			console.log('Will UNINSTALL');
			// Send the data using post
			$.ajax({
			  url: bakerURLatFORGEBoxInstall+'/services/api/client/ibuns/'+ bunUUID,
			  type: 'DELETE',
			  contentType:"application/json; charset=utf-8",
			  dataType:"json",
			  success: function(dataX){
				  console.log( dataX );
			  }
			});
		}
    }
  
    </script>
    
</body>


</html>