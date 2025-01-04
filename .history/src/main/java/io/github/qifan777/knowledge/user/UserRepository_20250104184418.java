package io.github.qifan777.knowledge.user;

import org.babyfish.jimmer.spring.repository.JRepository;
import org.babyfish.jimmer.spring.repository.Fetcher;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JRepository<User, String> {
    static final Fetcher FETCHER = Fetcher.of(User.class)
            .allScalarFields()
            .password()
            .avatar()
            .nickname()
            .phone()
            .gender();

    UserTable t = UserTable.$;

    default Optional<User> findByPhone(String phone) {
        return sql().createQuery(t)
                .where(t.phone().eq(phone))
                .select(t)
                .fetchOptional();
    }
}
