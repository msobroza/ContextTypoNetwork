package tools;

import org.apache.commons.net.telnet.TelnetClient;

import java.io.InputStream;
import java.io.PrintStream;

public class AutomatedTelnetClient {

    private TelnetClient telnet = new TelnetClient();
    private InputStream in;
    private PrintStream out;
    private String prompt = "$";
    private StringBuffer result;
    private static final String server="10.29.182.68";
    // LOGIN HERE !
    private static final String user="***";
    // PASSWORD HERE !
    private static final String password="****";
    private static final String dir="/homes/***/stage/lia_phon";

    public AutomatedTelnetClient() {
        try {
            result= new StringBuffer();
            // Connect to the specified server
            telnet.connect(server, 23);

            // Get input and output stream references
            in = telnet.getInputStream();
            out = new PrintStream(telnet.getOutputStream());

            // Log the user on
            readUntil("login: ",false);
            write(user, false);
            readUntil("Password: ",false);
            write(password, false);

            // Advance to a prompt
            readUntil(prompt + " ", false);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public String readUntil(String pattern, boolean print) {
        try {
            char lastChar = pattern.charAt(pattern.length() - 1);
            StringBuffer sb = new StringBuffer();
            boolean found = false;
            char ch = (char) in.read();
            while (true) {
                if(print){
                    //System.out.print(ch);
                    result.append(ch);
                }
                sb.append(ch);
                if (ch == lastChar) {
                    if (sb.toString().endsWith(pattern)) {
                        return sb.toString();
                    }
                }
                ch = (char) in.read();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public void write(String value, boolean print) {
        try {
            out.println(value);
            out.flush();
            if(print){
                //System.out.println(value);
            }
                
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public String sendCommand(String command, boolean print) {
        try {
            write(command, false);
            return readUntil(prompt + " ", print);
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public void disconnect() {
        try {
            telnet.disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    private String getResultPhonemeLia(){
        //System.out.println(this.result.toString());
        //return this.result.toString().split("\n")[0].split(" ")[1];
        //System.out.println(this.result.toString());
        //System.out.println(this.result.toString().split("\n")[1].split(" ")[1]);
        return this.result.toString().split("\n")[1].split(" ")[1];
    }
    
    public String findPhonemeLia(String mot){
        result= new StringBuffer();
        /* this.sendCommand("echo \""+mot.toLowerCase().replaceAll(" ","").replaceAll("-", "")+"\" > /homes/msobroza/stage/lia_phon/motUTF8.txt", false);
        this.sendCommand("iconv -f utf8 -t latin1 /homes/msobroza/stage/lia_phon/motUTF8.txt > /homes/msobroza/stage/lia_phon/mot.txt", false);
        this.sendCommand("csh /homes/msobroza/stage/lia_phon/script/lia_lex2phon < /homes/msobroza/stage/lia_phon/mot.txt", true); */
        this.sendCommand("echo \""+mot.toLowerCase().replaceAll(" ","")+"\" > "+dir+"/motUTF8.txt", false);
        this.sendCommand("iconv -f utf8 -t latin1 "+dir+"/motUTF8.txt > "+dir+"/mot.txt", false);
        this.sendCommand("csh "+dir+"/script/lia_lex2phon < "+dir+"/mot.txt", true);
        
        return this.getResultPhonemeLia();
    }
    
}
