package br.com.ford.vinshare.controller;

import br.com.ford.vinshare.domain.concessionaria.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/concessionarias")
public class ConcessionariaController {

    @Autowired
    private ConcessionariaRepository repository;

    @PostMapping
    @Transactional
    public ResponseEntity<DadosDetalhamentoConcessionaria> cadastrarConcessionaria(
            @RequestBody @Valid DadosCadastroConcessionaria dados,
            UriComponentsBuilder uriBuilder) {
        var concessionaria = dados.toEntity();
        repository.save(concessionaria);
        var uri = uriBuilder.path("/concessionarias/{id}").buildAndExpand(concessionaria.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhamentoConcessionaria(concessionaria));
    }

    @GetMapping
    public ResponseEntity<Page<DadosListagemConcessionaria>> listarConcessionarias(Pageable paginacao) {
        var page = repository.findAllByAtivoTrue(paginacao).map(DadosListagemConcessionaria::new);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoConcessionaria> listarConcessionariaPorId(@PathVariable Long id) {
        var concessionaria = repository.findById(id)
                .orElseThrow(() -> new ConcessionariaNotFoundException("Concessionária não encontrada"));
        return ResponseEntity.ok(new DadosDetalhamentoConcessionaria(concessionaria));
    }

    @PutMapping
    @Transactional
    public ResponseEntity<DadosDetalhamentoConcessionaria> atualizarConcessionaria(
            @RequestBody @Valid DadosAtualizacaoConcessionaria dados) {
        var concessionaria = repository.findById(dados.id())
                .orElseThrow(() -> new ConcessionariaNotFoundException("Concessionária não encontrada"));
        dados.atualizarInformacoes(concessionaria);
        return ResponseEntity.ok(new DadosDetalhamentoConcessionaria(concessionaria));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void excluirConcessionaria(@PathVariable Long id) {
        var concessionaria = repository.findById(id)
                .orElseThrow(() -> new ConcessionariaNotFoundException("Concessionária não encontrada"));
        concessionaria.excluir();
    }
}