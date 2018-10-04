//*
*------------Assignment-1--Group-Chat-------------------------
*------------Submitted-by--Brady-Ibanez--100367230------------
*------------Submitted-on--October-4-2018---------------------
*------------SOFE-4790U-Distributed-Systems-------------------
*//


import java.net.*;
import java.io.*;
import java.lang.*;

public class groupChatClient{

   static String chat;
   static String hostName;
   static int portNumber;
   public static String keyboard;//global to further reference stdIn without race conditioning

   public static void main(String[] args) {

	if (args.length != 2) {
        	System.err.println("Usage: Include <host name> <port number>");
		System.exit(1);
	}

	hostName = args[0];
        portNumber = Integer.parseInt(args[1]);
	String checkUsers, commands = null;

	try {
		Socket serverSide = new Socket (hostName, portNumber);
		Chatting chatThread = new Chatting(serverSide);
		Listening listenThread = new Listening(serverSide);
		Thread chatterer = new Thread(chatThread);
		Thread listener = new Thread(listenThread);
		chatterer.start();
		listener.start();

	    }  catch (Exception e) {
		System.out.println("something bad happened...");
            }
    }
}

    class Listening implements Runnable{//thread class for listening to server

	Socket server = null;

	Listening(Socket serverSide) {
		this.server = serverSide;
	}

	public void run() {
		try
		{
			Chatting listenToChat = new Chatting(server);//Dummy object to return Chatting object output to server
			BufferedReader listenIn = new BufferedReader(new InputStreamReader(server.getInputStream()));
			String fromServer = new String();
			while ((fromServer = listenIn.readLine()) != null) {
				System.out.print("\033[H\033[2J");
				while(true){
					String referenceInput = listenToChat.getCommand();
					if (referenceInput != null) {
						if (referenceInput.equals("help")){
							System.out.print("\033[H\033[2J");
						        System.out.println("HELP DOC:");
							System.out.println(" ");
						        System.out.println("'list users': Find names of all active users.");
							System.out.println("'how many users?': Find if all conversation seats are full.");
							System.out.println("'which seat am I in?': Find out which conversation seat you are in.");
							System.out.println("'quit': Close your connection to the messaging server.");
							System.out.println("'change user name': Update displayed user name");
        					        System.out.println("'chat': Contribute to ongoing chat. Be sure to check if participants are present.");
							break;
						}
						else if (referenceInput.equals("list users")){
							System.out.println("CURRENT USERS");
							System.out.println(" ");
							System.out.println(fromServer);
							break;
						}
						else{
							System.out.println(fromServer);
						}break;
					} else {System.out.println("From server: " + fromServer); break;}
					}
				}
			} catch (IOException e) {
				System.out.println("I/O is having an issue...");
			}
		}
    	}

    class Chatting implements Runnable {//Thread class for talking to server

	Socket server = null;
	public static String userInput;//Needed to reference in Listener. Never manipulated anywhere but here

	Chatting(Socket serverSide) {
			this.server = serverSide;
		}
	public String getCommand(){
		return userInput;
	}

	public void run(){
	try
	{
              BufferedReader stdChatIn = new BufferedReader(new InputStreamReader(System.in));
              PrintWriter chatOut = new PrintWriter(server.getOutputStream(), true);

	      String[] userInRead = new String[6];
	      String userString = new String();
	      String messageChat = new String();
	      String command = new String();
	      String broadcast = new String (); //variable for all server provided instructions for a command

              System.out.print("\033[H\033[2J");
	      System.out.println("Welcome to the LAN group messenger!");
	      System.out.println(" ");
              System.out.println("If you'd like to see the list of app commands, type help at any time.");
	      System.out.println(" ");
	      System.out.println("Please enter the command 'change user name', then enter the name you would like to be referred to. This can be done at any time.");

	      if(userString.equals("I'm awake"))//Initialize client thread with acknowledgement from server, needed to not give thread preference to chat initiator
	      {
		chatOut.println("I'm awake");
              }

	      while((userString = stdChatIn.readLine()) != null) {
		userInput = userString;//needed to convert userString to readable String? Thanks Java
		chatOut.println(userInput);//sends stdIN to output stream by default

		if (userInput.equals("chat")) // send message content and trigger broadcast function from server after sending
		{
			System.out.print("\033[H\033[2J");
			String theMessage = userString;
			chatOut.println(theMessage);//Send the message to the server
		}

		else if(userInput.equals("quit"))//Farwell message after disconnect
		{
			System.out.print("\033[H\033[2J");
			System.out.println("Thanks for using the LAN group messenger!");
			System.exit(0);
		}

		else if(userInput.equals("I'm awake"))
		{
			System.out.println("Here");
			chatOut.println("I'm awake");
		}

		else //default wipe screen to see next input
		{
			System.out.print("\033[H\033[2J");
		}
	    }
	}catch(UnknownHostException e){
		System.err.println("Can't find the host. Terminating execution." );
		System.exit(1);
	}catch(IOException e){
		System.err.println("No I/O apparent.");
		System.exit(1);
	}
    }
}



