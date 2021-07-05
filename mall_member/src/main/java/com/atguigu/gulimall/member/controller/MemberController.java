package com.atguigu.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.atguigu.common.vo.SocialUser;
import com.atguigu.common.vo.UserLoginVo;
import com.atguigu.gulimall.member.Vo.MemberRegist;
import com.atguigu.gulimall.member.exception.PhoneExist;
import com.atguigu.gulimall.member.exception.UsernameExist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.common.vo.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * 会员
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-04-12 13:53:13
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }

    @PostMapping("/regist")
    public R register(@RequestBody MemberRegist vo){

        try{
            memberService.regist(vo);
        }catch(PhoneExist e){
            return R.error(10052,"手机号已存在");
        }catch(UsernameExist e){
            return R.error(10051,"用户名已存在");
        }

        return R.ok();
    }

    @PostMapping("/oauth2/login")
    public R login(@RequestBody SocialUser vo) throws Exception {
        MemberEntity entity = memberService.login(vo);
        if(entity != null){
            return R.ok().setData(entity);
        }else{
            return R.error(15003,"账号或者密码错误");
        }
    }

    @PostMapping("/login")
    public R login(@RequestBody UserLoginVo vo){
        MemberEntity entity = memberService.login(vo);
        if(entity != null){
            return R.ok();
        }else{
            return R.error(15003,"账号或者密码错误");
        }
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
