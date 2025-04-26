package com.itgr.thumbbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itgr.thumbbackend.model.empty.User;
import com.itgr.thumbbackend.service.UserService;
import com.itgr.thumbbackend.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
 * @author ygking
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2025-04-26 18:22:16
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}




