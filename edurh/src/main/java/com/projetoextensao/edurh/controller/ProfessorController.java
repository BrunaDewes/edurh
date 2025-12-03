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
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.projetoextensao.edurh.model.Usuario;
import com.projetoextensao.edurh.repository.DisciplinaRepository;
import com.projetoextensao.edurh.repository.MatrizRepository;
import com.projetoextensao.edurh.repository.ProfessorRepository;
import com.projetoextensao.edurh.repository.UsuarioRepository;

import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/professores")
@CrossOrigin
public class ProfessorController {

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private MatrizRepository matrizRepository;

    @Autowired
    private DisciplinaRepository disciplinaRepository; 

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ---------------- CRUD básico ----------------
    @PostMapping
    public ResponseEntity<Professor> criarProfessor(@RequestBody Professor professor) {
        Usuario dono = getUsuarioLogado();   // pega o usuário dono (do token)
        professor.setDono(dono);             // marca o professor como pertencente a esse usuário

        Professor salvo = professorRepository.save(professor);
        return ResponseEntity.ok(salvo);
    }

    @GetMapping
    public List<Professor> listarProfessores() {
        Usuario dono = getUsuarioLogado();
        return professorRepository.findByDono(dono);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Professor> buscarProfessor(@PathVariable Long id) {
        Usuario dono = getUsuarioLogado();

    return professorRepository.findById(id)
            .filter(p -> p.getDono().getId().equals(dono.getId()))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(403).build());  // proibido acessar dos outros
    }

    @PutMapping("/{id}")
    public ResponseEntity<Professor> atualizarProfessor(@PathVariable Long id, @RequestBody Professor dados) {
        Usuario dono = getUsuarioLogado();

        return professorRepository.findById(id)
                .filter(p -> p.getDono().getId().equals(dono.getId()))
                .map(professor -> {
                    professor.setNome(dados.getNome());
                    professor.setCargaHoraria(dados.getCargaHoraria());
                    professor.setTurno(dados.getTurno());
                    Professor salvo = professorRepository.save(professor);
                    return ResponseEntity.ok(salvo);
                })
                .orElse(ResponseEntity.status(403).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProfessor(@PathVariable Long id) {
        Usuario dono = getUsuarioLogado();

        Optional<Professor> opt = professorRepository.findById(id);
        if (opt.isEmpty()) {
            // professor não existe
            return ResponseEntity.notFound().build();
        }

        Professor professor = opt.get();

        // se o professor não pertence ao usuário logado, bloqueia
        if (professor.getDono() == null || !professor.getDono().getId().equals(dono.getId())) {
            return ResponseEntity.status(403).build();
        }

        // 💡 PASSO 1: Lidar com o relacionamento INVERSO (Disciplina -> Professor)
        // Para cada disciplina ligada a este professor, remove a referência ao professor.
        // Isso limpa as linhas na tabela de junção 'disciplina_professor'.
        for (Disciplina disciplina : new HashSet<>(professor.getDisciplinas())) {
            if (disciplina.getProfessores() != null) {
                disciplina.getProfessores().remove(professor);
                // Salva a disciplina para persistir a mudança no lado proprietário
                disciplinaRepository.save(disciplina);
            }
        }
        // Opcional, mas limpa o cache da coleção no objeto
        professor.getDisciplinas().clear();


        // 💡 PASSO 2: Lidar com o relacionamento PROPRIETÁRIO (Professor -> Matriz)
        // Limpa as referências na tabela de junção 'professor_matriz'.
        if (professor.getMatrizes() != null) {
            professor.getMatrizes().clear();
            professorRepository.save(professor); // Salva para persistir a limpeza antes do DELETE
        }

        // 💡 PASSO 3: Exclusão final
        professorRepository.deleteById(id);
        return ResponseEntity.noContent().build();
        }

    // ---------------- Vincular professor a uma disciplina ----------------
    @PostMapping("/{professorId}/disciplinas/{disciplinaId}")
    public ResponseEntity<?> adicionarProfessorADisciplina(@PathVariable Long professorId, @PathVariable Long disciplinaId) {
        Usuario dono = getUsuarioLogado();
        
        Optional<Professor> professorOpt = professorRepository.findById(professorId);
        Optional<Disciplina> disciplinaOpt = disciplinaRepository.findById(disciplinaId);

        if (professorOpt.isEmpty() || disciplinaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Professor professor = professorOpt.get();
        Disciplina disciplina = disciplinaOpt.get();

        // BLOQUEAR se estiver mexendo em registros de outro usuário
        if (!professor.getDono().getId().equals(dono.getId()) ||
            !disciplina.getDono().getId().equals(dono.getId())) {
            return ResponseEntity.status(403).body("Operação não permitida.");
        }

        // evita duplicar
        if (!disciplina.getProfessores().contains(professor)) {
            disciplina.getProfessores().add(professor);
            professor.getDisciplinas().add(disciplina);

            disciplinaRepository.save(disciplina);
            professorRepository.save(professor);
        }

        return ResponseEntity.ok("Professor adicionado à disciplina!");
    }

    // ---------------- Remover DISCIPLINA do PROFESSOR ----------------
    @DeleteMapping("/{professorId}/disciplinas/{disciplinaId}")
    public ResponseEntity<?> removerDisciplinaDoProfessor( @PathVariable Long professorId, @PathVariable Long disciplinaId) {

        Usuario dono = getUsuarioLogado();

        Optional<Professor> professorOpt = professorRepository.findById(professorId);
        Optional<Disciplina> disciplinaOpt = disciplinaRepository.findById(disciplinaId);

        if (professorOpt.isEmpty() || disciplinaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Professor professor = professorOpt.get();
        Disciplina disciplina = disciplinaOpt.get();

        // garantir que ambos pertencem ao usuário logado
        if (!professor.getDono().getId().equals(dono.getId()) ||
            !disciplina.getDono().getId().equals(dono.getId())) {
            return ResponseEntity.status(403).body("Operação não permitida.");
        }

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
    public ResponseEntity<?> adicionarMatrizAoProfessor(@PathVariable Long professorId, @PathVariable Long matrizId) {
        Usuario dono = getUsuarioLogado();

        Optional<Professor> profOpt = professorRepository.findById(professorId);
        Optional<Matriz> matrizOpt = matrizRepository.findById(matrizId);

        if (profOpt.isEmpty() || matrizOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Professor professor = profOpt.get();
        Matriz matriz = matrizOpt.get();

        // validar dono
        if (!professor.getDono().getId().equals(dono.getId()) ||
            !matriz.getDono().getId().equals(dono.getId())) {
            return ResponseEntity.status(403).body("Operação não permitida.");
        }

        if (!professor.getMatrizes().contains(matriz)) {
            professor.getMatrizes().add(matriz);
            professorRepository.save(professor);
        }

        return ResponseEntity.ok("Matriz adicionada ao professor!");
    }

    // ---------------- Remover MATRIZ do PROFESSOR ----------------
    @DeleteMapping("/{professorId}/matrizes/{matrizId}")
    public ResponseEntity<?> removerMatrizDoProfessor(@PathVariable Long professorId, @PathVariable Long matrizId) {
        Usuario dono = getUsuarioLogado();

        Optional<Professor> profOpt = professorRepository.findById(professorId);
        Optional<Matriz> matrizOpt = matrizRepository.findById(matrizId);

        if (profOpt.isEmpty() || matrizOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Professor professor = profOpt.get();
        Matriz matriz = matrizOpt.get();

        // validar dono
        if (!professor.getDono().getId().equals(dono.getId()) ||
            !matriz.getDono().getId().equals(dono.getId())) {
            return ResponseEntity.status(403).body("Operação não permitida.");
        }

        // remover se existir
        if (professor.getMatrizes() != null && professor.getMatrizes().contains(matriz)) {
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

    //HELPER PRO DONO
    private Usuario getUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new RuntimeException("Usuário não autenticado");
        }

        String email = auth.getPrincipal().toString();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }



//---------------------- Relatórios -------------------------------------------------------------------------------------------

    // ---------------- Relatório: CH por professor (com preparação de 16h) ----------------
    @GetMapping("/relatorio/ch")
    public List<Map<String, Object>> relatorioCHPorProfessor() {
        List<Map<String, Object>> relatorio = new ArrayList<>();

        Usuario dono = getUsuarioLogado();
        // carrega todos os professores do dono para iterar
        List<Professor> professoresDoDono = professorRepository.findByDono(dono);

        // percorre cada professor e calcula totalPeriodos a partir das disciplinas nas turmas/matrizes
        for (Professor prof : professoresDoDono) {

            // Calcula totalPeriodos contando disciplinas onde o professor aparece dentro das turmas das matrizes do dono
            int totalPeriodos = 0;

            for (Matriz matriz : matrizRepository.findByDono(dono)) {
                for (Turma turma : matriz.getTurmas()) {
                    for (Disciplina disciplina : turma.getDisciplinas()) {
                        // se a disciplina lista esse professor entre seus professores, acumula
                        if (disciplina.getProfessores() != null) {
                            boolean presente = disciplina.getProfessores().stream()
                                .anyMatch(p -> p != null && p.getId() != null && p.getId().equals(prof.getId()));
                            if (presente) {
                                totalPeriodos += Optional.ofNullable(disciplina.getCargaHoraria()).orElse(0);
                            }
                        }
                    }
                }
            }

            // Também pega RT e converte
            int rtHoras = Optional.ofNullable(prof.getCargaHoraria()).orElse(0); // RT do professor
            int maxPeriodos = calcularMaxPeriodosRT(rtHoras);                          // limite em períodos

            // aqui eu estou usando "preparação" como períodos ainda livres
            int preparacaoPeriodos = Math.max(0, maxPeriodos - totalPeriodos);

            Map<String, Object> item = new HashMap<>();
            item.put("professor", prof.getNome());
            item.put("professorId", prof.getId()); // útil para front
            item.put("cargaHorariaHoras", rtHoras);
            item.put("totalPeriodos", totalPeriodos);
            item.put("preparacaoPeriodos", preparacaoPeriodos);
            item.put("periodosParaAulas", maxPeriodos);

            relatorio.add(item);
        }
        return relatorio;
    }


    // ---------------- Relatório: Professores por Matriz ----------------
    @GetMapping("/relatorio/matriz")
    public List<Map<String, Object>> relatorioProfessoresPorMatriz() {
        List<Map<String, Object>> relatorio = new ArrayList<>();

        Usuario dono = getUsuarioLogado();
        // carga todos os professores do dono uma vez (evita N+1 e permite checar matrizes)
        List<Professor> professoresDoDono = professorRepository.findByDono(dono);

        for (Matriz matriz : matrizRepository.findByDono(dono)) {
            Map<String, Object> item = new HashMap<>();
            item.put("matriz", matriz.getTipo());

            Set<String> nomesProfessores = new HashSet<>();

            // 1) professores que aparecem nas disciplinas das turmas da matriz
            matriz.getTurmas().forEach(turma ->
                turma.getDisciplinas().forEach(disciplina ->
                    disciplina.getProfessores().forEach(prof -> {
                        if (prof != null && prof.getNome() != null) {
                            nomesProfessores.add(prof.getNome());
                        }
                    })
                )
            );

            // 2) professores que possuem essa matriz em professor.matrizes
            for (Professor prof : professoresDoDono) {
                if (prof.getMatrizes() != null) {
                    boolean temAMatriz = prof.getMatrizes().stream()
                        .anyMatch(m -> m != null && m.getId() != null && m.getId().equals(matriz.getId()));
                    if (temAMatriz && prof.getNome() != null) {
                        nomesProfessores.add(prof.getNome());
                    }
                }
            }

            item.put("professores", nomesProfessores);
            relatorio.add(item);
        }
        return relatorio;
    }


    // ---------------- Relatório detalhado: Matriz -> Turma -> Disciplina -> Professores ----------------
    @GetMapping("/relatorio/matriz-detalhado")
    public List<Map<String, Object>> relatorioMatrizDetalhado() {
        List<Map<String, Object>> relatorio = new ArrayList<>();

        Usuario dono = getUsuarioLogado();
        for (Matriz matriz : matrizRepository.findByDono(dono)) {
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

        Usuario dono = getUsuarioLogado();
        // Primeiro: percorre matrizes -> turmas -> disciplinas e acumula total por professor/turno
        for (Matriz matriz : matrizRepository.findByDono(dono)) {
            String periodo = (matriz.getTurno() != null) ? matriz.getTurno().name() : "UNDEFINED";

            for (Turma turma : matriz.getTurmas()) {
                String turmaNome = turma.getNome();

                for (Disciplina disciplina : turma.getDisciplinas()) {
                    int periodosDisciplina = Optional.ofNullable(disciplina.getCargaHoraria()).orElse(0);

                    for (Professor prof : disciplina.getProfessores()) {
                        if (prof == null || prof.getNome() == null) continue;

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
                            dto.put("professorId", prof.getId());
                            dto.put("totalPeriodos", 0);
                            dto.put("maxPeriodos", maxPeriodos);
                            dto.put("disciplinasTurmas", new ArrayList<String>());
                        }

                        // acumula períodos por (professor, periodo)
                        int atual = (int) dto.get("totalPeriodos");
                        dto.put("totalPeriodos", atual + periodosDisciplina);

                        @SuppressWarnings("unchecked")
                        List<String> lista = (List<String>) dto.get("disciplinasTurmas");
                        lista.add(disciplina.getNome() + " (" + turmaNome + ")");
                    }
                }
            }
        }

        // Agora: calcular totalAcross (soma de todos os turnos) e preencher periodosLivres corretamente
        Map<String, Integer> totalAcross = new HashMap<>(); // professorNome -> total em todos turnos
        Map<String, Integer> maxPeriodosPorProfessor = new HashMap<>(); // professorNome -> maxPeriodos

        for (Map.Entry<String, Map<String, Map<String, Object>>> entry : mapa.entrySet()) {
            String profNome = entry.getKey();
            int soma = 0;
            int maxP = 0;

            for (Map<String, Object> dto : entry.getValue().values()) {
                soma += (int) dto.getOrDefault("totalPeriodos", 0);
                // garante maxPeriodos (todos os dtos daquele professor têm o mesmo maxPeriodos)
                int mp = (int) dto.getOrDefault("maxPeriodos", 0);
                if (mp > maxP) maxP = mp;
            }

            totalAcross.put(profNome, soma);
            maxPeriodosPorProfessor.put(profNome, maxP);
        }

        // transformar em lista final e calcular períodos livres corretos
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Map<String, Map<String, Object>> mapaPeriodo : mapa.values()) {
            for (Map<String, Object> dto : mapaPeriodo.values()) {
                String profNome = (String) dto.get("professor");
                int totalPeriodos = (int) dto.getOrDefault("totalPeriodos", 0);
                int maxPeriodos = (int) dto.getOrDefault("maxPeriodos", 0);

                int totalDoProfessor = totalAcross.getOrDefault(profNome, 0);

                // Períodos livres por turno (apenas informativo — não representa disponibilidade global)
                int livresPorTurno = Math.max(0, maxPeriodos - totalPeriodos);

                // Períodos livres globais (correto): capacidade total do professor menos o total usado em todos os turnos
                int livresTotal = Math.max(0, maxPeriodosPorProfessor.getOrDefault(profNome, maxPeriodos) - totalDoProfessor);

                dto.put("periodosLivresPorTurno", livresPorTurno);
                dto.put("periodosLivresTotal", livresTotal);

                resultado.add(dto);
            }
        }
        return resultado;
    }
}
