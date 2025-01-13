package onedu.blue.member.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import onedu.blue.global.exceptions.BadRequestException;
import onedu.blue.global.libs.Utils;
import onedu.blue.member.services.MemberUpdateService;
import onedu.blue.member.validators.JoinValidator;
import org.springframework.http.HttpStatus;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final Utils utils;

    private final JoinValidator joinValidator;

    private final MemberUpdateService updateService;

    @PostMapping("/join")
    @ResponseStatus(HttpStatus.CREATED) // 201
    public void join(@RequestBody @Valid RequestJoin form, Errors errors) {

        joinValidator.validate(form, errors);

        if (errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }

        updateService.process(form);
    }

    @PostMapping("/login")
    public void login(@RequestBody @Valid RequestLogin form, Errors errors) {


    }
}