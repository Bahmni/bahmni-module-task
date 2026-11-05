# bahmni-module-task-extension

This module's objective is to provide specific implementation extensions. 


# Description
This module is intended as a replacement for default OpenMRS Task model and to expose a FHIR Task resource with typical operations.
As of now, this module is intended to be bundled independently onto openmrs. 

There are certain aspects that the OpenMRS Task implementation as part of fhir2 is not optimal and/or desirable. 
1. The scope of openmrs fhir_task model seems too narrow, and probably was intended to handle simple workflow like tracking a simple task by an external system.
2. Openmrs fhir_task does not allow for a task to have subtask. This is quite a common need - for example, a radiology order fulfilment task may consists of multiple tasks - scheduling, imaging, reporting tasks. 
3. To support polymorphic nature of references to task (reference elements that can point to multiple resource types), openmrs fhir_task stores all references in fhir_reference table. 
However, such references are hard to query or report using standard hibernate dialect or SQLs 
4. Openmrs fhir_task also lacks support for inputs for the task 


### Scope of this module
- Provides basic domain for task. Provides API to create, update or fetch tasks by different criteria
- This module is not intended to provide a shape or orchestration of any workflow. We opine such workflows are best defined for implementations


### Configuration


## Building from Source
You will need to have Java 1.8+ and Maven 2.x+ installed.  Use the command 'mvn package' to 
compile and package the module.  The .omod file will be in the omod/target folder.


## Installation
1. Build the module to produce the .omod file.
2. Use the OpenMRS Administration > Manage Modules screen to upload and install the .omod file.

If uploads are not allowed from the web (changable via a runtime property), you can drop the omod
into the ~/.OpenMRS/modules folder.  (Where ~/.OpenMRS is assumed to be the Application 
Data Directory that the running openmrs is currently using.)  After putting the file in there 
simply restart OpenMRS/tomcat and the module will be loaded and started.
