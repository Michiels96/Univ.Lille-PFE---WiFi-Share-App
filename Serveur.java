import java.io.*;
import java.net.*;

class Serveur{

    public static void main(String[] args){
        Thread threadTCP = new Thread(new ServeurTCP());
        threadTCP.start();
        /*Thread threadUDP = new Thread(new ServeurUDP());
        threadUDP.start();*/
    }


    public enum TCPCodes{
        SEARCH_FOR_OTHER_DEVICES("47b8!F"),
        RECIEVE_DATA_FILE("8#96hJ"),
        STILL_ALIVE("HyT8F*"),
        FILE_CODE("UD'()6"),
        MESSAGE_CODE("sTuP'8"),
        CONF_MESSAGE("3&-Fds");

        private String code;
        TCPCodes(String s) {
            this.code = s;
        }

        public String getCode() {
            return code;
        }
    }

    static class ServeurTCP implements Runnable{
        ServerSocket ss;
        ServerSocket ssFile = null;
        Socket mySocket;
        DataInputStream dis;
        DataOutputStream dos;
        String data;
        boolean isFileToRecieve;
        String fileNameToRecieve;
        int fileSize;

        @Override
        public void run(){
            try{
                while(true){
                    ss = new ServerSocket(3300);
                    ssFile = new ServerSocket(3301);
                    System.out.println("TCP Listening...");
                    mySocket = ss.accept();
                    dis = new DataInputStream(mySocket.getInputStream());
                    try {
                        data = dis.readUTF();
                        String code = data.substring(0, 6);
                        data = data.substring(6);
                        System.out.println("TCP");
                        switch (code) {
                            case "47b8!F":
                                System.out.println("\tRECHERCHE " + code);
                                System.out.println("\tEnvois du nom PortABLE");
                                dos = new DataOutputStream(mySocket.getOutputStream());
                                dos.writeUTF("PortABLE");
                                dos.close();
                                dis.close();
                                break;
                            case "sTuP'8":
                                System.out.println("\tRECEPTION type->message " + code);
                                isFileToRecieve = false;
                                System.out.println("\t\t" + data);
                                dos = new DataOutputStream(mySocket.getOutputStream());
                                dos.writeUTF(TCPCodes.CONF_MESSAGE.getCode());
                                dos.close();
                                dis.close();
                                break;
                            case "UD'()6":
                                System.out.println("\tRECEPTION type->fichier " + code);
                                fileSize = Integer.parseInt(data.substring(data.indexOf('|') + 1));
                                fileNameToRecieve = data.substring(0, data.indexOf('|'));
                                fileNameToRecieve = fileNameToRecieve.substring(1, fileNameToRecieve.length() - 1);
                                System.out.println("\tNomDuFichier -> " + fileNameToRecieve + " taille -> " + fileSize);
                                isFileToRecieve = true;
                                dos = new DataOutputStream(mySocket.getOutputStream());
                                dos.writeUTF(TCPCodes.CONF_MESSAGE.getCode());
                                dos.close();
                                dis.close();


                                mySocket.close();

                                Socket mySocketFile = null;
                                BufferedInputStream bis = null;
                                BufferedOutputStream bosFile = null;
                                try {

                                    mySocketFile = ssFile.accept();
                                    System.out.println("info" + fileSize + " " + fileNameToRecieve);
                                    bis = new BufferedInputStream(mySocketFile.getInputStream());

                                    try {
                                        /*if (file.createNewFile()) {
                                            System.out.println("File created: " + file.getName());
                                        } else {
                                            System.out.println("File already exists. " + file.getName() + " " + file.getCanonicalPath());
                                        }*/
                                        bosFile = new BufferedOutputStream(new FileOutputStream(fileNameToRecieve));
                                        //Create byte array
                                        byte[] b = new byte[1024 * 8];
                                        //Read character array
                                        int len;
                                        while ((len = bis.read(b)) != -1) {
                                            bosFile.write(b, 0, len);
                                        }
                                        bosFile.close();
                                        bis.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    System.out.println("Fichier reçu : " + fileNameToRecieve);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } finally {
                                    dis.close();
                                    dos.close();
                                    if (mySocketFile != null) {
                                        mySocketFile.close();
                                    }
                                    if (ssFile != null) {
                                        ssFile.close();
                                    }
                                    if (bis != null) {
                                        bis.close();
                                    }
                                    if (bosFile != null) {
                                        bosFile.close();
                                    }
                                }
                                break;
                            default:
                        }
                    }catch(BindException g){


                    }catch(IOException e) {
                        // rien faire, c'est normal lors d'un scan qu'il n'envoit pas de données
                    }
                    mySocket.close();
                    ssFile.close();
                    ss.close();
                }
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
    }

}