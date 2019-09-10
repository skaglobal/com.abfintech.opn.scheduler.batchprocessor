package com.abfintech.opn.scheduler.job;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.abfintech.opn.scheduler.service.FundTransferBatchProposalConfigPublishService;

@Component
public class FundTransferBatchProposalConfigPublishJob {
	@Autowired
	private FundTransferBatchProposalConfigPublishService fundTransferBatchProposalConfigPublishService;

	@Scheduled(fixedRate = 60000, initialDelay = 1000)
	public void createBatches() {
		System.out.println("Treasurer. So batch job starts at = " + new Date());
		System.err.println("Treasurer. So batch job starts at = " + new Date());
		fundTransferBatchProposalConfigPublishService.publish();
	}
}