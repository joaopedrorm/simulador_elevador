package jprm.simulador_elevadores;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuracao {

	private Properties properties;

	private String filename;

	private ClassLoader classLoader;

	private static final Logger logger = LoggerFactory.getLogger(Configuracao.class);
	
	private static final String filenameDefault = "config.properties";

	public Configuracao() {
		this.filename = filenameDefault;
		this.properties = new Properties();
		this.classLoader = this.getClass().getClassLoader();
		this.carregarResource();
	}
	
	public Configuracao(String filename) {
		this.filename = filename;
		this.properties = new Properties();
		this.classLoader = this.getClass().getClassLoader();
	}

	public Configuracao salvar() {
		try (OutputStream output = new FileOutputStream(this.filename)) {
			// save properties file
			this.properties.store(output, null);
		} catch (IOException e) {
			logger.error(String.format("Erro ao salvar %s", this.filename), e);
		}
		return this;
	}

	public Configuracao carregarResource() {
		return carregar(this.classLoader.getResource(this.filename).getPath());
	}

	public Configuracao carregar() {
		return carregar(this.filename);
	}
	
	private Configuracao carregar(String filenamePath) {
		try (InputStream input = new FileInputStream(filenamePath)) {
			// load properties file
			this.properties.load(input);
		} catch (IOException e) {
			logger.warn(String.format("Erro ao carregar %s", filenamePath), e);
		}
		return this;
	}

	public Configuracao set(String chave, String valor) {
		this.properties.setProperty(chave, valor);
		return this;
	}

	public Optional<String> get(String chave) {
		return Optional.ofNullable(this.properties.getProperty(chave));
	}

	public String get(String chave, String valorDefault) {
		return this.properties.getProperty(chave, valorDefault);
	}

}
