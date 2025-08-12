package com.valantic.sti;

import com.valantic.sti.mybatis.mapper.DatabaseConfigMapper;
import com.valantic.sti.mybatis.mapper.HistoryMapper;
import com.valantic.sti.mybatis.mapper.WarrantMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EmbeddedCamundaEngineTest {

    static final Logger log = LoggerFactory.getLogger(EmbeddedCamundaEngineTest.class);

    static final String BUSINESS_KEY = "businessKey";
    static final String APPROVED = "approved";

    ProcessEngine processEngine;

    RepositoryService repositoryService;
    RuntimeService runtimeService;
    HistoryService historyService;

    SqlSessionFactory sqlSessionFactory;

    Random random = new Random();

    @BeforeAll
    void setup() {
        ProcessEngineConfiguration configuration = ProcessEngineConfiguration
                .createStandaloneProcessEngineConfiguration()
                .setJdbcUrl("jdbc:mariadb://localhost:13306/camunda")
                .setJdbcUsername("camunda")
                .setJdbcPassword("camunda")
                .setJdbcDriver("org.mariadb.jdbc.Driver")
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE) // Create camunda database scheme automatically, if it doesn't exist
                .setJobExecutorActivate(false) // Mostly deactivated for tests
                .setHistory(ProcessEngineConfiguration.HISTORY_FULL);
        processEngine = configuration.buildProcessEngine();
        BpmnAwareTests.init(processEngine);

        sqlSessionFactory = ((ProcessEngineConfigurationImpl) configuration)
                .getDbSqlSessionFactory()
                .getSqlSessionFactory();

        // Register MyBatis mappers
        sqlSessionFactory.getConfiguration().addMapper(DatabaseConfigMapper.class);
        sqlSessionFactory.getConfiguration().addMapper(WarrantMapper.class);
        sqlSessionFactory.getConfiguration().addMapper(HistoryMapper.class);

        // MyBatis debugging: Show all mapped sql statements
        sqlSessionFactory.getConfiguration().getMappedStatementNames()
                .forEach(statementName -> log.info("Mapped statement: {}", statementName));

        repositoryService = processEngine.getRepositoryService();
        runtimeService = processEngine.getRuntimeService();
        historyService = processEngine.getHistoryService();

        // Deploy BPMN model for test
        repositoryService
                .createDeployment()
                .addClasspathResource("test-process.bpmn")
                .deploy();
    }

    @AfterAll
    void shutdownEngine() {
        if (processEngine != null) {
            processEngine.close();
        }
    }

    @Test
    @DisplayName("Generate process instances to populate history tables")
    @Order(1)
    void generateProcessInstances() {
        int startNumber = selectMaxWarrantNumber() + 1;
        for (int i = startNumber; i < startNumber + 2; i++) {
            Map<String, Object> variables = Map.of(
                    BUSINESS_KEY, "BESCHLUSS_" + i,
                    APPROVED, false
            );
            var instance = runtimeService
                    // .startProcessInstanceByKey("testProcess", businessKey, variables);
                    .startProcessInstanceByKey("testProcess", variables);
            BpmnAwareTests.assertThat(instance).isEnded();
        }
    }

    @Test
    @DisplayName("Find all process instances by process definition key and business key")
    @Order(2)
    void findAllProcessInstancesByProcessDefinitionKeyAndBusinessKey() {
        List<HistoricProcessInstance> historicProcessInstances = historyService
                .createHistoricProcessInstanceQuery()
                .variableValueEquals(BUSINESS_KEY, "BESCHLUSS_1")
                .variableValueEquals(APPROVED, false)
                .list();
        assertThat(historicProcessInstances).isNotEmpty();
    }

    @Disabled("works only for the first time after initial database setup")
    @Test
    @DisplayName("Find first process instance by process definition key and business key")
    @Order(2)
    void findFirstProcessInstanceByProcessDefinitionKeyAndBusinessKey() {
        HistoricProcessInstance historicProcessInstance = historyService
                .createHistoricProcessInstanceQuery()
                .variableValueEquals(BUSINESS_KEY, "BESCHLUSS_1")
                .variableValueEquals(APPROVED, false)
                .singleResult();
        Assertions.assertNotNull(historicProcessInstance);
    }

    Integer selectMaxWarrantNumber() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Integer maxWarrantNumber = session.getMapper(WarrantMapper.class).selectMaxWarrantNumber();
            return maxWarrantNumber == null ? 0 : maxWarrantNumber;
        }
    }

    // Access denied; you need (at least one of) the SUPER privilege(s) for this operation
    void enableGeneralLog() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            DatabaseConfigMapper testMapper = session.getMapper(DatabaseConfigMapper.class);
            testMapper.setGeneralLog("ON");
            testMapper.setGeneralLogFile("/var/log/mysql/general.log");
        }
    }

    // Access denied; you need (at least one of) the SUPER privilege(s) for this operation
    void disableGeneralLog() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            DatabaseConfigMapper testMapper = session.getMapper(DatabaseConfigMapper.class);
            testMapper.setGeneralLog("OFF");
        }
    }

    // Access denied; you need (at least one of) the SUPER privilege(s) for this operation
    void enableSlowQueryLog() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            DatabaseConfigMapper testMapper = session.getMapper(DatabaseConfigMapper.class);
            testMapper.setSlowQueryLog("ON");
            testMapper.setSlowQueryLogFile("/var/log/mysql/slow-query.log");
        }
    }

    // Access denied; you need (at least one of) the SUPER privilege(s) for this operation
    void disableSlowQueryLog() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            DatabaseConfigMapper testMapper = session.getMapper(DatabaseConfigMapper.class);
            testMapper.setSlowQueryLog("OFF");
        }
    }
}
