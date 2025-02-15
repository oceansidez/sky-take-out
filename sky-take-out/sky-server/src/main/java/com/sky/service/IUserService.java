package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

public interface IUserService {
    /**
     * 微信登录
     *
     * @param dto
     * @return
     */
    User wxLogin(UserLoginDTO dto);
}