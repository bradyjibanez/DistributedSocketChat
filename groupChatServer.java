//*
*------------Assignment-1--Group-Chat-------------------------
*------------Submitted-by--Brady-Ibanez--100367230------------
*------------Submitted-on--October-4-2018---------------------
*------------SOFE-4790U-Distributed-Systems-------------------
*//

import java.net.*;
import java.io.*;
import java.util.concurrent.locks.*;

public class groupChatServer extends Thread{

    //Run the whole thing
    public static void main(String[] args) {
//	boolean quit = false;

	//Tell users they need to devine their port number
	if (args.length != 1) {
		System.err.println("Usage: java EchoServer <port number>");
		System.exit(1);
	}
	//Port number integration from user instantiation request, Server instantiation, Server exec
	int portNumber = Integer.parseInt(args[0]);
	try{
		ServerSocket serverSocket = new ServerSocket(portNumber);
		int clientCount = 0;
		Socket clientUnison[] = new Socket[4];

		while(true) { //This loop necessary for multiple clients
			//Creates socket interaction with defined client requesting service
			Socket client = serverSocket.accept();
			PrintWriter out = new PrintWriter(client.getOutputStream(), true);//To client
			Connection aConnection = new Connection(client, serverSocket);
			clientCount++;
		}
	} catch (Exception e) {
		System.out.println("Exception: " + e);
        }
    }
}

class Connection extends Thread {

//VARIABLE DECLARATIONS

    //Locals sensitive to each thread
    Socket client;
    ServerSocket server;
    PrintWriter out;
    BufferedReader in, in2, in3;
    String whatWasSaid, whatWasSaidString, userName, newName, publicMessage;
    int loopCount; // Used to count how many times a thread has interacted. Only used now for start purposes.
    int mySeatCount; //Retains specific users[x][0] value for this user/client
    int myPort; //Retains client's local port
    ReentrantLock lock = new ReentrantLock();//Needed for mutex implementation on sensative vars

    //Globals to be shared by threads
    public static String[][] users = new String[4][2]; //Include up to 4 users. 1 base indexing in Java on declaration.
    public static int userCount = 0;//Holds a count of all active users. users[number][port=0,available=1]
    public static int seatCount; //Needs to be shared so threads know where previous threads were changing count
    public static String groupChat;//Used for global variable to update in group chat
    public static Socket clients[] = new Socket[4];

//CONSTRUCTOR DECLARATION - for each thread instatiation, used to trigger the tread to start

    public Connection(Socket c, ServerSocket s) {

	client = c;
	clients[userCount] = c;
	server = s;
	if (userCount == 4)
	{
		out.println("Sorry, we're full up. Try again later.");
		try {
		client.close();
		} catch (IOException exe){
		System.out.println("No client to close because: " + exe);
		}
	}

	else
	{
		for (int i = 0; i < 4; i++){ //Declare seat join for requesting client as initial is not otherwise
			if (users[i][1] != "true" && users[i][1] != "false")
			{
				users[i][1] = "initial";
			}
		}
	        try {
        	        out = new PrintWriter(client.getOutputStream(), true);//To client
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));//From client
			in2 = new BufferedReader(new InputStreamReader(client.getInputStream()));//needed for one of single reads that get stuck with the while loop in run()
        	} catch (IOException e) {
                	try {
                		client.close();
               		} catch (IOException ex) {
                		System.out.println("No client to close because: " + ex);
        		}
        	return;
        	}
	this.makeConnection();
	this.start();//thread starts here...connects run()
	return;
	}
    }

//METHOD DECLARATIONS

    //Provides means of updating registered users reference list with their ports/names. Only 4 allowed, but could be altered.
    public synchronized void makeConnection() {

	lock.lock();
	loopCount = 1; //Set to 1 so server doesn't start responding earlier. Starts @ > 1. Should be called startTrigger
	for (seatCount = 0; seatCount < 4; seatCount++){
		System.out.print("\033[H\033[2J");
		String reference = this.userConnectionAvailability(seatCount);

		System.out.println("CONNECTION REQUEST");
		System.out.println(" ");
		System.out.println("Seat Number: " + seatCount);
		System.out.println("Availability Status: " + reference);
		System.out.println(" ");

		//Initial seat usage
		if (reference.equals("initial") && users[seatCount][1] != "false")
		{
			System.out.println("CONNECTION STATUS");
			System.out.println(" ");
			System.out.println("Connection Available.");
		        mySeatCount = seatCount;
                        myPort = client.getPort();
                        userName = Integer.toString(myPort);
			users[mySeatCount][0] = userName;//User name
			users[mySeatCount][1] = "false";//Occupancy Check
			++userCount;
			System.out.println("Active users: " + userCount);
			System.out.println("Seat occupied: " + seatCount);
			loopCount++;
			System.out.println("User connected");
			break;
		}

		//Reuse of seat
		if (reference.equals("true") && users[seatCount][1] != "false")
		{
			System.out.println("CONNECTION STATUS");
			System.out.println(" ");
			System.out.println("Connection Available at user place: " + seatCount);
			mySeatCount = seatCount;
                        myPort = client.getPort();
                        userName = Integer.toString(myPort);
			users[mySeatCount][0] = userName;
			users[mySeatCount][1] = "false";
			++userCount;
			System.out.println("Active users: " + userCount);
			System.out.println("Seat occupied: " + seatCount);
			loopCount++;
			System.out.println("User connected");
			break;
		}

		//Seat unavailable
		if (reference.equals("false"))
		{
			System.out.println("CONNECTION STATUS");
			System.out.println(" ");
			System.out.println("Connection not Available");
		}
	}
	lock.unlock();
    }

    //Provides a means of catalouging connected users and organizing users[][] array
    //No need to synchronize, method only ever called within synchronized method makeConnection()
    public String userConnectionAvailability(int refCount) {

	String goAhead;
	int referenceCount = refCount;

	String connectionAvailability = users[referenceCount][1];

	if (connectionAvailability.equals("true"))
	{
		goAhead = "true";
	}

	else if (connectionAvailability.equals("false"))
	{
		goAhead = "false";
	}

	else
	{
		goAhead = "initial";
	}

	return goAhead;
  }

    //Removes client (port or name if written) from users list
    public synchronized void removeUserInfo() {
	lock.lock();
	System.out.print("\033[H\033[2J");
        System.out.println("CONNECTION TERMINATION REQUEST");
	System.out.println(" ");
	System.out.println("Seat/User: " + mySeatCount + "/" + users[mySeatCount][0]);
	users[mySeatCount][0] = null;//Clear client port
	users[mySeatCount][1] = null;//Clear connection status
	lock.unlock();
    }

    //Used to check what connections are made
    public void listUsers() {
	System.out.print("\033[H\033[2J");
	System.out.println("ACTIVE USERS REQUEST");
	System.out.println(" ");
	out.println(("'") + users[0][0] + ("'") + (" ") + ("'") + users[1][0] + ("'") + (" ") + ("'") + users[2][0] + ("'") + (" ") + ("'") + users[3][0] + ("'"));

	for(int i = 0; i < 4; i++)
	{
		if (users[i][0] != null)
		{
			System.out.println("Active user: " + users[i][0]);
		}

		else
		{
			System.out.println("Open seat: " + users[i][1]);
		}
	}
    }

    //Choose to terminate Connection and free space for other connections
    public synchronized void terminateConnection() {
	lock.lock();
//	out.println("Your connection has been terminated");
	users[mySeatCount][0] = null;
	users[mySeatCount][1] = "true";
	userCount--;
	System.out.println("New user count: " + userCount);
	try{
		client.close();
	} catch (IOException e) {
		System.out.println("Terminated the connection.");
	}
	lock.unlock();
    }

    //Needed for analyzing non chat commands vs chat input (more defined type analysis on BufferedStream/DataStream etc.)
    public String whatWasSaidString(String whatWasSaid){
	String whatWasSaidString = String.valueOf(whatWasSaid);
	return whatWasSaidString;
    }

    //Prints userCount to server and client display
    public void getUserCount(){ // should be printUserCount()
	System.out.print("\033[H\033[2J");
	System.out.println("USER VOLUME REQUEST");
	System.out.println(" ");
	System.out.println(userCount + " active user(s)");
	out.println("Currently " + userCount + " active user(s)");
    }

    //Prints concerned client index of userCount to server and client display
    public void getMyCount(){
	System.out.print("\033[H\033[2J");
	System.out.println("USER OCCUPANCY DETAIL REQUEST");
	System.out.println(" ");
	System.out.println("User: " + users[mySeatCount][0]);
	System.out.println("In seat: " + mySeatCount);
	out.println("You are in seat " + mySeatCount);
    }

    //Lets user opt for new display name
    public void changeUserName(){
	System.out.print("\033[H\033[2J");
	System.out.println("CHANGE USER NAME REQUEST");
	System.out.println(" ");
	System.out.println("Client must now select a new user name.");
	out.println("What would you like your user name to be?");
	try
	{
		newName = in2.readLine();//new Buffered Reader used to overcome while loop pitfall in run() method
		userName = newName;
		users[mySeatCount][0] = userName;
		out.println("You are now listed as: " + userName);
		System.out.println("New userName is: " + userName);
	}
	catch (IOException e)
	{
		System.out.println("Something happened");
	}
    }

    //Shows user possible commands when requested
    public void listCommands(){
	System.out.print("\033[H\033[2J");
	System.out.println("HELP REQUEST");
	System.out.println(" ");
	System.out.println("'list users': Find names of all active users.");
	System.out.println("'how many users?': Find if all conversation seats are full.");
	System.out.println("'which seat am I in?': Find out which conversation seat you are in.");
	System.out.println("'quit': Close your connection to the messaging server.");
	System.out.println("'change user name': Update displayed user name");
	System.out.println("'message': Contribute to ongoing chat...more instructions will follow.");
    }

    public void sendToAll(String whatWasSaid){
	out.flush();
	try
	{
		for (int count = 0; count < userCount; count++){
			out = new PrintWriter(clients[count].getOutputStream(), true);
			out.println("Message from " + users[mySeatCount][0] + ": " + whatWasSaidString);
		}
	} catch (IOException e) {
	System.out.println("I/O is having issues in sendToAll()");
	}
    }


//RUN/START METHOD DELARATION

    public void run(){

	String inputCheck;
	try {

		if (loopCount > 1)//Loop to determine if user/client has connected
		{
			whatWasSaid = in.readLine(); //to make sure whatWasSaid is initialized before do{}while()
			whatWasSaidString = this.whatWasSaidString(whatWasSaid);//outside:

			if (whatWasSaidString.equals("I'm awake"))//Initial transaction to server so clients can begin chatting regardless of admission count
                              {
                                     String acknowledgement = new String();
				     String acknowledgementString = new String();
                                     acknowledgement = in.readLine();
                                     acknowledgementString = this.whatWasSaidString(acknowledgement);//Might$
                                     out.println(acknowledgementString);
                              }

			do {  //This loop keeps client running until told otherwise
				System.out.print("\033[H\033[2J");
				System.out.println("INCOMING MESSAGE: ");
				whatWasSaidString = this.whatWasSaidString(whatWasSaid);
				System.out.println(" ");
				System.out.println("What was said: '" + whatWasSaid + "'");
				System.out.println("Said by: " + mySeatCount + "/" + userName);
				System.out.println(" ");

				if (whatWasSaidString.equals("help"))
				{
					this.listCommands();
				}

				if (whatWasSaidString.equals("quit"))
				{
					this.removeUserInfo();
					this.terminateConnection();
				}

				else if (whatWasSaidString.equals("list users"))
				{
					this.listUsers();
				}

				else if (whatWasSaidString.equals("how many users?"))
				{
					this.getUserCount();
				}

				else if (whatWasSaidString.equals("which seat am I in?"))
				{
					this.getMyCount();
				}

				else if (whatWasSaidString.equals("change user name"))
				{
					this.changeUserName();
				}

				else if (whatWasSaidString.equals("chat"))
				{
					out.println("What message would you like to share?");
					whatWasSaid = in.readLine();
					whatWasSaidString = this.whatWasSaidString(whatWasSaid);//Make it look twice to overcome the initial command to send a chat ("chat")
					whatWasSaid = in.readLine();
					whatWasSaidString = this.whatWasSaidString(whatWasSaid);
					this.sendToAll(whatWasSaidString);
				}

				else if (whatWasSaidString.equals("I'm awake"))//Welcome message on setup
				{
					out.println("Welcome to the LAN group chat!!");
				}

				else
				{
					String message = new String(); // Create string content to be sent back
					message = whatWasSaid; //Message from and for client for now
					groupChat = message;
					out.println("You echoed: '" + message + "'"); //you echoed if not working!!!!!!

				}
			}while((whatWasSaid = in.readLine()) != null);
		}
        } catch (IOException e) {
    	   System.out.println("Connection terminated");
	   System.out.println(e.getMessage());
        } catch (NumberFormatException ex) {
           System.out.println("Format not matching what I got...");
	   ex.printStackTrace();
	}
    }
}
