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
package org.openmrs.module.conceptmanagementapps.api.db;

import java.util.List;

import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptReferenceTermMap;
import org.openmrs.ConceptSource;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.conceptmanagementapps.api.ConceptManagementAppsService;

/**
 * Database methods for {@link ConceptManagementAppsService}.
 */
public interface ConceptManagementAppsDAO {
	
	/**
	 * Gets Concepts not mapped to the given source with desired classes
	 * 
	 * @param source
	 * @param classes
	 * @return List of Concepts
	 * @throws DAOException
	 */
	public List<Concept> getUnmappedConcepts(ConceptSource source, List<ConceptClass> classes) throws DAOException;
	
	/**
	 * Gets the concept reference terms for a source if given (otherwise all) from a starting point
	 * if given (otherwise from 0) returning specified number if given (otherwise 1000) ordered
	 * ascending or descending sorted on given column
	 * 
	 * @param specifiedSource
	 * @param startIndex
	 * @param numToReturn
	 * @param sortColumn
	 * @param order
	 * @return List of ConceptReferenceTerms
	 * @throws DAOException
	 */
	public List<ConceptReferenceTerm> getConceptReferenceTermsWithSpecifiedSourceIfIncluded(ConceptSource specifiedSource,
	                                                                                        Integer startIndex,
	                                                                                        Integer numToReturn,
	                                                                                        String sortColumn, int order)
	    throws DAOException;
	
	/**
	 * Gets the count of concept reference terms for a source if given (otherwise all)
	 * 
	 * @param specifiedSource
	 * @return Integer
	 * @throws DAOException
	 */
	public Integer getCountOfConceptReferenceTermsWithSpecifiedSourceIfIncluded(ConceptSource specifiedSource)
	    throws DAOException;
	
	/**
	 * Queries for matching reference terms given a query string from a starting point if given
	 * (otherwise from 0) returning specified number if given (otherwise 1000) ordered ascending or
	 * descending sorted on given column
	 * 
	 * @param query
	 * @param conceptSource
	 * @param start
	 * @param length
	 * @param includeRetired
	 * @param sortColumn
	 * @param order
	 * @return List of ConceptReferenceTerms
	 * @throws DAOException
	 */
	public List<ConceptReferenceTerm> getConceptReferenceTermsWithQuery(String query, ConceptSource conceptSource,
	                                                                    Integer start, Integer length,
	                                                                    boolean includeRetired, String sortColumn, int order)
	    throws DAOException;
	
	/**
	 * Gets count of matching reference terms given a query string
	 * 
	 * @param query
	 * @param conceptSource
	 * @param includeRetired
	 * @return Integer
	 * @throws DAOException
	 */
	public Integer getCountOfConceptReferenceTermsWithQuery(String query, ConceptSource conceptSource, boolean includeRetired)
	    throws DAOException;
	
	/**
	 * Gets the parents for the specified conceptReferenceTerm using the concept reference term map
	 * termA id
	 * 
	 * @param currentTerm
	 * @return List of ConceptReferenceTermMaps
	 * @throws DAOException
	 */
	public List<ConceptReferenceTermMap> getReferenceTermsParents(ConceptReferenceTerm currentTerm) throws DAOException;
	
	/**
	 * Gets the children for the specified conceptReferenceTerm using the concept reference term map
	 * termB id
	 * 
	 * @param currentTerm
	 * @return List of ConceptReferenceTermMaps
	 * @throws DAOException
	 */
	public List<ConceptReferenceTermMap> getReferenceTermsChildren(ConceptReferenceTerm currentTerm) throws DAOException;
	
}
