<%
	
    ui.includeJavascript("uicommons", "angular.min.js");
    ui.includeJavascript("conceptmanagementapps", "chooseConceptByHierarchy-angular.js");
    ui.includeJavascript("conceptmanagementapps", "angular-resource.min.js");
    ui.includeCss("conceptmanagementapps", "../css/conceptmanagement.css");
    
    
%>

<div id="searchConceptApp">

	<div lang="en" ng-controller="MainCtrl"> 
		
		<fieldset>
		<legend>Concept Query</legend>
 			<div class="concept-search">
    			<ng-autocomplete  remote-data="Wrapper.AutoComplete(request, response)" 
            		min-input="2" restrict="true" 
            		selected-concept="selectedConcept" 
            		placeholder="${ ui.message("conceptmanagementapps.chooseconceptbyhierarchy.placeholder") }" ng-model="message" >
    			</ng-autocomplete><br/>
 			</div> 
		</fieldset> 
			
	</div>

	<div lang="en" ng-controller="HierarchyCtrl"> 
 		
 		<fieldset>
			<legend>Snomed CT Reference Term Hierarchy</legend>
    		<ng-hierarchy remote-data="hierarchyServiceWrapper.BuildHierarchyView(request, response)" ng-model="message" >
    		selected-refTerm="selectedRefTerm"
    		</ng-hierarchy><br/>
    
		</fieldset>
	</div>
</div>



<script>
     angular.bootstrap('#searchConceptApp', ['searchConceptApp']);
</script>
 

