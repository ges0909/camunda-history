package com.valantic.sti.benchmark;

import com.valantic.sti.mybatis.mapper.WarrantMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class HistoryBenchmark {

    ProcessEngine processEngine;
    SqlSessionFactory sqlSessionFactory;
    RepositoryService repositoryService;
    RuntimeService runtimeService;
    HistoryService historyService;

    Random random = new Random();
    String randomBusinessKey = randomBusinessKey(1, 500_000);

    @Setup(Level.Trial)
    public void setup() {
        ProcessEngineConfiguration configuration = ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration()
                .setJdbcUrl("jdbc:mariadb://localhost:13306/camunda")
                .setJdbcUsername("camunda")
                .setJdbcPassword("camunda")
                .setJdbcDriver("org.mariadb.jdbc.Driver")
                // Create camunda database scheme automatically, if it doesn't exist
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE)
                // Mostly deactivated for tests
                .setJobExecutorActivate(false)
                // Set history level
                .setHistory(ProcessEngineConfiguration.HISTORY_FULL);
        processEngine = configuration.buildProcessEngine();

        sqlSessionFactory = ((ProcessEngineConfigurationImpl) configuration).getDbSqlSessionFactory().getSqlSessionFactory();
        sqlSessionFactory.getConfiguration().addMapper(WarrantMapper.class);

        repositoryService = processEngine.getRepositoryService();
        runtimeService = processEngine.getRuntimeService();
        historyService = processEngine.getHistoryService();

        // Deploy BPMN model for test
        repositoryService
                .createDeployment()
                .addClasspathResource("test-process.bpmn")
                .deploy();
    }

    @TearDown(Level.Trial)
    public void teardown() {
        if (processEngine != null) {
            processEngine.close();
        }
    }

    @Benchmark
    public void findAllHistoricProcessInstances() {
        historyService
                .createHistoricProcessInstanceQuery()
                // .processDefinitionKey("testProcess")
                .processInstanceBusinessKey(randomBusinessKey)
                .list();
    }

    @Benchmark
    public void findSingleHistoricProcessInstances() {
        historyService
                .createHistoricProcessInstanceQuery()
                // .processDefinitionKey("testProcess")
                .processInstanceBusinessKey(randomBusinessKey)
                .singleResult();
    }

    Integer selectMaxWarrantNumber() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Integer maxWarrantNumber = session.getMapper(WarrantMapper.class).selectMaxWarrantNumber();
            return maxWarrantNumber == null ? 0 : maxWarrantNumber;
        }
    }

    String randomBusinessKey(int min, int max) {
        return "BESCHLUSS_" + random.nextInt(max - min) + min;
    }
}
