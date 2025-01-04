//package io.github.qifan777.knowledge.user;
//
//import org.babyfish.jimmer.spring.repository.JRepository;
//import org.babyfish.jimmer.sql.fetcher.Fetcher;
//import org.babyfish.jimmer.sql.fetcher.FetcherFactory;
//import org.springframework.stereotype.Repository;
//import java.util.Optional;
//
//@Repository
//public interface UserRepository extends JRepository<User, String> {
//    Fetcher<User> FETCHER = FetcherFactory.newFetcher(User.class)
//            .allScalarFields();
//
//    UserTable t = UserTable.$;
//
//    default Optional<User> findByPhone(String phone) {
//        return sql().createQuery(t)
//                .where(t.phone().eq(phone))
//                .select(t)
//                .fetchOptional();
//    }
//}
package io.github.qifan777.knowledge.user;

import org.babyfish.jimmer.spring.repository.JRepository;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import io.github.qifan777.knowledge.user.UserFetcher; // 引入生成的 Fetcher 工具类
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JRepository<User, String> {

    // 使用 UserFetcher 定义 Fetcher，抓取所有标量字段
    Fetcher<User> FETCHER = UserFetcher.$.allScalarFields();

    // UserTable 别名
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
                .select(t.fetch(FETCHER)) // 使用 FETCHER 抓取需要的字段
                .fetchOptional();
    }
}
