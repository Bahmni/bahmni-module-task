/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) Thoughtworks Inc.
 */
package org.bahmni.module.task.api.dao.impl;

import java.util.List;
import org.bahmni.module.task.api.dao.TaskDao;
import org.bahmni.module.task.api.domain.Task;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.openmrs.Concept;
import org.openmrs.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("taskExtDao")
public class TaskDaoImpl implements TaskDao {
	
	private SessionFactory sessionFactory;
	
	@Autowired
	public TaskDaoImpl(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public Task createOrUpdate(Task task) {
		sessionFactory.getCurrentSession().saveOrUpdate(task);
		return task;
	}
	
	@Override
	public List<Task> findOrderTask(Order order, Concept taskCode) {
		Query<Task> taskQuery = sessionFactory.getCurrentSession()
		        .createQuery("SELECT t FROM Task t where t.order = :order AND t.taskCode = :code", Task.class);
		taskQuery.setParameter("order", order);
		taskQuery.setParameter("code", taskCode);
		return taskQuery.getResultList();
	}
	
	@Override
	public Task findByUuid(String uuid) {
		Query<Task> query = sessionFactory.getCurrentSession().createQuery("FROM Task WHERE uuid = :uuid", Task.class);
		query.setParameter("uuid", uuid);
		return query.uniqueResult();
	}
	
	@Override
	public List<Task> findChildTasksByParentTask(Task parentTask) {
		Query<Task> query = sessionFactory.getCurrentSession()
		        .createQuery("SELECT t FROM Task t WHERE t.partOf = :parentTask", Task.class);
		query.setParameter("parentTask", parentTask);
		return query.getResultList();
	}
}
