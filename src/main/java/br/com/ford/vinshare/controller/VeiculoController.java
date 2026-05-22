package br.com.ford.vinshare.controller;

import br.com.ford.vinshare.domain.veiculo.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/veiculos")
public class VeiculoController {

    @Autowired
    private VeiculoRepository repository;

    @PostMapping
    @Transactional
    public ResponseEntity<DadosDetalhamentoVeiculo> cadastrarVeiculo(
            @RequestBody @Valid DadosCadastroVeiculo dados,
            UriComponentsBuilder uriBuilder) {
        var veiculo = dados.toEntity();
        repository.save(veiculo);
        var uri = uriBuilder.path("/veiculos/{id}").buildAndExpand(veiculo.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhamentoVeiculo(veiculo));
    }

    @GetMapping
    public ResponseEntity<Page<DadosListagemVeiculo>> listarVeiculos(Pageable paginacao) {
        var page = repository.findAll(paginacao).map(DadosListagemVeiculo::new);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoVeiculo> listarVeiculoPorId(@PathVariable Long id) {
        var veiculo = repository.findById(id)
                .orElseThrow(() -> new VeiculoNotFoundException("Veículo não encontrado"));
        return ResponseEntity.ok(new DadosDetalhamentoVeiculo(veiculo));
    }

    @PutMapping
    @Transactional
    public ResponseEntity<DadosDetalhamentoVeiculo> atualizarVeiculo(
            @RequestBody @Valid DadosAtualizacaoVeiculo dados) {
        var veiculo = repository.findById(dados.id())
                .orElseThrow(() -> new VeiculoNotFoundException("Veículo não encontrado"));
        dados.atualizarInformacoes(veiculo);
        return ResponseEntity.ok(new DadosDetalhamentoVeiculo(veiculo));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void excluirVeiculo(@PathVariable Long id) {
        repository.findById(id)
                .orElseThrow(() -> new VeiculoNotFoundException("Veículo não encontrado"));
        repository.deleteById(id);
    }
}