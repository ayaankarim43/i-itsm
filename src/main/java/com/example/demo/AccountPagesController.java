package com.example.demo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
class AccountPagesController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    AccountPagesController(UserRepository users, PasswordEncoder encoder) { this.users = users; this.encoder = encoder; }

    @GetMapping("/login") String login() { return "login"; }
    @GetMapping("/register") String registerForm() { return "register"; }

    @PostMapping("/register")
    String register(@RequestParam String name, @RequestParam String email,
                    @RequestParam String password, @RequestParam String department,
                    RedirectAttributes ra) {
        if (name.isBlank() || department.isBlank() || !email.contains("@") || password.length() < 8) {
            ra.addFlashAttribute("error", "All fields are required, the email must be valid and the password needs 8+ characters");
            return "redirect:/register";
        }
        if (users.findByEmail(email).isPresent()) {
            ra.addFlashAttribute("error", "Email already registered");
            return "redirect:/register";
        }
        Employee e = new Employee(name.trim(), email.trim(), department.trim());
        e.changePassword(encoder.encode(password));
        users.save(e);
        ra.addFlashAttribute("ok", "Account created. Please log in.");
        return "redirect:/login";
    }
}
