package ai.ml.core_concepts.spring_batch.basics.config;//package com.zero.to.hero.springbatch.spring.batch.basics.config;
//
//
//import com.zero.to.hero.springbatch.spring.batch.basics.repository.CustomerRepository;
//import lombok.AllArgsConstructor;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.job.builder.JobBuilder;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.batch.core.step.builder.StepBuilder;
//import org.springframework.batch.item.data.RepositoryItemWriter;
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
//import org.springframework.core.task.SimpleAsyncTaskExecutor;
//import org.springframework.core.task.TaskExecutor;
//import org.springframework.transaction.PlatformTransactionManager;
//import com.zero.to.hero.springbatch.spring.batch.basics.entity.Customer;
//import java.io.File;
//import java.io.IOException;
//
//@Configuration
//@AllArgsConstructor
////@EnableBatchProcessing
//public class SpringBatchConfig {
//
//    private CustomerRepository customerRepository;
//    private final JobRepository jobRepository;
//    private PlatformTransactionManager transactionManager;
//    @Autowired
//    ResourceLoader resourceLoader;
//
//    @Bean
//    public FlatFileItemReader<Customer> reader() throws IOException {
//        //get Resource
//        Resource resource=resourceLoader.getResource("classpath:customers.csv");
//        //read File Content
//        File file = resource.getFile();
//
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
//    }
//
//    @Bean
//    public CustomerProcessor processor() {
//        return new CustomerProcessor();
//    }
//
//    @Bean
//    public RepositoryItemWriter<Customer> writer() {
//        RepositoryItemWriter<Customer> writer = new RepositoryItemWriter<>();
//        writer.setRepository(customerRepository);
//        writer.setMethodName("save");
//        return writer;
//    }
//
//    @Bean
//    public Step step1() throws IOException {
//        return new StepBuilder("csv-step", jobRepository)
//                .<Customer, Customer>chunk(10, transactionManager)
//                .reader(reader())
//                .processor(processor())
//                .writer(writer())
//                .taskExecutor(taskExecutor())
//                .build();
//    }
//
//    @Bean
//    public Job runJob() throws IOException {
//        return new JobBuilder("importCustomers", jobRepository)
//                .flow(step1()).end().build();
//    }
//
//    @Bean
//    public TaskExecutor taskExecutor() {
//        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
//        asyncTaskExecutor.setConcurrencyLimit(10);
//        return asyncTaskExecutor;
//    }
//}