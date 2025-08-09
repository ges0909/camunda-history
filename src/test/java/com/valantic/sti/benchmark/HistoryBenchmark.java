package com.valantic.sti.benchmark;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class HistoryBenchmark {

    private ProcessEngine processEngine;
    private RepositoryService repositoryService;
    private RuntimeService runtimeService;

    @Setup(Level.Trial)
    public void setup() {
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
        // Set history level
        configuration.setHistory(ProcessEngineConfiguration.HISTORY_FULL);
        // Start the process engine
        processEngine = configuration.buildProcessEngine();

        repositoryService = processEngine.getRepositoryService();
        runtimeService = processEngine.getRuntimeService();
    }

    @TearDown(Level.Trial)
    public void teardown() {
        processEngine.close();
    }

    /*
     * docker-compose up -d mariadb
     */
    @Benchmark
    public void benchmarkStartBpmnProcess() {
        // Deploy BPMN model
        repositoryService
                .createDeployment()
                .addClasspathResource("test-process.bpmn")
                .deploy();
        // Start process
        ProcessInstance instance = runtimeService
                .startProcessInstanceByKey("testProcess");
    }
}
