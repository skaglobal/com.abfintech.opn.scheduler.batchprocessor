package com.abfintech.opn.batch.confirmbatch.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abfintech.opn.model.dlt.Notifications;
import com.abfintech.core.kafka.consumer.processor.KafkaMessageProcessor;
import com.abfintech.core.model.AbBaseModel;
import com.abfintech.opn.batch.submitbatchproposal.service.LedgerService;

@Service("kafkaMessageProcessor")
public class KafkaFundTransferBatchStatusConsumer implements KafkaMessageProcessor {

	@Autowired
	private LedgerService ledgerService;

	@Override
	public void processMessage(AbBaseModel baseModel) {
		System.out.println("Treasurer. Confirm batch consumer.");
		System.err.println("Treasurer. Confirm batch consumer.");
		Notifications notifications = (Notifications) baseModel;
		System.err.println("Consumed Notifications from B5=" + notifications.toString());
		System.out.println("Consumed Notifications from B5=" + notifications.toString());
		ledgerService.sendConfirmBatch(notifications);
	}

}