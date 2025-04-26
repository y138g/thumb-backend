package com.itgr.thumbbackend.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.itgr.thumbbackend.common.BaseResponse;
import com.itgr.thumbbackend.common.ResultUtils;
import com.itgr.thumbbackend.model.empty.User;
import com.itgr.thumbbackend.model.dto.user.UserLoginRequest;
import com.itgr.thumbbackend.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 健康检测接口
     *
     * @return 返回结果
     */
    @GetMapping("/health")
    public String health() {
        return "hello world";
    }

    /**
     * 用户登陆伪接口
     *
     * @param userLoginRequest 用户登陆请求
     * @return 返回用户
     */
    @PostMapping("/login")
    public BaseResponse<User> login(@RequestBody UserLoginRequest userLoginRequest) {
        Long id = userLoginRequest.getId();
        User user = userService.getById(id);
        StpUtil.login(id);
        return ResultUtils.success(user);
    }
}
