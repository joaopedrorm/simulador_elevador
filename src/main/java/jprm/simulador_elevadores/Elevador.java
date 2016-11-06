package jprm.simulador_elevadores;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class Elevador {
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
		// pegar lista de paradas restantes
		List<Integer> paradas = getParadasLotacao();
		Integer ultimaParada = getUltimaParadaLotacao().orElse(andarAtual);
		Duration tempoParado = periodoParada.multipliedBy(paradas.size());
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
				.filter(a -> a > this.andarAtual && a <= this.andarMaximo).collect(Collectors.toList());
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
		return filaTerreo.stream().filter(Objects::nonNull).map(p -> p.getAndar())
				.filter(a -> a > this.andarAtual && a <= this.andarMaximo).collect(Collectors.toList());
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
			ElevadorStatus status, LocalDateTime marcadorTemporal) {
		super();
		this.andarAtual = andarAtual;
		this.andarMinimo = andarMinimo;
		this.andarMaximo = andarMaximo;
		this.lotacaoMaxima = lotacaoMaxima;
		this.status = status;
		this.marcadorTemporal = marcadorTemporal;
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
