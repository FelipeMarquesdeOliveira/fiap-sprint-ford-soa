package br.com.ford.vinshare.domain.vinshare;

import br.com.ford.vinshare.domain.cliente.Cliente;
import br.com.ford.vinshare.domain.cliente.PerfilCliente;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Cliente com risco de evasão da rede oficial (0 ou 1 serviço concluído). CPF mascarado (LGPD).")
public record ClienteRiscoResponse(
        Long id,
        String nome,
        @Schema(example = "529.***.***-25") String cpfMascarado,
        PerfilCliente perfilCliente,
        String regiao,
        Long concessionariaId,
        String concessionariaNome,
        String modeloVeiculo,
        String vin,
        long servicosConcluidos,
        @Schema(description = "ALTO: nenhum serviço concluído na rede; MEDIO: apenas um") NivelRisco nivelRisco
) {
    public enum NivelRisco { ALTO, MEDIO }

    public static ClienteRiscoResponse de(Cliente cliente, long servicosConcluidos) {
        return new ClienteRiscoResponse(
                cliente.getId(),
                cliente.getNome(),
                mascararCpf(cliente.getCpf()),
                cliente.getPerfilCliente(),
                cliente.getRegiao(),
                cliente.getConcessionaria().getId(),
                cliente.getConcessionaria().getNome(),
                cliente.getVeiculo().getModelo(),
                cliente.getVeiculo().getVin(),
                servicosConcluidos,
                servicosConcluidos == 0 ? NivelRisco.ALTO : NivelRisco.MEDIO
        );
    }

    static String mascararCpf(String cpf) {
        if (cpf == null || cpf.length() < 14) {
            return "***";
        }
        return cpf.substring(0, 4) + "***.***" + cpf.substring(11);
    }
}
