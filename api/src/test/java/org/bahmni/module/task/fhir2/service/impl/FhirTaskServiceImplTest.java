/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) Thoughtworks Inc.
 */
package org.bahmni.module.task.fhir2.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.bahmni.module.task.api.domain.Task;
import org.bahmni.module.task.fhir2.dao.FhirTaskDao;
import org.bahmni.module.task.fhir2.translators.TaskTranslator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.search.param.TaskSearchParams;
import org.openmrs.module.fhir2.api.translators.UpdatableOpenmrsTranslator;

@ExtendWith(MockitoExtension.class)
class FhirTaskServiceImplTest {
	
	@Mock
	private FhirTaskDao dao;
	
	@Mock
	private TaskTranslator translator;
	
	@Mock
	private SearchQueryInclude<org.hl7.fhir.r4.model.Task> searchQueryInclude;
	
	@Mock
	private SearchQuery<Task, org.hl7.fhir.r4.model.Task, FhirTaskDao, TaskTranslator, SearchQueryInclude<org.hl7.fhir.r4.model.Task>> searchQuery;
	
	@Mock
	private IBundleProvider bundleProvider;
	
	private FhirTaskServiceImpl service;
	
	@BeforeEach
	void setUp() {
		service = new FhirTaskServiceImpl(dao, translator, searchQueryInclude, searchQuery);
	}
	
	@Test
	void getDaoAndTranslatorShouldReturnInjectedBeans() {
		assertSame(dao, service.getDao());
		assertSame(translator, service.getTranslator());
	}
	
	/**
	 * BaseFhirService.applyUpdate only merges into the existing entity when the translator is an
	 * UpdatableOpenmrsTranslator; otherwise it builds a fresh transient Task and saveOrUpdate inserts a
	 * duplicate row. Guard that contract here, since it is invisible until runtime.
	 */
	@Test
	void translatorMustSupportInPlaceUpdates() {
		assertTrue(UpdatableOpenmrsTranslator.class.isAssignableFrom(TaskTranslator.class),
		    "TaskTranslator must extend UpdatableOpenmrsTranslator so PUT/PATCH merge in place");
	}
	
	@Test
	void searchForTasksShouldDelegateToSearchQuery() {
		TaskSearchParams params = new TaskSearchParams();
		when(searchQuery.getQueryResults(any(SearchParameterMap.class), eq(dao), eq(translator), eq(searchQueryInclude)))
		        .thenReturn(bundleProvider);
		
		IBundleProvider result = service.searchForTasks(params);
		
		assertSame(bundleProvider, result);
		verify(searchQuery).getQueryResults(any(SearchParameterMap.class), eq(dao), eq(translator), eq(searchQueryInclude));
	}
}
