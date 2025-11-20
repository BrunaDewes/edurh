package com.projetoextensao.edurh.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.server.ResponseStatusException;

import com.projetoextensao.edurh.model.Disciplina;
import com.projetoextensao.edurh.model.Matriz;
import com.projetoextensao.edurh.model.Turma;
import com.projetoextensao.edurh.repository.DisciplinaRepository;
import com.projetoextensao.edurh.repository.MatrizRepository;
import com.projetoextensao.edurh.repository.TurmaRepository;

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

    // LISTAR TODAS
    @GetMapping
    public List<Turma> listarTurmas() {
        return turmaRepository.findAll();
    }

    // BUSCAR POR ID (detalhes)
    @GetMapping("/{id}")
    public ResponseEntity<Turma> buscarPorId(@PathVariable Long id) {
        return turmaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // CRIAR TURMA
    @PostMapping
    public ResponseEntity<Turma> criarTurma(@RequestBody Turma turma) {

         // Obrigatoriedade de matriz ao criar turma
        if (turma.getMatriz() == null || turma.getMatriz().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A turma precisa estar vinculada a uma matriz."
            );
        }

        // se vier matriz com id, garante que é uma entidade gerenciada
        if (turma.getMatriz() != null && turma.getMatriz().getId() != null) {
            Optional<Matriz> matrizOpt = matrizRepository.findById(turma.getMatriz().getId());
            matrizOpt.ifPresent(turma::setMatriz);
        } else {
            turma.setMatriz(null); //mas nunca vai entrar aqui por causa da validação acima
        }

        Turma salva = turmaRepository.save(turma);
        return ResponseEntity.ok(salva);
    }

    // ATUALIZAR TURMA
    @PutMapping("/{id}")
    public ResponseEntity<Turma> atualizarTurma(@PathVariable Long id, @RequestBody Turma dados) {
        return turmaRepository.findById(id)
                .map(turma -> {
                    turma.setNome(dados.getNome());

                    //Obrigatoriedade de matriz também no UPDATE
                    if (dados.getMatriz() == null || dados.getMatriz().getId() == null) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "A turma precisa estar vinculada a uma matriz."
                        );
                    }

                    // ✔ Buscar matriz válida
                    Matriz matriz = matrizRepository.findById(dados.getMatriz().getId())
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    "Matriz informada não encontrada."
                            ));

                    turma.setMatriz(matriz);
                    return ResponseEntity.ok(turmaRepository.save(turma));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ---------------- Vincular DISCIPLINA à TURMA (com validação da matriz) ----------------
    @PostMapping("/{turmaId}/disciplinas/{disciplinaId}")
    public ResponseEntity<?> adicionarDisciplinaNaTurma(
            @PathVariable Long turmaId,
            @PathVariable Long disciplinaId) {

        Optional<Turma> turmaOpt = turmaRepository.findById(turmaId);
        Optional<Disciplina> disciplinaOpt = disciplinaRepository.findById(disciplinaId);

        if (turmaOpt.isEmpty() || disciplinaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Turma turma = turmaOpt.get();
        Disciplina disciplina = disciplinaOpt.get();

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
    public ResponseEntity<?> removerDisciplinaDaTurma(
            @PathVariable Long turmaId,
            @PathVariable Long disciplinaId) {

        Optional<Turma> turmaOpt = turmaRepository.findById(turmaId);
        Optional<Disciplina> disciplinaOpt = disciplinaRepository.findById(disciplinaId);

        if (turmaOpt.isEmpty() || disciplinaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Turma turma = turmaOpt.get();
        Disciplina disciplina = disciplinaOpt.get();

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
        if (!turmaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        turmaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
