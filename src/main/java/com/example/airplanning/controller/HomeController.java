package com.example.airplanning.controller;

import com.example.airplanning.domain.dto.user.UserDto;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.UserRole;
import com.example.airplanning.repository.UserRepository;
import com.example.airplanning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserRepository userRepository;

    @GetMapping(value = {"", "/"})
    public String home(Principal principal, Model model) {
        if (principal != null) {
            Optional<User> optUser = userRepository.findByUserName(principal.getName());
            if(optUser.isPresent()) {
                if(optUser.get().getRole().equals(UserRole.PLANNER)) {
                    model.addAttribute("nextUrl", "/planners/" + optUser.get().getPlanner().getId());
                } else {
                    model.addAttribute("nextUrl", "/boards/rankup/list");
                }
            } else {
                model.addAttribute("nextUrl", "/users/login");
            }
        } else {
            model.addAttribute("nextUrl", "/users/login");
        }
        return "home";
    }
}
