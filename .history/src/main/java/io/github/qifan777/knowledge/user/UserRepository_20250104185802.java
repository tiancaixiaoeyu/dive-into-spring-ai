package io.github.qifan777.knowledge.user;

import org.babyfish.jimmer.spring.repository.JRepository;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JRepository<User, String> {
    static final Fetchers.FETCHER<User> FETCHER = Fetchers.of(User.class)
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
