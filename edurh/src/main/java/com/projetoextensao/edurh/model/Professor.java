package com.projetoextensao.edurh.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Professor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private int cargaHoraria; // CH total permitida
    private String turno;     // manhã, tarde, noite

    @ManyToMany
    @JoinTable(
        name = "professor_matriz",
        joinColumns = @JoinColumn(name = "professor_id"),
        inverseJoinColumns = @JoinColumn(name = "matriz_id")
    )
    private Set<Matriz> matrizes = new HashSet<>();

    @ManyToMany(mappedBy = "professores")
    private Set<Disciplina> disciplinas = new HashSet<>();
    
    // getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getCargaHoraria() { return cargaHoraria; }
    public void setCargaHoraria(int cargaHoraria) { this.cargaHoraria = cargaHoraria; }

    public String getTurno() { return turno; }
    public void setTurno(String turno) { this.turno = turno; }

    public Set<Matriz> getMatrizes() { return matrizes; }
    public void setMatrizes(Set<Matriz> matrizes) { this.matrizes = matrizes; }

    public Set<Disciplina> getDisciplinas() { return disciplinas; }
    public void setDisciplinas(Set<Disciplina> disciplinas) { this.disciplinas = disciplinas; }
}
