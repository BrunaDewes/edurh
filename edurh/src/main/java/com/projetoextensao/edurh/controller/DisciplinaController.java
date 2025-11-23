package com.projetoextensao.edurh.controller;

import com.projetoextensao.edurh.model.Disciplina;
import com.projetoextensao.edurh.model.Professor;
import com.projetoextensao.edurh.model.Turma;
import com.projetoextensao.edurh.model.Usuario;
import com.projetoextensao.edurh.repository.DisciplinaRepository;
import com.projetoextensao.edurh.repository.ProfessorRepository;
import com.projetoextensao.edurh.repository.TurmaRepository;
import com.projetoextensao.edurh.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Criar disciplina
    @PostMapping
    public ResponseEntity<Disciplina> criarDisciplina(@RequestBody Disciplina disciplina) {
        Usuario dono = getUsuarioLogado();
        disciplina.setDono(dono);
        return ResponseEntity.ok(disciplinaRepository.save(disciplina));
    }

    // Listar todas disciplinas
    @GetMapping
    public List<Disciplina> listarDisciplinas() {
        Usuario dono = getUsuarioLogado();
        return disciplinaRepository.findByDono(dono);
    }

    // Buscar disciplina por ID
    @GetMapping("/{id}")
    public ResponseEntity<Disciplina> buscarDisciplina(@PathVariable Long id) {
        Usuario dono = getUsuarioLogado();

        return disciplinaRepository.findById(id)
                .filter(d -> d.getDono() != null && d.getDono().getId().equals(dono.getId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(403).build());
    }

    // Atualizar disciplina
    @PutMapping("/{id}")
    public ResponseEntity<Disciplina> atualizarDisciplina(@PathVariable Long id, @RequestBody Disciplina dados) {
        Usuario dono = getUsuarioLogado();

        return disciplinaRepository.findById(id)
                .filter(d -> d.getDono() != null && d.getDono().getId().equals(dono.getId()))
                .map(disciplina -> {
                    disciplina.setNome(dados.getNome());
                    disciplina.setCargaHoraria(dados.getCargaHoraria());
                    return ResponseEntity.ok(disciplinaRepository.save(disciplina));
                })
                .orElse(ResponseEntity.status(403).build());
    }

    // Deletar disciplina
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarDisciplina(@PathVariable Long id) {
        Usuario dono = getUsuarioLogado();

        Optional<Disciplina> opt = disciplinaRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Disciplina disciplina = opt.get();

        if (!disciplina.getDono().getId().equals(dono.getId())) {
            return ResponseEntity.status(403).build();
        }

        disciplinaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Vincular disciplina a turma
    @PostMapping("/{disciplinaId}/turmas/{turmaId}")
    public ResponseEntity<?> adicionarTurmaADisciplina(@PathVariable Long disciplinaId, @PathVariable Long turmaId) {
        Usuario dono = getUsuarioLogado();

        Optional<Disciplina> discOpt = disciplinaRepository.findById(disciplinaId);
        Optional<Turma> turmaOpt = turmaRepository.findById(turmaId);

        if (discOpt.isEmpty() || turmaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Disciplina disciplina = discOpt.get();
        Turma turma = turmaOpt.get();

        // Verificação multi-tenant
        if (!disciplina.getDono().getId().equals(dono.getId()) ||
            !turma.getDono().getId().equals(dono.getId())) {
            return ResponseEntity.status(403).body("Operação não permitida.");
        }

        disciplina.getTurmas().add(turma);
        turma.getDisciplinas().add(disciplina);

        disciplinaRepository.save(disciplina);
        turmaRepository.save(turma);

        return ResponseEntity.ok("Disciplina vinculada à turma!");
    }

    // Vincular professor a disciplina
    @PostMapping("/{disciplinaId}/professores/{professorId}")
    public ResponseEntity<?> adicionarProfessorADisciplina(@PathVariable Long disciplinaId, @PathVariable Long professorId) {
        Usuario dono = getUsuarioLogado();

        Optional<Disciplina> discOpt = disciplinaRepository.findById(disciplinaId);
        Optional<Professor> profOpt = professorRepository.findById(professorId);

        if (discOpt.isEmpty() || profOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Disciplina disciplina = discOpt.get();
        Professor professor = profOpt.get();

        // Verificação multi-tenant
        if (!disciplina.getDono().getId().equals(dono.getId()) ||
            !professor.getDono().getId().equals(dono.getId())) {
            return ResponseEntity.status(403).body("Operação não permitida.");
        }

        disciplina.getProfessores().add(professor);
        professor.getDisciplinas().add(disciplina);

        disciplinaRepository.save(disciplina);
        professorRepository.save(professor);

        return ResponseEntity.ok("Professor vinculado à disciplina!");
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
    