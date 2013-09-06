package org.openmrs.module.conceptmanagementapps;

import java.util.LinkedHashMap;
import java.util.List;

public class DataObject extends LinkedHashMap<String, Object> {
	
	private static final long serialVersionUID = 1L;
	
	public DataObject() {
		super();
	} 
	
	/**
	 * Utility method to create a {@link DataObject} given a varargs style list of property names
	 * and values. The array passed in must have even length. Every other element (starting from the
	 * 0-index one) must be a String (representing a property name) and be followed by its value.
	 * 
	 * @param propertyNamesAndValues
	 * @return
	 */
	public static DataObject create(List<Object> propertyNamesAndValues) {
		DataObject ret = new DataObject();
		for (int i = 0; i < propertyNamesAndValues.size(); i += 2) {
			String prop = (String) propertyNamesAndValues.get(i);
			ret.put(prop, propertyNamesAndValues.get(i + 1));
		}
		return ret;
	}
	
}
