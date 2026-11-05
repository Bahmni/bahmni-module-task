/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.bahmni.module.task.fhir2.dao.impl;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;

import java.util.Optional;
import org.bahmni.module.task.api.domain.Task;
import org.bahmni.module.task.fhir2.dao.FhirTaskDao;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hl7.fhir.exceptions.FHIRException;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.impl.BaseFhirDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class FhirTaskDaoImpl extends BaseFhirDao<Task> implements FhirTaskDao {
	
	private static final String PATIENT_PROPERTY = "patient";
	
	private static final String OWNER_PROPERTY = "owner";
	
	private static final String OWNER_ALIAS = "ow";
	
	private static final String ORDER_PROPERTY = "order";
	
	private static final String ORDER_ALIAS = "ord";
	
	private static final String TASK_CODE_PROPERTY = "taskCode";
	
	private static final String TASK_CODE_ALIAS = "tc";
	
	private static final String TASK_CODE_CONCEPT_MAP_ALIAS = "tcm";
	
	private static final String TASK_CODE_CONCEPT_REFERENCE_TERM_ALIAS = "tcrt";
	
	private static final String STATUS_PROPERTY = "status";
	
	private static final String UUID_PROPERTY = "uuid";
	
	@Override
	protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.FOR_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handlePatientReference(criteria,
					    (ReferenceAndListParam) param.getParam(), PATIENT_PROPERTY));
					break;
				case FhirConstants.OWNER_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleUuidReference(criteria, (ReferenceAndListParam) param.getParam(),
					    OWNER_PROPERTY, OWNER_ALIAS));
					break;
				case FhirConstants.BASED_ON_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleUuidReference(criteria, (ReferenceAndListParam) param.getParam(),
					    ORDER_PROPERTY, ORDER_ALIAS));
					break;
				case FhirConstants.TASK_CODE_SEARCH_HANDLER:
					entry.getValue().forEach(param -> handleTaskCode(criteria, (TokenAndListParam) param.getParam()));
					break;
				case FhirConstants.STATUS_SEARCH_HANDLER:
					entry.getValue()
					        .forEach(param -> handleStatus((TokenAndListParam) param.getParam()).ifPresent(criteria::add));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(entry.getValue()).ifPresent(criteria::add);
					break;
				default:
					break;
			}
		});
	}
	
	private void handleUuidReference(Criteria criteria, ReferenceAndListParam reference, String property, String alias) {
		if (reference == null) {
			return;
		}
		criteria.createAlias(property, alias);
		handleAndListParam(reference, token -> Optional.of(Restrictions.eq(alias + "." + UUID_PROPERTY, token.getIdPart())))
		        .ifPresent(criteria::add);
	}
	
	private void handleTaskCode(Criteria criteria, TokenAndListParam code) {
		if (code == null) {
			return;
		}
		criteria.createAlias(TASK_CODE_PROPERTY, TASK_CODE_ALIAS);
		handleCodeableConcept(criteria, code, TASK_CODE_ALIAS, TASK_CODE_CONCEPT_MAP_ALIAS,
		    TASK_CODE_CONCEPT_REFERENCE_TERM_ALIAS).ifPresent(criteria::add);
	}
	
	private Optional<Criterion> handleStatus(TokenAndListParam status) {
		if (status == null) {
			return Optional.empty();
		}
		return handleAndListParam(status,
		    token -> toTaskStatus(token.getValue()).map(s -> Restrictions.eq(STATUS_PROPERTY, s)));
	}
	
	private Optional<Task.TaskStatus> toTaskStatus(String code) {
		try {
			// fromCode returns null for null/blank codes and throws FHIRException for unrecognized ones;
			// an unknown status simply yields no filter rather than failing the whole search.
			org.hl7.fhir.r4.model.Task.TaskStatus fhirStatus = org.hl7.fhir.r4.model.Task.TaskStatus.fromCode(code);
			return Optional.ofNullable(fhirStatus).map(s -> Task.TaskStatus.valueOf(s.name()));
		}
		catch (FHIRException | IllegalArgumentException e) {
			return Optional.empty();
		}
	}
	
}
