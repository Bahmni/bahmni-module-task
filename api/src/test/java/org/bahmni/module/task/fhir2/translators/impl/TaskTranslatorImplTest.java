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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.bahmni.module.task.api.domain.Task;
import org.bahmni.module.task.api.domain.TaskInput;
import org.bahmni.module.task.fhir2.dao.FhirTaskDao;
import org.bahmni.module.task.fhir2.translators.TaskInputTranslator;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.OrderService;
import org.openmrs.module.fhir2.api.translators.*;

@ExtendWith(MockitoExtension.class)
class TaskTranslatorImplTest {
	
	@Mock
	private ConceptTranslator conceptTranslator;
	
	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Mock
	private EncounterReferenceTranslator<Encounter> encounterReferenceTranslator;
	
	@Mock
	private PractitionerReferenceTranslator<Provider> providerReferenceTranslator;
	
	@Mock
	private TaskInputTranslator taskInputTranslator;
	
	@Mock
	private LocationReferenceTranslator locationReferenceTranslator;
	
	@Mock
	private OrderService orderService;
	
	@Mock
	private FhirTaskDao fhirTaskDao;
	
	private TaskTranslatorImpl translator;
	
	@BeforeEach
	void setUp() {
		translator = new TaskTranslatorImpl(conceptTranslator, patientReferenceTranslator, encounterReferenceTranslator,
		        providerReferenceTranslator, locationReferenceTranslator, taskInputTranslator, orderService, fhirTaskDao);
	}
	
	/** Minimal resource satisfying the elements backing NOT NULL columns. */
	private org.hl7.fhir.r4.model.Task minimalResource(Concept code) {
		CodeableConcept codeableConcept = new CodeableConcept().setText("TASK_CODE");
		when(conceptTranslator.toOpenmrsType(codeableConcept)).thenReturn(code);
		
		org.hl7.fhir.r4.model.Task resource = new org.hl7.fhir.r4.model.Task();
		resource.setStatus(org.hl7.fhir.r4.model.Task.TaskStatus.REQUESTED);
		resource.setCode(codeableConcept);
		return resource;
	}
	
	@Test
	void shouldReturnNullWhenTaskIsNull() {
		assertNull(translator.toFhirResource(null));
	}
	
	@Test
	void shouldMapScalarFields() {
		Task task = new Task();
		task.setUuid("task-uuid");
		task.setStatus(Task.TaskStatus.REQUESTED);
		task.setPriority(Task.Priority.URGENT);
		task.setDescription("a description");
		Date created = new Date();
		task.setDateCreated(created);
		
		org.hl7.fhir.r4.model.Task resource = translator.toFhirResource(task);
		
		assertEquals("task-uuid", resource.getId());
		assertEquals(org.hl7.fhir.r4.model.Task.TaskStatus.REQUESTED, resource.getStatus());
		assertEquals(org.hl7.fhir.r4.model.Task.TaskIntent.ORDER, resource.getIntent());
		assertEquals(org.hl7.fhir.r4.model.Task.TaskPriority.URGENT, resource.getPriority());
		assertEquals("a description", resource.getDescription());
		assertEquals(created, resource.getAuthoredOn());
	}
	
	@Test
	void shouldMapCodeBusinessStatusAndReferences() {
		Task task = new Task();
		task.setUuid("task-uuid");
		task.setStatus(Task.TaskStatus.ACCEPTED);
		task.setPriority(Task.Priority.ROUTINE);
		
		Concept code = new Concept();
		Concept businessStatus = new Concept();
		task.setTaskCode(code);
		task.setBusinessStatus(businessStatus);
		Patient patient = new Patient();
		task.setPatient(patient);
		Encounter encounter = new Encounter();
		task.setEncounter(encounter);
		Provider owner = new Provider();
		task.setOwner(owner);
		
		CodeableConcept codeConcept = new CodeableConcept();
		CodeableConcept businessStatusConcept = new CodeableConcept();
		when(conceptTranslator.toFhirResource(code)).thenReturn(codeConcept);
		when(conceptTranslator.toFhirResource(businessStatus)).thenReturn(businessStatusConcept);
		Reference patientRef = new Reference("Patient/p");
		Reference encounterRef = new Reference("Encounter/e");
		Reference ownerRef = new Reference("Practitioner/o");
		when(patientReferenceTranslator.toFhirResource(patient)).thenReturn(patientRef);
		when(encounterReferenceTranslator.toFhirResource(encounter)).thenReturn(encounterRef);
		when(providerReferenceTranslator.toFhirResource(owner)).thenReturn(ownerRef);
		
		org.hl7.fhir.r4.model.Task resource = translator.toFhirResource(task);
		
		assertSame(codeConcept, resource.getCode());
		assertSame(businessStatusConcept, resource.getBusinessStatus());
		assertSame(patientRef, resource.getFor());
		assertSame(encounterRef, resource.getEncounter());
		assertSame(ownerRef, resource.getOwner());
	}
	
	@Test
	void shouldMapOrderAsBasedOnServiceRequestReference() {
		Task task = new Task();
		task.setStatus(Task.TaskStatus.REQUESTED);
		task.setPriority(Task.Priority.ROUTINE);
		Order order = new Order();
		order.setUuid("order-uuid");
		task.setOrder(order);
		
		org.hl7.fhir.r4.model.Task resource = translator.toFhirResource(task);
		
		assertEquals(1, resource.getBasedOn().size());
		assertEquals("ServiceRequest/order-uuid", resource.getBasedOnFirstRep().getReference());
	}
	
	@Test
	void shouldMapExecutionPeriodFromStartAndEndDates() {
		Task task = new Task();
		task.setStatus(Task.TaskStatus.INPROGRESS);
		task.setPriority(Task.Priority.STAT);
		Date start = new Date(1000L);
		Date end = new Date(2000L);
		task.setDateStarted(start);
		task.setDateEnded(end);
		
		org.hl7.fhir.r4.model.Task resource = translator.toFhirResource(task);
		
		assertEquals(start, resource.getExecutionPeriod().getStart());
		assertEquals(end, resource.getExecutionPeriod().getEnd());
	}
	
	@Test
	void shouldMapInputsToFhirParameterComponents() {
		Task task = new Task();
		task.setStatus(Task.TaskStatus.REQUESTED);
		task.setPriority(Task.Priority.ROUTINE);
		
		TaskInput input = new TaskInput();
		task.setInputs(Collections.singleton(input));
		
		org.hl7.fhir.r4.model.Task.ParameterComponent paramComponent = new org.hl7.fhir.r4.model.Task.ParameterComponent()
		        .setType(new CodeableConcept().setText("ORDER_DETAIL")).setValue(new StringType("detail-value"));
		when(taskInputTranslator.toFhirResource(input)).thenReturn(paramComponent);
		
		org.hl7.fhir.r4.model.Task resource = translator.toFhirResource(task);
		
		assertEquals(1, resource.getInput().size());
		assertSame(paramComponent, resource.getInput().get(0));
	}
	
	@Test
	void shouldSkipNullInputTranslationResults() {
		Task task = new Task();
		task.setStatus(Task.TaskStatus.REQUESTED);
		task.setPriority(Task.Priority.ROUTINE);
		
		TaskInput input = new TaskInput();
		task.setInputs(Collections.singleton(input));
		when(taskInputTranslator.toFhirResource(input)).thenReturn(null);
		
		org.hl7.fhir.r4.model.Task resource = translator.toFhirResource(task);
		
		assertTrue(resource.getInput().isEmpty());
	}
	
	@Test
	void toOpenmrsTypeShouldRejectNullResource() {
		assertThrows(InvalidRequestException.class, () -> translator.toOpenmrsType((org.hl7.fhir.r4.model.Task) null));
	}
	
	@Test
	void toOpenmrsTypeShouldMapAllSupportedElements() {
		Concept code = new Concept();
		Concept businessStatus = new Concept();
		org.hl7.fhir.r4.model.Task resource = minimalResource(code);
		resource.setId("client-supplied-id");
		resource.setIntent(org.hl7.fhir.r4.model.Task.TaskIntent.ORDER);
		resource.setPriority(org.hl7.fhir.r4.model.Task.TaskPriority.STAT);
		resource.setDescription("a description");
		
		CodeableConcept businessStatusConcept = new CodeableConcept().setText("BUSINESS_STATUS");
		resource.setBusinessStatus(businessStatusConcept);
		when(conceptTranslator.toOpenmrsType(businessStatusConcept)).thenReturn(businessStatus);
		
		Reference patientRef = new Reference("Patient/p");
		Reference encounterRef = new Reference("Encounter/e");
		Reference ownerRef = new Reference("Practitioner/o");
		resource.setFor(patientRef);
		resource.setEncounter(encounterRef);
		resource.setOwner(ownerRef);
		
		Patient patient = new Patient();
		Encounter encounter = new Encounter();
		Provider owner = new Provider();
		when(patientReferenceTranslator.toOpenmrsType(patientRef)).thenReturn(patient);
		when(encounterReferenceTranslator.toOpenmrsType(encounterRef)).thenReturn(encounter);
		when(providerReferenceTranslator.toOpenmrsType(ownerRef)).thenReturn(owner);
		
		Date start = new Date(1000L);
		Date end = new Date(2000L);
		resource.setExecutionPeriod(new Period().setStart(start).setEnd(end));
		
		Task result = translator.toOpenmrsType(resource);
		
		assertEquals(Task.TaskStatus.REQUESTED, result.getStatus());
		assertEquals(Task.Intent.ORDER, result.getIntent());
		assertEquals(Task.Priority.STAT, result.getPriority());
		assertEquals("a description", result.getDescription());
		assertSame(code, result.getTaskCode());
		assertSame(businessStatus, result.getBusinessStatus());
		assertSame(patient, result.getPatient());
		assertSame(encounter, result.getEncounter());
		assertSame(owner, result.getOwner());
		assertEquals(start, result.getDateStarted());
		assertEquals(end, result.getDateEnded());
	}
	
	@Test
	void toOpenmrsTypeShouldIgnoreClientSuppliedIdAndAuditFields() {
		org.hl7.fhir.r4.model.Task resource = minimalResource(new Concept());
		resource.setId("client-supplied-id");
		resource.setAuthoredOn(new Date(1000L));
		resource.setLastModified(new Date(2000L));
		
		Task result = translator.toOpenmrsType(resource);
		
		assertNotEquals("client-supplied-id", result.getUuid());
		assertNotNull(result.getUuid());
		assertNull(result.getCreator());
		assertNull(result.getDateCreated());
		assertNull(result.getDateChanged());
	}
	
	/**
	 * R4's TaskStatus matches the domain enum one-for-one apart from the synthetic NULL member, which
	 * is the only inbound value the narrowing can reject.
	 */
	@Test
	void toOpenmrsTypeShouldRejectStatusWithoutDomainEquivalent() {
		org.hl7.fhir.r4.model.Task resource = new org.hl7.fhir.r4.model.Task();
		resource.setStatus(org.hl7.fhir.r4.model.Task.TaskStatus.NULL);
		
		assertThrows(InvalidRequestException.class, () -> translator.toOpenmrsType(resource));
	}
	
	@Test
	void toOpenmrsTypeShouldRejectMissingStatus() {
		org.hl7.fhir.r4.model.Task resource = new org.hl7.fhir.r4.model.Task();
		
		assertThrows(InvalidRequestException.class, () -> translator.toOpenmrsType(resource));
	}
	
	@Test
	void toOpenmrsTypeShouldRejectIntentOtherThanOrder() {
		org.hl7.fhir.r4.model.Task resource = new org.hl7.fhir.r4.model.Task();
		resource.setStatus(org.hl7.fhir.r4.model.Task.TaskStatus.REQUESTED);
		resource.setIntent(org.hl7.fhir.r4.model.Task.TaskIntent.PLAN);
		
		assertThrows(InvalidRequestException.class, () -> translator.toOpenmrsType(resource));
	}
	
	@Test
	void toOpenmrsTypeShouldDefaultIntentAndPriority() {
		Task result = translator.toOpenmrsType(minimalResource(new Concept()));
		
		assertEquals(Task.Intent.ORDER, result.getIntent());
		assertEquals(Task.Priority.ROUTINE, result.getPriority());
	}
	
	@Test
	void toOpenmrsTypeShouldRejectMissingCode() {
		org.hl7.fhir.r4.model.Task resource = new org.hl7.fhir.r4.model.Task();
		resource.setStatus(org.hl7.fhir.r4.model.Task.TaskStatus.REQUESTED);
		
		assertThrows(InvalidRequestException.class, () -> translator.toOpenmrsType(resource));
	}
	
	@Test
	void toOpenmrsTypeShouldRejectUnresolvableCode() {
		CodeableConcept codeableConcept = new CodeableConcept().setText("UNKNOWN");
		when(conceptTranslator.toOpenmrsType(codeableConcept)).thenReturn(null);
		
		org.hl7.fhir.r4.model.Task resource = new org.hl7.fhir.r4.model.Task();
		resource.setStatus(org.hl7.fhir.r4.model.Task.TaskStatus.REQUESTED);
		resource.setCode(codeableConcept);
		
		assertThrows(InvalidRequestException.class, () -> translator.toOpenmrsType(resource));
	}
	
	@Test
	void toOpenmrsTypeShouldResolveTypelessBasedOnReferenceAsOrder() {
		org.hl7.fhir.r4.model.Task resource = minimalResource(new Concept());
		resource.addBasedOn(new Reference("ServiceRequest/order-uuid"));
		
		Order order = new Order();
		when(orderService.getOrderByUuid("order-uuid")).thenReturn(order);
		
		Task result = translator.toOpenmrsType(resource);
		
		assertSame(order, result.getOrder());
		verify(orderService).getOrderByUuid("order-uuid");
	}
	
	@Test
	void toOpenmrsTypeShouldRejectBasedOnOfWrongType() {
		org.hl7.fhir.r4.model.Task resource = minimalResource(new Concept());
		resource.addBasedOn(new Reference("Task/some-uuid"));
		
		assertThrows(InvalidRequestException.class, () -> translator.toOpenmrsType(resource));
		verifyNoInteractions(orderService);
	}
	
	@Test
	void toOpenmrsTypeShouldRejectUnresolvableBasedOn() {
		org.hl7.fhir.r4.model.Task resource = minimalResource(new Concept());
		resource.addBasedOn(new Reference("ServiceRequest/missing"));
		when(orderService.getOrderByUuid("missing")).thenReturn(null);
		
		assertThrows(InvalidRequestException.class, () -> translator.toOpenmrsType(resource));
	}
	
	@Test
	void toOpenmrsTypeShouldRejectMultipleBasedOnReferences() {
		org.hl7.fhir.r4.model.Task resource = minimalResource(new Concept());
		resource.setBasedOn(Arrays.asList(new Reference("ServiceRequest/a"), new Reference("ServiceRequest/b")));
		
		assertThrows(InvalidRequestException.class, () -> translator.toOpenmrsType(resource));
		verifyNoInteractions(orderService);
	}
	
	@Test
	void toOpenmrsTypeShouldResolvePartOfViaDao() {
		org.hl7.fhir.r4.model.Task resource = minimalResource(new Concept());
		resource.addPartOf(new Reference("Task/parent-uuid"));
		
		Task parent = new Task();
		parent.setUuid("parent-uuid");
		when(fhirTaskDao.get("parent-uuid")).thenReturn(parent);
		
		Task result = translator.toOpenmrsType(resource);
		
		assertSame(parent, result.getPartOf());
	}
	
	@Test
	void toOpenmrsTypeShouldRejectPartOfPointingAtItself() {
		Task existing = new Task();
		existing.setUuid("self-uuid");
		
		org.hl7.fhir.r4.model.Task resource = minimalResource(new Concept());
		resource.addPartOf(new Reference("Task/self-uuid"));
		when(fhirTaskDao.get("self-uuid")).thenReturn(existing);
		
		assertThrows(InvalidRequestException.class, () -> translator.toOpenmrsType(existing, resource));
	}
	
	@Test
	void toOpenmrsTypeShouldRejectPartOfPointingAtADescendant() {
		Task existing = new Task();
		existing.setUuid("existing-uuid");
		Task child = new Task();
		child.setUuid("child-uuid");
		child.setPartOf(existing);
		Task grandChild = new Task();
		grandChild.setUuid("grand-child-uuid");
		grandChild.setPartOf(child);
		
		org.hl7.fhir.r4.model.Task resource = minimalResource(new Concept());
		resource.addPartOf(new Reference("Task/grand-child-uuid"));
		when(fhirTaskDao.get("grand-child-uuid")).thenReturn(grandChild);
		
		assertThrows(InvalidRequestException.class, () -> translator.toOpenmrsType(existing, resource));
	}
	
	@Test
	void toOpenmrsTypeShouldTranslateReferenceTranslatorFailuresIntoClientErrors() {
		org.hl7.fhir.r4.model.Task resource = minimalResource(new Concept());
		Reference badRef = new Reference("Group/not-a-patient");
		resource.setFor(badRef);
		when(patientReferenceTranslator.toOpenmrsType(badRef))
		        .thenThrow(new IllegalArgumentException("Reference must be to a Patient"));
		
		assertThrows(InvalidRequestException.class, () -> translator.toOpenmrsType(resource));
	}
	
	@Test
	void toOpenmrsTypeShouldMapInputsAndSetBackReference() {
		Concept inputType = new Concept();
		org.hl7.fhir.r4.model.Task resource = minimalResource(new Concept());
		org.hl7.fhir.r4.model.Task.ParameterComponent parameter = new org.hl7.fhir.r4.model.Task.ParameterComponent()
		        .setType(new CodeableConcept().setText("ORDER_DETAIL")).setValue(new StringType("detail"));
		resource.addInput(parameter);
		
		TaskInput translated = new TaskInput();
		translated.setInputType(inputType);
		translated.setValueString("detail");
		when(taskInputTranslator.toOpenmrsType(parameter)).thenReturn(translated);
		
		Task result = translator.toOpenmrsType(resource);
		
		assertEquals(1, result.getInputs().size());
		TaskInput mapped = result.getInputs().iterator().next();
		assertSame(translated, mapped);
		assertSame(result, mapped.getTask());
	}
	
	@Test
	void toOpenmrsTypeShouldSkipNullInputTranslationResults() {
		org.hl7.fhir.r4.model.Task resource = minimalResource(new Concept());
		org.hl7.fhir.r4.model.Task.ParameterComponent parameter = new org.hl7.fhir.r4.model.Task.ParameterComponent();
		resource.addInput(parameter);
		when(taskInputTranslator.toOpenmrsType(parameter)).thenReturn(null);
		
		Task result = translator.toOpenmrsType(resource);
		
		assertTrue(result.getInputs().isEmpty());
	}
	
	@Test
	void toOpenmrsTypeShouldFallBackToRoutineForUnmappablePriority() {
		org.hl7.fhir.r4.model.Task resource = minimalResource(new Concept());
		resource.setPriority(org.hl7.fhir.r4.model.Task.TaskPriority.NULL);
		
		assertEquals(Task.Priority.ROUTINE, translator.toOpenmrsType(resource).getPriority());
	}
	
	@Test
	void toOpenmrsTypeShouldRejectUnparseableBasedOnReference() {
		org.hl7.fhir.r4.model.Task resource = minimalResource(new Concept());
		resource.addBasedOn(new Reference("not-a-typed-reference"));
		
		assertThrows(InvalidRequestException.class, () -> translator.toOpenmrsType(resource));
		verifyNoInteractions(orderService);
	}
	
	@Test
	void toOpenmrsTypeShouldRejectReferencesThatResolveToNothing() {
		org.hl7.fhir.r4.model.Task resource = minimalResource(new Concept());
		Reference patientRef = new Reference("Patient/missing");
		resource.setFor(patientRef);
		when(patientReferenceTranslator.toOpenmrsType(patientRef)).thenReturn(null);
		
		assertThrows(InvalidRequestException.class, () -> translator.toOpenmrsType(resource));
	}
	
	@Test
	void toOpenmrsTypeShouldRejectMultiplePartOfReferences() {
		org.hl7.fhir.r4.model.Task resource = minimalResource(new Concept());
		resource.setPartOf(Arrays.asList(new Reference("Task/a"), new Reference("Task/b")));
		
		assertThrows(InvalidRequestException.class, () -> translator.toOpenmrsType(resource));
		verifyNoInteractions(fhirTaskDao);
	}
	
	@Test
	void toOpenmrsTypeShouldRejectUnresolvablePartOf() {
		org.hl7.fhir.r4.model.Task resource = minimalResource(new Concept());
		resource.addPartOf(new Reference("Task/missing"));
		when(fhirTaskDao.get("missing")).thenReturn(null);
		
		assertThrows(InvalidRequestException.class, () -> translator.toOpenmrsType(resource));
	}
	
	@Test
	void updateShouldRejectNullArguments() {
		org.hl7.fhir.r4.model.Task resource = new org.hl7.fhir.r4.model.Task();
		assertThrows(InvalidRequestException.class, () -> translator.toOpenmrsType(null, resource));
		assertThrows(InvalidRequestException.class, () -> translator.toOpenmrsType(new Task(), null));
	}
	
	@Test
	void updateShouldPreserveIdentityAuditAndSubTasks() {
		Task existing = new Task();
		existing.setTaskId(42);
		existing.setUuid("existing-uuid");
		User creator = new User();
		Date created = new Date(500L);
		existing.setCreator(creator);
		existing.setDateCreated(created);
		Location location = new Location();
		existing.setLocation(location);
		
		Task subTask = new Task();
		Set<Task> subTasks = Collections.singleton(subTask);
		existing.setSubTasks(subTasks);
		existing.setStatus(Task.TaskStatus.REQUESTED);
		existing.setPriority(Task.Priority.ROUTINE);
		existing.setInputs(new HashSet<>());
		
		Concept newCode = new Concept();
		org.hl7.fhir.r4.model.Task resource = minimalResource(newCode);
		resource.setStatus(org.hl7.fhir.r4.model.Task.TaskStatus.INPROGRESS);
		resource.setPriority(org.hl7.fhir.r4.model.Task.TaskPriority.URGENT);
		
		Task result = translator.toOpenmrsType(existing, resource);
		
		assertSame(existing, result);
		assertEquals(Integer.valueOf(42), result.getTaskId());
		assertEquals("existing-uuid", result.getUuid());
		assertSame(creator, result.getCreator());
		assertEquals(created, result.getDateCreated());
		assertNotSame(location, result.getLocation());
		assertSame(subTasks, result.getSubTasks());
		assertNull(result.getDateChanged());
		
		assertEquals(Task.TaskStatus.INPROGRESS, result.getStatus());
		assertEquals(Task.Priority.URGENT, result.getPriority());
		assertSame(newCode, result.getTaskCode());
	}
	
	@Test
	void updateShouldClearOptionalElementsAbsentFromTheResource() {
		Task existing = new Task();
		existing.setUuid("existing-uuid");
		existing.setDescription("old description");
		existing.setBusinessStatus(new Concept());
		existing.setPatient(new Patient());
		existing.setEncounter(new Encounter());
		existing.setOwner(new Provider());
		existing.setOrder(new Order());
		existing.setPartOf(new Task());
		existing.setDateStarted(new Date(1000L));
		existing.setDateEnded(new Date(2000L));
		existing.setInputs(new HashSet<>());
		
		Task result = translator.toOpenmrsType(existing, minimalResource(new Concept()));
		
		assertNull(result.getDescription());
		assertNull(result.getBusinessStatus());
		assertNull(result.getPatient());
		assertNull(result.getEncounter());
		assertNull(result.getOwner());
		assertNull(result.getOrder());
		assertNull(result.getPartOf());
		assertNull(result.getDateStarted());
		assertNull(result.getDateEnded());
	}
	
	@Test
	void updateShouldMutateTheInputsCollectionInPlace() {
		Task existing = new Task();
		existing.setUuid("existing-uuid");
		TaskInput stale = new TaskInput();
		stale.setInputType(new Concept());
		stale.setValueString("stale");
		Set<TaskInput> originalInputs = new HashSet<>(Collections.singletonList(stale));
		existing.setInputs(originalInputs);
		
		org.hl7.fhir.r4.model.Task resource = minimalResource(new Concept());
		org.hl7.fhir.r4.model.Task.ParameterComponent parameter = new org.hl7.fhir.r4.model.Task.ParameterComponent()
		        .setType(new CodeableConcept().setText("ORDER_DETAIL")).setValue(new StringType("fresh"));
		resource.addInput(parameter);
		
		TaskInput fresh = new TaskInput();
		fresh.setInputType(new Concept());
		fresh.setValueString("fresh");
		when(taskInputTranslator.toOpenmrsType(parameter)).thenReturn(fresh);
		
		Task result = translator.toOpenmrsType(existing, resource);
		
		// all-delete-orphan requires the very same collection instance to stay attached
		assertSame(originalInputs, result.getInputs());
		assertEquals(1, result.getInputs().size());
		assertFalse(result.getInputs().contains(stale));
		assertSame(fresh, result.getInputs().iterator().next());
		assertSame(existing, fresh.getTask());
	}
	
	@Test
	void updateShouldClearInputsWhenTheResourceHasNone() {
		Task existing = new Task();
		existing.setUuid("existing-uuid");
		TaskInput stale = new TaskInput();
		Set<TaskInput> originalInputs = new HashSet<>(Collections.singletonList(stale));
		existing.setInputs(originalInputs);
		
		Task result = translator.toOpenmrsType(existing, minimalResource(new Concept()));
		
		assertSame(originalInputs, result.getInputs());
		assertTrue(result.getInputs().isEmpty());
	}
}
