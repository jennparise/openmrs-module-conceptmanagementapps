

<% 
    ui.decorateWith("appui", "standardEmrPage");
    ui.includeCss("uicommons", "emr/simpleFormUi.css", -200);
            
    ui.includeJavascript("conceptmanagementapps", "jquery.dataTables.min.js");
    ui.includeJavascript("conceptmanagementapps", "fourButtonPagination.js");
    
    ui.includeCss("conceptmanagementapps", "../css/dataTables.css");
    
    def sourceListMap=[]
    sourceListMap2 = [label: "Browse All", value: 0]
    sourceListMap << sourceListMap2
	sourceList.each { sourcelist ->
		sourceListMap2 = [label: sourcelist.name, value: sourcelist.id]
		sourceListMap << sourceListMap2

	}
	def sourceid = sourceId;

%>

${ ui.includeFragment("uicommons", "validationMessages")}
<script type="text/javascript">
    jQuery(function() {
        KeyboardController();
    }
</script>

 <script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("conceptmanagementapps.homepage.title") }", link: "${ ui.pageLink("conceptmanagementapps", "conceptManagementAppsMain") }" },
        { label: "${ ui.message("conceptmanagementapps.managesnomedct.title") }", link: "${ ui.pageLink("conceptmanagementapps", "manageSnomedCT") }" }
    ];
 </script>
 
 

<h2>
	${ui.message("conceptmanagementapps.managesnomedct.title")}
</h2>


${ui.message("conceptmanagementapps.managesnomedct.instructions")}

<form name="manageSnomedCT" class="simple-form-ui" method="post"> 
	        	
<fieldset>

    <legend> Configuration </legend>
	<p style="color:red">${manageSnomedCTError}</p>
	
    <p>Path to the "Terminology" folder:  
      
	<div id="showHideDirectoryLocationValidationError" style="display: none">
		<p  style="color:red" class="required">(${ ui.message("emr.formValidation.messages.requiredField") })</p>
	</div>   
	
    	<input  type="text" name="snomedDirectoryLocation" id="snomedDirectoryLocationId" size="35" value="<%= dirLocation.toString() %>"/>
    </p>
    
    <p>
		${ ui.includeFragment("uicommons", "field/dropDown", [
			label: ui.message("conceptmanagementapps.managesnomedct.source.label"),
			formFieldName: "sourceList",
			options: sourceListMap,
			maximumSize: 1,
			left: true,
			initialValue: sourceid
			]
			)}
	</p>
	</br></br></br></br>
    <p> 			
    	<input type="button" name="saveConfiguration" id="saveConfigurationId" value="Save" onclick="javascript:validateForm(this);"/>
	</p>
    
</fieldset>
				
	<fieldset id="importSnomedCtContent" style="display: none">	
	
	<legend>Import SNOMED CT Content</legend>

		<p>${ui.message("conceptmanagementapps.managesnomedct.reload.label")}</p>
	
		<div id="processStatus" style="background:#D3D3D3;display:none">
			<p>
				<%= processStatus.toString() %>
			</p>
			<p>
				<%= processPercentComplete.toString() %>
			</p> 
		</div>
		
		<div style="padding:1px;">

	  		<label>
				${ui.message("conceptmanagementapps.managesnomedct.addnames.title")}
			</label>  
   
			<p id="showHideAddNames" style="display: none">
				<input type="button" name="addSnomedCTNames" id="addSnomedCTNamesId" value="Start Task" onclick="javascript:validateForm(this);"/>
			</p>
			
			<p id="showHideCancelAddNames" style="display: block">
				<input type="button" name="cancelAddNames" id="cancelAddNamesId" value="Cancel" onclick="javascript:validateForm(this);"/>
			</p>
			
		</div>
		
		
		<div style="padding:1px;">

	  		<label>
				${ui.message("conceptmanagementapps.managesnomedct.addancestors.title")}
			</label>  

			<p id="showHideAddAncestors" style="display: none">
				<input type="button" name="addSnomedCTAncestors" id="addSnomedCTAncestorsId" value="Start Task" onclick="javascript:validateForm(this);"/>
			</p>
			
			<p id="showHideCancelAddAncestors" style="display: block">
				<input type="button" name="cancelAddAncestors" id="cancelAddAncestorsId" value="Cancel" onclick="javascript:validateForm(this);"/>
			</p>
			
		</div>	
		
		
		<div style="padding:1px;">

	  		<label>
				${ui.message("conceptmanagementapps.managesnomedct.addrelationships.title")}
			</label>  

			<p id="showHideAddRelationships"  style="display: none">
				<input type="button" name="addSnomedCTRelationships" id="addSnomedCTRelationshipsId" value="Start Task" onclick="javascript:validateForm(this);"/>
			</p>
		
			<p id="showHideCancelAddRelationships" style="display: block">	
				<input type="button" name="cancelAddRelationships" id="cancelAddRelationshipsId" value="Cancel" onclick="javascript:validateForm(this);"/>
			</p>
			
		</div>	
		
	</fieldset>
		
	<input type="hidden" name="inputType" id="inputTypeId"/>

</form>
<script type="text/javascript">
 function showHideValues(){

	resetButtonsAndFields();
	
	var manageSnomedCTError = "<%=manageSnomedCTError.toString()%>";
 	var configSaved = "<%=configSaved.toString()%>";
 
 	if(configSaved === "configSaved" && manageSnomedCTError.length < 1){
 		document.getElementById('importSnomedCtContent').style.display = "block";
 	}
 	
 	var theProcessRunning = "<%=processRunning.toString()%>";
	if(theProcessRunning === "addSnomedCTNames"){
	
		document.getElementById('addSnomedCTRelationshipsId').disabled = true;
		document.getElementById('addSnomedCTAncestorsId').disabled = true;
		document.getElementById('showHideAddNames').style.display = "none";
		document.getElementById('showHideCancelAddNames').style.display = "block";
		document.getElementById('processStatus').style.display = "block";
		document.getElementById('saveConfigurationId').disabled = true;
		setTimeout(function(){window.location.reload(1);}, 10000);	
	}
	if(theProcessRunning === "addSnomedCTRelationships"){
	
		document.getElementById('addSnomedCTNamesId').disabled = true;
		document.getElementById('addSnomedCTAncestorsId').disabled = true;
		document.getElementById('showHideAddRelationships').style.display = "none";
		document.getElementById('showHideCancelAddRelationships').style.display = "block";
		document.getElementById('processStatus').style.display = "block";
		document.getElementById('saveConfigurationId').disabled = true;
		setTimeout(function(){window.location.reload(1);}, 10000);
	}
	if(theProcessRunning === "addSnomedCTAncestors"){
	
		document.getElementById('addSnomedCTNamesId').disabled = true;
		document.getElementById('addSnomedCTRelationshipsId').disabled = true;
		document.getElementById('showHideAddAncestors').style.display = "none";
		document.getElementById('showHideCancelAddAncestors').style.display = "block";
		document.getElementById('processStatus').style.display = "block";
		document.getElementById('saveConfigurationId').disabled = true;
		setTimeout(function(){window.location.reload(1);}, 10000);
	}
	
	function resetButtonsAndFields(){

		document.getElementById('addSnomedCTAncestorsId').disabled = false;
		document.getElementById('addSnomedCTNamesId').disabled = false;
		document.getElementById('addSnomedCTRelationshipsId').disabled = false;
		document.getElementById('showHideAddNames').style.display = "block";
		document.getElementById('showHideAddRelationships').style.display = "block";
		document.getElementById('showHideAddAncestors').style.display = "block";
		document.getElementById('showHideCancelAddNames').style.display = "none";
		document.getElementById('showHideCancelAddRelationships').style.display = "none";
		document.getElementById('showHideCancelAddAncestors').style.display = "none";
		document.getElementById('saveConfigurationId').disabled = false;
		document.getElementById('processStatus').style.display = "none";

		
	}
}


function validateForm(inputType) {

	if(inputType.value == "Cancel"){
		document.getElementById('inputTypeId').value = inputType.value;
	}
	else{
		document.getElementById('inputTypeId').value = inputType.name;
		
	}
	
	var directoryLocationErrText = document.getElementById("showHideDirectoryLocationValidationError");
	var error=0;
	
    directoryLocationErrText.style.display = "none";
    
    if (document.getElementById('snomedDirectoryLocationId').value == null || document.getElementById('snomedDirectoryLocationId').value.length == 0) 
    { 
    	directoryLocationErrText.style.display = "block";
    	error=1;
    }
    if(inputType.name == "showHideCancelAdd"){
    	cancelAddNames.style.display = "block";
    }
    if(error == 1){
    	return false;
    }
    else 
    { 
    	document.manageSnomedCT.submit();

    	if(inputType.name === "addSnomedCTRelationships" || inputType.name === "addSnomedCTAncestors" || inputType.name === "addSnomedCTNames"){
    		setTimeout(function(){window.location.replace("${ ui.pageLink("conceptmanagementapps", "manageSnomedCT") }");}, 2000);
    	}
    	
    }
}

	window.onload=showHideValues();
 
</script>