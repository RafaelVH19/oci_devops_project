package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.InviteEmailContext;
import com.springboot.MyTodoList.dto.MailSendResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de gestionar el envío de correos electrónicos de invitación.
 *
 * Proporciona un método para enviar correos electrónicos de invitación utilizando
 * el servicio LumenMailService, que se encarga de la lógica específica de envío
 * de correos electrónicos.
 */
@Service
public class InviteEmailService {

    @Autowired
    private LumenMailService lumenMailService;

    /** Envía un correo electrónico de invitación utilizando el servicio LumenMailService y devuelve el resultado del envío. */
    public MailSendResult sendInviteEmail(InviteEmailContext context) {
        return lumenMailService.sendInvite(context);
    }
}
