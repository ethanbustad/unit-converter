public class Value<T> {

	public static void main(String[] args) {
		Value<Double> answer = new Value<Double>(12.3, "meters");

		System.out.println("answer: " + answer);
	}

	public Value(T quantity, String unit) {
		setQuantity(quantity);
		setUnits(unit);
	}

	public String getUnits() {
		return _unit;
	}

	public T getQuantity() {
		return _quantity;
	}

	public String toString() {
		return String.valueOf(_quantity) + _SEPARATOR + _unit;
	}

	protected void setQuantity(T quantity) {
		_quantity = quantity;
	}

	protected void setUnits(String unit) {
		_unit = unit;
	}

	private T _quantity;
	private String _unit;

	private static final String _SEPARATOR = " ";

}