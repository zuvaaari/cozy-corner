import java.io.*;
import java.net.Socket;
import javax.swing.JFileChooser;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class chat_client extends javax.swing.JFrame {

    static Socket s; //socket is used for network comms. static ensures that the socket is shared across all instances of the class
    static DataInputStream dis; //allow to recieve files
    static DataOutputStream dout; //send data from client to server
    static File saveDirectory;

    public chat_client() {
        initComponents(); //initializew the GUI
    }

    //User Interface
    private void initComponents() {
        //scroll panee
        jScrollPane1 = new javax.swing.JScrollPane();
        msg_area = new javax.swing.JTextPane(); //display the chat
        msg_txt = new javax.swing.JTextField(); //where user types the chat
        msg_send = new javax.swing.JButton(); //button to send the msg
        jLabel1 = new javax.swing.JLabel(); //label to display the title
        send_file = new javax.swing.JButton(); //button to send the file
        file_label = new javax.swing.JLabel(); //shpows the name of the file
        select_directory = new javax.swing.JButton(); //button to choose direct.

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("CozyCorner - Chatroom"); //title of the pop up window
        getContentPane().setBackground(new java.awt.Color(240, 240, 255));
        setPreferredSize(new java.awt.Dimension(400, 600));
        setLayout(new java.awt.GridBagLayout());
        //gridbag layout: align components
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(5, 5, 5, 5);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;

        //title
        jLabel1.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
        jLabel1.setForeground(new java.awt.Color(128, 0, 128));
        jLabel1.setText("CozyCorner - Chatting with Bob");
        //label position
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(jLabel1, gbc);

        //msg area: non editable
        msg_area.setEditable(false);
        msg_area.setBackground(new java.awt.Color(220, 220, 255));
        msg_area.setContentType("text/html");
        msg_area.setText("<html><body></body></html>");
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.8;
        jScrollPane1.setViewportView(msg_area);
        add(jScrollPane1, gbc);

        //area for where the chat message is typed 
        msg_txt.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 14));
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.8;
        gbc.weighty = 0;
        add(msg_txt, gbc);

        //send message button design
        msg_send.setBackground(new java.awt.Color(128, 0, 128));
        msg_send.setForeground(java.awt.Color.WHITE);
        msg_send.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
        msg_send.setText("SEND");
        msg_send.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(128, 0, 128)));
        //action listener to handle when the button is clicked
        msg_send.addActionListener(evt -> msg_sendActionPerformed(evt));
        gbc.gridx = 1;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.2;
        add(msg_send, gbc);
        //file label
        file_label.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
        file_label.setForeground(new java.awt.Color(128, 0, 128));
        file_label.setText("No file selected");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.6;
        add(file_label, gbc);

        //send file button design
        send_file.setBackground(new java.awt.Color(128, 0, 128));
        send_file.setForeground(java.awt.Color.WHITE);
        send_file.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
        send_file.setText("Send File");
        send_file.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(128, 0, 128)));
        send_file.addActionListener(evt -> send_fileActionPerformed(evt));
        gbc.gridx = 1;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.4;
        add(send_file, gbc);

        select_directory.setBackground(new java.awt.Color(128, 0, 128));
        select_directory.setForeground(java.awt.Color.WHITE);
        select_directory.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
        select_directory.setText("Select Save Directory");
        select_directory.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(128, 0, 128)));
        select_directory.addActionListener(evt -> select_directoryActionPerformed(evt));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(select_directory, gbc);
        
        //adjust window size and layout
        pack();
    }

    //action for the button click for the "send" button
    private void msg_sendActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            String msg = msg_txt.getText();
            dout.writeUTF("msg:" + msg);
            appendToPane(msg_area, "<span style='color:#8400F7;'><b>Alice: </b>" + msg + "</span><br>");
            msg_txt.setText("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //action for the send file button
    private void send_fileActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            file_label.setText(selectedFile.getName());
            try {
                dout.writeUTF("file:" + selectedFile.getName());
                FileInputStream fis = new FileInputStream(selectedFile);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    dout.write(buffer, 0, bytesRead);
                }
                fis.close();
                dout.flush();
                appendToPane(msg_area,
                        "<span style='color:#8400F7;'><b>Alice sent file: </b>" + selectedFile.getName()
                                + "</span><br>");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //pressing the set save directory button
    private void select_directoryActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser directoryChooser = new JFileChooser();
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnValue = directoryChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            saveDirectory = directoryChooser.getSelectedFile();
            appendToPane(msg_area, "<span style='color:#8400F7;'>Save directory set to: "
                    + saveDirectory.getAbsolutePath() + "</span><br>");
        }
    }

    //main method
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new chat_client().setVisible(true));
        try {
            //establish connection w/ the server
            s = new Socket("127.0.0.1", 1201);
            dis = new DataInputStream(s.getInputStream());
            dout = new DataOutputStream(s.getOutputStream());
            String msgin = "";
            //listen for incoming msgs until 'exit'
            while (!msgin.equals("exit")) {
                msgin = dis.readUTF();
                //if msg recieved, display it
                if (msgin.startsWith("msg:")) {
                    appendToPane(msg_area,
                            "<span style='color:#018223;'><b>Bob: </b>" + msgin.substring(4) + "</span><br>");
                } 
                //if file is recieved, save to directory. (default or the chosen)
                else if (msgin.startsWith("file:")) {
                    String fileName = msgin.substring(5);
                    File receivedFile;
                    if (saveDirectory != null) {
                        receivedFile = new File(saveDirectory, "received_" + fileName);
                    } else {
                        receivedFile = new File("received_" + fileName);
                    }
                    FileOutputStream fos = new FileOutputStream(receivedFile);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = dis.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                        if (bytesRead < buffer.length)
                            break; // End of file
                    }
                    fos.close();

                    //display the info of the file
                    appendToPane(msg_area,
                            "<span style='color:#018223;'><b>Bob sent file: </b>" + fileName + "</span><br>" +
                                    "<span style='color:#ff122e;'><i>Saved as: </i>" + receivedFile.getAbsolutePath()
                                    + "</span><br>");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //show the text in msg area
    private static void appendToPane(JTextPane textPane, String text) {
        HTMLEditorKit kit = (HTMLEditorKit) textPane.getEditorKit();
        HTMLDocument doc = (HTMLDocument) textPane.getDocument();
        try {
            kit.insertHTML(doc, doc.getLength(), text, 0, 0, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Variable Declaration: represent the components of thr GUI
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private static javax.swing.JTextPane msg_area;
    private javax.swing.JButton msg_send;
    private javax.swing.JTextField msg_txt;
    private javax.swing.JButton send_file;
    private javax.swing.JButton select_directory;
    private javax.swing.JLabel file_label;
}
