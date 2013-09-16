package org.openmrs.module.conceptmanagementapps.fragment.controller;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.conceptmanagementapps.api.ConceptManagementAppsService;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

public class BrowseTableOfReferenceTermsFragmentController {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	public String retrieveTableData(@RequestParam(value = "sSortDir_0", required = false) String sSortDirection,
	                                @RequestParam(value = "iSortCol_0", required = false) Integer iSortColumnIndex,
	                                @RequestParam(value = "sourceId", required = false) Integer sourceId,
	                                @RequestParam(value = "sEcho", required = false) int sEcho,
	                                @RequestParam(value = "sSearch", required = false) String sSearch,
	                                @RequestParam(value = "iDisplayLength", required = false) Integer iDisplayLength,
	                                @RequestParam(value = "iDisplayStart", required = false) Integer iDisplayStart,
	                                @RequestParam(value = "iTotal", required = false) Integer iTotal) throws Exception {
		
		int sortColumnIndex;
		if (iSortColumnIndex != null) {
			
			sortColumnIndex = iSortColumnIndex;
			
		} else {
			
			sortColumnIndex = 0;
		}
		
		String sortColumn = "code";
		if (sortColumnIndex == 0)
			sortColumn = "conceptSource";
		if (sortColumnIndex == 1)
			sortColumn = "code";
		if (sortColumnIndex == 2)
			sortColumn = "name";
		if (sortColumnIndex == 3)
			sortColumn = "description";
		
		int sortDirection;
		if (StringUtils.isEmpty(sSortDirection) || StringUtils.isBlank(sSortDirection)) {
			
			sortDirection = 1;
			
		} else {
			
			sortDirection = sSortDirection.equals("asc") ? -1 : 1;
		}
		
		Integer iTotalRecords = 0;
		Integer numOfRefTermsToRetrieve = iDisplayLength;
		Integer startIndex = iDisplayStart;
		List<ConceptReferenceTerm> referenceTermList;
		
		ConceptService conceptService = (ConceptService) Context.getService(ConceptService.class);
		ConceptManagementAppsService conceptManagementAppsService = (ConceptManagementAppsService) Context
		        .getService(ConceptManagementAppsService.class);
		
		ConceptSource specifiedSource = null;
		if (conceptService.getConceptSource(sourceId) != null) {
			specifiedSource = conceptService.getConceptSource(sourceId);
		}
		
		if (StringUtils.isNotEmpty(sSearch) && StringUtils.isNotBlank(sSearch)) {
			if (sourceId == 0) {
				
				iTotalRecords = conceptManagementAppsService.getCountOfConceptReferenceTermsWithQuery(sSearch, null, false);
				referenceTermList = conceptManagementAppsService.getConceptReferenceTermsWithQuery(sSearch, null,
				    startIndex, numOfRefTermsToRetrieve, false, sortColumn, sortDirection);
				
			} else {
				iTotalRecords = conceptManagementAppsService.getCountOfConceptReferenceTermsWithQuery(sSearch, Context
				        .getConceptService().getConceptSource(sourceId), false);
				referenceTermList = conceptManagementAppsService.getConceptReferenceTermsWithQuery(sSearch, Context
				        .getConceptService().getConceptSource(sourceId), startIndex, numOfRefTermsToRetrieve, false,
				    sortColumn, sortDirection);
			}
			
		} else {
			
			if (sourceId == 0) {
				iTotalRecords = conceptManagementAppsService
				        .getCountOfConceptReferenceTermsWithSpecifiedSourceIfIncluded(null);
				referenceTermList = conceptManagementAppsService.getConceptReferenceTermsWithSpecifiedSourceIfIncluded(null,
				    startIndex, numOfRefTermsToRetrieve, sortColumn, sortDirection);
				
			} else {
				iTotalRecords = conceptManagementAppsService
				        .getCountOfConceptReferenceTermsWithSpecifiedSourceIfIncluded(specifiedSource);
				referenceTermList = conceptManagementAppsService.getConceptReferenceTermsWithSpecifiedSourceIfIncluded(
				    specifiedSource, startIndex, numOfRefTermsToRetrieve, sortColumn, sortDirection);
				
			}
			
		}
		
		if (referenceTermList.size() < iDisplayLength) {
			
			referenceTermList = referenceTermList.subList(0, referenceTermList.size());
			
		} else {
			
			referenceTermList = referenceTermList.subList(0, iDisplayLength);
		}
		
		String startDataString = "{  \"sEcho\": " + sEcho + " ," + "   \"iTotalRecords\": " + iTotalRecords + ","
		        + "   \"iTotalDisplayRecords\": " + iTotalRecords + "," + "   \"aaData\": [";
		
		String endDataString = "   ]" + "}";
		
		String dataString = "";
		
		int i = 0;
		for (ConceptReferenceTerm crt : referenceTermList) {
			
			String name = " ";
			String description = " ";
			String source = " ";
			String code = " ";
			
			if (StringUtils.isNotEmpty(crt.getConceptSource().getName())
			        || StringUtils.isNotBlank(crt.getConceptSource().getName())) {
				
				source = crt.getConceptSource().getName();
			}
			
			if (StringUtils.isNotEmpty(crt.getCode()) || StringUtils.isNotBlank(crt.getCode())) {
				
				code = crt.getCode();
			}
			
			if (StringUtils.isNotEmpty(crt.getName()) || StringUtils.isNotBlank(crt.getName())) {
				
				name = crt.getName();
			}
			
			if (StringUtils.isNotEmpty(crt.getDescription()) || StringUtils.isNotBlank(crt.getDescription())) {
				
				description = crt.getDescription();
			}
			
			dataString += "[" + "           \"" + source + "\",\"" + code + "\",\"" + name + "\",\"" + description + "\"";
			
			if (i < referenceTermList.size() - 1) {
				
				dataString += "],";
				
			} else {
				
				dataString += "]";
			}
			i++;
		}
		return startDataString + dataString + endDataString;
	}
	
	public void get(UiSessionContext sessionContext, PageModel model) throws Exception {
		
		model.addAttribute("sourceId", 0);
		
	}
	
}
