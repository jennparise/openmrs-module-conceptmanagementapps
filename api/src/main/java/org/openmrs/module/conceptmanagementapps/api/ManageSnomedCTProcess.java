package org.openmrs.module.conceptmanagementapps.api;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openmrs.api.ConceptService;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.conceptmanagementapps.api.ConceptManagementAppsService;
import org.openmrs.module.conceptmanagementapps.api.impl.ConceptManagementAppsServiceImpl;
import org.springframework.transaction.annotation.Transactional;

public class ManageSnomedCTProcess {
	
	private String currentManageSnomedCTProcessName = "none";
	
	private String currentManageSnomedCTProcessStatus = "";
	
	private Date currentManageSnomedCTProcessStartTime = new Date();
	
	private String currentManageSnomedCTProcessDirectoryLocation = "";
	
	private int currentManageSnomedCTProcessNumProcessed = 0;
	
	private int currentManageSnomedCTProcessNumToProcess = 0;
	
	private long currentManageSnomedCTProcessTimeStartedMilliSecs = 0;
	
	
	
	public ManageSnomedCTProcess(String processName) {
		
		
		Date date = new Date();
		
		setCurrentManageSnomedCTProcessName(processName);
		setCurrentManageSnomedCTProcessStatus("starting");
		setCurrentManageSnomedCTProcessStartTime(date);
		setCurrentManageSnomedCTProcessTimeStartedMilliSecs(System.currentTimeMillis());
	}
	
	/**
	 * @param currentManageSnomedCTProcess the currentManageSnomedCTProcess to set
	 */
	public void setCurrentManageSnomedCTProcessName(String currentManageSnomedCTProcessName) {
		this.currentManageSnomedCTProcessName = currentManageSnomedCTProcessName;
	}
	
	/**
	 * @return currentManageSnomedCTProcess
	 */
	public String getCurrentManageSnomedCTProcessName() {
		return currentManageSnomedCTProcessName;
	}
	
	/**
	 * @param currentManageSnomedCTProcessTimeStartedMilliSecsthe currentManageSnomedCTProcessTimeStartedMilliSecs to set
	 */
	public void setCurrentManageSnomedCTProcessTimeStartedMilliSecs(long currentManageSnomedCTProcessTimeStartedMilliSecs) {
		this.currentManageSnomedCTProcessTimeStartedMilliSecs = currentManageSnomedCTProcessTimeStartedMilliSecs;
	}
	
	/**
	 * @return currentManageSnomedCTProcessTimeStartedMilliSecs
	 */
	public long getCurrentManageSnomedCTProcessTimeStartedMilliSecs() {
		return currentManageSnomedCTProcessTimeStartedMilliSecs;
	}
	
	/**
	 * @param currentManageSnomedCTProcessStatus the currentManageSnomedCTProcessStatus to set
	 */
	public void setCurrentManageSnomedCTProcessStatus(String currentManageSnomedCTProcessStatus) {
		this.currentManageSnomedCTProcessStatus = currentManageSnomedCTProcessStatus;
	}
	
	/**
	 * @return currentManageSnomedCTProcessStatus
	 */
	public String getCurrentManageSnomedCTProcessStatus() {
		return currentManageSnomedCTProcessStatus;
	}
	
	/**
	 * @param currentManageSnomedCTProcessStartTime the currentManageSnomedCTProcessStartTime to set
	 */
	public void setCurrentManageSnomedCTProcessStartTime(Date currentManageSnomedCTProcessStartTime) {
		this.currentManageSnomedCTProcessStartTime = currentManageSnomedCTProcessStartTime;
	}
	
	/**
	 * @return currentManageSnomedCTProcessStartTime
	 */
	public Date getCurrentManageSnomedCTProcessStartTime() {
		return currentManageSnomedCTProcessStartTime;
	}
	
	/**
	 * @param currentManageSnomedCTProcessDirectoryLocation the
	 *            currentManageSnomedCTProcessDirectoryLocation to set
	 */
	public void setCurrentManageSnomedCTProcessDirectoryLocation(String currentManageSnomedCTProcessDirectoryLocation) {
		this.currentManageSnomedCTProcessDirectoryLocation = currentManageSnomedCTProcessDirectoryLocation;
	}
	
	/**
	 * @return currentManageSnomedCTProcessDirectoryLocation
	 */
	public String getCurrentManageSnomedCTProcessDirectoryLocation() {
		return currentManageSnomedCTProcessDirectoryLocation;
	}
	
	/**
	 * @param currentManageSnomedCTProcessNumProcessed the currentManageSnomedCTProcessNumProcessed
	 *            to set
	 */
	public void setCurrentManageSnomedCTProcessNumProcessed(int currentManageSnomedCTProcessNumProcessed) {
		this.currentManageSnomedCTProcessNumProcessed = currentManageSnomedCTProcessNumProcessed;
	}
	
	/**
	 * @return currentManageSnomedCTProcessNumProcessed
	 */
	public int getCurrentManageSnomedCTProcessNumProcessed() {
		return currentManageSnomedCTProcessNumProcessed;
	}
	
	/**
	 * @param currentManageSnomedCTProcessNumToProcess the currentManageSnomedCTProcessNumToProcess
	 *            to set
	 */
	public void setCurrentManageSnomedCTProcessNumToProcess(int currentManageSnomedCTProcessNumToProcess) {
		this.currentManageSnomedCTProcessNumToProcess = currentManageSnomedCTProcessNumToProcess;
	}
	
	/**
	 * @return currentManageSnomedCTProcessNumToProcess
	 */
	public int getCurrentManageSnomedCTProcessNumToProcess() {
		return currentManageSnomedCTProcessNumToProcess;
	}
	
}
