package onedu.blue.member.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import onedu.blue.global.exceptions.BadRequestException;
import onedu.blue.global.libs.Utils;
import onedu.blue.global.rests.JSONData;
import onedu.blue.member.jwt.TokenService;
import onedu.blue.member.services.MemberUpdateService;
import onedu.blue.member.validators.JoinValidator;
import onedu.blue.member.validators.LoginValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member", description = "회원 인증/인가 API")
@RestController
@RequiredArgsConstructor
public class MemberController {

    @Value("${front.domain}")
    private  String frontDomain;

    private final Utils utils;

    private final TokenService tokenService;

    private final JoinValidator joinValidator;

    private final LoginValidator loginValidator;

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

    /**
     * 로그인 성공시 Token 발급
     *
     * @param form
     * @param errors
     */
    @PostMapping("/login")
    public JSONData login(@RequestBody @Valid RequestLogin form, Errors errors, HttpServletResponse response) {

        loginValidator.validate(form, errors);

        if (errors.hasErrors()) throw new BadRequestException(utils.getErrorMessages(errors));

        String email = form.getEmail();
        String token = tokenService.create(email);

        if (StringUtils.hasText(frontDomain)) {

            String[] domains = frontDomain.split(",");

            for (String domain : domains) {

                Cookie cookie = new Cookie("token", token);

                // 전체 경로 가능
                cookie.setPath("/");
                cookie.setDomain(domain);
                cookie.setSecure(true);
                cookie.setHttpOnly(true);

                response.addCookie(cookie);
            }
        }

        return new JSONData(token);
    }

    // 회원 전용 접근 테스트
//    @PreAuthorize("isAuthenticated()")
//    @GetMapping("/test")
//    public void test(@AuthenticationPrincipal MemberInfo memberInfo) {
//
//        System.out.println(memberInfo);
//        System.out.println("회원 전용 URL");
//    }
}