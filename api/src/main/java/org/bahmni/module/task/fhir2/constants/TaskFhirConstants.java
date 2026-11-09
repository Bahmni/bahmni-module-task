/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) Thoughtworks Inc.
 */
package org.bahmni.module.task.fhir2.constants;

/**
 * Task-specific FHIR search constants. These were previously borrowed from {@code
 * org.bahmni.module.fhir2addlextension.api.BahmniFhirConstants}, which declares ~35 constants
 * shared across many unrelated FHIR resources; only the two actually used by this module's Task
 * search are duplicated here so the Task module has no dependency on fhir2addlextension.
 */
public class TaskFhirConstants {
	
	public static final String NAME_SEARCH_HANDLER = "task.name.search.handler";
	
	public static final String SP_TASK_NAME = "name";
	
	private TaskFhirConstants() {
	}
}
