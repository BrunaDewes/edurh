package com.projetoextensao.edurh.dto;

import com.projetoextensao.edurh.model.Usuario;

public class LoginResponse {
    private String token;
    private Usuario usuario;

    public LoginResponse(String token, Usuario usuario) {
        this.token = token;
        this.usuario = usuario;
    }

    public String getToken() {
        return token;
    }

     public Usuario getUsuario() {
        return usuario;
    }
}
