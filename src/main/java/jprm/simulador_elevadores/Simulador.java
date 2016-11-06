package jprm.simulador_elevadores;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Simulador {

	/**
	 * Parâmetros da simulação
	 */
	private LocalDateTime instanteInicial;
	private LocalDateTime instanteAtual;
	private LocalDateTime instanteFinal;
	private List<Pessoa> listaPessoasRestantes;
	private List<Elevador> listaElevadores;

	private Integer quantidadeElevadores;
	private Duration periodoParadaElevador;
	private Duration periodoEntreAndaresElevador;
	private Integer lotacaoMaximaElevador;
	private Integer andarMinimoElevador;
	private Integer andarMaximoElevador;

	// assumindo 1s como maior unidade de tempo sem perda de eventos
	private static final Long passoSimulacaoSegundosDefault = 1l;

	/**
	 * Segundo enunciado:
	 */

	// quantidade de elevadores = 4
	private static final Integer quantidadeElevadoresDefault = 4;

	// tempo parada do elevador = 20s
	private static final Duration periodoParadaElevadorDefault = Duration.ofSeconds(20l);

	// velocidade o elevador 0.5 andar por segundo, ou 1 andar a cada 2s
	private static final Duration periodoEntreAndaresElevadorDefault = Duration.ofSeconds(2l);

	// lotação máxima elevador
	private static final Integer lotacaoMaximaElevadorDefault = 8;

	// andar mínimo = andar térreo = 1 andar
	private static final Integer andarMinimoElevadorDefault = 1;

	// andar máximo = 25 andar
	private static final Integer andarMaximoElevadorDefault = 25;

	public void inicializar(List<Pessoa> pessoas) {
		if (pessoas.isEmpty()) {
			throw new RuntimeException("A lista de pessoas para simulação está vazia");
		}

		this.listaPessoasRestantes = pessoas;

		Optional<LocalDateTime> instanteInicialOpt = pessoas.stream().filter(Objects::nonNull)
				.map(Pessoa::getInstanteChegada).min(LocalDateTime::compareTo);

		// inicializar instanteInicial
		this.instanteInicial = instanteInicialOpt
				.orElseThrow(() -> new RuntimeException("Não foi possível obter o instante inicial para a simulação"));

		// LocalDateTime é imutável
		this.instanteAtual = this.instanteInicial;

		// incializar elevadores
		this.listaElevadores = new ArrayList<Elevador>(this.quantidadeElevadores);
		for (int i = 0; i < this.quantidadeElevadores; i++) {
			Elevador e = new Elevador(this.lotacaoMaximaElevador);
			e.setAndarAtual(this.andarMinimoElevador);
			e.setAndarMaximo(this.andarMaximoElevador);
			e.setAndarMinimo(this.andarMinimoElevador);
			e.setIdentificacao(i + 1);
			e.setMarcadorTemporal(this.instanteAtual);
			e.setStatus(ElevadorStatus.ESPERA_TERREO);
			this.listaElevadores.add(e);
		}
	}

	public void incrementarInstanteAtual() {
		this.instanteAtual = this.instanteAtual.plusSeconds(passoSimulacaoSegundosDefault);
	}

	public void processarInstante() {

	}

	public Simulador() {
		this.quantidadeElevadores = quantidadeElevadoresDefault;
		this.periodoParadaElevador = periodoParadaElevadorDefault;
		this.periodoEntreAndaresElevador = periodoEntreAndaresElevadorDefault;
		this.lotacaoMaximaElevador = lotacaoMaximaElevadorDefault;
		this.andarMinimoElevador = andarMinimoElevadorDefault;
		this.andarMaximoElevador = andarMaximoElevadorDefault;
	}

	public LocalDateTime getInstanteInicial() {
		return instanteInicial;
	}

	public void setInstanteInicial(LocalDateTime instanteInicial) {
		this.instanteInicial = instanteInicial;
	}

	public LocalDateTime getInstanteAtual() {
		return instanteAtual;
	}

	public void setInstanteAtual(LocalDateTime instanteAtual) {
		this.instanteAtual = instanteAtual;
	}

	public LocalDateTime getInstanteFinal() {
		return instanteFinal;
	}

	public void setInstanteFinal(LocalDateTime instanteFinal) {
		this.instanteFinal = instanteFinal;
	}

	public List<Pessoa> getListaPessoasRestantes() {
		return listaPessoasRestantes;
	}

	public void setListaPessoasRestantes(List<Pessoa> listaPessoasRestantes) {
		this.listaPessoasRestantes = listaPessoasRestantes;
	}

	public List<Elevador> getListaElevadores() {
		return listaElevadores;
	}

	public void setListaElevadores(List<Elevador> listaElevadores) {
		this.listaElevadores = listaElevadores;
	}

	public Integer getQuantidadeElevadores() {
		return quantidadeElevadores;
	}

	public void setQuantidadeElevadores(Integer quantidadeElevadores) {
		this.quantidadeElevadores = quantidadeElevadores;
	}

	public Duration getPeriodoParadaElevador() {
		return periodoParadaElevador;
	}

	public void setPeriodoParadaElevador(Duration periodoParadaElevador) {
		this.periodoParadaElevador = periodoParadaElevador;
	}

	public Duration getPeriodoEntreAndaresElevador() {
		return periodoEntreAndaresElevador;
	}

	public void setPeriodoEntreAndaresElevador(Duration periodoEntreAndaresElevador) {
		this.periodoEntreAndaresElevador = periodoEntreAndaresElevador;
	}

	public Integer getLotacaoMaximaElevador() {
		return lotacaoMaximaElevador;
	}

	public void setLotacaoMaximaElevador(Integer lotacaoMaximaElevador) {
		this.lotacaoMaximaElevador = lotacaoMaximaElevador;
	}

	public Integer getAndarMinimoElevador() {
		return andarMinimoElevador;
	}

	public void setAndarMinimoElevador(Integer andarMinimoElevador) {
		this.andarMinimoElevador = andarMinimoElevador;
	}

	public Integer getAndarMaximoElevador() {
		return andarMaximoElevador;
	}

	public void setAndarMaximoElevador(Integer andarMaximoElevador) {
		this.andarMaximoElevador = andarMaximoElevador;
	}

	@Override
	public String toString() {
		return "\n Simulador [instanteInicial=" + instanteInicial + ", instanteAtual=" + instanteAtual
				+ ", instanteFinal=" + instanteFinal + "]";
	}

}
