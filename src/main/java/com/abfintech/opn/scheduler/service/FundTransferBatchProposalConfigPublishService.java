package com.abfintech.opn.scheduler.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abfintech.opn.dal.batch.entity.FundTransferBatchProposalConfig;
import com.abfintech.opn.dal.batch.repository.FundTransferBatchProposalConfigRepository;
import com.abfintech.opn.batch.createbatchproposal.producer.KafkaFundTransferBatchProposalPublishService;

@Service
public class FundTransferBatchProposalConfigPublishService {

	@Value("${spring.application.name}")
	private String applicationname;

	@Autowired
	private FundTransferBatchProposalConfigRepository fundTransferBatchProposalConfigRepository;

	@Autowired
	private KafkaFundTransferBatchProposalPublishService kafkaFundTransferBatchProposalPublishService;

	public void publish() {
		List<FundTransferBatchProposalConfig> fundTransferBatchProposalConfigList = fundTransferBatchProposalConfigRepository
				.findAll();

		if (null != fundTransferBatchProposalConfigList && !fundTransferBatchProposalConfigList.isEmpty()) {
			for (FundTransferBatchProposalConfig fundTransferBatchProposalConfig : fundTransferBatchProposalConfigList) {

				com.abfintech.opn.model.batch.FundTransferBatchProposalConfig fundTransferBatchProposalConfigNew = new com.abfintech.opn.model.batch.FundTransferBatchProposalConfig();
				fundTransferBatchProposalConfigNew.setMemberBankBic(fundTransferBatchProposalConfig.getMemberBankBic());
				fundTransferBatchProposalConfigNew.setCurrency(fundTransferBatchProposalConfig.getCurrency());

				System.out.println(new Date() + "FundTransferBatchProposalConfig="
						+ fundTransferBatchProposalConfigNew);
				System.err.println(new Date() + "FundTransferBatchProposalConfig="
						+ fundTransferBatchProposalConfigNew);
				kafkaFundTransferBatchProposalPublishService
						.publishFundTransferBatchProposal(fundTransferBatchProposalConfigNew);
			}
		} else {
			System.out.println("There is no batch config available.");
			System.err.println("There is no batch config available.");
		}

	}

}
