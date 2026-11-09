/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) Thoughtworks Inc.
 */
package org.bahmni.module.task.api.dao;

import java.util.List;
import org.bahmni.module.task.api.domain.Task;
import org.openmrs.Concept;
import org.openmrs.Order;

public interface TaskDao {
	
	//same as fhirTaskDao.createOrUpdate(task)
	Task createOrUpdate(Task task);
	
	//same as fhirTaskDao.getTasks(Order order, Concept taskCode)
	List<Task> findOrderTask(Order order, Concept taskCode);
	
	//same as fhirTaskDao.get(uuid)
	Task findByUuid(String uuid);
	
	//same as fhirTaskDao.getSubTasks(parent)
	List<Task> findChildTasksByParentTask(Task parentTask);
}
