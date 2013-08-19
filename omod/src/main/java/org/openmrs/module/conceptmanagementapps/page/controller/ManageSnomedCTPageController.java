package org.openmrs.module.conceptmanagementapps.page.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.conceptmanagementapps.api.ConceptManagementAppsService;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ManageSnomedCTPageController {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	public void post(@RequestParam("snomedDirectoryLocation") String snomedFileDirectoryLocation, UiUtils ui,
	                 PageRequest pageRequest, HttpServletRequest request, PageModel model) {
		
		String inputType = request.getParameter("inputType");
		
		ConceptManagementAppsService conceptManagementAppsService = (ConceptManagementAppsService) Context
		        .getService(ConceptManagementAppsService.class);
		
		if (StringUtils.equalsIgnoreCase("addAncestors", inputType)) {
			
			conceptManagementAppsService.setCancelManageSnomedCTProcess(false);
			conceptManagementAppsService.addAncestorsToSnomedCTTerms(snomedFileDirectoryLocation);
			model.addAttribute("processRunning", "addAncestors");
			
		}
		
		if (StringUtils.equalsIgnoreCase("addParents", inputType)) {
			
			conceptManagementAppsService.setCancelManageSnomedCTProcess(false);
			conceptManagementAppsService.addParentsToSnomedCTTerms(snomedFileDirectoryLocation);
			model.addAttribute("processRunning", "addParents");
			
		}
		
		if (StringUtils.equalsIgnoreCase("addNames", inputType)) {
			
			conceptManagementAppsService.setCancelManageSnomedCTProcess(false);
			conceptManagementAppsService.addNamesToSnomedCTTerms(snomedFileDirectoryLocation);
			model.addAttribute("processRunning", "addNames");
			
		}
		if (StringUtils.equalsIgnoreCase("cancel", inputType)) {
			
			conceptManagementAppsService.setCancelManageSnomedCTProcess(true);
			model.addAttribute("processRunning", "none");
		}

	}
	
	public void get(UiSessionContext sessionContext, PageModel model) throws Exception {
		ConceptManagementAppsService conceptManagementAppsService = (ConceptManagementAppsService) Context
		        .getService(ConceptManagementAppsService.class);
		
		if (conceptManagementAppsService.getCancelManageSnomedCTProcess()) {
			model.addAttribute("processRunning", "none");
		} else {
			
			model.addAttribute("processRunning", "processRunning");
			
		}
	}
	
}
