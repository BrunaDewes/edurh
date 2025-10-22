package com.projetoextensao.edurh.controller;

import com.projetoextensao.edurh.model.Disciplina;
import com.projetoextensao.edurh.model.Professor;
import com.projetoextensao.edurh.model.Turma;
import com.projetoextensao.edurh.repository.DisciplinaRepository;
import com.projetoextensao.edurh.repository.ProfessorRepository;
import com.projetoextensao.edurh.repository.TurmaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/disciplinas")
public class DisciplinaController {

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    // Criar disciplina
    @PostMapping
    public ResponseEntity<Disciplina> criarDisciplina(@RequestBody Disciplina disciplina) {
        return ResponseEntity.ok(disciplinaRepository.save(disciplina));
    }

    // Listar todas disciplinas
    @GetMapping
    public List<Disciplina> listarDisciplinas() {
        return disciplinaRepository.findAll();
    }

    // Buscar disciplina por ID
    @GetMapping("/{id}")
    public ResponseEntity<Disciplina> buscarDisciplina(@PathVariable Long id) {
        return disciplinaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Atualizar disciplina
    @PutMapping("/{id}")
    public ResponseEntity<Disciplina> atualizarDisciplina(@PathVariable Long id, @RequestBody Disciplina dados) {
        return disciplinaRepository.findById(id)
                .map(disciplina -> {
                    disciplina.setNome(dados.getNome());
                    disciplina.setCargaHoraria(dados.getCargaHoraria());
                    return ResponseEntity.ok(disciplinaRepository.save(disciplina));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Deletar disciplina
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarDisciplina(@PathVariable Long id) {
        if (disciplinaRepository.existsById(id)) {
            disciplinaRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Vincular disciplina a turma
    @PostMapping("/{disciplinaId}/turmas/{turmaId}")
    public ResponseEntity<?> adicionarTurmaADisciplina(@PathVariable Long disciplinaId, @PathVariable Long turmaId) {
        Optional<Disciplina> discOpt = disciplinaRepository.findById(disciplinaId);
        Optional<Turma> turmaOpt = turmaRepository.findById(turmaId);

        if (discOpt.isPresent() && turmaOpt.isPresent()) {
            Disciplina disciplina = discOpt.get();
            Turma turma = turmaOpt.get();

            disciplina.getTurmas().add(turma);
            turma.getDisciplinas().add(disciplina);

            disciplinaRepository.save(disciplina);
            turmaRepository.save(turma);

            return ResponseEntity.ok("Disciplina vinculada à turma!");
        }
        return ResponseEntity.notFound().build();
    }

    // Vincular professor a disciplina
    @PostMapping("/{disciplinaId}/professores/{professorId}")
    public ResponseEntity<?> adicionarProfessorADisciplina(@PathVariable Long disciplinaId, @PathVariable Long professorId) {
        Optional<Disciplina> discOpt = disciplinaRepository.findById(disciplinaId);
        Optional<Professor> profOpt = professorRepository.findById(professorId);

        if (discOpt.isPresent() && profOpt.isPresent()) {
            Disciplina disciplina = discOpt.get();
            Professor professor = profOpt.get();

            disciplina.getProfessores().add(professor);
            professor.getDisciplinas().add(disciplina);

            disciplinaRepository.save(disciplina);
            professorRepository.save(professor);

            return ResponseEntity.ok("Professor vinculado à disciplina!");
        }
        return ResponseEntity.notFound().build();
    }
}
    