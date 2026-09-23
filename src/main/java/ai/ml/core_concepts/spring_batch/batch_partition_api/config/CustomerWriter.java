package ai.ml.core_concepts.spring_batch.batch_partition_api.config;//package com.zero.to.hero.springbatch.spring.batch.batch_partition_api.config;
//import com.zero.to.hero.springbatch.spring.batch.batch_partition_api.entity.Customer;
//import com.zero.to.hero.springbatch.spring.batch.batch_partition_api.repository.CustomerRepository;
//import org.springframework.batch.item.Chunk;
//import org.springframework.batch.item.ItemWriter;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Component
//public class CustomerWriter implements ItemWriter<Customer> {
//
//    @Autowired
//    private CustomerRepository customerRepository;
//
//    @Override
//    public void write(Chunk<? extends Customer> chunk) throws Exception {
//        System.out.println("Thread Name : -"+Thread.currentThread().getName());
//        customerRepository.saveAll(chunk);
//    }
//}
