package com.ethanbustad.converter;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.URL;
import java.net.URLConnection;
import java.net.SocketTimeoutException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CurrencyConverter {

	public static void main(String[] args) throws Exception {
		CurrencyConverter cc = new CurrencyConverter();

		if (args.length < 2) {
			throw new Exception("Please include a 'from' currency code and " +
				"at least one 'to' currency code.");
		}

		String fromCode = args[0];

		String[] toCodes = new String[args.length - 1];

		for (int i = 1; i < args.length; i++) {
			toCodes[i - 1] = args[i];
		}

		double[] currencyRates = cc.getCurrencyRates(fromCode, toCodes);

		System.out.println("currencyRates: " + Arrays.toString(currencyRates));
	}

	public double getCurrencyRate(
			String fromCurrencyCode, String toCurrencyCode)
		throws Exception {

		double[] currencyRates = getCurrencyRates(
			fromCurrencyCode, toCurrencyCode);

		return currencyRates[0];
	}

	public double[] getCurrencyRates(
			String fromCurrencyCode, String... toCurrencyCodes)
		throws Exception {

		StringBuilder sb = new StringBuilder();

		sb.append(YAHOO_API_PREFIX);

		for (int i = 0; i < toCurrencyCodes.length; i++) {
			String toCurrencyCode = toCurrencyCodes[i];

			sb.append(YAHOO_API_CONVERSION_PREFIX);
			sb.append(fromCurrencyCode);
			sb.append(toCurrencyCode);
			sb.append(YAHOO_API_CONVERSION_SUFFIX);

			if (i < (toCurrencyCodes.length - 1)) {
				sb.append(YAHOO_API_CONVERSION_SEPARATOR);
			}
		}

		sb.append(YAHOO_API_SUFFIX);

		List<String> currencyRateStrings = _getResponseLines(sb.toString());

		double[] currencyRates = new double[currencyRateStrings.size()];

		for (int i = 0; i < currencyRateStrings.size(); i++) {
			String currencyRateString = currencyRateStrings.get(i);

			currencyRates[i] = Double.valueOf(currencyRateString);
		}

		return currencyRates;
	}

	private List<String> _getResponseLines(String urlString) throws Exception {
		URL url = new URL(urlString);

		URLConnection urlConnection = url.openConnection();

		urlConnection.setConnectTimeout(10000);

		BufferedReader bufferedReader = null;

		try {
			bufferedReader = new BufferedReader(new InputStreamReader(
				urlConnection.getInputStream()));
		}
		catch (SocketTimeoutException ste) {
			throw new Exception("Connection timed out.");
		}

		List<String> responseLines = new ArrayList<String>();

		String line = null;

		while ((line = bufferedReader.readLine()) != null) {
			responseLines.add(line);
		}

		bufferedReader.close();

		return responseLines;
	}

	private static final String YAHOO_API_CONVERSION_PREFIX = "s=";
	private static final String YAHOO_API_CONVERSION_SEPARATOR = "&...&";
	private static final String YAHOO_API_CONVERSION_SUFFIX = "=X";
	private static final String YAHOO_API_PREFIX =
		"http://download.finance.yahoo.com/d/quotes.csv?";
	private static final String YAHOO_API_SUFFIX = "&f=l1&e=.cs";


}