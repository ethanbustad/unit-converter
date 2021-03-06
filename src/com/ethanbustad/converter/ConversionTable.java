package com.ethanbustad.converter;

import java.math.BigDecimal;
import java.math.MathContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConversionTable {

	public static void main(String[] args) throws Exception {
		ConversionTable ct = new ConversionTable();

		System.out.println("5mi to ft: " + ct.convert(5, "mile", "foot"));
		System.out.println("50min to s: " + ct.convert(50, "min", "s"));
		System.out.println("34in to cm: " + ct.convert(34, "inches", "cm"));

		System.out.println("5mi to ft: " + ct.convert(5, "miles", "feet"));
		System.out.println("50min to s: " + ct.convert(50, "mins", "secs"));
		System.out.println("34in to cm: " + ct.convert(34, "inches", "cms"));

		System.out.println("6 in to ft: " + ct.convert(6, "in", "ft"));
		System.out.println("0.5 ft to in: " + ct.convert(0.5, "ft", "in"));

		System.out.println("5m to cm: " + ct.convert(5, "m", "cm"));
		System.out.println("5km to m: " + ct.convert(5, "km", "m"));
		System.out.println("5 m to km: " + ct.convert(5, "m", "km"));
	}

	public ConversionTable() {
		_addedUnits = new ArrayList<String>();
		_rateRegexes = new HashMap<String, Number>();
		_table = new HashMap<String, Number>();
		_units = new HashMap<String, String>();
		_unitPrefixes = new HashMap<String, String>();
		_unitSuffixes = new HashMap<String, String>();

		_setDefaults();
	}

	public void addRate(String from, String to, Number rate) throws Exception {
		from = _standardizeUnits(from);
		to = _standardizeUnits(to);

		String key = _getKey(from, to);

		_table.put(key, rate);

		_addedUnits.add(from);
		_addedUnits.add(to);
	}

	public Value<Double> convert(Value<? extends Number> from, String to)
		throws Exception {

		Number quantity = convert(from.getQuantity(), from.getUnits(), to);

		return new Value<Double>(quantity.doubleValue(), to);
	}

	public Number convert(Number value, String from, String to)
		throws Exception {

		from = _standardizeUnits(from);
		to = _standardizeUnits(to);

		String key = _getKey(from, to);

		if (_table.containsKey(key)) {
			return _convert(value, _table.get(key));
		}

		key = _getKey(to, from);

		if (_table.containsKey(key)) {
			return _convert(value, _inverse(_table.get(key)));
		}

		Number rate = _deriveConversionRate(from, to);

		if (rate != null) {
			return _convert(value, rate);
		}

		throw new Exception("Rate for this conversion not available.");
	}

	public boolean hasRate(String from, String to) {
		String key = _getKey(from, to);

		return _table.containsKey(key);
	}

	private String _checkStandardFormat(String unit) {
		String prefix = null;

		for (String unitPrefix : _unitPrefixes.keySet()) {
			if (Pattern.matches(unitPrefix, unit)) {
				prefix = unitPrefix;

				break;
			}
		}

		String suffix = null;

		for (String unitSuffix : _unitSuffixes.keySet()) {
			if (Pattern.matches(unitSuffix, unit)) {
				suffix = unitSuffix;

				break;
			}
		}

		if ((prefix != null) && (suffix != null)) {
			return _unitPrefixes.get(prefix) + _unitSuffixes.get(suffix);
		}

		return null;
	}

	private BigDecimal _convert(Number value, Number rate) {
		BigDecimal bdValue = new BigDecimal(value.toString());
		BigDecimal bdRate = new BigDecimal(rate.toString());

		return bdValue.multiply(bdRate, MathContext.DECIMAL64);
	}

	private BigDecimal _deriveConversionRate(String from, String to)
		throws Exception {

		List<Number> connectionPath = _getConnectionPath(from, to, null);

		if (connectionPath == null) {
			return null;
		}

		connectionPath.remove(_MARKER);

		BigDecimal conversionRate = BigDecimal.ONE;

		for (Number connection : connectionPath) {
			BigDecimal bdConnectionRate = new BigDecimal(connection.toString());

			conversionRate = conversionRate.multiply(bdConnectionRate);
		}

		return conversionRate;
	}

	private List<Number> _getConnectionPath(
		String from, String to, String exclude) {

		List<Number> path = new ArrayList<Number>();

		if (from.equals(to)) {
			path.add(1);
			path.add(_MARKER);

			return path;
		}

		String keyGuess1 = _getKey(from, to);
		String keyGuess2 = _getKey(to, from);

		if (_table.containsKey(keyGuess1)) {
			path.add(_table.get(keyGuess1));
			path.add(_MARKER);

			return path;
		}
		else if (_table.containsKey(keyGuess2)) {
			path.add(_inverse(_table.get(keyGuess2)));
			path.add(_MARKER);

			return path;
		}

		for (String regexKey : _rateRegexes.keySet()) {
			Pattern pattern = Pattern.compile(regexKey);

			Matcher matcher = pattern.matcher(from);

			if (matcher.matches() &&
				_unitSuffixes.values().contains(matcher.group(1)) &&
				!((exclude != null) && exclude.equals(matcher.group(1)))) {

				path.add(_inverse(_rateRegexes.get(regexKey)));
				path.addAll(_getConnectionPath(matcher.group(1), to, from));

				return path;
			}
		}

		if (_unitSuffixes.values().contains(from)) {
			for (String regexKey : _rateRegexes.keySet()) {
				String steppingStone =
					regexKey.replace(_REGEX_PORTION, _BLANK) + from;

				List<Number> subpath = _getConnectionPath(
					steppingStone, to, from);

				if ((subpath == null) || subpath.contains(null)) {
					continue;
				}

				if (subpath.contains(_MARKER)) {
					path.add(_rateRegexes.get(regexKey));
					path.addAll(subpath);

					return path;
				}
			}
		}

		String keyOption1 = from.concat(_KEY_SEPARATOR);
		String keyOption2 = _KEY_SEPARATOR.concat(from);

		for (String key : _table.keySet()) {
			path = new ArrayList<Number>();
			String steppingStone = null;
			Number value = 0;

			if (key.startsWith(keyOption1)) {
				steppingStone = key.replace(keyOption1, _BLANK);

				value = _table.get(_getKey(from, steppingStone));
			}
			else if (key.endsWith(keyOption2)) {
				steppingStone = key.replace(keyOption2, _BLANK);

				value = _inverse(_table.get(_getKey(steppingStone, from)));
			}
			else {
				continue;
			}

			if ((exclude != null) && steppingStone.equals(exclude)) {
				continue;
			}

			List<Number> subpath = _getConnectionPath(steppingStone, to, from);

			if ((subpath == null) || subpath.contains(null)) {
				continue;
			}

			if (subpath.contains(_MARKER)) {
				path.add(value);
				path.addAll(subpath);

				return path;
			}
		}

		return null;
	}

	private String _getKey(String from, String to) {
		return from.concat(_KEY_SEPARATOR).concat(to);
	}

	private BigDecimal _inverse(Number num) {
		return BigDecimal.ONE.divide(
			new BigDecimal(num.toString()), 20, BigDecimal.ROUND_HALF_DOWN);
	}

	private void _setDefaults() {
		for (String[] defaultRate : _defaultRates) {
			String key = _getKey(defaultRate[0], defaultRate[1]);

			_table.put(key, Double.valueOf(defaultRate[2]));
		}

		for (String[] rateRegex : _defaultRateRegexes) {
			_rateRegexes.put(rateRegex[0], Double.valueOf(rateRegex[1]));
		}
// find a better way to set these defaults
		for (String[] unitPair : _defaultUnits) {
			_units.put(unitPair[0], unitPair[1]);
		}

		for (String[] unitPrefixes : _defaultUnitPrefixRegexes) {
			_unitPrefixes.put(unitPrefixes[0], unitPrefixes[1]);
		}

		for (String[] unitSuffixes : _defaultUnitSuffixRegexes) {
			_unitSuffixes.put(unitSuffixes[0], unitSuffixes[1]);
		}
	}

	private String _standardizeUnits(String unit) throws Exception {
		String lcUnit = unit.toLowerCase();

		if (_units.values().contains(lcUnit)) {
			return lcUnit;
		}

		if (_units.containsKey(lcUnit)) {
			return _units.get(lcUnit);
		}

		String expandedUnit = _checkStandardFormat(unit);

		if (expandedUnit != null) {
			return expandedUnit;
		}

		if (unit.endsWith(_PLURAL_SUFFIX)) {
			String singularUnit = unit.substring(
				0, unit.length() - _PLURAL_SUFFIX.length());

			return _standardizeUnits(singularUnit);
		}

		if (_addedUnits.contains(lcUnit)) {
			return lcUnit;
		}

		return unit;
	}

	private List<String> _addedUnits;
	private Map<String, Number> _rateRegexes;
	private Map<String, Number> _table;
	private Map<String, String> _units;
	private Map<String, String> _unitPrefixes;
	private Map<String, String> _unitSuffixes;

	private static final String _BLANK = "";
	private static final Number _MARKER = Double.NaN;
	private static final String _KEY_SEPARATOR = "`->`";
	private static final String _PLURAL_SUFFIX = "s";
	private static final String _REGEX_PORTION = "(.+)";

	private static final String[][] _defaultRates = {
		{"cup", "fluid ounce", "8"},
		{"day", "hour", "24"},
		{"foot", "inch", "12"},
		{"gallon", "fluid ounce", "128"},
		{"hour", "minute", "60"},
		{"inch", "meter", "0.0254"},
		{"liter", "gallon", "0.2642"},
		{"mile", "foot", "5280"},
		{"minute", "second", "60"},
		{"pint", "cup", "2"},
		{"pound", "gram", "453.59237"},
		{"pound", "ounce", "16"},
		{"quart", "cup", "4"},
		{"ton", "pound", "2000"},
		{"tonne", "gram", "1000000"},
		{"week", "day", "7"},
		{"yard", "foot", "3"},
		{"year", "day", "365.24"},
		{"year", "month", "12"}
	};

	private static final String[][] _defaultRateRegexes = {
		{"centi(.+)", "100"},
		{"deca(.+)", "0.1"},
		{"deci(.+)", "10"},
		{"femto(.+)", "1000000000000000"},
		{"giga(.+)", "0.000000001"},
		{"hecto(.+)", "0.01"},
		{"kilo(.+)", "0.001"},
		{"mega(.+)", "0.000001"},
		{"micro(.+)", "1000000"},
		{"milli(.+)", "1000"},
		{"nano(.+)", "1000000000"},
		{"peta(.+)", "0.000000000000001"},
		{"pico(.+)", "1000000000000"},
		{"tera(.+)", "0.000000000001"}
	};

	private static final String[][] _defaultUnitPrefixRegexes = {
		{"c.", "centi"},
		{"k.", "kilo"},
		{"m.", "milli"},
		{"mc.", "micro"},
		{"μ.", "micro"},
		{"n.", "nano"}
	};

	private static final String[][] _defaultUnitSuffixRegexes = {
		{".{1,2}l", "liter"},
		{".{1,2}m", "meter"},
		{".{1,2}g", "gram"},
		{".{1,2}s", "second"}
	};
// maybe put the units into the value class, and have a getStandardizedUnits()
	private static final String[][] _defaultUnits = {
		{"c", "cup"},
		{"d", "day"},
		{"feet", "foot"},
		{"fl oz", "fluid ounce"},
		{"ft", "foot"},
		{"gal", "gallon"},
		{"g", "gram"},
		{"h", "hour"},
		{"hr", "hour"},
		{"in", "inch"},
		{"inches", "inch"},
		{"l", "liter"},
		{"lb", "pound"},
		{"m", "meter"},
		{"mi", "mile"},
		{"min", "minute"},
		{"mon", "month"},
		{"oz", "ounce"},
		{"qt", "quart"},
		{"sec", "second"},
		{"s", "second"},
		{"wk", "week"},
		{"y", "year"},
		{"yr", "year"}
	};

}