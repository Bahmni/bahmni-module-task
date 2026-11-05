/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.bahmni.module.task.fhir2.dao;

import static org.bahmni.module.task.constants.TaskPrivilegeConstants.EDIT_TASKS;
import static org.bahmni.module.task.constants.TaskPrivilegeConstants.GET_TASKS;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import org.bahmni.module.task.api.domain.Task;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

public interface FhirTaskDao extends FhirDao<Task> {
	
	@Authorized({ GET_TASKS })
	@Override
	Task get(String uuid);
	
	@Authorized({ GET_TASKS })
	@Override
	List<Task> get(Collection<String> uuids);
	
	@Authorized({ GET_TASKS })
	@Override
	List<Task> getSearchResults(SearchParameterMap searchParameterMap);
	
	@Authorized({ GET_TASKS })
	@Override
	int getSearchResultsCount(SearchParameterMap searchParameterMap);
	
	@Authorized({ EDIT_TASKS })
	@Override
	Task createOrUpdate(@Nonnull Task newEntry);
	
	@Authorized({ EDIT_TASKS })
	@Override
	Task delete(@Nonnull String uuid);
	
}
