package io.github.qifan777.knowledge.user;

import org.babyfish.jimmer.spring.repository.JRepository;
import org.babyfish.jimmer.sql.fetcher.RecursiveListFieldConfig;
import org.babyfish.jimmer.sql.fetcher.RecursiveFieldConfig;

import org.springframework.stereotype.Repository;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import java.util.Optional;

@Repository
public interface UserRepository extends JRepository<User, String> {
    Fetcher<User> FETCHER = Fetcher.ofType(User.class)
            .allScalarFields();

    UserTable t = UserTable.$;

    default Optional<User> findByPhone(String phone) {
        return sql().createQuery(t)
                .where(t.phone().eq(phone))
                .select(t)
                .fetchOptional();
    }
}
