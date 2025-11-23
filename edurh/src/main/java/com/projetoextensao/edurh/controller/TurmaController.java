package com.projetoextensao.edurh.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
import org.springframework.web.server.ResponseStatusException;

import com.projetoextensao.edurh.model.Disciplina;
import com.projetoextensao.edurh.model.Matriz;
import com.projetoextensao.edurh.model.Turma;
import com.projetoextensao.edurh.model.Usuario;
import com.projetoextensao.edurh.repository.DisciplinaRepository;
import com.projetoextensao.edurh.repository.MatrizRepository;
import com.projetoextensao.edurh.repository.TurmaRepository;
import com.projetoextensao.edurh.repository.UsuarioRepository;

@RestController
@RequestMapping("/turmas")
@CrossOrigin
public class TurmaController {

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private MatrizRepository matrizRepository;

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // LISTAR TODAS
    @GetMapping
    public List<Turma> listarTurmas() {
        Usuario dono = getUsuarioLogado();
        return turmaRepository.findByDono(dono);
    }

    // BUSCAR POR ID (detalhes)
    @GetMapping("/{id}")
    public ResponseEntity<Turma> buscarPorId(@PathVariable Long id) {
        Usuario dono = getUsuarioLogado();

        return turmaRepository.findById(id)
                .filter(t -> t.getDono() != null && t.getDono().getId().equals(dono.getId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(403).build());
    }

    // CRIAR TURMA
    @PostMapping
    public ResponseEntity<Turma> criarTurma(@RequestBody Turma turma) {

         Usuario dono = getUsuarioLogado();

        // Obrigatoriedade de matriz ao criar turma
        if (turma.getMatriz() == null || turma.getMatriz().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A turma precisa estar vinculada a uma matriz."
            );
        }

        // Buscar matriz e validar dono
        Matriz matriz = matrizRepository.findById(turma.getMatriz().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Matriz informada não encontrada."
                ));

        if (matriz.getDono() == null || !matriz.getDono().getId().equals(dono.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não pode vincular turmas a uma matriz de outro usuário."
            );
        }

        turma.setMatriz(matriz);
        turma.setDono(dono);

        Turma salva = turmaRepository.save(turma);
        return ResponseEntity.ok(salva);
    }

    // ATUALIZAR TURMA
    @PutMapping("/{id}")
    public ResponseEntity<Turma> atualizarTurma(@PathVariable Long id, @RequestBody Turma dados) {
        Usuario dono = getUsuarioLogado();

        return turmaRepository.findById(id)
                .filter(t -> t.getDono() != null && t.getDono().getId().equals(dono.getId()))
                .map(turma -> {
                    turma.setNome(dados.getNome());

                    //Obrigatoriedade de matriz também no UPDATE
                    if (dados.getMatriz() == null || dados.getMatriz().getId() == null) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "A turma precisa estar vinculada a uma matriz."
                        );
                    }

                    // Buscar matriz válida
                    Matriz matriz = matrizRepository.findById(dados.getMatriz().getId())
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    "Matriz informada não encontrada."
                            ));
                    
                    // Validar dono da matriz
                    if (matriz.getDono() == null || !matriz.getDono().getId().equals(dono.getId())) {
                        throw new ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "Você não pode vincular turmas a uma matriz de outro usuário."
                        );
                    }

                    turma.setMatriz(matriz);
                    Turma salva = turmaRepository.save(turma);
                    return ResponseEntity.ok(salva);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ---------------- Vincular DISCIPLINA à TURMA (com validação da matriz) ----------------
    @PostMapping("/{turmaId}/disciplinas/{disciplinaId}")
    public ResponseEntity<?> adicionarDisciplinaNaTurma(@PathVariable Long turmaId, @PathVariable Long disciplinaId) {
        Usuario dono = getUsuarioLogado();

        Optional<Turma> turmaOpt = turmaRepository.findById(turmaId);
        Optional<Disciplina> disciplinaOpt = disciplinaRepository.findById(disciplinaId);

        if (turmaOpt.isEmpty() || disciplinaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Turma turma = turmaOpt.get();
        Disciplina disciplina = disciplinaOpt.get();

        // validar dono
        if (turma.getDono() == null || !turma.getDono().getId().equals(dono.getId()) ||
            disciplina.getDono() == null || !disciplina.getDono().getId().equals(dono.getId())) {
            return ResponseEntity.status(403).body("Operação não permitida.");
        }

        // Se a turma não tiver matriz, não tem como validar
        Matriz matriz = turma.getMatriz();
        if (matriz == null) {
            return ResponseEntity
                    .badRequest()
                    .body("Esta turma não possui uma matriz associada. Atribua uma matriz antes de adicionar disciplinas.");
        }

        // Evitar duplicar disciplina na turma
        if (turma.getDisciplinas() != null && turma.getDisciplinas().contains(disciplina)) {
            return ResponseEntity.ok("Disciplina já está vinculada a esta turma.");
        }

        // Soma de períodos já existentes na turma
        int somaPeriodosExistentes = turma.getDisciplinas()
                .stream()
                .mapToInt(Disciplina::getCargaHoraria)
                .sum();

        // Nova soma considerando a disciplina que está sendo adicionada
        int novaSoma = somaPeriodosExistentes + disciplina.getCargaHoraria();

        // Validação: não pode ultrapassar a CH da matriz
        if (novaSoma > matriz.getCargaHoraria()) {
            String msg = String.format(
                    "A soma dos períodos das disciplinas (%d) excede a carga horária da matriz (%d).",
                    novaSoma, matriz.getCargaHoraria()
            );
            return ResponseEntity.badRequest().body(msg);
        }

        // Passou na validação → vincula nos dois lados do relacionamento
        turma.getDisciplinas().add(disciplina);
        disciplina.getTurmas().add(turma);

        turmaRepository.save(turma);
        disciplinaRepository.save(disciplina);

        return ResponseEntity.ok("Disciplina vinculada à turma com sucesso!");
    }


    // ---------------- Remover DISCIPLINA da TURMA ----------------
    @DeleteMapping("/{turmaId}/disciplinas/{disciplinaId}")
    public ResponseEntity<?> removerDisciplinaDaTurma(@PathVariable Long turmaId, @PathVariable Long disciplinaId) {
        Usuario dono = getUsuarioLogado();
        
        Optional<Turma> turmaOpt = turmaRepository.findById(turmaId);
        Optional<Disciplina> disciplinaOpt = disciplinaRepository.findById(disciplinaId);

        if (turmaOpt.isEmpty() || disciplinaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Turma turma = turmaOpt.get();
        Disciplina disciplina = disciplinaOpt.get();

        //validar dono
        if (turma.getDono() == null || !turma.getDono().getId().equals(dono.getId()) ||
            disciplina.getDono() == null || !disciplina.getDono().getId().equals(dono.getId())) {
            return ResponseEntity.status(403).body("Operação não permitida.");
        }

        // remove dos dois lados do ManyToMany
        if (turma.getDisciplinas() != null) {
            turma.getDisciplinas().remove(disciplina);
        }
        if (disciplina.getTurmas() != null) {
            disciplina.getTurmas().remove(turma);
        }

        turmaRepository.save(turma);
        disciplinaRepository.save(disciplina);

        return ResponseEntity.ok("Disciplina removida da turma!");
    }

    // DELETAR TURMA
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarTurma(@PathVariable Long id) {
        Usuario dono = getUsuarioLogado();

        Optional<Turma> opt = turmaRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Turma turma = opt.get();

        if (turma.getDono() == null || !turma.getDono().getId().equals(dono.getId())) {
            return ResponseEntity.status(403).build();
        }

        turmaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
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
}
