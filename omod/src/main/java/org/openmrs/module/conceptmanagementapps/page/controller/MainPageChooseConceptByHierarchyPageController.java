package org.openmrs.module.conceptmanagementapps.page.controller;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptSearchResult;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class MainPageChooseConceptByHierarchyPageController {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	public void controller(UiUtils ui,
                           @RequestParam(value = "existingDiagnoses", required = false) Diagnosis[] existingDiagnoses1,
                           PageModel model) throws Exception {

        // to ensure we have the exact same json format as search results, borrow the simplify method from here


                           
        

        model.addAttribute("jsForExisting", "this is a test");
    }

	public SimpleObject simplify(ConceptSearchResult result, UiUtils ui, Locale locale) throws Exception {
        SimpleObject simple = SimpleObject.fromObject(result, ui, "word", "conceptName.id", "conceptName.conceptNameType", "conceptName.name", "concept.id", "concept.conceptMappings.conceptMapType", "concept.conceptMappings.conceptReferenceTerm.code", "concept.conceptMappings.conceptReferenceTerm.name", "concept.conceptMappings.conceptReferenceTerm.conceptSource.name");

        Concept concept = result.getConcept();
        ConceptName conceptName = result.getConceptName();
        ConceptName preferredName = concept.getPreferredName(locale);
        PropertyUtils.setProperty(simple, "concept.preferredName", preferredName.getName());

        return simple;
    }

}