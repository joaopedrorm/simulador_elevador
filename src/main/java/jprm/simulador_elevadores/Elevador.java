package jprm.simulador_elevadores;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

public class Elevador {

	// private static final Logger logger =
	// LoggerFactory.getLogger(Elevador.class);

	private Integer identificacao;
	private Integer andarAtual;
	private Integer andarMinimo;
	private Integer andarMaximo;
	private List<Pessoa> lotacao;
	private Integer lotacaoMaxima;
	private List<Pessoa> filaTerreo;
	private ElevadorStatus status;
	private LocalDateTime marcadorTemporal;
	private Duration periodoEntreAndares;
	private Duration periodoParada;

	/**
	 * Atualiza posição e status do elevador
	 * 
	 * @param instanteAtual
	 */
	public void atualizar(LocalDateTime instanteAtual) {
		switch (this.status) {
		case PARADO_SUBIR:
			if (periodoFinalizado(instanteAtual, this.periodoParada)) {
				this.status = ElevadorStatus.SUBINDO;
			}
			break;

		case PARADO_DESCER:
			if (periodoFinalizado(instanteAtual, this.periodoParada)) {
				this.status = ElevadorStatus.DESCENDO;
			}
			break;

		case SUBINDO:
			if (periodoFinalizado(instanteAtual, this.periodoEntreAndares)) {
				this.andarAtual += 1;
				if (this.andarAtual > this.andarMaximo) {
					throw new RuntimeException("O andar atual (" + this.andarAtual + ") é maior que o andar máximo ("
							+ this.andarMaximo + ")");
				}
				atualizarStatusSubindo();
			}
			break;

		case DESCENDO:
			if (periodoFinalizado(instanteAtual, this.periodoEntreAndares)) {
				this.andarAtual -= 1;
				if (this.andarAtual < this.andarMinimo) {
					throw new RuntimeException("O andar atual (" + this.andarAtual + ") é menor que o andar mínimo ("
							+ this.andarMinimo + ")");
				}
				atualizarStatusDescendo();
			}
			break;

		case ESPERA_TERREO:
		default:
			// nenhuma ação a ser tomada
			break;
		}
	}

	/**
	 * retorna true se o periodo calculado é maior ou igual ao periodo
	 * pre-definido, atualizando o marcador temporal para o instante atual se
	 * true
	 * 
	 * @param instanteAtual
	 * @param periodoComparacao
	 * @return
	 */
	private Boolean periodoFinalizado(LocalDateTime instanteAtual, Duration periodoComparacao) {
		Boolean finalizado = Duration.between(this.marcadorTemporal, instanteAtual).compareTo(periodoComparacao) >= 0;
		if (finalizado) {
			this.marcadorTemporal = LocalDateTime.from(instanteAtual);
		}
		return finalizado;
	}

	private void atualizarStatusSubindo() {
		List<Integer> paradas = getParadasLotacao();
		if (paradas.contains(this.andarAtual)) {
			// o andar atual é uma parada programada
			if (paradas.stream().distinct().count() == 1) {
				// o andar atual é a última parada
				this.status = ElevadorStatus.PARADO_DESCER;
			} else {
				this.status = ElevadorStatus.PARADO_SUBIR;
			}
		}
	}

	private void atualizarStatusDescendo() {
		// considerando que o elevador desce vazio para o andarMinimo
		if (this.andarAtual == this.andarMinimo) {
			this.status = ElevadorStatus.ESPERA_TERREO;
		}
	}

	/**
	 * Calcula o tempo restante para o elevador chegar ao Andar Mínimo
	 * 
	 * @param instanteAtual
	 * @return
	 */
	public Duration calcularTempoRestanteTerreo(LocalDateTime instanteAtual) {
		if (status == ElevadorStatus.DESCENDO || status == ElevadorStatus.PARADO_DESCER) {
			return calcularTempoRestanteTerreoDescendo(instanteAtual);
		} else if (status == ElevadorStatus.SUBINDO || status == ElevadorStatus.PARADO_SUBIR) {
			return calcularTempoRestanteUltimoAndarSubindo(instanteAtual).plus(calcularTempoDescida());
		} else {
			return Duration.ZERO;
		}
	}

	/**
	 * Calcula o tempo de subida do elevador até o último andar programado
	 * 
	 * @param instanteAtual
	 * @return
	 */
	private Duration calcularTempoRestanteUltimoAndarSubindo(LocalDateTime instanteAtual) {
		// pegar lista de paradas, removendo andar atual se estiver presente
		List<Integer> paradas = getParadasLotacao();
		paradas.remove(this.andarAtual);
		Integer ultimaParada = getUltimaParadaLotacao().orElse(andarAtual);
		Duration tempoParado = periodoParada.multipliedBy(paradas.stream().distinct().count());
		Integer multiplicador = ultimaParada - andarAtual;
		if (status == ElevadorStatus.SUBINDO) {
			multiplicador -= 1;
		}
		Duration tempoSubindo = periodoEntreAndares.multipliedBy(multiplicador);
		return tempoParado.plus(tempoSubindo).plus(calcularTempoRestanteMudancaStatusAndar(instanteAtual));
	}

	/**
	 * Calcula o tempo de descida do elevador em relação à utlima parada
	 * programada (andar atual se não houver parada programada) até o Andar
	 * Mínimo, desconsiderando o tempo para conclusão da ação atual
	 * 
	 * @return
	 */
	private Duration calcularTempoDescida() {
		if (status == ElevadorStatus.DESCENDO || status == ElevadorStatus.PARADO_DESCER) {
			throw new RuntimeException(
					"Status Incorreto, não é possível calcular o tempo de descida: Status=" + status);
		}
		Integer ultimaParada = getUltimaParadaLotacao().orElse(andarAtual);
		return periodoEntreAndares.multipliedBy(ultimaParada - andarMinimo);
	}

	/**
	 * Calcula tempo restante para elevador chegar ao terreo (andarMinimo),
	 * assumindo que não haverão paradas
	 * 
	 * @param instanteAtual
	 * @return
	 */
	private Duration calcularTempoRestanteTerreoDescendo(LocalDateTime instanteAtual) {
		if (status == ElevadorStatus.SUBINDO || status == ElevadorStatus.PARADO_SUBIR
				|| status == ElevadorStatus.ESPERA_TERREO) {
			throw new RuntimeException(
					"Status Incorreto, não é possível calcular o tempo restante de descida: Status=" + status);
		}
		Integer multiplicador = andarAtual - andarMinimo;
		if (status == ElevadorStatus.DESCENDO) {
			multiplicador -= 1;
		}
		return periodoEntreAndares.multipliedBy(multiplicador)
				.plus(calcularTempoRestanteMudancaStatusAndar(instanteAtual));
	}

	/**
	 * Calcula o tempo restante entre o instate atual e o instante em que o
	 * elevador mudará de status ou de andar
	 * 
	 * @param instanteAtual
	 * @return
	 */
	public Duration calcularTempoRestanteMudancaStatusAndar(LocalDateTime instanteAtual) {
		if (status == ElevadorStatus.SUBINDO || status == ElevadorStatus.DESCENDO) {
			return Duration.between(marcadorTemporal, instanteAtual).minus(periodoEntreAndares);
		} else if (status == ElevadorStatus.PARADO_SUBIR || status == ElevadorStatus.PARADO_DESCER) {
			return Duration.between(marcadorTemporal, instanteAtual).minus(periodoParada);
		} else {
			return Duration.ZERO;
		}
	}

	/**
	 * retorna uma lista com paradas programadas, com repetição
	 * 
	 * @return
	 */
	public List<Integer> getParadasLotacao() {
		return lotacao.stream().filter(Objects::nonNull).map(p -> p.getAndar())
				.filter(a -> a >= this.andarAtual && a <= this.andarMaximo).collect(Collectors.toList());
	}

	/**
	 * retorna a ultima parada programada
	 * 
	 * @return
	 */
	public Optional<Integer> getUltimaParadaLotacao() {
		return lotacao.stream().filter(Objects::nonNull).map(Pessoa::getAndar).max(Integer::compare);
	}

	/**
	 * retorna a ultima parada escolhida mas ainda não ativa (pessoas na fila do
	 * elevador)
	 * 
	 * @return
	 */
	public Optional<Integer> getUltimaParadaFilaTerreo() {
		return filaTerreo.stream().filter(Objects::nonNull).map(Pessoa::getAndar).max(Integer::compare);
	}

	/**
	 * retorna a lista de paradas ecolhidas pelas pessoas da fila do elevador
	 * mas ainda não ativas, com repetição
	 * 
	 * @return
	 */
	public List<Integer> getParadasFilaTerreo() {
		return filaTerreo.stream().filter(Objects::nonNull).map(p -> p.getAndar()).filter(a -> a <= this.andarMaximo)
				.collect(Collectors.toList());
	}

	public Duration simularTempoFilaEspera(Pessoa p) {
		List<Integer> listaParadas = getParadasFilaTerreo();
		listaParadas.add(p.getAndar());
		
		List<Duration> temposEspera = new ArrayList<>();
		for (List<Integer> particao : Lists.partition(listaParadas, this.lotacaoMaxima)) {
			temposEspera.add((simularTempoSubida(particao)).plus(simularTempoDescida(particao)));
		}
		return temposEspera.stream().reduce(Duration::plus).orElse(Duration.ZERO);
	}

	private Duration simularTempoDescida(List<Integer> paradas) {
		Integer ultimaParada = paradas.stream().max(Integer::compare).get();
		Integer multiplicador = ultimaParada - this.andarMinimo;
		return periodoEntreAndares.multipliedBy(multiplicador);
	}

	private Duration simularTempoSubida(List<Integer> paradas) {
		// pegar lista de paradas, removendo andar atual se estiver presente
		// cuidado get() pode soltar exception
		Integer ultimaParada = paradas.stream().max(Integer::compare).get();
		Duration tempoParado = periodoParada.multipliedBy(paradas.stream().distinct().count());
		Integer multiplicador = ultimaParada - this.andarMinimo;
		return periodoEntreAndares.multipliedBy(multiplicador).plus(tempoParado);
	}

	/**
	 * Construtor
	 * 
	 * @param andarAtual
	 * @param andarMinimo
	 * @param andarMaximo
	 * @param lotacaoMaxima
	 * @param status
	 * @param marcadorTemporal
	 */
	public Elevador(Integer andarAtual, Integer andarMinimo, Integer andarMaximo, Integer lotacaoMaxima,
			ElevadorStatus status, LocalDateTime marcadorTemporal, Duration periodoEntreAndares,
			Duration periodoParada) {
		super();
		this.andarAtual = andarAtual;
		this.andarMinimo = andarMinimo;
		this.andarMaximo = andarMaximo;
		this.lotacaoMaxima = lotacaoMaxima;
		this.status = status;
		this.marcadorTemporal = marcadorTemporal;
		this.periodoEntreAndares = periodoEntreAndares;
		this.periodoParada = periodoParada;
		this.lotacao = new ArrayList<>(this.lotacaoMaxima);
		this.filaTerreo = new LinkedList<>();
	}

	/**
	 * Contrutor Minimo
	 * 
	 * @param lotacaoMaxima
	 */
	public Elevador(Integer lotacaoMaxima) {
		super();
		this.lotacaoMaxima = lotacaoMaxima;
		this.lotacao = new ArrayList<>(this.lotacaoMaxima);
		this.filaTerreo = new LinkedList<>();
	}

	/**
	 * Geters e Seters
	 * 
	 */
	public Integer getIdentificacao() {
		return identificacao;
	}

	public void setIdentificacao(Integer identificacao) {
		this.identificacao = identificacao;
	}

	public Integer getAndarAtual() {
		return andarAtual;
	}

	public void setAndarAtual(Integer andarAtual) {
		this.andarAtual = andarAtual;
	}

	public Integer getAndarMinimo() {
		return andarMinimo;
	}

	public void setAndarMinimo(Integer andarMinimo) {
		this.andarMinimo = andarMinimo;
	}

	public Integer getAndarMaximo() {
		return andarMaximo;
	}

	public void setAndarMaximo(Integer andarMaximo) {
		this.andarMaximo = andarMaximo;
	}

	public List<Pessoa> getLotacao() {
		return lotacao;
	}

	public void setLotacao(List<Pessoa> lotacao) {
		this.lotacao = lotacao;
	}

	public Integer getLotacaoMaxima() {
		return lotacaoMaxima;
	}

	public void setLotacaoMaxima(Integer lotacaoMaxima) {
		this.lotacaoMaxima = lotacaoMaxima;
	}

	public List<Pessoa> getFilaTerreo() {
		return filaTerreo;
	}

	public void setFilaTerreo(List<Pessoa> filaTerreo) {
		this.filaTerreo = filaTerreo;
	}

	public ElevadorStatus getStatus() {
		return status;
	}

	public void setStatus(ElevadorStatus status) {
		this.status = status;
	}

	public LocalDateTime getMarcadorTemporal() {
		return marcadorTemporal;
	}

	public void setMarcadorTemporal(LocalDateTime marcadorTemporal) {
		this.marcadorTemporal = marcadorTemporal;
	}

	public Duration getPeriodoEntreAndares() {
		return periodoEntreAndares;
	}

	public void setPeriodoEntreAndares(Duration periodoEntreAndares) {
		this.periodoEntreAndares = periodoEntreAndares;
	}

	public Duration getPeriodoParada() {
		return periodoParada;
	}

	public void setPeriodoParada(Duration periodoParada) {
		this.periodoParada = periodoParada;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identificacao == null) ? 0 : identificacao.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Elevador other = (Elevador) obj;
		if (identificacao == null) {
			if (other.identificacao != null)
				return false;
		} else if (!identificacao.equals(other.identificacao))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "\n Elevador [identificacao=" + identificacao + ", andarAtual=" + andarAtual + ", andarMinimo="
				+ andarMinimo + ", andarMaximo=" + andarMaximo + ", lotacao=" + lotacao + ", lotacaoMaxima="
				+ lotacaoMaxima + ", filaTerreo=" + filaTerreo + ", status=" + status + ", marcadorTemporal="
				+ marcadorTemporal + "]";
	}

}
