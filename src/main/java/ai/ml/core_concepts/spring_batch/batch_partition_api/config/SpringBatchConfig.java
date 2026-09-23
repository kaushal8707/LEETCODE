package ai.ml.core_concepts.spring_batch.batch_partition_api.config;//package com.zero.to.hero.springbatch.spring.batch.batch_partition_api.config;
//import com.zero.to.hero.springbatch.spring.batch.batch_partition_api.partition.ColumnRangePartitioner;
//import com.zero.to.hero.springbatch.spring.batch.batch_partition_api.repository.CustomerRepository;
//import lombok.AllArgsConstructor;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.job.builder.JobBuilder;
//import org.springframework.batch.core.partition.PartitionHandler;
//import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.batch.core.step.builder.StepBuilder;
//import org.springframework.batch.item.file.FlatFileItemReader;
//import org.springframework.batch.item.file.LineMapper;
//import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
//import org.springframework.batch.item.file.mapping.DefaultLineMapper;
//import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.FileSystemResource;
//import org.springframework.core.io.Resource;
//import org.springframework.core.io.ResourceLoader;
//import org.springframework.core.task.TaskExecutor;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//import org.springframework.transaction.PlatformTransactionManager;
//import com.zero.to.hero.springbatch.spring.batch.batch_partition_api.entity.Customer;
//import java.io.File;
//import java.io.IOException;
//
//@Configuration
//@AllArgsConstructor
//public class SpringBatchConfig {
//
//    private CustomerRepository customerRepository;
//    private final JobRepository jobRepository;
//    private PlatformTransactionManager transactionManager;
//
//    private CustomerWriter customerWriter;
//
//    @Autowired
//    ResourceLoader resourceLoader;
//
//    @Bean
//    public FlatFileItemReader<Customer> reader() throws IOException {
//        //Read resource
//        Resource classPathResource = resourceLoader.getResource("classpath:customers.csv");
//        //Get file content
//        File file = classPathResource.getFile();
//
//        FlatFileItemReader<Customer> itemReader = new FlatFileItemReader<>();
//        itemReader.setResource(new FileSystemResource(file));
//        itemReader.setName("csvReader");
//        itemReader.setLinesToSkip(1);
//        itemReader.setLineMapper(lineMapper());
//        return itemReader;
//    }
//
//    private LineMapper<Customer> lineMapper() {
//        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();
//
//        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
//        lineTokenizer.setDelimiter(",");
//        lineTokenizer.setStrict(false);
//        lineTokenizer.setNames("id", "firstName", "lastName", "email", "gender", "contactNo", "country", "dob");
//
//        BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
//        fieldSetMapper.setTargetType(Customer.class);
//
//        lineMapper.setLineTokenizer(lineTokenizer);
//        lineMapper.setFieldSetMapper(fieldSetMapper);
//        return lineMapper;
//
//    }
//
//    @Bean
//    public CustomerProcessor processor() {
//        return new CustomerProcessor();
//    }
//
//
//    @Bean
//    public ColumnRangePartitioner partitioner() {
//        return new ColumnRangePartitioner();
//    }
//
//    @Bean
//    public PartitionHandler partitionHandler() throws IOException {
//        TaskExecutorPartitionHandler taskExecutorPartitionHandler = new TaskExecutorPartitionHandler();
//        taskExecutorPartitionHandler.setGridSize(4);
//        taskExecutorPartitionHandler.setTaskExecutor(taskExecutor());
//        taskExecutorPartitionHandler.setStep(slaveStep());
//        return taskExecutorPartitionHandler;
//    }
//
//    @Bean
//    public Step slaveStep() throws IOException {
//        return new StepBuilder("slaveStep", jobRepository)
//                .<Customer, Customer>chunk(250, transactionManager)
//                .reader(reader())
//                .processor(processor())
//                .writer(customerWriter)
//                .build();
//    }
//
//    @Bean
//    public Step masterStep() throws IOException {
//        return new StepBuilder("masterSTep", jobRepository)
//                .partitioner(slaveStep().getName(), partitioner())
//                .partitionHandler(partitionHandler())
//                .build();
//    }
//
//    @Bean
//    public Job runJob() throws IOException {
//        return new JobBuilder("importCustomers", jobRepository)
//                .flow(masterStep()).end().build();
//
//    }
//
//    @Bean
//    public TaskExecutor taskExecutor() {
//        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
//        taskExecutor.setMaxPoolSize(4);
//        taskExecutor.setCorePoolSize(4);
//        taskExecutor.setQueueCapacity(4);
//        return taskExecutor;
//    }
//
//}
