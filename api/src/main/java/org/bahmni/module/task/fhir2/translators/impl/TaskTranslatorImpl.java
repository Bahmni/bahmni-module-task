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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.bahmni.module.task.api.domain.Task;
import org.bahmni.module.task.api.domain.TaskInput;
import org.bahmni.module.task.fhir2.dao.FhirTaskDao;
import org.bahmni.module.task.fhir2.translators.TaskInputTranslator;
import org.bahmni.module.task.fhir2.translators.TaskTranslator;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Provider;
import org.openmrs.api.OrderService;
import org.openmrs.module.fhir2.api.translators.*;
import org.openmrs.module.fhir2.api.util.FhirUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("fhirTaskTranslator")
public class TaskTranslatorImpl implements TaskTranslator {
	
	private static final String SERVICE_REQUEST_REFERENCE_PREFIX = "ServiceRequest/";
	
	private static final String TASK_REFERENCE_PREFIX = "Task/";
	
	private static final String SERVICE_REQUEST_TYPE = "ServiceRequest";
	
	private static final String TASK_TYPE = "Task";
	
	private final ConceptTranslator conceptTranslator;
	
	private final PatientReferenceTranslator patientReferenceTranslator;
	
	private final EncounterReferenceTranslator<Encounter> encounterReferenceTranslator;
	
	private final PractitionerReferenceTranslator<Provider> providerReferenceTranslator;
	
	private final LocationReferenceTranslator locationReferenceTranslator;
	
	private final TaskInputTranslator taskInputTranslator;
	
	private final OrderService orderService;
	
	private final FhirTaskDao fhirTaskDao;
	
	@Autowired
	public TaskTranslatorImpl(ConceptTranslator conceptTranslator, PatientReferenceTranslator patientReferenceTranslator,
	    EncounterReferenceTranslator<Encounter> encounterReferenceTranslator,
	    PractitionerReferenceTranslator<Provider> providerReferenceTranslator,
	    LocationReferenceTranslator locationReferenceTranslator, TaskInputTranslator taskInputTranslator,
	    OrderService orderService, FhirTaskDao fhirTaskDao) {
		this.conceptTranslator = conceptTranslator;
		this.patientReferenceTranslator = patientReferenceTranslator;
		this.encounterReferenceTranslator = encounterReferenceTranslator;
		this.providerReferenceTranslator = providerReferenceTranslator;
		this.locationReferenceTranslator = locationReferenceTranslator;
		this.taskInputTranslator = taskInputTranslator;
		this.orderService = orderService;
		this.fhirTaskDao = fhirTaskDao;
	}
	
	@Override
	public org.hl7.fhir.r4.model.Task toFhirResource(Task task) {
		if (task == null) {
			return null;
		}
		
		org.hl7.fhir.r4.model.Task resource = new org.hl7.fhir.r4.model.Task();
		resource.setId(task.getUuid());
		resource.setStatus(toFhirStatus(task.getStatus()));
		resource.setIntent(org.hl7.fhir.r4.model.Task.TaskIntent.ORDER);
		resource.setPriority(toFhirPriority(task.getPriority()));
		resource.setDescription(task.getDescription());
		Optional.ofNullable(task.getTaskCode()).map(conceptTranslator::toFhirResource).ifPresent(resource::setCode);
		Optional.ofNullable(task.getBusinessStatus()).map(conceptTranslator::toFhirResource)
		        .ifPresent(resource::setBusinessStatus);
		Optional.ofNullable(task.getPatient()).map(patientReferenceTranslator::toFhirResource).ifPresent(resource::setFor);
		Optional.ofNullable(task.getEncounter()).map(encounterReferenceTranslator::toFhirResource)
		        .ifPresent(resource::setEncounter);
		Optional.ofNullable(task.getOwner()).map(providerReferenceTranslator::toFhirResource).ifPresent(resource::setOwner);
		Optional.ofNullable(task.getOrder()).map(order -> new Reference(SERVICE_REQUEST_REFERENCE_PREFIX + order.getUuid()))
		        .ifPresent(resource::addBasedOn);
		Optional.ofNullable(task.getPartOf()).map(partOf -> new Reference(TASK_REFERENCE_PREFIX + partOf.getUuid()))
		        .ifPresent(resource::addPartOf);
		Optional.ofNullable(task.getLocation()).map(locationReferenceTranslator::toFhirResource)
		        .ifPresent(resource::setLocation);
		
		Date dateStarted = task.getDateStarted();
		Date dateEnded = task.getDateEnded();
		if (dateStarted != null || dateEnded != null) {
			resource.setExecutionPeriod(new Period().setStart(dateStarted).setEnd(dateEnded));
		}
		
		resource.setAuthoredOn(task.getDateCreated());
		resource.setLastModified(task.getDateChanged());
		
		if (task.getInputs() != null && !task.getInputs().isEmpty()) {
			List<org.hl7.fhir.r4.model.Task.ParameterComponent> inputs = task.getInputs().stream()
			        .map(taskInputTranslator::toFhirResource).filter(Objects::nonNull).collect(Collectors.toList());
			resource.setInput(inputs);
		}
		
		return resource;
	}
	
	@Override
	public Task toOpenmrsType(org.hl7.fhir.r4.model.Task task) {
		if (task == null) {
			throw new InvalidRequestException("A Task resource must be supplied");
		}
		
		Task openmrsTask = new Task();
		setOpenmrsTaskFields(openmrsTask, task);
		return openmrsTask;
	}
	
	@Override
	public Task toOpenmrsType(@Nonnull Task existingObject, @Nonnull org.hl7.fhir.r4.model.Task resource) {
		if (existingObject == null) {
			throw new InvalidRequestException("An existing Task must be supplied");
		}
		if (resource == null) {
			throw new InvalidRequestException("A Task resource must be supplied");
		}
		
		setOpenmrsTaskFields(existingObject, resource);
		return existingObject;
	}
	
	/**
	 * Applies every FHIR element the read side is able to represent onto the supplied OpenMRS task.
	 * <p>
	 * PUT is a full replacement, so optional elements absent from the resource are cleared. {@code
	 * location} is the deliberate exception: {@link #toFhirResource} never emits it, so a client cannot
	 * see it and must not be able to erase it. {@code uuid}, {@code creator}, {@code
	 * dateCreated} and {@code dateChanged} are owned by OpenMRS and never written here.
	 */
	private void setOpenmrsTaskFields(Task target, org.hl7.fhir.r4.model.Task source) {
		target.setStatus(toOpenmrsStatus(source));
		target.setIntent(toOpenmrsIntent(source));
		target.setPriority(toOpenmrsPriority(source));
		
		target.setTaskCode(requireConcept(source.hasCode() ? source.getCode() : null, "Task.code"));
		target.setBusinessStatus(
		    source.hasBusinessStatus() ? requireConcept(source.getBusinessStatus(), "Task.businessStatus") : null);
		target.setDescription(source.hasDescription() ? source.getDescription() : null);
		
		target.setPatient(
		    source.hasFor() ? resolve(source.getFor(), patientReferenceTranslator::toOpenmrsType, "Task.for") : null);
		target.setEncounter(source.hasEncounter()
		        ? resolve(source.getEncounter(), encounterReferenceTranslator::toOpenmrsType, "Task.encounter")
		        : null);
		target.setOwner(
		    source.hasOwner() ? resolve(source.getOwner(), providerReferenceTranslator::toOpenmrsType, "Task.owner") : null);
		target.setLocation(
		    source.hasLocation() ? resolve(source.getLocation(), locationReferenceTranslator::toOpenmrsType, "Task.location")
		            : null);
		
		target.setOrder(resolveOrder(source));
		target.setPartOf(resolvePartOf(target, source));
		
		if (source.hasExecutionPeriod()) {
			Period executionPeriod = source.getExecutionPeriod();
			target.setDateStarted(executionPeriod.getStart());
			target.setDateEnded(executionPeriod.getEnd());
		} else {
			target.setDateStarted(null);
			target.setDateEnded(null);
		}
		
		setInputs(target, source);
	}
	
	private Task.TaskStatus toOpenmrsStatus(org.hl7.fhir.r4.model.Task source) {
		if (!source.hasStatus()) {
			throw new InvalidRequestException("Task.status is required");
		}
		try {
			return Task.TaskStatus.valueOf(source.getStatus().name());
		}
		catch (IllegalArgumentException e) {
			throw new InvalidRequestException("Unsupported Task.status: " + source.getStatus().name());
		}
	}
	
	private Task.Intent toOpenmrsIntent(org.hl7.fhir.r4.model.Task source) {
		if (!source.hasIntent()) {
			return Task.Intent.ORDER;
		}
		try {
			return Task.Intent.valueOf(source.getIntent().name());
		}
		catch (IllegalArgumentException e) {
			throw new InvalidRequestException(
			        "Unsupported Task.intent: " + source.getIntent().toCode() + ", only order is supported");
		}
	}
	
	private Task.Priority toOpenmrsPriority(org.hl7.fhir.r4.model.Task source) {
		if (!source.hasPriority()) {
			return Task.Priority.ROUTINE;
		}
		try {
			return Task.Priority.valueOf(source.getPriority().name());
		}
		catch (IllegalArgumentException e) {
			return Task.Priority.ROUTINE;
		}
	}
	
	private Concept requireConcept(CodeableConcept codeableConcept, String element) {
		if (codeableConcept == null) {
			throw new InvalidRequestException(element + " is required");
		}
		Concept concept = conceptTranslator.toOpenmrsType(codeableConcept);
		if (concept == null) {
			throw new InvalidRequestException(element + " could not be resolved to a concept");
		}
		return concept;
	}
	
	/**
	 * The reference translators throw {@link IllegalArgumentException} for a wrong-typed reference,
	 * which would escape HAPI as a 500; translate it into a client error instead.
	 */
	private <T> T resolve(Reference reference, Function<Reference, T> resolver, String element) {
		T resolved;
		try {
			resolved = resolver.apply(reference);
		}
		catch (IllegalArgumentException e) {
			throw new InvalidRequestException(element + " is not a valid reference: " + e.getMessage());
		}
		if (resolved == null) {
			throw new InvalidRequestException(element + " could not be resolved: " + reference.getReference());
		}
		return resolved;
	}
	
	private Order resolveOrder(org.hl7.fhir.r4.model.Task source) {
		Reference reference = singleReference(source.getBasedOn(), "Task.basedOn");
		if (reference == null) {
			return null;
		}
		
		String uuid = referenceId(reference, SERVICE_REQUEST_TYPE, "Task.basedOn");
		Order order = orderService.getOrderByUuid(uuid);
		if (order == null) {
			throw new InvalidRequestException("Task.basedOn could not be resolved: " + uuid);
		}
		return order;
	}
	
	private Task resolvePartOf(Task target, org.hl7.fhir.r4.model.Task source) {
		Reference reference = singleReference(source.getPartOf(), "Task.partOf");
		if (reference == null) {
			return null;
		}
		
		String uuid = referenceId(reference, TASK_TYPE, "Task.partOf");
		Task parent = fhirTaskDao.get(uuid);
		if (parent == null) {
			throw new InvalidRequestException("Task.partOf could not be resolved: " + uuid);
		}
		// setPartOf bypasses Task.addSubTask's loop guard, so check the hierarchy here.
		if (parent.equals(target) || Task.isInHierarchy(parent, target)) {
			throw new InvalidRequestException("Task.partOf would create a hierarchy loop");
		}
		return parent;
	}
	
	/**
	 * The domain holds a single order and a single parent, so a list with more than one entry would
	 * silently lose data.
	 */
	private Reference singleReference(List<Reference> references, String element) {
		if (references == null || references.isEmpty()) {
			return null;
		}
		if (references.size() > 1) {
			throw new InvalidRequestException(element + " supports at most one reference");
		}
		return references.get(0);
	}
	
	private String referenceId(Reference reference, String expectedType, String element) {
		String type = FhirUtils.getReferenceType(reference).orElseThrow(
		    () -> new InvalidRequestException(element + " must be a reference of the form " + expectedType + "/{uuid}"));
		if (!expectedType.equals(type)) {
			throw new InvalidRequestException(element + " must reference a " + expectedType + ", got " + type);
		}
		return FhirUtils.referenceToId(reference.getReference()).orElseThrow(
		    () -> new InvalidRequestException(element + " must be a reference of the form " + expectedType + "/{uuid}"));
	}
	
	/**
	 * The inputs collection is mapped {@code cascade="all-delete-orphan"}, so it has to be mutated in
	 * place rather than replaced, and {@code inverse="true"} means the child owns the foreign key.
	 */
	private void setInputs(Task target, org.hl7.fhir.r4.model.Task source) {
		Set<TaskInput> inputs = target.getInputs();
		if (inputs == null) {
			inputs = new HashSet<>();
			target.setInputs(inputs);
		}
		
		inputs.clear();
		for (org.hl7.fhir.r4.model.Task.ParameterComponent parameter : source.getInput()) {
			TaskInput input = taskInputTranslator.toOpenmrsType(parameter);
			if (input == null) {
				continue;
			}
			input.setTask(target);
			inputs.add(input);
		}
	}
	
	private org.hl7.fhir.r4.model.Task.TaskStatus toFhirStatus(Task.TaskStatus status) {
		return Optional.ofNullable(status).map(s -> org.hl7.fhir.r4.model.Task.TaskStatus.valueOf(s.name())).orElse(null);
	}
	
	private org.hl7.fhir.r4.model.Task.TaskPriority toFhirPriority(Task.Priority priority) {
		return Optional.ofNullable(priority).map(p -> org.hl7.fhir.r4.model.Task.TaskPriority.valueOf(p.name()))
		        .orElse(null);
	}
}
