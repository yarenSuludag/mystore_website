package com.bmt.MyStore.controllers;

import com.bmt.MyStore.models.AppUser;
import com.bmt.MyStore.models.RegisterDto;
import com.bmt.MyStore.repositories.AppUserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.Date;

@Controller
public class AccountController {

    @Autowired
    private AppUserRepository repo;

    @GetMapping("/register")
    public String register(Model model) {
        RegisterDto registerDto = new RegisterDto();
        model.addAttribute(registerDto);
        model.addAttribute("success", false);
        return "register";
    }

    @PostMapping("/register")
    public String register(
            Model model,
            @Valid @ModelAttribute RegisterDto registerDto,
            BindingResult result
    ) {

        if (!registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
            result.addError(
                    new FieldError("registerDto", "confirmPassword", "Password and Confirm Password do not match")
            );
        }

        AppUser appUser = repo.findByEmail(registerDto.getEmail());
        if (appUser != null) {
            result.addError(new FieldError("registerDto", "email", "Email address is already used"));
        }

        if (result.hasErrors()) {
            return "register";
        }

        try {
            var bCryptEncoder = new BCryptPasswordEncoder();

            AppUser newUser = new AppUser();
            newUser.setFirstName(registerDto.getFirstName());
            newUser.setLastName(registerDto.getLastName());
            newUser.setEmail(registerDto.getEmail());
            newUser.setPhone(registerDto.getPhone());
            newUser.setAddress(registerDto.getAddress());
            newUser.setRole("client");
            newUser.setCreatedAt(new Date());
            newUser.setPassword(bCryptEncoder.encode(registerDto.getPassword()));

            repo.save(newUser);

            model.addAttribute("registerDto", new RegisterDto());
            model.addAttribute("success", true);
        } catch (Exception ex) {
            result.addError(
                    new FieldError("registerDto", "firstName", ex.getMessage())
            );
        }

        return "register";
    }

    @GetMapping("/profile")
    public String showProfile(Model model) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser user = repo.findByEmail(email);
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("user") AppUser updatedUser,
                                BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "profile";
        }

        try {
            AppUser currentUser = repo.findByEmail(updatedUser.getEmail());

            // Kullanıcı bilgilerini güncelle
            currentUser.setFirstName(updatedUser.getFirstName());
            currentUser.setLastName(updatedUser.getLastName());
            currentUser.setPhone(updatedUser.getPhone());
            currentUser.setAddress(updatedUser.getAddress());

            // Şifre güncelleme işlemi (isteğe bağlı)
            if (!updatedUser.getPassword().isEmpty()) {
                BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                currentUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }

            repo.save(currentUser);
            model.addAttribute("success", true);

        } catch (Exception e) {
            result.rejectValue("firstName", "error.user", "An error occurred while updating profile");
        }

        return "profile";
    }
}
