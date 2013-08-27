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
package org.openmrs.module.conceptmanagementapps.api.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptReferenceTermMap;
import org.openmrs.ConceptSource;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.conceptmanagementapps.ConceptManagementAppsConstants;
import org.openmrs.module.conceptmanagementapps.api.ConceptManagementAppsService;
import org.openmrs.module.conceptmanagementapps.api.ManageSnomedCTProcess;
import org.openmrs.module.conceptmanagementapps.api.db.ConceptManagementAppsDAO;
import org.openmrs.ui.framework.page.FileDownload;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;
import org.openmrs.module.conceptmanagementapps.ConceptManagementAppsProperties;

/**
 * It is a default implementation of {@link ConceptManagementAppsService}.
 */
public class ConceptManagementAppsServiceImpl extends BaseOpenmrsService implements ConceptManagementAppsService {
	
	protected Log log = LogFactory.getLog(getClass());
	
	private static final CsvPreference TAB_DELIMITED = new CsvPreference.Builder('\"', '\t', "\n").build();
	
	private static final String RETIRED_TERM = "1";
	
	private static final String PARENT_TERM = "sourceId";
	
	private static final String CHILD_TERM = "destinationId";
	
	private static final String TERM_ID = "conceptId";
	
	private static final String IS_A_RELATIONSHIP = "116680003";
	
	private static final String ROW_ID = "id";
	
	private static final String TERM_NAME = "term";
	
	private static final String EFFECTIVE_DATE = "effectiveDate";
	
	private static final String RELATIONSHIP_FILE = "sct2_Relationship_Full_INT_20130131.txt";
	
	private static final String DESCRIPTION_FILE = "sct2_Description_Full-en_INT_20130131.txt";
	
	private ConceptManagementAppsDAO dao;
	
	private boolean cancelManageSnomedCTProcess = true;
	
	private ManageSnomedCTProcess currentSnomedCTProcess;
	
	private static StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_44);
	
	private static String snomedIndexFileDirectoryLocation;
	
	/**
	 * @param dao the dao to set
	 */
	public void setDao(ConceptManagementAppsDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @return the dao
	 */
	public ConceptManagementAppsDAO getDao() {
		return dao;
	}
	
	/**
	 * @param currentSnomedCTProcess the currentSnomedCTProcess to set
	 */
	public void setCurrentSnomedCTProcess(ManageSnomedCTProcess currentSnomedCTProcess) {
		this.currentSnomedCTProcess = currentSnomedCTProcess;
	}
	
	/**
	 * @return the currentSnomedCTProcess
	 */
	public ManageSnomedCTProcess getCurrentSnomedCTProcess() {
		return currentSnomedCTProcess;
	}
	
	/**
	 * @param cancelManageSnomedCTProcess the cancelManageSnomedCTProcess to set
	 */
	public void setCancelManageSnomedCTProcess(Boolean cancelManageSnomedCTProcess) {
		this.cancelManageSnomedCTProcess = cancelManageSnomedCTProcess;
	}
	
	/**
	 * @return cancelManageSnomedCTProcess
	 */
	public Boolean getCancelManageSnomedCTProcess() {
		return cancelManageSnomedCTProcess;
	}
	
	@Transactional(readOnly = true)
	public List<Concept> getUnmappedConcepts(ConceptSource conceptSource, List<ConceptClass> classesToInclude) {
		
		return this.dao.getUnmappedConcepts(conceptSource, classesToInclude);
	}
	
	@Transactional(readOnly = true)
	public List<ConceptReferenceTerm> getConceptReferenceTerms(ConceptSource specifiedSource, Integer startIndex,
	                                                           Integer numToReturn, String sortColumn, int order)
	    throws DAOException {
		
		return this.dao.getConceptReferenceTerms(specifiedSource, startIndex, numToReturn, sortColumn, order);
	}
	
	@Transactional(readOnly = true)
	public Integer getCountOfConceptReferenceTerms(ConceptSource specifiedSource) throws DAOException {
		
		return this.dao.getCountOfConceptReferenceTerms(specifiedSource);
	}
	
	@Transactional(readOnly = true)
	public List<ConceptReferenceTerm> getConceptReferenceTermsWithQuery(String query, ConceptSource conceptSource,
	                                                                    Integer start, Integer length,
	                                                                    boolean includeRetired, String sortColumn, int order)
	    throws DAOException {
		
		return this.dao.getConceptReferenceTermsWithQuery(query, conceptSource, start, length, includeRetired, sortColumn,
		    order);
	}
	
	@Transactional(readOnly = true)
	public Integer getCountOfConceptReferenceTermsWithQuery(String query, ConceptSource conceptSource, boolean includeRetired)
	    throws DAOException {
		
		return this.dao.getCountOfConceptReferenceTermsWithQuery(query, conceptSource, includeRetired);
		
	}
	
	@Transactional
	public FileDownload uploadSpreadsheet(MultipartFile spreadsheetFile) throws APIException {
		
		List<String> fileLines = new ArrayList<String>();
		ICsvMapReader mapReader = null;
		FileDownload fileShowingErrors = null;
		boolean hasErrors = false;
		String errorReason = null;
		
		try {
			
			mapReader = new CsvMapReader(new InputStreamReader(spreadsheetFile.getInputStream()),
			        CsvPreference.STANDARD_PREFERENCE);
			
			final String[] header = mapReader.getHeader(true);
			final CellProcessor[] processors = getSpreadsheetProcessors();
			
			String delimiter = ",";
			fileLines.add("errors - delete this column to resubmit" + delimiter + "map type" + delimiter + "source name"
			        + delimiter + "source code" + delimiter + "concept Id" + delimiter + "concept uuid" + delimiter
			        + "preferred name" + delimiter + "description" + delimiter + "class" + delimiter + "datatype"
			        + delimiter + "all existing mappings");
			
			for (Map<String, Object> mapList = mapReader.read(header, processors); mapList != null; mapList = mapReader
			        .read(header, processors)) {
				
				errorReason = " ";
				errorReason = getInitialErrorsBeforeTryingToSaveConcept(mapList);
				
				String line = mapReader.getUntokenizedRow();
				fileLines.add(errorReason + "," + line);
				
				if (StringUtils.isNotEmpty(errorReason) && StringUtils.isNotBlank(errorReason)) {
					hasErrors = true;
				}
			}
			fileShowingErrors = writeToFile(fileLines);
			
		}
		
		catch (APIException e) {
			e.printStackTrace();
			throw new APIException("error on row " + mapReader.getRowNumber() + "," + mapReader.getUntokenizedRow() + e);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		finally {
			
			if (mapReader != null) {
				
				try {
					mapReader.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		//if there are no errors then go ahead and pass through again and save
		if (!hasErrors) {
			
			setMapAndSaveConcept(spreadsheetFile);
			return null;
		}
		
		return fileShowingErrors;
		
	}
	
	@Transactional
	public void startManageSnomedCTProcess(String process, String snomedFileDirectory) throws APIException {
		
		try {
			
			snomedIndexFileDirectoryLocation = snomedFileDirectory + "/tmpLucene";
			
			currentSnomedCTProcess = new ManageSnomedCTProcess(process);
			currentSnomedCTProcess.setCurrentManageSnomedCTProcessDirectoryLocation(snomedFileDirectory);
			
			indexSnomedFiles(snomedFileDirectory);
			
			if (process.contains("addSnomedCTNames")) {
				
				addNamesToSnomedCTTerms(snomedFileDirectory);
			}
			if (process.contains("addSnomedCTAncestors")) {
				
				addAncestorsToSnomedCTTerms(snomedFileDirectory);
			}
			if (process.contains("addSnomedCTRelationships")) {
				
				addRelationshipsToSnomedCTTerms(snomedFileDirectory);
			}
		}
		finally {
			try {
				FileUtils.cleanDirectory(new File(snomedIndexFileDirectoryLocation));
				
			}
			catch (IOException e) {
				log.error("Error cleaning Lucene Index Directory ", e);
			}
		}
		
	}
	
	@Transactional(readOnly = true)
	public ICsvMapWriter writeFileWithMissingConceptMappings(List<Concept> conceptList, PrintWriter spreadsheetWriter,
	                                                         String mapTypeDefaultValue, String conceptSourceName)
	    throws Exception {
		Locale locale = Context.getLocale();
		
		final String[] header = new String[] { "map type", "source name", "source code", "concept Id", "concept uuid",
		        "preferred name", "description", "class", "datatype", "all existing mappings" };
		
		final Map<String, Object> conceptsMissingMappings = new HashMap<String, Object>();
		
		ICsvMapWriter mapWriter = null;
		
		try {
			
			mapWriter = new CsvMapWriter(spreadsheetWriter, CsvPreference.STANDARD_PREFERENCE);
			
			final CellProcessor[] downloadProcessors = getSpreadsheetProcessors();
			
			mapWriter.writeHeader(header);
			String mapTypeValue;
			if (StringUtils.isNotEmpty(mapTypeDefaultValue) && StringUtils.isNotBlank(mapTypeDefaultValue)) {
				mapTypeValue = mapTypeDefaultValue;
				
			} else {
				mapTypeValue = " ";
			}
			
			for (Concept concept : conceptList) {
				
				conceptsMissingMappings.put(header[0], mapTypeValue);
				conceptsMissingMappings.put(header[1], conceptSourceName);
				conceptsMissingMappings.put(header[2], " ");
				conceptsMissingMappings.put(header[3], concept.getConceptId());
				conceptsMissingMappings.put(header[4], concept.getUuid());
				
				if (concept.getPreferredName(locale) != null) {
					conceptsMissingMappings.put(header[5], concept.getPreferredName(locale));
				} else {
					conceptsMissingMappings.put(header[5], " ");
				}
				
				if (concept.getDescription(locale) != null) {
					ConceptDescription cd = concept.getDescription(locale);
					conceptsMissingMappings.put(header[6], cd.getDescription());
				} else {
					conceptsMissingMappings.put(header[6], " ");
				}
				
				conceptsMissingMappings.put(header[7], concept.getConceptClass().getName());
				conceptsMissingMappings.put(header[8], concept.getDatatype().getName());
				
				String mappingsName = "  ";
				for (ConceptMap cm : concept.getConceptMappings()) {
					if (cm.getConceptMapType() != null) {
						mappingsName += cm.getConceptMapType().getName() + " ";
					}
					if (cm.getConceptReferenceTerm() != null && cm.getConceptReferenceTerm().getConceptSource() != null) {
						mappingsName += cm.getConceptReferenceTerm().getConceptSource().getName() + "\n";
					}
				}
				
				//strip new line off of last entry
				mappingsName = mappingsName.substring(0, mappingsName.length() - 1);
				conceptsMissingMappings.put(header[9], mappingsName);
				
				// write the conceptsMissingMappings maps
				mapWriter.write(conceptsMissingMappings, header, downloadProcessors);
			}
		}
		finally {
			if (mapWriter != null) {
				mapWriter.close();
			}
			if (spreadsheetWriter != null) {
				spreadsheetWriter.close();
			}
			
		}
		
		return mapWriter;
		
	}
	
	private FileDownload writeToFile(List<String> lines) {
		String linesShowingIfThereAreErrors = "";
		
		for (String aline : lines) {
			
			linesShowingIfThereAreErrors += aline + "\n";
		}
		
		String theDate = new SimpleDateFormat("dMy_Hm").format(new Date());
		String contentType = "text/csv;charset=UTF-8";
		String errorFilename = "conceptsMissingMappingsErrors" + theDate + ".csv";
		
		return new FileDownload(errorFilename, contentType, linesShowingIfThereAreErrors.getBytes());
	}
	
	private void addAncestorsToSnomedCTTerms(String snomedFileDirectory) throws APIException {
		
		ConceptService cs = Context.getConceptService();
		
		ConceptManagementAppsProperties cmap = new ConceptManagementAppsProperties();
		String snomedSourceUuid = cmap
		        .getSnomedCTConceptSourceUuidGlobalProperty(ConceptManagementAppsConstants.SNOMED_CT_CONCEPT_SOURCE_UUID_GP);
		
		ConceptSource snomedSource = cs.getConceptSourceByUuid(snomedSourceUuid);
		
		List<ConceptReferenceTerm> sourceRefTerms = getConceptReferenceTerms(snomedSource, 0, -1, "code", 1);
		List<ConceptReferenceTerm> listOfTermsToSave = new ArrayList<ConceptReferenceTerm>();
		
		Set<Long> listOfNewTermIds = new HashSet<Long>();
		Set<Integer> listOfDocIds = new HashSet<Integer>();
		Set<Integer> listOfExistingIds = new HashSet<Integer>();
		Set<ConceptReferenceTerm> listOfNewTerms = new HashSet<ConceptReferenceTerm>();
		
		try {
			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(snomedIndexFileDirectoryLocation)));
			IndexSearcher searcher = new IndexSearcher(reader);
			
			for (ConceptReferenceTerm term : sourceRefTerms) {
				if (!getCancelManageSnomedCTProcess()) {
					Set<Integer> tmpListOfDocIds = new HashSet<Integer>();
					
					tmpListOfDocIds = searchIndexesGetAncestorTermIds(term.getCode(), listOfNewTermIds, listOfDocIds,
					    snomedSource, searcher);
					
					listOfExistingIds.add(term.getCode().hashCode());
					
					listOfDocIds.addAll(tmpListOfDocIds);
				} else {
					return;
				}
				
			}
			
			listOfNewTerms = createNewTerms(listOfDocIds, searcher, snomedSource, listOfExistingIds);
			listOfTermsToSave.addAll(listOfNewTerms);
			
			if (listOfTermsToSave != null) {
				saveNewOrUpdatedRefTerms(listOfTermsToSave);
			}
			
			reader.close();
		}
		catch (IOException e) {
			log.error("Error Adding Ancestors ", e);
		}
		finally {
			try {
				FileUtils.cleanDirectory(new File(snomedIndexFileDirectoryLocation));
				
			}
			catch (IOException e) {
				log.error("Error Adding Ancestors ", e);
			}
		}
		
	}
	
	private void addRelationshipsToSnomedCTTerms(String snomedFileDirectory) throws APIException {
		
		ConceptService cs = Context.getConceptService();
		
		ConceptManagementAppsProperties cmap = new ConceptManagementAppsProperties();
		String snomedSourceUuid = cmap
		        .getSnomedCTConceptSourceUuidGlobalProperty(ConceptManagementAppsConstants.SNOMED_CT_CONCEPT_SOURCE_UUID_GP);
		
		ConceptSource snomedSource = cs.getConceptSourceByUuid(snomedSourceUuid);
		ConceptMapType snomedMapType = cs
		        .getConceptMapTypeByUuid(ConceptManagementAppsConstants.SAME_AS_CONCEPT_MAP_TYPE_UUID);
		
		List<ConceptReferenceTerm> listOfMappedTerms = new ArrayList<ConceptReferenceTerm>();
		
		try {
			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(snomedIndexFileDirectoryLocation)));
			IndexSearcher searcher = new IndexSearcher(reader);
			
			List<ConceptReferenceTerm> sourceRefTermsNew = getConceptReferenceTerms(snomedSource, 0, -1, "code", 1);
			listOfMappedTerms = createNewMappings(sourceRefTermsNew, searcher, snomedMapType);
			if (listOfMappedTerms != null) {
				saveNewOrUpdatedRefTerms(listOfMappedTerms);
			}
			
			reader.close();
		}
		catch (IOException e) {
			
		}
		finally {
			try {
				FileUtils.cleanDirectory(new File(snomedIndexFileDirectoryLocation));
				
			}
			catch (IOException e) {
				log.error("Error Adding Parents ", e);
			}
		}
		
	}
	
	private void addNamesToSnomedCTTerms(String snomedFileDirectory) throws APIException {
		
		ConceptService cs = Context.getConceptService();
		
		ConceptManagementAppsProperties cmap = new ConceptManagementAppsProperties();
		String snomedSourceUuid = cmap
		        .getSnomedCTConceptSourceUuidGlobalProperty(ConceptManagementAppsConstants.SNOMED_CT_CONCEPT_SOURCE_UUID_GP);
		
		ConceptSource snomedSource = cs.getConceptSourceByUuid(snomedSourceUuid);
		
		List<ConceptReferenceTerm> sourceRefTerms = getConceptReferenceTerms(snomedSource, 0, -1, "code", 1);
		List<ConceptReferenceTerm> listOfUpdatedTerms = new ArrayList<ConceptReferenceTerm>();
		
		try {
			
			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(snomedIndexFileDirectoryLocation)));
			IndexSearcher searcher = new IndexSearcher(reader);
			
			listOfUpdatedTerms = addNamesToAllReferenceTerms(sourceRefTerms, searcher);
			if (listOfUpdatedTerms != null) {
				saveNewOrUpdatedRefTerms(listOfUpdatedTerms);
			}
			
			reader.close();
		}
		catch (IOException e) {
			
			log.error("Error Adding Names ", e);
		}
		finally {
			try {
				FileUtils.cleanDirectory(new File(snomedIndexFileDirectoryLocation));
				
			}
			catch (IOException e) {
				log.error("Error Adding Names ", e);
			}
		}
		
	}
	
	private void saveNewOrUpdatedRefTerms(List<ConceptReferenceTerm> listOfTerms) {
		
		int batchSize = 0;
		
		currentSnomedCTProcess.setCurrentManageSnomedCTProcessNumToProcess(listOfTerms.size());
		
		for (ConceptReferenceTerm termToSave : listOfTerms) {
			if (!getCancelManageSnomedCTProcess()) {
				ConceptService cs = Context.getConceptService();
				cs.saveConceptReferenceTerm(termToSave);
				
				batchSize++;
				
				currentSnomedCTProcess.setCurrentManageSnomedCTProcessNumProcessed(batchSize);
				
				if (batchSize % 20 == 0) {
					
					Context.flushSession();
					
				}
			} else {
				return;
			}
		}
		
	}
	
	private void indexSnomedFiles(String snomedFiles) {
		if (!getCancelManageSnomedCTProcess()) {
			try {
				
				File file = new File(snomedFiles);
				FSDirectory dir = FSDirectory.open(new File(snomedIndexFileDirectoryLocation));
				IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_44, analyzer);
				
				for (File f : file.listFiles()) {
					
					IndexWriter writer = new IndexWriter(dir, config);
					
					if (StringUtils.equalsIgnoreCase(f.getName(), RELATIONSHIP_FILE)) {
						
						BufferedReader br = new BufferedReader(new FileReader(f));
						
						for (String line = br.readLine(); line != null; line = br.readLine()) {
							
							String[] fileFields = line.split("\t");
							
							if (fileFields[0].length() > 0) {
								
							}
							//is term active and an IS-A relationship
							if (StringUtils.equalsIgnoreCase(fileFields[2], RETIRED_TERM)
							        && StringUtils.equalsIgnoreCase(fileFields[7], IS_A_RELATIONSHIP)) {
								
								Document doc = new Document();
								
								//get the row id, the parent term, and the child term
								doc.add(new StringField(ROW_ID, fileFields[0], Field.Store.YES));
								doc.add(new StringField(PARENT_TERM, fileFields[4], Field.Store.YES));
								doc.add(new StringField(CHILD_TERM, fileFields[5], Field.Store.YES));
								writer.addDocument(doc);
								
							}
						}
						br.close();
					}
					if (StringUtils.equalsIgnoreCase(f.getName(), DESCRIPTION_FILE)) {
						
						BufferedReader br = new BufferedReader(new FileReader(f));
						
						for (String line = br.readLine(); line != null; line = br.readLine()) {
							
							String[] fileFields = line.split("\t");
							
							//is the term active
							if (fileFields[0].length() > 0) {
								if (StringUtils.equalsIgnoreCase(fileFields[2], RETIRED_TERM)) {
									
									Document doc = new Document();
									
									//get the term id, the name, and the effective date(effective date for finding out if it is the newest)
									doc.add(new StringField(TERM_ID, fileFields[4], Field.Store.YES));
									doc.add(new StringField(TERM_NAME, fileFields[7], Field.Store.YES));
									doc.add(new StringField(EFFECTIVE_DATE, fileFields[1], Field.Store.YES));
									writer.addDocument(doc);
									
								}
							}
							
						}
						br.close();
					}
					
					writer.close();
				}
			}
			catch (FileNotFoundException e) {
				log.error("Error Indexing Snomed Files ", e);
			}
			catch (IOException e) {
				log.error("Error Indexing Snomed Files ", e);
			}
			catch (Exception e) {
				log.error("Error Indexing Snomed Files ", e);
			}
		} else {
			return;
		}
	}
	
	private Set<Integer> searchIndexesGetAncestorTermIds(String termId, Set<Long> listOfNewTermIds,
	                                                     Set<Integer> listOfDocIds, ConceptSource conceptSource,
	                                                     IndexSearcher searcher) {
		try {
			
			TopScoreDocCollector sourceIdCollector = TopScoreDocCollector.create(1000, true);
			Query sourceIdQuery = new QueryParser(Version.LUCENE_44, PARENT_TERM, analyzer).parse(termId);
			searcher.search(sourceIdQuery, sourceIdCollector);
			ScoreDoc[] hits = sourceIdCollector.topDocs().scoreDocs;
			
			for (int i = 0; i < hits.length; ++i) {
				if (!getCancelManageSnomedCTProcess()) {
					int docId = hits[i].doc;
					Document d = searcher.doc(docId);
					
					Long id = Long.valueOf(d.get(ROW_ID)).longValue();
					String childIdString = d.get(CHILD_TERM);
					
					int listSizeBefore = listOfNewTermIds.size();
					listOfNewTermIds.add(id);
					listOfDocIds.add(docId);
					int listSizeAfter = listOfNewTermIds.size();
					if (listSizeAfter > listSizeBefore) {
						
						searchIndexesGetAncestorTermIds(childIdString, listOfNewTermIds, listOfDocIds, conceptSource,
						    searcher);
						
					}
				} else {
					return null;
				}
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return listOfDocIds;
		
	}
	
	private Set<ConceptReferenceTerm> createNewTerms(Set<Integer> listOfDocIds, IndexSearcher searcher,
	                                                 ConceptSource conceptSource, Set<Integer> listOfExistingIds) {
		
		Set<ConceptReferenceTerm> listOfNewTerms = new HashSet<ConceptReferenceTerm>();
		Set<String> listOfAlreadyAddedTerms = new HashSet<String>();
		
		for (Integer docId : listOfDocIds) {
			if (!getCancelManageSnomedCTProcess()) {
				Document termIds;
				try {
					termIds = searcher.doc(docId.intValue());
					
					String termCode = termIds.get(PARENT_TERM);
					
					int beforeSize = listOfAlreadyAddedTerms.size();
					listOfAlreadyAddedTerms.add(termIds.get(PARENT_TERM));
					int afterSize = listOfAlreadyAddedTerms.size();
					
					if (!listOfExistingIds.contains(termIds.get(PARENT_TERM).hashCode()) && beforeSize < afterSize) {
						ConceptReferenceTerm newChildTerm = new ConceptReferenceTerm();
						
						newChildTerm.setCode(termCode);
						newChildTerm.setConceptSource(conceptSource);
						newChildTerm = addNameToReferenceTerm(newChildTerm, searcher);
						
						listOfNewTerms.add(newChildTerm);
						
					}
					
					String childTermCode = termIds.get(CHILD_TERM);
					
					beforeSize = listOfAlreadyAddedTerms.size();
					listOfAlreadyAddedTerms.add(termIds.get(CHILD_TERM));
					afterSize = listOfAlreadyAddedTerms.size();
					
					if (!listOfExistingIds.contains(termIds.get(CHILD_TERM).hashCode()) && beforeSize < afterSize) {
						ConceptReferenceTerm newChildTerm = new ConceptReferenceTerm();
						
						newChildTerm.setCode(childTermCode);
						newChildTerm.setConceptSource(conceptSource);
						newChildTerm = addNameToReferenceTerm(newChildTerm, searcher);
						
						listOfNewTerms.add(newChildTerm);
					}
					
				}
				catch (IOException e) {
					log.error("Error Creating New Terms ", e);
				}
			} else {
				return null;
			}
		}
		return listOfNewTerms;
		
	}
	
	private Map<String, ConceptReferenceTerm> createConceptReferenceTermCodeHashMap(List<ConceptReferenceTerm> listOfExistingTerms) {
		
		Map<String, ConceptReferenceTerm> hashMapOfExistingTerms = new HashMap<String, ConceptReferenceTerm>();
		for (ConceptReferenceTerm term : listOfExistingTerms) {
			
			hashMapOfExistingTerms.put(term.getCode(), term);
			
		}
		
		return hashMapOfExistingTerms;
		
	}
	
	private List<ConceptReferenceTerm> createNewMappings(List<ConceptReferenceTerm> listOfExistingTerms,
	                                                     IndexSearcher searcher, ConceptMapType mapType) {
		
		List<ConceptReferenceTerm> listOfTermsWithNewMappings = new ArrayList<ConceptReferenceTerm>();
		Map<String, ConceptReferenceTerm> termHashMap = createConceptReferenceTermCodeHashMap(listOfExistingTerms);
		
		try {
			
			for (ConceptReferenceTerm term : listOfExistingTerms) {
				if (!getCancelManageSnomedCTProcess()) {
					boolean mapAdded = false;
					Set<Long> listOfTermIdsAlreadyMapped = new HashSet<Long>();
					
					TopScoreDocCollector sourceIdCollector = TopScoreDocCollector.create(1000, true);
					Query sourceIdQuery = new QueryParser(Version.LUCENE_44, PARENT_TERM, analyzer).parse(term.getCode());
					searcher.search(sourceIdQuery, sourceIdCollector);
					ScoreDoc[] hits = sourceIdCollector.topDocs().scoreDocs;
					
					for (int i = 0; i < hits.length; ++i) {
						
						int docId = hits[i].doc;
						Document termIds = searcher.doc(docId);
						
						int beforeSize = listOfTermIdsAlreadyMapped.size();
						listOfTermIdsAlreadyMapped.add(Long.parseLong(termIds.get(CHILD_TERM)));
						int afterSize = listOfTermIdsAlreadyMapped.size();
						
						if (beforeSize < afterSize) {
							ConceptReferenceTerm childTerm = termHashMap.get(termIds.get(CHILD_TERM));
							ConceptReferenceTermMap newMap = new ConceptReferenceTermMap();
							
							newMap.setConceptMapType(mapType);
							newMap.setTermA(term);
							newMap.setTermB(childTerm);
							
							term.addConceptReferenceTermMap(newMap);
							mapAdded = true;
						}
					}
					if (mapAdded) {
						listOfTermsWithNewMappings.add(term);
					}
					
				} else {
					return null;
				}
			}
		}
		
		catch (Exception e) {
			log.error("Error Creating New Mappings ", e);
		}
		return listOfTermsWithNewMappings;
		
	}
	
	private ConceptReferenceTerm addNameToReferenceTerm(ConceptReferenceTerm term, IndexSearcher searcher) {
		
		if (!getCancelManageSnomedCTProcess()) {
			TopScoreDocCollector termCollector = TopScoreDocCollector.create(1000, true);
			String currentTermWithName = null;
			Document currentTermDoc = null;
			
			try {
				Query termQuery = new QueryParser(Version.LUCENE_44, TERM_ID, analyzer).parse(term.getCode());
				
				if (termQuery != null) {
					
					searcher.search(termQuery, termCollector);
					ScoreDoc[] termHits = termCollector.topDocs().scoreDocs;
					
					if (searcher != null && termHits.length > 0) {
						for (int i = 0; i < termHits.length; ++i) {
							
							int docId = termHits[i].doc;
							Document d = searcher.doc(docId);
							
							if (currentTermDoc == null) {
								
								currentTermDoc = searcher.doc(docId);
								currentTermWithName = currentTermDoc.get(TERM_NAME);
								
							} else {
								
								if (Integer.parseInt(d.get(EFFECTIVE_DATE)) > Integer.parseInt(currentTermDoc
								        .get(EFFECTIVE_DATE))) {
									
									currentTermDoc = d;
									currentTermWithName = d.get(TERM_NAME);
									
								}
							}
							
						}
						term.setName(currentTermWithName);
					}
				}
			}
			catch (org.apache.lucene.queryparser.classic.ParseException e) {
				log.error("Lucene Error Adding Names To Reference Term ", e);
			}
			catch (IOException e) {
				log.error("Error Adding Names To Reference Term ", e);
			}
			return term;
			
		} else {
			return null;
		}
	}
	
	private List<ConceptReferenceTerm> addNamesToAllReferenceTerms(List<ConceptReferenceTerm> terms, IndexSearcher searcher) {
		List<ConceptReferenceTerm> namedTerms = new ArrayList<ConceptReferenceTerm>();
		
		try {
			for (ConceptReferenceTerm term : terms) {
				if (!getCancelManageSnomedCTProcess()) {
					String currentTermWithName = null;
					Document currentTermDoc = null;
					
					Query termQuery = new QueryParser(Version.LUCENE_44, TERM_ID, analyzer).parse(term.getCode());
					
					if (termQuery != null) {
						TopScoreDocCollector termCollector = TopScoreDocCollector.create(1000, true);
						searcher.search(termQuery, termCollector);
						ScoreDoc[] termHits = termCollector.topDocs().scoreDocs;
						
						if (searcher != null && termHits.length > 0) {
							for (int i = 0; i < termHits.length; ++i) {
								
								int docId = termHits[i].doc;
								Document d = searcher.doc(docId);
								
								if (currentTermDoc == null) {
									
									currentTermDoc = searcher.doc(docId);
									currentTermWithName = currentTermDoc.get(TERM_NAME);
									
								} else {
									
									if (Integer.parseInt(d.get(EFFECTIVE_DATE)) > Integer.parseInt(currentTermDoc
									        .get(EFFECTIVE_DATE))) {
										
										currentTermDoc = d;
										currentTermWithName = d.get(TERM_NAME);
										
									}
								}
								
							}
							
							term.setName(currentTermWithName);
							namedTerms.add(term);
						}
					}
					
				} else {
					return null;
				}
			}
			
		}
		catch (org.apache.lucene.queryparser.classic.ParseException e) {
			log.error("Adding Names To All Reference Terms ", e);
		}
		catch (IOException e) {
			log.error("Error Adding Names To All Reference Terms ", e);
		}
		return namedTerms;
		
	}
	
	/**
	 * Sets up the processors used for the spreadsheet to download.
	 * 
	 * @return the cell processors
	 */
	private static CellProcessor[] getSpreadsheetProcessors() {
		
		final CellProcessor[] processors = new CellProcessor[] { new Optional(), // map type
		        new Optional(), // source name
		        new Optional(), // source code
		        new Optional(), // concept id
		        new Optional(), // concept uuid
		        new Optional(), // preferred name
		        new Optional(), // description
		        new Optional(), // class
		        new Optional(), // datatype
		        new Optional() // all existing mappings
		};
		
		return processors;
	}
	
	private String getInitialErrorsBeforeTryingToSaveConcept(Map<String, Object> mapList) {
		String errorString = "";
		ConceptService cs = Context.getConceptService();
		if (isMapTypeNull(mapList)) {
			errorString = " " + errorString
			        + Context.getMessageSourceService().getMessage("conceptmanagementapps.file.maptype.error") + " ";
		}
		
		if (isSourceNameNull(mapList)) {
			errorString = " " + errorString
			        + Context.getMessageSourceService().getMessage("conceptmanagementapps.file.sourcename.error") + " ";
			
		}
		
		if (isSourceCodeNull(mapList)) {
			errorString = " " + errorString
			        + Context.getMessageSourceService().getMessage("conceptmanagementapps.file.sourcecode.error") + " ";
			
		}
		
		if (!isSourceNameNull(mapList)
		        && !isSourceCodeNull(mapList)
		        && cs.getConceptReferenceTermByCode((String) mapList.get("source code"),
		            cs.getConceptSourceByName((String) mapList.get("source name"))) == null) {
			errorString = " "
			        + errorString
			        + Context.getMessageSourceService().getMessage(
			            "conceptmanagementapps.file.getConceptReferenceTermByCode.error") + " ";
			
		}
		if (!isSourceNameNull(mapList) && cs.getConceptSourceByName((String) mapList.get("source name")) == null) {
			
			errorString = " "
			        + errorString
			        + Context.getMessageSourceService()
			                .getMessage("conceptmanagementapps.file.getConceptSourceByName.error") + " ";
		}
		
		if (!isMapTypeNull(mapList) && cs.getConceptMapTypeByName(((String) mapList.get("map type"))) == null) {
			errorString = " "
			        + errorString
			        + Context.getMessageSourceService().getMessage(
			            "conceptmanagementapps.file.getConceptMapTypeByName.error") + " ";
			
		}
		
		if (cs.getConcept(((String) mapList.get("concept Id"))) == null) {
			errorString = " " + errorString
			        + Context.getMessageSourceService().getMessage("conceptmanagementapps.file.getConcept.error") + " ";
			
		}
		
		return errorString;
	}
	
	private void setMapAndSaveConcept(MultipartFile spreadsheetFile) {
		ConceptService cs = Context.getConceptService();
		ICsvMapReader mapReader = null;
		try {
			
			mapReader = new CsvMapReader(new InputStreamReader(spreadsheetFile.getInputStream()),
			        CsvPreference.STANDARD_PREFERENCE);
			
			final String[] header = mapReader.getHeader(true);
			final CellProcessor[] processors = getSpreadsheetProcessors();
			
			for (Map<String, Object> mapList = mapReader.read(header, processors); mapList != null; mapList = mapReader
			        .read(header, processors)) {
				
				ConceptMap conceptMap = new ConceptMap();
				Collection<ConceptMap> conceptMappings;
				
				ConceptReferenceTerm refTerm = cs.getConceptReferenceTermByCode((String) mapList.get("source code"),
				    cs.getConceptSourceByName((String) mapList.get("source name")));
				
				conceptMap.setConceptReferenceTerm(refTerm);
				
				conceptMap.setConceptMapType(cs.getConceptMapTypeByName((String) mapList.get("map type")));
				
				if (cs.getConcept(((String) mapList.get("concept Id"))) != null) {
					
					Concept concept = cs.getConcept(((String) mapList.get("concept Id")));
					
					//see if concept has mappings we need to add to
					if (concept.getConceptMappings() == null) {
						
						List<ConceptMap> conceptMappingsList = new ArrayList<ConceptMap>();
						conceptMappingsList.add(conceptMap);
						concept.setConceptMappings(conceptMappingsList);
						
					} else {
						
						conceptMappings = concept.getConceptMappings();
						conceptMappings.add(conceptMap);
						concept.setConceptMappings(conceptMappings);
						
					}
					
					cs.saveConcept(concept);
					
				}
			}
		}
		
		catch (APIException e) {
			e.printStackTrace();
			throw new APIException("error on row " + mapReader.getRowNumber() + "," + mapReader.getUntokenizedRow() + e);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		finally {
			
			if (mapReader != null) {
				
				try {
					mapReader.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	/**
	 * @return boolean for if sourcName is null or empty
	 */
	private boolean isSourceNameNull(Map<String, Object> mapList) {
		String sourceName = "";
		if (mapList.get("source name") == null) {
			return true;
		} else {
			sourceName = (String) mapList.get("source name");
		}
		if (StringUtils.isNotEmpty(sourceName) && StringUtils.isNotBlank(sourceName)) {
			return false;
		}
		return true;
	}
	
	/**
	 * @return boolean for if sourceCode is null or empty
	 */
	private boolean isSourceCodeNull(Map<String, Object> mapList) {
		String sourceCode = "";
		if (mapList.get("source code") == null) {
			return true;
		} else {
			sourceCode = (String) mapList.get("source code");
		}
		if (StringUtils.isNotEmpty(sourceCode) && StringUtils.isNotBlank(sourceCode)) {
			return false;
		}
		return true;
	}
	
	/**
	 * @return boolean for if mapType is null or empty
	 */
	private boolean isMapTypeNull(Map<String, Object> mapList) {
		String mapType = "";
		if (mapList.get("map type") == null) {
			return true;
		} else {
			mapType = (String) mapList.get("map type");
		}
		if (StringUtils.isNotEmpty(mapType) && StringUtils.isNotBlank(mapType)) {
			return false;
		}
		return true;
	}
	
}
