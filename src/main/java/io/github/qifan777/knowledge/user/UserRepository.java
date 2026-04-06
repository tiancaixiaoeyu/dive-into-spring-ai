package io.github.qifan777.knowledge.user;

import org.babyfish.jimmer.spring.repository.JRepository;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JRepository<User, String> {

    Fetcher<User> FETCHER = UserFetcher.$.allScalarFields()
            .avatar();

    UserTable t = UserTable.$;

    /**
     * 根据电话号码查找用户
     *
     * @param phone 用户电话号码
     * @return 包含用户的 Optional 对象
     */
    default Optional<User> findByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return Optional.empty();
        }
        return sql().createQuery(t)
                .where(t.phone().eq(phone))
                .select(t.fetch(FETCHER))
                .fetchOptional();
    }
}
