package com.projetoextensao.edurh.repository;

import com.projetoextensao.edurh.model.Matriz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatrizRepository extends JpaRepository<Matriz, Long> {}
