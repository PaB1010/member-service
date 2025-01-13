package onedu.blue.member.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import onedu.blue.global.exceptions.UnAuthorizedException;
import onedu.blue.member.MemberInfo;
import onedu.blue.member.services.MemberInfoService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Token 생성 & 로그인 처리
 *
 */
@Lazy
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class TokenService {

    private final JwtProperties properties;

    private final MemberInfoService infoService;

    private Key key;

    public TokenService(JwtProperties properties, MemberInfoService infoService) {

        this.properties = properties;
        this.infoService = infoService;

        byte[] keyBytes = Decoders.BASE64.decode(properties.getSecret());

        // HMAC 시그니쳐 = sh512 방식 사용 예정
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * JWT Token 생성
     *
     * @param email
     * @return
     */
    public String create(String email) {

        MemberInfo memberInfo = (MemberInfo) infoService.loadUserByUsername(email);

        // Token - 이메일 & 권한 실어 보낼 것
        // HMAC 시그니쳐 = sh512 해쉬
        String authorities = memberInfo.getAuthorities().stream().map(a -> a.getAuthority()).collect(Collectors.joining("||"));

        // Token 만료 시간 가공용
        int validTime = properties.getValidTime() * 1000;

        // 만료시간 (Date Class 형태) = 현재로부터 15분 뒤
        Date date = new Date((new Date()).getTime() + validTime);

        // Token 생성
        return Jwts.builder()
                .setSubject(memberInfo.getEmail()) // 회원 정보 찾아서 로그인 처리
                .claim("authorities", authorities) // 데이터 많을때 사용, Key & Value 형태, 문자열로 가공??
                .signWith(key, SignatureAlgorithm.HS512) // HMAC 512 시그니쳐
                .setExpiration(date)
                .compact();
    }

    /**
     * Token 으로 인증 처리 (로그인 처리)
     * Base Method
     *
     * 요청 헤더 :
     *      Authorization : Bearer Token (문자열)
     *
     * @param token
     * @return
     */
    public Authentication authenticate(String token) {

        // Parser = Subject, Claim 정보 분해해 가져와 검증후 완성 용도
        Claims claims = Jwts.parser()
                .setSigningKey(key) // 위변조 여부 검증
                .build() // parse 객체 생성
                .parseClaimsJws(token)
                .getPayload(); // Payload = 실제로 사용할 데이터

        String email = claims.getSubject();

        String authorities = (String) claims.get("authorities"); // Jwt.claim() 에 있던 이름 참고

        List<SimpleGrantedAuthority> _authorities = Arrays.stream(authorities.split("||")).map(SimpleGrantedAuthority::new).toList();

        MemberInfo memberInfo = (MemberInfo) infoService.loadUserByUsername(email);

        memberInfo.setAuthorities(_authorities);

        // 강제 로그인
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(memberInfo, null, _authorities);

        // 로그인 처리
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return authentication;
    }

    // 편의상 요청객체만 가지고도 로그인 처리할 수 있도록 오버로드
    public Authentication authenticate(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (StringUtils.hasText(authHeader)) {

            throw new UnAuthorizedException();
        }

        // authHeader = authHeader.replace("Bearer ", "");

        String token = authHeader.substring(7);

        // Base Method
        return authenticate(token);
    }
}