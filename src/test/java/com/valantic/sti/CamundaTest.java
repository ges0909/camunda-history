package com.valantic.sti;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CamundaTest {

    private ProcessEngine processEngine;
    private RepositoryService repositoryService;
    private RuntimeService runtimeService;

    @BeforeAll
    void setupProcessEngine() {
        ProcessEngineConfiguration configuration = ProcessEngineConfiguration
                .createStandaloneProcessEngineConfiguration();
        // Configure database
        configuration.setJdbcUrl("jdbc:mariadb://localhost:13306/camunda");
        configuration.setJdbcUsername("camunda");
        configuration.setJdbcPassword("camunda");
        configuration.setJdbcDriver("org.mariadb.jdbc.Driver");
        // Create camunda database scheme automatically, if it doesn't exist
        configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        // Mostly deactivated for tests
        configuration.setJobExecutorActivate(false);
        // Start the process engine
        processEngine = configuration.buildProcessEngine();

        repositoryService = processEngine.getRepositoryService();
        runtimeService = processEngine.getRuntimeService();

        // Initializes the test assertion library to allow usage of 'assertThat()'
        BpmnAwareTests.init(processEngine);
    }

    @Test
    void shouldStartAndFinishProcess() {
        // Deploy BPMN model
        repositoryService
                .createDeployment()
                .addClasspathResource("test-process.bpmn")
                .deploy();
        // Start process
        ProcessInstance instance = runtimeService
                .startProcessInstanceByKey("testProcess");

        BpmnAwareTests.assertThat(instance).isEnded();

        // This should be equal 'BpmnAwareTests.assertThat(instance).isEnded()'
        // assertEquals(0, processEngine.getRuntimeService()
        //        .createProcessInstanceQuery()
        //        .processInstanceId(instance.getId())
        //        .count());
    }

    @AfterAll
    void shutdownEngine() {
        processEngine.close();
    }
}

