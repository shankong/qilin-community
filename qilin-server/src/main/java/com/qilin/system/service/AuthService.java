package com.qilin.system.service;

import com.qilin.system.dto.request.AuthLoginRequest;
import com.qilin.system.vo.AuthLoginVO;

public interface AuthService {
    AuthLoginVO authLogin(AuthLoginRequest request);
}
