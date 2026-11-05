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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.bahmni.module.task.api.domain.TaskInput;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.Concept;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;

@ExtendWith(MockitoExtension.class)
class TaskInputTranslatorImplTest {
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	private TaskInputTranslatorImpl translator;
	
	@BeforeEach
	void setUp() {
		translator = new TaskInputTranslatorImpl(conceptTranslator);
	}
	
	@Test
	void toFhirResourceShouldReturnNullWhenValueStringIsNull() {
		TaskInput input = new TaskInput();
		input.setInputType(new Concept());
		
		Task.ParameterComponent result = translator.toFhirResource(input);
		
		assertNull(result);
		verifyNoInteractions(conceptTranslator);
	}
	
	@Test
	void toFhirResourceShouldTranslateValueString() {
		Concept concept = new Concept();
		CodeableConcept codeableConcept = new CodeableConcept().setText("ORDER_DETAIL");
		when(conceptTranslator.toFhirResource(concept)).thenReturn(codeableConcept);
		
		TaskInput input = new TaskInput();
		input.setInputType(concept);
		input.setValueString("sample-value");
		
		Task.ParameterComponent result = translator.toFhirResource(input);
		
		assertNotNull(result);
		assertSame(codeableConcept, result.getType());
		assertInstanceOf(StringType.class, result.getValue());
		assertEquals("sample-value", result.getValue().primitiveValue());
	}
	
	@Test
	void toFhirResourceShouldMapInputTypeFromConcept() {
		Concept concept = new Concept();
		concept.setUuid("concept-uuid");
		CodeableConcept codeableConcept = new CodeableConcept().setText("INPUT_TYPE");
		when(conceptTranslator.toFhirResource(concept)).thenReturn(codeableConcept);
		
		TaskInput input = new TaskInput();
		input.setInputType(concept);
		input.setValueString("value");
		
		Task.ParameterComponent result = translator.toFhirResource(input);
		
		assertEquals("INPUT_TYPE", result.getType().getText());
		verify(conceptTranslator).toFhirResource(concept);
	}
	
	@Test
	void toOpenmrsTypeShouldMapInputTypeAndValueString() {
		Concept concept = new Concept();
		CodeableConcept codeableConcept = new CodeableConcept().setText("ORDER_DETAIL");
		when(conceptTranslator.toOpenmrsType(codeableConcept)).thenReturn(concept);
		
		Task.ParameterComponent parameter = new Task.ParameterComponent().setType(codeableConcept)
		        .setValue(new StringType("sample-value"));
		
		TaskInput result = translator.toOpenmrsType(parameter);
		
		assertNotNull(result);
		assertSame(concept, result.getInputType());
		assertEquals("sample-value", result.getValueString());
	}
	
	@Test
	void toOpenmrsTypeShouldLeaveValueStringNullWhenValueIsAbsent() {
		Concept concept = new Concept();
		CodeableConcept codeableConcept = new CodeableConcept().setText("ORDER_DETAIL");
		when(conceptTranslator.toOpenmrsType(codeableConcept)).thenReturn(concept);
		
		TaskInput result = translator.toOpenmrsType(new Task.ParameterComponent().setType(codeableConcept));
		
		assertSame(concept, result.getInputType());
		assertNull(result.getValueString());
	}
	
	@Test
	void toOpenmrsTypeShouldRejectMissingType() {
		Task.ParameterComponent parameter = new Task.ParameterComponent().setValue(new StringType("sample-value"));
		
		assertThrows(InvalidRequestException.class, () -> translator.toOpenmrsType(parameter));
		verifyNoInteractions(conceptTranslator);
	}
	
	@Test
	void toOpenmrsTypeShouldRejectUnresolvableConcept() {
		CodeableConcept codeableConcept = new CodeableConcept().setText("UNKNOWN");
		when(conceptTranslator.toOpenmrsType(codeableConcept)).thenReturn(null);
		
		Task.ParameterComponent parameter = new Task.ParameterComponent().setType(codeableConcept)
		        .setValue(new StringType("sample-value"));
		
		assertThrows(InvalidRequestException.class, () -> translator.toOpenmrsType(parameter));
	}
	
	@Test
	void toOpenmrsTypeShouldRejectNonStringValueTypes() {
		Concept concept = new Concept();
		CodeableConcept codeableConcept = new CodeableConcept().setText("ORDER_DETAIL");
		when(conceptTranslator.toOpenmrsType(codeableConcept)).thenReturn(concept);
		
		Task.ParameterComponent integerValued = new Task.ParameterComponent().setType(codeableConcept)
		        .setValue(new IntegerType(7));
		Task.ParameterComponent booleanValued = new Task.ParameterComponent().setType(codeableConcept)
		        .setValue(new BooleanType(true));
		
		assertThrows(InvalidRequestException.class, () -> translator.toOpenmrsType(integerValued));
		assertThrows(InvalidRequestException.class, () -> translator.toOpenmrsType(booleanValued));
	}
	
	@Test
	void toOpenmrsTypeShouldReturnNullForNullParameter() {
		assertNull(translator.toOpenmrsType(null));
	}
}
