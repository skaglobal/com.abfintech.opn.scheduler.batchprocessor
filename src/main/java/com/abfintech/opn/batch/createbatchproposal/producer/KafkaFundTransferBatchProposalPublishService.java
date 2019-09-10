package com.abfintech.opn.batch.createbatchproposal.producer;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.abfintech.opn.dal.tdb.model.FundTransferInstructionDetails;
import com.abfintech.opn.dal.tdb.repository.LedgerRepository;
import com.abfintech.opn.model.batch.FundTransferBatchProposal;
import com.abfintech.opn.model.batch.FundTransferBatchProposalConfig;
import com.abfintech.opn.batch.submitbatchproposal.service.LedgerService;

@Component
public class KafkaFundTransferBatchProposalPublishService {

	@Autowired
	private LedgerService ledgerService;

	@Autowired
	private FundTransferBatchProposalHelper fundTransferBatchProposalHelper;

	@Autowired
	private LedgerRepository ledgerRepository;

	public void publishFundTransferBatchProposal(FundTransferBatchProposalConfig fundTransferBatchProposalConfig) {
		String memberBankBic = fundTransferBatchProposalConfig.getMemberBankBic();
		String currency = fundTransferBatchProposalConfig.getCurrency();
		System.out.println(memberBankBic + "---------" + currency);
		System.err.println(memberBankBic + "---------" + currency);

		//String participantPdc = fundTransferBatchProposalHelper.getParticipantPdc(memberBankBic);

		List<FundTransferInstructionDetails> outwardFundTransferInstructionDetailsList = ledgerRepository
				.findBySenderBicAndDebitCurrencyAndStatusGroupByPdcOrderByUpdatedOnDesc("DLT_INIT", memberBankBic,
						currency, null);
		int outwardFtoCount = outwardFundTransferInstructionDetailsList.size();

		List<FundTransferInstructionDetails> inwardFundTransferInstructionDetailsList = ledgerRepository
				.findByReceiverBicAndCreditCurrencyStatusGroupByPdcOrderByUpdatedOnDesc("DLT_INIT", memberBankBic,
						currency, null);
		int inwardFtoCount = inwardFundTransferInstructionDetailsList.size();

		if (outwardFtoCount == 0 && inwardFtoCount == 0) {
			// throw Exception
			System.out.println("Threre are no inward/outward records");
			System.err.println("Threre are no inward/outward records");
		} else {
			FundTransferBatchProposal fundTransferBatchProposal = fundTransferBatchProposalHelper.prepareProposal(
					memberBankBic, currency, outwardFundTransferInstructionDetailsList,
					inwardFundTransferInstructionDetailsList);

			System.out.println("FundTransferBatchProposal=" + fundTransferBatchProposal.toString());
			System.err.println("FundTransferBatchProposal=" + fundTransferBatchProposal.toString());
			ledgerService.sendCreateBatchProposal(fundTransferBatchProposal);
		}
	}

}