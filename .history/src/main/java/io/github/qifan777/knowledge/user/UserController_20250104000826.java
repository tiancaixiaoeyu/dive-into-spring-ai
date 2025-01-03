package io.github.qifan777.knowledge.user;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import io.github.qifan777.knowledge.user.dto.UserLoginInput;
import io.github.qifan777.knowledge.user.dto.UserRegisterInput;
import io.github.qifan777.knowledge.user.dto.PasswordUpdateDTO;
import io.github.qifan777.knowledge.user.dto.UserUpdateInput;
import io.qifan.infrastructure.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.babyfish.jimmer.client.FetchBy;
import org.babyfish.jimmer.sql.EnableDtoGeneration;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RequestMapping("user")
@RestController
@AllArgsConstructor
public class UserController {
    private final UserRepository userRepository;

    @GetMapping
    public @FetchBy(value = "FETCHER", ownerType = UserRepository.class) User userInfo() {
        return userRepository.findById(StpUtil.getLoginIdAsString(), UserRepository.FETCHER)
                .orElseThrow(() -> new BusinessException("用户信息不存在"));
    }

    @PostMapping("login")
    public SaTokenInfo login(@RequestBody UserLoginInput input) {
        User databaseUser = userRepository.findByPhone(input.getPhone())
                .orElseThrow(() -> new BusinessException("用户名/密码错误"));
        if (!BCrypt.checkpw(input.getPassword(), databaseUser.password())) {
            throw new BusinessException("用户名/密码错误");
        }
        StpUtil.login(databaseUser.id());
        return StpUtil.getTokenInfo();
    }

    @PostMapping("register")
    public SaTokenInfo register(@RequestBody UserRegisterInput input) {
        Optional<User> byPhone = userRepository.findByPhone(input.getPhone());
        if (byPhone.isPresent()) {
            throw new BusinessException("手机号已存在, 请登录");
        }
        User save = userRepository.save(UserDraft.$.produce(draft -> {
            draft.setPhone(input.getPhone())
                    .setPassword(BCrypt.hashpw(input.getPassword()));
        }));
        StpUtil.login(save.id());
        return StpUtil.getTokenInfo();
    }

    @PutMapping("password")
    public void updatePassword(@RequestBody PasswordUpdateDTO input) {
        User user = userRepository.findById(StpUtil.getLoginIdAsString())
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (!BCrypt.checkpw(input.getOldPassword(), user.password())) {
            throw new BusinessException("原密码错误");
        }
        
        userRepository.save(UserDraft.$.produce(user, draft -> {
            draft.setPassword(BCrypt.hashpw(input.getNewPassword()));
        }));
    }

    @DeleteMapping
    public void deleteAccount() {
        String userId = StpUtil.getLoginIdAsString();
        userRepository.deleteById(userId);
        StpUtil.logout();
    }

    @PutMapping
    public void updateUser(@RequestBody UserUpdateInput input) {
        String userId = StpUtil.getLoginIdAsString();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
                
        userRepository.save(UserDraft.$.produce(user, draft -> {
            draft.setNickname(input.getNickname());
            draft.setAvatar(input.getAvatar());
        }));
    }
}
