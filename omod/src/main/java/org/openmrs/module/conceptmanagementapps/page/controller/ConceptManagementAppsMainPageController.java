package org.openmrs.module.conceptmanagementapps.page.controller;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.api.context.Context;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.PageRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;

@Controller
public class ConceptManagementAppsMainPageController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	public void get(PageModel model, @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService,
	                PageRequest request, UiUtils ui) throws IOException {
		List<Extension> extensions = appFrameworkService
		        .getExtensionsForCurrentUser("org.openmrs.conceptmanagementapps.homepageLink");
		Set<Extension> extensionSet=new HashSet<Extension>();
		extensionSet.addAll(extensions);
		model.addAttribute("extensions", extensionSet);
		model.addAttribute("authenticatedUser", Context.getAuthenticatedUser());
	}
}
