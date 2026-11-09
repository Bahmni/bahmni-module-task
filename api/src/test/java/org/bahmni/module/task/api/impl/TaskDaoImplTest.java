/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) Thoughtworks Inc. 
 */
package org.bahmni.module.task.api.impl;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bahmni.module.task.api.dao.impl.TaskDaoImpl;
import org.bahmni.module.task.api.domain.Task;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.Concept;
import org.openmrs.Order;

@ExtendWith(MockitoExtension.class)
class TaskDaoImplTest {
	
	@Mock
	private SessionFactory sessionFactory;
	
	@Mock
	private Session session;
	
	@Mock
	private Query<Task> query;
	
	private TaskDaoImpl dao;
	
	@BeforeEach
	void setUp() {
		dao = new TaskDaoImpl(sessionFactory);
		when(sessionFactory.getCurrentSession()).thenReturn(session);
	}
	
	@Test
	void createOrUpdateShouldSaveOrUpdateAndReturnTheSameTask() {
		Task task = new Task();
		
		Task result = dao.createOrUpdate(task);
		
		verify(session).saveOrUpdate(task);
		assertSame(task, result);
	}
	
	@Test
	void findOrderTaskShouldFilterByOrderAndTaskCode() {
		Order order = new Order();
		Concept taskCode = new Concept();
		when(session.createQuery("SELECT t FROM Task t where t.order = :order AND t.taskCode = :code", Task.class))
		        .thenReturn(query);
		when(query.setParameter("order", order)).thenReturn(query);
		when(query.setParameter("code", taskCode)).thenReturn(query);
		
		dao.findOrderTask(order, taskCode);
		
		verify(query).setParameter("order", order);
		verify(query).setParameter("code", taskCode);
		verify(query).getResultList();
	}
	
	@Test
	void findByUuidShouldReturnUniqueResult() {
		Task task = new Task();
		when(session.createQuery("FROM Task WHERE uuid = :uuid", Task.class)).thenReturn(query);
		when(query.setParameter("uuid", "task-uuid")).thenReturn(query);
		when(query.uniqueResult()).thenReturn(task);
		
		Task result = dao.findByUuid("task-uuid");
		
		assertSame(task, result);
	}
	
	@Test
	void findChildTasksByParentTaskShouldFilterByPartOf() {
		Task parent = new Task();
		when(session.createQuery("SELECT t FROM Task t WHERE t.partOf = :parentTask", Task.class)).thenReturn(query);
		when(query.setParameter("parentTask", parent)).thenReturn(query);
		
		dao.findChildTasksByParentTask(parent);
		
		verify(query).setParameter("parentTask", parent);
		verify(query).getResultList();
	}
}
