package com.atguigu.gulimall.member.service;

import com.atguigu.common.vo.SocialUser;
import com.atguigu.common.vo.UserLoginVo;
import com.atguigu.gulimall.member.Vo.MemberRegist;
import com.atguigu.gulimall.member.exception.PhoneExist;
import com.atguigu.gulimall.member.exception.UsernameExist;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.vo.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-04-12 13:53:13
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegist vo);

    void checkPhoneUnique(String phone) throws PhoneExist;

    void checkUserNameUnique(String username) throws UsernameExist;

    MemberEntity login(UserLoginVo vo);

    MemberEntity login(SocialUser vo) throws Exception;

}

