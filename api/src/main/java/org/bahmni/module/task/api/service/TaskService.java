/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.bahmni.module.task.api.service;

import java.util.List;
import org.bahmni.module.task.api.dao.TaskDao;
import org.bahmni.module.task.api.domain.Task;
import org.openmrs.Concept;
import org.openmrs.Order;

/**
 * Public, business-facing contract for the Task module. This is the API other modules should depend
 * on instead of the internal {@link TaskDao}.
 */
public interface TaskService {
	
	Task createOrUpdate(Task task);
	
	List<Task> getTasks(Order order, Concept taskCode);
	
	Task findByUuid(String uuid);
	
	List<Task> getSubTasks(Task parentTask);
}
