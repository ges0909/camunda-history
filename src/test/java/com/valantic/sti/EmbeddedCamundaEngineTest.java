package com.valantic.sti;

import com.valantic.sti.mapper.TestMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmbeddedCamundaEngineTest {

    static final Logger log = LoggerFactory.getLogger(EmbeddedCamundaEngineTest.class);

    ProcessEngineConfiguration configuration;
    ProcessEngine processEngine;

    HistoryService historyService;
    RuntimeService runtimeService;
    RepositoryService repositoryService;

    SqlSessionFactory sqlSessionFactory;

    @BeforeAll
    void setup() throws IOException {
        configureProcessEngine();
        getCamundaServices();
        registerMyBatisSqlMappers();
        deployTestBpmnModel();
    }

    @AfterAll
    void shutdownEngine() {
        if (processEngine != null) {
            processEngine.close();
        }
    }

    private void configureProcessEngine() {
        configuration = ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration()
                .setJdbcUrl("jdbc:mariadb://localhost:13306/camunda")
                .setJdbcUsername("camunda")
                .setJdbcPassword("camunda")
                .setJdbcDriver("org.mariadb.jdbc.Driver")
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE)
                .setJobExecutorActivate(false)
                .setHistory(ProcessEngineConfiguration.HISTORY_FULL);
        processEngine = configuration.buildProcessEngine();
        BpmnAwareTests.init(processEngine);
    }

    private void getCamundaServices() {
        historyService = processEngine.getHistoryService();
        repositoryService = processEngine.getRepositoryService();
        runtimeService = processEngine.getRuntimeService();
        sqlSessionFactory = ((ProcessEngineConfigurationImpl) configuration).getDbSqlSessionFactory().getSqlSessionFactory();
    }

    private void registerMyBatisSqlMappers() {
        sqlSessionFactory.getConfiguration().addMapper(TestMapper.class);
        // Show all mapped sql statements for debugging
        // sqlSessionFactory.getConfiguration().getMappedStatementNames()
        //        .forEach(statementName -> log.info("Mapped statement: {}", statementName));
    }

    private void deployTestBpmnModel() {
        repositoryService
                .createDeployment()
                .addClasspathResource("test-process.bpmn")
                .deploy();
    }

    @Test
    void shouldStartAndFinishProcess() {
        for (int i = 0; i < 10_000; i++) {
            ProcessInstance instance = runtimeService
                    .startProcessInstanceByKey("testProcess");
            BpmnAwareTests.assertThat(instance).isEnded();
        }
        // This should be equal 'BpmnAwareTests.assertThat(instance).isEnded()'
        // assertEquals(0, processEngine.getRuntimeService()
        //        .createProcessInstanceQuery()
        //        .processInstanceId(instance.getId())
        //        .count());
    }

    @Test
    void shouldFindLastHistoricTask() {
        List<Map<String, Object>> lastHistoricTaskOrFallback;
        try (SqlSession session = sqlSessionFactory.openSession()) {
            TestMapper testMapper = session.getMapper(TestMapper.class);
            lastHistoricTaskOrFallback = testMapper.findLastHistoricTask();
            log.info("Found: {}", lastHistoricTaskOrFallback);
        }
        List<HistoricTaskInstance> tasks = historyService
                .createHistoricTaskInstanceQuery()
                .orderByHistoricTaskInstanceEndTime()
                .desc()
                .listPage(0, 1);
        System.out.println(tasks);
    }

}

