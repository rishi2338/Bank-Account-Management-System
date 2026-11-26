package com.rishi.bankweb;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller
public class BankController {
 private final BankService bank; public BankController(BankService bank){this.bank=bank;}
 @GetMapping("/") public String home(HttpSession s){return s.getAttribute("accountId")==null?"redirect:/login":"redirect:/dashboard";}
 @GetMapping("/register") public String registerPage(){return "register";}
 @PostMapping("/register") public String register(@RequestParam String fullName,@RequestParam String email,@RequestParam String password,HttpSession s,RedirectAttributes f){
  try{if(fullName.isBlank()||fullName.length()>100||!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")||password.length()<8||password.length()>72)throw new IllegalArgumentException("Enter a valid name, email and password (8–72 characters).");
   if(bank.findByEmail(email)!=null)throw new IllegalArgumentException("An account with this email already exists.");Map<String,Object>a=bank.register(fullName,email,password);s.setAttribute("accountId",((Number)a.get("id")).longValue());f.addFlashAttribute("success","Your account was created successfully.");return "redirect:/dashboard";
  }catch(Exception e){f.addFlashAttribute("error",e.getMessage()==null?"Unable to create account.":e.getMessage());return "redirect:/register";}
 }
 @GetMapping("/login") public String loginPage(){return "login";}
 @PostMapping("/login") public String login(@RequestParam String email,@RequestParam String password,HttpSession s,RedirectAttributes f){Map<String,Object>a=bank.authenticate(email,password);if(a==null){f.addFlashAttribute("error","Incorrect email or password.");return "redirect:/login";}s.setAttribute("accountId",((Number)a.get("id")).longValue());s.changeSessionId();return "redirect:/dashboard";}
 @GetMapping("/dashboard") public String dashboard(HttpSession s,Model m){Long id=(Long)s.getAttribute("accountId");if(id==null)return "redirect:/login";Map<String,Object>a=bank.account(id);if(a==null){s.invalidate();return "redirect:/login";}m.addAttribute("account",a);m.addAttribute("transactions",bank.history(id));return "dashboard";}
 @PostMapping("/deposit") public String deposit(@RequestParam BigDecimal amount,HttpSession s,RedirectAttributes f){return action(s,f,()->bank.deposit(id(s),amount),"Deposit completed.");}
 @PostMapping("/withdraw") public String withdraw(@RequestParam BigDecimal amount,HttpSession s,RedirectAttributes f){return action(s,f,()->bank.withdraw(id(s),amount),"Withdrawal completed.");}
 @PostMapping("/transfer") public String transfer(@RequestParam String recipientAccount,@RequestParam BigDecimal amount,HttpSession s,RedirectAttributes f){return action(s,f,()->bank.transfer(id(s),recipientAccount,amount),"Transfer completed.");}
 @PostMapping("/logout") public String logout(HttpSession s){s.invalidate();return "redirect:/login";}
 private long id(HttpSession s){Long id=(Long)s.getAttribute("accountId");if(id==null)throw new IllegalArgumentException("Please sign in again.");return id;}
 private String action(HttpSession s,RedirectAttributes f,Runnable r,String msg){try{r.run();f.addFlashAttribute("success",msg);}catch(Exception e){f.addFlashAttribute("error",e.getMessage());}return "redirect:/dashboard";}
}