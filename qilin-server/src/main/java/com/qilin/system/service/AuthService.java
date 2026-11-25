package com.qilin.system.service;

import java.util.List;

import com.qilin.system.dto.request.AuthLoginRequest;
import com.qilin.system.dto.request.AuthUpdatePasswordRequest;
import com.qilin.system.vo.AuthLoginVO;

public interface AuthService {
    AuthLoginVO authLogin(AuthLoginRequest request);

    List<String> findRoleCodes(Long userId);

    void updatePassword(Long userId,AuthUpdatePasswordRequest request);
}
