package br.com.ford.vinshare.controller;

import br.com.ford.vinshare.domain.cliente.*;
import br.com.ford.vinshare.domain.concessionaria.ConcessionariaRepository;
import br.com.ford.vinshare.domain.veiculo.VeiculoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private ClienteRepository repository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private ConcessionariaRepository concessionariaRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<DadosDetalhamentoCliente> cadastrarCliente(
            @RequestBody @Valid DadosCadastroCliente dados,
            UriComponentsBuilder uriBuilder) {
        var cliente = new Cliente();
        cliente.setCpf(dados.cpf());
        cliente.setNome(dados.nome());
        cliente.setEmail(dados.email());
        cliente.setTelefone(dados.telefone());
        cliente.setIdade(dados.idade());
        cliente.setSexo(dados.sexo());
        cliente.setRegiao(dados.regiao());
        cliente.setDataCompra(dados.dataCompra());
        cliente.setPerfilCliente(dados.perfilCliente());
        cliente.setAtivo(true);

        if (dados.veiculoId() != null) {
            veiculoRepository.findById(dados.veiculoId())
                    .ifPresent(cliente::setVeiculo);
        }
        if (dados.concessionariaId() != null) {
            concessionariaRepository.findById(dados.concessionariaId())
                    .ifPresent(cliente::setConcessionaria);
        }

        repository.save(cliente);
        var uri = uriBuilder.path("/clientes/{id}").buildAndExpand(cliente.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhamentoCliente(cliente));
    }

    @GetMapping
    public ResponseEntity<Page<DadosListagemCliente>> listarClientes(Pageable paginacao) {
        var page = repository.findAllByAtivoTrue(paginacao).map(DadosListagemCliente::new);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoCliente> listarClientePorId(@PathVariable Long id) {
        var cliente = repository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente não encontrado"));
        return ResponseEntity.ok(new DadosDetalhamentoCliente(cliente));
    }

    @PutMapping
    @Transactional
    public ResponseEntity<DadosDetalhamentoCliente> atualizarCliente(
            @RequestBody @Valid DadosAtualizacaoCliente dados) {
        var cliente = repository.findById(dados.id())
                .orElseThrow(() -> new ClienteNotFoundException("Cliente não encontrado"));
        dados.atualizarInformacoes(cliente);
        return ResponseEntity.ok(new DadosDetalhamentoCliente(cliente));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void excluirCliente(@PathVariable Long id) {
        var cliente = repository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente não encontrado"));
        cliente.excluir();
    }
}