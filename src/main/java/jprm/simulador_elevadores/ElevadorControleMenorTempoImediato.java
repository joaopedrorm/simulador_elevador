package jprm.simulador_elevadores;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElevadorControleMenorTempoImediato implements ElevadorControle {

	private List<Elevador> elevadores;

	@Override
	public void inicializar(List<Elevador> elevadores) {
		this.elevadores = elevadores;
	}

	@Override
	public Elevador decisao(Pessoa p, LocalDateTime instanteAtual) {
		// calcula tempo restante para elevador chegar, e calcula tempo simulado com a pessoa inclusa na fila
		Map<Duration, Elevador> mapa = new HashMap<>();
		for (Elevador e : this.elevadores) {
			mapa.put(e.calcularTempoRestanteTerreo(instanteAtual).plus(e.simularTempoFilaEspera(p)), e);
		}
		// cuidado get() pode soltar exception
		Duration tempoMinimo = mapa.keySet().stream().min(Duration::compareTo).get();
		
		return mapa.get(tempoMinimo);
	}

	@Override
	public String getNome() {
		return "Controle Menor Tempo Imediato";
	}

}
