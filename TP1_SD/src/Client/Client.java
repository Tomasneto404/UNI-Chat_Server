package Client;

import Exceptions.StopReadingException;

import java.io.*;
import java.net.*;
import java.util.Scanner;

import static java.lang.System.exit;

/***
 * Classe responsável por estabelecer uma conexão com um servidor remoto
 * e gerenciar a comunicação entre o cliente e o servidor.
 */
public class Client {

    private static final String SERVER_ADDRESS = "127.0.0.1";
    
    //Porta padrão do servidor
    private int serverPort = 4444;

    /**
     * Construtor para inicializar o cliente com uma porta específica.
     * @param serverPort A porta que o servidor está à escuta.
     * @throws IOException Se ocorrer um problema durante a criação do cliente.
     */
    public Client(int serverPort) throws IOException {
        this.serverPort = serverPort;
    }

    /***
     * Método principal que permite iniciar a conexão entre o servidor e gerir a comunicação.
     * @throws IOException Se ocorrerem problemas de input/output durante a comunicação
     */
    public void start() throws IOException {

        try {
    
            Socket socket = new Socket(SERVER_ADDRESS, serverPort);

            //configuração dos fluxos de entrada e saída para a comunicação com o servidor.
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in);

            UserInterface menu = new UserInterface();

            System.out.println("Connected to the server!");

            while (true) {
                try {
                    menu.start(scanner, reader, writer);
                } catch (StopReadingException e) {
                    exit(0);
                }
            }

        } catch (IOException e) {
            System.err.println("Error in client: " + e.getMessage());
        }
    }

    /***
     * Método "Main", ou seja, ponto de entrada da aplicação.
     * @param args Argumentos da linha de comandos por default.
     */
    public static void main(String[] args) {
        try {
            Client client = new Client(4444);
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}


