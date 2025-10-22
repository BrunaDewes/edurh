package com.projetoextensao.edurh.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Matriz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo; // campo livre: ex. EJA - Multi 

    private int cargaHoraria;

    @Enumerated(EnumType.STRING)
    private Turno turno; // INTEGRAL, MANHA, TARDE, NOTURNO

    @OneToMany(mappedBy = "matriz", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Turma> turmas = new HashSet<>();

    // getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getCargaHoraria() { return cargaHoraria; }
    public void setCargaHoraria(int cargaHoraria) { this.cargaHoraria = cargaHoraria; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public Turno getTurno() { return turno; }
    public void setTurno(Turno turno) { this.turno = turno; }

    public Set<Turma> getTurmas() { return turmas; }
    public void setTurmas(Set<Turma> turmas) { this.turmas = turmas; }
}
    
