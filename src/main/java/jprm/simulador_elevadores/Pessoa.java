package jprm.simulador_elevadores;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public class Pessoa {

	private String nome;
	private Integer andar;
	private LocalDateTime instanteChegada;
	private LocalDateTime instanteEmbarque;
	private LocalDateTime instanteDesembarque;

	public Optional<Duration> calculaTempoEsperaFila() {
		if (this.instanteEmbarque == null) {
			return Optional.empty();
		} else {
			return Optional.of(Duration.between(this.instanteChegada, this.instanteEmbarque));
		}
	}

	public Optional<Duration> calculaTempoChegadaAndar() {
		if (this.instanteEmbarque == null || this.instanteDesembarque == null) {
			return Optional.empty();
		} else {
			return Optional.of(Duration.between(this.instanteEmbarque, this.instanteDesembarque));
		}
	}

	public Optional<Duration> calculaTempoTotalPercurso() {
		return calculaTempoEsperaFila().flatMap(tef -> calculaTempoChegadaAndar().map(tca -> tca.plus(tef)));
	}

	public Pessoa(String nome, Integer andar, LocalDateTime instanteChegada) {
		super();
		this.nome = nome;
		this.andar = andar;
		this.instanteChegada = instanteChegada;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Integer getAndar() {
		return andar;
	}

	public void setAndar(Integer andar) {
		this.andar = andar;
	}

	public LocalDateTime getInstanteChegada() {
		return instanteChegada;
	}

	public void setInstanteChegada(LocalDateTime instanteChegada) {
		this.instanteChegada = instanteChegada;
	}

	public LocalDateTime getInstanteEmbarque() {
		return instanteEmbarque;
	}

	public void setInstanteEmbarque(LocalDateTime instanteEmbarque) {
		this.instanteEmbarque = instanteEmbarque;
	}

	public LocalDateTime getInstanteDesembarque() {
		return instanteDesembarque;
	}

	public void setInstanteDesembarque(LocalDateTime instanteDesembarque) {
		this.instanteDesembarque = instanteDesembarque;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((andar == null) ? 0 : andar.hashCode());
		result = prime * result + ((instanteChegada == null) ? 0 : instanteChegada.hashCode());
		result = prime * result + ((instanteDesembarque == null) ? 0 : instanteDesembarque.hashCode());
		result = prime * result + ((instanteEmbarque == null) ? 0 : instanteEmbarque.hashCode());
		result = prime * result + ((nome == null) ? 0 : nome.hashCode());
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
		Pessoa other = (Pessoa) obj;
		if (andar == null) {
			if (other.andar != null)
				return false;
		} else if (!andar.equals(other.andar))
			return false;
		if (instanteChegada == null) {
			if (other.instanteChegada != null)
				return false;
		} else if (!instanteChegada.equals(other.instanteChegada))
			return false;
		if (instanteDesembarque == null) {
			if (other.instanteDesembarque != null)
				return false;
		} else if (!instanteDesembarque.equals(other.instanteDesembarque))
			return false;
		if (instanteEmbarque == null) {
			if (other.instanteEmbarque != null)
				return false;
		} else if (!instanteEmbarque.equals(other.instanteEmbarque))
			return false;
		if (nome == null) {
			if (other.nome != null)
				return false;
		} else if (!nome.equals(other.nome))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "\n Pessoa [nome=" + nome + ", andar=" + andar + ", instanteChegada=" + instanteChegada
				+ ", instanteEmbarque=" + instanteEmbarque + ", instanteDesembarque=" + instanteDesembarque
				+ ", tempoEsperaFila=" + calculaTempoEsperaFila() + ", tempoChegadaAndar=" + calculaTempoChegadaAndar()
				+ ", tempoTotalPercurso=" + calculaTempoTotalPercurso() + "]";
	}

}
