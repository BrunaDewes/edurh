package com.projetoextensao.edurh.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;

@Entity
public class Disciplina {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome; // Ex: "História", "Matemática"

    // Uma disciplina pode estar em várias turmas
    @ManyToMany
    @JoinTable(
        name = "disciplina_turma",
        joinColumns = @JoinColumn(name = "disciplina_id"),
        inverseJoinColumns = @JoinColumn(name = "turma_id")
    )
    private Set<Turma> turmas = new HashSet<>();

    // Uma disciplina pode ter vários professores
    @ManyToMany
    @JoinTable(
        name = "disciplina_professor",
        joinColumns = @JoinColumn(name = "disciplina_id"),
        inverseJoinColumns = @JoinColumn(name = "professor_id")
    )
    private Set<Professor> professores = new HashSet<>(); // pode ter 1 ou mais professores

    private int cargaHoraria; // CH dessa disciplina

    // getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Set<Turma> getTurmas() { return turmas; }
    public void setTurmas(Set<Turma> turmas) { this.turmas = turmas; }

    public Set<Professor> getProfessores() { return professores; }
    public void setProfessores(Set<Professor> professores) { this.professores = professores; }

    public int getCargaHoraria() { return cargaHoraria; }
    public void setCargaHoraria(int cargaHoraria) { this.cargaHoraria = cargaHoraria; }
}