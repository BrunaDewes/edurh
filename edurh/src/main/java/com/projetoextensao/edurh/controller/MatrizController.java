package com.projetoextensao.edurh.controller;

import com.projetoextensao.edurh.model.Matriz;
import com.projetoextensao.edurh.model.Turma;
import com.projetoextensao.edurh.model.Usuario;
import com.projetoextensao.edurh.repository.MatrizRepository;
import com.projetoextensao.edurh.repository.TurmaRepository;
import com.projetoextensao.edurh.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/matrizes")
public class MatrizController {

    @Autowired
    private MatrizRepository matrizRepository;

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Criar matriz
    @PostMapping
    public ResponseEntity<Matriz> criarMatriz(@RequestBody Matriz matriz) {
        Usuario dono = getUsuarioLogado();
        matriz.setDono(dono);
        Matriz salva = matrizRepository.save(matriz);
        return ResponseEntity.ok(salva);
    }

    // Listar matrizes
    @GetMapping
    public List<Matriz> listarMatrizes() {
        Usuario dono = getUsuarioLogado();
        return matrizRepository.findByDono(dono);
    }

    // Buscar matriz por ID
    @GetMapping("/{id}")
    public ResponseEntity<Matriz> buscarMatriz(@PathVariable Long id) {
        Usuario dono = getUsuarioLogado();

        return matrizRepository.findById(id)
                .filter(m -> m.getDono() != null && m.getDono().getId().equals(dono.getId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(403).build());
    }

    // Atualizar matriz
    @PutMapping("/{id}")
    public ResponseEntity<Matriz> atualizarMatriz(@PathVariable Long id, @RequestBody Matriz dados) {
         Usuario dono = getUsuarioLogado();

        return matrizRepository.findById(id)
                .filter(m -> m.getDono() != null && m.getDono().getId().equals(dono.getId()))
                .map(matriz -> {
                    matriz.setTipo(dados.getTipo());
                    matriz.setTurno(dados.getTurno());
                    matriz.setCargaHoraria(dados.getCargaHoraria());
                    Matriz salva = matrizRepository.save(matriz);
                    return ResponseEntity.ok(salva);
                })
                .orElse(ResponseEntity.status(403).build());
    }

    // Deletar matriz
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarMatriz(@PathVariable Long id) {
        Usuario dono = getUsuarioLogado();

        Optional<Matriz> opt = matrizRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Matriz matriz = opt.get();

        if (matriz.getDono() == null || !matriz.getDono().getId().equals(dono.getId())) {
            return ResponseEntity.status(403).build();
        }

        matrizRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Adicionar turma a uma matriz
    @PostMapping("/{matrizId}/turmas")
    public ResponseEntity<Turma> adicionarTurma(@PathVariable Long matrizId, @RequestBody Turma turma) {
        Usuario dono = getUsuarioLogado();

        return matrizRepository.findById(matrizId)
            .filter(m -> m.getDono() != null && m.getDono().getId().equals(dono.getId()))
            .map(matriz -> {
                turma.setMatriz(matriz);

                // precisa existir campo "dono" em Turma
                turma.setDono(dono);

                Turma novaTurma = turmaRepository.save(turma);
                return ResponseEntity.ok(novaTurma);
            })
            .orElse(ResponseEntity.status(403).build());
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
