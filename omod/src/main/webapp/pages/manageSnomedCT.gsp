

<% 
    ui.decorateWith("appui", "standardEmrPage");
    ui.includeCss("uicommons", "emr/simpleFormUi.css", -200);
            
    ui.includeJavascript("conceptmanagementapps", "jquery.dataTables.min.js");
    ui.includeJavascript("conceptmanagementapps", "fourButtonPagination.js");
    
    ui.includeCss("conceptmanagementapps", "../css/dataTables.css");

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
        { label: "${ ui.message("conceptmanagementapps.managesnomedct.title") }", link: "${ ui.pageLink("conceptmanagementapps", "manageSnomedCT") }" }
    ];
 </script>
 
 <script type="text/javascript">
 function showHideValues(){
 	var theProcessRunning="<%=processRunning.toString()%>";
	if(theProcessRunning.localeCompare("none")){
	
		showHideAddNames.style.display = "none";
		showHideAddParents.style.display = "none";
		showHideAddAncestors.style.display = "none";
		showHideCancelAdd.style.display = "block";
	}
	
	else{
	
		showHideAddNames.style.display = "block";
		showHideAddParents.style.display = "block";
		showHideAddAncestors.style.display = "block";
		showHideCancelAdd.style.display = "none";
	
	}
}
function validateForm(inputType) {
	if(inputType.value=="Cancel"){
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
    if(this.name=="showHideCancelAdd"){
    	cancelAddNames.style.display = "block";
    }
    if(error==1){
    	return false;
    }
    else 
    { 
        document.manageSnomedCT.submit();
        inputType.value = "Cancel";
        document.getElementById('inputTypeId').value = inputType.value;
    }
}
</script>

 <h2>
        ${ui.message("conceptmanagementapps.managesnomedct.title")}
 </h2>

<form name="manageSnomedCT" class="simple-form-ui" method="post">
           

			           


                <div id="showHideDirectoryLocationValidationError" style="display: none">
            		<p  style="color:red" class="required">(${ ui.message("emr.formValidation.messages.requiredField") })</p>
            	</div>            
				<p>
					<label name="snomedDirectoryLocationId">${ui.message("conceptmanagementapps.managesnomedct.snomeddirectorylocation.label")}</label>
					<input  type="text" name="snomedDirectoryLocation" id="snomedDirectoryLocationId" size="35"/>
				</p>

				<div id="showHideCancelAdd" style="display: block">
					<p>
					<input type="button" name="cancelAddAncestors" id="cancelAddAncestorsId" value="Cancel" onclick="javascript:validateForm(this);"/>
					</p>
				</div>
			           
            <fieldset>
                <legend>
       	 			${ui.message("conceptmanagementapps.managesnomedct.addnames.title")}
     			</legend>
     			<div id="showHideAddNames" style="display: none">
				<p class="left">
				<input type="button" name="addNames" id="addNamesId" value="Start Task" onclick="javascript:validateForm(this);"/>
				</p>
				</div>
			</fieldset>
			
            <fieldset>
                <legend>
       	 			${ui.message("conceptmanagementapps.managesnomedct.addparents.title")}
     			</legend>
     			<div id="showHideAddParents"  style="display: none">
				<p>
				<input type="button" name="addParents" id="addParentsId" value="Start Task" onclick="javascript:validateForm(this);"/>
				</p>
				</div>
			</fieldset>
			
			 <fieldset>
                <legend>
       	 			${ui.message("conceptmanagementapps.managesnomedct.addancestors.title")}
     			</legend>
     			<div id="showHideAddAncestors" style="display: none">
				<p>
				<input type="button" name="addAncestors" id="addAncestorsId" value="Start Task" onclick="javascript:validateForm(this);"/>
				</p>
				</div>
				
			</fieldset>
			
			<input type="hidden" name="inputType" id="inputTypeId"/>

</form>

<script type="text/javascript">
window.onload=showHideValues();

 </script>