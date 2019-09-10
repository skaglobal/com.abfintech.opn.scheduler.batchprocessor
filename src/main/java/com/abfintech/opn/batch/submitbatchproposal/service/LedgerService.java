package com.abfintech.opn.batch.submitbatchproposal.service;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.abfintech.abw.dal.tdb.entity.Ledger;
import com.abfintech.abw.dal.tdb.repository.LedgerRepository;
import com.abfintech.abw.model.batch.FundTransferBatchProposal;
import com.abfintech.abw.model.batch.FundTransferBatchResponse;
import com.abfintech.abw.model.batch.FundTransferBatchStatus;
import com.abfintech.abw.model.dlt.Notifications;
import com.google.gson.Gson;

@Component
public class LedgerService {

	@Autowired
	private RestTemplate restTemplate;

	@Value("${ledger.url}")
	private String ledgerUrl;

	@Autowired
	private LedgerRepository ledgerRepository;

	// Submit
	public void sendCreateBatchProposal(FundTransferBatchProposal fundTransferBatchProposal) {
		System.out.println("Sending create batch request to Blockchain");
		System.err.println("Sending create batch request to Blockchain");
		try {
			fundTransferBatchProposal.setStatus(FundTransferBatchStatus.DLT_PROPOSED.toString());

			System.out.println("FundTransferBatchProposal to Ledger==" + new Gson().toJson(fundTransferBatchProposal));
			System.err.println("FundTransferBatchProposal to Ledger==" + new Gson().toJson(fundTransferBatchProposal));

			HttpEntity<FundTransferBatchProposal> fundTransferBatchProposalEntity = new HttpEntity<>(
					fundTransferBatchProposal, prepareHeaders(fundTransferBatchProposal));
			System.out.println("LedgerService.sendCreateBatchProposal(),ledgerUrl : " + ledgerUrl.toLowerCase());
			System.err.println("LedgerService.sendCreateBatchProposal(),ledgerUrl : " + ledgerUrl.toLowerCase());
			ResponseEntity<FundTransferBatchResponse> createFundTransferBatchResponseEntity = restTemplate
					.postForEntity(ledgerUrl.toLowerCase() + "/limitbatch", fundTransferBatchProposalEntity,
							FundTransferBatchResponse.class);

			System.out.println("Blockchain response==" + new Gson().toJson(createFundTransferBatchResponseEntity));
			System.err.println("Blockchain response==" + new Gson().toJson(createFundTransferBatchResponseEntity));
			FundTransferBatchResponse createFundTransferBatchResponse = createFundTransferBatchResponseEntity.getBody();
			if (null != createFundTransferBatchResponse && createFundTransferBatchResponse.getCode() != "P2001") {
				System.out.println("Create Fund Transfer batch Failed");
				System.err.println("Create Fund Transfer batch Failed");
				// throw some exception }
			}

			System.out.println("Posted create batch request to Ledger");
			System.err.println("Posted create batch request to Ledger");
		} catch (Exception e) {
			System.out.println("LedgerService.sendConfirmBatch(), sendCreateBatchProposal : " + e);
			System.err.println("LedgerService.sendConfirmBatch(), sendCreateBatchProposal : " + e);
			e.printStackTrace();
		}
	}

	private HttpHeaders prepareHeaders(FundTransferBatchProposal fundTransferBatchProposal) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("correlation-key", fundTransferBatchProposal.getBatchId());
		return headers;
	}

	// Validate
	public FundTransferBatchResponse retrieveBatchDetails(FundTransferBatchProposal fundTransferBatchProposal) {
		try {
			RestTemplate restTemplate = new RestTemplate();
			System.out.println("LedgerService.retrieveBatchDetails(),ledgerUrl : " + ledgerUrl.toLowerCase());
			System.err.println("LedgerService.retrieveBatchDetails(),ledgerUrl : " + ledgerUrl.toLowerCase());
			HttpEntity<FundTransferBatchProposal> fundTransferBatchProposalEntity = new HttpEntity<>(
					prepareValidateBatchProposalRequest(fundTransferBatchProposal.getBatchId()));
			return restTemplate.postForObject(ledgerUrl.toLowerCase() + "/limitbatch/id",
					fundTransferBatchProposalEntity, FundTransferBatchResponse.class);
		} catch (Exception e) {
			System.out.println("LedgerService.retrieveBatchDetails(), Exception : " + e);
			System.err.println("LedgerService.retrieveBatchDetails(), Exception : " + e);
			e.printStackTrace();
			return null;
		}
	}

	private FundTransferBatchProposal prepareValidateBatchProposalRequest(String batchId) {
		return new FundTransferBatchProposal(batchId, FundTransferBatchStatus.DLT_VALIDATED.toString());
	}

	// Confirm batch
	public void sendConfirmBatch(Notifications notifications) {
		System.out.println("Sending confirm batch request to Sending confirm batch request to BlockchainBlockchain");
		System.err.println("Sending confirm batch request to Sending confirm batch request to BlockchainBlockchain");
		try {
			FundTransferBatchProposal fundTransferBatchProposal = new FundTransferBatchProposal();
			fundTransferBatchProposal.setBatchId(notifications.getEventObject().getBatchID());
//			fundTransferBatchProposal.setStatus(FundTransferBatchStatus.DLT_CONFIRMED.toString());

			HttpEntity<FundTransferBatchProposal> fundTransferBatchProposalEntity = new HttpEntity<>(
					fundTransferBatchProposal, prepareHeaders(fundTransferBatchProposal));
			System.out.println("LedgerService.sendConfirmBatch(),ledgerUrl : " + ledgerUrl.toLowerCase()
					+ " / fundTransferBatchProposal: " + fundTransferBatchProposal);
			System.err.println("LedgerService.sendConfirmBatch(),ledgerUrl : " + ledgerUrl.toLowerCase()
					+ " / fundTransferBatchProposal: " + fundTransferBatchProposal);
			ResponseEntity<FundTransferBatchResponse> confirmBatchResponse = restTemplate.postForEntity(
					ledgerUrl.toLowerCase() + "/limitbatch/confirmbatch", fundTransferBatchProposalEntity,
					FundTransferBatchResponse.class);

			System.out.println("Blockchain response==" + new Gson().toJson(confirmBatchResponse));
			System.err.println("Blockchain response==" + new Gson().toJson(confirmBatchResponse));
			if (confirmBatchResponse.getStatusCode() == HttpStatus.OK) {
				List<Ledger> batchList = ledgerRepository.findByBatchIdnetifer(notifications.getEventObject().getBatchID());
				System.out.println("LedgerService.sendConfirmBatch(),batchList : " + batchList);
				System.err.println("LedgerService.sendConfirmBatch(),batchList : " + batchList);
				for (Ledger ledger : batchList) {
					System.out.println("LedgerService.sendConfirmBatch(),current status : " + ledger.getStatus()+ " / Update status to "+FundTransferBatchStatus.DLT_BATCHED.toString());
					System.err.println("LedgerService.sendConfirmBatch(),current status : " + ledger.getStatus()+ " / Update status to "+FundTransferBatchStatus.DLT_BATCHED.toString());
					ledger.setStatus(FundTransferBatchStatus.DLT_BATCHED.toString());
					ledgerRepository.save(ledger);
				}

			} else if (confirmBatchResponse.getStatusCode() != HttpStatus.OK) {
				// throw some exception
				System.out.println("LedgerService.sendConfirmBatch(), status code received : "+confirmBatchResponse.getStatusCode() );
				System.err.println("LedgerService.sendConfirmBatch(), status code received : "+confirmBatchResponse.getStatusCode() );
			}
			System.out.println("Posted confirm batch request to Ledger");
			System.err.println("Posted confirm batch request to Ledger");
		} catch (Exception e) {
			System.out.println("LedgerService.sendConfirmBatch(), Exception : " + e);
			System.err.println("LedgerService.sendConfirmBatch(), Exception : " + e);
			e.printStackTrace();
		}

	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		// Do any additional configuration here
		return builder.setConnectTimeout(Duration.ofMinutes(5)).setReadTimeout(Duration.ofMinutes(5)).build();
	}

}
