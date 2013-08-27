package org.openmrs.module.conceptmanagementapps;

import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.utils.ModuleProperties;


public class  ConceptManagementAppsProperties extends ModuleProperties{

	public String getSnomedCTConceptSourceUuidGlobalProperty(String globalProperty) {
		AdministrationService as = Context.getAdministrationService();
	    String conceptSourceGP = as.getGlobalProperty(globalProperty);
        return conceptSourceGP;
    }
}