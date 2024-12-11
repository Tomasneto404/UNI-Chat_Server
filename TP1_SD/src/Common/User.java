package Common;

import Enums.Rank;

import Enums.OperationType;

import java.io.*;

/**
 * A classe User representa um utilizador com atributos como ID, nome, rank,
 * password,
 * estado de operação e estado de disponibilidade (online/offline). Inclui
 * também
 * métodos para manipulação e acesso aos atributos, bem como para interação com
 * ficheiros.
 * 
 */
public class User {

    private int id;
    private String name;
    private Rank rank;
    private String password;
    private OperationType op;
    private boolean status;

    /**
     * Construtor da classe User.
     * 
     * @param id   Identificador do utilizador.
     * @param name Nome do utilizador.
     * @param rank Rank do utilizador.
     */
    public User(int id, String name, Rank rank) {
        this.id = id;
        this.name = name;
        this.rank = rank;
    }

    /**
     * Construtor da classe User.
     * 
     * @param name     Nome do utilizador.
     * @param rank     Rank do utilizador.
     * @param password Password do utilizador.
     */
    public User(String name, Rank rank, String password) {
        this.name = name;
        this.rank = rank;
        this.password = password;
        this.status = false;
    }

    /**
     * Construtor da classe User.
     * 
     * @param name     Nome do utilizador.
     * @param password Password do utilizador.
     */
    public User(String name, String password) {
        this.id = 0;
        this.name = name;
        this.password = password;
        this.rank = Rank.NONE;
    }

    /**
     * Construtor da classe User.
     * 
     * @param id       Identificador do utilizador.
     * @param name     Nome do utilizador.
     * @param password Password do utilizador.
     * @param rank     Rank do utilizador.
     */
    public User(int id, String name, String password, Rank rank) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.rank = rank;
    }

    /**
     * Construtor da classe User.
     * 
     * @param id       Identificador do utilizador.
     * @param name     Nome do utilizador.
     * @param password Password do utilizador.
     * @param rank     Rank do utilizador.
     * @param status   Estado do utilizador (online/offline).
     */
    public User(int id, String name, String password, Rank rank, Boolean status) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.rank = rank;
        this.status = status;
    }

    /**
     * Construtor da classe User.
     * 
     * @param name Nome do utilizador.
     */
    public User(String name) {
        this.name = name;
    }

    /**
     * Define o tipo de operação do utilizador.
     * 
     * @param op Tipo de operação.
     */
    public void setOperation(OperationType op) {
        this.op = op;
    }

    /**
     * Obtém o tipo de operação do utilizador.
     * 
     * @return Tipo de operação.
     */
    public OperationType getOperation() {
        return this.op;
    }

    /**
     * Obtém o nome do utilizador.
     * 
     * @return Nome do utilizador.
     */
    public String getName() {
        return name;
    }

    /**
     * Obtém a password do utilizador.
     * 
     * @return Password do utilizador.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Obtém o rank do utilizador.
     * 
     * @return Rank do utilizador.
     */
    public Rank getRank() {
        return this.rank;
    }

    /**
     * Converte os atributos do utilizador para uma string no formato CSV.
     * 
     * @param chr Delimitador a ser usado no CSV.
     * @return String no formato CSV.
     */
    public String toCSV(String chr) {
        return this.id + chr + this.name + chr + this.password + chr + this.rank + chr + this.status + "\n";
    }

    /**
     * Escreve os dados do utilizador para um ficheiro.
     * 
     * @param file Caminho do ficheiro onde os dados serão escritos.
     * @throws IOException Lança uma exceção se ocorrer um erro de I/O.
     */
    public void writeToFile(String file) throws IOException {

        FileWriter fileWriter = new FileWriter(file, true);
        fileWriter.append(this.toCSV(";"));
        fileWriter.close();

    }

    /**
     * Verifica se o utilizador está online.
     * 
     * @return true se o utilizador estiver online, false caso contrário.
     */
    public boolean isOnline() {
        return status;
    }

    /**
     * Define o estado do utilizador (online/offline).
     * @param status Novo estado do utilizador.
     */
    public void setStatus(boolean status) {
        this.status = status;
    }
}
