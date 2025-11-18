package com.projetoextensao.edurh.controller;

import com.projetoextensao.edurh.model.Matriz;
import com.projetoextensao.edurh.model.Turma;
import com.projetoextensao.edurh.repository.MatrizRepository;
import com.projetoextensao.edurh.repository.TurmaRepository;
import com.projetoextensao.edurh.repository.DisciplinaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
        // se vier matriz com id, garante que é uma entidade gerenciada
        if (turma.getMatriz() != null && turma.getMatriz().getId() != null) {
            Optional<Matriz> matrizOpt = matrizRepository.findById(turma.getMatriz().getId());
            matrizOpt.ifPresent(turma::setMatriz);
        } else {
            turma.setMatriz(null);
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
                    if (dados.getMatriz() != null && dados.getMatriz().getId() != null) {
                        matrizRepository.findById(dados.getMatriz().getId())
                                .ifPresent(turma::setMatriz);
                    } else {
                        turma.setMatriz(null);
                    }
                    return ResponseEntity.ok(turmaRepository.save(turma));
                })
                .orElse(ResponseEntity.notFound().build());
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
