package com.projetoextensao.edurh.dto;

import java.util.List;

public class DistribuicaoCHPeriodoDTO {
    // Cada período tem 50 minutos = 0.8333 horas
    public int getPeriodos() {
        // Considera totalCH em horas, cada período = 50 minutos = 0.8333 horas
        return (int) Math.round(totalCH / 0.8333);
    }
    private String professor;
    private String periodo;
    private int totalCH;
    private List<String> disciplinasTurmas;

    public DistribuicaoCHPeriodoDTO(String professor, String periodo, int totalCH, List<String> disciplinasTurmas) {
        this.professor = professor;
        this.periodo = periodo;
        this.totalCH = totalCH;
        this.disciplinasTurmas = disciplinasTurmas;
    }

    public String getProfessor() { return professor; }
    public String getPeriodo() { return periodo; }
    public int getTotalCH() { return totalCH; }
    public void setTotalCH(int totalCH) { this.totalCH = totalCH; }
    public List<String> getDisciplinasTurmas() { return disciplinasTurmas; }
}
