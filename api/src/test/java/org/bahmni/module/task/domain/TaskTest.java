/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.bahmni.module.task.domain;

import java.util.Date;
import org.bahmni.module.task.api.domain.Task;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Order;
import org.openmrs.User;

public class TaskTest {
	
	@Test
	public void shouldAddSubTasks() {
		Order radiologyOrder = new Order();
		Concept fulfilmentType = new Concept();
		Concept imagingType = new Concept();
		Concept reportingType = new Concept();
		
		Location requestedLocation = new Location();
		Task fulfillmentTask = createTask(radiologyOrder, Task.TaskStatus.READY, fulfilmentType, requestedLocation);
		Task imagingTask = createTask(radiologyOrder, Task.TaskStatus.DRAFT, imagingType, requestedLocation);
		Task reportingTask = createTask(radiologyOrder, Task.TaskStatus.DRAFT, reportingType, requestedLocation);
		fulfillmentTask.addSubTask(imagingTask);
		fulfillmentTask.addSubTask(reportingTask);
		Assert.assertEquals(2, fulfillmentTask.getSubTasks().size());
	}
	
	@Test(expected = RuntimeException.class)
	public void shouldThrowIfAddingSubtaskToItself() {
		Task root = new Task();
		root.addSubTask(root);
	}
	
	@Test(expected = RuntimeException.class)
	public void shouldThrowIfRepeatingSubtaskInHierarchy() {
		Task root = new Task();
		Task child = new Task();
		Task grandChild = new Task();
		root.addSubTask(child);
		child.addSubTask(grandChild);
		root.addSubTask(grandChild);
	}
	
	private Task createTask(Order order, Task.TaskStatus initialStatus, Concept taskType, Location location) {
		Task task = new Task();
		task.setStatus(initialStatus);
		task.setIntent(Task.Intent.ORDER);
		task.setDescription(taskType.getDisplayString());
		task.setTaskCode(taskType);
		task.setDateStarted(new Date());
		task.setOrder(order);
		task.setEncounter(null);
		task.setDateCreated(new Date());
		task.setCreator(new User());
		task.setPatient(order.getPatient());
		task.setLocation(location);
		Concept businessStatus = new Concept();
		businessStatus.setConceptId(999);
		task.setBusinessStatus(businessStatus);
		return task;
	}
}
