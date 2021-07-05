package com.atguigu.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.common.vo.SocialUser;
import com.atguigu.common.vo.UserLoginVo;
import com.atguigu.gulimall.member.Vo.MemberRegist;
import com.atguigu.gulimall.member.dao.MemberLevelDao;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.atguigu.gulimall.member.exception.PhoneExist;
import com.atguigu.gulimall.member.exception.UsernameExist;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.member.dao.MemberDao;
import com.atguigu.common.vo.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public void regist(MemberRegist vo) {

        MemberDao memberDao = this.baseMapper;
        MemberEntity menberEntity = new MemberEntity();
        //设置默认等级
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        menberEntity.setLevelId(levelEntity.getId());

        //检查用户名和手机号是否注册过
        checkPhoneUnique(vo.getPhone());
        checkUserNameUnique(vo.getUserName());
        menberEntity.setMobile(vo.getPhone());
        menberEntity.setUsername(vo.getUserName());

        //设置密码
        //MD5加盐值加密
        //Spring 密码编码器
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String password = passwordEncoder.encode(vo.getPassword());
        menberEntity.setPassword(password);

        //其他默认信息

        memberDao.insert(menberEntity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExist{
        MemberDao baseMapper = this.baseMapper;
        Integer mobel = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if(mobel > 0){
            throw new PhoneExist();
        }

    }

    @Override
    public void checkUserNameUnique(String username) throws UsernameExist{
        MemberDao baseMapper = this.baseMapper;
        Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if(count > 0){
            throw new UsernameExist();
        }
    }

    @Override
    public MemberEntity login(UserLoginVo vo) {
        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();
        // 查询数据库
        MemberDao memberDao = this.baseMapper;
        MemberEntity entity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", loginacct));
        if (entity != null){
            //登录成功
            String passwordDb = entity.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            //密码检测

            boolean matches = passwordEncoder.matches(password, passwordDb);
            if(matches){
                return entity;
            }else{
                return null;
            }
        }else{
            //登录失败
            return null;
        }

    }

    @Override
    public MemberEntity login(SocialUser vo) throws Exception {
        //登录和注册合并逻辑
        String uid = vo.getUid();
        //1.判断当前社交用户是否已经登陆过
        MemberDao memberDao = this.baseMapper;
        MemberEntity entity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if(entity != null){
            //这个用户已经注册了
            MemberEntity update = new MemberEntity();
            update.setId(entity.getId());
            update.setAccessToken(vo.getAccess_token());
            update.setExpiresIn(vo.getExpires_in());
            memberDao.updateById(update);

            entity.setAccessToken(vo.getAccess_token());
            entity.setExpiresIn(vo.getExpires_in());
            return entity;
        }else {
            //2. 没查到就注册
            MemberEntity regist = new MemberEntity();
            try {
                Map<String, String> query = new HashMap<>();
                query.put("access_token", vo.getAccess_token());
                query.put("uid", vo.getUid());
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<>(), query);
                if (response.getStatusLine().getStatusCode() == 200) {
                    String json = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(json);
                    String name = jsonObject.getString("name");
                    String gender = jsonObject.getString("gender");
                    regist.setGender("m".equals(gender) ? 1 : 0);
                }
            }catch(Exception e){}
            //3. 查询当前用户的社交账号信息
            regist.setSocialUid(vo.getUid());
            regist.setAccessToken(vo.getAccess_token());
            regist.setExpiresIn(vo.getExpires_in());
            memberDao.insert(regist);
            return regist;
        }
    }
}