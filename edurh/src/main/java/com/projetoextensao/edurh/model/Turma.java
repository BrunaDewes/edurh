package com.projetoextensao.edurh.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id"
)
@Entity
public class Turma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome; // Ex: "6º ano", "7º ano"

    @ManyToOne
    @JoinColumn(name = "matriz_id")
    @JsonIgnoreProperties("turmas")
    private Matriz matriz;

    @ManyToMany(mappedBy = "turmas")
    @JsonIgnoreProperties("turmas")
    private Set<Disciplina> disciplinas = new HashSet<>();

    // getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Matriz getMatriz() { return matriz; }
    public void setMatriz(Matriz matriz) { this.matriz = matriz; }

    public Set<Disciplina> getDisciplinas() { return disciplinas; }
    public void setDisciplinas(Set<Disciplina> disciplinas) { this.disciplinas = disciplinas; }
}