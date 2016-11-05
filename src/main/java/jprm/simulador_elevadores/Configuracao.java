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

	public Configuracao() {
		this.properties = new Properties();
		this.filename = "config.properties";
		this.classLoader = this.getClass().getClassLoader();
		this.carregarResource();
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
		try (InputStream input = this.classLoader.getResourceAsStream(this.filename)) {
			// load properties file
			this.properties.load(input);
		} catch (IOException e) {
			logger.error(String.format("Erro ao carregar %s", this.filename), e);
		}
		return this;
	}

	public Configuracao carregar() {
		try (InputStream input = new FileInputStream(this.filename)) {
			// load properties file
			this.properties.load(input);
		} catch (IOException e) {
			logger.warn(String.format("Erro ao carregar %s", this.filename), e);
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
