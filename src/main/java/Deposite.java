import java.math.BigDecimal;

import org.json.JSONException;
import org.json.JSONObject;


public class Deposite {
	private String customer;
	private BigDecimal id;
	private BigDecimal balance;
	private BigDecimal upperBound;
	public Deposite(String customer, BigDecimal id,
			BigDecimal initBalance, BigDecimal upperBound) {
		this.customer = customer;
		this.id =id;
		this.balance = initBalance;
		this.upperBound = upperBound;
	}
	public BigDecimal getId(){
		return id;
	}
	public BigDecimal getBalance(){
		return balance;
	}
	public void applyAction(String type, BigDecimal amount)
			throws UpperBoundExceededException, ValueNotValidException, LowBalanceException, UnknownAction{
		if(type.equals("deposit")){
//			synchronized (this) {
				depositeAction(amount);
//			}
		}
		else if (type.equals("withdraw"))
//			synchronized (this) {
				withdrawAction(amount);
//			}
		else
			throw new UnknownAction();
	}
	private void depositeAction(BigDecimal amount)
			throws UpperBoundExceededException, ValueNotValidException{
		if(amount == null)
			throw new ValueNotValidException();
		if(amount.compareTo(BigDecimal.ZERO) == -1)
			throw new ValueNotValidException();
		if(upperBound.compareTo(balance.add(amount)) == -1)
			throw new UpperBoundExceededException();

		balance = balance.add(amount);
	}
	private void withdrawAction(BigDecimal amount)
			throws ValueNotValidException, LowBalanceException{
		if(amount == null)
			throw new ValueNotValidException();
		if(amount.compareTo(BigDecimal.ZERO) == -1)
			throw new ValueNotValidException();
		if(balance.compareTo(amount) == -1)
			throw new LowBalanceException();

		balance = balance.subtract(amount);
	}
	public String toJSONString() throws JSONException{
		JSONObject json = new JSONObject();
		json.put("customer", customer);
		json.put("id", id);
		json.put("initialBalance", balance);
		json.put("upperBound", upperBound);
		return json.toString();
	}
}
