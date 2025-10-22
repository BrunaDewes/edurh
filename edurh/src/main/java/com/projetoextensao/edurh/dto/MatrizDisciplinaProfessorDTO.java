package com.projetoextensao.edurh.dto;

public class MatrizDisciplinaProfessorDTO {
    private Long matrizId;
    private String tipoMatriz;
    private String disciplina;
    private String professor;
    private int cargaHoraria;
    private String turno;

    public MatrizDisciplinaProfessorDTO(Long matrizId, String tipoMatriz, String disciplina, String professor, int cargaHoraria, String turno) {
        this.matrizId = matrizId;
        this.tipoMatriz = tipoMatriz;
        this.disciplina = disciplina;
        this.professor = professor;
        this.cargaHoraria = cargaHoraria;
        this.turno = turno;
    }

    public Long getMatrizId() { return matrizId; }
    public String getTipoMatriz() { return tipoMatriz; }
    public String getDisciplina() { return disciplina; }
    public String getProfessor() { return professor; }
    public int getCargaHoraria() { return cargaHoraria; }
    public String getTurno() { return turno; }
}
