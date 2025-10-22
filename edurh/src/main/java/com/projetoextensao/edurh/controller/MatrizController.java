package com.projetoextensao.edurh.controller;

import com.projetoextensao.edurh.model.Matriz;
import com.projetoextensao.edurh.model.Turma;
import com.projetoextensao.edurh.repository.MatrizRepository;
import com.projetoextensao.edurh.repository.TurmaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/matrizes")
public class MatrizController {

    @Autowired
    private MatrizRepository matrizRepository;

    @Autowired
    private TurmaRepository turmaRepository;

    // Criar matriz
    @PostMapping
    public ResponseEntity<Matriz> criarMatriz(@RequestBody Matriz matriz) {
        return ResponseEntity.ok(matrizRepository.save(matriz));
    }

    // Listar matrizes
    @GetMapping
    public List<Matriz> listarMatrizes() {
        return matrizRepository.findAll();
    }

    // Buscar matriz por ID
    @GetMapping("/{id}")
    public ResponseEntity<Matriz> buscarMatriz(@PathVariable Long id) {
        return matrizRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Atualizar matriz
    @PutMapping("/{id}")
    public ResponseEntity<Matriz> atualizarMatriz(@PathVariable Long id, @RequestBody Matriz dados) {
        return matrizRepository.findById(id)
                .map(matriz -> {
                    matriz.setTipo(dados.getTipo());
                    matriz.setTurno(dados.getTurno());
                    matriz.setCargaHoraria(dados.getCargaHoraria());
                    return ResponseEntity.ok(matrizRepository.save(matriz));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Deletar matriz
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarMatriz(@PathVariable Long id) {
        if (matrizRepository.existsById(id)) {
            matrizRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Adicionar turma a uma matriz
    @PostMapping("/{matrizId}/turmas")
    public ResponseEntity<Turma> adicionarTurma(@PathVariable Long matrizId, @RequestBody Turma turma) {
    return matrizRepository.findById(matrizId)
        .map(matriz -> {
            turma.setMatriz(matriz);
            Turma novaTurma = turmaRepository.save(turma);
            return ResponseEntity.ok(novaTurma);
        })
        .orElse(ResponseEntity.notFound().build());
}

}
