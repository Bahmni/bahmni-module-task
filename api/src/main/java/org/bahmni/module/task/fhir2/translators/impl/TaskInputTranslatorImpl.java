/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.bahmni.module.task.fhir2.translators.impl;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import javax.annotation.Nonnull;
import org.bahmni.module.task.api.domain.TaskInput;
import org.bahmni.module.task.fhir2.translators.TaskInputTranslator;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Type;
import org.openmrs.Concept;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("fhirTaskInputTranslator")
public class TaskInputTranslatorImpl implements TaskInputTranslator {
	
	private static final String ERROR_MESSAGE_INPUT_TYPE_REQUIRED = "Task.input.type must be a resolvable concept";
	
	private static final String ERROR_MESSAGE_UNSUPPORTED_VALUE_TYPE = "Only Task.input.valueString is supported, got ";
	
	private final ConceptTranslator conceptTranslator;
	
	@Autowired
	public TaskInputTranslatorImpl(ConceptTranslator conceptTranslator) {
		this.conceptTranslator = conceptTranslator;
	}
	
	@Override
	public Task.ParameterComponent toFhirResource(@Nonnull TaskInput taskInput) {
		Type value = translateValue(taskInput);
		if (value == null)
			return null;
		return new Task.ParameterComponent().setType(conceptTranslator.toFhirResource(taskInput.getInputType()))
		        .setValue(value);
	}
	
	protected Type translateValue(TaskInput taskInput) {
		if (taskInput.getValueString() == null)
			return null;
		return new StringType(taskInput.getValueString());
	}
	
	@Override
	public TaskInput toOpenmrsType(@Nonnull Task.ParameterComponent parameterComponent) {
		if (parameterComponent == null) {
			return null;
		}
		
		Concept inputType = parameterComponent.hasType() ? conceptTranslator.toOpenmrsType(parameterComponent.getType())
		        : null;
		if (inputType == null) {
			throw new InvalidRequestException(ERROR_MESSAGE_INPUT_TYPE_REQUIRED);
		}
		
		TaskInput taskInput = new TaskInput();
		taskInput.setInputType(inputType);
		taskInput.setValueString(translateValue(parameterComponent));
		return taskInput;
	}
	
	protected String translateValue(Task.ParameterComponent parameterComponent) {
		if (!parameterComponent.hasValue()) {
			return null;
		}
		Type value = parameterComponent.getValue();
		if (value instanceof StringType) {
			return ((StringType) value).getValue();
		}
		throw new InvalidRequestException(ERROR_MESSAGE_UNSUPPORTED_VALUE_TYPE + value.fhirType());
	}
}
