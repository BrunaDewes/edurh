package com.projetoextensao.edurh.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projetoextensao.edurh.model.Disciplina;
import com.projetoextensao.edurh.model.Matriz;
import com.projetoextensao.edurh.model.Professor;
import com.projetoextensao.edurh.model.Turma;
import com.projetoextensao.edurh.repository.DisciplinaRepository;
import com.projetoextensao.edurh.repository.MatrizRepository;
import com.projetoextensao.edurh.repository.ProfessorRepository;

@RestController
@RequestMapping("/professores")
@CrossOrigin
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

    // ---------------- Remover DISCIPLINA do PROFESSOR ----------------
    @DeleteMapping("/{professorId}/disciplinas/{disciplinaId}")
    public ResponseEntity<?> removerDisciplinaDoProfessor(
            @PathVariable Long professorId,
            @PathVariable Long disciplinaId) {

        Optional<Professor> professorOpt = professorRepository.findById(professorId);
        Optional<Disciplina> disciplinaOpt = disciplinaRepository.findById(disciplinaId);

        if (professorOpt.isEmpty() || disciplinaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Professor professor = professorOpt.get();
        Disciplina disciplina = disciplinaOpt.get();

        // Remove dos dois lados da relação ManyToMany
        if (professor.getDisciplinas() != null) {
            professor.getDisciplinas().remove(disciplina);
        }
        if (disciplina.getProfessores() != null) {
            disciplina.getProfessores().remove(professor);
        }

        professorRepository.save(professor);
        disciplinaRepository.save(disciplina);

        return ResponseEntity.ok("Disciplina removida do professor!");
    }

    // ---------------- Vincular MATRIZ ao PROFESSOR ----------------
    @PostMapping("/{professorId}/matrizes/{matrizId}")
    public ResponseEntity<?> adicionarMatrizAoProfessor(
            @PathVariable Long professorId,
            @PathVariable Long matrizId) {

        Optional<Professor> profOpt = professorRepository.findById(professorId);
        Optional<Matriz> matrizOpt = matrizRepository.findById(matrizId);

        if (profOpt.isEmpty() || matrizOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Professor professor = profOpt.get();
        Matriz matriz = matrizOpt.get();

        // evita duplicar
        if (!professor.getMatrizes().contains(matriz)) {
            professor.getMatrizes().add(matriz);
            professorRepository.save(professor);
        }

        return ResponseEntity.ok("Matriz adicionada ao professor!");
    }

    // ---------------- Remover MATRIZ do PROFESSOR ----------------
    @DeleteMapping("/{professorId}/matrizes/{matrizId}")
    public ResponseEntity<?> removerMatrizDoProfessor(
            @PathVariable Long professorId,
            @PathVariable Long matrizId) {

        Optional<Professor> profOpt = professorRepository.findById(professorId);
        Optional<Matriz> matrizOpt = matrizRepository.findById(matrizId);

        if (profOpt.isEmpty() || matrizOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Professor professor = profOpt.get();
        Matriz matriz = matrizOpt.get();

        if (professor.getMatrizes() != null &&
            professor.getMatrizes().contains(matriz)) {

            professor.getMatrizes().remove(matriz);
            professorRepository.save(professor);
        }

        return ResponseEntity.ok("Matriz removida do professor!");
    }

    // ---- Helpers para cálculo de períodos / RT (mesma lógica do front) ----
    private int calcularMaxPeriodosRT(Integer rt) {
        if (rt == null || rt <= 0) return 0;

        switch (rt) {
            case 20:
                return 16;
            case 30:
                return 24;
            case 40:
                return 32;
            default:
                // aproximação: 80% do RT em períodos de 50min
                return Math.round(rt * 0.8f);
        }
    }

    private int calcularTotalPeriodosProfessor(Professor prof) {
        if (prof.getDisciplinas() == null) return 0;

        return prof.getDisciplinas().stream()
                .filter(d -> d != null)
                .mapToInt(d -> Optional.ofNullable(d.getCargaHoraria()).orElse(0))
                .sum();
    }



//---------------------- Relatórios -------------------------------------------------------------------------------------------

    // ---------------- Relatório: CH por professor (com preparação de 16h) ----------------
    @GetMapping("/relatorio/ch")
    public List<Map<String, Object>> relatorioCHPorProfessor() {
        List<Map<String, Object>> relatorio = new ArrayList<>();

        for (Professor prof : professorRepository.findAll()) {

            int rtHoras = Optional.ofNullable(prof.getCargaHoraria()).orElse(0); // RT do professor
            int totalPeriodos = calcularTotalPeriodosProfessor(prof);                  // períodos usados
            int maxPeriodos = calcularMaxPeriodosRT(rtHoras);                          // limite em períodos

            // aqui eu estou usando "preparação" como períodos ainda livres
            int preparacaoPeriodos = Math.max(0, maxPeriodos - totalPeriodos);

            Map<String, Object> item = new HashMap<>();
            item.put("professor", prof.getNome());
            item.put("cargaHorariaHoras", rtHoras);       // agora é RT de verdade (20, 30, 40, 44…)
            item.put("totalPeriodos", totalPeriodos);     // soma das disciplinas (igual DetalhesProfessor)
            item.put("preparacaoPeriodos", preparacaoPeriodos); // períodos ainda disponíveis
            item.put("periodosParaAulas", maxPeriodos);   // limite a partir do RT (igual DetalhesProfessor)

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

        // Mapa: professorNome -> (periodo -> DTO)
        Map<String, Map<String, Map<String, Object>>> mapa = new HashMap<>();

        for (Matriz matriz : matrizRepository.findAll()) {
            String periodo = (matriz.getTurno() != null) ? matriz.getTurno().name() : "UNDEFINED";

            for (Turma turma : matriz.getTurmas()) {
                String turmaNome = turma.getNome();

                for (Disciplina disciplina : turma.getDisciplinas()) {
                    int periodosDisciplina = disciplina.getCargaHoraria(); // períodos

                    for (Professor prof : disciplina.getProfessores()) {
                        String profNome = prof.getNome();

                        mapa.putIfAbsent(profNome, new HashMap<>());
                        Map<String, Map<String, Object>> mapaPeriodo = mapa.get(profNome);

                        mapaPeriodo.putIfAbsent(periodo, new HashMap<>());
                        Map<String, Object> dto = mapaPeriodo.get(periodo);

                        if (dto.isEmpty()) {
                            int rtHoras = Optional.ofNullable(prof.getCargaHoraria()).orElse(0);
                            int maxPeriodos = calcularMaxPeriodosRT(rtHoras);

                            dto.put("professor", profNome);
                            dto.put("periodo", periodo);
                            dto.put("totalPeriodos", 0);          // vamos acumular
                            dto.put("maxPeriodos", maxPeriodos);  // limite pelo RT
                            dto.put("disciplinasTurmas", new ArrayList<String>());
                        }

                        // somar períodos
                        int atual = (int) dto.get("totalPeriodos");
                        dto.put("totalPeriodos", atual + periodosDisciplina);

                        @SuppressWarnings("unchecked")
                        List<String> lista = (List<String>) dto.get("disciplinasTurmas");
                        lista.add(disciplina.getNome() + " (" + turmaNome + ")");
                    }
                }
            }
        }

        // transformar em lista final e calcular períodos livres
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Map<String, Map<String, Object>> mapaPeriodo : mapa.values()) {
            for (Map<String, Object> dto : mapaPeriodo.values()) {
                int totalPeriodos = (int) dto.get("totalPeriodos");
                int maxPeriodos = (int) dto.get("maxPeriodos");
                int livres = Math.max(0, maxPeriodos - totalPeriodos);

                dto.put("periodosLivres", livres);
                resultado.add(dto);
            }
        }

        return resultado;
    }
}
