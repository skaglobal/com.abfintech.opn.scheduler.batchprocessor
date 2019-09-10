package com.abfintech.opn.scheduler.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = { "com.abfintech.abw.scheduler", "com.abfintech.abw.batch.confirmbatch", "com.abfintech.abw.batch.createbatchproposal",
		"com.abfintech.abw.batch.submitbatchproposal",
		"com.abfintech.core.kafka.consumer", "com.abfintech.abw.dal.batch", "com.abfintech.abw.dal.tdb"})
public class FundTransferBatchProposalSchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FundTransferBatchProposalSchedulerApplication.class, args);
	}

}