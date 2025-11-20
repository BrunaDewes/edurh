package com.projetoextensao.edurh.controller;

import com.projetoextensao.edurh.repository.ProfessorRepository;
import com.projetoextensao.edurh.repository.MatrizRepository;
import com.projetoextensao.edurh.repository.TurmaRepository;
import com.projetoextensao.edurh.repository.DisciplinaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

import com.projetoextensao.edurh.model.Professor;
import com.projetoextensao.edurh.model.Disciplina;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin
public class DashboardController {

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private MatrizRepository matrizRepository;

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    @GetMapping("/estatisticas")
    public Map<String, Long> getEstatisticas() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("professores", professorRepository.count());
        stats.put("matrizes", matrizRepository.count());
        stats.put("turmas", turmaRepository.count());
        stats.put("disciplinas", disciplinaRepository.count());
        return stats; // Spring já transforma em JSON
    }

    @GetMapping("/professores-ultrapassados")
    public List<Map<String, Object>> getProfessoresUltrapassados() {
        List<Professor> professores = professorRepository.findAll();
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Professor p : professores) {
            // Soma dos períodos (disciplinas do professor)
            int totalPeriodos = 0;
            if (p.getDisciplinas() != null) {
                totalPeriodos = p.getDisciplinas().stream()
                        .filter(Objects::nonNull)
                        .mapToInt(Disciplina::getCargaHoraria)
                        .sum();
            }

            // Novo cálculo correto usando tabela RT -> períodos
            int maxPeriodos = calcularMaxPeriodosRT(p.getCargaHoraria());

            // Excedeu?
            if (totalPeriodos > maxPeriodos) {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", p.getId());
                dto.put("nome", p.getNome());
                dto.put("rtHoras", p.getCargaHoraria());
                dto.put("maxPeriodos", maxPeriodos);
                dto.put("totalPeriodos", totalPeriodos);
                resultado.add(dto);
            }
        }

        return resultado;
    }

    // Converte RT (horas) em períodos de aula (50 min)
    private int calcularMaxPeriodosRT(int rt) {
        if (rt <= 0) return 0;

        switch (rt) {
            case 20:
                return 16;
            case 30:
                return 24;
            case 40:
                return 32;
            default:
                // aproximação: 80% do RT em períodos
                return Math.round(rt * 0.8f);
        }
    }


}
