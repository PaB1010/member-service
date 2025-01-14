package onedu.blue.member.jwt.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import onedu.blue.global.exceptions.UnAuthorizedException;
import onedu.blue.member.jwt.TokenService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginFilter extends GenericFilterBean {

    private final TokenService tokenService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse reponse, FilterChain filterChain) throws IOException, ServletException {

        try {
            // Token 이 유입되면 로그인 처리
            tokenService.authenticate((HttpServletRequest) request);

        } catch (UnAuthorizedException e) {

            HttpServletResponse res = (HttpServletResponse) reponse;

            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());

            e.printStackTrace();
        }



    }
}
