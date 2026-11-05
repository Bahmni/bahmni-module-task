/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.bahmni.module.task.fhir2.translators;

import javax.annotation.Nonnull;
import org.bahmni.module.task.api.domain.Task;
import org.openmrs.module.fhir2.api.translators.OpenmrsFhirUpdatableTranslator;

public interface TaskTranslator extends OpenmrsFhirUpdatableTranslator<Task, org.hl7.fhir.r4.model.Task> {
	
	@Override
	org.hl7.fhir.r4.model.Task toFhirResource(Task task);
	
	@Override
	Task toOpenmrsType(org.hl7.fhir.r4.model.Task task);
	
	@Override
	Task toOpenmrsType(@Nonnull Task existingObject, @Nonnull org.hl7.fhir.r4.model.Task resource);
}
