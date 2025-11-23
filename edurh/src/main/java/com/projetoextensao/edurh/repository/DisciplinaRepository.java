package com.projetoextensao.edurh.repository;

import com.projetoextensao.edurh.model.Disciplina;
import com.projetoextensao.edurh.model.Usuario;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {
    // Lista apenas os professores do usuário dono
    List<Disciplina> findByDono(Usuario dono);

    // Conta apenas os professores do usuário dono
    long countByDono(Usuario dono);
}

