

<% 
    ui.decorateWith("appui", "standardEmrPage");
    ui.includeCss("uicommons", "emr/simpleFormUi.css", -200);
            
    ui.includeJavascript("conceptmanagementapps", "jquery.dataTables.min.js");
    ui.includeJavascript("conceptmanagementapps", "fourButtonPagination.js");
    
    ui.includeCss("conceptmanagementapps", "../css/dataTables.css");

    def sourceListMap=[]
	sourceList.each { sourcelist ->
		sourceListMap2 = [label: sourcelist.name, value: sourcelist.id]
		sourceListMap << sourceListMap2

	}

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
        { label: "${ ui.message("conceptmanagementapps.browsereferenceterms.title") }", link: "${ ui.pageLink("conceptmanagementapps", "browseReferenceTermsTable") }" },
        { label: "${ ui.message("conceptmanagementapps.addterm.title") }", link: "${ ui.pageLink("conceptmanagementapps", "addTerm") }" }
    ];
 </script>
 <script type="text/javascript">

function validateForm() {
	var conceptSourceErrText = document.getElementById("showHideConceptSourceValidationError");
	var refTermCodeErrText = document.getElementById("showHideRefTermCodeValidationError");
	var error=0;
    conceptSourceErrText.style.display = "none";
    refTermCodeErrText.style.display = "none";
    if(document.getElementsByName("sourceList")[0].value.length==0){
        conceptSourceErrText.style.display = "block";
        error=1;
    }
    if (document.getElementById('refTermCodeId').value == null || document.getElementById('refTermCodeId').value.length == 0) 
    { 
    	refTermCodeErrText.style.display = "block";
    	error=1;
    }
    if(error==1){
    	return false;
    }
    else 
    { 
        document.addTerm.submit();
    }
}
</script>

 <h2>
        ${ui.message("conceptmanagementapps.addterm.title")}
 </h2>

<form name="addTerm" class="simple-form-ui" method="post">
           
           
                <div id="showHideConceptSourceValidationError" style="display: none">
            		<p  style="color:red" class="required">(${ ui.message("emr.formValidation.messages.requiredField") })</p>
            	</div>
				${ ui.includeFragment("uicommons", "field/dropDown", [
					label: ui.message("conceptmanagementapps.browsereferenceterms.select.source.label"),
					formFieldName: "sourceList",
					options: sourceListMap,
                	classes: ["required"],					
					maximumSize: 1,
					left: true,
					initialValue: 0
					]
				)}
				</br></br></br></br>
                <div id="showHideRefTermCodeValidationError" style="display: none">
            		<p  style="color:red" class="required">(${ ui.message("emr.formValidation.messages.requiredField") })</p>
            	</div>            
				<p>
				<label name="refTermCodeId">${ui.message("conceptmanagementapps.addterm.code.label")}</label>
				<input  type="text" name="refTermCode" id="refTermCodeId"/>
				</p>

				<p>
				<label name="refTermNameId">${ui.message("conceptmanagementapps.addterm.name.label")}</label>
				<input type="text" name="refTermName" id="refTermNameId"/>
				</p>

				<p>
				<label name="refTermDescriptionId">${ui.message("conceptmanagementapps.addterm.description.label")}</label>
				<textarea name="refTermDescription" id="refTermDescriptionId"></textarea>
				</p>

			           
            <div id="submit">
            <p style="display: inline"><input type="button" class="confirm" value="Save Reference Term" onclick="javascript:validateForm();"/></p>
			</div>

</form>