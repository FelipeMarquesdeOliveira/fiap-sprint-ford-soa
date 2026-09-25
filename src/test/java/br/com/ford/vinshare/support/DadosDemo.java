package br.com.ford.vinshare.support;

/** Identificadores da carga de demonstração (src/main/resources/db/seed/V4__inserir-dados-demonstracao.sql). */
public final class DadosDemo {

    public static final String ADMIN = "admin@vinshare.test";
    public static final String SENHA_ADMIN = "Admin@123";
    public static final String ANALISTA = "analista@vinshare.test";
    public static final String SENHA_ANALISTA = "Analista@123";
    public static final String CONCESSIONARIA_SP = "concessionaria.sp@vinshare.test";
    public static final String CONCESSIONARIA_RJ = "concessionaria.rj@vinshare.test";
    public static final String SENHA_CONCESSIONARIA = "Conc@1234";

    public static final long ID_ADMIN = 1;
    public static final long ID_ANALISTA = 2;
    public static final long ID_USUARIO_SP = 3;
    public static final long ID_USUARIO_RJ = 4;

    /** Concessionárias: 1 Paulista (SP), 2 Carioca (RJ), 3 Mineira (MG), 4 Gaúcha (RS). */
    public static final long CONCESSIONARIA_PAULISTA = 1;
    public static final long CONCESSIONARIA_CARIOCA = 2;
    public static final long CONCESSIONARIA_MINEIRA = 3;
    public static final long CONCESSIONARIA_GAUCHA = 4;

    /** Clientes da Paulista (1, 2, 3), Carioca (4, 5, 6), Mineira (7, 8) e Gaúcha (9, 10). */
    public static final long CLIENTE_ANA_SP = 1;
    public static final long CLIENTE_CARLA_SP = 3;
    public static final long CLIENTE_DIEGO_RJ = 4;

    /** Veículo i pertence ao cliente i, exceto: cliente 1 -> veículo 1, cliente 2 -> 3, cliente 3 -> 4, cliente 4 -> 2. */
    public static final long VEICULO_ANA = 1;
    public static final long VEICULO_CARLA = 4;
    public static final long VEICULO_DIEGO = 2;

    /** Serviços: 1-5 Paulista (CONCLUIDO), 6-7 Carioca (CONCLUIDO), 8 Carioca (AGENDADO), 15 Gaúcha (CANCELADO). */
    public static final long SERVICO_CONCLUIDO_SP = 1;
    public static final long SERVICO_AGENDADO_RJ = 8;
    public static final long SERVICO_CANCELADO_RS = 15;

    public static final String CNPJ_PAULISTA = "11.222.333/0001-81";
    public static final String CNPJ_NOVO = "55.666.777/0001-81";
    public static final String CPF_ANA = "529.982.247-25";
    public static final String CPF_NOVO = "173.205.080-52";
    public static final String VIN_ANA = "9BFZH55L8R8000001";
    public static final String VIN_NOVO = "9BFZH55L8S8000011";

    private DadosDemo() {
    }
}
