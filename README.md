# BANKING-MANAGEMENT-SYSTEM-
This Banking management system that uses swing as GUI in frontend and springboot in backend with its connection to JDBC
Java Swing Banking Client

This project is a desktop Graphical User Interface (GUI) client built using Java Swing. It is designed to interact with a separate Spring Boot Banking API (assumed to be running on http://localhost:8080).

The client demonstrates advanced Java concepts, including:

Building a multi-screen GUI (Login/Main Application) using JFrame and standard Swing components.

Handling asynchronous operations using SwingWorker to prevent the UI from freezing.

Making HTTP requests to a REST API using the modern java.net.http.HttpClient.

JSON serialization and deserialization using the Jackson library.

Prerequisites

Before running this client, you must have the following:

Java Development Kit (JDK) 11 or later: Required to compile and run the Java code.

Spring Boot Banking API: A companion API server must be running on http://localhost:8080.

Jackson Libraries: The following three JAR files must be downloaded and placed in a lib/ directory inside the project root:

jackson-databind-2.16.1.jar

jackson-core-2.16.1.jar

jackson-annotations-2.16.1.jar

Project Structure

The client is divided into two main Java files:

File Name

Description

LoginFrame.java

The entry point of the application. Handles user authentication (admin/password for demo) and transitions to the main GUI upon successful login.

BankingSystemGUI.java

The main application window. Contains the UI logic for displaying balances, executing transfers, and creating new accounts by interacting with the REST API.

Installation and Execution

You must compile both files and include the Jackson JARs in the classpath for both compilation and execution.

Step 1: Compilation

Navigate to the root directory of your project (banking-swing-client/) in your terminal and execute the appropriate command below.

For Mac/Linux:

javac -cp ".:lib/jackson-databind-2.16.1.jar:lib/jackson-core-2.16.1.jar:lib/jackson-annotations-2.16.1.jar" LoginFrame.java BankingSystemGUI.java


For Windows (Command Prompt / PowerShell):

javac -cp ".;lib\jackson-databind-2.16.1.jar;lib\jackson-core-2.16.1.jar;lib\jackson-annotations-2.16.1.jar" LoginFrame.java BankingSystemGUI.java


Step 2: Execution

After successful compilation, run the application using the LoginFrame class as the main entry point.

For Mac/Linux:

java -cp ".:lib/jackson-databind-2.16.1.jar:lib/jackson-core-2.16.1.jar:lib/jackson-annotations-2.16.1.jar" LoginFrame


For Windows (Command Prompt / PowerShell):

java -cp ".;lib\jackson-databind-2.16.1.jar;lib\jackson-core-2.16.1.jar;lib\jackson-annotations-2.16.1.jar" LoginFrame


Step 3: Login

A login screen will appear. Use the following default credentials to proceed:

Username: admin

Password: password

Troubleshooting

Error Message

Possible Cause

Solution

The import com.fasterxml cannot be resolved

The Jackson JAR files are missing or the classpath is incorrect.

Ensure the three Jackson JAR files are in the lib/ directory and verify the version number (2.16.1) in the compilation/run command.

Connection refused or Blank UI

The Spring Boot Banking API is not running.

Start the backend API application on port 8080 before running the client.

Could not find or load main class LoginFrame

The class file was not compiled correctly.

Re-run the compilation step, ensuring the classpath is correctly specified and includes the current directory (. or .;).
