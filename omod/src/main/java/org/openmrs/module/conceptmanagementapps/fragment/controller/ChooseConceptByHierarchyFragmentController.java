package org.openmrs.module.conceptmanagementapps.fragment.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSource;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.conceptmanagementapps.ConceptManagementAppsConstants;
import org.openmrs.module.conceptmanagementapps.ConceptManagementAppsProperties;
import org.openmrs.module.conceptmanagementapps.DataObject;
import org.openmrs.module.conceptmanagementapps.api.ConceptManagementAppsService;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.Gson;

public class ChooseConceptByHierarchyFragmentController {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	public List<String> search(UiSessionContext context, @RequestParam("id") String query) throws Exception {
		
		Locale locale = context.getLocale();
		
		List<ConceptSearchResult> hits = Context.getConceptService().getConcepts(query, locale, true);
		if (hits.size() > 100) {
			hits = hits.subList(0, 100);
		}
		
		List<String> list = new ArrayList<String>();
		
		Gson gson = new Gson();
		for (ConceptSearchResult hit : hits) {
			list.add(gson.toJson(simplifyConcept(hit.getConcept(), locale)));
		}
		
		return list;
	}
	
	public List<String> getAncestors(UiSessionContext context, @RequestParam("termId") String termId,
	                                 @RequestParam("conceptId") String conceptId, @RequestParam("updateBy") String updateBy)
	    throws Exception {
		
		Locale locale = context.getLocale();
		ConceptService conceptService = (ConceptService) Context.getService(ConceptService.class);
		
		ConceptManagementAppsProperties conceptManagementAppsProperties = new ConceptManagementAppsProperties();
		ConceptSource conceptSource = conceptService
		        .getConceptSourceByUuid(conceptManagementAppsProperties
		                .getSnomedCTConceptSourceUuidGlobalProperty(ConceptManagementAppsConstants.SNOMED_CT_CONCEPT_SOURCE_UUID_GP));
		
		if (StringUtils.equals(updateBy, "conceptUpdate")) {
			
			Collection<ConceptMap> mappings = conceptService.getConcept(Integer.parseInt(conceptId)).getConceptMappings();
			
			for (ConceptMap conceptMap : mappings) {
				
				if (StringUtils.equals(conceptMap.getConceptReferenceTerm().getConceptSource().getUuid(),
				    conceptSource.getUuid())
				        && StringUtils.equals(conceptMap.getConceptMapType().getUuid(),
				            ConceptManagementAppsConstants.SAME_AS_CONCEPT_MAP_TYPE_UUID)) {
					termId = Integer.toString(conceptMap.getConceptReferenceTerm().getConceptReferenceTermId());
				}
			}
			return getRefTermAncestors(context, termId, locale, conceptSource);
		} else {
			return getRefTermAncestors(context, termId, locale, conceptSource);
		}
	}
	
	private List<String> getRefTermAncestors(UiSessionContext context, String id, Locale locale, ConceptSource conceptSource)
	    throws Exception {
		
		ConceptService conceptService = (ConceptService) Context.getService(ConceptService.class);
		
		ConceptManagementAppsService conceptManagementAppsService = (ConceptManagementAppsService) Context
		        .getService(ConceptManagementAppsService.class);
		
		//there are no mappings so we need to send it through blank and let the user know
		if (StringUtils.isEmpty(id) || StringUtils.isBlank(id) || id.contains("empty")) {

			id = "0";
			Set<ConceptReferenceTerm> parentTerms = new HashSet<ConceptReferenceTerm>();
			Set<ConceptReferenceTerm> childTerms = new HashSet<ConceptReferenceTerm>();
			return formatByRefTermForUIWithGson(conceptService, id, childTerms, parentTerms, locale);
		}
		
		Set<ConceptReferenceTerm> parentTerms = conceptManagementAppsService.getRefTermParentReferenceTerms(
		    conceptService.getConceptReferenceTerm(Integer.parseInt(id)), conceptSource);
		
		Set<ConceptReferenceTerm> childTerms = conceptManagementAppsService.getRefTermChildReferenceTerms(
		    conceptService.getConceptReferenceTerm(Integer.parseInt(id)), conceptSource);
		
		return formatByRefTermForUIWithGson(conceptService, id, childTerms, parentTerms, locale);
		
	}
	
	private List<String> formatByRefTermForUIWithGson(ConceptService conceptService, String id,
	                                                  Set<ConceptReferenceTerm> childTerms,
	                                                  Set<ConceptReferenceTerm> parentTerms, Locale locale) {
		
		List<String> data = new ArrayList<String>();
		try {
			
			Gson gson = new Gson();
			
			ConceptReferenceTerm currentRefTerm = conceptService.getConceptReferenceTerm(Integer.parseInt(id));
			HashMap<ConceptReferenceTerm, List<Concept>> parentMappings = getAssociatedConceptsToRefTerms(parentTerms);
			HashMap<ConceptReferenceTerm, List<Concept>> childMappings = getAssociatedConceptsToRefTerms(childTerms);
			
			List<String> parents = new ArrayList<String>();
			for (ConceptReferenceTerm term : parentTerms) {
				
				List<Concept> mappedConcepts = parentMappings.get(term);
				List<DataObject> mappedConceptsDOList = new ArrayList<DataObject>();
				
				for (Concept concept : mappedConcepts) {
					
					DataObject mappedConceptDataObject = simplifyConcept(concept, locale);
					mappedConceptsDOList.add(mappedConceptDataObject);
				}
				DataObject refTermDataObject = simplifyReferenceTerm(term);
				parents.add(gson.toJson(simplifyMapping(mappedConceptsDOList, refTermDataObject)));
			}
			List<String> children = new ArrayList<String>();
			for (ConceptReferenceTerm term : childTerms) {
				
				List<Concept> mappedConcepts = childMappings.get(term);
				List<DataObject> mappedConceptsDOList = new ArrayList<DataObject>();
				
				for (Concept concept : mappedConcepts) {
					
					DataObject mappedConceptDataObject = simplifyConcept(concept, locale);
					mappedConceptsDOList.add(mappedConceptDataObject);
				}
				DataObject refTermDataObject = simplifyReferenceTerm(term);
				children.add(gson.toJson(simplifyMapping(mappedConceptsDOList, refTermDataObject)));
			}
			String currentTerm = gson.toJson(simplifyReferenceTerm(currentRefTerm));
			
			DataObject ancestorsDataObject = simplifyAncestors(parents, children, currentTerm);
			
			data.add(gson.toJson(ancestorsDataObject));
			
		}
		catch (Exception e) {
			log.error("Error generated", e);
		}
		return data;
	}
	
	private DataObject simplifyMapping(List<DataObject> conceptString, DataObject refTermString) {
		
		List<Object> propertyNamesAndValues = new ArrayList<Object>();
		
		propertyNamesAndValues.add("mappedRefTerm");
		propertyNamesAndValues.add(refTermString);
		
		propertyNamesAndValues.add("mappedConcept");
		propertyNamesAndValues.add(conceptString);
		
		DataObject dataObject = DataObject.create(propertyNamesAndValues);
		
		return dataObject;
	}
	
	private DataObject simplifyAncestors(List<String> parents, List<String> children, String term) throws Exception {
		List<Object> propertyNamesAndValues = new ArrayList<Object>();
		
		propertyNamesAndValues.add("parents");
		propertyNamesAndValues.add(parents);
		propertyNamesAndValues.add("children");
		propertyNamesAndValues.add(children);
		propertyNamesAndValues.add("term");
		propertyNamesAndValues.add(term);
		
		DataObject dataObject = DataObject.create(propertyNamesAndValues);
		
		return dataObject;
	}
	
	public DataObject simplifyReferenceTerm(ConceptReferenceTerm term) throws Exception {
		
		List<Object> propertyNamesAndValues = new ArrayList<Object>();
		
		if (term == null || StringUtils.isBlank(term.getCode()) || StringUtils.isEmpty(term.getCode())) {
			
			propertyNamesAndValues.add("termCode");
			propertyNamesAndValues.add("");
			propertyNamesAndValues.add("termId");
			propertyNamesAndValues.add("");
			propertyNamesAndValues.add("termName");
			propertyNamesAndValues.add("No Snomed CT Reference Terms Mapped To This Concept");
			
		} else {
			
			propertyNamesAndValues.add("termCode");
			propertyNamesAndValues.add((term.getCode()));
			propertyNamesAndValues.add("termId");
			propertyNamesAndValues.add((term.getId()));
			
			if (term.getName() != null) {
				propertyNamesAndValues.add("termName");
				propertyNamesAndValues.add(term.getName());
			}
		}
		DataObject dataObject = DataObject.create(propertyNamesAndValues);
		return dataObject;
	}
	
	public DataObject simplifyConcept(Concept concept, Locale locale) throws Exception {
		
		List<Object> propertyNamesAndValues = new ArrayList<Object>();
		ConceptName preferredName = concept.getPreferredName(locale);
		
		propertyNamesAndValues.add("conceptId");
		propertyNamesAndValues.add(Integer.toString((concept.getConceptId())));
		
		if (preferredName != null && preferredName.getName() != null) {
			propertyNamesAndValues.add("conceptName");
			propertyNamesAndValues.add(preferredName.getName());
		} else if (preferredName == null && concept.getName() != null) {
			propertyNamesAndValues.add("conceptName");
			propertyNamesAndValues.add(concept.getName().getName());
		}
		DataObject dataObject = DataObject.create(propertyNamesAndValues);
		return dataObject;
	}
	
	private HashMap<ConceptReferenceTerm, List<Concept>> getAssociatedConceptsToRefTerms(Set<ConceptReferenceTerm> terms) {
		
		ConceptService conceptService = (ConceptService) Context.getService(ConceptService.class);
		HashMap<ConceptReferenceTerm, List<Concept>> hmconceptsToRefTerms = new HashMap<ConceptReferenceTerm, List<Concept>>();
		
		for (ConceptReferenceTerm refTerm : terms) {
			
			List<Concept> mappedConcept = conceptService.getConceptsByMapping(refTerm.getCode(), refTerm.getConceptSource()
			        .getName(), false);
			hmconceptsToRefTerms.put(refTerm, mappedConcept);
		}
		return hmconceptsToRefTerms;
	}
	
	public void get(UiSessionContext sessionContext, UiUtils ui, PageModel model) throws Exception {
		
	}
	
}
