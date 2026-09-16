package com.openbiz.workorder.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginBody;
import com.ruoyi.framework.web.service.SysLoginService;

/**
 * Minimal RuoYi login shell for independent consumer (SysLoginController lives in ruoyi-admin, not framework).
 * Not an OpenBiz WorkOrder reimplementation.
 */
@RestController
public class AuthController
{
    private final SysLoginService loginService;

    public AuthController(SysLoginService loginService)
    {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody loginBody)
    {
        AjaxResult ajax = AjaxResult.success();
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(),
                loginBody.getCode(), loginBody.getUuid());
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }
}
