package jprm.simulador_elevadores;

import java.time.LocalDateTime;

public class Pessoa {

	private String nome;
	private Integer andar;
	private LocalDateTime instanteChegada;
	private LocalDateTime instanteEmbarque;
	private LocalDateTime instanteDembarque;

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

	public LocalDateTime getInstanteDembarque() {
		return instanteDembarque;
	}

	public void setInstanteDembarque(LocalDateTime instanteDembarque) {
		this.instanteDembarque = instanteDembarque;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((andar == null) ? 0 : andar.hashCode());
		result = prime * result + ((instanteChegada == null) ? 0 : instanteChegada.hashCode());
		result = prime * result + ((instanteDembarque == null) ? 0 : instanteDembarque.hashCode());
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
		if (instanteDembarque == null) {
			if (other.instanteDembarque != null)
				return false;
		} else if (!instanteDembarque.equals(other.instanteDembarque))
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
		return "Pessoa [nome=" + nome + ", andar=" + andar + ", instanteChegada=" + instanteChegada
				+ ", instanteEmbarque=" + instanteEmbarque + ", instanteDembarque=" + instanteDembarque + "]";
	}

}
