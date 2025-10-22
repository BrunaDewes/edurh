package com.projetoextensao.edurh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.projetoextensao.edurh.model.Usuario;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Aqui define buscas personalizadas
    // Ex: procurar usuário pelo email
    Optional<Usuario> findByEmail(String email);
}

