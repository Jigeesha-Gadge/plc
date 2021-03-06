package com.plc.user.controller;

import com.plc.exception.RoleNotFound;
import com.plc.jwt.JwtUtils;
import com.plc.payload.Request.LoginRequest;
import com.plc.payload.Request.SignupRequest;
import com.plc.payload.Response.JwtResponse;
import com.plc.payload.Response.MessageResponse;
import com.plc.secutiry.UserDetailsImpl;
import com.plc.user.entity.Role;
import com.plc.user.entity.Roles;
import com.plc.user.entity.User;
import com.plc.user.repository.RoleRepository;
import com.plc.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/deltaplc/user")
public class AuthController {

    private final static Logger log= LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtils jwtUtils;
    @PostMapping("signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        log.info("Login Number {} ",loginRequest.getMobilenumber());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getMobilenumber(), loginRequest.getPassword()));
        log.info("Login Number {} ",loginRequest.getMobilenumber());

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        // System.out.println(jwt);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
                .collect(Collectors.toList());
        //System.out.println(userDetails.getId());
        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(),
                userDetails.getEmail(), userDetails.getMobileNumber(),
                userDetails.getDatetime(), roles));
    }
    @PostMapping("signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) throws Exception {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByMobileNumber(signUpRequest.getMobileNumber())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account encoder.encode(signUpRequest.getPassword())
        User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(),
                passwordEncoder.encode(signUpRequest.getPassword())
                , signUpRequest.getMobileNumber(), signUpRequest.getCity(), signUpRequest.getAddress());

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();
        System.out.println(signUpRequest.getRoles());
        if (strRoles == null) {
            Role moderatorRole = roleRepository.findByName(Roles.ROLE_MODERATOR)
                    .orElseThrow(() -> new RoleNotFound("Error: Role is not found."));
            System.out.println(moderatorRole.getName());
            roles.add(moderatorRole);
            System.out.println("moderator values");
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "ADMIN":
                        Role adminRole = roleRepository.findByName(Roles.ROLE_ADMIN)
                                .orElseThrow(() -> new RoleNotFound("Error: Role is not found."));
                        roles.add(adminRole);
                        System.out.println("admin values");

                        break;
                    case "USER":
                        Role userRole = roleRepository.findByName(Roles.ROLE_USER)
                                .orElseThrow(() -> new RoleNotFound("Error: Role is not found."));
                        roles.add(userRole);
                        System.out.println("userRole values");

                        break;
                    case "SUPERADMIN":
                        Role superRole = roleRepository.findByName(Roles.ROLE_SUPERADMIN)
                                .orElseThrow(() -> new RoleNotFound("Error: Role is not found."));
                        roles.add(superRole);
                        System.out.println("superRole values");

                        break;

                    default:
                        Role empRole = roleRepository.findByName(Roles.ROLE_EMPLOYEE)
                                .orElseThrow(() -> new RoleNotFound("Error: Role is not found."));
                        roles.add(empRole);
                        System.out.println("empRole values");
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }



}
