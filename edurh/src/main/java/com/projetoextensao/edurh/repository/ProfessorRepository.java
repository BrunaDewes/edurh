package com.projetoextensao.edurh.repository;

import com.projetoextensao.edurh.model.Professor;
import com.projetoextensao.edurh.model.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Long> {
    // Lista apenas os professores do usuário dono
    List<Professor> findByDono(Usuario dono);

    // Conta apenas os professores do usuário dono
    long countByDono(Usuario dono);
}
