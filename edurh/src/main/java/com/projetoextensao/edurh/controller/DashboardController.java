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
            int totalPeriodos = 0;
            if (p.getDisciplinas() != null) {
                for (Disciplina d : p.getDisciplinas()) {
                    if (d != null) {
                        totalPeriodos += d.getCargaHoraria(); // períodos
                    }
                }
            }

            // por enquanto comparamos direto períodos x RT em horas (lógica simples)
            if (totalPeriodos > p.getCargaHoraria()) {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", p.getId());
                dto.put("nome", p.getNome());
                dto.put("cargaHorariaMaxima", p.getCargaHoraria());
                dto.put("cargaHorariaAtual", totalPeriodos);
                resultado.add(dto);
            }
        }

        return resultado;
    }

}
