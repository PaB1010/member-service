package onedu.blue.member.services;

import lombok.RequiredArgsConstructor;
import onedu.blue.member.constants.Authority;
import onedu.blue.member.controllers.RequestJoin;
import onedu.blue.member.entities.Authorities;
import onedu.blue.member.entities.Member;
import onedu.blue.member.entities.QAuthorities;
import onedu.blue.member.repositories.AuthoritiesRepository;
import onedu.blue.member.repositories.MemberRepository;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 회원 가입 & 정보 수정 기능
 *
 */
// @Lazy = 지연 로딩 - 최초로 해당 Bean 사용할 때 생성
@Lazy
@Service
@RequiredArgsConstructor
@Transactional
public class MemberUpdateService {

    private final MemberRepository memberRepository;

    private final AuthoritiesRepository authoritiesRepository;

    private final PasswordEncoder passwordEncoder;

    // ModelMapper
    // 같은 getter setter 처리시 일괄 처리해주는 Reflection API 편의 기능
    private final ModelMapper modelMapper;

    /**
     * 메서드 오버로드 - 커맨드 객체의 타입에 따라서
     *
     * RequestJoin 이면 회원 가입 처리
     * RequestProfile 이면 회원 정보 수정 처리
     *
     * @param form
     */
    public void process(RequestJoin form) {

        // 커맨드 객체 -> Entity 객체로 Data 옮기기
        /*
        Member member = new Member();
        member.setEmail(form.getEmail());
        member.setName(form.getName());
        ..
        ...

         */
        Member member = modelMapper.map(form, Member.class);

        // 선택 약관 처리
        List<String> optionalTerms = form.getOptionalTerms();

        // 선택 약관 값이 있을때에만 -> 약관 항목1||약관 항목2||... 형태로 가공 처리
        if (optionalTerms != null) {

            member.setOptionalTerms(String.join("||", optionalTerms));
        }

        // 비밀번호 해시화 - BCrypt (단방성, 유동 해시)
        String hash = passwordEncoder.encode(form.getPassword());
        member.setPassword(hash);
        // ★ 비밀번호 변경 일자 Null 이 아닌 오늘로 설정 ★
        member.setCredentialChangedAt(LocalDateTime.now());

        // 회원 권한 부여
        Authorities auth = new Authorities();
        auth.setMember(member);
        // 처음 가입시 일반 회원(USER)
        auth.setAuthority(Authority.USER);

        save(member, List.of(auth)); // 회원 저장 처리
    }


    /**
     * 회원 정보 추가 OR 수정 완료 처리
     *
     */
    public void save(Member member, List<Authorities> authorities) {

        memberRepository.saveAndFlush(member);

        /* 회원 권한 업데이트 처리 S */
        // 추후 Builder 로 변경

        if (authorities != null) {
            /*
             * 기존 권한을 삭제하고 다시 등록
             */

            QAuthorities qAuthorities = QAuthorities.authorities;

            List<Authorities> items = (List<Authorities>) authoritiesRepository.findAll(qAuthorities.member.eq(member));

            if (items != null) {

                authoritiesRepository.deleteAll(items);

                authoritiesRepository.flush();
            }

            authoritiesRepository.saveAllAndFlush(authorities);
        }
        /* 회원 권한 업데이트 처리 E */
    }
}