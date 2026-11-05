/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.bahmni.module.task.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Provider;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({ "subTasks", "inputs" })
public class Task extends BaseOpenmrsData {
	
	private Integer taskId;
	
	private Concept taskCode;
	
	private Concept businessStatus;
	
	private TaskStatus status;
	
	private Intent intent;
	
	private Priority priority;
	
	private Patient patient;
	
	private Date dateStarted;
	
	private Date dateEnded;
	
	private Location location;
	
	private String description;
	
	private Encounter encounter;
	
	private Order order;
	
	private Task partOf;
	
	private Provider owner;
	
	private Set<Task> subTasks;
	
	private Set<TaskInput> inputs;
	
	@Override
	public Integer getId() {
		return taskId;
	}
	
	@Override
	public void setId(Integer id) {
		setTaskId(id);
	}
	
	public void addSubTask(Task subTask) {
		if (subTask == null) {
			return;
		}
		
		if (subTasks == null) {
			subTasks = new HashSet<>();
		}
		
		if (subTask.equals(this)) {
			throw new RuntimeException("Task.cannot.be.its.own.child");
		}
		// Traverse all the way up (down?) to the root, then check whether the child is already
		// anywhere in the tree
		Task root = this;
		while (root.getPartOf() != null) {
			root = root.getPartOf();
		}
		
		if (isInHierarchy(subTask, root)) {
			throw new RuntimeException("Task.hierarchy.loop");
		}
		
		subTask.setPartOf(this);
		subTasks.add(subTask);
	}
	
	public static Boolean isInHierarchy(Task aTask, Task root) {
		if (root == null) {
			return false;
		}
		while (true) {
			if (aTask == null) {
				return false;
			} else if (root.equals(aTask)) {
				return true;
			}
			aTask = aTask.getPartOf();
		}
	}
	
	public enum TaskStatus {
		DRAFT,
		REQUESTED,
		RECEIVED,
		ACCEPTED,
		REJECTED,
		READY,
		CANCELLED,
		INPROGRESS,
		ONHOLD,
		FAILED,
		COMPLETED,
		ENTEREDINERROR;
	}
	
	public enum Intent {
		ORDER;
	}
	
	public enum Priority {
		ROUTINE,
		URGENT,
		ASAP,
		STAT;
	}
	
	public static final Set<Task.TaskStatus> TERMINAL_TASK_STATUSES = EnumSet.of(Task.TaskStatus.FAILED,
	    Task.TaskStatus.CANCELLED, Task.TaskStatus.ENTEREDINERROR, Task.TaskStatus.COMPLETED);
	
	public boolean areSubTasksCompleted() {
		if (this.subTasks == null) {
			return true;
		}
		long count = this.subTasks.stream().filter(task -> !TERMINAL_TASK_STATUSES.contains(task.getStatus())).count();
		return count == 0;
	}
}
