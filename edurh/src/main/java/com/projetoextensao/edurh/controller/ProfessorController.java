package com.projetoextensao.edurh.controller;

import com.projetoextensao.edurh.model.*;
import com.projetoextensao.edurh.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/professores")
public class ProfessorController {

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private MatrizRepository matrizRepository;

    @Autowired
    private DisciplinaRepository disciplinaRepository; // crie se ainda não existir

    // ---------------- CRUD básico ----------------
    @PostMapping
    public ResponseEntity<Professor> criarProfessor(@RequestBody Professor professor) {
        return ResponseEntity.ok(professorRepository.save(professor));
    }

    @GetMapping
    public List<Professor> listarProfessores() {
        return professorRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Professor> buscarProfessor(@PathVariable Long id) {
        return professorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Professor> atualizarProfessor(@PathVariable Long id, @RequestBody Professor dados) {
        return professorRepository.findById(id)
                .map(professor -> {
                    professor.setNome(dados.getNome());
                    professor.setCargaHoraria(dados.getCargaHoraria());
                    professor.setTurno(dados.getTurno());
                    return ResponseEntity.ok(professorRepository.save(professor));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProfessor(@PathVariable Long id) {
        if (professorRepository.existsById(id)) {
            professorRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ---------------- Vincular professor a uma disciplina ----------------
    @PostMapping("/{professorId}/disciplinas/{disciplinaId}")
    public ResponseEntity<?> adicionarProfessorADisciplina(@PathVariable Long professorId, @PathVariable Long disciplinaId) {
    Optional<Professor> professorOpt = professorRepository.findById(professorId);
    Optional<Disciplina> disciplinaOpt = disciplinaRepository.findById(disciplinaId);

    if (professorOpt.isPresent() && disciplinaOpt.isPresent()) {
        Professor professor = professorOpt.get();
        Disciplina disciplina = disciplinaOpt.get();

        disciplina.getProfessores().add(professor);
        professor.getDisciplinas().add(disciplina);

        disciplinaRepository.save(disciplina);
        professorRepository.save(professor);

        return ResponseEntity.ok("Professor adicionado à disciplina!");
    }
    return ResponseEntity.notFound().build();
}

    // ---------------- Relatório: CH por professor (com preparação de 16h) ----------------
    @GetMapping("/relatorio/ch")
    public List<Map<String, Object>> relatorioCHPorProfessor() {
        List<Map<String, Object>> relatorio = new ArrayList<>();

        final int PREPARACAO_PERIODOS = 16;
        final float FATOR_PERIODOS_POR_HORA = 0.8f; // conforme definido: totalPeriodos = horas * 0.8

        for (Professor prof : professorRepository.findAll()) {
            // soma das CH em horas pelas disciplinas que o professor ministra
            int somaHoras = prof.getDisciplinas()
                            .stream()
                            .mapToInt(Disciplina::getCargaHoraria)
                            .sum();

            int totalPeriodos = Math.round(somaHoras * FATOR_PERIODOS_POR_HORA);
            int periodosParaAulas = Math.max(0, totalPeriodos - PREPARACAO_PERIODOS);

            Map<String, Object> item = new HashMap<>();
            item.put("professor", prof.getNome());
            item.put("cargaHorariaHoras", somaHoras);         // ex: 40
            item.put("totalPeriodos", totalPeriodos);        // ex: 32
            item.put("preparacaoPeriodos", PREPARACAO_PERIODOS); // 16
            item.put("periodosParaAulas", periodosParaAulas);// ex: 16

            relatorio.add(item);
        }
        return relatorio;
    }


    // ---------------- Relatório: Professores por Matriz ----------------
    @GetMapping("/relatorio/matriz")
    public List<Map<String, Object>> relatorioProfessoresPorMatriz() {
        List<Map<String, Object>> relatorio = new ArrayList<>();

        for (Matriz matriz : matrizRepository.findAll()) {
            Map<String, Object> item = new HashMap<>();
            item.put("matriz", matriz.getTipo());

            Set<String> nomesProfessores = new HashSet<>();
            matriz.getTurmas().forEach(turma ->
                turma.getDisciplinas().forEach(disciplina ->
                    disciplina.getProfessores().forEach(prof -> nomesProfessores.add(prof.getNome()))
                )
            );

            item.put("professores", nomesProfessores);
            relatorio.add(item);
        }
        return relatorio;
    }


    // ---------------- Relatório detalhado: Matriz -> Turma -> Disciplina -> Professores ----------------
    @GetMapping("/relatorio/matriz-detalhado")
    public List<Map<String, Object>> relatorioMatrizDetalhado() {
        List<Map<String, Object>> relatorio = new ArrayList<>();

        for (Matriz matriz : matrizRepository.findAll()) {
            String tipoMatriz = matriz.getTipo();
            Long matrizId = matriz.getId();
            String turnoMatriz = matriz.getTurno() != null ? matriz.getTurno().name() : null;
            for (Turma turma : matriz.getTurmas()) {
                String nomeTurma = turma.getNome();
                for (Disciplina disciplina : turma.getDisciplinas()) {
                    List<String> nomesProf = disciplina.getProfessores().stream()
                            .map(Professor::getNome).collect(Collectors.toList());
                    Map<String, Object> item = new HashMap<>();
                    item.put("matrizId", matrizId);
                    item.put("matrizTipo", tipoMatriz);
                    item.put("turma", nomeTurma);
                    item.put("disciplina", disciplina.getNome());
                    item.put("professores", nomesProf);
                    item.put("cargaHoraria", disciplina.getCargaHoraria());
                    item.put("turno", turnoMatriz);
                    relatorio.add(item);
                }
            }
        }

        return relatorio;
    }

    // ---------------- Relatório: distribuição de CH por período (turno) por professor ----------------
    @GetMapping("/relatorio/distribuicao-ch-turno")
    public List<Map<String, Object>> relatorioDistribuicaoCHPorPeriodo() {

        // chave: professorNome -> periodo -> DTO(map)
        Map<String, Map<String, Map<String, Object>>> mapa = new HashMap<>();

        // para evitar duplicação por professor+periodo+disciplina, armazenamos ids já contados
        Map<String, Map<String, Set<Long>>> contados = new HashMap<>();

        for (Matriz matriz : matrizRepository.findAll()) {
            String periodo = matriz.getTurno() != null ? matriz.getTurno().name() : "UNDEFINED";
            for (Turma turma : matriz.getTurmas()) {
                String turmaNome = turma.getNome();
                for (Disciplina disciplina : turma.getDisciplinas()) {
                    int ch = disciplina.getCargaHoraria();
                    for (Professor prof : disciplina.getProfessores()) {
                        String profNome = prof.getNome();
                        mapa.putIfAbsent(profNome, new HashMap<>());
                        contados.putIfAbsent(profNome, new HashMap<>());

                        Map<String, Map<String, Object>> mapaPeriodo = mapa.get(profNome);
                        Map<String, Set<Long>> contadosPeriodo = contados.get(profNome);

                        mapaPeriodo.putIfAbsent(periodo, new HashMap<>());
                        contadosPeriodo.putIfAbsent(periodo, new HashSet<>());

                        // evita contar a mesma disciplina duas vezes para o mesmo prof/periodo
                        Set<Long> jaContados = contadosPeriodo.get(periodo);
                        if (!jaContados.contains(disciplina.getId())) {
                            Map<String, Object> dto = mapaPeriodo.get(periodo);
                            if (dto.isEmpty()) {
                                dto.put("professor", profNome);
                                dto.put("periodo", periodo);
                                dto.put("totalCH", 0);
                                dto.put("disciplinasTurmas", new ArrayList<String>());
                            }
                            // atualizar totalCH e lista
                            int atual = (int) dto.get("totalCH");
                            dto.put("totalCH", atual + ch);
                            @SuppressWarnings("unchecked")
                            List<String> lista = (List<String>) dto.get("disciplinasTurmas");
                            lista.add(disciplina.getNome() + " (" + turmaNome + ")");
                            jaContados.add(disciplina.getId());
                        }
                    }
                }
            }
        }

        // transformar para lista final e calcular preparacao/periodos
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Map<String, Map<String, Object>> mapaPeriodo : mapa.values()) {
            for (Map<String, Object> dto : mapaPeriodo.values()) {
                int totalCH = (int) dto.get("totalCH");
                
                int preparacaoPeriodos = 16; 
                int cargaParaAulas = Math.max(0, totalCH - preparacaoPeriodos);
                int periodos = Math.round(cargaParaAulas * 0.8f); 

                dto.put("preparacaoPeriodos", preparacaoPeriodos);
                dto.put("cargaParaAulas", cargaParaAulas);
                dto.put("periodos", periodos);
                resultado.add(dto);
            }
        }

        return resultado;
    }
}
