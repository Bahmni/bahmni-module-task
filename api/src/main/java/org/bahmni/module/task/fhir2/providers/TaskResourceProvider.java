/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.bahmni.module.task.fhir2.providers;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Patch;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.NotImplementedOperationException;
import java.util.HashSet;
import org.bahmni.module.task.fhir2.constants.TaskFhirConstants;
import org.bahmni.module.task.fhir2.search.TaskSearchParams;
import org.bahmni.module.task.fhir2.service.FhirTaskService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Task;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Extends upstream fhir2's {@code TaskFhirResourceProvider} directly rather than Bahmni's {@code
 * BahmniTaskFhirR4ResourceProvider}: {@code searchTasks} below is a fresh {@code @Search}
 * declaration (upstream's own narrower {@code searchTasks} coexists as a separate, non-overriding
 * overload - the same shape Bahmni's own class used in production), so this module has no
 * dependency on fhir2addlextension.
 */
@R4Provider
public class TaskResourceProvider implements IResourceProvider {
	
	private final FhirTaskService taskService;
	
	@Autowired
	public TaskResourceProvider(FhirTaskService taskService) {
		this.taskService = taskService;
	}
	
	@Read
	public Task getTaskById(@IdParam IdType id) {
		return taskService.get(id.getIdPart());
	}
	
	@Search
	public IBundleProvider searchTasks(
	        @OptionalParam(name = Task.SP_BASED_ON, chainWhitelist = { "" }) ReferenceAndListParam basedOnReference,
	        @OptionalParam(name = Task.SP_OWNER, chainWhitelist = { "",
	                Practitioner.SP_IDENTIFIER }, targetTypes = Practitioner.class) ReferenceAndListParam ownerReference,
	        @OptionalParam(name = Task.SP_SUBJECT, chainWhitelist = { "", Patient.SP_IDENTIFIER, Patient.SP_GIVEN,
	                Patient.SP_FAMILY,
	                Patient.SP_NAME }, targetTypes = Patient.class) ReferenceAndListParam subjectReference,
	        @OptionalParam(name = Task.SP_FOCUS, chainWhitelist = { "" }) ReferenceAndListParam focusReference,
	        @OptionalParam(name = Task.SP_STATUS) TokenAndListParam status,
	        @OptionalParam(name = Task.SP_ENCOUNTER, chainWhitelist = {
	                "" }, targetTypes = Encounter.class) ReferenceAndListParam encounterReference,
	        @OptionalParam(name = TaskFhirConstants.SP_TASK_NAME) StringAndListParam name,
	        @OptionalParam(name = Task.SP_RES_ID) TokenAndListParam id,
	        @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated, @IncludeParam(allow = { "Task:patient",
	                "Task:owner", "Task:based-on", "Task:encounter" }) HashSet<Include> includes,
	        @Sort SortSpec sort) {
		TaskSearchParams params = new TaskSearchParams();
		params.setBasedOnReference(basedOnReference);
		params.setOwnerReference(ownerReference);
		params.setForReference(subjectReference);
		params.setFocusReference(focusReference);
		params.setStatus(status);
		params.setEncounterReference(encounterReference);
		params.setName(name);
		params.setId(id);
		params.setLastUpdated(lastUpdated);
		params.setIncludes(includes != null ? includes : new HashSet<>());
		params.setSort(sort);
		return taskService.searchForTasks(params);
	}
	
	@Create
	public MethodOutcome createTask(@ResourceParam Task task) {
		return FhirProviderUtils.buildCreate(taskService.create(task));
	}
	
	@Update
	public MethodOutcome updateTask(@IdParam IdType id, @ResourceParam Task task) {
		return FhirProviderUtils.buildUpdate(taskService.update(id.getIdPart(), task));
	}
	
	@Patch
	public MethodOutcome patchTask(@IdParam IdType id, PatchTypeEnum patchType, @ResourceParam String body,
	        RequestDetails requestDetails) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to patch Task resource");
		}
		return FhirProviderUtils.buildPatch(taskService.patch(id.getIdPart(), patchType, body, requestDetails));
	}
	
	@Delete
	public OperationOutcome deleteTask(@IdParam IdType id) {
		throw new NotImplementedOperationException("Deleting a Task is not supported");
	}
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Task.class;
	}
}
