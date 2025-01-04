package io.github.qifan777.knowledge.user;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import io.github.qifan777.knowledge.user.PasswordUpdateDTO;
import io.github.qifan777.knowledge.user.dto.UserLoginInput;
import io.github.qifan777.knowledge.user.dto.UserRegisterInput;
import io.qifan.infrastructure.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.client.FetchBy;
import org.babyfish.jimmer.sql.EnableDtoGeneration;
import org.babyfish.jimmer.client.meta.Api;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@Slf4j
@Api
@RequestMapping("user")
@RestController
@AllArgsConstructor
public class UserController {
    private final UserRepository userRepository;

    @GetMapping
   public @FetchBy(value = "FETCHER", ownerType = UserRepository.class) User getCurrentUser() {
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

    @PutMapping("/update")
    public void updateUser(@RequestBody User userDTO) {
        String userId = StpUtil.getLoginIdAsString();
        log.info("开始更新用户信息，用户ID: {}, 更新数据: {}", userId, userDTO);
        try {
            if (!userId.equals(userDTO.id())) {
                log.warn("用户 {} 尝试修改其他用户 {} 的信息", userId, userDTO.id());
                throw new BusinessException("无权修改其他用户信息");
            }
            
            User existingUser = userRepository.findById(userId, FETCHER)
                    .orElseThrow(() -> new BusinessException("用户不存在"));
                
            userRepository.update(UserDraft.$.produce(draft -> {
                draft.setId(userId)
                        .setNickname(userDTO.nickname())
                        .setAvatar(userDTO.avatar())
                        .setPhone(existingUser.phone())
                        .setPassword(existingUser.password());
            }));
            
            log.info("用户信息更新成功，用户ID: {}", userId);
        } catch (Exception e) {
            log.error("更新用户信息失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/info")
    public User userInfo() {
        String userId = StpUtil.getLoginIdAsString();
        return userRepository.findById(userId, UserRepository.FETCHER)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    @PutMapping("/password")
    public void updatePassword(@RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        String userId = StpUtil.getLoginIdAsString();
        log.info("开始修改密码，用户ID: {}", userId);
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException("用户不存在"));
            
            if (!BCrypt.checkpw(passwordUpdateDTO.getOldPassword(), user.password())) {
                log.warn("用户 {} 输入的原密码不正确", userId);
                throw new BusinessException("原密码不正确");
            }

            userRepository.update(UserDraft.$.produce(draft -> {
                draft.setId(userId)
                        .setPassword(BCrypt.hashpw(passwordUpdateDTO.getNewPassword()))
                        .setPhone(user.phone())
                        .setNickname(user.nickname())
                        .setAvatar(user.avatar());
            }));
            log.info("密码修改成功，用户ID: {}", userId);
        } catch (Exception e) {
            log.error("修改密码失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping
    public void deleteAccount() {
        String userId = StpUtil.getLoginIdAsString();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 删除用户
        userRepository.deleteById(userId);
        
        // 注销登录
        StpUtil.logout();
    }
}
