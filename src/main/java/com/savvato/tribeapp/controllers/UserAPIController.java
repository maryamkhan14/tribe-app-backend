package com.savvato.tribeapp.controllers;

import com.savvato.tribeapp.constants.Constants;
import com.savvato.tribeapp.controllers.annotations.controllers.UserAPIController.*;
import com.savvato.tribeapp.controllers.dto.ChangePasswordRequest;
import com.savvato.tribeapp.controllers.dto.UserRequest;
import com.savvato.tribeapp.dto.UserDTO;
import com.savvato.tribeapp.entities.User;
import com.savvato.tribeapp.repositories.UserRepository;
import com.savvato.tribeapp.services.ProfileService;
import com.savvato.tribeapp.services.SMSChallengeCodeService;
import com.savvato.tribeapp.services.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/public/users")
@Tag(
        name = "public/users",
        description =
                "Publicly available user-information relevant paths, no login/credentials needed to make requests to them")
public class UserAPIController {

    @Autowired
    UserService userService;

    @Autowired
    ProfileService profileService;

    @Autowired
    UserRepository ur;

    @Autowired
    SMSChallengeCodeService smsccs;

    UserAPIController() {
    }

    @CreateNewUser
    @PostMapping
    public ResponseEntity createUser(@RequestBody @Valid UserRequest req) {

        try {
            Optional<User> response = userService.createNewUser(req, Constants.SMS);

            return new ResponseEntity<>(response.get(), HttpStatus.OK);
        } catch (IllegalArgumentException iae) {
            return new ResponseEntity(iae.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // TODO: figure a way to have these enddpoints not be publicly accessible, but also not require a
    // user to be logged in.
    // 			a secret, but general purpose for-the-app login.


    private boolean isUsernameAvailable(
            @Parameter(description = "The username", example = "admin") @RequestParam("q")
            String queryStr) {
        return !this.ur.findByName(queryStr).isPresent();
    }

    // api/public/user/isPhoneNumberAvailable?q=7205870001
    private boolean isPhoneNumberAvailable(
            @Parameter(description = "The phone number", example = "1234567890") @RequestParam("q")
            String queryStr) {
        Optional<List<User>> opt = this.ur.findByPhone(queryStr);

        if (opt.isPresent()) return opt.get().size() == 0;
        else return true;
    }

    // api/public/user/isEmailAddressAvailable?q=anAddress@domain.com
    private boolean isEmailAddressAvailable(
            @Parameter(description = "The email address", example = "admin@tribeapp.com")
            @RequestParam("q")
            String queryStr) {
        return !this.ur.findByEmail(queryStr).isPresent();
    }

    // api/public/user/isUserInformationUnique?name=sample&phone=7205870001&email=anAddress@domain.com
    @IsUserInformationUnique
    @GetMapping("/availability")
    public ResponseEntity<String> isUserInformationUnique(
            @Parameter(description = "The username", example = "admin") @RequestParam(name = "username", required = false)
            String username,
            @Parameter(description = "The phone number", example = "1234567890") @RequestParam(name = "phone", required = false)
            String phone,
            @Parameter(description = "The email address", example = "admin@tribeapp.com")
            @RequestParam(name = "email", required = false)
            String email) {
        if (StringUtils.isBlank(username) && StringUtils.isBlank(email) && StringUtils.isBlank(phone)) {
            return ResponseEntity.badRequest().body("Please provide at least one parameter");
        }
        if (!isUsernameAvailable(username)) return ResponseEntity.ok("{\"response\": \"username\"}");
        if (!isPhoneNumberAvailable(phone)) return ResponseEntity.ok("{\"response\": \"phone\"}");
        if (!isEmailAddressAvailable(email)) return ResponseEntity.ok("{\"response\": \"email\"}");

        return ResponseEntity.ok("{\"response\": true}");
    }

    @ChangePassword
    @PostMapping("/password-reset")
    public UserDTO changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        return userService.changePassword(request.pw, request.phoneNumber, request.smsChallengeCode);
    }
}
