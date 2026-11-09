/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) Thoughtworks Inc.
 */
package org.bahmni.module.task.fhir2.search;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bahmni.module.task.fhir2.constants.TaskFhirConstants;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

/**
 * Adds {@code encounterReference} and a custom {@code name} parameter on top of upstream fhir2's
 * own {@link org.openmrs.module.fhir2.api.search.param.TaskSearchParams}, ported from Bahmni's
 * {@code BahmniTaskSearchParams} so this module no longer depends on fhir2addlextension.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TaskSearchParams extends org.openmrs.module.fhir2.api.search.param.TaskSearchParams {
	
	private ReferenceAndListParam encounterReference;
	
	private StringAndListParam name;
	
	@Override
	public SearchParameterMap toSearchParameterMap() {
		SearchParameterMap map = super.toSearchParameterMap();
		if (encounterReference != null) {
			map.addParameter(FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER, encounterReference);
		}
		if (name != null) {
			map.addParameter(TaskFhirConstants.NAME_SEARCH_HANDLER, name);
		}
		return map;
	}
}
