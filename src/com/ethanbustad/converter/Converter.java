package com.ethanbustad.converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Converter {

	public static void main(String[] args) throws Exception {
		StringBuilder sb = new StringBuilder();

		for (String arg : args) {
			sb.append(arg);
			sb.append(_SPACE);
		}

		String input = sb.toString();

		String output = parseInput(input.substring(0, input.length() - 1));

		System.out.println(output);
	}

	public static String parseInput(String input) throws Exception {
		Pattern inputPattern = Pattern.compile(_INPUT_REGEX);

		Matcher inputMatcher = inputPattern.matcher(input);

		String fromQuantity;
		String fromUnits;
		String toUnits;

		if (inputMatcher.matches()) {
			fromQuantity = inputMatcher.group(1);
			fromUnits = inputMatcher.group(2);
			toUnits = inputMatcher.group(3);
		}
		else {
			inputPattern = Pattern.compile(_BACKUP_INPUT_REGEX);

			inputMatcher = inputPattern.matcher(input);

			if (!inputMatcher.matches()) {
				throw new Exception("Invalid input.");
			}

			toUnits = inputMatcher.group(1);
			fromQuantity = inputMatcher.group(2);
			fromUnits = inputMatcher.group(3);
		}

		if (fromQuantity.equalsIgnoreCase(_A) ||
			fromQuantity.equalsIgnoreCase(_AN)) {

			fromQuantity = _1;
		}

		if (_ct == null) {
			_ct = new ConversionTable();
		}

		if (Pattern.matches(_CURRENCY_REGEX, fromUnits) &&
			Pattern.matches(_CURRENCY_REGEX, toUnits) &&
			!_ct.hasRate(fromUnits, toUnits)) {

			if (_cc == null) {
				_cc = new CurrencyConverter();
			}

			double conversionRate = _cc.getCurrencyRate(fromUnits, toUnits);

			_ct.addRate(fromUnits, toUnits, conversionRate);
		}

		Value<Double> from = new Value<Double>(
			Double.valueOf(fromQuantity), fromUnits);

		Value<Double> result = _ct.convert(from, toUnits);

		return from.toString() + _SEPARATOR + result.toString();
	}

	private static CurrencyConverter _cc;
	private static ConversionTable _ct;

	private static final String _1 = "1";
	private static final String _A = "a";
	private static final String _AN = "an";
	private static final String _BACKUP_INPUT_REGEX =
		".*?([a-zA-Z]+) \\w+ ([\\.0-9]+|a) ([a-zA-Z]+)\\W?";
	private static final String _CURRENCY_REGEX = "[a-z]{3}|[A-Z]{3}";
	private static final String _INPUT_REGEX =
		".*?([\\.0-9]+|a )\\s?([a-zA-Z]+) .+ ([a-zA-Z]+)\\W?";
	private static final String _SEPARATOR = " = ";
	private static final String _SPACE = " ";

}