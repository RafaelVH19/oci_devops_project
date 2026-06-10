package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.InviteEmailContext;
import com.springboot.MyTodoList.dto.MailSendResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InviteEmailService {

    @Autowired
    private LumenMailService lumenMailService;

    public MailSendResult sendInviteEmail(InviteEmailContext context) {
        return lumenMailService.sendInvite(context);
    }
}
