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
package org.openmrs.module.conceptmanagementapps.api.db.hibernate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptReferenceTermMap;
import org.openmrs.ConceptSource;
import org.openmrs.api.APIException;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.conceptmanagementapps.api.db.ConceptManagementAppsDAO;

/**
 * It is a default implementation of {@link ConceptManagementAppsDAO}.
 */
public class HibernateConceptManagementAppsDAO implements ConceptManagementAppsDAO {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private SessionFactory sessionFactory;
	
	/**
	 * @param sessionFactory the sessionFactory to set
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @return the sessionFactory
	 */
	private SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	/**
	 * @see org.openmrs.module.conceptmanagementapps.api.db.ConceptManagementAppsDAO#getReferenceTermsChildren(org.openmrs.ConceptReferenceTerm)
	 */
	@SuppressWarnings("unchecked")
	public List<ConceptReferenceTermMap> getReferenceTermsChildren(ConceptReferenceTerm currentTerm) {
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptReferenceTermMap.class, "term");
		
		criteria.add(Restrictions.eq("termA.id", currentTerm.getId()));
		
		criteria.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
		
		return (List<ConceptReferenceTermMap>) criteria.list();
		
	}
	
	/**
	 * @see org.openmrs.module.conceptmanagementapps.api.db.ConceptManagementAppsDAO#getReferenceTermsParents(org.openmrs.ConceptReferenceTerm)
	 */
	@SuppressWarnings("unchecked")
	public List<ConceptReferenceTermMap> getReferenceTermsParents(ConceptReferenceTerm currentTerm) {
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptReferenceTermMap.class, "term");
		
		criteria.add(Restrictions.eq("termB.id", currentTerm.getId()));
		
		criteria.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
		
		return (List<ConceptReferenceTermMap>) criteria.list();
		
	}
	
	/**
	 * Find concepts not mapped to specified concept source
	 */
	public List<Concept> getUnmappedConcepts(ConceptSource conceptSource, List<ConceptClass> classesToInclude)
	    throws DAOException {
		
		Set<Concept> allConceptsMappedToOurSource = getConceptsWithMappingsToThisSource(conceptSource);
		List<Concept> partiallyFilteredConceptList = getConceptsNotRetiredWithDesiredClasses(conceptSource, classesToInclude);
		
		List<Concept> fullyFilteredConceptList = new ArrayList<Concept>();
		
		//filter out concepts mapped to our source
		for (Concept concept : partiallyFilteredConceptList) {
			
			//if the concept does not have a mapping to our desired source then add it to the list to return
			if (!allConceptsMappedToOurSource.contains(concept)) {
				fullyFilteredConceptList.add(concept);
			}
			
		}
		return fullyFilteredConceptList;
	}
	
	/**
	 * Find concepts mapped to specified concept source
	 */
	@SuppressWarnings("unchecked")
	private Set<Concept> getConceptsWithMappingsToThisSource(ConceptSource conceptSource) {
		
		Criteria conceptMapCrit = sessionFactory.getCurrentSession().createCriteria(ConceptMap.class, "concept");
		
		conceptMapCrit.createAlias("conceptReferenceTerm", "term");
		
		conceptMapCrit.add(Restrictions.eq("term.conceptSource", conceptSource));
		
		Set<Concept> mappedConcept = new HashSet<Concept>();
		for (ConceptMap conceptMap : (List<ConceptMap>) conceptMapCrit.list()) {
			mappedConcept.add(conceptMap.getConcept());
		}
		return mappedConcept;
	}
	
	/**
	 * Find concepts not retired with desired classes
	 */
	@SuppressWarnings("unchecked")
	private List<Concept> getConceptsNotRetiredWithDesiredClasses(ConceptSource conceptSource,
	                                                              List<ConceptClass> classesToInclude) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Concept.class);
		
		// ignore retired concepts
		criteria.add(Restrictions.eq("retired", false));
		
		// only want concepts with the following conceptClass(s)
		Criterion orConceptClass = null;
		for (ConceptClass conceptClass : classesToInclude) {
			if (orConceptClass == null)
				orConceptClass = Restrictions.eq("conceptClass", conceptClass);
			else
				orConceptClass = Restrictions.or(orConceptClass, Restrictions.eq("conceptClass", conceptClass));
		}
		criteria.add(orConceptClass);
		
		// we only want distinct concepts
		criteria.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
		
		return (List<Concept>) criteria.list();
		
	}
	
	/**
	 * @see org.openmrs.module.conceptmanagementapps.api.db.ConceptManagementAppsDAO#getConceptReferenceTermsWithSpecifiedSourceIfIncluded(org.openmrs.ConceptSource,
	 *      java.lang.Integer, java.lang.Integer, java.lang.String, int)
	 */
	@SuppressWarnings("unchecked")
	public List<ConceptReferenceTerm> getConceptReferenceTermsWithSpecifiedSourceIfIncluded(ConceptSource specifiedSource,
	                                                                                        Integer startIndex,
	                                                                                        Integer numToReturn,
	                                                                                        String sortColumn, int order)
	    throws DAOException {
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptReferenceTerm.class);
		if (specifiedSource != null) {
			criteria.add(Restrictions.eq("conceptSource", specifiedSource));
		}
		
		if (order == 1)
			criteria.addOrder(Order.desc(sortColumn));
		if (order == -1)
			criteria.addOrder(Order.asc(sortColumn));
		if (startIndex != null)
			criteria.setFirstResult(startIndex);
		if (numToReturn != null && numToReturn > 0) {
			criteria.setMaxResults(numToReturn);
		} else {
			if (numToReturn != -1) {
				criteria.setMaxResults(1000);
			}
		}
		
		criteria.add(Restrictions.eq("retired", false));
		
		return (List<ConceptReferenceTerm>) criteria.list();
	}
	
	/**
	 * @see org.openmrs.module.conceptmanagementapps.api.db.ConceptManagementAppsDAO#getCountOfConceptReferenceTermsWithSpecifiedSourceIfIncluded(org.openmrs.ConceptSource)
	 */
	public Integer getCountOfConceptReferenceTermsWithSpecifiedSourceIfIncluded(ConceptSource specifiedSource)
	    throws DAOException {
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptReferenceTerm.class);
		
		if (specifiedSource != null)
			criteria.add(Restrictions.eq("conceptSource", specifiedSource));
		criteria.add(Restrictions.eq("retired", false));
		
		criteria.setProjection(Projections.rowCount());
		Integer count = ((Number) criteria.uniqueResult()).intValue();
		
		return count;
	}
	
	/**
	 * @see org.openmrs.module.conceptmanagementapps.api.db.ConceptManagementAppsDAO#getConceptReferenceTermsWithQuery(java.lang.String,
	 *      org.openmrs.ConceptSource, java.lang.Integer, java.lang.Integer, boolean,
	 *      java.lang.String, int)
	 */
	@SuppressWarnings("unchecked")
	public List<ConceptReferenceTerm> getConceptReferenceTermsWithQuery(String query, ConceptSource conceptSource,
	                                                                    Integer start, Integer length,
	                                                                    boolean includeRetired, String sortColumn, int order)
	    throws APIException {
		Criteria criteria = createConceptReferenceTermCriteria(query, conceptSource, includeRetired);
		
		if (order == 1)
			criteria.addOrder(Order.desc(sortColumn));
		if (order == -1)
			criteria.addOrder(Order.asc(sortColumn));
		if (start != null)
			criteria.setFirstResult(start);
		if (length != null && length > 0)
			criteria.setMaxResults(length);
		return criteria.list();
	}
	
	/**
	 * @param query
	 * @param conceptSource
	 * @param includeRetired
	 * @return
	 */
	private Criteria createConceptReferenceTermCriteria(String query, ConceptSource conceptSource, boolean includeRetired) {
		Criteria searchCriteria = sessionFactory.getCurrentSession().createCriteria(ConceptReferenceTerm.class);
		
		if (conceptSource != null)
			searchCriteria.add(Restrictions.eq("conceptSource", conceptSource));
		if (!includeRetired)
			searchCriteria.add(Restrictions.eq("retired", false));
		if (query != null)
			searchCriteria.add(Restrictions.or(Restrictions.ilike("name", query, MatchMode.ANYWHERE),
			    Restrictions.ilike("code", query, MatchMode.ANYWHERE)));
		
		return searchCriteria;
	}
	
	/**
	 * @see org.openmrs.module.conceptmanagementapps.api.db.ConceptManagementAppsDAO#getCountOfConceptReferenceTermsWithQuery(java.lang.String,
	 *      org.openmrs.ConceptSource, boolean)
	 */
	public Integer getCountOfConceptReferenceTermsWithQuery(String query, ConceptSource conceptSource, boolean includeRetired)
	    throws APIException {
		
		Criteria criteria = createConceptReferenceTermCriteria(query, conceptSource, includeRetired);
		
		criteria.setProjection(Projections.rowCount());
		Integer count = ((Number) criteria.uniqueResult()).intValue();
		
		return count;
		
	}
}
