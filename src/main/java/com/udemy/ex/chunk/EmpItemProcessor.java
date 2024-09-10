package com.udemy.ex.chunk;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.udemy.ex.model.Employee;

import lombok.extern.slf4j.Slf4j;

@Component("EmpItemProcessor")
@StepScope
@Slf4j
public class EmpItemProcessor implements ItemProcessor<Employee, Employee> {@Override
	
	public Employee process(Employee item) throws Exception {
		log.info("Processing ... {}", item);
		item.setJobTitle(item.getJobTitle().toUpperCase());
		return item;
	}

}
