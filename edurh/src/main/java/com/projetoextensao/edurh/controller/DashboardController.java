package com.projetoextensao.edurh.controller;

import com.projetoextensao.edurh.repository.ProfessorRepository;
import com.projetoextensao.edurh.repository.MatrizRepository;
import com.projetoextensao.edurh.repository.TurmaRepository;
import com.projetoextensao.edurh.repository.DisciplinaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
}
