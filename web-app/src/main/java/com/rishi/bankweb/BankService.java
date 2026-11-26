package com.rishi.bankweb;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class BankService {
 private final JdbcTemplate jdbc; private final PasswordEncoder encoder; private final SecureRandom random=new SecureRandom();
 public BankService(JdbcTemplate jdbc,PasswordEncoder encoder){this.jdbc=jdbc;this.encoder=encoder;}
 public Map<String,Object> register(String name,String email,String password){
  String number; do {number=String.format("%010d",random.nextLong(10_000_000_000L));} while(!jdbc.queryForList("SELECT id FROM bank_accounts WHERE account_number=?",number).isEmpty());
  jdbc.update("INSERT INTO bank_accounts(account_number,full_name,email,password_hash,balance) VALUES(?,?,?,?,?)",number,name.trim(),email.trim().toLowerCase(),encoder.encode(password),new BigDecimal("0.00"));
  return findByEmail(email);
 }
 public Map<String,Object> findByEmail(String email){List<Map<String,Object>> r=jdbc.queryForList("SELECT id,account_number,full_name,email,balance,created_at FROM bank_accounts WHERE email=?",email.trim().toLowerCase());return r.isEmpty()?null:r.get(0);}
 public Map<String,Object> authenticate(String email,String password){
  List<Map<String,Object>> r=jdbc.queryForList("SELECT * FROM bank_accounts WHERE email=?",email.trim().toLowerCase());
  if(r.isEmpty()||!encoder.matches(password,(String)r.get(0).get("password_hash")))return null;return publicAccount(r.get(0));
 }
 public Map<String,Object> account(long id){List<Map<String,Object>> r=jdbc.queryForList("SELECT id,account_number,full_name,email,balance,created_at FROM bank_accounts WHERE id=?",id);return r.isEmpty()?null:r.get(0);}
 @Transactional public void deposit(long id,BigDecimal amount){validate(amount);jdbc.update("UPDATE bank_accounts SET balance=balance+? WHERE id=?",amount,id);record(id,"DEPOSIT",amount,"Cash deposit");}
 @Transactional public void withdraw(long id,BigDecimal amount){validate(amount);int n=jdbc.update("UPDATE bank_accounts SET balance=balance-? WHERE id=? AND balance>=?",amount,id,amount);if(n==0)throw new IllegalArgumentException("Insufficient balance.");record(id,"WITHDRAWAL",amount,"Cash withdrawal");}
 @Transactional public void transfer(long from,String recipient,BigDecimal amount){
  validate(amount);List<Map<String,Object>> r=jdbc.queryForList("SELECT id FROM bank_accounts WHERE account_number=?",recipient.trim());if(r.isEmpty())throw new IllegalArgumentException("Recipient account was not found.");
  long to=((Number)r.get(0).get("id")).longValue();if(to==from)throw new IllegalArgumentException("You cannot transfer to your own account.");
  if(jdbc.update("UPDATE bank_accounts SET balance=balance-? WHERE id=? AND balance>=?",amount,from,amount)==0)throw new IllegalArgumentException("Insufficient balance.");
  jdbc.update("UPDATE bank_accounts SET balance=balance+? WHERE id=?",amount,to);record(from,"TRANSFER",amount,"Transfer to "+recipient);record(to,"RECEIVED",amount,"Transfer received");
 }
 public List<Map<String,Object>> history(long id){return jdbc.queryForList("SELECT transaction_type,amount,description,created_at FROM bank_transactions WHERE account_id=? ORDER BY created_at DESC,id DESC LIMIT 100",id);}
 private void record(long id,String type,BigDecimal amount,String desc){jdbc.update("INSERT INTO bank_transactions(account_id,transaction_type,amount,description) VALUES(?,?,?,?)",id,type,amount,desc);}
 private void validate(BigDecimal a){if(a==null||a.compareTo(BigDecimal.ZERO)<=0||a.scale()>2||a.compareTo(new BigDecimal("10000000"))>0)throw new IllegalArgumentException("Enter a valid positive amount (maximum ₹1 crore, up to 2 decimal places).");}
 private Map<String,Object> publicAccount(Map<String,Object> r){return Map.of("id",r.get("id"),"account_number",r.get("account_number"),"full_name",r.get("full_name"),"email",r.get("email"),"balance",r.get("balance"),"created_at",r.get("created_at"));}
}