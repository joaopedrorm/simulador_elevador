package jprm.simulador_elevadores;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Simulador {

	private static final Logger logger = LoggerFactory.getLogger(Simulador.class);

	/**
	 * Parâmetros da simulação
	 */
	private LocalDateTime instanteInicial;
	private LocalDateTime instanteAtual;
	private LocalDateTime instanteFinal;
	private List<Pessoa> listaPessoas;
	private List<Elevador> listaElevadores;
	private ElevadorControle elevadorControle;

	private Integer quantidadeElevadores;
	private Duration periodoParadaElevador;
	private Duration periodoEntreAndaresElevador;
	private Integer lotacaoMaximaElevador;
	private Integer andarMinimoElevador;
	private Integer andarMaximoElevador;
	private Integer andarInicialElevador;

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

	// andar inicial = 1 andar
	private static final Integer andarInicialElevadorDefault = 1;

	public void inicializar(List<Pessoa> pessoas) {
		if (pessoas.isEmpty()) {
			throw new RuntimeException("A lista de pessoas para simulação está vazia");
		}

		this.listaPessoas = pessoas;

		Optional<LocalDateTime> instanteInicialOpt = pessoas.stream().filter(Objects::nonNull)
				.map(Pessoa::getInstanteChegada).min(LocalDateTime::compareTo);

		// inicializar instanteInicial
		this.instanteInicial = instanteInicialOpt
				.orElseThrow(() -> new RuntimeException("Não foi possível obter o instante inicial para a simulação"));

		// LocalDateTime é imutável
		this.instanteAtual = LocalDateTime.from(this.instanteInicial);

		// incializar elevadores
		this.listaElevadores = new ArrayList<Elevador>(this.quantidadeElevadores);
		for (int i = 0; i < this.quantidadeElevadores; i++) {
			Elevador e = new Elevador(this.lotacaoMaximaElevador);
			e.setAndarAtual(this.andarInicialElevador);
			e.setAndarMaximo(this.andarMaximoElevador);
			e.setAndarMinimo(this.andarMinimoElevador);
			e.setIdentificacao(i + 1);
			e.setMarcadorTemporal(this.instanteAtual);
			e.setPeriodoEntreAndares(this.periodoEntreAndaresElevador);
			e.setPeriodoParada(this.periodoParadaElevador);
			e.setStatus(ElevadorStatus.ESPERA_TERREO);
			this.listaElevadores.add(e);
		}

		this.elevadorControle.inicializar(this.listaElevadores);
	}

	public void incrementarInstanteAtual() {
		this.instanteAtual = this.instanteAtual.plusSeconds(passoSimulacaoSegundosDefault);
	}

	public SimulacaoStatus processarInstante() {
		if (this.instanteAtual == null) {
			throw new RuntimeException("O instante atual é nulo");
		}
		if (this.instanteFinal != null && this.instanteAtual.isAfter(instanteFinal)) {
			throw new RuntimeException("Instante atual é posterior ao final da simulação");
		}

		// atualizar posição elevadores
		this.listaElevadores.forEach(e -> e.atualizar(this.instanteAtual));

		// se o elevador estiver parado, verifica se há pessoas para desembarque
		for (Elevador e : this.listaElevadores) {
			if (e.getStatus() == ElevadorStatus.PARADO_SUBIR || e.getStatus() == ElevadorStatus.PARADO_DESCER) {
				List<Pessoa> lotacao = e.getLotacao().stream().filter(p -> p.getAndar() == e.getAndarAtual())
						.collect(Collectors.toList());
				e.getLotacao().removeAll(lotacao);
				lotacao.forEach(p -> {
					p.setInstanteDesembarque(this.instanteAtual);
					registrarDesembarque(p, e);
				});
			}
		}

		// pegar pessoas para processamento (instante atual = instante de
		// chegada)
		List<Pessoa> pessoasChegaram = this.listaPessoas.stream()
				.filter(p -> p.getInstanteChegada().isEqual(this.instanteAtual)).collect(Collectors.toList());

		// para cada pessoa para o controlador decidirá qual elevador pegar, e
		// adiciona a pessoa no final da lista da fila do terreo
		pessoasChegaram.forEach(p -> this.elevadorControle.decisao(p, instanteAtual).getFilaTerreo().add(p));

		// se o elevador estiver em espera no andar minimo, verifica se há
		// pessoas para embarque, se o elevador estiver no andar minimo mas o
		// status for parado_subindo considera-se que o elevador não está mais
		// disponível para embarque de pessoas na fila que acabaram de chegar
		this.listaElevadores.stream().filter(e -> e.getStatus() == ElevadorStatus.ESPERA_TERREO).forEach(e -> {
			List<Pessoa> pessoasFila = e.getFilaTerreo();
			if (!pessoasFila.isEmpty()) {
				e.setStatus(ElevadorStatus.PARADO_SUBIR);
			}
			List<Pessoa> lotacao = e.getLotacao();
			// usando iterator para evitar problemas de concorrencia na chamada
			// array.remove(obj)
			Iterator<Pessoa> it = pessoasFila.iterator();
			while (it.hasNext()) {
				Pessoa p = it.next();
				if (lotacao.size() < this.getLotacaoMaximaElevador()) {
					lotacao.add(p);
					it.remove();
					p.setInstanteEmbarque(instanteAtual);
				}
			}
		});

		// condição: se lista de pessoas que ainda não desembarcaram estiver vazia,
		// finalizar simulação
		if (this.listaPessoas.stream().filter(p -> p.getInstanteDesembarque() == null).count() == 0) {
			this.instanteFinal = LocalDateTime.from(this.instanteAtual);
			return SimulacaoStatus.FINALIZADA;
		} else {
			return SimulacaoStatus.PROCESSANDO;
		}
	}

	public void registrarDesembarque(Pessoa p, Elevador e) {
		logger.info(String.format("%n %n --> Desembarque: Elevador %d %s %n --> Elevadores: %s %n",
				e.getIdentificacao(), p, imprimeElevadores()));
	}

	private String imprimeElevadores() {
		String retorno = "";
		for (Elevador e : this.listaElevadores) {
			String lotacao = e.getLotacao().size()
					+ e.getLotacao().stream().map(p -> String.format("%s -> %d andar", p.getNome(), p.getAndar()))
							.collect(Collectors.joining(",", "[", "]"));
			retorno += String.format("%n Elevador %d: andarAtual=%d, status=%s, lotacao=%s", e.getIdentificacao(),
					e.getAndarAtual(), e.getStatus(), lotacao);
		}
		return retorno;
	}

	public void imprimeEstatisticasSimulacao() {
		// calcula tempo médio de espera na fila
		List<Duration> listaTempoEsperaFila = obterListaTempo(p -> p.calculaTempoEsperaFila());

		Optional<Duration> maiorTempoEsperaFilaOpt = listaTempoEsperaFila.stream().max(Duration::compareTo);

		Optional<Duration> tempoMedioEsperaFilaOpt = calculaTempoMedio(listaTempoEsperaFila);

		// verifica pessoas que não embarcaram
		String pessoasNaoEmbarcaram = String.format("Quantidade de pessoas que não embarcaram=%d",
				this.listaPessoas.size() - listaTempoEsperaFila.size());

		// calcula tempo médio do embarque até desembarque (Chegada até o andar)
		List<Duration> listaTempoChegadaAndar = obterListaTempo(p -> p.calculaTempoChegadaAndar());

		Optional<Duration> maiorTempoCheagadaAndarOpt = listaTempoChegadaAndar.stream().max(Duration::compareTo);

		Optional<Duration> tempoMedioChegadaAndarOpt = calculaTempoMedio(listaTempoChegadaAndar);

		// verifica pessoas que não desembarcaram
		String pessoasNaoDesembarcaram = String.format("Quantidade de pessoas que não embarcaram=%d",
				this.listaPessoas.size() - listaTempoChegadaAndar.size());

		// calcula tempo médio total de percurso
		List<Duration> listaTempoTotalPercurso = obterListaTempo(p -> p.calculaTempoTotalPercurso());

		Optional<Duration> maiorTempoTotalPercursoOpt = listaTempoTotalPercurso.stream().max(Duration::compareTo);

		Optional<Duration> tempoMedioTotalPercursoOpt = calculaTempoMedio(listaTempoTotalPercurso);

		// Mensagem
		String mensagem = "%n %n Estatísticas da Simulação: %s %n Tempo médio de espera na fila = %s "
				+ "%n Tempo médio entre embarque e desembarque do elevador = %s "
				+ "%n Tempo médio total de percurso = %s " + "%n Maior Tempo de espera = %s "
				+ "%n Maior Tempo entre Embarque e Desembarque = %s " + "%n Maior tempo total de percurso = %s "
				+ "%n %s %n %s %n";

		logger.info(String.format(mensagem, this.elevadorControle.getNome(), tempoMedioEsperaFilaOpt,
				tempoMedioChegadaAndarOpt, tempoMedioTotalPercursoOpt, maiorTempoEsperaFilaOpt,
				maiorTempoCheagadaAndarOpt, maiorTempoTotalPercursoOpt, pessoasNaoEmbarcaram, pessoasNaoDesembarcaram));

	}

	private Optional<Duration> calculaTempoMedio(List<Duration> l) {
		return l.stream().reduce(Duration::plus).map(d -> d.dividedBy(l.size()));
	}

	private List<Duration> obterListaTempo(Function<? super Pessoa, ? extends Optional<Duration>> mapper) {
		return this.listaPessoas.stream().map(mapper).filter(Optional::isPresent).map(Optional::get)
				.collect(Collectors.toList());
	}

	public Simulador(ElevadorControle elevadorControle) {
		this.quantidadeElevadores = quantidadeElevadoresDefault;
		this.periodoParadaElevador = periodoParadaElevadorDefault;
		this.periodoEntreAndaresElevador = periodoEntreAndaresElevadorDefault;
		this.lotacaoMaximaElevador = lotacaoMaximaElevadorDefault;
		this.andarMinimoElevador = andarMinimoElevadorDefault;
		this.andarMaximoElevador = andarMaximoElevadorDefault;
		this.andarInicialElevador = andarInicialElevadorDefault;
		this.elevadorControle = elevadorControle;
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

	public List<Pessoa> getListaPessoas() {
		return listaPessoas;
	}

	public void setListaPessoas(List<Pessoa> listaPessoas) {
		this.listaPessoas = listaPessoas;
	}

	public List<Elevador> getListaElevadores() {
		return listaElevadores;
	}

	public void setListaElevadores(List<Elevador> listaElevadores) {
		this.listaElevadores = listaElevadores;
	}

	public ElevadorControle getElevadorControle() {
		return elevadorControle;
	}

	public void setElevadorControle(ElevadorControle elevadorControle) {
		this.elevadorControle = elevadorControle;
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

	public Integer getAndarInicialElevador() {
		return andarInicialElevador;
	}

	public void setAndarInicialElevador(Integer andarInicialElevador) {
		this.andarInicialElevador = andarInicialElevador;
	}

	@Override
	public String toString() {
		return "\n Simulador [instanteInicial=" + instanteInicial + ", instanteAtual=" + instanteAtual
				+ ", instanteFinal=" + instanteFinal + "]";
	}

}
