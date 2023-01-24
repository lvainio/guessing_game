import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import java.net.ServerSocket;
import java.net.Socket;

class Server {

    // SET COOKIE
    // we need some sort of hashtable that stores information about guesses for each cookie id
    // we need to randomize number for every client (also stored in hashmap depending on cookie i guess)
    // implement the logic for the guessing, pretty easy tbh. 
    // implement a congrats screen
    // what happens if the guess is not a nr?
    // exception handling WITHOUT crashing the server from something the client does or sends.
    // PRG
    // print a hint (do we need another html file)
    // html file for conggrats screen?
    // hur håller vi isär olika spelomgångar.

    // KRAV:
    // - Kom ihåg användare genom att spara sessionID i cookie
    // - Vid en gissning ska servern svara med ledtråden om vilket intervall nästa gissning ska vara
    // - HTML:en måste vara valid (kontrollera detta)
    // - Vid korrekt gissning:
    //      1. Sessionen avslutas, vilket inkluderar att sessionskakan invalideras,
    //      och all info om sessionen och avklarade spelomgången raderas från minnet
    //      2. Användaren får en sida med information om att gissningen var korrekt med antal gjorda gissningar
    //      3. Det ska finnas en knapp (länk) så att användaren kan starta en ny omgång
    //      4. Om användaren uppdaterar sidan genom att klicka in i adressfältet och trycka på enter ska programmet visa samma sida
    // - Din lösning ska vara baserad på webbutvecklingsdesginmönstret PRG
    // - En klients handling på webbläsaren ska inte krascha servern

    private final int PORT = 1234;

    public static void main(String[] args) {
        new Server();
    }

    public Server() {
		try (ServerSocket server = new ServerSocket(PORT)) {
			Socket socket;
			while((socket = server.accept()) != null) {
				BufferedReader socketIn = new BufferedReader (new InputStreamReader(socket.getInputStream()));
				PrintStream socketOut = new PrintStream(socket.getOutputStream());
				String headers = readHeaders(socketIn);

				if(headers.indexOf("GET") == 0) {
					handleGET(headers, socketOut, socket);
				} else if(headers.indexOf("POST") == 0) { 
					handlePOST(headers, socketIn, socketOut, socket);
				} else {
					System.out.println("Unknown request!");
				} 
			}
		} catch (IOException e){
			e.printStackTrace();
			// TODO: exit?
		}
    }

    // handleGET
    public void handleGET(String headers, PrintStream socketOut, Socket socket) {
        System.out.println(headers + "<"); // log
        String payload = loadFile("./files/index.html");
        String response= "HTTP/1.1 200 OK\nDate: Mon, 15 Jan 2018 22:14:15 GMT\nContent-Length: " + 
                            payload.length() + 
                            "\nConnection: close\nContent-Type: text/html\n\n";
        response += payload;
        socketOut.print(response);
        try {
            socket.shutdownOutput();
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: exit?
        }
    }

	// handlePost
    public void handlePOST(String headers, BufferedReader socketIn, PrintStream socketOut, Socket socket) {
		String number="";

		System.out.println(headers);
		String request_payload=null;
		String response_payload=null;
											
		if (headers.indexOf("/guess") == 5) {
			request_payload = readPayload(socketIn, headers);
            number = request_payload.substring(request_payload.indexOf("number=") + "number=".length());
            response_payload = loadFile("./files/guessing.html");						
		}

		response_payload = put_numbers_in_payload(response_payload, number);
		String response= "HTTP/1.1 200 OK\nDate: Mon, 15 Jan 2018 22:14:15 GMT\nContent-Length: " + 
                            response_payload.length() + 
                            "\nConnection: close\nContent-Type: text/html\n\n";
		response += response_payload;
		socketOut.print(response);
		try {
			socket.shutdownOutput();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    // loadFile reads a file and returns its content as a string.
    public String loadFile(String file) {
        String contents = "";
        String line = "";
        try (BufferedReader br = new BufferedReader(new FileReader(file));) {
            while ((line = br.readLine()) != null) {
                contents += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
		return contents;
    }

    // readHeaders reads the header part of a request and returns it as a string.
    public String readHeaders(BufferedReader socketIn) {
        String headers = "";
        try {
            String line = socketIn.readLine(); 
            while (!line.equals("")) { // reached end of headers (remember: two \n\n)
                headers += line + "\n";
                line = socketIn.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return headers;
    }

    // readPayload reads the payload part of a request and returns it as a string.
    public String readPayload(BufferedReader scktIn, String headers) {
        int content_length = Integer.parseInt((((headers.split("Content-Length: "))[1]).split("\n"))[0]);
        char[] buf = new char[content_length];
        try {
            scktIn.read(buf, 0, content_length);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(content_length);
        }
		return new String(buf);
    }

    // put_numbers_in_payload
    public String put_numbers_in_payload(String response_payload, String numbers){
		return response_payload.replace("$numbers$", numbers);
    }
}
