/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC. All Rights Reserved.
 */
package org.openmrs.module.conceptmanagementapps.api;

import java.io.PrintWriter;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.ui.framework.page.FileDownload;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.supercsv.io.ICsvMapWriter;

/**
 * This service exposes module's core functionality. It is a Spring managed bean which is configured
 * in moduleApplicationContext.xml.
 * <p>
 * It can be accessed only via Context:<br>
 * <code>
 * Context.getService(ConceptManagementAppsService.class).someMethod();
 * </code>
 * 
 * @see org.openmrs.api.context.Context
 */

public interface ConceptManagementAppsService extends OpenmrsService {
	
	public void setCancelManageSnomedCTProcess(Boolean cancelProcess);
	
	public Boolean getCancelManageSnomedCTProcess();
	
	public ManageSnomedCTProcess getCurrentSnomedCTProcess();
	
	@Transactional(readOnly = true)
	public List<Concept> getUnmappedConcepts(ConceptSource conceptSource, List<ConceptClass> classesToInclude);
	
	@Transactional
	public FileDownload uploadSpreadsheet(MultipartFile spreadsheetFile) throws APIException;
	
	@Transactional
	public void startManageSnomedCTProcess(String process, String dirPath) throws APIException;
	
	@Transactional(readOnly = true)
	public List<ConceptReferenceTerm> getConceptReferenceTerms(ConceptSource specifiedSource, Integer startIndex,
	                                                           Integer numToReturn, String sortColumn, int order)
	    throws APIException;
	
	@Transactional(readOnly = true)
	public Integer getCountOfConceptReferenceTerms(ConceptSource specifiedSource) throws APIException;
	
	@Transactional(readOnly = true)
	public List<ConceptReferenceTerm> getConceptReferenceTermsWithQuery(String query, ConceptSource conceptSource,
	                                                                    Integer start, Integer length,
	                                                                    boolean includeRetired, String sortColumn, int order)
	    throws APIException;
	
	@Transactional(readOnly = true)
	public ICsvMapWriter writeFileWithMissingConceptMappings(List<Concept> conceptList, PrintWriter spreadsheetWriter,
	                                                         String mapTypeForDefaultValue, String conceptSourceName)
	    throws Exception;
	
	@Transactional(readOnly = true)
	public Integer getCountOfConceptReferenceTermsWithQuery(String query, ConceptSource conceptSource, boolean includeRetired)
	    throws APIException;
	
}
