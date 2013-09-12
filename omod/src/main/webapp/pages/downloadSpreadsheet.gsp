<%
    
    ui.decorateWith("appui", "standardEmrPage");
    ui.includeCss("uicommons", "emr/simpleFormUi.css", -200);
    
def sourceListMap=[]
	sourceList.each { sourcelist ->
		sourceListMap2 = [label: sourcelist.name, value: sourcelist.id]
		sourceListMap << sourceListMap2

	}

def classListMap=[]
	classList.each { classlist ->
		classListMap2 = [label: classlist.name, value: classlist.id]
		classListMap << classListMap2

	}
	
def mapTypeListMap=[]
	mapTypeList.each { maptypelist ->
		mapTypeListMap2 = [label: maptypelist.name, value: maptypelist.id]
		mapTypeListMap << mapTypeListMap2

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
        { label: "${ ui.message('conceptmanagementapps.downloadpage.title') }", link: "${ ui.pageLink('conceptmanagementapps', 'downloadSpreadsheet') }" }
    ];
 </script>
 <script type="text/javascript">

function validateForm() {  
	var conceptClassErrText = document.getElementById("showHideConceptClassValidationError");
	var sourceIdErrText = document.getElementById("showHideSourceIdValidationError");
	var error=0;
    conceptClassErrText.style.display = "none";
    sourceIdErrText.style.display = "none";
    var conceptClassFields = jQuery("input[name='conceptClass']").serializeArray(); 
    if (conceptClassFields.length == 0) 
    { 
    	conceptClassErrText.style.display = "block";
    	error=1;
    } 
    if(document.getElementsByName("sourceList")[0].value.length==0){
        sourceIdErrText.style.display = "block";
        error=1;
    }
    if(error==1){
    	return false;
    }
    else 
    { 
        document.downloadForm.submit();
    }
}
</script>
<link rel="stylesheet" href="/openmrs/ms/uiframework/resource/referenceapplication/styles/referenceapplication.css" type="text/css"/>

    <h2>
        ${ui.message("conceptmanagementapps.downloadpage.title")}
    </h2>

      
<form class="simple-form-ui" name="downloadForm" method="post">

            <div id="showHideSourceIdValidationError" style="display: none">
            	<p  style="color:red" class="required">(${ ui.message("emr.formValidation.messages.requiredField") })</p>
            </div>
            <fieldset>        
                ${ ui.includeFragment("uicommons", "field/dropDown", [
                label: ui.message("conceptmanagementapps.downloadpage.select.source.label"),
                formFieldName: "sourceList",
                options: sourceListMap,
                classes: ["required"],
                maximumSize: 1,
               	left: true]
               	)}
        	</fieldset>
        	
        	<fieldset>        
                ${ ui.includeFragment("uicommons", "field/dropDown", [
                label: ui.message("conceptmanagementapps.downloadpage.select.maptype.label"),
                formFieldName: "mapTypeList",
                options: mapTypeListMap,
                maximumSize: 1,
               	left: true]
               	)}
        	</fieldset>

            
       		<fieldset>
            	<div id="showHideConceptClassValidationError" style="display: none">
            	<p  style="color:red" class="required">(${ ui.message("emr.formValidation.messages.requiredField") })</p>
            	</div>
            	
				<div class="checkbox">
				<label class="required">${ui.message("conceptmanagementapps.downloadpage.class.label")}</label>
				</br>
						<% classList.each { classlist -> %> 
							<div class="checkbox" style="float: left;width: 30%; margin-right: 1%;">
								<p><input type="checkbox" value=${classlist.id} name="conceptClass" id="${classlist.id}"/>
								<label>${ui.message(classlist.name)}</label></p>
							</div>
						<%}%>
				</div>
	
			</fieldset>
            <div id="submit">
            <p style="display: inline"><input type="button" class="confirm" value="Download" onclick="javascript:validateForm();"/></p>
			</div>
	</form>
</div>