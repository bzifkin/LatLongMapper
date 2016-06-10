package me.bzifkin;

import java.io.IOException;

/**
 * Created by bzifkin on 6/3/16.
 */
public class CommandLineTest {
    public static void main(String[] args) {
        while(true) {
            System.out.println("Context For Location");
            System.out.println("Best 10 results");
            String user = readString("Which one is right? ");
            if(user == null || user.equals("q")) break;
            if(user.isEmpty()) { System.out.println("skip"); }
            try {
                int number = Integer.parseInt(user);
                System.out.println("Great! you identified the location as #"+number);
            } catch (NumberFormatException nfe) {
                System.out.println("Not a Number!"); continue;
            }
        } // keep going forever.
    }
    public static String readString(String prompt) {
        System.out.print(prompt);
        StringBuilder sb = new StringBuilder();
        while(true) {
            int ch = 0;
            try {
                ch = System.in.read();
            } catch (IOException e) {
                break;
            }
            if(ch == -1) return null;
            if(ch == '\n' || ch == '\r') break;
            sb.append((char) ch);
        }
        return sb.toString();
    }

}
