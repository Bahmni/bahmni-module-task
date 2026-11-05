/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.bahmni.module.task.fhir2.service.impl;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.bahmni.module.task.api.domain.Task;
import org.bahmni.module.task.fhir2.dao.FhirTaskDao;
import org.bahmni.module.task.fhir2.service.FhirTaskService;
import org.bahmni.module.task.fhir2.translators.TaskTranslator;
import org.openmrs.module.fhir2.api.impl.BaseFhirService;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.TaskSearchParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class FhirTaskServiceImpl extends BaseFhirService<org.hl7.fhir.r4.model.Task, Task> implements FhirTaskService {
	
	private final FhirTaskDao dao;
	
	private final TaskTranslator translator;
	
	private final SearchQueryInclude<org.hl7.fhir.r4.model.Task> searchQueryInclude;
	
	private final SearchQuery<Task, org.hl7.fhir.r4.model.Task, FhirTaskDao, TaskTranslator, SearchQueryInclude<org.hl7.fhir.r4.model.Task>> searchQuery;
	
	@Autowired
	public FhirTaskServiceImpl(FhirTaskDao dao, TaskTranslator translator,
	    SearchQueryInclude<org.hl7.fhir.r4.model.Task> searchQueryInclude,
	    SearchQuery<Task, org.hl7.fhir.r4.model.Task, FhirTaskDao, TaskTranslator, SearchQueryInclude<org.hl7.fhir.r4.model.Task>> searchQuery) {
		this.dao = dao;
		this.translator = translator;
		this.searchQueryInclude = searchQueryInclude;
		this.searchQuery = searchQuery;
	}
	
	@Override
	protected FhirTaskDao getDao() {
		return dao;
	}
	
	@Override
	protected TaskTranslator getTranslator() {
		return translator;
	}
	
	@Override
	public IBundleProvider searchForTasks(TaskSearchParams params) {
		return searchQuery.getQueryResults(params.toSearchParameterMap(), dao, translator, searchQueryInclude);
	}
}
