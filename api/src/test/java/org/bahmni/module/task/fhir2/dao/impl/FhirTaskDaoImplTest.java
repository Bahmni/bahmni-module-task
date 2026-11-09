/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) Thoughtworks Inc.
 */
package org.bahmni.module.task.fhir2.dao.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

@ExtendWith(MockitoExtension.class)
class FhirTaskDaoImplTest {
	
	@Mock
	private Criteria criteria;
	
	private FhirTaskDaoImpl dao;
	
	@BeforeEach
	void setUp() {
		dao = new FhirTaskDaoImpl();
		lenient().when(criteria.createAlias(anyString(), anyString())).thenReturn(criteria);
		lenient().when(criteria.add(any(Criterion.class))).thenReturn(criteria);
	}
	
	private static TokenAndListParam token(String value) {
		return new TokenAndListParam().addAnd(new TokenOrListParam().add(new TokenParam(value)));
	}
	
	private static ReferenceAndListParam reference(String id) {
		return new ReferenceAndListParam().addAnd(new ReferenceOrListParam().add(new ReferenceParam(id)));
	}
	
	@Test
	void shouldAddStatusCriterionForKnownStatus() {
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.STATUS_SEARCH_HANDLER,
		    token("requested"));
		
		dao.setupSearchParams(criteria, params);
		
		verify(criteria).add(any(Criterion.class));
	}
	
	@Test
	void shouldNotThrowOnUnknownStatusCode() {
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.STATUS_SEARCH_HANDLER,
		    token("not-a-real-status"));
		
		assertDoesNotThrow(() -> dao.setupSearchParams(criteria, params));
	}
	
	@Test
	void shouldJoinOwnerByUuid() {
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.OWNER_REFERENCE_SEARCH_HANDLER,
		    reference("owner-uuid"));
		
		dao.setupSearchParams(criteria, params);
		
		verify(criteria).createAlias("owner", "ow");
		verify(criteria).add(any(Criterion.class));
	}
	
	@Test
	void shouldJoinBasedOnOrderByUuid() {
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.BASED_ON_REFERENCE_SEARCH_HANDLER,
		    reference("order-uuid"));
		
		dao.setupSearchParams(criteria, params);
		
		verify(criteria).createAlias("order", "ord");
	}
	
	@Test
	void shouldJoinTaskCodeConcept() {
		SearchParameterMap params = new SearchParameterMap().addParameter(FhirConstants.TASK_CODE_SEARCH_HANDLER,
		    token("CT"));
		
		dao.setupSearchParams(criteria, params);
		
		verify(criteria).createAlias("taskCode", "tc");
	}
}
