package io.github.qifan777.knowledge.user;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import io.github.qifan777.knowledge.user.dto.UserLoginInput;
import io.github.qifan777.knowledge.user.dto.UserRegisterInput;
import io.qifan.infrastructure.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.babyfish.jimmer.client.FetchBy;
import org.babyfish.jimmer.sql.EnableDtoGeneration;
import org.babyfish.jimmer.client.meta.Api;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@Api
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

    @PutMapping
    public void updateUser(@RequestBody UserDTO userDTO) {
        userRepository.update(userDTO);
    }

    @GetMapping("/info")
    public UserDTO userInfo() {
        // 实现获取用户信息的逻辑
    }

    @PutMapping("/password")
    public void updatePassword(@RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        // 实现更新密码的逻辑
    }

    @DeleteMapping
    public void deleteAccount() {
        // 实现删除账号的逻辑
    }
}
