package com.abfintech.opn.scheduler.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = { "com.abfintech.opn.scheduler", "com.abfintech.opn.batch.confirmbatch", "com.abfintech.opn.batch.createbatchproposal",
		"com.abfintech.opn.batch.submitbatchproposal",
		"com.abfintech.core.kafka.consumer", "com.abfintech.opn.dal.batch", "com.abfintech.opn.dal.tdb"})
public class FundTransferBatchProposalSchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FundTransferBatchProposalSchedulerApplication.class, args);
	}

}