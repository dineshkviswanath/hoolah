package com.read.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.read.csv.bean.TransactionRecord;

/**
 * @author DK 
 * 		   This class accepts the input file path of CSV file. Accepts the
 *         input parameters: - fromDate - toDate - MerchantName 
 *         
 *         Parse CSV file
 *         to fetch - the total transaction records for the Merchant & - Average
 *         Transaction Value of non-reversal transactions
 *
 */
public class ParseCSVMain {

	public static void main(String args[]) {

		System.out.println("--Parsing Transaction Records CSV--");
		Scanner scanner = new Scanner(System.in);
		try {
			System.out.println("--Please provide the path of the CSV file--");
			String csvFileName = scanner.nextLine();
			System.out.println("--CSV file path provided : " + csvFileName);

			System.out.println("--Please provide the Merchant Name--");
			String merchantNAme = scanner.nextLine();
			System.out.println("--merchantNAme provided : " + merchantNAme);

			System.out.println("--Please provide the fromDate (dd/MM/yyyy HH:mm:s)--");
			String fromDate = scanner.nextLine();
			System.out.println("--fromDate provided : " + fromDate);

			System.out.println("--Please provide the toDate (dd/MM/yyyy HH:mm:s)--");
			String toDate = scanner.nextLine();
			System.out.println("--toDate provided : " + toDate);

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
			LocalDateTime fromDateTime = LocalDateTime.parse(fromDate, formatter);

			LocalDateTime toDateTime = LocalDateTime.parse(toDate, formatter);

			List<TransactionRecord> parsedRecordsList = parseCSV(csvFileName);

			DoubleSummaryStatistics transactionAmountStatistics = fetchSummaryStatistics(parsedRecordsList,
					merchantNAme, fromDateTime, toDateTime);

			System.out.println("--Output--");
			System.out.println("--Total Transaction Count is --" + transactionAmountStatistics.getCount());
			System.out.println("--AVerage Transaction Value is --" + transactionAmountStatistics.getAverage());

		} catch (Exception e) {
			scanner.close();
			System.out.println("Exception occurred. Please check the inputs provided.");

		} finally {
			scanner.close();
		}

	}

	private static List<TransactionRecord> parseCSV(String csvFilePath) {
		List<TransactionRecord> empList = new ArrayList<TransactionRecord>();
		try {
			File inputF = new File(csvFilePath);
			InputStream inputFS = new FileInputStream(inputF);
			BufferedReader br = new BufferedReader(new InputStreamReader(inputFS));
			// Skipping the CSV file headers
			empList = br.lines().skip(1).map(csv2TransactionRecord).collect(Collectors.toList());
			br.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		return empList;
	}

	private static Function<String, TransactionRecord> csv2TransactionRecord = (line) -> {
		String[] record = line.split(",");
		TransactionRecord transactionRecord = new TransactionRecord();
		if (record[0] != null && record[0].trim().length() > 0) {
			transactionRecord.setId(record[0]);
		}
		if (record[1] != null && record[1].trim().length() > 0) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
			transactionRecord.setTransactionTime(LocalDateTime.parse(record[1], formatter));
		}
		if (record[2] != null && record[2].trim().length() > 0) {
			transactionRecord.setTransactionAmount(Double.valueOf(record[2]));
		}
		if (record[3] != null && record[3].trim().length() > 0) {
			transactionRecord.setMerchantName(record[3]);
		}
		if (record[4] != null && record[4].trim().length() > 0) {
			transactionRecord.setTransactionType(record[4]);
		}
		if (record.length > 5 && record[5] != null && record[5].trim().length() > 0) {
			transactionRecord.setRelatedTransaction(record[5]);
		}
		return transactionRecord;
	};
	
	public static DoubleSummaryStatistics fetchSummaryStatistics(List<TransactionRecord> empList, String merchantName,
			LocalDateTime fromDate, LocalDateTime toDate) {

		return empList.stream().filter(t -> t.getTransactionType().equalsIgnoreCase("PAYMENT"))
				.filter(t -> t.getMerchantName().equals(merchantName))
				.filter(t -> t.getTransactionTime().isAfter(fromDate))
				.filter(t -> t.getTransactionTime().isBefore(toDate))
				.mapToDouble(TransactionRecord::getTransactionAmount).summaryStatistics();

	}

}
