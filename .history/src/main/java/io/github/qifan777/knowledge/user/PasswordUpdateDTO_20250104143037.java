package io.github.qifan777.knowledge.user;


import lombok.Data;

public class PasswordUpdateDTO {
    private String oldPassword;
    private String newPassword;

    // 构造方法
    public PasswordUpdateDTO(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    // Getter 方法
    public String getOldPassword() {
        return oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }


    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
