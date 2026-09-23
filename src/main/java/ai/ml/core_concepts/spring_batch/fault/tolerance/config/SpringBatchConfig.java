package ai.ml.core_concepts.spring_batch.fault.tolerance.config;//package com.zero.to.hero.springbatch.spring.batch.fault.tolerance.config;
//import com.zero.to.hero.springbatch.spring.batch.basics.entity.Customer;
//import com.zero.to.hero.springbatch.spring.batch.basics.repository.CustomerRepository;
//import com.zero.to.hero.springbatch.spring.batch.fault.tolerance.listener.StepSkipListener;
//import lombok.AllArgsConstructor;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.SkipListener;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.job.builder.JobBuilder;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.batch.core.step.builder.StepBuilder;
//import org.springframework.batch.core.step.skip.SkipPolicy;
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
//import org.springframework.transaction.PlatformTransactionManager;
//import java.io.File;
//import java.io.IOException;
//
//@Configuration
//@AllArgsConstructor
//public class SpringBatchConfig {
//
//    private JobRepository jobRepository;
//    private PlatformTransactionManager transactionManager;
//    private CustomerRepository customerRepository;
//    private CustomerItemWriter customerItemWriter;
//
//    @Autowired
//    ResourceLoader resourceLoader;
//
//    @Bean
//    public FlatFileItemReader<Customer> itemReader() throws IOException {
//        //get Resource
//        Resource resource=resourceLoader.getResource("classpath:customers.csv");
//        //get file content
//        File file=resource.getFile();
//        FlatFileItemReader<Customer> flatFileItemReader = new FlatFileItemReader<>();
//        flatFileItemReader.setResource(new FileSystemResource(file));
//        flatFileItemReader.setName("CSV-Reader");
//        flatFileItemReader.setLinesToSkip(1);
//        flatFileItemReader.setLineMapper(lineMapper());
//        return flatFileItemReader;
//    }
//
//    private LineMapper<Customer> lineMapper() {
//        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();
//
//        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
//        lineTokenizer.setDelimiter(",");
//        lineTokenizer.setStrict(false);
//        lineTokenizer.setNames("id", "firstName", "lastName", "email", "gender", "contactNo", "country", "dob", "age");
//
//        BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
//        fieldSetMapper.setTargetType(Customer.class);
//
//        lineMapper.setLineTokenizer(lineTokenizer);
//        lineMapper.setFieldSetMapper(fieldSetMapper);
//
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
//        return new StepBuilder("slaveStep", jobRepository)
//                .<Customer, Customer>chunk(10, transactionManager)
//                .reader(itemReader())
//                .processor(processor())
//                .writer(customerItemWriter)
//                .faultTolerant()
//                //.skipLimit(100)
//                //.skip(NumberFormatException.class)
//                //.noSkip(IllegalArgumentException.class)
//                .listener(skipListener())
//                .skipPolicy(skipPolicy())
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
//    public SkipPolicy skipPolicy(){
//        return new ExceptionSkipPolicy();
//    }
//
//    @Bean
//    public SkipListener skipListener(){
//        return new StepSkipListener();
//    }
//}
