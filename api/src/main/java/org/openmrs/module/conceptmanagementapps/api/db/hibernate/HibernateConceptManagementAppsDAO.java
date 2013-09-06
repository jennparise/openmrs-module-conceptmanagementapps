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
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
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
	
	@SuppressWarnings("unchecked")
	public List<ConceptReferenceTermMap> getReferenceTermsChildren(ConceptReferenceTerm currentTerm) {
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptReferenceTermMap.class, "term");
		
		criteria.add(Restrictions.eq("termA.id", currentTerm.getId()));
		
		criteria.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
		
		return (List<ConceptReferenceTermMap>) criteria.list();
		
	}
	
	@SuppressWarnings("unchecked")
	public List<ConceptReferenceTermMap> getReferenceTermsParents(ConceptReferenceTerm currentTerm) {
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptReferenceTermMap.class, "term");
		
		criteria.add(Restrictions.eq("termB.id", currentTerm.getId()));
		
		criteria.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
		
		return (List<ConceptReferenceTermMap>) criteria.list();
		
	}
	
	public List<Concept> getUnmappedConcepts(ConceptSource conceptSource, List<ConceptClass> classesToInclude)
	    throws DAOException {
		
		List<Concept> conceptsWithNoMappings = getConceptsWithNoMappings(classesToInclude);
		List<Concept> conceptsWithOtherMappings = getConceptsWithMappingsButNotToThisSource(conceptSource, classesToInclude);
		
		List<Concept> allConceptsNotMappedToOurSource = conceptsWithOtherMappings;
		allConceptsNotMappedToOurSource.addAll(conceptsWithNoMappings);
		
		return filterUnique((List<Concept>) allConceptsNotMappedToOurSource);
		
	}
	
	@SuppressWarnings("unchecked")
	private List<Concept> getConceptsWithMappingsButNotToThisSource(ConceptSource conceptSource,
	                                                                List<ConceptClass> classesToInclude) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Concept.class);
		Criteria conceptMapCrit = sessionFactory.getCurrentSession().createCriteria(Concept.class, "concept");
		
		conceptMapCrit.createAlias("conceptMappings", "conceptMappings");
		conceptMapCrit.createAlias("conceptMappings.conceptReferenceTerm", "term");
		
		conceptMapCrit.add(Restrictions.eq("term.conceptSource", conceptSource));
		
		criteria.createAlias("conceptMappings", "conceptMappings");
		
		//we don't want any mappings that have a concept which has a mapping to our source
		criteria.add(Restrictions.not(Restrictions.in("conceptMappings.concept", (List<Concept>) conceptMapCrit.list())));
		
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
	
	private List<Concept> filterUnique(List<Concept> input) {
		Set<Integer> already = new HashSet<Integer>();
		List<Concept> unique = new ArrayList<Concept>();
		for (Concept candidate : input) {
			if (already.contains(candidate.getId())) {
				continue;
			} else {
				already.add(candidate.getId());
				unique.add(candidate);
			}
		}
		return unique;
	}
	
	@SuppressWarnings("unchecked")
	private List<Concept> getConceptsWithNoMappings(List<ConceptClass> classesToInclude) {
		
		//we also want all concepts with out a mapping at all because that also means it is not mapped to our source
		String hql = "";
		hql += "select distinct concept";
		hql += " from Concept as concept";
		hql += " left join concept.conceptMappings as conceptMappings where conceptMappings is NULL";
		hql += " and";
		hql += " concept.retired = false";
		hql += " and";
		
		int i = 1;
		// only want concepts with the following conceptClass(s)
		for (ConceptClass conceptClass : classesToInclude) {
			
			if (i < classesToInclude.size()) {
				hql += " concept.conceptClass.conceptClassId=" + conceptClass.getConceptClassId();
				hql += " or";
				
			} else {
				hql += " concept.conceptClass.conceptClassId=" + conceptClass.getConceptClassId();
			}
			i++;
		}
		
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		return (List<Concept>) query.list();
	}
	
	/**
	 * @see org.openmrs.module.conceptmanagementapps.api.db.ConceptManagementAppsDAO#getConceptReferenceTermsBySource(ConceptSource)
	 */
	@SuppressWarnings("unchecked")
	public List<ConceptReferenceTerm> getConceptReferenceTerms(ConceptSource specifiedSource, Integer startIndex,
	                                                           Integer numToReturn, String sortColumn, int order)
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
	
	public Integer getCountOfConceptReferenceTerms(ConceptSource specifiedSource) throws DAOException {
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptReferenceTerm.class);
		
		if (specifiedSource != null)
			criteria.add(Restrictions.eq("conceptSource", specifiedSource));
		criteria.add(Restrictions.eq("retired", false));
		
		criteria.setProjection(Projections.rowCount());
		Integer count = ((Number) criteria.uniqueResult()).intValue();
		
		return count;
	}
	
	/**
	 * @see org.openmrs.api.db.ConceptDAO#getConceptReferenceTerms(String, ConceptSource, Integer,
	 *      Integer, boolean)
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
	
	public Integer getCountOfConceptReferenceTermsWithQuery(String query, ConceptSource conceptSource, boolean includeRetired)
	    throws APIException {
		
		Criteria criteria = createConceptReferenceTermCriteria(query, conceptSource, includeRetired);
		
		criteria.setProjection(Projections.rowCount());
		Integer count = ((Number) criteria.uniqueResult()).intValue();
		
		return count;
		
	}
}
