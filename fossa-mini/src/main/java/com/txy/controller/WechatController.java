package com.txy.controller;

import com.txy.dto.*;
import com.txy.entity.Player;
import com.txy.log.ApiLog;
import com.txy.service.PlayerService;

import com.txy.service.WechatService;
import com.txy.util.CommonUtil;
import com.txy.util.Md5Salt;
import com.txy.vo.PlayerVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by huyawei 2026/9/10 09:33
 */
@RestController
@RequestMapping("/api/wechat")
public class WechatController extends BaseController{

    @Autowired
    private WechatService wechatService;
    @Autowired
    private PlayerService playerService;

    /**
     * 登录接口
     * POST /api/wechat/login
     */
    @ApiLog("用户登陆接口")
    @PostMapping("/login")
    public ResponseEntity<PlayerVo> login(@RequestBody WxLoginRequest request) {
        ResponseEntity responseEntity = new ResponseEntity(StatusCode.OK.getValue(), "请求成功");

        // 1. 用 code 换 openid + session_key
        Code2SessionResponse session = wechatService.code2Session(request.getCode());

        if (session.getErrcode() != null && session.getErrcode() != 0) {
            responseEntity = new ResponseEntity(StatusCode.ERROR.getValue(), "请求失败");
            return responseEntity;
        }

        String openId = session.getOpenid();
//        String sessionKey = session.getSession_key();
        PlayerVo playerVo = new PlayerVo();
        Player player = playerService.getByOpenId(openId);
        if (CommonUtil.isNull(player)){
            player = new Player();
            try {
                player.setId(Md5Salt.encrypt(openId)[1]);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            player.setWxOpenId(openId);
            long l = System.currentTimeMillis();
            playerService.add(player);
        }
        BeanUtils.copyProperties(player, playerVo);
        playerVo.setSessionKey(player.getId());
        refreshCurrentBuyerVo(player.getId(),playerVo);
//        if (request.getEncryptedData() != null && request.getIv() != null) {
//            PhoneNumberInfo phoneInfo = wechatService.decryptPhoneNumber(
//                    request.getEncryptedData(),
//                    sessionKey,
//                    request.getIv()
//            );
//            // 5. 把手机号存到用户表
//            player.setPhone(phoneInfo.getPhoneNumber());
//            playerService.update(player);
//        }
        responseEntity.setResult(playerVo);
        return responseEntity;
    }
    @ApiLog("用户修改接口")
    @PostMapping("/user/update")
    public ResponseEntity<UserInfo> login(@RequestBody UserRequest request, @RequestHeader("sessionId") String sessionId) {
        ResponseEntity responseEntity = new ResponseEntity(StatusCode.OK.getValue(), "请求成功");

        // 获取当前授权用户信息
        PlayerVo playerVo = getCurrentBuyerVo( sessionId);


        if (CommonUtil.isNull(playerVo)){
            responseEntity.setStatusCode(StatusCode.SESSION_TIMEOUT.getValue());
            responseEntity.setMessage("会话过期");
            return responseEntity;
        }
        Player player = new Player();
        player.setId(sessionId);
//        player.setWxOpenId(sessionId);
        player.setName(request.getNickname());
        player.setAvatar(request.getAvatar());
        playerService.update(player);
        playerVo.setAvatar(request.getAvatar());
        playerVo.setName(request.getNickname());
        responseEntity.setResult(playerVo);
        refreshCurrentBuyerVo(player.getId(),playerVo);

        // 2. 查/建用户（按你自己的用户表来）
        // User user = userService.findByOpenid(openid);
        // if (user == null) { userService.create(openid); }

        // 3. 生成你自己的 token（JWT 等）
        // String token = jwtUtil.generateToken(openid);

        // 4. 如果有 encryptedData + iv，顺便解密手机号
//        if (request.getEncryptedData() != null && request.getIv() != null) {
//            PhoneNumberInfo phoneInfo = wechatService.decryptPhoneNumber(
//                    request.getEncryptedData(),
//                    sessionKey,
//                    request.getIv()
//            );
//            // 5. 把手机号存到用户表
//
//            player.setPhone(phoneInfo.getPhoneNumber());
//            playerService.update(player);
//        }

        return responseEntity;
    }

}
