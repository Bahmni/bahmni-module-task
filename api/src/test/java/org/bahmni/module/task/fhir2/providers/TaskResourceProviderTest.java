/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) Thoughtworks Inc.
 */
package org.bahmni.module.task.fhir2.providers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.NotImplementedOperationException;
import org.bahmni.module.task.fhir2.service.FhirTaskService;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.module.fhir2.api.search.param.TaskSearchParams;

@ExtendWith(MockitoExtension.class)
class TaskResourceProviderTest {
	
	@Mock
	private FhirTaskService taskService;
	
	@Mock
	private IBundleProvider bundleProvider;
	
	@Mock
	private RequestDetails requestDetails;
	
	private TaskResourceProvider provider;
	
	@BeforeEach
	void setUp() {
		provider = new TaskResourceProvider(taskService);
	}
	
	@Test
	void getTaskByIdShouldDelegateToService() {
		Task task = new Task();
		task.setId("task-uuid");
		when(taskService.get("task-uuid")).thenReturn(task);
		
		Task result = provider.getTaskById(new IdType("task-uuid"));
		
		assertSame(task, result);
	}
	
	@Test
	void searchTasksShouldDelegateToService() {
		when(taskService.searchForTasks(any(TaskSearchParams.class))).thenReturn(bundleProvider);
		
		IBundleProvider result = provider.searchTasks(null, null, null, null, null, null, null, null, null, null, null);
		
		assertSame(bundleProvider, result);
		verify(taskService).searchForTasks(any(TaskSearchParams.class));
	}
	
	@Test
	void createTaskShouldDelegateToServiceAndReportCreated() {
		Task submitted = new Task();
		Task saved = new Task();
		saved.setId("new-uuid");
		when(taskService.create(submitted)).thenReturn(saved);
		
		MethodOutcome outcome = provider.createTask(submitted);
		
		assertTrue(outcome.getCreated());
		assertSame(saved, outcome.getResource());
		assertEquals("new-uuid", outcome.getId().getIdPart());
		verify(taskService).create(submitted);
	}
	
	@Test
	void updateTaskShouldDelegateToService() {
		Task submitted = new Task();
		submitted.setId("task-uuid");
		Task saved = new Task();
		saved.setId("task-uuid");
		when(taskService.update("task-uuid", submitted)).thenReturn(saved);
		
		MethodOutcome outcome = provider.updateTask(new IdType("task-uuid"), submitted);
		
		assertFalse(outcome.getCreated());
		assertSame(saved, outcome.getResource());
		verify(taskService).update("task-uuid", submitted);
	}
	
	@Test
	void patchTaskShouldDelegateToService() {
		String body = "[{\"op\":\"replace\",\"path\":\"/status\",\"value\":\"completed\"}]";
		Task patched = new Task();
		patched.setId("task-uuid");
		when(taskService.patch("task-uuid", PatchTypeEnum.JSON_PATCH, body, requestDetails)).thenReturn(patched);
		
		MethodOutcome outcome = provider.patchTask(new IdType("task-uuid"), PatchTypeEnum.JSON_PATCH, body, requestDetails);
		
		assertFalse(outcome.getCreated());
		assertSame(patched, outcome.getResource());
		verify(taskService).patch("task-uuid", PatchTypeEnum.JSON_PATCH, body, requestDetails);
	}
	
	@Test
	void patchTaskShouldRejectMissingId() {
		assertThrows(InvalidRequestException.class,
		    () -> provider.patchTask(null, PatchTypeEnum.JSON_PATCH, "[]", requestDetails));
		assertThrows(InvalidRequestException.class,
		    () -> provider.patchTask(new IdType(), PatchTypeEnum.JSON_PATCH, "[]", requestDetails));
		verifyNoInteractions(taskService);
	}
	
	@Test
	void deleteTaskShouldThrowNotImplemented() {
		assertThrows(NotImplementedOperationException.class, () -> provider.deleteTask(new IdType("task-uuid")));
	}
}
