/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) Thoughtworks Inc.
 */
package org.bahmni.module.task.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;

@Getter
@Setter
@NoArgsConstructor
public class TaskInput extends BaseOpenmrsData {
	
	private Integer id;
	
	private Task task;
	
	private Concept inputType;
	
	private String valueString;
}
