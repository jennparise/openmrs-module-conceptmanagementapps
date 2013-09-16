package org.openmrs.module.conceptmanagementapps.page.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptSource;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.conceptmanagementapps.ConceptManagementAppsConstants;
import org.openmrs.module.conceptmanagementapps.ConceptManagementAppsProperties;
import org.openmrs.module.conceptmanagementapps.api.ConceptManagementAppsService;
import org.openmrs.module.conceptmanagementapps.api.ManageSnomedCTProcess;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ManageSnomedCTPageController {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	public void post(@RequestParam("snomedDirectoryLocation") String snomedFileDirectoryLocation,
	                 @RequestParam(value = "sourceList", required = false) String[] sourceId, UiUtils ui,
	                 PageRequest pageRequest, HttpServletRequest request, PageModel model) {
		
		ConceptService conceptService = Context.getConceptService();
		
		ConceptSource snomedSource = conceptService.getConceptSource(Integer.parseInt(sourceId[0]));
		
		String inputType = request.getParameter("inputType");
		
		ConceptManagementAppsService conceptManagementAppsService = (ConceptManagementAppsService) Context
		        .getService(ConceptManagementAppsService.class);
		
		if (StringUtils.equalsIgnoreCase("reload", inputType)) {
			
			if (conceptManagementAppsService.getManageSnomedCTProcessCancelled()) {
				
				setValuesForNoProcessRunning(model);
				
			} else {
				
				setValuesForCurrentProcess(model, conceptManagementAppsService);
			}
			
		} else if (StringUtils.equalsIgnoreCase("cancel", inputType)) {
			
			conceptManagementAppsService.setManageSnomedCTProcessCancelled(true);
			
			setValuesForNoProcessRunning(model);
			
		} else {
			
			conceptManagementAppsService.setManageSnomedCTProcessCancelled(false);
			conceptManagementAppsService.startManageSnomedCTProcess(inputType, snomedFileDirectoryLocation, snomedSource);
			conceptManagementAppsService.setManageSnomedCTProcessCancelled(true);
			
			setValuesForNoProcessRunning(model);
			
		}
		
	}
	
	public void get(UiSessionContext sessionContext, PageModel model) throws Exception {
		
		ConceptManagementAppsService conceptManagementAppsService = (ConceptManagementAppsService) Context
		        .getService(ConceptManagementAppsService.class);
		
		List<ConceptSource> sourceList = Context.getConceptService().getAllConceptSources();
		int sourceId = 0;
		
		ConceptManagementAppsProperties cmap = new ConceptManagementAppsProperties();
		String snomedSourceUuid = cmap
		        .getSnomedCTConceptSourceUuidGlobalProperty(ConceptManagementAppsConstants.SNOMED_CT_CONCEPT_SOURCE_UUID_GP);
		for (ConceptSource source : sourceList) {
			
			if (StringUtils.equals(source.getUuid(), snomedSourceUuid)) {
				sourceId = source.getId();
			}
		}
		model.addAttribute("sourceList", sourceList);
		model.addAttribute("sourceId", sourceId);
		
		if (conceptManagementAppsService.getManageSnomedCTProcessCancelled()) {
			
			setValuesForNoProcessRunning(model);
			
		} else {
			
			if (conceptManagementAppsService.getCurrentSnomedCTProcess() != null) {
				
				setValuesForCurrentProcess(model, conceptManagementAppsService);
				
			} else {
				
				setValuesForNoProcessRunning(model);
				
			}
		}
	}
	
	private void setValuesForNoProcessRunning(PageModel model) {
		
		model.addAttribute("processRunning", "none");
		model.addAttribute("processStatus", "");
		model.addAttribute("dirLocation", "");
		model.addAttribute("processPercentComplete", "");
	}
	
	private void setValuesForCurrentProcess(PageModel model, ConceptManagementAppsService conceptManagementAppsService) {
		
		ManageSnomedCTProcess currentProcess = conceptManagementAppsService.getCurrentSnomedCTProcess();
		
		int numToProcess = currentProcess.getCurrentManageSnomedCTProcessNumToProcess();
		int numProcessed = currentProcess.getCurrentManageSnomedCTProcessNumProcessed();
		float percentComplete = 0;
		
		if (numToProcess > 0) {
			percentComplete = (float) numProcessed / (float) numToProcess;
			percentComplete = percentComplete * 100;
		}
		
		Date timeStarted = currentProcess.getCurrentManageSnomedCTProcessStartTime();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String formattedDate = dateFormat.format(timeStarted);
		
		long timeStartedMillis = currentProcess.getCurrentManageSnomedCTProcessTimeStartedMilliSecs();
		long timeNowMillis = System.currentTimeMillis();
		long timePassed = timeNowMillis - timeStartedMillis;
		
		String processStatus = "Current process: " + currentProcess.getCurrentManageSnomedCTProcessName()
		        + " </br> Started on " + formattedDate + " Running for " + timePassed / 1000 + " seconds.";
		model.addAttribute("processStatus", processStatus);
		model.addAttribute("processRunning", currentProcess.getCurrentManageSnomedCTProcessName());
		model.addAttribute("dirLocation", currentProcess.getCurrentManageSnomedCTProcessDirectoryLocation());
		model.addAttribute("processPercentComplete", Math.round(percentComplete) + "% Complete Processing " + numProcessed
		        + " of " + numToProcess);
	}
}
