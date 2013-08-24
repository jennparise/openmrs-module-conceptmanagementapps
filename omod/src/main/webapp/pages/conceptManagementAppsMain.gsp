<%
    ui.decorateWith("appui", "standardEmrPage")

    def htmlSafeId = { extension ->
        "${ extension.id.replace(".", "-") }-${ extension.id.replace(".", "-") }-extension"
    }
%>
<script type="text/javascript">
    jQuery(function() {
        KeyboardController();
    }
</script>


 <script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("Home") }", link: "${ ui.pageLink("referenceapplication", "home") }" }
    ];
 </script>
<div id="home-container">

	<h1>${ui.message("conceptmanagementapps.homepage.title")}</h1>



    <div id="apps">
    <fieldset>
    <legend>${ ui.message('conceptmanagementapps.mainpage.referenceTerms.title') }</legend>
        <% extensions.each { ext -> %>
        <% if (ext.id.contains('ReferenceTerms')) {%>
			
            <a id="${ htmlSafeId(ext) }" href="/${ contextPath }/${ ext.url }" class="button app big">
                <% if (ext.icon) { %>
                   <i class="${ ext.icon }"></i>
                <% } %>
                ${ ui.message(ext.label) }
            </a>
 
       <% } %>
       <% } %>
 	</fieldset>
 	<fieldset>
 	<legend>${ ui.message('conceptmanagementapps.mainpage.missingconceptmappings.title') }</legend>
       <% extensions.each { ext -> %>
       <% if (ext.id.contains('conceptsMissingMappings')) {%>
            <a id="${ htmlSafeId(ext) }" href="/${ contextPath }/${ ext.url }" class="button app big">
                <% if (ext.icon) { %>
                   <i class="${ ext.icon }"></i>
                <% } %>
                ${ ui.message(ext.label) }
            </a>
       <% } %>
       <% } %>
       </fieldset>
       <fieldset>
       <legend>${ ui.message('conceptmanagementapps.mainpage.importsnomeddistribution.title') }</legend>
       <% extensions.each { ext -> %>
       <% if (ext.id.contains('importSnomedDistribution')) {%>
            <a id="${ htmlSafeId(ext) }" href="/${ contextPath }/${ ext.url }" class="button app big">
                <% if (ext.icon) { %>
                   <i class="${ ext.icon }"></i>
                <% } %>
                ${ ui.message(ext.label) }
            </a>
       <% } %>
        <% } %>
        </fieldset>
    </div>

</div>