package com.abfintech.opn.batch.createbatchproposal.producer;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.abfintech.abw.dal.tdb.entity.ParticipantRelation;
import com.abfintech.abw.dal.tdb.model.FundTransferInstructionDetails;
import com.abfintech.abw.dal.tdb.repository.ParticipantRelationRepository;
import com.abfintech.abw.model.batch.FundTransferBatchProposal;
import com.abfintech.abw.model.batch.FundTransferBatchProposalDetails;

@Component
public class FundTransferBatchProposalHelper {

	@Autowired
	private ParticipantRelationRepository participantRelationRepository;

	public String getParticipantPdc(String memberBankBic) {
		ParticipantRelation participantRelation = participantRelationRepository.findByPartyID1_PartyBIC(memberBankBic);
		String participantPdc = participantRelation.getPdc();
		return participantPdc;
	}

	public FundTransferBatchProposal prepareProposal(String memberBankBic, String currency,
			List<FundTransferInstructionDetails> owpFtoList, List<FundTransferInstructionDetails> iwpFtoList) {

		Date stopWatch = null;

		Map<String, FundTransferInstructionDTO> outwardPdcFundTransferInstructionDTOMap = getPdcFundTransferInstructionDTOMap(
				owpFtoList, true);
		Map<String, FundTransferInstructionDTO> inwardPdcFundTransferInstructionDTOMap = getPdcFundTransferInstructionDTOMap(
				iwpFtoList, false);

		Map<String, FundTransferBatchProposalDetails> pdcMap = new HashMap<String, FundTransferBatchProposalDetails>();

		if (!outwardPdcFundTransferInstructionDTOMap.isEmpty()) {
			for (Map.Entry<String, FundTransferInstructionDTO> entry : outwardPdcFundTransferInstructionDTOMap
					.entrySet()) {

				List<String> iwpFtoIdList = null;
				BigDecimal iwpAggregate = null;
				Long iwpFtoCount = null;

				String pdc = entry.getKey();
				FundTransferInstructionDTO inwardFundTransferInstructionDTO = entry.getValue();

				if (null == stopWatch || stopWatch.before(inwardFundTransferInstructionDTO.getUpdatedOn())) {
					stopWatch = inwardFundTransferInstructionDTO.getUpdatedOn();
				}

				FundTransferBatchProposalDetails fundTransferBatchDetails = new FundTransferBatchProposalDetails();

				if (inwardPdcFundTransferInstructionDTOMap.containsKey(pdc)) {
					FundTransferInstructionDTO inwardfundTransferInstructionDTO = inwardPdcFundTransferInstructionDTOMap
							.get(pdc);
					iwpAggregate = inwardfundTransferInstructionDTO.getAggregateAmount();
					iwpFtoCount = inwardfundTransferInstructionDTO.getRecordCount();
					iwpFtoIdList = inwardfundTransferInstructionDTO.getFtoIdList();
					if (null == stopWatch || stopWatch.before(inwardfundTransferInstructionDTO.getUpdatedOn())) {
						stopWatch = inwardfundTransferInstructionDTO.getUpdatedOn();
					}
				} else {
					iwpFtoIdList = null;
					iwpAggregate = BigDecimal.ZERO;
					iwpFtoCount = 0L;
				}

				fundTransferBatchDetails.setOwpAggregate(inwardFundTransferInstructionDTO.getAggregateAmount());
				fundTransferBatchDetails.setIwpAggregate(iwpAggregate);

				fundTransferBatchDetails
						.setOwpFtoCount(Long.valueOf(inwardFundTransferInstructionDTO.getRecordCount()));
				fundTransferBatchDetails.setIwpFtoCount(Long.valueOf(iwpFtoCount));

				fundTransferBatchDetails.setOwpFtoList(inwardFundTransferInstructionDTO.getFtoIdList());
				fundTransferBatchDetails.setIwpFtoList(iwpFtoIdList);

				fundTransferBatchDetails.setPdc(pdc);

				pdcMap.put(pdc, fundTransferBatchDetails);
			}

		}

		if (!inwardPdcFundTransferInstructionDTOMap.isEmpty()) {

			for (Map.Entry<String, FundTransferInstructionDTO> entry : inwardPdcFundTransferInstructionDTOMap
					.entrySet()) {
				String pdc = entry.getKey();
				FundTransferInstructionDTO fundTransferInstructionDTOIwp = entry.getValue();

				FundTransferBatchProposalDetails fundTransferBatchDetails = new FundTransferBatchProposalDetails();

				if (!outwardPdcFundTransferInstructionDTOMap.containsKey(pdc)) {

					if (null == stopWatch || stopWatch.before(fundTransferInstructionDTOIwp.getUpdatedOn())) {
						stopWatch = fundTransferInstructionDTOIwp.getUpdatedOn();
					}

					fundTransferBatchDetails.setIwpAggregate(fundTransferInstructionDTOIwp.getAggregateAmount());

					fundTransferBatchDetails
							.setIwpFtoCount(Long.valueOf(fundTransferInstructionDTOIwp.getRecordCount()));

					fundTransferBatchDetails.setIwpFtoList(fundTransferInstructionDTOIwp.getFtoIdList());

					fundTransferBatchDetails.setPdc(pdc);

					pdcMap.put(pdc, fundTransferBatchDetails);
				}
			}

		}

		FundTransferBatchProposal fundTransferBatchProposal = new FundTransferBatchProposal();
		fundTransferBatchProposal.setBatchId(memberBankBic + UUID.randomUUID().toString());
		fundTransferBatchProposal.setCurrency(currency);
		fundTransferBatchProposal.setMemberBankBic(memberBankBic);
		fundTransferBatchProposal.setPdcMap(pdcMap);

		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
		String stopWatchStr = dateFormatter.format(new Date());
		fundTransferBatchProposal.setStopWatch(stopWatchStr);

		fundTransferBatchProposal.setTreasury("ARABTR");
		return fundTransferBatchProposal;
	}

	private Map<String, FundTransferInstructionDTO> getPdcFundTransferInstructionDTOMap(
			List<FundTransferInstructionDetails> fundTransferInstructionDetailsList, boolean outward) {
		Map<String, FundTransferInstructionDTO> pdcFundTransferInstructionDTOListMap = new HashMap<String, FundTransferInstructionDTO>();
		String pdc = null;

		if (fundTransferInstructionDetailsList.isEmpty()) {
			return pdcFundTransferInstructionDTOListMap;
		}

		BigDecimal aggregateAmount = BigDecimal.ZERO;
		List<String> ftoIdList = null;
		Date stopWatch = null;
		for (FundTransferInstructionDetails fundTransferInstructionDetails : fundTransferInstructionDetailsList) {
			if (null == pdc) {
				pdc = fundTransferInstructionDetails.getPdc();
				ftoIdList = new ArrayList<String>();
				stopWatch = (null == fundTransferInstructionDetails.getUpdatedOn())
						? fundTransferInstructionDetails.getCreatedOn()
						: fundTransferInstructionDetails.getUpdatedOn();
			} else if (!pdc.equals(fundTransferInstructionDetails.getPdc())) {
				pdcFundTransferInstructionDTOListMap.put(pdc, new FundTransferInstructionDTO(aggregateAmount,
						Long.valueOf(ftoIdList.size()), pdc, ftoIdList, stopWatch));
				pdc = fundTransferInstructionDetails.getPdc();
				ftoIdList = new ArrayList<String>();
				stopWatch = (null == fundTransferInstructionDetails.getUpdatedOn())
						? fundTransferInstructionDetails.getCreatedOn()
						: fundTransferInstructionDetails.getUpdatedOn();
			}

			// if (outward) {
			// aggregateAmount.add(fundTransferInstructionDetails.getAmount());
			// } else {
			aggregateAmount = aggregateAmount.add(fundTransferInstructionDetails.getAmount());
			// }
			ftoIdList.add(fundTransferInstructionDetails.getFtoId());
		}

		pdcFundTransferInstructionDTOListMap.put(pdc, new FundTransferInstructionDTO(aggregateAmount,
				Long.valueOf(ftoIdList.size()), pdc, ftoIdList, stopWatch));

		return pdcFundTransferInstructionDTOListMap;
	}

	private class FundTransferInstructionDTO {
		private BigDecimal aggregateAmount;
		private Long recordCount;
		private String pdc;
		private List<String> ftoIdList;
		private Date updatedOn;

		public FundTransferInstructionDTO(BigDecimal aggregateAmount, Long recordCount, String pdc,
				List<String> ftoIdList, Date updatedOn) {
			super();
			this.aggregateAmount = aggregateAmount;
			this.recordCount = recordCount;
			this.pdc = pdc;
			this.ftoIdList = ftoIdList;
			this.updatedOn = updatedOn;
		}

		public BigDecimal getAggregateAmount() {
			return aggregateAmount;
		}

		public void setAggregateAmount(BigDecimal aggregateAmount) {
			this.aggregateAmount = aggregateAmount;
		}

		public Long getRecordCount() {
			return recordCount;
		}

		public void setRecordCount(Long recordCount) {
			this.recordCount = recordCount;
		}

		public String getPdc() {
			return pdc;
		}

		public void setPdc(String pdc) {
			this.pdc = pdc;
		}

		public List<String> getFtoIdList() {
			return ftoIdList;
		}

		public void setFtoIdList(List<String> ftoIdList) {
			this.ftoIdList = ftoIdList;
		}

		public Date getUpdatedOn() {
			return updatedOn;
		}

		public void setUpdatedOn(Date updatedOn) {
			this.updatedOn = updatedOn;
		}

	}

}