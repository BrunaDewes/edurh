package com.projetoextensao.edurh.controller;

import com.projetoextensao.edurh.dto.LoginRequest;
import com.projetoextensao.edurh.dto.LoginResponse;
import com.projetoextensao.edurh.model.Usuario;
import com.projetoextensao.edurh.repository.UsuarioRepository;
import com.projetoextensao.edurh.security.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000") // libera o frontend
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

        return ResponseEntity.ok(new LoginResponse(token));
    }

    // GET ---------------- PEGAR USUÁRIO PELO TOKEN ----------------
    @GetMapping("/me")
    public ResponseEntity<?> usuarioLogado(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Token inválido ou ausente.");
            }

            String token = authHeader.substring(7);
            String email = JwtUtil.validateToken(token);

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
}
