package jprm.simulador_elevadores;

import java.util.List;

public interface ElevadorControle {
	public void inicializar(List<Elevador> elevadores);

	public Elevador decisao(Pessoa p);
}
