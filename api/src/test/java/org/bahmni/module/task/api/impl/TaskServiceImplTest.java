/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.bahmni.module.task.api.impl;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.bahmni.module.task.api.dao.TaskDao;
import org.bahmni.module.task.api.domain.Task;
import org.bahmni.module.task.api.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.Concept;
import org.openmrs.Order;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {
	
	@Mock
	private TaskDao dao;
	
	private TaskServiceImpl service;
	
	@BeforeEach
	void setUp() {
		service = new TaskServiceImpl(dao);
	}
	
	@Test
	void createOrUpdateShouldDelegateToDao() {
		Task task = new Task();
		when(dao.createOrUpdate(task)).thenReturn(task);
		
		Task result = service.createOrUpdate(task);
		
		assertSame(task, result);
		verify(dao).createOrUpdate(task);
	}
	
	@Test
	void findOrderTaskShouldDelegateToDao() {
		Order order = new Order();
		Concept taskCode = new Concept();
		List<Task> tasks = Collections.singletonList(new Task());
		when(dao.findOrderTask(order, taskCode)).thenReturn(tasks);
		
		List<Task> result = service.getTasks(order, taskCode);
		
		assertSame(tasks, result);
	}
	
	@Test
	void findByUuidShouldDelegateToDao() {
		Task task = new Task();
		when(dao.findByUuid("task-uuid")).thenReturn(task);
		
		Task result = service.findByUuid("task-uuid");
		
		assertSame(task, result);
	}
	
	@Test
	void findChildTasksByParentTaskShouldDelegateToDao() {
		Task parent = new Task();
		List<Task> children = Collections.singletonList(new Task());
		when(dao.findChildTasksByParentTask(parent)).thenReturn(children);
		
		List<Task> result = service.getSubTasks(parent);
		
		assertSame(children, result);
	}
}
