<% 
    ui.decorateWith("appui", "standardEmrPage");
    ui.includeCss("uicommons", "emr/simpleFormUi.css", -200);
%>


${ ui.includeFragment("uicommons", "validationMessages")}


 <script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("conceptmanagementapps.homepage.title") }", link: "${ ui.pageLink("conceptmanagementapps", "conceptManagementAppsMain") }" },
        { label: "${ ui.message("conceptmanagementapps.chooseconceptgyhierarchy.title") }", link: "${ ui.pageLink("conceptmanagementapps", "chooseconceptgyhierarchy") }" }
    ];
 </script>


 <h2>
        ${ui.message("conceptmanagementapps.chooseconceptgyhierarchy.title")}
 </h2>


${ ui.includeFragment("conceptmanagementapps", "chooseConceptByHierarchy")}
