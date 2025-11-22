package com.projetoextensao.edurh.controller;

import com.projetoextensao.edurh.dto.LoginRequest;
import com.projetoextensao.edurh.dto.LoginResponse;
import com.projetoextensao.edurh.model.Usuario;
import com.projetoextensao.edurh.repository.UsuarioRepository;
import com.projetoextensao.edurh.security.JwtUtil;

import com.projetoextensao.edurh.dto.ResetSenhaRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.regex.Pattern;
import java.util.Map;


import java.util.Optional;

@RestController
@RequestMapping("/auth")
//@CrossOrigin(origins = "http://localhost:3000") // libera o frontend
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    private static final Pattern SENHA_FORTE = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*]).{8,}$"
    );

    // POST ----------------- LOGIN --------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(loginRequest.getEmail());

        // usuário não encontrado
        if (usuario.isEmpty()) {
            return ResponseEntity.status(401).body("Usuário não encontrado!");
        }

        // senha incorreta
        if (!passwordEncoder.matches(loginRequest.getSenha(), usuario.get().getSenha())) {
            return ResponseEntity.status(401).body("Senha inválida!");
        }

        // gerar token JWT
        String token = JwtUtil.generateToken(usuario.get().getEmail());

        // RETORNAR TAMBÉM O USUÁRIO
        return ResponseEntity.ok(
            new LoginResponse(token, usuario.get())
        );
    }

    // GET ---------------- PEGAR USUÁRIO PELO TOKEN ----------------
    @GetMapping("/me")
    public ResponseEntity<?> usuarioLogado(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Token inválido ou ausente.");
            }

            String token = authHeader.substring(7);
            String email = JwtUtil.getEmailFromToken(token); 

            Optional<Usuario> usuario = usuarioRepository.findByEmail(email);

            if (usuario.isPresent()) {
                Usuario u = usuario.get();
                Usuario usuarioSemSenha = new Usuario();
                usuarioSemSenha.setId(u.getId());
                usuarioSemSenha.setNome(u.getNome());
                usuarioSemSenha.setEmail(u.getEmail());
                return ResponseEntity.ok(usuarioSemSenha);
            } else {
                return ResponseEntity.status(404).body("Usuário não encontrado.");
            }

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Token inválido.");
        }
    }

    @PostMapping("/esqueci-senha")
    public ResponseEntity<?> esqueciSenha(@RequestBody Map<String, String> body) {
        String email = body.get("email");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("E-mail é obrigatório.");
        }

        // Procura usuário
        Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);

        // Resposta genérica, mesmo se não encontrar (pra não revelar se o e-mail existe)
        String msgGenerica = "Se o e-mail estiver cadastrado, você receberá um link para redefinir a senha.";

        if (optUsuario.isEmpty()) {
            // Não revela que não existe
            return ResponseEntity.ok(msgGenerica);
        }

        Usuario usuario = optUsuario.get();

        // Gera token JWT usando o email
        String token = JwtUtil.generateToken(email);

        // Link do frontend - AJUSTA se tua porta/URL forem outras
        String linkReset = "http://localhost:3000/reset-senha?token=" + token;

        // Monta o e-mail
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Redefinição de senha - EduRH");
            message.setText(
                    "Olá, " + usuario.getNome() + ",\n\n" +
                    "Recebemos uma solicitação para redefinir a sua senha no portal EduRH.\n" +
                    "Clique no link abaixo para escolher uma nova senha (válido por 2 horas):\n\n" +
                    linkReset + "\n\n" +
                    "Se você não fez essa solicitação, ignore este e-mail."
            );

            mailSender.send(message);
        } catch (Exception e) {
            // Se der erro no envio de e-mail
            return ResponseEntity.status(500).body("Erro ao enviar o e-mail de redefinição.");
        }

        return ResponseEntity.ok(msgGenerica);
    }

    @PostMapping("/reset-senha")
    public ResponseEntity<?> resetSenha(@RequestBody ResetSenhaRequest request) {
        String token = request.getToken();
        String novaSenha = request.getNovaSenha();

        if (token == null || token.isBlank() || novaSenha == null || novaSenha.isBlank()) {
            return ResponseEntity.badRequest().body("Token e nova senha são obrigatórios.");
        }

        // Valida formato da senha (igual ao frontend)
        if (!SENHA_FORTE.matcher(novaSenha).matches()) {
            return ResponseEntity.badRequest().body(
                    "A senha deve ter no mínimo 8 caracteres, incluindo 1 maiúscula, 1 minúscula e 1 caractere especial."
            );
        }

        // Valida o token
        if (!JwtUtil.validarToken(token)) {
            return ResponseEntity.status(400).body("Token inválido ou expirado.");
        }

        String email = JwtUtil.getEmailFromToken(token);
        if (email == null) {
            return ResponseEntity.status(400).body("Token inválido.");
        }

        Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);
        if (optUsuario.isEmpty()) {
            return ResponseEntity.status(404).body("Usuário não encontrado.");
        }

        Usuario usuario = optUsuario.get();
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        return ResponseEntity.ok("Senha alterada com sucesso!");
    }
}
