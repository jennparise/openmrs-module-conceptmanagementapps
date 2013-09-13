<%
    def sourceListMap=[]
    sourceListMap2 = [label: "Browse All", value: 0]
    sourceListMap << sourceListMap2
	sourceList.each { sourcelist ->
		sourceListMap2 = [label: sourcelist.name, value: sourcelist.id]
		sourceListMap << sourceListMap2

	}

%>

 <script type="text/javascript">
 function getSourceId() {
    var sourceId=document.getElementsByName("sourceList")[0].value;
    if(sourceId.length<1){
        document.getElementsByName("sourceList")[0].value=0;
    	sourceId=0;
    }
    return sourceId;
 }
       
 
</script>

<script type="text/javascript"> 
jQuery(function() {
jQuery('[name=sourceList]').change(function () {
	var actionUrl='${ ui.actionLink("conceptmanagementapps", "browseTableOfReferenceTerms", "retrieveTableData") }';
	jQuery('#browser').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" id="browserTable"></table>' );
    jQuery('#browserTable').dataTable( {
    	"iDisplayLength": 25,
    	"bProcessing": true,
       	"bServerSide": true,
    	"sPaginationType": "four_button",
    	"sAjaxSource": actionUrl,
    	"aoColumns": [
        	{ "sTitle": "source"},
            { "sTitle": "code" },
            { "sTitle": "name" },
           	{ "sTitle": "description"}
        	],
        "fnServerParams": function ( aoData ) {
      		aoData.push({ "name": "sourceId", "value": getSourceId()});
    		},
		"fnServerData": function ( sSource, aoData, fnCallback ) {
			jQuery.getJSON( sSource, aoData, function (json) {
            	var parsedJSON = jQuery.parseJSON(json);
            	fnCallback(parsedJSON)
                		
            });
         }
	});        
});   		   

}); 
</script>
<script type="text/javascript">
jQuery(function() {

	var actionUrl='${ ui.actionLink("conceptmanagementapps", "browseTableOfReferenceTerms", "retrieveTableData") }';
	jQuery('#browser').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" id="browserTable"></table>' );
    jQuery('#browserTable').dataTable( {
    	"iDisplayLength": 25,
    	"bProcessing": true,
       	"bServerSide": true,
    	"sPaginationType": "four_button",
    	"sAjaxSource": actionUrl,
    	"aoColumns": [
        	{ "sTitle": "source"},
            { "sTitle": "code" },
            { "sTitle": "name" },
           	{ "sTitle": "description"}
        	],

        "fnServerParams": function ( aoData ) {
      		aoData.push({ "name": "sourceId", "value": getSourceId()});
    		},
		"fnServerData": function ( sSource, aoData, fnCallback ) {
			jQuery.getJSON( sSource, aoData, function (json) {
            	var parsedJSON = jQuery.parseJSON(json);
            	fnCallback(parsedJSON)
                		
            });
         }
               
	});  
});
</script>

<fieldset>        
	${ ui.includeFragment("uicommons", "field/dropDown", [
		label: ui.message("conceptmanagementapps.browsereferenceterms.select.source.label"),
		formFieldName: "sourceList",
		options: sourceListMap,
		maximumSize: 1,
		left: true,
		initialValue: 0
		]
	)}
</fieldset>
</p>


