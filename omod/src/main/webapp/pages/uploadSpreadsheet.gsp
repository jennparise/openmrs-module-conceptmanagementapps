<%
    
    ui.decorateWith("appui", "standardEmrPage");
    ui.includeJavascript("uicommons", "navigator/validators.js", Integer.MAX_VALUE - 19);
    ui.includeJavascript("uicommons", "navigator/navigator.js", Integer.MAX_VALUE - 20);
    ui.includeJavascript("uicommons", "navigator/navigatorHandlers.js", Integer.MAX_VALUE - 21);
    ui.includeJavascript("uicommons", "navigator/navigatorModels.js", Integer.MAX_VALUE - 21);
    ui.includeJavascript("uicommons", "navigator/exitHandlers.js", Integer.MAX_VALUE - 22);
    ui.includeCss("uicommons", "emr/simpleFormUi.css", -200);
    
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
        { label: "${ ui.message("conceptmanagementapps.uploadpage.title") }", link: "${ ui.pageLink("conceptmanagementapps", "uploadSpreadsheet") }" }
    ];
 </script>
 <script type="text/javascript">

function validateForm() {  
	var fileToUploadErrText = document.getElementById("showHideFileUploadValidationError");
	var error=0;
    fileToUploadErrText.style.display = "none";
    if(document.getElementsByName("spreadsheet")[0].value.length==0){
        fileToUploadErrText.style.display = "block";
        error=1;
    }
    if(error==1){
    	return false;
    }
    else 
    { 
        document.uploadform.submit();
    }
}
</script>

<div>
	<p>
		<p style="color:red">${errorMessage}</p>
	</p>
    <h2>
        ${ui.message("conceptmanagementapps.uploadpage.title")}
    </h2>
    <h3>
        ${ui.message("conceptmanagementapps.uploadpage.subtitle")}
    </h3>

<form method="post" name="uploadform" encType="multipart/form-data">
    	 <div id="showHideFileUploadValidationError" style="display: none">
     		<p  style="color:red" class="required">${ ui.message("emr.formValidation.messages.requiredField") }</p>
     	</div>
     	<b>${ui.message("conceptmanagementapps.uploadpage.upload.file.label")} <input type="file" name="spreadsheet"/></b>
    	<div id="submit">
        	<p style="display: inline"><input type="button" class="confirm" value="Upload" onclick="javascript:validateForm();"/></p>
        </div>
</form>
</div>