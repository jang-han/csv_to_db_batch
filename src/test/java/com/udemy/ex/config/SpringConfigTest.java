package com.udemy.ex.config;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import com.udemy.ex.model.Employee;

@RunWith(MockitoJUnitRunner.class)
public class SpringConfigTest {

    @Mock
    private JobLauncher jobLauncher;
    
    @Mock
    private JobRepository jobRepository;
    
    @Mock
    private PlatformTransactionManager transactionManager;
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private ItemProcessor<Employee, Employee> empItemProcessor;
    
    @Mock
    private Resource inputCSV;
    
    @InjectMocks
    private SpringConfig springConfig;
    
    @Before
    public void setUp() {
        when(inputCSV.exists()).thenReturn(true);
    }
    
    @Test
    public void testCsvItemReader() throws Exception {
        FlatFileItemReader<Employee> reader = springConfig.csvItemReader();
        
        // 리플렉션을 통해 getEncoding 메서드 호출
        Method getEncodingMethod = reader.getClass().getDeclaredMethod("getEncoding");
        getEncodingMethod.setAccessible(true);
        String encoding = (String) getEncodingMethod.invoke(reader);
        assertThat(encoding).isEqualTo(StandardCharsets.UTF_8.name());
        
        // 리플렉션을 통해 LineMapper 가져오기
        Field lineMapperField = reader.getClass().getDeclaredField("lineMapper");
        lineMapperField.setAccessible(true);
        Object lineMapper = lineMapperField.get(reader);
        
        // LineMapper의 내부 클래스에서 FieldSetMapper 가져오기
        Method getFieldSetMapperMethod = lineMapper.getClass().getDeclaredMethod("getFieldSetMapper");
        getFieldSetMapperMethod.setAccessible(true);
        Object fieldSetMapper = getFieldSetMapperMethod.invoke(lineMapper);
        
        // FieldSetMapper의 TargetType 확인
        Method getTargetTypeMethod = fieldSetMapper.getClass().getDeclaredMethod("getTargetType");
        getTargetTypeMethod.setAccessible(true);
        Class<?> targetType = (Class<?>) getTargetTypeMethod.invoke(fieldSetMapper);
        assertThat(targetType).isEqualTo(Employee.class);
        
        // DelimitedLineTokenizer의 Names 확인
        Method getLineTokenizerMethod = lineMapper.getClass().getDeclaredMethod("getLineTokenizer");
        getLineTokenizerMethod.setAccessible(true);
        Object lineTokenizer = getLineTokenizerMethod.invoke(lineMapper);
        
        Method getNamesMethod = lineTokenizer.getClass().getDeclaredMethod("getNames");
        getNamesMethod.setAccessible(true);
        String[] names = (String[]) getNamesMethod.invoke(lineTokenizer);
        assertThat(names).containsExactly("EmpNumber", "EmpName", "JobTitle", "MgrNumber", "HireDate");
    }
    
    @Test
    public void testJdbcItemWriter() throws Exception {
        JdbcBatchItemWriter<Employee> writer = springConfig.jdbcItemWriter();
        
        // 리플렉션을 통해 DataSource 확인
        Field dataSourceField = writer.getClass().getDeclaredField("dataSource");
        dataSourceField.setAccessible(true);
        DataSource writerDataSource = (DataSource) dataSourceField.get(writer);
        assertThat(writerDataSource).isEqualTo(dataSource);
        
        // 리플렉션을 통해 SQL 확인
        Field sqlField = writer.getClass().getDeclaredField("sql");
        sqlField.setAccessible(true);
        String sql = (String) sqlField.get(writer);
        assertThat(sql).isEqualTo("INSERT INTO employee (empnumber, empname, jobtitle, mgrnumber, hiredate) VALUES (:empNumber, :empName, :jobTitle, :mgrNumber, :hireDate)");
    }
    
    @Test
    public void testChunkStep1() {
        Step step = springConfig.chunkStep1();
        
        assertThat(step).isNotNull();
        assertThat(step.getName()).isEqualTo("EmpImportStep1");
    }
    
    @Test
    public void testChunkJob() {
        Job job = springConfig.chunk();
        
        assertThat(job).isNotNull();
        assertThat(job.getName()).isEqualTo("chunkJob");
    }
}
