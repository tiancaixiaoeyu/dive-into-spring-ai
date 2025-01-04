package io.github.qifan777.knowledge.user;

import org.babyfish.jimmer.spring.repository.JRepository;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JRepository<User, String> {
    Fetcher<User> FETCHER = Fetcher.<User>of(User.class)
            .allScalarFields();
}
