/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.variables;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.charset.Charset;
import java.util.Scanner;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Test;

/**
 * @author Ronny Bräunlich
 *
 */
public class FileValueProcessSerialiazationTest extends PluggableProcessEngineTestCase {

  @Test
  public void testSerializeFileVariable() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("process").startEvent().userTask().endEvent().done();
    Deployment deployment = repositoryService.createDeployment().addModelInstance("process.bpmn", modelInstance).deploy();
    VariableMap variables = Variables.createVariables();
    String filename = "test.txt";
    String type = "text/plain";
    FileValue fileValue = Variables.fileValue(filename).file("ABC".getBytes()).encoding("UTF-8").mimeType(type).create();
    variables.put("file", fileValue);
    runtimeService.startProcessInstanceByKey("process", variables);
    Task task = taskService.createTaskQuery().singleResult();
    VariableInstance result = runtimeService.createVariableInstanceQuery().processInstanceIdIn(task.getProcessInstanceId()).singleResult();
    FileValue value = (FileValue) result.getTypedValue();

    assertThat(value.getFilename(), is(filename));
    assertThat(value.getMimeType(), is(type));
    assertThat(value.getEncoding(), is(Charset.forName("UTF-8")));
    Scanner scanner = new Scanner(value.getValue());
    assertThat(scanner.nextLine(), is("ABC"));

    // clean up
    repositoryService.deleteDeployment(deployment.getId(), true);
  }

}
