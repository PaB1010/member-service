package onedu.blue.member.repositories;

import onedu.blue.member.entities.Member;
import onedu.blue.member.entities.QMember;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, QuerydslPredicateExecutor<Member> {

    // Query Method
    // 권한(Authority)은 @OneToMany 라서 지연 로딩 상태지만
    // finByEmail 메서드 사용시에는 즉시 로딩되도록 fetch Join
    @EntityGraph("authorities")
    Optional<Member> findByEmail(String email);

    default boolean exists(String email) {

        QMember member = QMember.member;

        return exists(member.email.eq(email));
    }
}